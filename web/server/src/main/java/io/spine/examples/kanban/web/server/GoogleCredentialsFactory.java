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

package io.spine.examples.kanban.web.server;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static io.spine.util.Exceptions.newIllegalStateException;
import static java.nio.charset.Charset.defaultCharset;

/**
 * Provides factory methods for creating {@link GoogleCredentials}.
 */
final class GoogleCredentialsFactory {

    /**
     * Prevents the utility class instantiation.
     */
    private GoogleCredentialsFactory() {

    }

    /**
     * Creates {@link GoogleCredentials} from the JSON representation of an OAuth2
     * token.
     */
    static GoogleCredentials fromJson(String json) {
        try {
            return GoogleCredentials.fromStream(streamFrom(json));
        } catch (IOException e) {
            throw newIllegalStateException(e, "Unable to read `GoogleCredentials`.");
        }
    }

    private static ByteArrayInputStream streamFrom(String data) {
        return new ByteArrayInputStream(data.getBytes(defaultCharset()));
    }

    /**
     * Creates mock {@link GoogleCredentials} for accessing a database emulator.
     */
    static GoogleCredentials forEmulator() {
        return GoogleCredentials.create(tokenForEmulator());
    }

    private static AccessToken tokenForEmulator() {
        Date expirationDate = Date.from(
                Instant.now().plus(Duration.of(1, ChronoUnit.DAYS))
        );
        return new AccessToken(
                "emulator-does-not-require-authentication",
                expirationDate
        );
    }
}
