package br.dev.pedrolamarao.loom.http;

public interface HttpParserSource
{
    int peek();

    void skip();

    int take();
}
