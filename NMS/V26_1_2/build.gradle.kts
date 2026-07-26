plugins {
    id("io.papermc.paperweight.userdev")
}

group "NMS:V26_1_2"

dependencies {
    compileOnly(project(":API"))
    compileOnly("net.kyori:adventure-api:4.26.1")
    paperweight.paperDevBundle("26.1.2.build.66-stable")
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