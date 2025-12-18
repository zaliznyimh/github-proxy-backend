package com.zaliznyimh.github_proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

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

        return allUserRepositories.stream()
                .filter(repository -> !repository.fork())
                .map(this::mapToRepositoryResponse)
                .toList();
    }

    private RepositoryResponse mapToRepositoryResponse(GitRepository repository) {
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
