package br.dev.pedrolamarao.loom.http;

public record HttpStartRequest(CharSequence version, CharSequence verb, CharSequence path) implements HttpStart
{
}