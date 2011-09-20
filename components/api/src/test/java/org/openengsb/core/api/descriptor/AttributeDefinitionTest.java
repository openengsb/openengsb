/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.api.descriptor;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openengsb.core.api.descriptor.AttributeDefinition.Builder;
import org.openengsb.core.api.l10n.PassThroughLocalizableString;
import org.openengsb.core.api.l10n.StringLocalizer;
import org.openengsb.core.api.validation.FieldValidator;
import org.openengsb.core.api.validation.SingleAttributeValidationResult;

public class AttributeDefinitionTest {

    private StringLocalizer strings;
    private Builder builder;
    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setup() {
        strings = mock(StringLocalizer.class);
        builder = AttributeDefinition.builder(strings);

        when(strings.getString("nameKey")).thenReturn(new PassThroughLocalizableString("name"));
        when(strings.getString("descKey")).thenReturn(new PassThroughLocalizableString("desc"));
        when(strings.getString("defaultValue")).thenReturn(new PassThroughLocalizableString("localized"));
        when(strings.getString("optionLabelKey")).thenReturn(new PassThroughLocalizableString("option"));
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
        assertThat(builder.build().getName().getString(null), is("name"));
        assertThat(builder.build().getDescription().getString(null), is("desc"));
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
        assertThat(builder.build().getOptions().get(0).getLabel().getString(null), is("option"));
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
        @SuppressWarnings("serial")
        FieldValidator fieldValidator = new FieldValidator() {
            @Override
            public SingleAttributeValidationResult validate(String validate) {
                return null;
            }
        };
        builder.validator(fieldValidator);
        AttributeDefinition build = builder.build();
        Assert.assertSame(fieldValidator, build.getValidator());
    }
}
