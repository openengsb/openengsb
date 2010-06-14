/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.util.schema.ClasspathEntityResolver;
import org.openengsb.util.schema.XmlValidator;

public class XmlValidatorTest {
    private XmlValidator validator;

    @Before
    public void setUp() {
        validator = new XmlValidator();
        ClasspathEntityResolver res = new ClasspathEntityResolver();
        res.setPathValidationString("http://www.openengsb.org");
        validator.setEntityResolver(res);
    }

    @Test
	public void validSampleMessage_shouldValidate() throws Exception {
		String message = IOUtils.toString(IO.getResourceAsStream("valid-sample-message.xml"));
		assertThat(validator.isWellFormed(message), is(true));
		assertThat(validator.validate(message), is(true));
	}

    @Test
    public void messageMissingSchemaLocation_shouldNotValidate() throws Exception {
        String message = IOUtils.toString(IO.getResourceAsStream("invalid-missing-schema.xml"));
        assertThat(validator.isWellFormed(message), is(true));
        assertThat(validator.validate(message), is(false));
    }

    @Test
    public void messageMissingFirstElement_shouldNotValidate() throws Exception {
        String message = IOUtils.toString(IO.getResourceAsStream("invalid-missing-element.xml"));
        assertThat(validator.isWellFormed(message), is(true));
        assertThat(validator.validate(message), is(false));
    }

    @Test
    public void messageMissingTag_shouldNotBeWellformedAndNotValidate() throws Exception {
        String message = IOUtils.toString(IO.getResourceAsStream("invalid-missing-tag.xml"));
        assertThat(validator.isWellFormed(message), is(false));
    }
}
