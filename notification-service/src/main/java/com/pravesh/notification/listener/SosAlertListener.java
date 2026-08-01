package com.pravesh.notification.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pravesh.notification.feign.EmergencyContactResponse;
import com.pravesh.notification.feign.UserFeignClient;
import com.pravesh.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SosAlertListener {

    private static final Logger log = LoggerFactory.getLogger(SosAlertListener.class);
    private final SmsService smsService;
    private final UserFeignClient userFeignClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    @RabbitListener(queues = "pravesh.sos.queue")
    public void handleSosAlert(String rawPayload) {
        try {
            JsonNode node = mapper.readTree(rawPayload);
            String eventType = node.path("eventType").asText("");
            Long societyId = node.path("societyId").asLong();
            long residentUserId = node.path("residentUserId").asLong(0);

            // Existing: guard/admin live banner, scoped to the whole society.
            log.info("Pushing SOS to /topic/sos/{}", societyId);
            messagingTemplate.convertAndSend("/topic/sos/" + societyId, rawPayload);

            // NEW: the raising resident's own private live-status topic. Same
            // payload, different destination -- the resident's frontend only
            // ever subscribes to their OWN userId's topic, so they see their
            // own alert's status change in real time without being able to see
            // (or being shown) every other alert in the society, unlike guards/admins.
            //
            // NOTE: this topic is protected only by the resident needing to know
            // their own userId (which they obviously do) -- the /ws handshake
            // itself is unauthenticated by design (same as the existing
            // /topic/sos/{societyId} banner), so this is "obscurity", not real
            // per-topic authorization. Consistent with the rest of this app's
            // WebSocket layer; flagged here so it's not a silent gap.
            if (residentUserId > 0) {
                log.info("Pushing SOS status to /topic/sos-status/{}", residentUserId);
                messagingTemplate.convertAndSend("/topic/sos-status/" + residentUserId, rawPayload);
            }

            if (!"SOS_RAISED".equals(eventType)) {
                return; // status updates don't need a fresh SMS dispatch
            }

            String residentName = node.path("residentName").asText("A resident");
            String flatNumber   = node.path("flatNumber").asText("N/A");
            String category     = node.path("category").asText("EMERGENCY");

            EmergencyContactResponse contact = userFeignClient.getEmergencyContact(societyId).data();
            if (contact == null || "NONE".equals(contact.role()) || contact.phone() == null) {
                log.warn("No guard or admin available to notify for SOS alert — society {}, resident {}",
                        societyId, residentName);
                return;
            }

            String body = "SOS ALERT (" + category + ") from " + residentName
                    + ", Flat " + flatNumber + ". Respond immediately via Pravesh.";
            smsService.sendSms(contact.phone(), body);
            log.info("SOS SMS sent to {} ({}) for alert from {} at flat {}",
                    contact.name(), contact.role(), residentName, flatNumber);

        } catch (Exception e) {
            log.error("Failed to process SOS alert message: {}", e.getMessage(), e);
        }
    }
}
