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

import java.util.Map;

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

        	log.info("Pushing SOS to /topic/sos/{}", societyId);
        	messagingTemplate.convertAndSend("/topic/sos/" + societyId, rawPayload);

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