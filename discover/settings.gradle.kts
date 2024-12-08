plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "tractor-store-discover"

include(":contracts")
project(":contracts").projectDir = file("modules/contracts")

include(":navigation")
project(":navigation").projectDir = file("modules/navigation")

include(":discovery")
project(":discovery").projectDir = file("modules/discovery")

include(":product")
project(":product").projectDir = file("modules/product")
