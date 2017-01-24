package jiraConnection;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import javaSeed.constants.Const;

public class JIRAUpdate {
	public static final String JIRA_UPDATEFLAG = null;//= Const.ENVIRONMENT_EXEC_ARRAY[1];
	public static final String JIRA_SERVER = null;//= Const.ENVIRONMENT_EXEC_ARRAY[7];
	public static final String JIRA_USERNAME = null;//= Const.ENVIRONMENT_EXEC_ARRAY[8];
	public static final String JIRA_B64PASSWORD = null;//= Const.ENVIRONMENT_EXEC_ARRAY[9];
	public static final String JIRA_PROJECTKEY = null;//= Const.ENVIRONMENT_EXEC_ARRAY[10];
	public static final String JIRA_PROJ_VERSIONNAME = null;//= Const.ENVIRONMENT_EXEC_ARRAY[11];
	public static final String JIRA_PROJ_CYCLENAME = null;//= Const.ENVIRONMENT_EXEC_ARRAY[12];
	public static final String JIRA_REPORTUPLOAD_TCKEY = null;//= Const.ENVIRONMENT_EXEC_ARRAY[13];

	public static void ConnectJiraUpdateTCStatus(List<String> passList,List<String> failList) {

		try{
		
		ZephyrConfigModel ZCModel = MakeConnectionGetTCsFromCycle();	
		
		TestCaseUtil.executeTests(ZCModel, passList, failList);
		}
		 catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Boolean UploadTestExecutionReport(String ReportPath, String IssueKey) {

		Boolean AttachmentStatus = null;
		try{
			
		ZephyrConfigModel ZCModel = MakeConnectionGetTCsFromCycle();
		
		//String EntityID = TestCaseUtil.getEntityIDFromIssueKey(ZCModel, IssueKey);
		
		AttachmentStatus = TestCaseUtil.AddAttachmentJIRAExecution(ZCModel, ReportPath, IssueKey);
		
		}
		 catch (Exception e) {
			e.printStackTrace();
		}
		return AttachmentStatus;
	}
	
	
	public static ZephyrConfigModel MakeConnectionGetTCsFromCycle() {
		
		Map<String, Map<Long, String>> TCKeyIDSummary = new HashMap<String, Map<Long, String>>();
		ZephyrConfigModel ZCModel = new ZephyrConfigModel();
		try{
			
		// Declarations AND Input Parameters
		String VersionID=null,projectID=null,CycleID=null;
		
		// ZephyrInstance Object instantiation 
		ZephyrInstance zephyrServer = new ZephyrInstance();		
		zephyrServer.setServerAddress(JIRA_SERVER);
		zephyrServer.setUsername(JIRA_USERNAME);
		zephyrServer.setPassword(new String(Base64.getDecoder().decode(JIRA_B64PASSWORD)));
		
		// RestClient Object instantiation 
		//RestClient oRestClient = new RestClient(zephyrServer);
		RestClient oRestClient = new RestClient(JIRA_SERVER, JIRA_USERNAME, new String(Base64.getDecoder().decode(JIRA_B64PASSWORD)));
		projectID = Project.getProjectIdByKey(JIRA_PROJECTKEY, oRestClient);
		VersionID = Project.getProjectVersionNameID(JIRA_PROJECTKEY, JIRA_PROJ_VERSIONNAME, oRestClient);
		CycleID=Cycle.getCyclesIDByName(VersionID,projectID, JIRA_PROJ_CYCLENAME, oRestClient);

		// ZephyrConfigModel Object instantiation 
		
		ZCModel.setZephyrProjectId(Long.parseLong(projectID));
		ZCModel.setVersionId(Long.parseLong(VersionID));		
		ZCModel.setCycleId(Long.parseLong(CycleID));
		ZCModel.setCycleName(JIRA_PROJ_CYCLENAME);
		ZCModel.setRestClient(oRestClient);
		
		TCKeyIDSummary = TestCaseUtil.fetchIssueKeyIdSummaryFromCycle(ZCModel);
		
		ZCModel.setTCKeyID(TCKeyIDSummary);
		
		}
		 catch (Exception e) {
			e.printStackTrace();
		}
		return ZCModel;
	}
}
