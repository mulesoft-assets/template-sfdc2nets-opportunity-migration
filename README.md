
# Anypoint Template: Salesforce to Netsuite Opportunity Migration

# License Agreement
This template is subject to the conditions of the 
<a href="https://s3.amazonaws.com/templates-examples/AnypointTemplateLicense.pdf">MuleSoft License Agreement</a>.
Review the terms of the license before downloading and using this template. You can use this template for free 
with the Mule Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case
As a Salesforce admin I want to synchronize Opportunities from Salesforce to Netsuite.

This Anypoint Template should serve as a foundation for the process of migrating Opportunities from Salesforce instance to Netsuite, being able to specify filtering criteria and desired behaviour when an Opportunity already exists in the destination system. 

As implemented, this Anypoint Template leverages the [Batch Module](http://www.mulesoft.org/documentation/display/current/Batch+Processing).

Firstly the Anypoint Template will query Salesforce for all the existing Opportunities that match the filtering criteria. Account in Salesforce is represented by Customer in Netsuite. In the Saleforce it is possible to have an Opportunity without associated Account but in the Netsuite the Customer field is required in the Opportunity. The template will fetch and migrate only Opportunities with associated Account.

In the Process stage Customer is looked up by Opportunity Account name. If the Customer is found, the first one is selected to be used in Netsuite Opportunity. If it is not found, then new Customer is created with user defined subsidiary.

The last step of the Process stage will upsert the Opportunities into Netsuite based on the externalId field of the Netsuite Opportunity which should match the Salesforse Opportunity Id.

Finally during the On Complete stage the Anypoint Template will print output statistics data into the console and send a notification e-mail with the results of the batch execution.

# Considerations

To make this Anypoint Template run, there are certain preconditions that must be considered. All of them deal with the preparations in both, that must be made in order for all to run smoothly. **Failling to do so could lead to unexpected behavior of the template.**

For correct mapping of the Salesforce Opportunity *stage* to Netsuite Opportunity *status*, check DataWeave function stageToStatus() in *Opportunity To OPPORTUNITY* mapping. It should be checked if it contains correct data. The predefined data can be modified or new data can be added. If the Salesforce Opportunity *stage* is not found in the table the default value will be used. It can be specified in the *nets.opportunity.status.internalId* property.

The relevant data can be found this way:

+ **Salesforce**: Setup -> Customize -> Opportunities -> Fields -> Stage
+ **Netsuite**: Setup -> Sales -> Customer Statuses

Customer must be assigned to subsidiary. In this template, this is done statically and you must configure the property file with subsidiary *internalId* that is already in the system. You can find out this number by entering 'subsidiaries' 
into the NetSuite search field and selecting 'Page - Subsidiaries'. When you click on the 'View' next to the subsidiary chosen, you will see the ID in the URL line. Please, use this Id to populate *nets.subsidiaryId* property in the property file.



## Salesforce Considerations

Here's what you need to know about Salesforce to get this template to work.

### FAQ

- Where can I check that the field configuration for my Salesforce instance is the right one? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US">Salesforce: Checking Field Accessibility for a Particular Field</a>
- Can I modify the Field Access Settings? How? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US">Salesforce: Modifying Field Access Settings</a>

### As a Data Source

If the user who configured the template for the source system does not have at least *read only* permissions for the fields that are fetched, then an *InvalidFieldFault* API fault displays.

```
java.lang.RuntimeException: [InvalidFieldFault [ApiQueryFault [ApiFault  exceptionCode='INVALID_FIELD'
exceptionMessage='
Account.Phone, Account.Rating, Account.RecordTypeId, Account.ShippingCity
^
ERROR at Row:1:Column:486
No such column 'RecordTypeId' on entity 'Account'. If you are attempting to use a custom field, be sure to append the '__c' after the custom field name. Reference your WSDL or the describe call for the appropriate names.'
]
row='1'
column='486'
]
]
```






## NetSuite Considerations


### As a Data Destination

There are no considerations with using NetSuite as a data destination.




# Run it!
Simple steps to get Salesforce to Netsuite Opportunity Migration running.
In any of the ways you would like to run this Anypoint Template this is an example of the output you'll see after hitting the HTTP endpoint:

<pre>
<h1>Batch Process initiated</h1>
<b>ID:</b>6eea3cc6-7c96-11e3-9a65-55f9f3ae584e<br/>
<b>Records to Be Processed: </b>9<br/>
<b>Start execution on: </b>Thu Apr 23 08:05:33 CEST 2015
</pre>

## Running On Premises
In this section we help you run your template on your computer.


### Where to Download Anypoint Studio and the Mule Runtime
If you are a newcomer to Mule, here is where to get the tools.

+ [Download Anypoint Studio](https://www.mulesoft.com/platform/studio)
+ [Download Mule runtime](https://www.mulesoft.com/lp/dl/mule-esb-enterprise)


### Importing a Template into Studio
In Studio, click the Exchange X icon in the upper left of the taskbar, log in with your
Anypoint Platform credentials, search for the template, and click **Open**.


### Running on Studio
After you import your template into Anypoint Studio, follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources.
+ Complete all the properties required as per the examples in the "Properties to Configure" section.
+ Right click the template project folder.
+ Hover your mouse over `Run as`
+ Click `Mule Application (configure)`
+ Inside the dialog, select Environment and set the variable `mule.env` to the value `dev`
+ Click `Run`


### Running on Mule Standalone
Complete all properties in one of the property files, for example in mule.prod.properties and run your app with the corresponding environment variable. To follow the example, this is `mule.env=prod`. 
After this, to trigger the use case you just need to hit the local http endpoint with the port you configured in your file. If this is, for instance, `9090` then you should hit: `http://localhost:9090/migrate` and report will be sent to the e-mails configured.

## Running on CloudHub
While creating your application on CloudHub (or you can do it later as a next step), go to Runtime Manager > Manage Application > Properties to set the environment variables listed in "Properties to Configure" as well as the **mule.env**.
Once your app is all set and started, supposing you choose as domain name `sfdc2netsOpportunityMigration` to trigger the use case you just need to hit `http://sfdc2netsOpportunityMigration.cloudhub.io/migrate` and report will be sent to the e-mails configured.

### Deploying your Anypoint Template on CloudHub
Studio provides an easy way to deploy your template directly to CloudHub, for the specific steps to do so check this


## Properties to Configure
To use this template, configure properties (credentials, configurations, etc.) in the properties file or in CloudHub from Runtime Manager > Manage Application > Properties. The sections that follow list example values.
### Application Configuration
+ http.port `9090`
+ page.size `200`		

#### Salesforce Connector configuration

+ sfdc.a.username `bob.dylan@orga`
+ sfdc.a.password `DylanPassword123`
+ sfdc.a.securityToken `avsfwCUl7apQs56Xq2AKi3X`
+ sfdc.a.url `https://login.salesforce.com/services/Soap/u/32.0`

#### Netsuite Connector configuration

+ nets.email `email@example.com`
+ nets.password `password`
+ nets.account `ABCDEF1234567`
+ nets.roleId `1`
+ nets.applicationId `77EBCBD6-AF9F-11E5-BF7F-FEFF819CDC9F`

#### Netsuite required fields

+ nets.customer.subsidiary.internalId `1`
+ nets.opportunity.status.internalId `10`

#### SMTP Services configuration

+ smtp.host `smtp.gmail.com`
+ smtp.port `587`
+ smtp.user `email%40example.com`
+ smtp.password `password`

#### Email Details

+ mail.from `batch.migrateOpportunities.migration%40mulesoft.com`
+ mail.to `your@email.com`
+ mail.subject `Batch Job Finished Report`

# API Calls
Salesforce imposes limits on the number of API Calls that can be made. However, we make API call to Salesforce only once during migration, so this is not something to worry about.


# Customize It!
This brief guide intends to give a high level idea of how this template is built and how you can change it according to your needs.
As Mule applications are based on XML files, this page describes the XML files used with this template.

More files are available such as test classes and Mule application files, but to keep it simple, we focus on these XML files:

* config.xml
* businessLogic.xml
* endpoints.xml
* errorHandling.xml


## config.xml
Configuration for connectors and configuration properties are set in this file. Even change the configuration here, all parameters that can be modified are in properties file, which is the recommended place to make your changes. However if you want to do core changes to the logic, you need to modify this file.

In the Studio visual editor, the properties are on the *Global Element* tab.


## businessLogic.xml
Functional aspect of the Anypoint Template is implemented on this XML, directed by one flow responsible of excecuting the logic.
For the pourpose of this particular Anypoint Template the *mainFlow* just excecutes a [Batch Job](http://www.mulesoft.org/documentation/display/current/Batch+Processing). which handles all the logic of it:

1. Job execution is invoked from triggerFlow (endpoints.xml).
2. During the Process stage, each Opportunity will be filtered based on specified criteria. 
3. Account associated with Salesforce Opportunity is migrated to Customer associated with Opportunity in Netsuite. The matching is performed by querying a Netsuite instance for an entry with companyName same as the given Salesforce Account name.
4. The next step will insert a new Opportunity record into the Netsuite instance if there was none found in the previous step or update the existing one.
5. The final step will be sending execution report with statistics to e-mail adresses set.



## endpoints.xml
This is the file where you will find the inbound and outbound sides of your integration app.
This Anypoint Template has a [HTTP Listener Connector](http://www.mulesoft.org/documentation/display/current/HTTP+Listener+Connector) as the way to trigger the use case.

### Trigger Flow
**HTTP Listener Connector** - Start Report Generation

+ `${http.port}` is set as a property to be defined either on a property file or in CloudHub environment variables.
+ The path configured by default is `migrateopportunities` and you are free to change for the one you prefer.
+ The host name for all endpoints in your CloudHub configuration should be defined as `0.0.0.0`. CloudHub will then route requests from your application domain URL to the endpoint.



## errorHandling.xml
This is the right place to handle how your integration reacts depending on the different exceptions. 
This file provides error handling that is referenced by the main flow in the business logic.




