/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Rule;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.config.MuleProperties;
import org.mule.module.netsuite.api.BaseRefType;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.templates.builders.SfdcObjectBuilder;
import org.mule.transport.NullPayload;

/**
 * This is the base test class for Templates integration tests.
 * 
 * @author cesar.garcia
 */
public abstract class AbstractTemplateTestCase extends FunctionalTestCase {

	private static final String MAPPINGS_FOLDER_PATH = "./mappings";
	private static final String TEST_FLOWS_FOLDER_PATH = "./src/test/resources/flows/";
	private static final String MULE_DEPLOY_PROPERTIES_PATH = "./src/main/app/mule-deploy.properties";

	protected static final int TIMEOUT_SEC = 240;
	protected static final String TEMPLATE_NAME = "sfdc2nets-opp-migr";

	protected SubflowInterceptingChainLifecycleWrapper retrieveOpportunityFlow;
	protected SubflowInterceptingChainLifecycleWrapper retrieveCustomerFlow;

	@Rule
	public DynamicPort port = new DynamicPort("http.port");

	@Override
	protected String getConfigResources() {
		String resources = "";
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(MULE_DEPLOY_PROPERTIES_PATH));
			resources = props.getProperty("config.resources");
		} catch (Exception e) {
			throw new IllegalStateException(
					"Could not find mule-deploy.properties file on classpath. Please add any of those files or override the getConfigResources() method to provide the resources by your own.");
		}

		return resources + getTestFlows();
	}

	protected String getTestFlows() {
		StringBuilder resources = new StringBuilder();

		File testFlowsFolder = new File(TEST_FLOWS_FOLDER_PATH);
		File[] listOfFiles = testFlowsFolder.listFiles();
		if (listOfFiles != null) {
			for (File f : listOfFiles) {
				if (f.isFile() && f.getName().endsWith("xml")) {
					resources.append(",").append(TEST_FLOWS_FOLDER_PATH)
							.append(f.getName());
				}
			}
			return resources.toString();
		} else {
			return "";
		}
	}

	@Override
	protected Properties getStartUpProperties() {
		Properties properties = new Properties(super.getStartUpProperties());

		String pathToResource = MAPPINGS_FOLDER_PATH;
		File graphFile = new File(pathToResource);

		properties.put(MuleProperties.APP_HOME_DIRECTORY_PROPERTY,
				graphFile.getAbsolutePath());

		return properties;
	}

	protected void deleteTestOpportunityFromSandBox(
			List<Map<String, Object>> createdOpportunities) throws Exception {
		List<String> idList = new ArrayList<String>();

		// Delete the created opportunities in Salesforce
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("deleteOpportunitiesFromSalesforceFlow");
		flow.initialise();
		for (Map<String, Object> c : createdOpportunities) {
			idList.add((String) c.get("Id"));
		}
		flow.process(getTestEvent(idList,
				MessageExchangePattern.REQUEST_RESPONSE));

		// Delete the created opportunities in Netsuite
		List<BaseRefType> baseRefTypeList = new ArrayList<BaseRefType>();
		
		flow = getSubFlow("deleteOpportunitiesFromNetsuiteFlow");
		flow.initialise();
		for (Map<String, Object> c : createdOpportunities) {
			Map<String, Object> opportunity = invokeRetrieveFlow(
					retrieveOpportunityFlow, c);
			if (opportunity != null) {
				BaseRefType baseRefType = new BaseRefType();
				baseRefType.setInternalId((String) opportunity.get("internalId"));
				baseRefType.addSpecificField("type", "OPPORTUNITY");
				baseRefTypeList.add(baseRefType);
			}
		}
		flow.process(getTestEvent(baseRefTypeList,
				MessageExchangePattern.REQUEST_RESPONSE));
	}

	protected void deleteTestAccountFromSandBox(
			List<Map<String, Object>> createdAccounts) throws Exception {
		// Delete the created accounts in Salesforce
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("deleteAccountsFromSalesforceFlow");
		flow.initialise();

		List<Object> idList = new ArrayList<Object>();
		for (Map<String, Object> c : createdAccounts) {
			idList.add(c.get("Id"));
		}
		flow.process(getTestEvent(idList,
				MessageExchangePattern.REQUEST_RESPONSE));

		// Delete the created customers in Netsuite
		List<BaseRefType> baseRefTypeList = new ArrayList<BaseRefType>();
		
		flow = getSubFlow("deleteCustomersFromNetsuiteFlow");
		flow.initialise();
		for (Map<String, Object> c : createdAccounts) {
			Map<String, Object> customer = invokeRetrieveFlow(retrieveCustomerFlow,
					c);
			if (customer != null) {
				BaseRefType baseRefType = new BaseRefType();
				baseRefType.setInternalId((String) customer.get("internalId"));
				baseRefType.addSpecificField("type", "CUSTOMER");
				baseRefTypeList.add(baseRefType);
			}
		}
		flow.process(getTestEvent(baseRefTypeList,
				MessageExchangePattern.REQUEST_RESPONSE));
	}

	protected String buildUniqueName(String templateName, String name) {
		String timeStamp = new Long(new Date().getTime()).toString();

		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append(templateName);
		builder.append(timeStamp);

		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> invokeRetrieveFlow(
			SubflowInterceptingChainLifecycleWrapper flow,
			Map<String, Object> payload) throws Exception {
		MuleEvent event = flow.process(getTestEvent(payload,
				MessageExchangePattern.REQUEST_RESPONSE));

		Object resultPayload = event.getMessage().getPayload();
		if (resultPayload instanceof NullPayload) {
			return null;
		} else {
			return (Map<String, Object>) resultPayload;
		}
	}
	
	protected Map<String, Object> createAccount(int sequence) throws ParseException {
		return SfdcObjectBuilder
				.anAccount()
				.with("Name", buildUniqueName(TEMPLATE_NAME, "ReferencedAccountTest" + sequence + "_"))
				.with("BillingCity", "San Francisco")
				.with("BillingCountry", "USA")
				.with("Phone", "123456789")
				.with("Industry", "Education")
				.with("NumberOfEmployees", 9000).build();
	}

	protected Map<String, Object> createOpportunity(int sequence)
			throws ParseException {
		return SfdcObjectBuilder
				.anOpportunity()
				.with("Name",
						buildUniqueName(TEMPLATE_NAME, "OppName" + sequence
								+ "_")).with("StageName", "Negotiation/Review")
				.with("CloseDate", date("2050-10-10")).with("Probability", "1")
				.build();

	}

	private Date date(String dateString) throws ParseException {
		return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
	}

	protected String buildUniqueEmail(String user) {
		String server = "fakemail";

		StringBuilder builder = new StringBuilder();
		builder.append(buildUniqueName(TEMPLATE_NAME, user));
		builder.append("@");
		builder.append(server);
		builder.append(".com");

		return builder.toString();
	}
}
