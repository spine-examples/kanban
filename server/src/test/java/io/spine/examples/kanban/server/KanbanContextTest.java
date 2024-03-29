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

package io.spine.examples.kanban.server;

import io.spine.base.CommandMessage;
import io.spine.base.EventMessage;
import io.spine.testing.server.CommandSubject;
import io.spine.testing.server.EventSubject;
import io.spine.testing.server.blackbox.BlackBoxContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.protobuf.AnyPacker.unpack;

/**
 * Abstract base for tests in Kanban Bounded Context.
 */
public abstract class KanbanContextTest extends KanbanTest {

    private BlackBoxContext context;

    @BeforeEach
    void createContext() {
        context = BlackBoxContext.from(KanbanContext.newBuilder());
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    protected BlackBoxContext context() {
        return checkNotNull(context);
    }

    /**
     * Checks for commands of the provided type received by the bounded context under the test.
     */
    protected final <T extends CommandMessage> CommandSubject assertCommands(Class<T> commandClass) {
        return context()
                .assertCommands()
                .withType(commandClass);
    }

    /**
     * Streams commands of the provided type received by the bounded context during tests.
     */
    protected final <T extends CommandMessage> Stream<T> receivedCommands(Class<T> commandClass) {
        return context()
                .assertCommands()
                .withType(commandClass)
                .actual()
                .stream()
                .map(c -> unpack(c.getMessage(), commandClass));
    }

    /**
     * Checks for events of the provided type emitted by the bounded context under the test.
     */
    protected final <T extends EventMessage> EventSubject assertEvents(Class<T> eventClass) {
        return context()
                .assertEvents()
                .withType(eventClass);
    }
}
