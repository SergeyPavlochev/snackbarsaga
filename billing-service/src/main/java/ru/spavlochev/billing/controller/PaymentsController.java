package ru.spavlochev.billing.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.spavlochev.billing.openapi.api.PaymentsApi;
import ru.spavlochev.billing.openapi.dto.PaymentCancelRequestDto;
import ru.spavlochev.billing.openapi.dto.PaymentCancelResponseDto;
import ru.spavlochev.billing.openapi.dto.PaymentConfirmRequestDto;
import ru.spavlochev.billing.openapi.dto.PaymentConfirmResponseDto;
import ru.spavlochev.billing.openapi.dto.PaymentReserveRequestDto;
import ru.spavlochev.billing.openapi.dto.PaymentReserveResponseDto;
import ru.spavlochev.billing.service.PaymentsService;

@RestController
@RequiredArgsConstructor
public class PaymentsController implements PaymentsApi {

    private final PaymentsService paymentsService;

    @Override
    public ResponseEntity<PaymentReserveResponseDto> reserve(PaymentReserveRequestDto dto) {
        paymentsService.reserve(dto.getUserId(), dto.getOrderId(), dto.getAmount(), dto.getCurrency());
        return ResponseEntity.ok(new PaymentReserveResponseDto("reserved"));
    }

    @Override
    public ResponseEntity<PaymentConfirmResponseDto> confirm(PaymentConfirmRequestDto dto) {
        paymentsService.confirm(dto.getOrderId());
        return ResponseEntity.ok(new PaymentConfirmResponseDto("confirmed"));
    }

    @Override
    public ResponseEntity<PaymentCancelResponseDto> cancel(PaymentCancelRequestDto dto) {
        paymentsService.cancel(dto.getOrderId());
        return ResponseEntity.ok(new PaymentCancelResponseDto("cancelled"));
    }
}
