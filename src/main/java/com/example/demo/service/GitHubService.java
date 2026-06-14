package com.example.demo.service;

import com.example.demo.exceptions.GitHubException;
import com.example.demo.model.Branch;
import com.example.demo.model.BranchToDisplay;
import com.example.demo.model.RepoToDisplay;
import com.example.demo.model.Repository;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpHeaders;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@Service
public class GitHubService {


    private static final String API_URL = "https://api.github.com/";
    private final RestClient restClient;

    public GitHubService(RestClient restClient){
        this.restClient = restClient;
    }


    public List<RepoToDisplay> getNonForkRepos(String username, HttpServletRequest request){

        Consumer<HttpHeaders> headers = getHeaders(request);


        var allRepos = restClient.get().uri(API_URL+"users/{username}/repos", username).headers(headers).retrieve().body(Repository[].class);

        if (allRepos == null){
            throw new GitHubException("GitHub user "+username+" does not exist");
        }


        try (var executor = Executors.newVirtualThreadPerTaskExecutor()){
            List<Future<RepoToDisplay>> futures = Arrays.stream(allRepos).filter(repo -> !repo.fork()).
                    map(repo -> executor.submit(() -> {
                        var repoToAdd = new RepoToDisplay(repo.name(),repo.owner().login(),new ArrayList<>());

                        Branch[] allBranches = restClient.get().uri(API_URL+"repos/{owner}/{repo}/branches",username,repo.name()).headers(headers).retrieve().body(Branch[].class);

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

    private Consumer<HttpHeaders> getHeaders(HttpServletRequest request) {
        return headers -> {
            String accept = request.getHeader("Accept");
            String version = request.getHeader("X-GitHub-Api-Version");
            String authorization = request.getHeader("Authorization");

            if (accept != null) {
                headers.set("Accept", accept);
            }

            if (version != null) {
                headers.set("X-GitHub-Api-Version", version);
            }

            if (authorization != null) {
                headers.set("Authorization", authorization);
            }
        };
    }


}
