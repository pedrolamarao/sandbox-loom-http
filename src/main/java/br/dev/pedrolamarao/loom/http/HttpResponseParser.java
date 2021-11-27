package br.dev.pedrolamarao.loom.http;

public class HttpResponseParser extends HttpParser
{
    public HttpResponseParser (HttpParserSource source)
    {
        super(source);
    }

    @Override
    HttpStart parseStart (HttpParserSource source)
    {
        return parseStartResponse(source);
    }

    static HttpStartResponse parseStartResponse (HttpParserSource source)
    {
        final var version = parseVersion(source);
        final var status = parseStatus(source);
        final var reason = parseReason(source);
        return new HttpStartResponse(version, status, reason);
    }
}