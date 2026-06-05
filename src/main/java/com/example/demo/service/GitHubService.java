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
import java.util.List;

@Service
public class GitHubService {

    private final RestClient restClient;

    public GitHubService(RestClient restClient){
        this.restClient = restClient;
    }


    public List<RepoToDisplay> getNonForkRepos(String username){
        @Nullable
        var allRepos = restClient.get().uri("https://api.github.com/users/{username}/repos", username).retrieve().body(Repository[].class);
        List<RepoToDisplay> toReturn = new ArrayList<>();
        if (allRepos == null){
            throw new GitHubException("GitHub user "+username+" does not exist");
        }
        for (Repository repo : allRepos){
            var repoToAdd = new RepoToDisplay(repo.name(),repo.owner().login(),new ArrayList<>());
            if (!repo.fork()){
                @Nullable
                Branch[] allBranches = restClient.get().uri("https://api.github.com/repos/{owner}/{repo}/branches",username,repo.name()).retrieve().body(Branch[].class);
                if (allBranches == null){
                    throw new GitHubException("Failed to fetch branches of "+repo.name()+" repository");
                }
                for (Branch branch : allBranches){
                    repoToAdd.branches().add(new BranchToDisplay(branch.name(),branch.commit().sha()));
                }
                toReturn.add(repoToAdd);
            }
        }
        return toReturn;

    }
}
