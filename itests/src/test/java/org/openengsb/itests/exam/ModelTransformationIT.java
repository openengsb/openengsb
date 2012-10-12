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

package org.openengsb.itests.exam;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.transformations.TransformationUtils;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.openengsb.core.ekb.api.transformation.TransformationOperation;
import org.openengsb.core.ekb.api.transformation.TransformationOperationException;
import org.openengsb.core.util.ModelUtils;
import org.openengsb.core.workflow.api.RuleManager;
import org.openengsb.core.workflow.api.WorkflowService;
import org.openengsb.core.workflow.api.model.RuleBaseElementId;
import org.openengsb.core.workflow.api.model.RuleBaseElementType;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

@RunWith(JUnit4TestRunner.class)
public class ModelTransformationIT extends AbstractPreConfiguredExamTestHelper {
    private TransformationEngine transformationEngine;
    private Version exampleDomainVersion;

    @Before
    public void setup() throws Exception {
        transformationEngine = getOsgiService(TransformationEngine.class);
        Bundle b = getInstalledBundle("org.openengsb.domain.example");
        exampleDomainVersion = b.getVersion();
    }

    @Test
    public void testIfServiceIsFound_shouldWork() throws Exception {
        assertThat(transformationEngine, notNullValue());
    }

    private ModelDescription getExampleRequestDescription() {
        return new ModelDescription(ExampleRequestModel.class, exampleDomainVersion);
    }

    private ModelDescription getExampleResponseDescription() {
        return new ModelDescription(ExampleResponseModel.class, exampleDomainVersion);
    }

    private ExampleResponseModel transformRequestToResponse(ExampleRequestModel model) {
        return (ExampleResponseModel) transformationEngine.performTransformation(getExampleRequestDescription(),
            getExampleResponseDescription(), model);
    }

    private ExampleRequestModel transformResponseToRequest(ExampleResponseModel model) {
        return (ExampleRequestModel) transformationEngine.performTransformation(getExampleResponseDescription(),
            getExampleRequestDescription(), model);
    }

    @Test
    public void testIfTransformationWorks_shouldWork() throws Exception {
        TransformationDescription description =
            new TransformationDescription(getExampleRequestDescription(), getExampleResponseDescription());
        description.concatField("result", "-", "name", "id");
        transformationEngine.saveDescription(description);

        ExampleRequestModel modelA = new ExampleRequestModel();
        modelA.setName("test");
        modelA.setId(42);

        ExampleResponseModel modelB = transformRequestToResponse(modelA);

        assertThat(modelB.getResult(), is("test-42"));
    }

    @Test
    public void testIfTransformationsFromFileWork_shouldWork() throws Exception {
        loadDescriptionsFromFile();
        ExampleResponseModel modelA = new ExampleResponseModel();
        modelA.setResult("test-42");

        ExampleRequestModel modelB = transformResponseToRequest(modelA);

        transformationEngine.deleteDescriptionsByFile("testDescription.transformation");
        assertThat(modelB.getName(), is("test"));
    }

    @Test
    public void testIfCustomTransformationsWork_shouldWork() throws Exception {
        BundleContext context = getBundleContext();
        Dictionary<String, String> dictionary = new Hashtable<String, String>();
        dictionary.put("transformation.operation", "dummy");
        context.registerService(TransformationOperation.class, new DummyTransformationOperation(), dictionary);

        TransformationDescription description =
            new TransformationDescription(getExampleResponseDescription(), getExampleRequestDescription());
        description.addStep("dummy", Arrays.asList("result"), "name", new HashMap<String, String>());
        transformationEngine.saveDescription(description);
        
        ExampleResponseModel modelA = new ExampleResponseModel();
        modelA.setResult("teststring");
        
        ExampleRequestModel modelB = transformResponseToRequest(modelA);

        assertThat(modelB.getName(), is("DUMMYteststringDUMMY"));
    }

