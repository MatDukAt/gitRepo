package com.example.demo.model;

import java.util.ArrayList;

public record RepoToDisplay (String repositoryName, String ownerLogin, ArrayList<BranchToDisplay> branches){
}
