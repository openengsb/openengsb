package org.openengsb.ui.web;

import org.apache.servicemix.jbi.deployer.AdminCommandsService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class Index extends BasePage {

	// @SpringBean(name = "testDao")
	// private TestDaoInteface dao;

	@SpringBean(name = "testService")
	private AdminCommandsService adminCmd;

	public Index() {
		add(new Label("helloworld", "Hello World"));
	}

}
