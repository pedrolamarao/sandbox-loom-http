package br.dev.pedrolamarao.loom.http;

public class HttpRequestParser extends HttpParser
{
    public HttpRequestParser (HttpParserSource source)
    {
        super(source);
    }

    @Override
    HttpStart parseStart (HttpParserSource source)
    {
        return parseStartRequest(source);
    }

    static HttpStartRequest parseStartRequest (HttpParserSource source)
    {
        final var verb = parseVerb(source);
        final var path = parsePath(source);
        final var version = parseVersion(source);
        return new HttpStartRequest(version, verb, path);
    }
}