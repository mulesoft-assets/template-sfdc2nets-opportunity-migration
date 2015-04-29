/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;

import com.netsuite.webservices.platform.core_2014_1.RecordRef;
import com.sforce.soap.partner.SaveResult;

/**
 * The objective of this class is to validate the correct behavior of the Mule
 * Template that make calls to external systems.
 * 
 * The test will invoke the batch process and afterwards check that the
 * opportunities had been correctly created and that the ones that should be
 * filtered are not in the destination sand box.
 * 
 * The test validates that an account will get sync as result of the
 * integration.
 * 
 * @author cesar.garcia
 */
public class BusinessLogicTestCreateAccountIT extends AbstractTemplateTestCase {

	private List<Map<String, Object>> createdOpportunities = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> createdAccounts = new ArrayList<Map<String, Object>>();
	
	private SubflowInterceptingChainLifecycleWrapper createAccountFlow;
	private SubflowInterceptingChainLifecycleWrapper createOpportunityFlow;

	@Before
	public void setUp() throws Exception {		
		getAndInitializeFlows();
		createTestDataInSandBox();
	}

	@After
	public void tearDown() throws Exception {
		deleteTestDataFromSandBox();
	}
	
	private void getAndInitializeFlows() throws InitialisationException {
		retrieveOpportunityFlow = getSubFlow("retrieveOpportunityFlow");
		retrieveOpportunityFlow.initialise();

		retrieveCustomerFlow = getSubFlow("retrieveCustomerFlow");
		retrieveCustomerFlow.initialise();
		
		createAccountFlow = getSubFlow("createAccountFlow");
		createAccountFlow.initialise();
		
		createOpportunityFlow = getSubFlow("createOpportunityFlow");
		createOpportunityFlow.initialise();
	}

	@Test
	public void testMainFlow() throws Exception {
		runFlow("mainFlow");

		// Wait for the batch job executed by the poll flow to finish
		Thread.sleep(TIMEOUT_SEC * 1000);

		Assert.assertEquals("The opportunity should not have been sync", null, invokeRetrieveFlow(retrieveOpportunityFlow, createdOpportunities.get(0)));
		
		Assert.assertEquals("The account should not have been sync", null, invokeRetrieveFlow(retrieveCustomerFlow, createdAccounts.get(1)));

		Map<String, Object> accountPayload = invokeRetrieveFlow(retrieveCustomerFlow, createdAccounts.get(0));
		Map<String, Object> opportunityPayload = invokeRetrieveFlow(retrieveOpportunityFlow, createdOpportunities.get(1));
		
		Assert.assertEquals("The opportunity should have been sync", createdOpportunities.get(1).get("Name"), opportunityPayload.get("title"));
		Assert.assertEquals("The opportunity should have been sync", "11", ((RecordRef)opportunityPayload.get("entityStatus")).getInternalId());

		Assert.assertEquals("The opportunity should belong to a different customer ", accountPayload.get("internalId"),
				((RecordRef) opportunityPayload.get("entity")).getInternalId());
	}

	private void createTestDataInSandBox() throws MuleException, Exception {
		createAccounts();
		createOpportunities();
	}

	@SuppressWarnings("unchecked")
	private void createAccounts() throws Exception {
		createdAccounts.add(createAccount(0));
		createdAccounts.add(createAccount(1));

		MuleEvent event = createAccountFlow.process(getTestEvent(createdAccounts, MessageExchangePattern.REQUEST_RESPONSE));
		List<SaveResult> results = (List<SaveResult>) event.getMessage().getPayload();
		for (int i = 0; i < results.size(); i++) {
			createdAccounts.get(i).put("Id", results.get(i).getId());
		}

		System.out.println("Results of data creation in sandbox" + createdAccounts.toString());
	}

	@SuppressWarnings("unchecked")
	private void createOpportunities() throws Exception {
		// This opportunity should not be sync
		Map<String, Object> opportunity = createOpportunity(0);
		opportunity.put("Amount", 130000);
		createdOpportunities.add(opportunity);
		
		// This opportunity should BE sync with it's account
		opportunity = createOpportunity(1);
		opportunity.put("Amount", 130000);
		opportunity.put("AccountId", createdAccounts.get(0).get("Id"));
		createdOpportunities.add(opportunity);

		MuleEvent event = createOpportunityFlow.process(getTestEvent(createdOpportunities, MessageExchangePattern.REQUEST_RESPONSE));
		List<SaveResult> results = (List<SaveResult>) event.getMessage().getPayload();
		for (int i = 0; i < results.size(); i++) {
			createdOpportunities.get(i).put("Id", results.get(i).getId());
		}
	}

	private void deleteTestDataFromSandBox() throws MuleException, Exception {
		deleteTestOpportunityFromSandBox(createdOpportunities);
		deleteTestAccountFromSandBox(createdAccounts);
	}

}
