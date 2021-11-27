package br.dev.pedrolamarao.loom.http.internal;

import br.dev.pedrolamarao.loom.http.HttpCloseException;
import br.dev.pedrolamarao.loom.http.HttpParserSource;

public record EmptySource() implements HttpParserSource
{
    @Override
    public int peek () { throw new HttpCloseException(); }

    @Override
    public void skip () { }

    @Override
    public int take () { throw new HttpCloseException(); }
}