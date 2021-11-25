package br.dev.pedrolamarao.loom.http;

public record HttpField(String name, String value) implements HttpPart
{
}