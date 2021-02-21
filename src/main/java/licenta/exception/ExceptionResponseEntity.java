package licenta.exception;

import java.io.Serializable;

public class ExceptionResponseEntity implements Serializable {
    private final int errorCode;
    private final String errorMessage;

    public ExceptionResponseEntity(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
