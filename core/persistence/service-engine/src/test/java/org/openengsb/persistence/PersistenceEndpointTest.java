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
package org.openengsb.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.model.ReturnValue;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

public class PersistenceEndpointTest {

    private PersistenceEndpoint endpoint;

    @Before
    public void setUp() throws Exception {
        this.endpoint = new PersistenceEndpoint();
        this.endpoint.setPersistence(getPersistenceImpl());
    }

    protected PersistenceInternal getPersistenceImpl(){
        return new PersistenceMock();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateAndQuerySingle() throws Exception {
        List<TestBean> createList = new ArrayList<TestBean>();
        createList.add(new TestBean("test", new Integer(42)));

        String createInMessage = getListCall("create", createList);
        String createResult = this.endpoint.handlePersistenceCall(createInMessage);

        ReturnValue createReturnValue = Transformer.toReturnValue(createResult);
        Assert.assertNull(createReturnValue.getValue());

        List<TestBean> queryList = new ArrayList<TestBean>();
        queryList.add(new TestBean("test", null));

        String queryInMessage = getListCall("query", queryList);
        String queryResult = this.endpoint.handlePersistenceCall(queryInMessage);

        ReturnValue queryReturnValue = Transformer.toReturnValue(queryResult);
        Assert.assertEquals(List.class, queryReturnValue.getType());
        List<TestBean> outList = (List<TestBean>) queryReturnValue.getValue();

        Assert.assertEquals(1, outList.size());
        Assert.assertEquals("test", outList.get(0).getFoo());
        Assert.assertEquals(new Integer(42), outList.get(0).getBar());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateAndQueryMultiple() throws Exception {
        List<TestBean> createList = new ArrayList<TestBean>();
        createList.add(new TestBean("test", new Integer(42)));
        createList.add(new TestBean("buz", new Integer(21)));
        createList.add(new TestBean("foobarbuz", new Integer(63)));

        String createInMessage = getListCall("create", createList);
        String createResult = this.endpoint.handlePersistenceCall(createInMessage);

        ReturnValue createReturnValue = Transformer.toReturnValue(createResult);
        Assert.assertNull(createReturnValue.getValue());

        List<TestBean> queryList = new ArrayList<TestBean>();
        queryList.add(new TestBean("test", null));
        queryList.add(new TestBean("buz", null));
        queryList.add(new TestBean("foobarbuz", null));

        String queryInMessage = getListCall("query", queryList);
        String queryResult = this.endpoint.handlePersistenceCall(queryInMessage);

        ReturnValue queryReturnValue = Transformer.toReturnValue(queryResult);
        Assert.assertEquals(List.class, queryReturnValue.getType());
        List<TestBean> outList = (List<TestBean>) queryReturnValue.getValue();

        Assert.assertEquals(3, outList.size());
        Assert.assertTrue(queryList.containsAll(outList));
        Assert.assertTrue(outList.containsAll(queryList));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEmptyResultQuery() throws Exception {
        List<TestBean> queryList = new ArrayList<TestBean>();

        String queryInMessage = getListCall("query", queryList);
        String queryResult = this.endpoint.handlePersistenceCall(queryInMessage);

        ReturnValue queryReturnValue = Transformer.toReturnValue(queryResult);
        Assert.assertEquals(List.class, queryReturnValue.getType());
        List<TestBean> outList = (List<TestBean>) queryReturnValue.getValue();

        Assert.assertEquals(0, outList.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDelete() throws Exception {
        List<TestBean> createList = new ArrayList<TestBean>();
        createList.add(new TestBean("test", new Integer(42)));

        String createInMessage = getListCall("create", createList);
        String createResult = this.endpoint.handlePersistenceCall(createInMessage);

        ReturnValue createReturnValue = Transformer.toReturnValue(createResult);
        Assert.assertNull(createReturnValue.getValue());

        List<TestBean> deleteList = new ArrayList<TestBean>();
        deleteList.add(new TestBean("test", null));

        String deleteInMessage = getListCall("delete", deleteList);
        String deleteResult = this.endpoint.handlePersistenceCall(deleteInMessage);
        ReturnValue deleteReturnValue = Transformer.toReturnValue(deleteResult);

        Assert.assertNull(deleteReturnValue.getValue());

        String queryInMessage = getListCall("query", deleteList);
        String queryResult = this.endpoint.handlePersistenceCall(queryInMessage);

        ReturnValue queryReturnValue = Transformer.toReturnValue(queryResult);
        Assert.assertEquals(List.class, queryReturnValue.getType());
        List<TestBean> outList = (List<TestBean>) queryReturnValue.getValue();

        Assert.assertEquals(0, outList.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdate() throws Exception {
        List<TestBean> createList = new ArrayList<TestBean>();
        TestBean bean = new TestBean("test", new Integer(42));
        createList.add(bean);

        String createInMessage = getListCall("create", createList);
        String createResult = this.endpoint.handlePersistenceCall(createInMessage);

        ReturnValue createReturnValue = Transformer.toReturnValue(createResult);
        Assert.assertNull(createReturnValue.getValue());

        String deleteInMessage = getUpdateCall(bean, new TestBean("foo", new Integer(21)));
        String deleteResult = this.endpoint.handlePersistenceCall(deleteInMessage);
        ReturnValue deleteReturnValue = Transformer.toReturnValue(deleteResult);

        Assert.assertNull(deleteReturnValue.getValue());

        List<TestBean> queryList = new ArrayList<TestBean>();
        queryList.add(new TestBean("foo", null));

        String queryInMessage = getListCall("query", queryList);
        String queryResult = this.endpoint.handlePersistenceCall(queryInMessage);

        ReturnValue queryReturnValue = Transformer.toReturnValue(queryResult);
        Assert.assertEquals(List.class, queryReturnValue.getType());
        List<TestBean> outList = (List<TestBean>) queryReturnValue.getValue();

        Assert.assertEquals(1, outList.size());
        Assert.assertEquals("foo", outList.get(0).getFoo());
        Assert.assertEquals(new Integer(21), outList.get(0).getBar());
    }

    private String getListCall(String methodName, List<?> param) throws SerializationException {
        MethodCall call = new MethodCall(methodName, new Object[] { param }, new Class<?>[] { List.class });
        return Transformer.toXml(call);
    }

    private String getUpdateCall(Object oldBean, Object newBean) throws SerializationException {
        MethodCall call = new MethodCall("update", new Object[] { oldBean, newBean }, new Class<?>[] { Object.class,
                Object.class });
        return Transformer.toXml(call);
    }

    public static class PersistenceMock implements PersistenceInternal {

        private List<PersistenceObject> stored = new ArrayList<PersistenceObject>();

        @Override
        public void create(List<PersistenceObject> elements) {
            for (PersistenceObject element : elements) {
                stored.add(element);
            }
        }

        @Override
        public void delete(List<PersistenceObject> examples) {
            List<PersistenceObject> hits = query(examples);
            for (PersistenceObject hit : hits) {
                stored.remove(hit);
            }
        }

        @Override
        public List<PersistenceObject> query(List<PersistenceObject> examples) {
            List<PersistenceObject> result = new ArrayList<PersistenceObject>();
            for (PersistenceObject element : stored) {
                for (PersistenceObject example : examples) {
                    if (similar(element, example)) {
                        result.add(element);
                        break;
                    }
                }
            }
            return result;
        }

        private boolean similar(PersistenceObject element, PersistenceObject example) {
            String keyWord = getKeyWord(example);
            return getKeyWord(element).equals(keyWord);
        }

        private String getKeyWord(PersistenceObject example) {
            String prefix = "<primitive><string>";
            String postfix = "</string></primitive>";
            int start = example.getXml().indexOf(prefix) + prefix.length();
            int end = example.getXml().indexOf(postfix);
            return example.getXml().substring(start, end);
        }

        @Override
        public void update(PersistenceObject oldElement, PersistenceObject newElement) {
            List<PersistenceObject> query = query(Collections.singletonList(oldElement));
            if (query.size() != 1) {
                throw new IllegalStateException("For update exactly one match for query object is necessary, but "
                        + query.size() + " matches were found.");
            }
            PersistenceObject toUpdate = query.get(0);
            stored.remove(toUpdate);
            stored.add(newElement);
        }

    }

    public static class TestBean {
        private String foo;

        private Integer bar;

        public TestBean(String foo, Integer bar) {
            this.foo = foo;
            this.bar = bar;
        }

        public TestBean() {
        }

        public Integer getBar() {
            return bar;
        }

        public String getFoo() {
            return foo;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TestBean)) {
                return false;
            }
            TestBean other = (TestBean) obj;
            return this.foo == null ? other.foo == null
                    && (this.bar == null ? other.bar == null : this.bar.equals(other.bar)) : this.foo.equals(other.foo);
        }

        @Override
        public int hashCode() {
            int fooCode = foo == null ? 0 : foo.hashCode();
            int barCode = bar == null ? 0 : bar.hashCode();
            return 13 + 17 * fooCode + 17 * barCode;
        }

    }
}
