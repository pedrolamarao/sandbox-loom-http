package br.dev.pedrolamarao.loom.http;

public class HttpParserException extends RuntimeException
{
    public HttpParserException ()
    {
        super();
    }

    public HttpParserException (String message)
    {
        super(message);
    }

    public HttpParserException (String message, Throwable cause)
    {
        super(message, cause);
    }

    public HttpParserException (Throwable cause)
    {
        super(cause);
    }
}