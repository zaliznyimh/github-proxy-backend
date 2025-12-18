package com.zaliznyimh.github_proxy;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
class GithubController {

    private final GithubService githubService;

    GithubController(GithubService githubService) {
        this.githubService = githubService;
    }

    @GetMapping("/users/{username}/repositories")
    ResponseEntity<List<RepositoryResponse>> getNonForkedRepositories(
            @PathVariable String username
    ) {
        var response = githubService.getNonForkedUserRepositories(username);
        return ResponseEntity.ok().body(response);
    }
}
