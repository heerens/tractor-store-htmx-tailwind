import org.springframework.boot.gradle.tasks.bundling.BootJar

val springBootVersion = "3.3.0"
val springCloudAwsVersion = "3.1.1"
val kotlinVersion = "1.9.24"
val koTestVersion = "4.6.+"
val mockkVersion = "1.10.+"
val kotlinLoggingVersion = "1.12.+"
val jteVersion = "3.1.6"

group = "com.inauditech.tractorstore.discover"
version = "1.0-SNAPSHOT"

plugins {
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"
    id("gg.jte.gradle") version "3.1.6"
}

allprojects {
    repositories {
        mavenCentral()
    }
}

dependencies {

    implementation(project("navigation"))
    implementation(project("product"))
    implementation(project("discovery"))

    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("gg.jte:jte-spring-boot-starter-3:$jteVersion")
    implementation("gg.jte:jte-kotlin:$jteVersion")

    testImplementation(kotlin("test"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<BootJar> {
    archiveFileName.set("app.jar")
    manifest {
        attributes("Start-Class" to "com.inauditech.tractorstore.discover.DiscoveryApplicationKt")
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    // apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
//    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "gg.jte.gradle")

    dependencyManagement {
        imports {
            // use the same spring boot version as in the root
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
        }
    }

    dependencies {

        implementation("org.springframework.boot:spring-boot-starter")
        implementation("org.springframework.boot:spring-boot-starter-web")

        implementation("gg.jte:jte-spring-boot-starter-3:$jteVersion")
        implementation("gg.jte:jte-kotlin:$jteVersion")

        testImplementation(kotlin("test"))
    }

    kotlin {
        jvmToolchain(17)
    }
}
