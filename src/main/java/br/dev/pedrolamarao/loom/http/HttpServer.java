package br.dev.pedrolamarao.loom.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
            try (var server = ServerSocketChannel.open())
            {
                servers.put(address, server);

                server.bind(address);

                logger.atInfo().log("listener: {}: listening", address);

                while (! Thread.currentThread().isInterrupted()) {
                    final var client = server.accept();
                    executor.submit( new HttpWorker(client) );
                }
            }
            finally
            {
                servers.remove(address);
            }

            logger.atInfo().log("listener: {}: closed", address);

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
        public Void call ()
        {
            SocketAddress address = null;

            try (socket)
            {
                address = socket.getRemoteAddress();

                logger.atInfo().log("client: {}: servicing", address);

                final var parser = new HttpRequestParser( HttpParserSources.fromChannel(socket) );

                while (! Thread.currentThread().isInterrupted())
                {
                    parser.parseRequest();
                    socket.write( UTF_8.encode( "HTTP/1.1 200\r\nContent-Length: 0\r\n\r\n") );
                    logger.atInfo().log("client: {}: request responded", address);
                }

                logger.atInfo().log("client: {}: interrupted", address);
            }
            catch (HttpCloseException e)
            {
                logger.atInfo().log("client: {}: closed", address);
            }
            catch (Exception e)
            {
                logger.atError().log("client: {}: failed", address, e);
            }

            return null;
        }
    }
}