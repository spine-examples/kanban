/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.examples.kanban.server;

import io.spine.base.Production;
import io.spine.server.BoundedContextBuilder;
import io.spine.server.Server;
import io.spine.server.ServerEnvironment;
import io.spine.server.storage.memory.InMemoryStorageFactory;
import io.spine.server.transport.memory.InMemoryTransportFactory;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;

/**
 * A gRPC server running a Kanban Bounded Context.
 */
public final class KanbanServer {

    /**
     * Prevents direct instantiation.
     */
    private KanbanServer() {
    }

    /**
     * Creates a new Spine {@code Server} instance at the
     * {@linkplain io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT default} port.
     */
    static Server create() {
        configureEnvironment();
        BoundedContextBuilder context = KanbanContext.newBuilder();
        return Server
                .atPort(DEFAULT_CLIENT_SERVICE_PORT)
                .add(context)
                .build();
    }

    private static void configureEnvironment() {
        ServerEnvironment
                .when(Production.class)
                .use(InMemoryStorageFactory.newInstance())
                .use(InMemoryTransportFactory.newInstance());
    }

    /**
     * The entry point of the server application.
     */
    public static void main(String[] args) throws IOException {
        Server server = create();
        server.start();
        server.awaitTermination();
    }
}
