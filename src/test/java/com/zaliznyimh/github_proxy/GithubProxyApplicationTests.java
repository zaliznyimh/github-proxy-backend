package com.zaliznyimh.github_proxy;

import org.apache.commons.lang3.time.StopWatch;
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
import static org.assertj.core.api.Assertions.assertThat;

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
                        .withFixedDelay(1000)
                        .withBody("""
                            [
                                {
                                    "name": "expected-repo",
                                    "fork": false,
                                    "owner": { "login": "test-username" }
                                },
                                {
                                    "name": "second-expected-repo",
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
                        .withFixedDelay(1000)
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

        stubFor(get(urlPathEqualTo("/repos/" + username + "/second-expected-repo/branches"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(1000)
                        .withBody("""
                            [
                                {
                                    "name": "dev",
                                    "commit": { "sha": "ktw54321" }
                                },
                                {
                                    "name": "feature/virtual-threads",
                                    "commit": { "sha": "kra67585" }
                                }
                            ]
                            """)));

        var expectedResponse = List.of(
                new RepositoryResponse("test-username", "expected-repo", List.of(
                        new BranchResponse("main", "qwerty123"),
                        new BranchResponse("feature/admin-panel", "sha12345")
                )),
                new RepositoryResponse("test-username", "second-expected-repo", List.of(
                        new BranchResponse("dev", "ktw54321"),
                        new BranchResponse("feature/virtual-threads", "kra67585")
                )));

        StopWatch stopwatch = new StopWatch();
        stopwatch.start();

        webTestClient.get()
                .uri("/api/v1/users/{username}/repositories", username)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RepositoryResponse.class)
                .hasSize(2)
                .isEqualTo(expectedResponse);

        stopwatch.stop();
        long responseTime = stopwatch.getTime();

        verify(1, getRequestedFor(urlPathEqualTo("/users/test-username/repos")));
        verify(1, getRequestedFor(urlPathEqualTo("/repos/test-username/expected-repo/branches")));
        verify(1, getRequestedFor(urlPathEqualTo("/repos/test-username/second-expected-repo/branches")));
        verify(0, getRequestedFor(urlPathEqualTo("/repos/test-username/forked-repo/branches")));

        assertThat(responseTime)
                .isGreaterThanOrEqualTo(2000L)
                .isLessThanOrEqualTo(3000L);
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
