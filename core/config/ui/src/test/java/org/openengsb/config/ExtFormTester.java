/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.config;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;

public class ExtFormTester {
    private final FormTester formTester;
    private final WicketTester tester;
    private final String formPath;
    private final ExtWicketTester extTester;

    public ExtFormTester(WicketTester tester, String path) {
        this(tester, tester.newFormTester(path), path);
    }

    public ExtFormTester(WicketTester tester, FormTester formTester, String path) {
        this.formTester = formTester;
        this.tester = tester;
        this.formPath = path;
        this.extTester = new ExtWicketTester(tester);
    }

    public ExtFormTester selectDropDownChoice(String path, int index) {
        String fullPath = formPath + ":" + path;
        extTester.assertEnabled(fullPath, DropDownChoice.class);
        formTester.select(path, index);
        return this;
    }

    public ExtFormTester setTextFieldValue(String path, String value) {
        String fullPath = formPath + ":" + path;
        extTester.assertEnabled(fullPath, TextField.class);
        formTester.setValue(path, value);
        return this;
    }

    public ExtFormTester setTextAreaValue(String path, String value) {
        String fullPath = formPath + ":" + path;
        extTester.assertEnabled(fullPath, TextArea.class);
        formTester.setValue(path, value);
        return this;
    }

    public void submitWithButton(String path) {
        String fullPath = formPath + ":" + path;
        extTester.assertEnabled(fullPath, Button.class);
        formTester.submit(path);
    }

    public void submitWithSubmitLink(String path) {
        String fullPath = formPath + ":" + path;
        extTester.assertEnabled(fullPath, SubmitLink.class);
        formTester.submit(path);
    }
}
