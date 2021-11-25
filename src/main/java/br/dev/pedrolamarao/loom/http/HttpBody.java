package br.dev.pedrolamarao.loom.http;

import java.nio.ByteBuffer;

public record HttpBody(byte[] content) implements HttpPart
{
}