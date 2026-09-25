package ru.spavlochev.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.spavlochev.notification.openapi.api.NotificationsApi;
import ru.spavlochev.notification.openapi.dto.NotificationDto;
import ru.spavlochev.notification.service.NotificationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NotificationsRestController implements NotificationsApi {

    private final NotificationService notificationService;

    @Override
    public ResponseEntity<List<NotificationDto>> getUserNotifications(UUID userId, Integer limit) {
        var notifications = notificationService.getNotifications(userId, limit);
        return ResponseEntity.ok(notifications);
    }
}
