package com.zaliznyimh.github_proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
class GithubClient {

    private final RestClient restClient;

    GithubClient(
            RestClient.Builder builder,
            @Value("${github.api.base-url}") String githubBaseUrl
    ) {
        this.restClient = builder
                .baseUrl(githubBaseUrl)
                .defaultHeader("User-Agent", "GitHub-Repositories-Viewer")
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();
    }

    List<GitRepository> getUserRepositories(String username) {
        try {
            return restClient
                    .get()
                    .uri("/users/{username}/repos",username)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() { });
        } catch (HttpClientErrorException.NotFound e) {
            throw new UserNotFoundException("Not found account for user with username: " + username);
        }
    }

    List<GitBranch> getRepositoryBranches(String owner, String repo) {
        return restClient.get()
                .uri("/repos/{owner}/{repo}/branches", owner, repo)
                .retrieve()
                .body(new ParameterizedTypeReference<>() { });
    }
}
