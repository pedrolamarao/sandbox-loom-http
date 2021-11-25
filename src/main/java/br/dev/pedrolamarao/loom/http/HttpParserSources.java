package br.dev.pedrolamarao.loom.http;

import br.dev.pedrolamarao.loom.http.internal.ByteBufferSource;
import br.dev.pedrolamarao.loom.http.internal.ReadableByteChannelSource;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpParserSources
{
    private HttpParserSources() { }

    public static HttpParserSource fromChannel (ReadableByteChannel channel) { return new ReadableByteChannelSource(channel); }

    public static HttpParserSource fromBuffer (ByteBuffer buffer)
    {
        return new ByteBufferSource(buffer);
    }

    public static HttpParserSource fromString (String string) { return new ByteBufferSource( ByteBuffer.wrap( string.getBytes(UTF_8) ) ); }
}