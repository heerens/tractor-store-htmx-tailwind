


dependencies {
    implementation("io.github.microutils:kotlin-logging:1.12.+")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}

jte {
    sourceDirectory.set(file("src/main/kotlin/com/inauditech/tractorstore/checkout/presentation").toPath())
    generate()
}
