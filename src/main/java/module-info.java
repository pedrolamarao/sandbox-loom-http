module br.dev.pedrolamarao.loom.http
{
    requires info.picocli;
    requires org.eclipse.jetty.http;
    requires org.slf4j;

    opens br.dev.pedrolamarao.loom.http to info.picocli;
}