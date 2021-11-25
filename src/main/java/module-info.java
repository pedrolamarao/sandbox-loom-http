module br.dev.pedrolamarao.loom.http
{
    requires info.picocli;
    requires org.slf4j;

    opens br.dev.pedrolamarao.loom.http to info.picocli;
    opens br.dev.pedrolamarao.loom.http.internal to info.picocli;
}