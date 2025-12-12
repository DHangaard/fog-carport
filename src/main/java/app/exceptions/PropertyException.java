package app.exceptions;

public class PropertyException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public PropertyException(String message)
    {
        super(message);
    }

    public PropertyException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
