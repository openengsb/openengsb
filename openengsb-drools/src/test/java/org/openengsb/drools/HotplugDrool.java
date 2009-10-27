package org.openengsb.drools;

import java.io.StringReader;
import java.util.HashMap;

import javax.jbi.messaging.InOnly;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.drools.DroolsComponent;
import org.apache.servicemix.drools.DroolsEndpoint;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class HotplugDrool {

	private static final String ruleString = "package org.openengsb.drools"
			+ "\n" + "import org.openengsb.drools.model.Event" + "\n"
			+ "rule \"Hello2\"" + "\n" + "when" + "\n"
			+ "e: Event(name == \"greet\", handled == false)" + "\n" + "then" + "\n"
			+ "	System.out.println(\"Hi\"); e.setHandled(true);" + "\n" + "end";

	private JBIContainer jbi;
	private DroolsComponent drools;
	private ServiceMixClient client;

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
		me.getInMessage().setContent(new StringSource("<event><name>test</name></event>"));
		me.getInMessage().setProperty("prop", Boolean.TRUE);
		client.sendSync(me, 1000);
//		client.done(me);
		
		me = client.createInOnlyExchange();
		me.setService(new QName("drools"));
		me.getInMessage().setContent(new StringSource("<event><name>greet</name></event>"));
		me.getInMessage().setProperty("prop", Boolean.TRUE);
		client.sendSync(me, 1000);
		
		endpoint.addDrlRule(new StringReader(ruleString));
		
		me = client.createInOnlyExchange();
		me.setService(new QName("drools"));
		me.getInMessage().setContent(new StringSource("<event><name>greet</name></event>"));
		me.getInMessage().setProperty("prop", Boolean.TRUE);
		client.sendSync(me, 1000);
		
		// Element e = new SourceTransformer().toDOMElement(me.getOutMessage());
		// assertEquals("result", e.getLocalName());
		// assertEquals("12586269025", e.getTextContent());
		// client.done(me);

		// me = client.createInOutExchange();
		// me.setService(new QName("drools"));
		// me.getInMessage().setContent(
		// new StringSource("<event>Greet</event>"));
		// me.getInMessage().setProperty("prop", Boolean.TRUE);
		// client.sendSync(me);
		// assertNotNull(me.getFault());
		// client.done(me);

//		Thread.sleep(500);
	}

}
