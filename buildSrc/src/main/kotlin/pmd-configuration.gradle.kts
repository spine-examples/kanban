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

import io.spine.examples.kanban.dependency.Pmd

/**
 * Adds the PMD code analyzer to a project and configures it.
 *
 * - Configures project compilation to fail in case the analyzer finds issues.
 * - Removes the default set of rules and loads custom rules specified in
 *   the `buildSrc/src/main/resources/pmd.xml`.
 * - Configures PMD to analyze only the main source set and avoid analyzing tests.
 */

plugins {
    java
    pmd
}

pmd {
    toolVersion = Pmd.version
    isConsoleOutput = true
    isIgnoreFailures = false

    // Remove default rules.
    ruleSets = listOf()

    // Load custom rules.
    val pmdSettings = file("$rootDir/buildSrc/src/main/resources/pmd.xml")
    ruleSetConfig = resources.text.fromFile(pmdSettings)

    reportsDir = file("$projectDir/build/reports/pmd")

    // Analyze only the main source set (i.e. do not analyze tests).
    sourceSets = listOf(project.sourceSets.named("main").get())
}