    @Test
    public void testCallTransformerFromWorkflowRule_shouldWork() throws Exception {
        loadDescriptionsFromFile();
        DummyLogDomain exampleMock = new DummyLogDomain();
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("domain", "example");
        properties.put("connector", "example");
        properties.put("location.foo", "example2");
        getBundleContext().registerService(ExampleDomain.class.getName(), exampleMock, properties);

        RuleManager ruleManager = getOsgiService(RuleManager.class);

        ruleManager.addImport(ExampleDomain.class.getName());
        ruleManager.addImport(LogEvent.class.getName());
        ruleManager.addImport(TransformationEngine.class.getName());
        ruleManager.addImport(ModelUtils.class.getName());
        ruleManager.addImport(ExampleResponseModel.class.getName());
        ruleManager.addImport(ExampleRequestModel.class.getName());
        ruleManager.addImport(ModelDescription.class.getName());
        ruleManager.addImport(OpenEngSBModelEntry.class.getName());

        ruleManager.addGlobal(ExampleDomain.class.getName(), "example2");
        ruleManager.addGlobal(TransformationEngine.class.getName(), "ekbTransformationService");

        String sourceDescription = "ModelDescription source = new ModelDescription(\"%s\", \"%s\");";
        String targetDescription = "ModelDescription target = new ModelDescription(\"%s\", \"%s\");";
        sourceDescription =
            String.format(sourceDescription, ExampleResponseModel.class.getName(), exampleDomainVersion.toString());
        targetDescription =
            String.format(targetDescription, ExampleRequestModel.class.getName(), exampleDomainVersion.toString());

        ruleManager
            .add(
                new RuleBaseElementId(RuleBaseElementType.Rule, "example"),
                ""
                        + "when\n"
                        + "  event : LogEvent()\n"
                        + "then\n"
                        + sourceDescription
                        + targetDescription
                        + "  ExampleResponseModel object = new ExampleResponseModel();"
                        + "  object.setResult(\"test-42\");"
                        + "  ExampleRequestModel model = "
                        + "(ExampleRequestModel) "
                        + "ekbTransformationService.performTransformation(source, target, object);"
                        + "  example2.doSomethingWithModel(model);\n"
            );

        ContextHolder.get().setCurrentContextId("foo");
        WorkflowService workflowService = getOsgiService(WorkflowService.class);

        authenticate("admin", "password");
        workflowService.processEvent(new LogEvent());

        transformationEngine.deleteDescriptionsByFile("testDescription.transformation");
        ExampleRequestModel result = exampleMock.getModel();
        assertThat(result.getName(), is("test"));
    }

    private void loadDescriptionsFromFile() throws Exception {
        InputStream stream =
            getClass().getClassLoader().getResourceAsStream("transformations/testDescription.transformation");
        List<TransformationDescription> descriptions = TransformationUtils.getDescriptionsFromXMLInputStream(stream);
        for (TransformationDescription description : descriptions) {
            description.getSourceModel().setVersion(exampleDomainVersion);
            description.getTargetModel().setVersion(exampleDomainVersion);
        }
        transformationEngine.saveDescriptions(descriptions);
    }

    public static class DummyLogDomain extends AbstractOpenEngSBService implements ExampleDomain {
        private ExampleRequestModel model;

        @Override
        public String doSomethingWithMessage(String message) {
            return "something";
        }

        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
        }

        @Override
        public String doSomethingWithLogEvent(LogEvent event) {
            return "something";
        }

        public boolean isWasCalled() {
            return true;
        }

        @Override
        public ExampleResponseModel doSomethingWithModel(ExampleRequestModel model) {
            this.model = model;
            return null;
        }

        public ExampleRequestModel getModel() {
            return model;
        }
    }

    public static class DummyTransformationOperation implements TransformationOperation {

        @Override
        public String getOperationDescription() {
            return "";
        }

        @Override
        public Integer getOperationInputCount() {
            return 1;
        }

        @Override
        public String getOperationName() {
            return "dummy";
        }

        @Override
        public Map<String, String> getOperationParameterDescriptions() {
            return new HashMap<String, String>();
        }

        @Override
        public Object performOperation(List<Object> arg0, Map<String, String> arg1)
            throws TransformationOperationException {
            return String.format("DUMMY%sDUMMY", arg0.get(0).toString());
        }

    }

}
