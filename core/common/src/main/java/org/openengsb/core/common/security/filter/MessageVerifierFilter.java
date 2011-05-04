package org.openengsb.core.common.security.filter;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ArrayUtils;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.security.MessageVerificationFailedException;
import org.openengsb.core.api.security.model.AuthenticationInfo;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.remote.AbstractFilterChainElement;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

public class MessageVerifierFilter extends AbstractFilterChainElement<SecureRequest, SecureResponse> {

    private FilterAction next;

    private long timeout = 10 * 60 * 1000; // 10 minutes
    private ConcurrentMap<AuthenticationInfo, Long> lastMessageTimestamp = new MapMaker()
        .expireAfterWrite(timeout, TimeUnit.MILLISECONDS)
        .makeComputingMap(new Function<AuthenticationInfo, Long>() {
            @Override
            public Long apply(AuthenticationInfo input) {
                return 0L;
            };
        });

    public MessageVerifierFilter() {
        super(SecureRequest.class, SecureResponse.class);
    }

    @Override
    protected SecureResponse doFilter(SecureRequest input, Map<String, Object> metaData) {
        try {
            verify(input);
        } catch (MessageVerificationFailedException e) {
            throw new FilterException(e);
        }
        return (SecureResponse) next.filter(input, metaData);
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, SecureRequest.class, SecureResponse.class);
        this.next = next;
    }

    private void verify(SecureRequest request) throws MessageVerificationFailedException {
        checkOverallAgeOfRequest(request);
        verifyCheckSum(request);
        checkForReplayedMessage(request);
    }

    private void checkForReplayedMessage(SecureRequest request) throws MessageVerificationFailedException {
        AuthenticationInfo authenticationInfo = request.retrieveAuthenticationInfo();
        synchronized (lastMessageTimestamp) {
            if (lastMessageTimestamp.get(authenticationInfo) >= request.getTimestamp()) {
                throw new MessageVerificationFailedException(
                    "Message's timestamp was too old. Message with higher timestamp already receiverd."
                            + "Possible replay detected.");
            }
            lastMessageTimestamp.put(authenticationInfo, request.getTimestamp());
        }
    }

    private void verifyCheckSum(SecureRequest request) throws MessageVerificationFailedException {
        if (!ArrayUtils.isEquals(request.calcChecksum(), request.getVerification())) {
            throw new MessageVerificationFailedException(
                "checksum verification failed. The message might have been altered.");
        }
    }

    private void checkOverallAgeOfRequest(SecureRequest request) throws MessageVerificationFailedException {
        long current = System.currentTimeMillis();
        if (request.getTimestamp() + timeout < current) {
            throw new MessageVerificationFailedException("Message timestamp is too old.");
        }
    }
}
