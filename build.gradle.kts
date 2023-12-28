import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    `maven-publish`
}

group = "org.mechdancer"
version = "0.4.0"

repositories {
    google()
    mavenCentral()
    jcenter() {
        content {
            includeModule("org.mechdancer", "linearalgebra")
            includeModule("org.mechdancer", "remote")
            includeModule("org.slf4j", "slf4j-api")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    // Matrix Operations
    implementation("org.mechdancer", "linearalgebra", "0.2.8-snapshot-3")

    testImplementation("junit", "junit", "+")
    testImplementation(kotlin("test-junit"))

    // Support network tools
    testImplementation(kotlin("reflect"))
    testImplementation("org.mechdancer", "dependency", "+")
    testImplementation("org.mechdancer", "remote", "+")
    testImplementation("org.slf4j", "slf4j-api", "+")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}

// Source code export task
val sourceTaskName = "sourcesJar"
task<Jar>(sourceTaskName) {
    archiveClassifier.set("sources")
    group = "build"

    from(sourceSets["main"].allSource)
}
tasks["jar"].dependsOn(sourceTaskName)

// Default inline class
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/MechDancer/symbolcalculator")
            credentials {
                username = System.getenv("USERNAME")
                password = System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("library") {
            from(components["kotlin"])
        }
    }
}
