plugins {
    kotlin("jvm") version "1.9.24"
    application
}

group = "com.inauditech.tractorstore"
version = "0.0.1"

repositories {
    mavenCentral()
}

val cdkVersion = "2.170.0"
val constructsVersion = "10.3.0"

dependencies {
    implementation("software.amazon.awscdk:aws-cdk-lib:$cdkVersion")
    implementation("software.constructs:constructs:$constructsVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass.set("com.inauditech.tractorstore.infra.AppKt")
}

tasks.withType<JavaExec>().configureEach {
    standardInput = System.`in`
}
