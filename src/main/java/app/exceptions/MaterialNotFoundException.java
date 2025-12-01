package app.exceptions;

public class MaterialNotFoundException extends Exception
{
    public MaterialNotFoundException(String message)
    {
        super(message);
    }

    public MaterialNotFoundException(String message, Exception exception)
    {
        super(message, exception);
    }
}
