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

package io.spine.examples.kanban.dependency

// https://github.com/google/error-prone
object ErrorProne {
    private const val group = "com.google.errorprone"

    object CorePlugin {
        const val version = "2.4.0"
        const val lib = "${group}:error_prone_core:${version}"
    }

    object JavacPlugin {
        const val version = "9+181-r4173-1"
        const val lib = "${group}:javac:${version}"
    }

    // https://github.com/tbroyer/gradle-errorprone-plugin
    object GradlePlugin {
        const val id = "net.ltgt.errorprone"

        /**
         * The version of this plugin is already specified in `buildSrc/build.gradle.kts` file.
         * Thus, when applying the plugin in projects build files, only the [id] should be used.
         */
        const val version = "2.0.2"
        const val lib = "net.ltgt.gradle:gradle-errorprone-plugin:${version}"
    }
}
