package ru.yandex.practicum.mymarket.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import ru.yandex.practicum.mymarket.client.ApiClient;
import ru.yandex.practicum.mymarket.client.api.PaymentsApi;

import java.time.Duration;

@Configuration
public class PaymentClientConfiguration {

    @Bean
    public PaymentsApi paymentsApi(@Value("${payment.service.url}") String paymentServiceUrl,
                                   ReactiveClientRegistrationRepository registrations,
                                   ReactiveOAuth2AuthorizedClientService authorizedClients) {
        var manager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(registrations, authorizedClients);
        var authorization = OAuth2AuthorizeRequest.withClientRegistrationId("market-app").principal("market-app").build();
        Duration connectTimeout = Duration.ofSeconds(3);
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout.toMillis())
                .resolver(resolver -> resolver.queryTimeout(connectTimeout).maxQueriesPerResolve(2))
                .disableRetry(true);
        WebClient webClient = ApiClient.buildWebClientBuilder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((request, next) -> manager.authorize(authorization)
                        .timeout(Duration.ofSeconds(5))
                        .flatMap(client -> next.exchange(ClientRequest.from(request)
                                .headers(headers -> headers.setBearerAuth(client.getAccessToken().getTokenValue()))
                                .build())))
                .build();
        ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath(paymentServiceUrl);
        return new PaymentsApi(apiClient);
    }
}
