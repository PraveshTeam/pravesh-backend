package com.pravesh.user.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String OTP_QUEUE = "pravesh.otp.queue";
    public static final String DLX = "pravesh.dlx";

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX);
    }

    @Bean
    public Queue otpDeadLetterQueue() {
        return QueueBuilder.durable("pravesh.otp.dlq").build();
    }

    @Bean
    public Binding otpDlqBinding() {
        return BindingBuilder.bind(otpDeadLetterQueue())
                .to(deadLetterExchange())
                .with("otp.dlq");
    }

    @Bean
    public Queue otpQueue() {
        return QueueBuilder.durable(OTP_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", "otp.dlq")
                .build();
    }
}