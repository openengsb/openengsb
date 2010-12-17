package org.openengsb.core.ports.jms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.common.communication.MethodCall;

public class RequestMapping extends MethodCall {

    private String callId;

    private boolean answer;

    private List<String> classes;

    @Override
    public final List<String> getClasses() {
        return classes;
    }

    public final void setClasses(List<String> classes) {
        this.classes = classes;
    }

    public RequestMapping() {
    }

    public RequestMapping(MethodCall call) {
        this.setArgs(call.getArgs());
        this.setMetaData(call.getMetaData());
        this.setMethodName(call.getMethodName());
    }

    public final String getCallId() {
        return callId;
    }

    public final void setCallId(String callId) {
        this.callId = callId;
    }

    public final boolean isAnswer() {
        return answer;
    }

    public final void setAnswer(boolean answer) {
        this.answer = answer;
    }

    /**
     * Converts the Args read by Jackson into the correct classes that have to be used for calling the method.
     */
    public void resetArgs() {
        if (this.getClasses().size() != this.getArgs().length) {
            throw new IllegalStateException("Classes and Args have to be the same");
        }
        ObjectMapper mapper = new ObjectMapper();
        Iterator<String> iterator = this.getClasses().iterator();

        List<Object> values = new ArrayList<Object>();

        for (Object arg : this.getArgs()) {
            Class<?> class1;
            try {
                class1 = Class.forName(iterator.next());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            values.add(mapper.convertValue(arg, class1));
        }
        this.setArgs(values.toArray());
    }

}
