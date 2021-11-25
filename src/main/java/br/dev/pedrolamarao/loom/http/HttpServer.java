package br.dev.pedrolamarao.loom.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
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

    final class HttpWorker implements Callable<Void>
    {
        final SocketChannel socket;

        HttpWorker (SocketChannel socket)
        {
            this.socket = socket;
        }

        @Override
        public Void call () throws Exception
        {
            logger.atInfo().log("client: {}: servicing", socket.getLocalAddress());

            final var source = HttpParserSources.fromChannel(socket);

            final var parser = new HttpParser();

            while (! Thread.currentThread().isInterrupted())
            {
                while (! Thread.currentThread().isInterrupted())
                {
                    final var part = parser.parse(source);
                    if (part instanceof HttpFinish) break;
                }

                socket.write( UTF_8.encode( "HTTP/1.1 404\r\nContent-Length: 0\r\n\r\n") );
                logger.atInfo().log("client: {}: request responded", socket.getLocalAddress());
            }

            logger.atInfo().log("client: {}: closed", socket.getLocalAddress());
            return null;
        }
    }
}