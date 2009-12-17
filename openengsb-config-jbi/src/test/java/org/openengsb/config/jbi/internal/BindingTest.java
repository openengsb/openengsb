package org.openengsb.config.jbi.internal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openengsb.config.jbi.types.ChoiceType;
import org.openengsb.config.jbi.types.IntType;
import org.openengsb.config.jbi.types.StringType;

import com.thoughtworks.xstream.XStream;

public class BindingTest {
    private final XStream x = XStreamFactory.createXStream();

    @Test
    public void parseStringType() throws Exception {
        String xml = "<string name=\"name\" optional=\"true\" maxLength=\"1\" defaultValue=\"a\" />";
        StringType o = (StringType)x.fromXML(xml);
        assertThat(o.getName(), is("name"));
        assertThat(o.isOptional(), is(true));
        assertThat(o.getMaxLength(), is(1));
        assertThat(o.getDefaultValue(), is("a"));
    }

    @Test
    public void parseChoiceType() throws Exception {
        String xml = "<choice values=\"a,b,c\" />";
        ChoiceType o = (ChoiceType)x.fromXML(xml);
        assertThat(o.getValues().length, is(3));
        assertThat(o.getValues()[0], is("a"));
        assertThat(o.getValues()[1], is("b"));
        assertThat(o.getValues()[2], is("c"));
    }

    @Test
    public void parseIntType() throws Exception {
        String xml = "<int min=\"-1\" max=\"2\" />";
        IntType o = (IntType)x.fromXML(xml);
        assertThat(o.getMin(), is(-1));
        assertThat(o.getMax(), is(2));
    }
}
