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
package org.openengsb.core.common.descriptor;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.descriptor.AttributeDefinition.Builder;
import org.openengsb.core.common.util.BundleStrings;
import org.openengsb.core.common.validation.FieldValidator;
import org.openengsb.core.common.validation.FieldValidationResult;

public class AttributeDefinitionTest {

    private Locale locale;
    private BundleStrings strings;
    private Builder builder;
    @Rule
    public final ExpectedException expected = ExpectedException.none();

    @Before
    public void setup() {
        locale = new Locale("en");
        strings = mock(BundleStrings.class);
        builder = AttributeDefinition.builder(locale, strings);

        when(strings.getString("nameKey", locale)).thenReturn("name");
        when(strings.getString("descKey", locale)).thenReturn("desc");
        when(strings.getString("defaultValue", locale)).thenReturn("localized");
        when(strings.getString("optionLabelKey", locale)).thenReturn("option");
        builder.id("a");
        builder.name("nameKey");
        builder.description("descKey");
    }

    protected void assertBuildException(String expectedMessage) {
        expected.expect(IllegalStateException.class);
        expected.expectMessage(expectedMessage);
        builder.build();
    }

    @Test
    public void builderShouldLocalizeNameAndDescription() {
        assertThat(builder.build().getName(), is("name"));
        assertThat(builder.build().getDescription(), is("desc"));
    }

    @Test
    public void builderShouldNotLocalizeDefaultValue() {
        builder.defaultValue("defaultValue");
        assertThat(builder.build().getDefaultValue(), is("defaultValue"));
    }

    @Test
    public void builderShouldLocalizeForcedDefaultValueLocalization() {
        builder.defaultValueLocalized("defaultValue");
        assertThat(builder.build().getDefaultValue(), is("localized"));
    }

    @Test
    public void buildWithoutId_shouldThrowISE() {
        builder.id(null);
        assertBuildException("id");
    }

    @Test
    public void buildWithoutName_shouldThrowISE() {
        builder.name(null);
        assertBuildException("name");
    }

    @Test
    public void buildWithoutDescription_shouldBeAllowed() {
        builder.description(null);
        builder.build();
    }

    @Test
    public void builderShouldLocalizeOptionLabel() {
        builder.option("optionLabelKey", "value");
        assertThat(builder.build().getOptions().get(0).getValue(), is("value"));
        assertThat(builder.build().getOptions().get(0).getLabel(), is("option"));
    }

    @Test
    public void emptyOptionLabel_shouldThrowISE() {
        builder.option("", "value");
        assertBuildException("option");
    }

    @Test
    public void emptyOptionValue_shouldThrowISE() {
        builder.option("optionLabelKey", "");
        assertBuildException("option");
    }

    @Test
    public void buildWithOptionAndBooleanSettings_shouldThrowISE() {
        builder.option("optionLabelKey", "value");
        builder.asBoolean();
        assertBuildException("boolean");
    }

    @Test
    public void buildWithOptionAndPasswordSettings_shouldThrowISE() {
        builder.option("optionLabelKey", "value");
        builder.asPassword();
        assertBuildException("password");
    }

    @Test
    public void buildWithBooleanAndPasswordSettings_shouldThrowISE() {
        builder.asBoolean();
        builder.asPassword();
        assertBuildException("password");
    }

    @Test
    public void buildWithValidator_shouldReturnSameValidator() {
        FieldValidator fieldValidator = new FieldValidator() {
            @Override
            public FieldValidationResult validate(String validate) {
                return null;
            }
        };
        builder.validator(fieldValidator);
        AttributeDefinition build = builder.build();
        Assert.assertSame(fieldValidator, build.getValidator());
    }
}
