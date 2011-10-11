package org.openengsb.itests.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a helper class for the console integration tests.
 * Its purpose is to gather the result of the executed command in an ArrayList.
 */
public class OutputStreamHelper extends OutputStream {

    private List<String> result = new ArrayList<String>();

    @Override
    public void write(int i) throws IOException {
        // ignore
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        super.write(bytes);
    }

    @Override
    public void write(byte[] bytes, int i, int i1) throws IOException {
        String substring = new String(bytes).substring(i, i1);
        // filter out some stuff like empty strings or new line stuff
        if (substring != null && !"".equals(substring) && !"\n".equals(substring)) {
            result.add(substring);
        }
        super.write(bytes, i, i1);
    }

    public List<String> getResult() {
        return result;
    }
}
