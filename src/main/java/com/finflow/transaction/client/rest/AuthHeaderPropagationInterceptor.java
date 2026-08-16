package com.finflow.transaction.client.rest;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

/**
 * account-service va user-service ikkalasi ham hisob/foydalanuvchi egaligini o'zining
 * SecurityContext'idagi JWT orqali tekshiradi — shuning uchun kelgan so'rovning tokeni
 * shu yerda forward qilinishi SHART, aks holda tashqi chaqiruv 401/403 bilan qaytadi.
 * Ikkala RestClient (account, user) shu bitta interceptor'ni ishlatadi — logika bitta joyda.
 *
 * Quartz job / Kafka consumer thread'ida HTTP so'rov konteksti yo'q — bunday holatda
 * token forward qilinmaydi (null qaytadi).
 * TODO(javohir): scheduled payment / event-driven oqimlar uchun service-to-service
 * auth strategiyasi kelishilgach shu yerga qo'shiladi.
 */
@Component
public class AuthHeaderPropagationInterceptor implements ClientHttpRequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String token = currentAuthorizationHeader();
        if (token != null) {
            request.getHeaders().set(AUTHORIZATION_HEADER, token);
        }
        return execution.execute(request, body);
    }

    private String currentAuthorizationHeader() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }
        return servletAttributes.getRequest().getHeader(AUTHORIZATION_HEADER);
    }
}