package ru.yandex.practicum.payment;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

@TestConfiguration
public class PaymentTokenTestConfiguration {

    @Bean
    RSAKey signingKey() throws JOSEException {
        return new RSAKeyGenerator(2048).keyID("test-key").generate();
    }

    @Bean(destroyMethod = "shutdown")
    MockWebServer keyServer(RSAKey signingKey) {
        String keys = new JWKSet(signingKey.toPublicJWK()).toString();
        MockWebServer server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse().setHeader("Content-Type", "application/json").setBody(keys);
            }
        });
        return server;
    }

    @Bean
    DynamicPropertyRegistrar keyServerProperties(MockWebServer keyServer) {
        return registry -> registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> keyServer.url("/certs").toString());
    }
}
