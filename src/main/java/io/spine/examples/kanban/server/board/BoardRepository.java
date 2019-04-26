/*
 * Copyright 2019, TeamDev. All rights reserved.
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
import io.spine.examples.kanban.event.CardCreated;
import io.spine.examples.kanban.event.ColumnCreated;
import io.spine.server.aggregate.AggregateRepository;

/**
 * The repository for managing {@link BoardAggregate} instances.
 *
 * <p>Routes the {@link io.spine.examples.kanban.event.CardCreated card created event} to an
 * appropriate {@linkplain BoardAggregate board aggregate} so that a new card appears in the
 * first column.
 */
public final class BoardRepository extends AggregateRepository<BoardId, BoardAggregate> {

    public BoardRepository() {
        super();
        eventRouting()
                //TODO:2019-04-26:alexander.yevsyukov: Use interface-base routing when the feature is available form core-java.
                // See: https://github.com/SpineEventEngine/core-java/issues/1037
                //                .route(BoardElementEvent.class, (event, context) -> event.board())

                .route(CardCreated.class, (event, context) -> event.board())
                .route(ColumnCreated.class, (event, context) -> event.board());
    }
}
