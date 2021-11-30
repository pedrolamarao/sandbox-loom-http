package br.dev.pedrolamarao.loom.http;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

class HttpServerTest
{
    @Test
    void smoke () throws Exception
    {
        try (var server = new HttpServer(Executors.newVirtualThreadPerTaskExecutor()))
        {
            server.bind(new InetSocketAddress(0));
            server.start();
            Thread.sleep(100);
            server.stop();
        }
    }
}