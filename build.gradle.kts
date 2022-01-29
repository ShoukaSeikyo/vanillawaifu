import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://raw.githubusercontent.com/TerraformersMC/Archive/main/releases/")
        maven(url = "https://maven.shedaniel.me/")
    }
}

plugins {
//    kotlin("jvm") version "1.4.31"
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("fabric-loom") version "0.11-SNAPSHOT"
    `kotlin-dsl`
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    minecraft("com.mojang:minecraft:1.18.1")
    mappings("net.fabricmc:yarn:1.18.1+build.4:v2")

    implementation(gradleApi())
    implementation("com.google.code.gson:gson:2.8.9")

    modImplementation("net.fabricmc:fabric-loader:0.12.11")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.44.0+1.18")

    modImplementation("net.fabricmc:fabric-language-kotlin:1.6.1+kotlin.1.5.10")

    compileOnly ("org.projectlombok:lombok:1.18.22")
    annotationProcessor ("org.projectlombok:lombok:1.18.22")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.4"
    jvmTarget = "17"
    freeCompilerArgs = listOf("-Xjvm-default=all")
}

// allprojects {
//
//     apply(plugin = "fabric-loom")
//     apply(plugin = "org.jetbrains.kotlin.jvm")
//
//     dependencies {
//         minecraft(group = "com.mojang", name = "minecraft", version = ModInfo.minecraftVersion)
//         mappings(group = "net.fabricmc", name = "yarn", version = ModInfo.mappings.yarn, classifier = "v2")
//
//         modImplementation("net.fabricmc:fabric-loader:${ModInfo.mappings.loader}")
//         modImplementation("net.fabricmc.fabric-api:fabric-api:${ModInfo.mappings.fabric}")
//         modImplementation("net.fabricmc:fabric-language-kotlin:1.6.1+kotlin.1.5.10")
//
//         implementation(group = "com.github.twitch4j", name = "twitch4j", version = "1.5.0")
//         api(group = "com.github.twitch4j", name = "twitch4j", version = "1.5.0")
//
//         compileOnly ("org.projectlombok:lombok:1.18.22")
//         annotationProcessor ("org.projectlombok:lombok:1.18.22")
//
//         testCompileOnly ("org.projectlombok:lombok:1.18.22")
//         testAnnotationProcessor ("org.projectlombok:lombok:1.18.22")
//
//         ModInfo.mappings.setup.invoke(this)
//     }
//
//     tasks.getByName("processResources", ProcessResources::class) {
//         filesMatching("fabric.mod.json") {
//             expand("version" to ModInfo.minecraftVersion)
//         }
//     }
//
//     val compileKotlin: KotlinCompile by tasks
//     compileKotlin.kotlinOptions {
//         languageVersion = "1.4"
//         jvmTarget = "15"
//         freeCompilerArgs = listOf("-Xjvm-default=all")
//     }
// }
