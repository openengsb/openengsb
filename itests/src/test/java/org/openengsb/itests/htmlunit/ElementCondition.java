package org.openengsb.itests.htmlunit;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public interface ElementCondition {

    boolean isPresent(HtmlPage page);

}
