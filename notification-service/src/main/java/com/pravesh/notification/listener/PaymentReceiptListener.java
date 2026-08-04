package com.pravesh.notification.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pravesh.notification.document.Notification;
import com.pravesh.notification.feign.UserContactResponse;
import com.pravesh.notification.feign.UserFeignClient;
import com.pravesh.notification.repository.NotificationRepository;
import com.pravesh.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentReceiptListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentReceiptListener.class);

    private final EmailService emailService;
    private final UserFeignClient userFeignClient;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @RabbitListener(queues = "pravesh.payment-receipt.queue")
    public void handlePaymentReceipt(String rawPayload) {
        try {
            JsonNode node = mapper.readTree(rawPayload);

            Long paymentOrderId = node.path("paymentOrderId").asLong();
            Long residentId = node.path("residentId").asLong();
            double amount = node.path("amount").asDouble();
            String purpose = node.path("purpose").asText("PAYMENT");
            String paidAt = node.path("paidAt").asText("");

            // Payment events don't carry their own correlationId (unlike OTP/SOS),
            // so we derive one from the payment order id -- stable and unique per
            // payment, which is all idempotency needs here. RabbitMQ's at-least-once
            // delivery can redeliver this message; without this check, a redelivery
            // would email/SMS the resident twice for the same payment.
            String correlationId = "payment-" + paymentOrderId;
            if (notificationRepository.existsBySourceEventCorrelationId(correlationId)) {
                log.info("Payment receipt for order {} already processed -- skipping (idempotent no-op)",
                        paymentOrderId);
                return;
            }

            UserContactResponse contact = userFeignClient.getContact(residentId).data();
            if (contact == null) {
                log.warn("Could not resolve resident {} for payment receipt (order {}) -- " +
                        "notification skipped, but the payment itself is already safely PAID", residentId, paymentOrderId);
                return;
            }

            String title = "Payment Receipt";
            String message = String.format(
                    "Your %s payment of Rs.%.2f was received successfully.", purpose, amount);

            String formattedDate = formatPaidAt(paidAt);

            // Payment receipts go by EMAIL ONLY -- a payment confirmation isn't
            // the same urgency class as an OTP or SOS alert, so it doesn't need
            // the SMS channel's near-instant delivery; email is sufficient and
            // gives a naturally-storable receipt in the resident's inbox.
            if (contact.email() != null) {
                String htmlBody = buildReceiptEmail(contact.name(), purpose, amount, formattedDate, paymentOrderId);
                try {
                    emailService.sendHtmlEmail(contact.email(), title, htmlBody);
                } catch (Exception e) {
                    log.warn("Failed to send payment receipt email to {}: {}", contact.email(), e.getMessage());
                }
            } else {
                log.warn("Resident {} has no email on file -- payment receipt for order {} not sent anywhere",
                        residentId, paymentOrderId);
            }

            Notification notification = Notification.builder()
                    .userId(residentId)
                    .type("PAYMENT_RECEIPT")
                    .channel(List.of("EMAIL"))
                    .title(title)
                    .message(message)
                    .sourceEvent(Notification.SourceEvent.builder()
                            .eventType("PAYMENT_RECEIPT_READY")
                            .correlationId(correlationId)
                            .payload(Map.of(
                                    "paymentOrderId", paymentOrderId,
                                    "amount", amount,
                                    "purpose", purpose))
                            .build())
                    .isRead(false)
                    .createdAt(Instant.now())
                    .build();
            notificationRepository.save(notification);

            log.info("Payment receipt delivered to {} for order {} ({} Rs.{})",
                    contact.name(), paymentOrderId, purpose, amount);

        } catch (Exception e) {
            log.error("Failed to process payment-receipt event: {}", e.getMessage(), e);
        }
    }

    /** paidAt arrives as a raw LocalDateTime.toString(), e.g. "2026-07-31T23:33:31.347406700" -- turn it into something a resident actually wants to read. */
    private String formatPaidAt(String rawIso) {
        try {
            LocalDateTime dt = LocalDateTime.parse(rawIso);
            return dt.format(DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a"));
        } catch (Exception e) {
            return rawIso; // fall back to raw value rather than blow up the whole notification
        }
    }

    /**
     * Inline-styled HTML receipt. Email clients strip <style> blocks and external
     * stylesheets unpredictably, so every rule here is inline -- the only reliable
     * way to guarantee consistent rendering across Gmail, Outlook, and mobile mail apps.
     */
    private String buildReceiptEmail(String residentName, String purpose, double amount,
                                      String formattedDate, Long paymentOrderId) {
        String purposeLabel = switch (purpose) {
            case "MAINTENANCE" -> "Maintenance";
            case "EVENT" -> "Event Fee";
            case "ACTIVITY" -> "Activity Fee";
            case "TRIP" -> "Trip Fee";
            default -> purpose;
        };

        return "<!DOCTYPE html>"
            + "<html><body style=\"margin:0;padding:0;background-color:#f4f5f7;font-family:'Segoe UI',Helvetica,Arial,sans-serif;\">"
            + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#f4f5f7;padding:32px 0;\">"
            + "<tr><td align=\"center\">"
            + "<table role=\"presentation\" width=\"560\" cellpadding=\"0\" cellspacing=\"0\" "
            +   "style=\"background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,0.06);\">"

            // Header banner -- dark navy gradient, centered white title
            + "<tr><td style=\"background:linear-gradient(135deg,#1e293b,#0f172a);padding:26px 32px;text-align:center;\">"
            +   "<span style=\"color:#ffffff;font-size:20px;font-weight:700;\">Payment Received</span>"
            + "</td></tr>"

            // Body
            + "<tr><td style=\"padding:32px;\">"

            +   "<p style=\"margin:0 0 4px 0;color:#111827;font-size:15px;\">Hi <b>" + residentName + "</b>,</p>"
            +   "<p style=\"margin:0 0 20px 0;color:#111827;font-size:15px;line-height:1.5;\">"
            +     "Your <b>" + purposeLabel + "</b> payment has been "
            +     "<b style=\"color:#16a34a;\">received</b> successfully. This email confirms the payment "
            +     "against your Pravesh account.</p>"

            // Details box -- matches the existing Relocation-Approved email's grey card style
            +   "<div style=\"background-color:#f8fafc;border-radius:8px;padding:4px 20px;\">"
            +   "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">"
            +     detailRow("Amount Paid", "<span style=\"color:#c2410c;font-size:16px;\">&#8377;" + String.format("%.2f", amount) + "</span>")
            +     detailRow("Payment For", purposeLabel)
            +     detailRow("Receipt No.", "#PVS-" + paymentOrderId)
            +     detailRowLast("Paid On", formattedDate)
            +   "</table>"
            +   "</div>"

            +   "<p style=\"margin:20px 0 0 0;color:#6b7280;font-size:13px;line-height:1.5;\">"
            +     "Keep this email for your records. If you believe this payment was made in error, "
            +     "please contact your society admin through the Pravesh app.</p>"

            // CTA button
            +   "<div style=\"text-align:center;margin-top:24px;\">"
            +     "<a href=\"#\" style=\"background:linear-gradient(135deg,#f97316,#ea580c);color:#ffffff;"
            +       "text-decoration:none;font-weight:700;font-size:14px;padding:12px 28px;border-radius:8px;"
            +       "display:inline-block;\">View Payment History</a>"
            +   "</div>"

            + "</td></tr>"

            // Footer bar -- matches existing email's plain footer style
            + "<tr><td style=\"background-color:#f8fafc;padding:16px 32px;text-align:center;border-top:1px solid #e5e7eb;\">"
            +   "<span style=\"color:#6b7280;font-size:12px;\">Pravesh &mdash; Visitor Access Control</span>"
            + "</td></tr>"

            + "</table>"
            + "</td></tr>"
            + "</table>"
            + "</body></html>";
    }

    private String detailRow(String label, String value) {
        return "<tr>"
            + "<td style=\"padding:8px 0;color:#6b7280;font-size:14px;border-bottom:1px dashed #e5e7eb;\">" + label + "</td>"
            + "<td style=\"padding:8px 0;text-align:right;font-weight:700;color:#111827;font-size:14px;border-bottom:1px dashed #e5e7eb;\">" + value + "</td>"
            + "</tr>";
    }

    private String detailRowLast(String label, String value) {
        return "<tr>"
            + "<td style=\"padding:8px 0;color:#6b7280;font-size:14px;\">" + label + "</td>"
            + "<td style=\"padding:8px 0;text-align:right;font-weight:700;color:#111827;font-size:14px;\">" + value + "</td>"
            + "</tr>";
    }
}