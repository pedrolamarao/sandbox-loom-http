plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "br.dev.pedrolamarao"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha10")
    implementation("info.picocli:picocli:4.6.2")
    implementation("org.eclipse.jetty:jetty-http:11.0.7")
    implementation("org.slf4j:slf4j-api:2.0.0-alpha5")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "br.dev.pedrolamarao.loom.http.HttpServerTool"
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}