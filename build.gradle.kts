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

/**
 * Gradle configuration for the whole project.
 *
 * The configuration is divided in multiple script plugins located in `buildSrc/src/kotlin`.
 * Only a brief description is provided when applying a plugin. However, each of these
 * plugins contains a more detailed description in their source file.
 */

allprojects {
    apply<IdeaPlugin>()
}

subprojects {
    apply<JavaPlugin>()

    /*
     * Configure the `Javac`. The main configuration is instructing the `Javac` that
     * project uses JDK 8.
     */
    apply<JavacConfigurationPlugin>()

    // Configure repositories, add dependencies and force transitive dependencies.
    apply<DependencyManagementPlugin>()

    /*
     * Apply the Error Prone plugin. Also, configures the `Javac` to avoid known
     * issues with Error Prone.
     */
    apply<ErrorProneConfigurationPlugin>()

    // Apply and configure the PMD plugin.
    apply<PmdConfigurationPlugin>()

    // Configure test-running tasks.
    apply<TestsConfigurationPlugin>()
}
