package ru.spavlochev.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.spavlochev.order.openapi.api.OrdersApi;
import ru.spavlochev.order.openapi.dto.CreateOrderRequestDto;
import ru.spavlochev.order.openapi.dto.OrderDto;
import ru.spavlochev.order.service.OrderService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrdersRestController implements OrdersApi {

    private final OrderService orderService;

    @Override
    public ResponseEntity<OrderDto> createOrder(UUID userId, CreateOrderRequestDto createOrderRequestDto) {
        var order = orderService.createOrder(userId, createOrderRequestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(order);
    }

    @Override
    public ResponseEntity<OrderDto> getOrder(UUID userId, UUID id) {
        var order = orderService.getOrder(userId, id);
        return ResponseEntity.ok(order);
    }
}
