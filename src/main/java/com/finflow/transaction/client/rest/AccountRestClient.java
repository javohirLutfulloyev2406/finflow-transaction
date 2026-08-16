package com.finflow.transaction.client.rest;

import com.finflow.transaction.client.AccountClient;
import com.finflow.transaction.client.dto.AccountBalance;
import com.finflow.transaction.client.dto.AccountOperationResult;
import com.finflow.transaction.client.rest.dto.AccountServiceAccountResponse;
import com.finflow.transaction.client.rest.dto.AccountServiceBalanceResponse;
import com.finflow.transaction.client.rest.dto.AmountRequest;
import com.finflow.transaction.domain.vo.Money;
import com.finflow.transaction.enums.Currency;
import com.finflow.transaction.exception.AccountServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * account-service bilan hozirgi (haqiqiy) kontraktga mos RestClient implementatsiyasi.
 *
 * DIQQAT — quyidagi qismlar account-service tomonida hali aniqlashtirilmagan,
 * kontrakt kelishilgach shu yerlar yangilanadi:
 *  - operationId: account-service javobida umuman qaytmaydi (faqat ichki audit
 *    yozuv bor, API orqali chiqmaydi) — hozircha null.
 *  - Xato kodi (INSUFFICIENT_FUNDS, ACCOUNT_NOT_FOUND, ...): account-service
 *    GlobalExceptionHandler faqat oddiy matn (plain text, JSON emas) qaytaradi
 *    va har doim 400 status beradi — kod alohida ajratib bo'lmaydi, shuning
 *    uchun message() ichiga xom matn qo'yiladi.
 *  - Auth: hozircha service-to-service token/header yo'q, hech narsa yuborilmaydi.
 */
@Component
@RequiredArgsConstructor
public class AccountRestClient implements AccountClient {

    private final RestClient accountServiceRestClient;

    @Override
    public AccountBalance getBalance(UUID accountId) {
        try {
            // Read-only, side-effect'siz — shuning uchun retry xavfsiz (RetrySupport javadoc'i).
            return RetrySupport.withRetry("getBalance", 3, 200, () -> {
                AccountServiceAccountResponse response = accountServiceRestClient.get()
                        .uri("/api/v1/accounts/{id}", accountId)
                        .retrieve()
                        .body(AccountServiceAccountResponse.class);

                Currency currency = Currency.valueOf(response.currency());
                return new AccountBalance(response.id(), Money.of(response.balance(), currency));
            });
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw new AccountServiceUnavailableException();
        }
        // TODO: HttpClientErrorException (404/400) uchun xatti-harakat account-service
        // kontrakti aniqlashgach belgilanadi (masalan ACCOUNT_NOT_FOUND).
    }

    @Override
    public AccountOperationResult debit(UUID accountId, Money amount) {
        return execute("/api/v1/accounts/withdraw/{id}", accountId, amount);
    }

    @Override
    public AccountOperationResult credit(UUID accountId, Money amount) {
        return execute("/api/v1/accounts/deposit/{id}", accountId, amount);
    }

    private AccountOperationResult execute(String path, UUID accountId, Money amount) {
        try {
            accountServiceRestClient.post()
                    .uri(path, accountId)
                    .body(new AmountRequest(amount.getAmount()))
                    .retrieve()
                    .body(AccountServiceBalanceResponse.class);

            return new AccountOperationResult(true, null, "OK");
        } catch (HttpClientErrorException e) {
            return new AccountOperationResult(false, null, e.getResponseBodyAsString());
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw new AccountServiceUnavailableException();
        }
    }
}
