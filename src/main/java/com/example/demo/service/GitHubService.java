package com.example.demo.service;

import com.example.demo.exceptions.GitHubException;
import com.example.demo.model.Branch;
import com.example.demo.model.BranchToDisplay;
import com.example.demo.model.RepoToDisplay;
import com.example.demo.model.Repository;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class GitHubService {

    private final RestClient restClient;

    public GitHubService(RestClient restClient){
        this.restClient = restClient;
    }


    public List<RepoToDisplay> getNonForkRepos(String username){
        @Nullable
        var allRepos = restClient.get().uri("https://api.github.com/users/{username}/repos", username).retrieve().body(Repository[].class);
        if (allRepos == null){
            throw new GitHubException("GitHub user "+username+" does not exist");
        }

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()){
            List<Future<RepoToDisplay>> futures = Arrays.stream(allRepos).filter(repo -> !repo.fork()).
                    map(repo -> executor.submit(() -> {
                        var repoToAdd = new RepoToDisplay(repo.name(),repo.owner().login(),new ArrayList<>());
                        @Nullable
                        Branch[] allBranches = restClient.get().uri("https://api.github.com/repos/{owner}/{repo}/branches",username,repo.name()).retrieve().body(Branch[].class);
                        if (allBranches == null){
                            throw new GitHubException("Failed to fetch branches of "+repo.name()+" repository");
                        }
                        for (Branch branch : allBranches){
                            repoToAdd.branches().add(new BranchToDisplay(branch.name(),branch.commit().sha()));
                        }
                        return repoToAdd;
                    })).toList();
            List<RepoToDisplay> toReturn = new ArrayList<>();

            for (Future<RepoToDisplay> future : futures) {
                try {
                    toReturn.add(future.get());
                } catch (Exception e) {
                    throw new GitHubException("Failed to fetch repositories");
                }
            }
            return toReturn;
        }

    }
}
