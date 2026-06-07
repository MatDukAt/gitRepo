package com.example.demo.controller;

import com.example.demo.model.RepoToDisplay;
import com.example.demo.service.GitHubService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class GitHubController {
    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/repos")
    public ResponseEntity<List<RepoToDisplay>> getUserRepositories(@RequestParam String username, HttpServletRequest request){
        List<RepoToDisplay> repos = gitHubService.getNonForkRepos(username,request);
        return ResponseEntity.ok(repos);
    }

}
