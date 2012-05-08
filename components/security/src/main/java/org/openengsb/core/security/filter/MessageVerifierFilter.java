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

package org.openengsb.core.security.filter;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.security.MessageVerificationFailedException;
import org.openengsb.core.api.security.model.AuthenticationInfo;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.remote.AbstractFilterChainElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * This filter does no actual transformation. It takes a {@link SecureRequest} extracts the verification information and
 * verifies it. If the verification fails an Exception is thrown and the next filter is not invoked.
 * 
 * This filter is intended for incoming ports.
 * 
 * <code>
 * <pre>
 *      [SecureRequest]  > Filter > [SecureRequest]    > ...
 *                                                        |
 *                                                        v
 *      [SecureResponse] < Filter < [SecureResponse]   < ...
 * </pre>
 * </code>
 */
public class MessageVerifierFilter extends AbstractFilterChainElement<SecureRequest, SecureResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageVerifierFilter.class);

    private static final String DISABLE_VERIFICATION = "org.openengsb.security.noverify";

    private FilterAction next;

    private long timeout = 10 * 60 * 1000; // 10 minutes
    private LoadingCache<AuthenticationInfo, Long> lastMessageTimestamp = CacheBuilder.newBuilder()
        .expireAfterWrite(timeout, TimeUnit.MILLISECONDS)
        .build(new CacheLoader<AuthenticationInfo, Long>() {
            public Long load(AuthenticationInfo key) throws Exception {
                return 0L;
            };
        });

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
        if (Boolean.getBoolean(DISABLE_VERIFICATION)) {
            return;
        }
        LOGGER.trace("checking age of message");
        checkOverallAgeOfRequest(request);
        LOGGER.trace("checking for replay");
        checkForReplayedMessage(request);
    }

    private void checkForReplayedMessage(SecureRequest request) throws MessageVerificationFailedException {
        AuthenticationInfo authenticationInfo = request.retrieveAuthenticationInfo();
        synchronized (lastMessageTimestamp) {
            try {
                if (lastMessageTimestamp.get(authenticationInfo) >= request.getTimestamp()) {
                    throw new MessageVerificationFailedException(
                        "Message's timestamp was too old. Message with higher timestamp already receiverd."
                                + "Possible replay detected.");
                }
            } catch (ExecutionException e) {
                LOGGER.error("error when accessing cache", e);
            }
            lastMessageTimestamp.put(authenticationInfo, request.getTimestamp());
            LOGGER.debug("updated lastMessageTimestamp for {} to {}", authenticationInfo, request.getTimestamp());
        }
    }

    private void checkOverallAgeOfRequest(SecureRequest request) throws MessageVerificationFailedException {
        long ageInMillis = System.currentTimeMillis() - request.getTimestamp();
        LOGGER.debug("request-age in ms: {}", ageInMillis);
        if (ageInMillis < 0) {
            throw new MessageVerificationFailedException("Message timestamp was too far in the future");
        }
        if (ageInMillis > timeout) {
            throw new MessageVerificationFailedException("Message timestamp is too old.");
        }
    }
}
