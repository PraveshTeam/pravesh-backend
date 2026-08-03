package com.pravesh.payment.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PAYMENT_RECEIPT_QUEUE = "pravesh.payment-receipt.queue";

    @Bean
    public Queue paymentReceiptQueue() {
        return new Queue(PAYMENT_RECEIPT_QUEUE, true); // durable
    }
}
