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

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import io.spine.web.firebase.FirebaseClient;
import io.spine.web.firebase.FirebaseClientFactory;

import static io.spine.examples.kanban.web.server.Configuration.firebaseDatabaseUrl;
import static io.spine.examples.kanban.web.server.Configuration.firebaseServiceAccountSecret;
import static io.spine.examples.kanban.web.server.Configuration.productionEnvironment;

/**
 * Creates a singleton of the {@link FirebaseClient}.
 *
 * <p> The creation process depends on the server's environment.
 * <ul>
 *     <li> In the production environment retrieves the service account JSON key from
 *     Secret Manager to access Firebase Database.
 *     <li> In the development environment assumes that a database emulator is used
 *     and mocks credentials to access the emulator.
 * </ul>
 */
final class FirebaseClients {

    private static final FirebaseClient INSTANCE = createClient();

    /**
     * Prevents the utility class instantiation.
     */
    private FirebaseClients() {
    }

    /**
     * Returns the instance of {@link FirebaseClient}.
     */
    static FirebaseClient instance() {
        return INSTANCE;
    }

    private static FirebaseClient createClient() {
        FirebaseOptions options =
                FirebaseOptions.builder()
                               .setCredentials(credentials())
                               .setDatabaseUrl(firebaseDatabaseUrl())
                               .build();
        FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
        FirebaseDatabase database = FirebaseDatabase.getInstance(firebaseApp);
        FirebaseClient client = FirebaseClientFactory.remoteClient(database);
        return client;
    }

    private static GoogleCredentials credentials() {
        if (productionEnvironment()) {
            String key = RetrieveSecret.withName(firebaseServiceAccountSecret());
            return GoogleCredentialsFactory.fromJson(key);
        } else {
            return GoogleCredentialsFactory.forEmulator();
        }
    }
}
