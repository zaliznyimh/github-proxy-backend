package com.zaliznyimh.github_proxy;

record GitBranch(
        String name,
        GitCommit commit
) {
}

record GitCommit(
        String sha
) {
}

record GitOwner(
        String login
) {
}

record GitRepository(
        String name,
        Boolean fork,
        GitOwner owner
) {
}
