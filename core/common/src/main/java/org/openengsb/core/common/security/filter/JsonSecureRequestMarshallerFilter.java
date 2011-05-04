package org.openengsb.core.common.security.filter;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.remote.AbstractFilterChainElement;

public class JsonSecureRequestMarshallerFilter extends AbstractFilterChainElement<byte[], byte[]> {

    private FilterAction next;

    private ObjectMapper mapper = new ObjectMapper();

    public JsonSecureRequestMarshallerFilter() {
        super(byte[].class, byte[].class);
    }

    @Override
    protected byte[] doFilter(byte[] input, Map<String, Object> metaData) {
        try {
            SecureRequest request = mapper.readValue(input, SecureRequest.class);
            SecureResponse response = (SecureResponse) next.filter(request, metaData);
            return mapper.writeValueAsBytes(response);
        } catch (IOException e) {
            throw new FilterException(e);
        }
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, SecureRequest.class, SecureResponse.class);
        this.next = next;
    }
}
