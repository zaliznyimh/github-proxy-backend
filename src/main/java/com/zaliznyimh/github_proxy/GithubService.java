package com.zaliznyimh.github_proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Service
class GithubService {

    private static final Logger log = LoggerFactory.getLogger(GithubService.class);

    private final GithubClient githubClient;

    GithubService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<RepositoryResponse> getNonForkedUserRepositories(String username) {
        log.info("Started fetching repositories for user with username: {}", username);

        var allUserRepositories = githubClient.getUserRepositories(username);

        log.debug("Found {} repositories for user {}", allUserRepositories.size(), username);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            var futures = allUserRepositories.stream()
                    .filter(repository -> !repository.fork())
                    .map(repo -> CompletableFuture.supplyAsync(
                            () -> mapToRepositoryResponse(repo),
                            executor
                    ))
                    .toList();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
        }
    }

    private RepositoryResponse mapToRepositoryResponse(GitRepository repository) {
        log.info("Fetching branches for repository {}:", repository.name());

        var branches = githubClient.getRepositoryBranches(
                repository.owner().login(),
                repository.name()
        );

        var branchResponses = branches.stream()
                .map(branch -> new BranchResponse(
                        branch.name(),
                        branch.commit().sha()
                ))
                .toList();

        return new RepositoryResponse(
                repository.owner().login(),
                repository.name(),
                branchResponses
        );
    }
}
