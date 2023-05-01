package xyz.krevis.learn.springcloudgw;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"httpbin=http://localhost:${wiremock.server.port}"})
@AutoConfigureWireMock(port = 0) // Starts WireMock on a random port for us.
class ApplicationTests {

    @Autowired
    private WebTestClient webClient;

    @Test
    void contextLoads() {
        // Stubs
        stubFor(get(urlEqualTo("/get"))
                    .willReturn(aResponse().withBody("{\"headers\":{\"Hello\":\"World\"}}")
                                           .withHeader("Content-Type", "application/json")));
        stubFor(get(urlEqualTo("/delay/3"))
                    .willReturn(aResponse().withBody("no fallback")
                                           .withFixedDelay(3000)));

        webClient
            .get().uri("/get")
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$.headers.Hello").isEqualTo("World");

        webClient
            .get().uri("/delay/3")
            .header("Host", "www.circuitbreaker.com")
            .exchange()
            .expectStatus().isOk()
            .expectBody().consumeWith(response -> assertThat(response.getResponseBody()).isEqualTo("fallback".getBytes()));
    }
}
