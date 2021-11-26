package br.dev.pedrolamarao.loom.http;

import br.dev.pedrolamarao.loom.http.internal.EmptySource;
import org.junit.jupiter.api.Test;

import static br.dev.pedrolamarao.loom.http.HttpParserSources.fromString;
import static org.junit.jupiter.api.Assertions.*;

class HttpParserTest
{
    @Test
    void parseVersion ()
    {
        final var simple = HttpParser.parseVersion( fromString("HTTP/1.1 GET") );
        assertEquals("HTTP/1.1", simple);

        assertThrows(Exception.class, () ->
            HttpParser.parseVersion(new EmptySource())
        );
    }

    @Test
    void parseVerb ()
    {
        final var simple = HttpParser.parseVerb( fromString("GET /") );
        assertEquals("GET", simple);

        assertThrows(Exception.class, () ->
            HttpParser.parseVerb(new EmptySource())
        );
    }

    @Test
    void parsePath ()
    {
        final var simple = HttpParser.parsePath( fromString("/index.html\r\n") );
        assertEquals("/index.html", simple);

        assertThrows(Exception.class, () ->
            HttpParser.parsePath(new EmptySource())
        );
    }

    @Test
    void parseStartRequest ()
    {
        final var start = (HttpStartRequest) HttpParser.parseStart( fromString("HTTP/1.1 GET /index.html\r\n") );
        assertEquals("HTTP/1.1", start.version());
        assertEquals("GET", start.verb());
        assertEquals("/index.html", start.path());
    }

    @Test
    void parseStatus ()
    {
        final var simple = HttpParser.parseStatus( fromString("404\r\n") );
        assertEquals("404", simple);

        final var reason = HttpParser.parseStatus( fromString("404 NOT_FOUND") );
        assertEquals("404", reason);

        assertThrows(Exception.class, () ->
            HttpParser.parseStatus(new EmptySource())
        );
    }

    @Test
    void parseReason ()
    {
        final var simple = HttpParser.parseReason( fromString("NOT_FOUND\r\n") );
        assertEquals("NOT_FOUND", simple);

        final var empty = HttpParser.parseReason( fromString("\r\n") );
        assertEquals("", empty);

        assertThrows(Exception.class, () ->
            HttpParser.parseReason(new EmptySource())
        );
    }

    @Test
    void parseStartResponse ()
    {
        final var simple = (HttpStartResponse) HttpParser.parseStart( fromString("HTTP/1.1 404\r\n") );
        assertEquals("HTTP/1.1", simple.version());
        assertEquals("404", simple.status());
        assertEquals("", simple.reason());

        final var reason = (HttpStartResponse) HttpParser.parseStart( fromString("HTTP/1.1 404 NOT_FOUND\r\n") );
        assertEquals("HTTP/1.1", reason.version());
        assertEquals("404", reason.status());
        assertEquals("NOT_FOUND", reason.reason());
    }

    @Test
    void parseFieldName ()
    {
        final var simple = HttpParser.parseFieldName( fromString("Content-Type: text/plain") );
        assertEquals("Content-Type", simple);

        final var spaces = HttpParser.parseFieldName( fromString("Content-Type  :  text/plain") );
        assertEquals("Content-Type", spaces);
    }

    @Test
    void parseFieldValue ()
    {
        final var simple = HttpParser.parseFieldValue( fromString("text/plain\r\nContent-Length") );
        assertEquals("text/plain", simple);

        final var multi = HttpParser.parseFieldValue( fromString("text/plain;\r\n    charset=UTF-8\r\n\r\n") );
        assertEquals("text/plain;    charset=UTF-8", multi);
    }

    @Test
    void parseField ()
    {
        final var simple = HttpParser.parseField( fromString("Content-Type: text/plain\r\nContent-Length") );
        assertEquals("Content-Type", simple.name());
        assertEquals("text/plain", simple.value());

        final var multi = HttpParser.parseField( fromString("Content-Type: text/plain;\r\n    charset=UTF-8\r\n\r\n") );
        assertEquals("Content-Type", multi.name());
        assertEquals("text/plain;    charset=UTF-8", multi.value());
    }

    @Test
    void parseSmallestRequest ()
    {
        final var source = fromString("HTTP/1.1 GET /\r\n\r\n");
        final var parser = new HttpParser();
        final var start = (HttpStartRequest) parser.parse(source);
        assertEquals("HTTP/1.1", start.version());
        assertEquals("GET", start.verb());
        assertEquals("/", start.path());
        final var body = (HttpBody) parser.parse(source);
        final var finish = (HttpFinish) parser.parse(source);
    }

    @Test
    void parseRequestSimple ()
    {
        final var source = fromString("HTTP/1.1 GET /\r\nHost: localhost\r\n\r\n");
        final var parser = new HttpParser();
        final var start = (HttpStartRequest) parser.parse(source);
        assertEquals("HTTP/1.1", start.version());
        assertEquals("GET", start.verb());
        assertEquals("/", start.path());
        final var host = (HttpField) parser.parse(source);
        assertEquals("Host", host.name());
        assertEquals("localhost", host.value());
        final var body = (HttpBody) parser.parse(source);
        final var finish = (HttpFinish) parser.parse(source);
    }

    @Test
    void parseRequestContentLengthBody ()
    {
        final var source = fromString("HTTP/1.1 GET /\r\nContent-Length: 4\r\n\r\nTODO");
        final var parser = new HttpParser();
        final var start = (HttpStartRequest) parser.parse(source);
        assertEquals("HTTP/1.1", start.version());
        assertEquals("GET", start.verb());
        assertEquals("/", start.path());
        final var field = (HttpField) parser.parse(source);
        assertEquals("Content-Length", field.name());
        assertEquals("4", field.value());
        final var body = (HttpBody) parser.parse(source);
        final var finish = (HttpFinish) parser.parse(source);
    }

    @Test
    void parseSmallestResponse ()
    {
        final var source = fromString("HTTP/1.1 200\r\n\r\n");
        final var parser = new HttpParser();
        final var start = (HttpStartResponse) parser.parse(source);
        assertEquals("HTTP/1.1", start.version());
        assertEquals("200", start.status());
        assertEquals("", start.reason());
        final var body = (HttpBody) parser.parse(source);
        final var finish = (HttpFinish) parser.parse(source);
    }
}