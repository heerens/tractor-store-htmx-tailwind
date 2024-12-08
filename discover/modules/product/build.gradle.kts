


dependencies {
    implementation(project(":contracts"))

    implementation("io.github.microutils:kotlin-logging:1.12.+")
}

jte {
    sourceDirectory.set(file("src/main/kotlin/com/inauditech/tractorstore/product/presentation").toPath())
    generate()
}
