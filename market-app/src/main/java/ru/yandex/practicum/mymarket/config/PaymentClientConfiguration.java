package ru.yandex.practicum.mymarket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.mymarket.client.ApiClient;
import ru.yandex.practicum.mymarket.client.api.PaymentsApi;

@Configuration
public class PaymentClientConfiguration {

    @Bean
    public PaymentsApi paymentsApi(@Value("${payment.service.url}") String paymentServiceUrl,
                                   ReactiveClientRegistrationRepository registrations,
                                   ReactiveOAuth2AuthorizedClientService authorizedClients) {
        var manager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(registrations, authorizedClients);
        var authorization = OAuth2AuthorizeRequest.withClientRegistrationId("market-app").principal("market-app").build();
        WebClient webClient = ApiClient.buildWebClientBuilder()
                .filter((request, next) -> manager.authorize(authorization)
                        .flatMap(client -> next.exchange(ClientRequest.from(request)
                                .headers(headers -> headers.setBearerAuth(client.getAccessToken().getTokenValue()))
                                .build())))
                .build();
        ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath(paymentServiceUrl);
        return new PaymentsApi(apiClient);
    }
}
