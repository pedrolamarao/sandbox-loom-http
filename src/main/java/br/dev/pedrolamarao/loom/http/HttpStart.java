package br.dev.pedrolamarao.loom.http;

public sealed interface HttpStart extends HttpPart permits HttpStartRequest, HttpStartResponse
{
}
