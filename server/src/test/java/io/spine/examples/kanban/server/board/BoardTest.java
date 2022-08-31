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

package io.spine.examples.kanban.server.board;

import io.spine.examples.kanban.Column;
import io.spine.examples.kanban.event.ColumnAdded;
import io.spine.examples.kanban.server.KanbanContextTest;
import io.spine.testing.server.EventSubject;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Kanban Context Board logic should")
@Ignore("Restore the tests when move card API is defined")
class BoardTest extends KanbanContextTest {

    @BeforeEach
    void setupBoard() {
        context().receivesCommand(createBoard());
    }

    @Nested
    @DisplayName("add a new column")
    class AddColumn {

        @BeforeEach
        void addColumn() {
            context().receivesCommand(BoardTest.this.addColumn());
        }

        @Test
        @DisplayName("as entity with `Column` state")
        void entity() {
            context().assertEntityWithState(column(), Column.class)
                     .exists();
        }

        @Test
        @DisplayName("generating `ColumnAdded` event")
        void event() {
            EventSubject assertEvents =
                    context().assertEvents()
                             .withType(ColumnAdded.class);

            assertEvents.hasSize(1);
            ColumnAdded expected = ColumnAdded
                    .newBuilder()
                    .setBoard(board())
                    .setColumn(column())
                    // We call `buildPartial()` instead of `vBuild()` to be able to
                    // omit the `name` field, which is `required` in the event.
                    .buildPartial();
            assertEvents.message(0)
                        .comparingExpectedFieldsOnly()
                        .isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Move card emitting")
    class MoveCard {

        @BeforeEach
        void setupCard() {
            context().receivesCommand(createCard());
        }
    }
}
