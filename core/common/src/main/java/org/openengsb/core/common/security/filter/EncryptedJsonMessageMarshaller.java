package org.openengsb.core.common.security.filter;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.security.model.EncryptedBinaryMessage;
import org.openengsb.core.common.remote.AbstractFilterChainElement;

public class EncryptedJsonMessageMarshaller extends AbstractFilterChainElement<byte[], byte[]> {

    private FilterAction next;

    public EncryptedJsonMessageMarshaller() {
        super(byte[].class, byte[].class);
    }

    @Override
    protected byte[] doFilter(byte[] input, Map<String, Object> metaData) {
        ObjectMapper objectMapper = new ObjectMapper();
        EncryptedBinaryMessage message;
        try {
            message = objectMapper.readValue(input, EncryptedBinaryMessage.class);
        } catch (IOException e) {
            throw new FilterException(e);
        }
        return (byte[]) next.filter(message, metaData);
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, EncryptedBinaryMessage.class, byte[].class);
        this.next = next;
    }

}
