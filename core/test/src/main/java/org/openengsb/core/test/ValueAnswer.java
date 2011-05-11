package org.openengsb.core.test;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * May be used when {@link org.mockito.stubbing.OngoingStubbing#thenReturn(Object)} does not work because of generic
 * type incompatibilities
 */
public class ValueAnswer<T> implements Answer<T> {
    private T answer;

    public ValueAnswer(T answer) {
        this.answer = answer;
    }

    @Override
    public T answer(InvocationOnMock invocation) throws Throwable {
        return answer;
    }
}
