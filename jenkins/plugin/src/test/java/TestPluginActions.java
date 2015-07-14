import com.gargoylesoftware.htmlunit.Page;
import com.hp.octane.plugins.jenkins.actions.PluginActions;
import com.hp.octane.plugins.jenkins.model.api.ParameterConfig;
import com.hp.octane.plugins.jenkins.model.parameters.ParameterType;
import hudson.model.*;
import jenkins.model.Jenkins;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 07/01/15
 * Time: 22:09
 * To change this template use File | Settings | File Templates.
 */

public class TestPluginActions {
	final private String projectName = "root-job";

	@Rule
	final public JenkinsRule rule = new JenkinsRule();

	@Test
	public void testProjectsListClassNoParams() throws IOException {
		PluginActions.ProjectsList projectsList = new PluginActions.ProjectsList(true);
		assertEquals(projectsList.jobs.getClass(), PluginActions.ProjectConfig[].class);
		assertEquals(projectsList.jobs.length, 0);

		rule.createFreeStyleProject(projectName);
		projectsList = new PluginActions.ProjectsList(true);
		assertEquals(projectsList.jobs.length, 1);
		assertEquals(projectsList.jobs[0].getName(), projectName);
		assertEquals(projectsList.jobs[0].getParameters().getClass(), ParameterConfig[].class);
		assertEquals(projectsList.jobs[0].getParameters().length, 0);
	}

	@Test
	public void testProjectsListClassWithParams() throws IOException {
		FreeStyleProject fsp;
		ParameterConfig tmpConf;
		PluginActions.ProjectsList projectsList = new PluginActions.ProjectsList(true);
		assertEquals(projectsList.jobs.getClass(), PluginActions.ProjectConfig[].class);
		assertEquals(projectsList.jobs.length, 0);

		fsp = rule.createFreeStyleProject(projectName);
		ParametersDefinitionProperty params = new ParametersDefinitionProperty(Arrays.asList(
				(ParameterDefinition) new BooleanParameterDefinition("ParamA", true, "bool"),
				(ParameterDefinition) new StringParameterDefinition("ParamB", "str", "string"),
				(ParameterDefinition) new FileParameterDefinition("ParamC", "file param")
		));
		fsp.addProperty(params);

		projectsList = new PluginActions.ProjectsList(true);
		assertEquals(projectsList.jobs.length, 1);
		assertEquals(projectsList.jobs[0].getName(), projectName);
		assertEquals(projectsList.jobs[0].getParameters().length, 3);

		tmpConf = projectsList.jobs[0].getParameters()[0];
		assertEquals(tmpConf.getName(), "ParamA");
		assertEquals(tmpConf.getType(), ParameterType.BOOLEAN.toString());
		assertEquals(tmpConf.getDefaultValue(), true);
		assertEquals(tmpConf.getDescription(), "bool");

		tmpConf = projectsList.jobs[0].getParameters()[1];
		assertEquals(tmpConf.getName(), "ParamB");
		assertEquals(tmpConf.getType(), ParameterType.STRING.toString());
		assertEquals(tmpConf.getDefaultValue(), "str");
		assertEquals(tmpConf.getDescription(), "string");

		tmpConf = projectsList.jobs[0].getParameters()[2];
		assertEquals(tmpConf.getName(), "ParamC");
		assertEquals(tmpConf.getType(), ParameterType.FILE.toString());
		assertEquals(tmpConf.getDefaultValue(), "");
		assertEquals(tmpConf.getDescription(), "file param");
	}

	@Test
	public void testPluginActionsMethods() {
		PluginActions pluginActions = new PluginActions();
		assertEquals(pluginActions.getIconFileName(), null);
		assertEquals(pluginActions.getDisplayName(), null);
		assertEquals(pluginActions.getUrlName(), "octane");
	}

