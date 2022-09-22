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

package io.spine.examples.kanban.server.board;

import io.spine.examples.kanban.Board;
import io.spine.examples.kanban.ColumnId;
import io.spine.examples.kanban.ColumnPosition;
import io.spine.examples.kanban.command.AddColumn;
import io.spine.examples.kanban.event.BoardCreated;
import io.spine.examples.kanban.server.KanbanContextTest;
import io.spine.examples.kanban.server.given.ColumnPositions;
import io.spine.testing.server.EventSubject;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.kanban.rejection.Rejections.ColumnNameAlreadyTaken;
import static io.spine.testing.TestValues.randomString;

@DisplayName("Kanban Context Board logic should")
@Ignore("Restore the tests when move card API is defined")
class BoardTest extends KanbanContextTest {

    @BeforeEach
    void setupBoard() {
        context().receivesCommand(createBoard());
    }

    @Nested
    @DisplayName("create a board")
    class CreateBoard {

        @Test
        @DisplayName("as a `Board` entity")
        void entity() {
            context().assertEntityWithState(board(), Board.class)
                     .exists();
        }

        @Test
        @DisplayName("emitting the `BoardCreated` event")
        void event() {
            BoardCreated expected =
                    BoardCreated.newBuilder()
                                .setBoard(board())
                                .build();

            context().assertEvents()
                     .withType(BoardCreated.class)
                     .message(0)
                     .isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("keep column names unique")
    class GuardColumnNameUniqueness {

        private AddColumn rejectedCommand;

        @BeforeEach
        void sendCommands() {
            String name = randomString();
            ColumnPosition position = ColumnPositions.of(
                    DefaultColumns.count() + 1,
                    DefaultColumns.count() + 1
            );
            AddColumn successfulCommand =
                    AddColumn.newBuilder()
                             .setBoard(board())
                             .setColumn(ColumnId.generate())
                             .setName(name)
                             .setDesiredPosition(position)
                             .vBuild();
            position = ColumnPositions.of(
                    DefaultColumns.count() + 2,
                    DefaultColumns.count() + 2
            );
            rejectedCommand = AddColumn.newBuilder()
                                       .setBoard(board())
                                       .setColumn(ColumnId.generate())
                                       .setName(name)
                                       .setDesiredPosition(position)
                                       .vBuild();

            context().receivesCommand(successfulCommand);
            context().receivesCommand(rejectedCommand);
        }

        @Test
        @DisplayName("by rejecting the addition of the column with a duplicate name")
        void rejection() {
            EventSubject assertRejections =
                    context().assertEvents()
                             .withType(ColumnNameAlreadyTaken.class);
            assertRejections.hasSize(1);

            ColumnNameAlreadyTaken expected =
                    ColumnNameAlreadyTaken
                            .newBuilder()
                            .setColumn(rejectedCommand.getColumn())
                            .setName(rejectedCommand.getName())
                            .build();
            assertRejections.message(0).isEqualTo(expected);
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
