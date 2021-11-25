package br.dev.pedrolamarao.loom.http;

public record HttpStartResponse(String version, String status, String reason) implements HttpStart
{
}