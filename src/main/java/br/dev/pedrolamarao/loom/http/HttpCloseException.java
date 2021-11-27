package br.dev.pedrolamarao.loom.http;

public class HttpCloseException extends RuntimeException
{
    public HttpCloseException()
    {
        super();
    }

    public HttpCloseException(String message)
    {
        super(message);
    }

    public HttpCloseException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public HttpCloseException(Throwable cause)
    {
        super(cause);
    }
}