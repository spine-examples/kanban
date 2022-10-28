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
import io.spine.examples.kanban.command.MoveColumn;
import io.spine.examples.kanban.event.BoardCreated;
import io.spine.examples.kanban.server.KanbanContextTest;
import io.spine.testing.server.EventSubject;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.examples.kanban.rejection.Rejections.ColumnCannotBeMoved;
import static io.spine.examples.kanban.rejection.Rejections.ColumnNameAlreadyTaken;
import static io.spine.testing.TestValues.randomString;

@DisplayName("`Board` should")
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

            assertEvents(BoardCreated.class)
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
            rejectedCommand =
                    AddColumn.newBuilder()
                             .setBoard(board())
                             .setColumn(ColumnId.generate())
                             .setName(name)
                             .setDesiredPosition(position)
                             .vBuild();

            context().receivesCommand(successfulCommand);
            context().receivesCommand(rejectedCommand);
        }

        @Test
        @DisplayName("by rejecting addition of the column with a duplicate name")
        void rejection() {
            EventSubject assertRejections = assertEvents(ColumnNameAlreadyTaken.class);
            assertRejections.hasSize(1);

            ColumnNameAlreadyTaken expected =
                    ColumnNameAlreadyTaken.newBuilder()
                                          .setColumn(rejectedCommand.getColumn())
                                          .setName(rejectedCommand.getName())
                                          .vBuild();
            assertRejections.message(0)
                            .isEqualTo(expected);
        }
    }

    @Nested
    class MoveColumns {

        @Test
        @DisplayName("move columns left")
        void moveLeft() {
            ColumnPosition from = ColumnPositions.of(1, 4);
            ColumnId column = currentBoardState().getColumn(from.zeroBasedIndex());
            ColumnPosition to = ColumnPositions.of(4, 4);
            moveColumn(column, from, to);
            ColumnId actual = currentBoardState().getColumn(to.zeroBasedIndex());
            assertThat(column).isEqualTo(actual);
        }

        private Board currentBoardState() {
            return (Board) context()
                    .assertEntityWithState(board(), Board.class)
                    .actual()
                    .state();
        }

        private void moveColumn(ColumnId column, ColumnPosition from, ColumnPosition to) {
            MoveColumn command =
                    MoveColumn.newBuilder()
                              .setColumn(column)
                              .setBoard(board())
                              .setFrom(from)
                              .setTo(to)
                              .vBuild();
            context().receivesCommand(command);
        }

        @Test
        @DisplayName("move columns right")
        void moveRight() {
            ColumnPosition from = ColumnPositions.of(4, 4);
            ColumnId column = currentBoardState().getColumn(from.zeroBasedIndex());
            ColumnPosition to = ColumnPositions.of(1, 4);
            moveColumn(column, from, to);
            ColumnId actual = currentBoardState().getColumn(to.zeroBasedIndex());
            assertThat(column).isEqualTo(actual);
        }

        @Test
        @DisplayName("reject moving columns if `from` is invalid")
        void invalidFrom() {
            ColumnPosition invalidFrom = invalidPosition();
            ColumnId column = ColumnId.generate();
            ColumnPosition to = ColumnPositions.of(1, 4);
            moveColumn(column, invalidFrom, to);
            assertColumnCannotBeMoved(column, invalidFrom, to);
        }

        private ColumnPosition invalidPosition() {
            return ColumnPosition
                    .newBuilder()
                    .setIndex(3)
                    .setOfTotal(2)
                    .vBuild();
        }

        private void assertColumnCannotBeMoved(ColumnId column, ColumnPosition from, ColumnPosition to) {
            ColumnCannotBeMoved expected = columnCannotBeMoved(column, from, to);
            assertEvents(ColumnCannotBeMoved.class)
                    .message(0)
                    .isEqualTo(expected);
        }

        private ColumnCannotBeMoved columnCannotBeMoved(
                ColumnId column,
                ColumnPosition from,
                ColumnPosition to
        ) {
            return ColumnCannotBeMoved
                    .newBuilder()
                    .setColumn(column)
                    .setFrom(from)
                    .setTo(to)
                    .vBuild();
        }

        @Test
        @DisplayName("reject moving columns if `to` is invalid")
        void invalidTo() {
            ColumnPosition from = ColumnPositions.of(1, 4);
            ColumnId column = ColumnId.generate();
            ColumnPosition invalidTo = invalidPosition();
            moveColumn(column, from, invalidTo);
            assertColumnCannotBeMoved(column, from, invalidTo);
        }

        @Test
        @DisplayName("reject moving columns if index in `from` is wrong")
        void wrongIndexInFrom() {
            ColumnPosition wrongFrom = ColumnPositions.of(3, 4);
            ColumnId column = currentBoardState().getColumn(0);
            ColumnPosition to = ColumnPositions.of(2, 4);
            moveColumn(column, wrongFrom, to);
            assertColumnCannotBeMoved(column, wrongFrom, to);
        }

        @Test
        @DisplayName("reject moving columns if the total number of columns in `from` is wrong")
        void wrongTotalInFrom() {
            ColumnPosition wrongFrom = ColumnPositions.of(1, 2);
            ColumnId column = currentBoardState().getColumn(wrongFrom.zeroBasedIndex());
            ColumnPosition to = ColumnPositions.of(2, 4);
            moveColumn(column, wrongFrom, to);
            assertColumnCannotBeMoved(column, wrongFrom, to);
        }

        @Test
        @DisplayName("reject moving columns if the total number of columns in `to` is wrong")
        void wrongTotalInTo() {
            ColumnPosition from = ColumnPositions.of(1, 4);
            ColumnId column = currentBoardState().getColumn(from.zeroBasedIndex());
            ColumnPosition wrongTo = ColumnPositions.of(2, 2);
            moveColumn(column, from, wrongTo);
            assertColumnCannotBeMoved(column, from, wrongTo);
        }

        @Test
        @DisplayName("reject moving columns if `from` and `to` are same")
        void samePositions() {
            ColumnPosition position = ColumnPositions.of(1, 4);
            ColumnId column = currentBoardState().getColumn(position.zeroBasedIndex());
            moveColumn(column, position, position);
            assertColumnCannotBeMoved(column, position, position);
        }
    }

    @Nested
    @DisplayName("move card emitting")
    class MoveCard {

        @BeforeEach
        void setupCard() {
            context().receivesCommand(createCard());
        }
    }
}
