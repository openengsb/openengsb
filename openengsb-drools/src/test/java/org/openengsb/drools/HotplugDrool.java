package org.openengsb.drools;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Properties;

import javax.jbi.messaging.InOnly;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.drools.DroolsComponent;
import org.apache.servicemix.drools.DroolsEndpoint;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.drools.RuleBase;
import org.drools.agent.RuleAgent;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class HotplugDrool {

	private static final String ruleString = "package org.openengsb.drools"
			+ "\n" + "import org.openengsb.drools.model.Event" + "\n"
			+ "rule \"Hello2\"" + "\n" + "when" + "\n"
			+ "e: Event(name == \"greet\", handled == false)" + "\n" + "then"
			+ "\n" + "	System.out.println(\"Hi\"); e.setHandled(true);" + "\n"
			+ "end";

	private JBIContainer jbi;
	private DroolsComponent drools;
	private ServiceMixClient client;

	private static final String URL = "http://localhost:8080/drools-guvnor/org.drools.guvnor.Guvnor/package/org.openengsb/LATEST";

	@Before
	public void setUp() throws Exception {
		jbi = new JBIContainer();
		jbi.setEmbedded(true);
		jbi.init();
		client = new DefaultServiceMixClient(jbi);
	}

	@After
	public void tearDown() throws Exception {
		jbi.shutDown();
	}
	
	@Ignore
	@Test
	public void testHotplugHelloWorld() throws Exception {

		drools = new DroolsComponent();
		DroolsEndpoint endpoint = new DroolsEndpoint(drools.getServiceUnit(),
				new QName("drools"), "endpoint");
		endpoint
				.setRuleBaseResource(new ClassPathResource("openengsb-base.drl"));
		endpoint.setGlobals(new HashMap<String, Object>());
		endpoint.getGlobals().put("xml", new XmlHelper());
		drools.setEndpoints(new DroolsEndpoint[] { endpoint });
		jbi.activateComponent(drools, "servicemix-drools");

		jbi.start();

		InOnly me = client.createInOnlyExchange();
		me.setService(new QName("drools"));
		me.getInMessage().setContent(
				new StringSource("<event><name>test</name></event>"));
		me.getInMessage().setProperty("prop", Boolean.TRUE);
		client.sendSync(me, 1000);
		// client.done(me);

		me = client.createInOnlyExchange();
		me.setService(new QName("drools"));
		me.getInMessage().setContent(
				new StringSource("<event><name>greet</name></event>"));
		me.getInMessage().setProperty("prop", Boolean.TRUE);
		client.sendSync(me, 1000);

//		endpoint.addDrlRule(new StringReader(ruleString));

		me = client.createInOnlyExchange();
		me.setService(new QName("drools"));
		me.getInMessage().setContent(
				new StringSource("<event><name>greet</name></event>"));
		me.getInMessage().setProperty("prop", Boolean.TRUE);
		client.sendSync(me, 1000);

	}

	@Test
	public void testGuvnorConnect() throws Exception {
		drools = new DroolsComponent();
		DroolsEndpoint endpoint = new DroolsEndpoint(drools.getServiceUnit(),
				new QName("drools"), "endpoint");
		// endpoint
		// .setRuleBaseResource(new ClassPathResource("openengsb-base.drl"));
		Properties config = new Properties();
		config.put("url", URL);
		RuleAgent agent = RuleAgent.newRuleAgent(config);
		RuleBase ruleBase = agent.getRuleBase();
		endpoint.setRuleBase(ruleBase);
		endpoint.setGlobals(new HashMap<String, Object>());
		// endpoint.getGlobals().put("xml", new XmlHelper());
		drools.setEndpoints(new DroolsEndpoint[] { endpoint });
		jbi.activateComponent(drools, "servicemix-drools");

		jbi.start();
		
		InOnly me = client.createInOnlyExchange();
		me.setService(new QName("drools"));
		me.getInMessage().setContent(
				new StringSource("<event><name>hello</name></event>"));
		me.getInMessage().setProperty("prop", Boolean.TRUE);
		client.sendSync(me, 1000);
		
//		Source answer = client.receive(1000).getMessage("out").getContent();
//		System.out.println("answer" + answer);
		

	}

}
