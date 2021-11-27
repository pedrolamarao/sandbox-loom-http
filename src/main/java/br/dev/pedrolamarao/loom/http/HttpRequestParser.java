package br.dev.pedrolamarao.loom.http;

import java.util.ArrayList;

import static java.lang.Character.isAlphabetic;

public class HttpRequestParser extends HttpParser
{
    public HttpRequestParser (HttpParserSource source)
    {
        super(source);
    }

    static String parsePath (HttpParserSource source)
    {
        final var value = new StringBuilder();
        while (true) {
            final var next = source.take();
            if (next == ' ') break;
            value.append((char) next);
        }
        while (source.peek() == ' ') source.skip();
        return value.toString();
    }

    public HttpRequest parseRequest ()
    {
        var part = parsePart();
        if (! (part instanceof HttpStartRequest start))
            throw new HttpParserException("expected 'start', got " + part);

        final var header = new ArrayList<HttpField>();
        part = parsePart();
        while (part instanceof HttpField field) {
            header.add(field);
            part = parsePart();
        }

        if (! (part instanceof HttpBody body))
            throw new HttpParserException("expected 'body', got " + part);

        final var trailer = new ArrayList<HttpField>();
        part = parsePart();
        while (part instanceof HttpField field) {
            header.add(field);
            part = parsePart();
        }

        if (! (part instanceof HttpFinish))
            throw new HttpParserException("expected 'finish', got " + part);

        return new HttpRequest(start, header, body, trailer);
    }

    @Override
    HttpStart parseStart (HttpParserSource source)
    {
        return parseStartRequest(source);
    }

    static String parseVerb (HttpParserSource source)
    {
        final var value = new StringBuilder();
        while (true) {
            final var next = source.take();
            if (! isAlphabetic(next)) break;
            value.append((char) next);
        }
        while (source.peek() == ' ') source.skip();
        return value.toString();
    }

    static HttpStartRequest parseStartRequest (HttpParserSource source)
    {
        final var verb = parseVerb(source);
        final var path = parsePath(source);
        final var version = parseVersion(source);
        return new HttpStartRequest(version, verb, path);
    }
}