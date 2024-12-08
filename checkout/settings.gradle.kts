plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "tractor-store-checkout"

include(":contracts")
project(":contracts").projectDir = file("modules/contracts")

include(":checkout")
project(":checkout").projectDir = file("modules/checkout")

include(":account")
project(":account").projectDir = file("modules/account")
