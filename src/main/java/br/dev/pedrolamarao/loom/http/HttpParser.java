package br.dev.pedrolamarao.loom.http;

import java.io.ByteArrayOutputStream;
import java.util.function.Supplier;
import java.util.generator.GeneratorHandler;
import java.util.generator.Generator;

import static java.lang.Character.isAlphabetic;
import static java.lang.Character.isDigit;
import static java.lang.Thread.currentThread;

public abstract class HttpParser
{
    private final HttpParserSource source;

    private final Supplier<HttpPart> generator = new Generator<>(this::run);

    protected HttpParser (HttpParserSource source)
    {
        this.source = source;
    }

    public HttpPart parsePart ()
    {
        return generator.get();
    }

    //

    int contentLength = 0;

    void run (GeneratorHandler<HttpPart> handler)
    {
        while (! currentThread().isInterrupted())
        {
            // 1. parse start

            handler.yield( parseStart(source) );

            // 2. parse header

            while (true)
            {
                final var field = parseField(source);
                if (field == null) break;
                if (field.name().equals("Content-Length")) contentLength = Integer.parseUnsignedInt(field.value());
                handler.yield(field);
            }

            // 3. parse body

            if (contentLength > 0)
            {
                handler.yield( parseDefiniteBody(source, contentLength) );
            }
            else if (contentLength == 0)
            {
                handler.yield( new HttpBody(new byte[0]) );
            }
            else if (contentLength < 0)
            {
                throw new HttpParserException("indefinite length body not supported");
            }

            // 4. TODO: parse trailer

            // 5. finish

            handler.yield( new HttpFinish() );
        }
    }

    abstract HttpStart parseStart (HttpParserSource source);

    static HttpField parseField (HttpParserSource source)
    {
        if (source.peek() == '\r') {
            source.skip();
            if (source.take() != '\n') throw new HttpParserException("expected LF after CR");
            return null;
        }
        else {
            final var name = parseFieldName(source);
            final var value = parseFieldValue(source);
            return new HttpField(name, value);
        }
    }

    static HttpBody parseDefiniteBody (HttpParserSource source, int length)
    {
        final var value = new ByteArrayOutputStream();
        while (value.size() < length) {
            value.write( source.take() );
        }
        return new HttpBody(value.toByteArray());
    }

    static String parseVersion (HttpParserSource source)
    {
        final var value = new StringBuilder();
        while (true) {
            final var next = source.peek();
            if (! (isAlphabetic(next) || isDigit(next) || next == '/' || next == '.')) break;
            value.append((char) next);
            source.skip();
        }
        while (source.peek() == ' ') {
            source.skip();
        }
        if (source.peek() == '\r') {
            source.skip();
            if (source.take() != '\n') throw new HttpParserException("expected LF after CR");
        }
        return value.toString();
    }

    static String parseFieldName (HttpParserSource source)
    {
        final var value = new StringBuilder();
        while (true) {
            final var next = source.take();
            if (next == ' ' || next == ':') break;
            value.append((char) next);
        }
        while (true) {
            final var next = source.peek();
            if (next != ' ' && next != ':') break;
            source.skip();
        }
        return value.toString();
    }

    static String parseFieldValue (HttpParserSource source)
    {
        final var value = new StringBuilder();
        while (true) {
            final var next = source.take();
            if (next != '\r') {
                value.append((char) next);
                continue;
            }
            if (source.take() != '\n') throw new HttpParserException("expected LF after CR");
            if (source.peek() != ' ') break;
        }
        return value.toString();
    }
}