package jiraConnection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ZephyrConfigModel {

	private List<TestCaseResultModel> testcases;
	private Long zephyrProjectId;
	private Long versionId;
	private Long cycleId;
	private String cyclePrefix;
	private long testIssueTypeId;
	private String cycleName;
	private String cycleDuration;
	private RestClient restClient;
	private Map<String, Map<Long, String>> TCKeyIDSummary = new HashMap<String, Map<Long, String>>();


	public Map<String, Map<Long, String>> getTCKeyID() {
		return TCKeyIDSummary;
	}

	public void setTCKeyID(Map<String, Map<Long, String>> TCKeyIDSummary) {
		this.TCKeyIDSummary = TCKeyIDSummary;
	}

	public long getTestIssueTypeId() {
		return testIssueTypeId;
	}

	public void setTestIssueTypeId(long testIssueTypeId) {
		this.testIssueTypeId = testIssueTypeId;
	}

	public String getCyclePrefix() {
		return cyclePrefix;
	}

	public void setCyclePrefix(String cyclePrefix) {
		this.cyclePrefix = cyclePrefix;
	}

	public List<TestCaseResultModel> getTestcases() {
		return testcases;
	}

	public void setTestcases(List<TestCaseResultModel> testcases) {
		this.testcases = testcases;
	}

	public Long getZephyrProjectId() {
		return zephyrProjectId;
	}

	public void setZephyrProjectId(Long zephyrProjectId) {
		this.zephyrProjectId = zephyrProjectId;
	}

	public Long getCycleId() {
		return cycleId;
	}

	public void setCycleId(Long cycleId) {
		this.cycleId = cycleId;
	}

	public String getCycleName() {
		return cycleName;
	}

	public String getCycleDuration() {
		return cycleDuration;
	}

	public void setCycleDuration(String cycleDuration) {
		this.cycleDuration = cycleDuration;
	}

	public void setCycleName(String cycleName) {
		this.cycleName = cycleName;
	}

	public RestClient getRestClient() {
		return restClient;
	}

	public void setRestClient(RestClient restClient) {
		this.restClient = restClient;
	}

	public Long getVersionId() {
		return versionId;
	}

	public void setVersionId(Long versionId) {
		this.versionId = versionId;
	}

}