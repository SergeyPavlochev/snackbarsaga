package ru.spavlochev.billing.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.spavlochev.billing.openapi.api.UserAccountApi;
import ru.spavlochev.billing.openapi.dto.AccountDto;
import ru.spavlochev.billing.openapi.dto.DepositRequestDto;
import ru.spavlochev.billing.service.AccountService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserAccountController implements UserAccountApi {

    private final AccountService accountService;

    @Override
    public ResponseEntity<AccountDto> depositAccount(UUID userId, UUID accountId, DepositRequestDto depositRequestDto) {
        var amount = new BigDecimal(depositRequestDto.getAmount());
        var currency = depositRequestDto.getCurrency();

        var account = accountService.deposit(userId, accountId, amount, currency);
        return ResponseEntity.ok(account);
    }

    @Override
    public ResponseEntity<AccountDto> getAccount(UUID userId) {
        var account = accountService.getAccount(userId);
        return ResponseEntity.ok(account);
    }
}
