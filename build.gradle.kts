plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "br.dev.pedrolamarao.loom"
version = "1.0-SNAPSHOT"

dependencies {
    // LogBack
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha10")
    // Picocli
    implementation("info.picocli:picocli:4.6.2")
    // SLF4J
    implementation("org.slf4j:slf4j-api:2.0.0-alpha5")
    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "br.dev.pedrolamarao.loom.http.HttpServerTool"
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test> {
    jvmArgs("--enable-preview")
    useJUnitPlatform()
}