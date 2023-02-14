package com.qualizeal.tools;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;
import com.qualizeal.modal.azuredevops.ADODetails;
import com.qualizeal.modal.common.AuthType;
import com.qualizeal.modal.common.TestConfig;
import com.qualizeal.modal.common.ToolConfig;
import com.qualizeal.modal.azuredevops.StatusCode;
import com.qualizeal.modal.azuredevops.ADOParameters;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.qualizeal.modal.azuredevops.ADOParameters.*;

@Log
public class ADOActions implements ToolActions {

    private ToolConfig toolConfig;
    private List<TestConfig> testConfig;
    @Getter
    private List<String> runs;
    private String authToken;

    Supplier<HttpClient> httpClient = HttpClient::newHttpClient;

    @Override
    public ToolActions setToolConfig(ToolConfig toolConfig) {
        this.toolConfig = toolConfig;
        return this;
    }

    @Override
    public ToolActions setTestConfig(List<TestConfig> testConfig) {
        this.testConfig = testConfig;
        return this;
    }

    @Override
    public ToolActions setRuns(List<String> runIds) {
        this.runs = runIds;
        return this;
    }

//    @Override
    public List<String> getTestDetails(String runId) {
        List<ADODetails> testDetails = getTestExecutions(runId);
        return testDetails.stream().map(ADODetails::getAutomationReference).collect(Collectors.toList());
    }

    @Override
    public void authenticate() {
        if (AuthType.BASIC == this.toolConfig.getCredential().getAuthType()) {
            String usernamePassword = this.toolConfig.getCredential().getUsername() + ":" + this.toolConfig.getCredential().getPassword();
            authToken = "Basic " + Base64.getEncoder().encodeToString(usernamePassword.getBytes(StandardCharsets.UTF_8));
            System.out.println(authToken);
        }
    }

