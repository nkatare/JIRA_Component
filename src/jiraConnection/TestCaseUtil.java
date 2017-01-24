package jiraConnection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TestCaseUtil {
	
	private static final String URL_CREATE_EXECUTIONS_URL = "{SERVER}/rest/zapi/latest/execution?projectId={projectId}&versionId={versionId}&cycleId={cycleId}";
	private static final String URL_EXECUTE_TEST = "{SERVER}/rest/zapi/latest/execution/updateBulkStatus";
	private static final String URL_ADDATTACHMENT = "{SERVER}/rest/zapi/latest/attachment?entityId={entityId}&entityType=execution";
	
	// Get TCs data from the Cycle - Key, ID, Summary
	public static Map<String, Map<Long, String>> fetchIssueKeyIdSummaryFromCycle(ZephyrConfigModel zephyrData) {

		Map<String, Map<Long, String>> issueKeyExecutionIdMap = new HashMap<String, Map<Long, String>>();
		//Map<Long, String> executionIdSummary = new HashMap<Long, String>();

		HttpResponse response = null;
		try {
			
			String executionsURL = URL_CREATE_EXECUTIONS_URL.replace("{SERVER}", zephyrData.getRestClient().getUrl()).replace("{projectId}", zephyrData.getZephyrProjectId()+"").replace("{versionId}", zephyrData.getVersionId()+"").replace("{cycleId}", zephyrData.getCycleId()+"");
		
			HttpGet executionsURLRequest = new HttpGet(executionsURL);
			response = zephyrData.getRestClient().getHttpclient().execute(executionsURLRequest, zephyrData.getRestClient().getContext());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode >= 200 && statusCode < 300) {
			HttpEntity entity = response.getEntity();
			String string = null;
			try {
				string = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			
			try {
				JSONObject executionObject = new JSONObject(string);
				JSONArray executions = executionObject.getJSONArray("executions");
				
				for (int i = 0; i < executions.length(); i++) {
					Map<Long, String> executionIdSummary = new HashMap<Long, String>();
					JSONObject execution = executions.getJSONObject(i);
					String issueKey = execution.getString("issueKey").trim();
					String issueSummary = execution.getString("summary").trim();
					long executionId = execution.getLong("id");
					executionIdSummary.put(executionId, issueSummary);

					
					issueKeyExecutionIdMap.put(issueKey, executionIdSummary);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			
		} else {
			try {
				throw new ClientProtocolException("Unexpected response status: "
						+ statusCode);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}
		}
	
		return issueKeyExecutionIdMap;
	}
	
	// Update the Execution status to the Test Cases in the Cycle
	public static void executeTests(ZephyrConfigModel zephyrData, List<String> passList, List<String> failList) {

		Map<String, Map<Long, String>> TCKeyIDSummary = zephyrData.getTCKeyID();
		Boolean ExecutionIDGet = false;
		CloseableHttpResponse response = null;
		try {
			String bulkExecuteTestsURL = URL_EXECUTE_TEST.replace("{SERVER}", zephyrData.getRestClient().getUrl());
			
			
			if (failList.size() > 0) {
				JSONArray failedTests = new JSONArray();
				JSONObject failObj = new JSONObject();
				
				for (String failedTest: failList) {
					if(TCKeyIDSummary.get(failedTest) != null){
						String TCKey = TCKeyIDSummary.get(failedTest).keySet().toString().replace("[", "").replace("]", "");
						
					failedTests.put(TCKey);
					}
				}
				failObj.put("executions", failedTests);
				failObj.put("status", 2);
				StringEntity failEntity = new StringEntity(failObj.toString());
				HttpPut bulkUpdateFailedTests = new HttpPut(bulkExecuteTestsURL);
				bulkUpdateFailedTests.setHeader("Content-Type", "application/json");
				bulkUpdateFailedTests.setEntity(failEntity);
				response = zephyrData.getRestClient().getHttpclient().execute(bulkUpdateFailedTests, zephyrData.getRestClient().getContext());
					if(ExecutionIDGet==false){
						//ExecutionID = 
					}
			}
			
		
			if (passList.size() > 0) {
				if(response != null) {
					response.close();
				}
							
			JSONArray passedTests = new JSONArray();
			JSONObject passObj = new JSONObject();
			for (String passedTest: passList) {
				if(TCKeyIDSummary.get(passedTest) != null){
					String TCKey = TCKeyIDSummary.get(passedTest).keySet().toString().replace("[", "").replace("]", "");
					passedTests.put(TCKey);
				}				
			}
			passObj.put("executions", passedTests);
			passObj.put("status", 1);
			StringEntity passEntity = new StringEntity(passObj.toString());
			HttpPut bulkUpdatePassedTests = new HttpPut(bulkExecuteTestsURL);
			bulkUpdatePassedTests.setHeader("Content-Type", "application/json");
			bulkUpdatePassedTests.setEntity(passEntity);
			response = zephyrData.getRestClient().getHttpclient().execute(bulkUpdatePassedTests, zephyrData.getRestClient().getContext());
			}


		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode >= 200 && statusCode < 300) {
			HttpEntity entity = response.getEntity();
			try {
				EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			
		} else {
			try {
				throw new ClientProtocolException("Unexpected response status: "
						+ statusCode);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}
		}
	
	}
	
	// Get the Entity ID of the TC using Issue Key from the Cycle
	public static String getEntityIDFromIssueKey(ZephyrConfigModel zephyrData, String IssueKey) {

		String EntityID=null;

		HttpResponse response = null;
		try {
			
			String executionsURL = URL_CREATE_EXECUTIONS_URL.replace("{SERVER}", zephyrData.getRestClient().getUrl()).replace("{projectId}", zephyrData.getZephyrProjectId()+"").replace("{versionId}", zephyrData.getVersionId()+"").replace("{cycleId}", zephyrData.getCycleId()+"");
		
			HttpGet executionsURLRequest = new HttpGet(executionsURL);
			response = zephyrData.getRestClient().getHttpclient().execute(executionsURLRequest, zephyrData.getRestClient().getContext());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode >= 200 && statusCode < 300) {
			HttpEntity entity = response.getEntity();
			String string = null;
			try {
				string = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			
			try {
				JSONObject executionObject = new JSONObject(string);
				JSONArray executions = executionObject.getJSONArray("executions");
				
				for (int i = 0; i < executions.length(); i++) {
					JSONObject execution = executions.getJSONObject(i);
					if( execution.getString("issueKey").toUpperCase().trim().contentEquals(IssueKey.toUpperCase().trim())){
						EntityID = Long.toString(execution.getLong("id"));
					} 
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			
		} else {
			try {
				throw new ClientProtocolException("Unexpected response status: "
						+ statusCode);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}
		}
	
		return EntityID;
	}	
	
	// Add attachment to the Issue 
	public static Boolean AddAttachmentJIRAExecution(ZephyrConfigModel zephyrData, String ReportPath, String IssueKey) {
		
		String JIRA_B64PASSWORD = JIRAUpdate.JIRA_B64PASSWORD;
		Boolean AttachmentStatus = null;
		
		HttpResponse response = null;
		HttpPost httpPost = null;	
		int statusCode = 0;
		
		String EntityID = TestCaseUtil.getEntityIDFromIssueKey(zephyrData, IssueKey);
			
			try {
				String executionsURL = URL_ADDATTACHMENT.replace("{SERVER}", zephyrData.getRestClient().getUrl()).replace("{entityId}", EntityID+"");
				
				httpPost = new HttpPost(executionsURL);
				httpPost.setHeader("X-Atlassian-Token", "nocheck");
				httpPost.setHeader("Authorization", "Basic "+JIRA_B64PASSWORD);
				
/*				final CloseableHttpClient client = HttpClients.createDefault(); 
				final RequestConfig requestConfig = RequestConfig.custom() .setSocketTimeout(30000) .setConnectTimeout(30000) .build();
		*/		
				File fileToUpload = new File(ReportPath);

				final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		        builder.addBinaryBody("file", fileToUpload, ContentType.APPLICATION_OCTET_STREAM,
		        		fileToUpload.getName());
		        final HttpEntity multipart = builder.build();
		        httpPost.setEntity(multipart);
		        response = zephyrData.getRestClient().getHttpclient().execute(httpPost, zephyrData.getRestClient().getContext());
		        statusCode = response.getStatusLine().getStatusCode();
				
			} catch (JSONException e) {
				e.printStackTrace();
			}	catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			List<String> Pass = new ArrayList<String>();
			List<String> Fail = new ArrayList<String>();
			if (statusCode >= 200 && statusCode < 300) {
				Pass.add(IssueKey);
				TestCaseUtil.executeTests(zephyrData, Pass, Fail);
				AttachmentStatus = true;
			} else {
				Fail.add(IssueKey);
				TestCaseUtil.executeTests(zephyrData, Pass, Fail);
				AttachmentStatus = false;
			}
			return AttachmentStatus;	
	}

}
