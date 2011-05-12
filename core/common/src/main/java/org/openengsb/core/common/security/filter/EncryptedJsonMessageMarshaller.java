package org.openengsb.core.common.security.filter;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.common.remote.AbstractFilterChainElement;

public class EncryptedJsonMessageMarshaller extends AbstractFilterChainElement<String, String> {

    private FilterAction next;

    public EncryptedJsonMessageMarshaller() {
        super(String.class, String.class);
    }

    @Override
    protected String doFilter(String input, Map<String, Object> metaData) {
        ObjectMapper objectMapper = new ObjectMapper();
        EncryptedMessage message;
        try {
            message = objectMapper.readValue(input, EncryptedMessage.class);
        } catch (IOException e) {
            throw new FilterException(e);
        }
        byte[] result = (byte[]) next.filter(message, metaData);
        return new String(result);
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, EncryptedMessage.class, byte[].class);
        this.next = next;
    }

}
