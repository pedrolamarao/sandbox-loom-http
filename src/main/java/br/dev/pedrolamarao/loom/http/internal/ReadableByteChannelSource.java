package br.dev.pedrolamarao.loom.http.internal;

import br.dev.pedrolamarao.loom.http.HttpParserSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class ReadableByteChannelSource implements HttpParserSource
{
    final ByteBuffer buffer = ByteBuffer.allocate(4096);

    final ReadableByteChannel channel;

    public ReadableByteChannelSource (ReadableByteChannel channel)
    {
        this.channel = channel;
    }

    @Override
    public int peek ()
    {
        if (! buffer.hasRemaining()) fill();
        return buffer.get( buffer.position() );
    }

    @Override
    public void skip ()
    {
        if (! buffer.hasRemaining()) fill();
        buffer.get();
    }

    @Override
    public int take ()
    {
        if (! buffer.hasRemaining()) fill();
        return buffer.get();
    }

    void fill ()
    {
        try
        {
            buffer.clear();
            final var read = channel.read(buffer);
            if (read == -1) throw new RuntimeException("end-of-stream");
            buffer.flip();
        }
        catch (IOException e)
        {
            throw new RuntimeException("failure", e);
        }
    }
}