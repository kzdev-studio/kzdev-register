plugins {
    java
}

group = "pl.kzdev"
version = "1.0.0"

repositories {
    mavenCentral()
    maven { name = "Spigot"; url = uri("https://hub.spigotmc.org/nexus/content/groups/public/") }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
}

tasks.jar {
    archiveFileName.set("KZDevRegister.jar")
}
