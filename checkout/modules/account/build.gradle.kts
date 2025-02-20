


dependencies {
    implementation("io.github.microutils:kotlin-logging:1.12.+")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5") // For JWT creation & parsing
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}

jte {
    sourceDirectory.set(file("src/main/kotlin/com/inauditech/tractorstore/account/presentation").toPath())
    generate()
}