	@Test
	public void testPluginActions_REST_Status() throws IOException, SAXException {
		JenkinsRule.WebClient client = rule.createWebClient();
		Page page = client.goTo("octane/status", "application/json");
		JSONObject body = new JSONObject(page.getWebResponse().getContentAsString());
		JSONObject tmp;

		assertEquals(body.length(), 3);

		assertTrue(body.has("server"));
		tmp = body.getJSONObject("server");
		assertEquals("jenkins", tmp.getString("type"));
		assertEquals(Jenkins.VERSION, tmp.getString("version"));
		assertEquals(rule.getInstance().getRootUrl(), tmp.getString("url") + "/");
		assertFalse(tmp.isNull("instanceId"));
		//  TODO: extend the test deeper

		assertTrue(body.has("plugin"));
		tmp = body.getJSONObject("plugin");
		assertNotEquals("", tmp.getString("version"));
		//  TODO: extent the test deeper

		assertTrue(body.has("eventsClients"));
		//  TODO: extent the test deeper
	}

	@Test
	public void testPluginActions_REST_Jobs_NoParams() throws IOException, SAXException {
		JenkinsRule.WebClient client = rule.createWebClient();
		Page page;
		JSONObject body;
		JSONObject job;
		JSONArray jobs;

		page = client.goTo("octane/jobs", "application/json");
		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertTrue(body.has("jobs"));
		jobs = body.getJSONArray("jobs");
		assertEquals(jobs.length(), 0);

		rule.createFreeStyleProject(projectName);
		page = client.goTo("octane/jobs", "application/json");
		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertTrue(body.has("jobs"));
		jobs = body.getJSONArray("jobs");
		assertEquals(jobs.length(), 1);
		job = jobs.getJSONObject(0);
		assertEquals(job.getString("name"), projectName);
		assertEquals(job.getJSONArray("parameters").length(), 0);
	}

	@Test
	public void testPluginActions_REST_Jobs_WithParams() throws IOException, SAXException {
		FreeStyleProject fsp;
		JenkinsRule.WebClient client = rule.createWebClient();
		Page page;
		JSONObject body;
		JSONArray jobs;

		page = client.goTo("octane/jobs", "application/json");
		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertTrue(body.has("jobs"));
		jobs = body.getJSONArray("jobs");
		assertEquals(jobs.length(), 0);

		fsp = rule.createFreeStyleProject(projectName);
		ParametersDefinitionProperty params = new ParametersDefinitionProperty(Arrays.asList(
				(ParameterDefinition) new BooleanParameterDefinition("ParamA", true, "bool"),
				(ParameterDefinition) new StringParameterDefinition("ParamB", "str", "string"),
				(ParameterDefinition) new FileParameterDefinition("ParamC", "file param")
		));
		fsp.addProperty(params);

		page = client.goTo("octane/jobs", "application/json");
		body = new JSONObject(page.getWebResponse().getContentAsString());
		assertTrue(body.has("jobs"));
		jobs = body.getJSONArray("jobs");
		assertEquals(jobs.length(), 1);

		//  Test ParamA
		JSONObject paramBoolean = jobs.getJSONObject(0).getJSONArray("parameters").getJSONObject(0);
		assertEquals("ParamA", paramBoolean.get("name"));
		assertEquals(ParameterType.BOOLEAN.toString(), paramBoolean.get("type"));
		assertEquals("bool", paramBoolean.get("description"));
		assertEquals(true, paramBoolean.get("defaultValue"));

		//  Test ParamB
		JSONObject paramString = jobs.getJSONObject(0).getJSONArray("parameters").getJSONObject(1);
		assertEquals("ParamB", paramString.get("name"));
		assertEquals(ParameterType.STRING.toString(), paramString.get("type"));
		assertEquals("string", paramString.get("description"));
		assertEquals("str", paramString.get("defaultValue"));

		//  Test ParamC
		JSONObject paramFile = jobs.getJSONObject(0).getJSONArray("parameters").getJSONObject(2);
		assertEquals("ParamC", paramFile.get("name"));
		assertEquals(ParameterType.FILE.toString(), paramFile.get("type"));
		assertEquals("file param", paramFile.get("description"));
		assertEquals("", paramFile.get("defaultValue"));
	}
}
