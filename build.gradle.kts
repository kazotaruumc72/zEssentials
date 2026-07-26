plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.4.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
    id("re.alwyn974.groupez.repository") version "1.0.0"
}

group = "fr.maxlego08.essentials"
version = "1.1.0.0"

extra.set("targetFolder", file("target/"))
extra.set("targetFolderDiscord", file("target-discord/"))
extra.set("apiFolder", file("target-api/"))
extra.set("classifier", System.getProperty("archive.classifier"))
extra.set("sha", System.getProperty("github.sha"))

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "re.alwyn974.groupez.repository")

    group = "fr.maxlego08.essentials"
    version = rootProject.version

    repositories {
        mavenLocal()
        mavenCentral()

        maven(url = "https://jitpack.io")
        maven(url = "https://repo.papermc.io/repository/maven-public/")
        maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven(url = "https://repo.tcoded.com/releases")
    }

    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "net.kyori" && requested.name == "adventure-text-serializer-ansi" && (requested.version.isNullOrBlank() || requested.version == ".")) {
                useVersion("4.20.0")
            }
        }
    }

    java {
        withSourcesJar()

        if (!project.path.startsWith(":NMS:")) {
            withJavadocJar()
        }
    }

    tasks.shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveAppendix.set(if (project.path == ":") "" else project.name)
        archiveClassifier.set("")
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
        options.release = 21
    }

    tasks.javadoc {
        options.encoding = "UTF-8"
        if (JavaVersion.current().isJava9Compatible)
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }

    dependencies {
//        compileOnly("fr.maxlego08.menu:zmenu-api:1.1.0.0")
        compileOnly(files("libs/zMenu-1.1.1.5.jar"))

        compileOnly("fr.maxlego08.sarah:sarah:1.23")
        compileOnly("com.tcoded:FoliaLib:0.5.1")
        compileOnly("fr.mrmicky:fastboard:2.1.5")
    }
}

tasks.compileJava {
    options.release = 25
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.66-stable")
    compileOnly("net.luckperms:api:5.4")

    api(project(":API"))

    api(project(":NMS:V26_1_2", configuration = "shadow"))
    api(project(":NMS:V26_2", configuration = "shadow"))

    rootProject.subprojects.filter { it.path.startsWith(":Hooks:") }.forEach { subproject ->
        api(project(subproject.path))
    }
}

tasks {
    shadowJar {
        relocate("com.tcoded.folialib", "fr.maxlego08.essentials.libs.folialib")
        relocate("fr.maxlego08.sarah", "fr.maxlego08.essentials.libs.sarah")
        relocate("fr.mrmicky.fastboard", "fr.maxlego08.essentials.libs.fastboard")

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }

        rootProject.extra.properties["sha"]?.let { sha ->
            archiveClassifier.set("${rootProject.extra.properties["classifier"]}-${sha}")
        } ?: run {
            archiveClassifier.set(rootProject.extra.properties["classifier"] as String?)
        }
        destinationDirectory.set(rootProject.extra["targetFolder"] as File)
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        inputs.property("version", project.version)
        from("resources")
        filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
            expand("version" to project.version)
        }
    }
}
