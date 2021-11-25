package br.dev.pedrolamarao.loom.http;

public sealed interface HttpPart permits HttpBody, HttpField, HttpFinish, HttpStart
{
}