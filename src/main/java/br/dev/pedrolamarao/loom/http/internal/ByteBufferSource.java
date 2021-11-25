package br.dev.pedrolamarao.loom.http.internal;

import br.dev.pedrolamarao.loom.http.HttpParserSource;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public record ByteBufferSource(ByteBuffer bytes) implements HttpParserSource
{
    public static ByteBufferSource from (String string, Charset charset)
    {
        return new ByteBufferSource( ByteBuffer.wrap( string.getBytes(charset) ) );
    }

    @Override
    public int peek()
    {
        return bytes.get(bytes.position());
    }

    @Override
    public void skip ()
    {
        bytes.get();
    }

    @Override
    public int take ()
    {
        return bytes.get();
    }
}