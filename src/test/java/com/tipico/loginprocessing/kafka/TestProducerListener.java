package com.tipico.loginprocessing.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.ProducerListener;

@Getter
public class TestProducerListener implements ProducerListener<String, Object> {
  // List to keep track of sent events
  private final Map<String, Map<String, List<Object>>> eventsSent = new HashMap<>();
  private final Map<String, Map<String, List<Object>>> eventsFailedToSent = new HashMap<>();

  @Override
  public void onSuccess(
      ProducerRecord<String, Object> producerRecord, RecordMetadata recordMetadata) {
    var topic = recordMetadata.topic();
    this.eventsSent.putIfAbsent(topic, new HashMap<>());
    this.eventsSent.get(topic).putIfAbsent(producerRecord.key(), new ArrayList<>());
    this.eventsSent.get(topic).get(producerRecord.key()).add(producerRecord.value());
  }

  @Override
  public void onError(
      ProducerRecord<String, Object> producerRecord,
      RecordMetadata recordMetadata,
      Exception exception) {
    assert recordMetadata != null;
    var topic = recordMetadata.topic();
    this.eventsFailedToSent.putIfAbsent(topic, new HashMap<>());
    this.eventsFailedToSent.get(topic).putIfAbsent(producerRecord.key(), new ArrayList<>());
    this.eventsFailedToSent.get(topic).get(producerRecord.key()).add(producerRecord.value());
  }
}
