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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

/**
 * Loads configuration from {@code resources/config.properties} and provides access
 * to it.
 *
 * <p> Configuration keys that can be set are present in the {@link Key}.
 */
class Configuration {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = loadProperties();

    /**
     * Prevents the utility class instantiation.
     */
    private Configuration() {
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream stream = configFileAsStream()) {
            properties.load(stream);
            return properties;
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static InputStream configFileAsStream() {
        return Configuration.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE);
    }

    static boolean productionEnvironment() {
        Environment currentEnvironment = Environment.valueOf(get(Key.ENV));
        return currentEnvironment == Environment.PRODUCTION;
    }

    private static String get(Key key) {
        String property = properties.getProperty(key.literal);

        checkNotNull(
                property,
                String.format("The `%s` property is not set.", key.literal)
        );
        checkArgument(
                !property.isEmpty(),
                String.format("The `%s` property cannot be an empty string.", key.literal)
        );

        return property;
    }

    /**
     * The name of the secret with service account key to access Firebase.
     */
    static String firebaseServiceAccountSecret() {
        return get(Key.FIREBASE_SERVICE_ACCOUNT_SECRET);
    }

    /**
     * The URL of the Firebase Realtime Database.
     */
    static String firebaseDatabaseUrl() {
        return get(Key.FIREBASE_DB_URL);
    }

    /**
     * The identifier of the Google Cloud project.
     */
    static String projectId() {
        return get(Key.GCP_PROJECT_ID);
    }

    /**
     * Configuration keys that can be set in the {@code resources/config.properties}.
     */
    private enum Key {

        /**
         * The name of the secret with service account key to access Firebase.
         */
        FIREBASE_SERVICE_ACCOUNT_SECRET("secret.firebase-service-account"),

        /**
         * The URL of the Firebase Realtime Database.
         */
        FIREBASE_DB_URL("firebase.database.url"),

        /**
         * The identifier of the Google Cloud project.
         */
        GCP_PROJECT_ID("gcp.project-id"),

        /**
         * The current environment.
         *
         * <p> Values should parseable to {@link Environment}.
         */
        ENV("env");

        private final String literal;

        Key(String literal) {
            this.literal = literal;
        }
    }

    /**
     * Values of the {@code env} setting in the {@code config.properties} file.
     */
    private enum Environment {
        PRODUCTION,
        DEVELOPMENT
    }
}
