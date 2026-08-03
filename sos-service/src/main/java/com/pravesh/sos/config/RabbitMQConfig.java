package com.pravesh.sos.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String SOS_QUEUE = "pravesh.sos.queue";

    @Bean
    public Queue sosQueue() {
        return new Queue(SOS_QUEUE, true); // durable
    }
}