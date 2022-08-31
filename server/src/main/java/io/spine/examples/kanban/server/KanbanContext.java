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

import io.spine.examples.kanban.server.board.BoardInitProcess;
import io.spine.examples.kanban.server.board.BoardRepository;
import io.spine.examples.kanban.server.card.CardRepository;
import io.spine.examples.kanban.server.column.ColumnAdditionRepository;
import io.spine.examples.kanban.server.column.ColumnRepository;
import io.spine.examples.kanban.server.column.MoveCardRepository;
import io.spine.examples.kanban.server.view.BoardProjection;
import io.spine.server.BoundedContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.server.DefaultRepository;

/**
 * Configures Kanban Bounded Context with repositories.
 */
final class KanbanContext {

    static final String NAME = "Kanban";

    /** Prevents instantiation of this utility class. */
    private KanbanContext() {
    }

    /**
     * Creates {@code BoundedContextBuilder} for the Kanban context and fills it with
     * repositories.
     */
    static BoundedContextBuilder newBuilder() {
        return BoundedContext
                .singleTenant(NAME)
                .add(new BoardRepository())
                .add(DefaultRepository.of(BoardInitProcess.class))
                .add(new ColumnRepository())
                .add(new ColumnAdditionRepository())
                .add(new CardRepository())
                .add(new MoveCardRepository())
                .add(DefaultRepository.of(BoardProjection.class));
    }
}
