package com.neu.info7255.bigdata_proj.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class KafkaPub implements PublishService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.name}")
    private String topic;

    @Override
    public void publish(String operation, String message) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, operation, message);

        ListenableFuture<SendResult<String, String>> sendResultListenableFuture = kafkaTemplate.send(producerRecord);

        sendResultListenableFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>(){

            @Override
            public void onSuccess(SendResult<String, String> stringStringSendResult) {
                logger.info("Sent message=[" + message +
                        "] with offset=[" + stringStringSendResult.getRecordMetadata().offset() + "]");
            }

            @Override
            public void onFailure(Throwable throwable) {
                logger.error("Unable to send message=["
                        + message + "] due to : " + throwable.getMessage());
            }
        });
    }
}
