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

package org.openengsb.ui.admin.edb;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;
import org.openengsb.core.api.security.annotation.SecurityAttribute;
import org.openengsb.core.api.security.annotation.SecurityAttributes;
import org.openengsb.core.ekb.api.EDBQueryFilter;
import org.openengsb.core.ekb.api.EKBService;
import org.openengsb.core.ekb.api.SingleModelQuery;
import org.openengsb.labs.delegation.service.ClassProvider;
import org.openengsb.ui.admin.basePage.BasePage;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

@SecurityAttributes({ @SecurityAttribute(key = "org.openengsb.ui.component", value = "EDB") })
@PaxWicketMountPoint(mountPoint = "edb")
public class EdbClient extends BasePage {

    private static final long serialVersionUID = 9004308842445884996L;

    public static final String PAGE_NAME_KEY = "edbClient.title";
    public static final String PAGE_DESCRIPTION_KEY = "edbClient.description";

    private static final Logger LOGGER = LoggerFactory.getLogger(EdbClient.class);

    static class EkbQuery implements Serializable {

        private static final long serialVersionUID = 1041945216734701953L;

        private Class<? extends OpenEngSBModel> model;
        private String query;

        public Class<? extends OpenEngSBModel> getModel() {
            return model;
        }

        public void setModel(Class<? extends OpenEngSBModel> model) {
            this.model = model;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }

    private final IModel<EkbQuery> queryModel = new Model<EdbClient.EkbQuery>(new EkbQuery());

    @Inject
    @Named("queryInterface")
    private EKBService ekbService;

    @Inject
    @Named("modelProviders")
    private List<ClassProvider> modelProviders;

    private TextField<String> queryField;

    private IModel<List<? extends OpenEngSBModel>> resultModel;

    private WebMarkupContainer resultContainer;

    private FeedbackPanel feedback;

    private class DomainModelListModel extends LoadableDetachableModel<List<Class<? extends OpenEngSBModel>>> {
        private static final long serialVersionUID = 608313722757619758L;

        @SuppressWarnings("unchecked")
        @Override
        protected List<Class<? extends OpenEngSBModel>> load() {
            List<Class<? extends OpenEngSBModel>> result = new ArrayList<Class<? extends OpenEngSBModel>>();
            for (ClassProvider p : modelProviders) {
                for (Class<?> c : p.listClasses()) {
                    if (!OpenEngSBModel.class.isAssignableFrom(c)) {
                        LOGGER.error("Class {} was not correctly woven, so it will not appear in model-dropdown",
                                c.getName());
                        continue;
                    }
                    result.add((Class<? extends OpenEngSBModel>) c);
                }

            }
            return result;
        }
    }

    public EdbClient() {
        Form<Object> form = new Form<Object>("form");
        final DropDownChoice<Class<? extends OpenEngSBModel>> modelSelector = new DropDownChoice<Class<? extends OpenEngSBModel>>(
                "modelSelector", new Model<Class<? extends OpenEngSBModel>>(), new DomainModelListModel());
        modelSelector.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            private static final long serialVersionUID = -1516333824153580148L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                Class<? extends OpenEngSBModel> convertedInput = modelSelector.getConvertedInput();
                queryModel.getObject().setModel(convertedInput);
                queryField.setEnabled(convertedInput != null);
                target.add(queryField);
            }
        });
        modelSelector.setChoiceRenderer(new IChoiceRenderer<Class<?>>() {
            private static final long serialVersionUID = 805430071751617166L;

            @Override
            public Object getDisplayValue(Class<?> object) {
                return object.getSimpleName();
            }

            @Override
            public String getIdValue(Class<?> object, int index) {
                return object.getSimpleName();
            }

        });
        form.add(modelSelector);
        queryField = new AutoCompleteTextField<String>("query") {
            private static final long serialVersionUID = 5028249986331789802L;

            @Override
            protected Iterator<String> getChoices(final String input) {
                Class<? extends OpenEngSBModel> model = queryModel.getObject().getModel();
                BeanInfo beanInfo;
                try {
                    beanInfo = Introspector.getBeanInfo(model);
                } catch (IntrospectionException e) {
                    LOGGER.warn("error introspecting {}. Auto-completing won't work." + model);
                    List<String> emptyList = Collections.emptyList();
                    return emptyList.iterator();
                }
                List<String> allKeys = Lists.transform(Arrays.asList(beanInfo.getPropertyDescriptors()),
                        new Function<PropertyDescriptor, String>() {
                            @Override
                            public String apply(PropertyDescriptor input) {
                                return input.getName() + ":";
                            }
                        });
                if (Strings.isNullOrEmpty(input)) {
                    return allKeys.iterator();
                }
                return Iterators.filter(allKeys.iterator(), new Predicate<String>() {
                    @Override
                    public boolean apply(String item) {
                        return item.contains(input);
                    }
                });
            }

        };
        queryField.setEnabled(false);
        queryField.setOutputMarkupId(true);
        queryField.setModel(new PropertyModel<String>(queryModel.getObject(), "query"));
        form.add(queryField);

        form.add(new IndicatingAjaxButton("submit") {
            private static final long serialVersionUID = -5425144434508998591L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                EkbQuery query = queryModel.getObject();
                List<? extends OpenEngSBModel> models;
                try {
                    models = ekbService.query(new SingleModelQuery(query.getModel(), new EDBQueryFilter(query
                            .getQuery()), null));
                    resultModel.setObject(models);
                    info(String.format("Found %s results", models.size()));
                } catch (Exception e) {
                    error(String.format("Error when querying for models %s (%s)", e.getMessage(), e.getClass()
                            .getName()));
                }
                target.add(feedback);
                target.add(resultContainer);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            }
        });
        add(form);
        List<? extends OpenEngSBModel> emptyList = Collections.emptyList();
        resultModel = Model.ofList(emptyList);
        resultContainer = new WebMarkupContainer("result");
        resultContainer.setOutputMarkupId(true);
        add(resultContainer);
        resultContainer.add(new ListView<OpenEngSBModel>("list", resultModel) {
            private static final long serialVersionUID = 5459114215962851286L;

            @Override
            protected void populateItem(ListItem<OpenEngSBModel> item) {
                String idProperty = "id";
                Class<? extends OpenEngSBModel> modelClass = queryModel.getObject().getModel();
                for (Field m : modelClass.getDeclaredFields()) {
                    if (m.getAnnotation(OpenEngSBModelId.class) != null) {
                        idProperty = m.getName();
                        break;
                    }
                }
                AjaxLink<String> historyLink = new AjaxLink<String>("id", new PropertyModel<String>(item
                        .getModelObject(), idProperty)) {
                    private static final long serialVersionUID = -6539033599615376277L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        this.setResponsePage(new EdbHistoryPanel(getModel().getObject()));
                    }
                };
                historyLink.add(new Label("text", new PropertyModel<String>(item.getModelObject(), idProperty)));
                item.add(historyLink);
                MultiLineLabel multiLineLabel = new MultiLineLabel("entries", item.getModelObject()
                        .toOpenEngSBModelEntries().toString());
                item.add(multiLineLabel);
            }
        });
        feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        form.add(feedback);
    }
}
