package Communication;

import java.io.Serial;
import java.io.Serializable;

public class Request implements Serializable{
@Serial
private static final long serialVersionUID = 1L;

    private Operations operations;
    private Object argument;

    public Request() {
    }

    public Request(Operations operation, Object argument) {
        this.operations = operation;
        this.argument = argument;
    }

    public Operations getOperation() {
        return operations;
    }

    public void setOperation(Operations operation) {
        this.operations = operation;
    }

    public Object getArgument() {
        return argument;
    }

    public void setArgument(Object argument) {
        this.argument = argument;
    }


}

