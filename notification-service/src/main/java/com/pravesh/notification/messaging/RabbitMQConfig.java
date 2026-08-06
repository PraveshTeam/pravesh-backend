package com.pravesh.notification.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String OTP_QUEUE = "pravesh.otp.queue";
<<<<<<< Updated upstream
=======
    public static final String SOS_QUEUE = "pravesh.sos.queue";
    public static final String PAYMENT_RECEIPT_QUEUE = "pravesh.payment-receipt.queue";
>>>>>>> Stashed changes
    public static final String DLX = "pravesh.dlx";

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX);
    }

<<<<<<< Updated upstream
=======
    // ---------------- OTP ----------------

>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
=======

    // ---------------- SOS ----------------

    @Bean
    public Queue sosDeadLetterQueue() {
        return QueueBuilder.durable("pravesh.sos.dlq").build();
    }

    @Bean
    public Binding sosDlqBinding() {
        return BindingBuilder.bind(sosDeadLetterQueue())
                .to(deadLetterExchange())
                .with("sos.dlq");
    }

    @Bean
    public Queue sosQueue() {
        return QueueBuilder.durable(SOS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", "sos.dlq")
                .build();
    }

    // ---------------- Payment Receipt ----------------


    @Bean
    public Queue paymentReceiptDeadLetterQueue() {
        return QueueBuilder.durable("pravesh.payment-receipt.dlq").build();
    }

    @Bean
    public Binding paymentReceiptDlqBinding() {
        return BindingBuilder.bind(paymentReceiptDeadLetterQueue())
                .to(deadLetterExchange())
                .with("payment-receipt.dlq");
    }

    @Bean
    public Queue paymentReceiptQueue() {
        return QueueBuilder.durable(PAYMENT_RECEIPT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", "payment-receipt.dlq")
                .build();
    }
>>>>>>> Stashed changes
}