    @Override
    public void updateResults() {
        //Test points - Name and point id
        List<ADODetails> testADOTests = getTestExecutions(runs.get(0));
        Map<String, String> activeRuns = testConfig.stream().collect(Collectors.toMap(TestConfig::getName, TestConfig::getStatus));
        runs.forEach(r -> {
            //instantiate the bulk
            JsonArray jsonRunArray = new JsonArray();
            List<Integer> testPointList = new ArrayList<Integer>();
            for (ADODetails a : testADOTests) {
                if (activeRuns.get(a.getAutomationReference()) != null) {
                    StatusCode statusCode = StatusCode.getByClientCode(activeRuns.get(a.getAutomationReference()));
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("status_id", statusCode.getToolCode());
                    jsonObject.addProperty("test_id", a.getId());
                    jsonObject.addProperty("test_point", a.getTestPoint());
                    testPointList.add(a.getTestPoint());
                    jsonRunArray.add(jsonObject);
                }
            }
            Integer currentRunID = getTestRunId(testPointList);
            HashMap<String,String> testResults= getResultID(currentRunID);
            try {
                String resultPayload = getRunResultsPayload(jsonRunArray,testResults);
                updateTestRunResults(resultPayload,currentRunID);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    private String getRunResultsPayload(JsonArray jsonTestRun, HashMap<String,String> testResults) throws ParseException {
        JsonArray jsonResultArray = new JsonArray();
        JSONParser parser = new JSONParser();
        for(int i=0;i<jsonTestRun.size();i++){
            String jsonEle= jsonTestRun.get(i).toString();
            JSONObject json = (JSONObject) parser.parse(jsonEle);
            JsonObject jsonObject = new JsonObject();
            String status = json.getAsString("status_id");
            String testPoint = json.getAsString("test_point");
            String runId = testResults.get(testPoint);
            jsonObject.addProperty("id", runId);
            jsonObject.addProperty("state", "Completed");
            jsonObject.addProperty("outcome", status);
            jsonResultArray.add(jsonObject);
        }
        return jsonResultArray.toString();
    }

    private void updateTestRunResults(String payload,Integer resultId){
        String serviceEndPoint = String.format(TEST_RESULT, resultId.toString());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(toolConfig.getEndPoint() + serviceEndPoint))
                .header("Authorization", authToken)
                .header("Content-Type", "application/json")
                .method("PATCH",HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.get().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            log.warning("" + e.getMessage());
        }

    }

    private HashMap<String,String> getResultID(Integer runId) {
        HashMap<String, String> testResultsID = new HashMap<>();
        String serviceEndPoint = String.format(TEST_RESULT, runId.toString());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(toolConfig.getEndPoint() + serviceEndPoint))
                .header("Authorization", authToken)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.get().send(request, HttpResponse.BodyHandlers.ofString());
            List JsonResults = JsonPath.parse(response.body()).read("$.value");
            int size = JsonResults.size();


            for (int iResultId = 0; iResultId < size; iResultId++) {
                String resultID = JsonPath.parse(response.body()).read("$.value[" + iResultId + "].id").toString();

                String pointID= JsonPath.parse(response.body()).read("$.value[" + iResultId + "].testPoint.id");
                testResultsID.put(pointID,resultID);
            }
        }catch(Exception e){System.out.println("Get result id" + e.toString());
        log.warning("Get result id" + e.toString());}
        return testResultsID;

    }
    private Integer getTestRunId(List testPoints){
        String payload = testRunPayload(testPoints,runs.get(0));
        String serviceEndPoint = String.format(ADOParameters.TEST_RUN, "");
        try {
            JSONParser parser = new JSONParser();
            JSONObject payloadJSON = (JSONObject) parser.parse(payload);
        }catch (Exception  e) {
            log.warning(e.getMessage());
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(toolConfig.getEndPoint() + serviceEndPoint))
                .header("Authorization", authToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.get().send(request, HttpResponse.BodyHandlers.ofString());
            Integer runID = JsonPath.parse(response.body()).read("$.id");
            return runID;
        } catch (IOException | InterruptedException e) {
            System.out.println("get run id" + e.toString());
        }
        return 0;
    }


    private String testRunPayload(List testPoints,String testPlanId){
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        String runName = "Demo Run" + timeStamp;
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\"name\": \"Demo run\",");
        sb.append("\"isAutomated\": true,");
        sb.append("\"plan\": {\"id\":");
        sb.append(testPlanId).append(" },");
        sb.append("\"pointIds\": ").append(testPoints);
        sb.append("}");
        return sb.toString();
    }

    private String testResultPayload(Integer resultId, String outcome){
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        sb.append("{\n");
        sb.append("\"id\":").append(resultId).append(",");
        sb.append("\"state\": \"Completed\",");
        sb.append("\"outcome\": \"").append(outcome).append("\"");
        sb.append("}\n");
        sb.append("]");
        return sb.toString();
    }

    @SneakyThrows
    private List<ADODetails> getTestExecutions(String runId) {
        String serviceEndPoint = String.format(TEST_CASES, runId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(toolConfig.getEndPoint() + serviceEndPoint))
                .header("Authorization", authToken)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.get().send(request, HttpResponse.BodyHandlers.ofString());
        List JsonResults = JsonPath.parse(response.body()).read("$.value");
        int size = JsonResults.size();
        ArrayList<ADODetails> testPoints = new ArrayList<>();

        for (int iTestCases = 0; iTestCases < size; iTestCases++) {
            int pointAssigned = JsonPath.parse(response.body()).read("$.value[" + iTestCases + "].pointAssignments[0].id");
            int testCaseId = JsonPath.parse(response.body()).read("$.value[" + iTestCases + "].workItem.id");
            ADODetails testADODetails = new ADODetails();
            testADODetails.setId(testCaseId);
            HashMap <String,String> AutomationName = JsonPath.parse(response.body()).read("$.value[" + iTestCases + "].workItem.workItemFields[8]");

            testADODetails.setAutomationReference(((String) AutomationName.get("Microsoft.VSTS.TCM.AutomatedTestName")));
            testADODetails.setTestPoint(pointAssigned);
            testPoints.add (testADODetails);
        }

        return testPoints;

    }

}