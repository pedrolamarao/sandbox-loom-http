package br.dev.pedrolamarao.loom.http.internal;

import br.dev.pedrolamarao.loom.http.HttpParserSource;

public record EmptySource() implements HttpParserSource
{
    @Override
    public int peek () { throw new RuntimeException("end-of-source"); }

    @Override
    public void skip () { }

    @Override
    public int take () { throw new RuntimeException("end-of-source"); }
}