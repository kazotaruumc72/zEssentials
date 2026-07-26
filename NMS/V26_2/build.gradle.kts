plugins {
    id("io.papermc.paperweight.userdev")
}

group "NMS:V26_2"

dependencies {
    compileOnly(project(":API"))
    compileOnly("net.kyori:adventure-api:4.26.1")
    paperweight.paperDevBundle("26.2.build.15-alpha")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.compileJava {
    options.release = 25
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}
