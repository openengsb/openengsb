package org.openengsb.domains.jms;

public final class MessageMapping {
    private MessageType type;
    private String name;
    private String message;

    public MessageMapping() {
    }

    public MessageMapping(MessageType type, String name, String message) {
        super();
        this.type = type;
        this.name = name;
        this.message = message;
    }

    public MessageType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
