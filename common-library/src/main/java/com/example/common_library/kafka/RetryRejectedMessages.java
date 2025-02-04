package com.example.common_library.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Collections;
import java.util.Properties;

public class RetryRejectedMessages {

    private final KafkaConsumer<String, String> consumer;
    private final KafkaProducer<String, String> producer;

    public RetryRejectedMessages(Properties consumerProps, Properties producerProps) {
        this.consumer = new KafkaConsumer<>(consumerProps);
        this.producer = new KafkaProducer<>(producerProps);
    }

    public void retry(String topic) {
        consumer.subscribe(Collections.singletonList(topic));

        for (ConsumerRecord<String, String> record : consumer.poll(1000)) {
            producer.send(new ProducerRecord<>(topic, record.key(), record.value()));
        }
    }
}
