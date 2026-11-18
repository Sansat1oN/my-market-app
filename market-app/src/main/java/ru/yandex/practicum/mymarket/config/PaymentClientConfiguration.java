package ru.yandex.practicum.mymarket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.mymarket.client.ApiClient;
import ru.yandex.practicum.mymarket.client.api.PaymentsApi;

@Configuration
public class PaymentClientConfiguration {

    @Bean
    public PaymentsApi paymentsApi(@Value("${payment.service.url}") String paymentServiceUrl) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(paymentServiceUrl);
        return new PaymentsApi(apiClient);
    }
}
