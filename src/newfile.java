package github.tools.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import github.tools.responseObjects.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Base64;

public class GitHubApiClient {
    private final String baseUrl = "https://api.github.com";
    private BasicAuth basicAuth;

    public GitHubApiClient(String user, String token) {
        this.basicAuth = new BasicAuth(user, token);
    }

    public void setUser(String user) {
        this.basicAuth = new BasicAuth(user, this.basicAuth.getPassword());
    }

    public void setToken(String token) {
        this.basicAuth = new BasicAuth(this.basicAuth.getUser(), token);
    }

    // https://docs.github.com/en/rest/reference/repos#create-a-repository-for-the-authenticated-user
    public CreateRepoResponse createRepo(RequestParams requestParams) {
        String endpoint = String.format("%s/user/repos", baseUrl);
        Response response = HttpRequest.post(endpoint, requestParams, basicAuth);
        return new CreateRepoResponse((JsonObject) response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#get-a-repository
    public GetRepoInfoResponse getRepoInfo(String repoOwner, String repoName) {
        String endpoint = String.format("%s/repos/%s/%s", baseUrl, repoOwner, repoName);
        Response response = HttpRequest.get(endpoint, null, basicAuth);
        return new GetRepoInfoResponse((JsonObject)response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#update-a-repository
    public UpdateRepoResponse updateRepo(String repoOwner, String repoName, RequestParams requestParams) {
        String endpoint = String.format("%s/repos/%s/%s", baseUrl, repoOwner, repoName);
        Response response = HttpRequest.patch(endpoint, requestParams, basicAuth);
        return new UpdateRepoResponse((JsonObject)response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#delete-a-repository
    public DeleteRepoResponse deleteRepo(String repoOwner, String repoName) {
        String endpoint = String.format("%s/repos/%s/%s", baseUrl, repoOwner, repoName);
        Response response = HttpRequest.delete(endpoint, null, basicAuth);
        return new DeleteRepoResponse((JsonObject)response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#list-repository-contributors
    public ListRepoContributorsResponse listRepoContributors(String repoOwner, String repoName, QueryParams queryParams) {
        String endpoint = String.format("%s/repos/%s/%s/contributors", baseUrl, repoOwner, repoName);
        Response response = HttpRequest.get(endpoint, queryParams, basicAuth);
        return new ListRepoContributorsResponse((JsonArray) response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#list-repositories-for-a-user
    public ListReposResponse listRepos(QueryParams queryParams) {
        String endpoint = String.format("%s/user/repos", baseUrl);
        Response response = HttpRequest.get(endpoint, queryParams, basicAuth);
        return new ListReposResponse((JsonArray) response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#list-branches
    public ListBranchesInRepoResponse listBranchesInRepo(String repoOwner, String repoName, QueryParams queryParams) {
        String endpoint = String.format("%s/repos/%s/%s/branches", baseUrl, repoOwner, repoName);
        Response response = HttpRequest.get(endpoint, queryParams, basicAuth);
        return new ListBranchesInRepoResponse((JsonArray) response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#get-a-branch
    public GetBranchInfoResponse getBranchInfoFromRepo(String repoOwner, String repoName, String branchName) {
        String endpoint = String.format("%s/repos/%s/%s/branches/%s", baseUrl, repoOwner, repoName, branchName);
        Response response = HttpRequest.get(endpoint, null, basicAuth);
        return new GetBranchInfoResponse((JsonObject) response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#rename-a-branch
    public RenameBranchInRepoResponse renameBranchInRepo(String repoOwner, String repoName, String branchName, RequestParams requestParams) {
        String endpoint = String.format("%s/repos/%s/%s/branches/%s/rename", baseUrl, repoOwner, repoName, branchName);
        Response response = HttpRequest.post(endpoint, requestParams, basicAuth);
        return new RenameBranchInRepoResponse((JsonObject) response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#list-repository-collaborators
    public ListRepoCollaboratorsResponse listRepoCollaborators(String repoOwner, String repoName) {
        String endpoint = String.format("%s/repos/%s/%s/collaborators", baseUrl, repoOwner, repoName);
        Response response = HttpRequest.get(endpoint, null, basicAuth);
        return new ListRepoCollaboratorsResponse((JsonArray) response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#check-if-a-user-is-a-repository-collaborator
    public boolean isUserACollaboratorInRepo(String repoOwner, String repoName, String username) {
        try {
            String endpoint = String.format("%s/repos/%s/%s/collaborators/%s", baseUrl, repoOwner, repoName, username);
            Response response = HttpRequest.get(endpoint, null, basicAuth);
            return response.getStatusCode() == 204;
        } catch (RequestFailedException e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    // https://docs.github.com/en/rest/reference/repos#add-a-repository-collaborator
    public AddUserToRepoResponse addUserToRepo(String repoOwner, String repoName, String username) {
        String endpoint = String.format("%s/repos/%s/%s/collaborators/%s", baseUrl, repoOwner, repoName, username);
        Response response = HttpRequest.put(endpoint, null, basicAuth);
        if (response.getStatusCode() == 201) {
            return new AddUserToRepoResponse((JsonObject) response.getBody());
        }
        if (response.getStatusCode() == 204) {
            System.err.println("User is already a collaborator in this repo");
        }
        return null;
    }

    // https://docs.github.com/en/rest/reference/repos#remove-a-repository-collaborator
    public RemoveUserFromRepoResponse removeUserFromRepo(String repoOwner, String repoName, String username) {
        String endpoint = String.format("%s/repos/%s/%s/collaborators/%s", baseUrl, repoOwner, repoName, username);
        Response response = HttpRequest.delete(endpoint, null, basicAuth);
        return new RemoveUserFromRepoResponse((JsonObject)response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#list-commits
    // note: "sha" is the query param that can be used to specify branch, it's not so obvious
    public ListCommitsInRepoResponse listCommitsInRepo(String repoOwner, String repoName, QueryParams queryParams) {
        String endpoint = String.format("%s/repos/%s/%s/commits", baseUrl, repoOwner, repoName);
        Response response = HttpRequest.get(endpoint, queryParams, basicAuth);
        return new ListCommitsInRepoResponse((JsonArray)response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#get-a-commit
    public GetCommitResponse getCommit(String repoOwner, String repoName, String commitHash, QueryParams queryParams) {
        String endpoint = String.format("%s/repos/%s/%s/commits/%s", baseUrl, repoOwner, repoName, commitHash);
        Response response = HttpRequest.get(endpoint, queryParams, basicAuth);
        return new GetCommitResponse((JsonObject)response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#list-repository-languages
    public ListRepoLanguagesResponse listRepoLanguages(String repoOwner, String repoName) {
        String endpoint = String.format("%s/repos/%s/%s/languages", baseUrl, repoOwner, repoName);
        Response response = HttpRequest.get(endpoint, null, basicAuth);
        return new ListRepoLanguagesResponse((JsonObject)response.getBody());
    }

    // https://docs.github.com/en/rest/reference/users#get-a-user
    public GetUserResponse getUser(String username) {
        String endpoint = String.format("%s/users/%s", baseUrl, username);
        Response response = HttpRequest.get(endpoint, null, basicAuth);
        return new GetUserResponse((JsonObject)response.getBody());
    }

    // https://docs.github.com/en/rest/reference/users#update-the-authenticated-user
    public UpdateUserResponse updateUser(RequestParams requestParams) {
        String endpoint = String.format("%s/user", baseUrl);
        Response response = HttpRequest.patch(endpoint, requestParams, basicAuth);
        return new UpdateUserResponse((JsonObject)response.getBody());
    }

    // https://docs.github.com/en/rest/reference/pulls#list-pull-requests
    public ListPullRequestsResponse listPullRequests(String repoOwner, String repoName, QueryParams queryParams) {
        String endpoint = String.format("%s/repos/%s/%s/pulls", baseUrl, repoOwner, repoName);
        Response response = HttpRequest.get(endpoint, queryParams, basicAuth);
        return new ListPullRequestsResponse((JsonArray) response.getBody());
    }

    // https://docs.github.com/en/rest/reference/pulls#get-a-pull-request
    public GetPullRequestResponse getPullRequest(String repoOwner, String repoName, int pullRequestNumber) {
        String endpoint = String.format("%s/repos/%s/%s/pulls/%s", baseUrl, repoOwner, repoName, pullRequestNumber);
        Response response = HttpRequest.get(endpoint, null, basicAuth);
        return new GetPullRequestResponse((JsonObject) response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#get-repository-content
    public GetRepoDirectoryResponse getRepoDirectory(String repoOwner, String repoName, String path, String branch) {
        String endpoint = String.format("%s/repos/%s/%s/contents/%s", baseUrl, repoOwner, repoName, path);
        QueryParams queryParams = new QueryParams();
        queryParams.addParam("ref", branch);
        Response response = HttpRequest.get(endpoint, queryParams, basicAuth);
        return new GetRepoDirectoryResponse((JsonArray) response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#get-repository-content
    public GetRepoFileResponse getRepoFile(String repoOwner, String repoName, String filePath, String branch) {
        String endpoint = String.format("%s/repos/%s/%s/contents/%s", baseUrl, repoOwner, repoName, filePath);
        QueryParams queryParams = new QueryParams();
        queryParams.addParam("ref", branch);
        Response response = HttpRequest.get(endpoint, queryParams, basicAuth);
        return new GetRepoFileResponse((JsonObject) response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#get-repository-content
    public ArrayList<RepoFileContent> getAllFilesInRepo(String repoOwner, String repoName, String branch) {
        ArrayList<RepoFileContent> repoFiles = new ArrayList<>();
        Queue<String> paths = new LinkedList<>();

        paths.add("");
        while (!paths.isEmpty()) {
            GetRepoDirectoryResponse repoDirectoryResponse = getRepoDirectory(repoOwner, repoName, paths.poll(), branch);
            for (int i = 0; i < repoDirectoryResponse.getRepoContent().size(); i++) {
                RepoContent repoContent = repoDirectoryResponse.getRepoContent().get(i);
                if (repoContent.getType().equals("file")) {
                    GetRepoFileResponse repoFileResponse = getRepoFile(repoOwner, repoName, repoContent.getPath(), branch);

                    repoFiles.add(new RepoFileContent(
                            repoFileResponse.getFileName(),
                            repoFileResponse.getFilePath(),
                            repoFileResponse.getHash(),
                            repoFileResponse.getText(),
                            repoFileResponse.getSize(),
                            repoFileResponse.getUrl()
                    ));
                } else if (repoContent.getType().equals("dir")) {
                    paths.add(repoContent.getPath());
                }
            }
        }
        return repoFiles;
    }

    // https://docs.github.com/en/rest/reference/repos#create-or-update-file-contents
    public CreateFileResponse createFile(String repoOwner, String repoName, String filePath, String branch, String fileText, String commitMessage) {
        RequestParams requestParams = new RequestParams();
        requestParams.addParam("branch", branch);
        String contentBase64Encoded = Base64.getMimeEncoder().encodeToString(fileText.getBytes(StandardCharsets.UTF_8));
        requestParams.addParam("content", contentBase64Encoded);
        requestParams.addParam("message", commitMessage);
        String endpoint = String.format("%s/repos/%s/%s/contents/%s", baseUrl, repoOwner, repoName, filePath);
        Response response = HttpRequest.put(endpoint, requestParams, basicAuth);
        return new CreateFileResponse((JsonObject) response.getBody());
    }

    // https://docs.github.com/en/rest/reference/repos#create-or-update-file-contents
    public UpdateFileResponse updateFile(String repoOwner, String repoName, String filePath, String branch, String fileText, String commitMessage) {
        RequestParams requestParams = new RequestParams();
        requestParams.addParam("branch", branch);
        String contentBase64Encoded = Base64.getEncoder().encodeToString(fileText.getBytes(StandardCharsets.UTF_8));
        requestParams.addParam("content", contentBase64Encoded);
        requestParams.addParam("message", commitMessage);
        GetRepoFileResponse getRepoFile = getRepoFile(repoOwner, repoName, filePath, branch);
        requestParams.addParam("sha", getRepoFile.getHash());
        String endpoint = String.format("%s/repos/%s/%s/contents/%s", baseUrl, repoOwner, repoName, filePath);
        Response response = HttpRequest.put(endpoint, requestParams, basicAuth);
        return new UpdateFileResponse((JsonObject) response.getBody());
    }
}