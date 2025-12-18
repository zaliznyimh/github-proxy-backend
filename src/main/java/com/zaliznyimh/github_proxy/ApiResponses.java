package com.zaliznyimh.github_proxy;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

record RepositoryResponse(
        String ownerLogin,
        String repositoryName,
        List<BranchResponse> branches
) {
}

record BranchResponse(
        String name,
        @JsonProperty("lastCommitSha") String lastCommitSHA
) {
}

record ApplicationErrorResponse(
        Integer status,
        String message
) {
}

