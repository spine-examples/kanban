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

package io.spine.examples.kanban.server.view;

import com.google.protobuf.Message;
import io.spine.examples.kanban.BoardId;
import io.spine.examples.kanban.Card;
import io.spine.examples.kanban.view.BoardView;
import io.spine.protobuf.AnyPacker;
import io.spine.server.projection.ProjectionRepository;
import io.spine.system.server.event.EntityStateChanged;

import static io.spine.server.route.EventRoute.noTargets;
import static io.spine.server.route.EventRoute.withId;

/**
 * Manages {@link BoardProjection}s.
 */
public final class BoardProjectionRepository
        extends ProjectionRepository<BoardId, BoardProjection, BoardView> {

    public BoardProjectionRepository() {
        super();
        eventRouting().route(EntityStateChanged.class, (event, context) -> {
            Message newState = AnyPacker.unpack(event.getNewState());
            return newState instanceof Card
                   ? withId(((Card) newState).getBoard())
                   : noTargets();
        });
    }
}
