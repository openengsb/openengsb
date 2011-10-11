package org.openengsb.core.common.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.PrintStream;

import org.junit.Test;


public class OutputStreamFormaterTest {

    @Test
    public void testSpaces() {
        assertThat(OutputStreamFormater.spaces(20).length(), is(20));
    }

    @Test
    public void testFormatValues() {
        String string = "  \u001B[1mOpenEngSB Version        \u001B[m   3.0.0-SNAPSHOT";
        assertThat(OutputStreamFormater.formatValues("OpenEngSB Version", "3.0.0-SNAPSHOT"), equalTo(string));
    }

    @Test
    public void testPrintValues() {
        PrintStream streamMock = mock(PrintStream.class);
        OutputStreamFormater.setOutputStream(streamMock);
        OutputStreamFormater.printValue("OpenEngSB");
        verify(streamMock, times(1)).println("OpenEngSB");
    }

    @Test
    public void testPrintValues2() {
        String string = "  \u001B[1mOpenEngSB Version        \u001B[m   3.0.0-SNAPSHOT";
        PrintStream streamMock = mock(PrintStream.class);
        OutputStreamFormater.setOutputStream(streamMock);
        OutputStreamFormater.printValue("OpenEngSB Version", "3.0.0-SNAPSHOT");
        verify(streamMock, times(1)).println(string);
    }
}
