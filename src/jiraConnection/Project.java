package jiraConnection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Project {

	private static String URL_GET_PROJECTS = "{SERVER}/rest/api/2/issue/createmeta";
	private static String TEST_ISSSUETYPE_NAME = "Test";

// Used
	public static String getProjectIdByKey(String projectKey, RestClient restClient) {

		String projectId = null;

		HttpResponse response = null;
		try {
			response = restClient.getHttpclient().execute(new HttpGet(restClient.getUrl() + "/rest/api/2/project/" + URLEncoder.encode(projectKey, "utf-8")), restClient.getContext());
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
			
			if(!(string.charAt(0)=='[')){
				string="["+string+"]";
			}
			
			try {
				JSONArray projArray = new JSONArray(string);
				List<Long> projectIdList = new ArrayList<Long>();
				for(int i = 0; i < projArray.length(); i++) {
					Long id = projArray.getJSONObject(i).getLong("id");
					projectIdList.add(id);
				}
				
				Collections.sort(projectIdList);
				projectId = projectIdList.get(0).toString();
				
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
	
		return projectId;
	}
// Used
	public static String getProjectVersionNameID(String projectKey,String VersionName,  RestClient restClient) {
		HashMap<String, String> versionIdNameList = new HashMap<String, String>();

		HttpResponse response = null;
		String VersionURL = null;
		try {
			VersionURL = restClient.getUrl() + "/rest/api/2/project/" + URLEncoder.encode(projectKey, "utf-8")
								+"/version";
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		try {
			response = restClient.getHttpclient().execute(new HttpGet(VersionURL), restClient.getContext());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode >= 200 && statusCode < 300) {
			HttpEntity entity = response.getEntity();
			String Projectstring = null;
			String VersionValueString = null;
			try {
				Projectstring = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				JSONObject projObj = new JSONObject(Projectstring);
				VersionValueString = projObj.get("values").toString();
				
				JSONArray versionObject = new JSONArray(VersionValueString);
				for (int i=0;i<versionObject.length();i++){
					String VersionID = versionObject.getJSONObject(i).getString("id");
					String sVersionName = versionObject.getJSONObject(i).getString("name");
					versionIdNameList.put(sVersionName, VersionID);
					if(versionIdNameList.get(VersionName)==null){
						return null;
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
	
		return versionIdNameList.get(VersionName);
	}
	
	public static Map<Long, String> getAllProjects(RestClient restClient) {


		Map<Long, String> projects = new TreeMap<Long, String>();
		
		HttpResponse response = null;
		
		final String url = URL_GET_PROJECTS.replace("{SERVER}", restClient.getUrl());
		try {
			response = restClient.getHttpclient().execute(new HttpGet(url), restClient.getContext());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (HttpHostConnectException e) {
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
				JSONObject projObject = new JSONObject(string);
				JSONArray projArray = projObject.getJSONArray("projects");
				for(int i = 0; i < projArray.length(); i++) {
					JSONObject project = projArray.getJSONObject(i);
					Long id = project.getLong("id");
					String projName = project.getString("name");
					JSONArray issueTypes = project.getJSONArray("issuetypes");
					
					boolean issueTypeTesstExists = false;
					for (int j = 0; j < issueTypes.length(); j++) {
						JSONObject issueType = issueTypes.getJSONObject(j);
						String issueTypeName = issueType.getString("name");
						
						if (issueTypeName.trim().equalsIgnoreCase(TEST_ISSSUETYPE_NAME)) {
							issueTypeTesstExists = true;
							break;
						}
					}
					
					if(!issueTypeTesstExists) {
						continue;
					}
					projects.put(id, projName);
				}
				
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			
		} else {
			
			projects.put(0L, "No Project");
			try {
				throw new ClientProtocolException("Unexpected response status: "
						+ statusCode);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}
		}
	
		return projects;
	}
}
