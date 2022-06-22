/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.net.URI

plugins {
    idea
    java
    pmd
}

subprojects {
    apply {
        plugin("idea")
        plugin("java")
        plugin("pmd")
        plugin("error-prone")

        from("$rootDir/version.gradle.kts")
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = URI("https://europe-maven.pkg.dev/spine-event-engine/releases")
        }
        maven {
            url = URI("https://europe-maven.pkg.dev/spine-event-engine/snapshots")
        }
    }

    val javaVersion = JavaVersion.VERSION_1_8
    java {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    val guavaVersion: String by extra
    val grpcVersion: String by extra

    val checkerFrameworkVersion: String by extra

    val apiGuardianVersion: String by extra
    val junit5Version: String by extra

    dependencies {
        implementation("com.google.guava:guava:$guavaVersion")
        runtimeOnly("io.grpc:grpc-netty:$grpcVersion")

        implementation("org.checkerframework:checker-qual:$checkerFrameworkVersion")

        testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junit5Version")
        testImplementation("org.apiguardian:apiguardian-api:$apiGuardianVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
    }

    configurations {
        all {
            resolutionStrategy {
                force(
                    "com.google.guava:guava:$guavaVersion",
                    "com.google.truth:truth:1.1.3",
                    "com.google.truth.extensions:truth-java8-extension:1.1.3",
                    "com.google.truth.extensions:truth-proto-extension:1.1.3"
                )
            }
        }
    }

    tasks.withType<JavaCompile> {
        with(options) {
            /**
             * Explicitly states the encoding of the source and test source files, ensuring
             * correct execution of the `javac` task.
             */
            encoding = "UTF-8"
            compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
        }
    }

    val pmdVersion: String by extra
    pmd {
        toolVersion = pmdVersion
        isConsoleOutput = true
        isIgnoreFailures = false

        // Disable the default rule set to use the custom rules (see below).
        ruleSets = listOf()

        // Load PMD rules.
        val pmdSettings = file("$rootDir/gradle/pmd.xml")
        val textResource: TextResource = resources.text.fromFile(pmdSettings)
        ruleSetConfig = textResource

        reportsDir = file("$projectDir/build/reports/pmd")

        // Analyze only the main source set(i.e. do not analyze tests).
        val mainSourceSet = sourceSets.find {
            it.name == "main"
        }
        sourceSets = listOf(mainSourceSet)
    }

    tasks.withType<Test> {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
    }
}