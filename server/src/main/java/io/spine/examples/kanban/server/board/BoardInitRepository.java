/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.examples.kanban.server.board;

import io.spine.examples.kanban.BoardId;
import io.spine.examples.kanban.BoardInit;
import io.spine.examples.kanban.event.ColumnCreated;
import io.spine.server.procman.ProcessManagerRepository;
import io.spine.server.route.EventRouting;

import static io.spine.server.route.EventRoute.noTargets;
import static io.spine.server.route.EventRoute.withId;

/**
 * Manages instances of {@link BoardInitProcess}.
 */
public final class BoardInitRepository
        extends ProcessManagerRepository<BoardId, BoardInitProcess, BoardInit> {

    @Override
    protected void setupEventRouting(EventRouting<BoardId> routing) {
        super.setupEventRouting(routing);
        routing.route(ColumnCreated.class, (event, context) ->
                        event.getBoardInit()
                                ? withId(event.getBoard())
                                : noTargets());
    }
}
