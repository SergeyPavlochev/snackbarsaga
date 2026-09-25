package ru.spavlochev.billing.service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.spavlochev.billing.entity.OutboxEvent;
import ru.spavlochev.billing.repository.OutboxEventRepository;
import ru.spavlochev.snackbar.events.v1.EventEnvelope;
import ru.spavlochev.snackbar.events.v1.Money;
import ru.spavlochev.snackbar.events.v1.OrderPaymentCompletedEvent;
import ru.spavlochev.snackbar.events.v1.OrderPaymentFailedEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private static final String PAYMENTS_TOPIC = "payments";
    private static final int BATCH_SIZE = 50;

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void publishPendingEvents() {
        // Считываем строки в транзакции и удерживаем блокировку до окончания обработки всех событий в пачке
        List<OutboxEvent> events = outboxRepository
                .findByStatusOrderByCreatedAtAsc(OutboxEvent.OutboxStatus.PENDING, PageRequest.of(0, BATCH_SIZE));

        for (OutboxEvent event : events) {
            try {
                publishEvent(event);
                markAsPublished(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event: eventId={}, type={}",
                        event.getEventId(), event.getEventType(), e);
                markAsFailed(event);
            }
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupOldEvents() {
        int deleted = outboxRepository.cleanupOldPublishedEvents(LocalDateTime.now().minusDays(1));
        if (deleted > 0) {
            log.info("Cleaned up {} old outbox events", deleted);
        }
    }

    private void publishEvent(OutboxEvent event) {
        EventEnvelope envelope = buildEnvelope(event);
        String key = (String) event.getPayload().get("userId");
        SendResult<String, EventEnvelope> result = kafkaTemplate.send(PAYMENTS_TOPIC, key, envelope).join();
        log.info("Event published: eventId={}, type={}, topic={}, partition={}, offset={}",
                envelope.getEventId(),
                envelope.getEventType(),
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
    }

    private EventEnvelope buildEnvelope(OutboxEvent event) {
        EventEnvelope.Builder builder = EventEnvelope.newBuilder()
                .setEventId(event.getEventId().toString())
                .setEventType(event.getEventType())
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build())
                .setCorrelationId(event.getAggregateId().toString())
                .setSource("billing-service");

        Map<String, Object> p = event.getPayload();
        if ("OrderPaymentCompleted".equals(event.getEventType())) {
            builder.setOrderPaymentCompleted(OrderPaymentCompletedEvent.newBuilder()
                    .setOrderId((String) p.get("orderId"))
                    .setUserId((String) p.get("userId"))
                    .setAmount(Money.newBuilder()
                            .setAmount((String) p.get("amount"))
                            .setCurrency((String) p.get("currency"))
                            .build())
                    .build());
        } else if ("OrderPaymentFailed".equals(event.getEventType())) {
            builder.setOrderPaymentFailed(OrderPaymentFailedEvent.newBuilder()
                    .setOrderId((String) p.get("orderId"))
                    .setUserId((String) p.get("userId"))
                    .setAmount(Money.newBuilder()
                            .setAmount((String) p.get("amount"))
                            .setCurrency((String) p.get("currency"))
                            .build())
                    .setReason((String) p.get("reason"))
                    .build());
        }

        return builder.build();
    }

    private void markAsPublished(OutboxEvent event) {
        event.setStatus(OutboxEvent.OutboxStatus.PUBLISHED);
        event.setPublishedAt(java.time.LocalDateTime.now());
        outboxRepository.save(event);
    }

    private void markAsFailed(OutboxEvent event) {
        event.setStatus(OutboxEvent.OutboxStatus.FAILED);
        outboxRepository.save(event);
    }
}

