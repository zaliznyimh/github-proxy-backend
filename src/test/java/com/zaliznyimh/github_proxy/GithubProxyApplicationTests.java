package com.zaliznyimh.github_proxy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@EnableWireMock(@ConfigureWireMock(port = 6789))
class GithubProxyApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("github.api.base-url", () -> "http://localhost:6789");
    }

    @Test
    @DisplayName("Should return only non-forked repositories with branches")
    void shouldReturnNotForkRepositoriesWithBranches() {
        String username = "test-username";

        stubFor(get(urlPathEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [
                                {
                                    "name": "expected-repo",
                                    "fork": false,
                                    "owner": { "login": "test-username" }
                                },
                                {
                                    "name": "forked-repo",
                                    "fork": true,
                                    "owner": { "login": "test-username" }
                                }
                            ]
                            """)));

        stubFor(get(urlPathEqualTo("/repos/" + username + "/expected-repo/branches"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [
                                {
                                    "name": "main",
                                    "commit": { "sha": "qwerty123" }
                                },
                                {
                                    "name": "feature/admin-panel",
                                    "commit": { "sha": "sha12345" }
                                }
                            ]
                            """)));

        var expectedResponse = new RepositoryResponse(
                "test-username",
                "expected-repo",
                List.of(
                        new BranchResponse("main", "qwerty123"),
                        new BranchResponse("feature/admin-panel", "sha12345")
                ));

        webTestClient.get()
                .uri("/api/v1/users/" + username + "/repositories")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RepositoryResponse.class)
                .hasSize(1)
                .contains(expectedResponse);
    }

    @Test
    @DisplayName("Should return 404 response when user not found")
    void shouldReturnNotFoundResponseWhenUserDoesNotExist() {
        String username = "non-existing-username";

        stubFor(get(urlPathEqualTo("/users/" + username + "/repos"))
                .willReturn(notFound()));

        var expectedResponse = new ApplicationErrorResponse(
                404,
                "Not found account for user with username: " + username
        );

        webTestClient.get()
                .uri("/api/v1/users/" + username + "/repositories")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ApplicationErrorResponse.class)
                .isEqualTo(expectedResponse);
    }
}
