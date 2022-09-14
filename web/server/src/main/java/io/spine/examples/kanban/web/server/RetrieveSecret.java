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

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkArgument;
import static io.spine.examples.kanban.web.server.Configuration.projectId;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Retrieves from the Secret Manager the {@code latest} version of the secret
 * with the provided name.
 *
 * <p> Secret is assumed to belong to the {@link Configuration#projectId()}.
 */
final class RetrieveSecret {

    /**
     * Prevents the utility class instantiation.
     */
    private RetrieveSecret() {
    }

    static String withName(String name) {
        checkArgument(
                !name.isEmpty(),
                "A secret's name cannot be an empty string."
        );

        SecretVersionName secret = SecretVersionName.of(projectId(), name, "latest");
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            return client
                    .accessSecretVersion(secret)
                    .getPayload()
                    .getData()
                    .toStringUtf8();
        } catch (IOException e) {
            throw newIllegalStateException(
                    e,
                    String.format("Unable to retrieve the `%s`.", secret)
            );
        }
    }
}
