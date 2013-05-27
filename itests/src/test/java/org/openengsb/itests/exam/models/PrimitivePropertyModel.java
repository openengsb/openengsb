package org.openengsb.itests.exam.models;

import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;

@Model
public class PrimitivePropertyModel {

    @OpenEngSBModelId
    private String id;

    private boolean booleanByGet;
    private boolean booleanByIs;
    private short primitiveShort;
    private int primitiveInt;
    private long primitiveLong;
    private double primitiveDouble;
    private float primitiveFloat;
    private char primitiveChar;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getBooleanByGet() {
        return booleanByGet;
    }

    public void setBooleanByGet(boolean booleanByGet) {
        this.booleanByGet = booleanByGet;
    }

    public boolean isBooleanByIs() {
        return booleanByIs;
    }

    public void setBooleanByIs(boolean booleanByIs) {
        this.booleanByIs = booleanByIs;
    }

    public short getPrimitiveShort() {
        return primitiveShort;
    }

    public void setPrimitiveShort(short primitiveShort) {
        this.primitiveShort = primitiveShort;
    }

    public int getPrimitiveInt() {
        return primitiveInt;
    }

    public void setPrimitiveInt(int primitiveInt) {
        this.primitiveInt = primitiveInt;
    }

    public long getPrimitiveLong() {
        return primitiveLong;
    }

    public void setPrimitiveLong(long primitiveLong) {
        this.primitiveLong = primitiveLong;
    }

    public double getPrimitiveDouble() {
        return primitiveDouble;
    }

    public void setPrimitiveDouble(double primitiveDouble) {
        this.primitiveDouble = primitiveDouble;
    }

    public float getPrimitiveFloat() {
        return primitiveFloat;
    }

    public void setPrimitiveFloat(float primitiveFloat) {
        this.primitiveFloat = primitiveFloat;
    }

    public char getPrimitiveChar() {
        return primitiveChar;
    }

    public void setPrimitiveChar(char primitiveChar) {
        this.primitiveChar = primitiveChar;
    }
}
