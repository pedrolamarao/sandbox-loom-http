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

    static String parseReason (HttpParserSource source)
    {
        final var value = new StringBuilder();
        while (true) {
            final var next = source.take();
            if (next == '\r') {
                if (source.take() != '\n') throw new HttpParserException("expected LF after CR");
                break;
            }
            else {
                value.append((char) next);
            }
        }
        return value.toString();
    }

    static HttpStartResponse parseStartResponse (HttpParserSource source)
    {
        final var version = parseVersion(source);
        final var status = parseStatus(source);
        final var reason = parseReason(source);
        return new HttpStartResponse(version, status, reason);
    }

    static String parseStatus (HttpParserSource source)
    {
        final var value = new StringBuilder();
        while (true) {
            final var byte_ = source.peek();
            if (byte_ == '\r') {
                break;
            }
            else if (byte_ == ' ') {
                break;
            }
            else {
                source.skip();
                value.append((char) byte_);
            }
        }
        while (source.peek() == ' ') source.skip();
        return value.toString();
    }
}