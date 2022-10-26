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

package io.spine.examples.kanban.server.view;

import io.spine.core.Subscribe;
import io.spine.examples.kanban.BoardId;
import io.spine.examples.kanban.Card;
import io.spine.examples.kanban.Column;
import io.spine.examples.kanban.event.BoardCreated;
import io.spine.examples.kanban.event.ColumnAdditionRequested;
import io.spine.examples.kanban.event.ColumnMovedOnBoard;
import io.spine.examples.kanban.event.ColumnPlaced;
import io.spine.examples.kanban.view.BoardView;
import io.spine.server.projection.Projection;

/**
 * Builds display information for a board.
 */
public final class BoardProjection
        extends Projection<BoardId, BoardView, BoardView.Builder> {

    @Subscribe
    void on(BoardCreated e) {
        builder().setId(e.getBoard());
    }

    @Subscribe
    void on(ColumnAdditionRequested e) {
        Column column = Column.newBuilder()
                              .setId(e.getColumn())
                              .setBoard(e.getBoard())
                              .setName(e.getName())
                              .setPosition(e.getDesiredPosition())
                              .vBuild();

        builder().addColumn(column.getPosition().zeroBasedIndex(), column);
    }

    @Subscribe
    void on(ColumnPlaced e) {
        Column column = state()
                .getColumn(e.getDesiredPosition().zeroBasedIndex())
                .toBuilder()
                .setPosition(e.getActualPosition())
                .vBuild();

        builder().removeColumn(e.getDesiredPosition().zeroBasedIndex())
                .addColumn(column.getPosition().zeroBasedIndex(), column);
    }

    @Subscribe
    void on(ColumnMovedOnBoard e) {
        Column column = state().getColumn(e.getFrom().zeroBasedIndex());
        column = column.toBuilder()
                       .setPosition(e.getTo())
                       .vBuild();

        builder().removeColumn(e.getFrom().zeroBasedIndex())
                .addColumn(column.getPosition().zeroBasedIndex(), column);
    }

    @Subscribe
    void updated(Card card) {
        int index = state().getCardList()
                           .indexOf(card);
        if (index != -1) {
            builder().setCard(index, card);
        } else {
            builder().addCard(card);
        }
    }
}
