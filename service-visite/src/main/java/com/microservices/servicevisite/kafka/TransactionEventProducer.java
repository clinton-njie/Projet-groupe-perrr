package com.microservices.servicevisite.kafka;

import com.microservices.servicevisite.dto.TransactionTermineeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionEventProducer {

private final KafkaTemplate<String, Object> kafkaTemplate;

private static final String TOPIC = "transaction.terminee";

public void publierTransactionTerminee(TransactionTermineeEvent event) {
kafkaTemplate.send(TOPIC, event);
}
}
