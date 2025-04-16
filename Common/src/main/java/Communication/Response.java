package Communication;

import java.io.Serial;
import java.io.Serializable;

public class Response implements Serializable{
@Serial
private static final long serialVersionUID = 1L;

    private Object result;
    private Exception exception;

    public Response() {
    }

    public Response(Object result, Exception exception) {
        this.result = result;
        this.exception = exception;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Exception getEx() {
        return exception;
    }

    public void setEx(Exception ex) {
        this.exception = ex;
    }


}