package br.dev.pedrolamarao.loom.http;

import java.util.ArrayList;
import java.util.List;

public record HttpRequest (HttpStartRequest start, List<HttpField> header, HttpBody body, List<HttpField> trailer)
{
    public static HttpRequest from (HttpRequestParser parser)
    {
        var part = parser.parse();
        if (! (part instanceof HttpStartRequest start))
            throw new HttpParserException("expected 'start', got " + part);

        final var header = new ArrayList<HttpField>();
        part = parser.parse();
        while (part instanceof HttpField field) {
            header.add(field);
            part = parser.parse();
        }

        if (! (part instanceof HttpBody body))
            throw new HttpParserException("expected 'body', got " + part);

        final var trailer = new ArrayList<HttpField>();
        part = parser.parse();
        while (part instanceof HttpField field) {
            header.add(field);
            part = parser.parse();
        }

        if (! (part instanceof HttpFinish))
            throw new HttpParserException("expected 'finish', got " + part);

        return new HttpRequest(start, header, body, trailer);
    }
}