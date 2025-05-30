plugins {
    id("java")
    id("application")
    id("com.palantir.docker") version "0.36.0"
}

group = "org.example"
version = "1.1-SNAPSHOT"
val fatJarFileName = "RandomMovieApp-${version}-all.jar"
val dockerImageName = "random-movie-app:latest"
val dockerContainerName = "random-movie-app-container"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass = "org.example.MovieServer"
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    description = "Creates a fat JAR including all dependencies"
    archiveVersion.set(version.toString())
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}


tasks.named<Copy>("dockerPrepare") {
    dependsOn("fatJar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Configure the Docker image build
docker {
    name = "random-movie-app:latest"
    files("build/libs/$fatJarFileName", "Dockerfile")
    buildArgs(
        mapOf(
            "JAR_FILE" to fatJarFileName
        )
    )
}


tasks.register<Exec>("removeDockerContainerIfExists") {
    group = "docker"
    description = "Removes the Docker container if it exists to avoid conflicts"
    commandLine("docker", "rm", "-f", dockerContainerName)
    isIgnoreExitValue = true  // don't fail if container doesn't exist
}

tasks.register("runAppContainer") {
    group = "docker"
    description = "Runs the Docker container for the RandomMovieApp"
    dependsOn("docker", "removeDockerContainerIfExists")  // build image before running

    doLast {
        exec {
            commandLine(
                "docker", "run", "-d",
                "--name", dockerContainerName,
                "-p", "8080:8080",
                dockerImageName
            )
        }
    }

    doLast {
        println("RandomMovieApp is running at http://localhost:8080/")
    }
}

tasks.register("stopAppContainer") {
    group = "docker"
    description = "Stops and removes the Docker container if running"

    doLast {
        exec {
            commandLine("docker", "rm", "-f", dockerContainerName)
            isIgnoreExitValue = true  // ignore if container not found
        }
    }
}

tasks.register("removeDockerImageIfExists") {
    group = "docker"
    description = "Removes the Docker image for the app"

    doLast {
        exec {
            commandLine("docker", "rmi", "-f", dockerImageName)
            isIgnoreExitValue = true
        }
        exec {
            commandLine("docker", "image", "prune", "-f")
            isIgnoreExitValue = true
        }
    }
}

tasks.named("removeDockerImageIfExists") {
    mustRunAfter("stopAppContainer")
}

tasks.register("cleanDocker") {
    group = "docker"
    description = "Removes Docker container and image for the app"

    dependsOn("stopAppContainer", "removeDockerImageIfExists")
}