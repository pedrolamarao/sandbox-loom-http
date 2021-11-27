package br.dev.pedrolamarao.loom.http;

import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Callable;

@CommandLine.Command
public class HttpServerTool implements Callable<Integer>
{
    @CommandLine.Option(names = "--address")
    URI address = URI.create("http://localhost:8080");

    @Override
    public Integer call () throws Exception
    {
        final var socketAddress = new InetSocketAddress( address.getHost(), address.getPort() );
        try (var server = HttpServer.create( Thread.ofVirtual().factory() ))
        {
            server.bind(socketAddress);
            server.start();
            synchronized (this) { wait(); }
        }
        return 0;
    }

    public static void main (String... args)
    {
        int exitCode = new CommandLine(new HttpServerTool()).execute(args);
        System.exit(exitCode);
    }
}