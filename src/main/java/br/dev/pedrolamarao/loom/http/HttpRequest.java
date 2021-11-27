package br.dev.pedrolamarao.loom.http;

import java.util.List;

public record HttpRequest (HttpStartRequest start, List<HttpField> header, HttpBody body, List<HttpField> trailer)
{
}