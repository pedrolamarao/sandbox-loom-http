package br.dev.pedrolamarao.loom.http;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpParser;
import org.eclipse.jetty.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpServer implements AutoCloseable
{
    final ArrayList<InetSocketAddress> addresses = new ArrayList<>();

    final ExecutorService executor;

    final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    final ConcurrentHashMap<InetSocketAddress, ServerSocketChannel> servers = new ConcurrentHashMap<>();

    HttpServer (ExecutorService executor)
    {
        this.executor = executor;
    }

    public static HttpServer create (ThreadFactory threadFactory)
    {
        return new HttpServer(Executors.newThreadPerTaskExecutor(threadFactory));
    }

    public void close ()
    {
        executor.shutdownNow();
        logger.atInfo().log("server: closed");
    }

    public void bind (InetSocketAddress address)
    {
        addresses.add(address);
    }

    public void start ()
    {
        addresses.forEach( address -> executor.submit( new HttpAcceptor(address) ) );
        logger.atInfo().log("server: started");
    }

    public void stop ()
    {
        servers.forEach( (address, server) -> {
            try { server.close(); }
            catch (IOException e) { logger.atDebug().log("stop: failure", e); }
        });
        servers.clear();
        logger.atInfo().log("server: closed");
    }

    final class HttpAcceptor implements Callable<Void>
    {
        final InetSocketAddress address;

        HttpAcceptor (InetSocketAddress address)
        {
            this.address = address;
        }

        @Override
        public Void call () throws Exception
        {
            final var server = ServerSocketChannel.open();
            server.bind(address);
            servers.put(address, server);
            logger.atInfo().log("listener: {}: listening", server.getLocalAddress());
            while (! Thread.currentThread().isInterrupted()) {
                final var client = server.accept();
                executor.submit( new HttpWorker(client) );
            }
            logger.atInfo().log("listener: {}: closed", server.getLocalAddress());
            return null;
        }
    }

    final class HttpWorker implements Callable<Void>, HttpParser.RequestHandler
    {
        final SocketChannel socket;

        HttpWorker (SocketChannel socket)
        {
            this.socket = socket;
        }

        @Override
        public Void call () throws Exception
        {
            final var buffer = ByteBuffer.allocate(4096);
            final var parser = new HttpParser(this);
            logger.atInfo().log("client: {}: servicing", socket.getLocalAddress());
            while (true)
            {
                buffer.clear();
                final var read = socket.read(buffer);
                if (read == -1) break;
                buffer.flip();
                while (buffer.hasRemaining()) {
                    if (parser.parseNext(buffer)) {
                        socket.write( UTF_8.encode( "HTTP/1.1 404\r\nContent-Length: 0\r\n\r\n") );
                        logger.atInfo().log("client: {}: request responded", socket.getLocalAddress());
                        parser.reset();
                    }
                }
            }
            logger.atInfo().log("client: {}: closed", socket.getLocalAddress());
            return null;
        }

        @Override public void startRequest (String method, String uri, HttpVersion version) { }

        @Override public boolean content (ByteBuffer item) { return false; }

        @Override public boolean headerComplete () { return false; }

        @Override public boolean contentComplete () { return false; }

        @Override public boolean messageComplete () { return true; }

        @Override public void parsedHeader (HttpField field) { }

        @Override public void earlyEOF () { }
    }
}