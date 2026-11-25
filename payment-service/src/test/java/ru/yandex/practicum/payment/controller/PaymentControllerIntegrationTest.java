package ru.yandex.practicum.payment.controller;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.payment.AbstractTest;
import ru.yandex.practicum.payment.domain.Balance;
import ru.yandex.practicum.payment.domain.PaymentRequest;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentControllerIntegrationTest extends AbstractTest {

    private static final String ISSUER = "http://localhost:8082/realms/market";

    @Autowired
    private RSAKey signingKey;

    @Test
    void createPayment_subtractsAmountOnlyFromBuyersBalance() throws JOSEException {
        long before = currentBalance("buyer1");
        long otherBalance = currentBalance("buyer2");

        marketClient().post()
                .uri("/payments?username=buyer1")
                .bodyValue(new PaymentRequest(100L))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(before - 100);

        assertEquals(before - 100, currentBalance("buyer1"));
        assertEquals(otherBalance, currentBalance("buyer2"));
    }

    @Test
    void createPayment_moreThanBalance_returnsConflictAndKeepsBalance() throws JOSEException {
        long before = currentBalance("buyer1");

        marketClient().post()
                .uri("/payments?username=buyer1")
                .bodyValue(new PaymentRequest(before + 1))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").value(message -> assertTrue(((String) message).startsWith("Недостаточно средств")));

        assertEquals(before, currentBalance("buyer1"));
    }

    @Test
    void missingToken_isUnauthorized() {
        assertPaymentAccessDenied(webTestClient, HttpStatus.UNAUTHORIZED);
    }

    @Test
    void malformedToken_isUnauthorized() {
        assertPaymentAccessDenied(clientWithToken("invalid"), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void expiredTokenIsUnauthorized() throws JOSEException {
        String expired = signedToken("market-app", "payment-service", ISSUER, Instant.now().minusSeconds(300));
        assertPaymentAccessDenied(clientWithToken(expired), HttpStatus.UNAUTHORIZED);
    }

    @ParameterizedTest
    @CsvSource({
            "another-client, payment-service, http://localhost:8082/realms/market, 403",
            "market-app, another-service, http://localhost:8082/realms/market, 401",
            "market-app, payment-service, https://other.example, 401"
    })
    void signedTokenWithWrongClaims_isRejected(String client, String audience, String issuer, int status)
            throws JOSEException {
        assertPaymentAccessDenied(clientWithToken(signedToken(client, audience, issuer)), HttpStatus.valueOf(status));
    }

    private void assertPaymentAccessDenied(WebTestClient client, HttpStatus status) {
        client.get().uri("/balance?username=buyer1")
                .exchange().expectStatus().isEqualTo(status);
        client.post().uri("/payments?username=buyer1").bodyValue(new PaymentRequest(1L))
                .exchange().expectStatus().isEqualTo(status);
    }

    private WebTestClient marketClient() throws JOSEException {
        return clientWithToken(signedToken("market-app", "payment-service", ISSUER));
    }

    private WebTestClient clientWithToken(String token) {
        return webTestClient.mutate().defaultHeaders(headers -> headers.setBearerAuth(token)).build();
    }

    private String signedToken(String client, String audience, String issuer) throws JOSEException {
        return signedToken(client, audience, issuer, Instant.now().plusSeconds(300));
    }

    private String signedToken(String client, String audience, String issuer, Instant expiresAt) throws JOSEException {
        var claims = new JWTClaimsSet.Builder().subject("service-account-" + client).claim("azp", client)
                .issuer(issuer).audience(audience).expirationTime(Date.from(expiresAt)).build();
        var header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKeyID()).build();
        var jwt = new SignedJWT(header, claims);
        jwt.sign(new RSASSASigner(signingKey));
        return jwt.serialize();
    }

    private long currentBalance(String username) throws JOSEException {
        return marketClient().get()
                .uri("/balance?username={username}", username)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Balance.class)
                .returnResult()
                .getResponseBody()
                .getBalance();
    }
}
