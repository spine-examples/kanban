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

import com.google.common.collect.ImmutableList;
import io.spine.examples.kanban.Column;
import io.spine.examples.kanban.command.AddColumn;
import io.spine.examples.kanban.event.BoardInitialized;
import io.spine.examples.kanban.server.KanbanContextTest;
import io.spine.examples.kanban.server.board.given.AddColumnCommands;
import io.spine.testing.server.CommandSubject;
import io.spine.testing.server.EventSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableList.toImmutableList;

@DisplayName("BoardInitProcess should")
class BoardInitProcessTest extends KanbanContextTest {

    @BeforeEach
    void sendCommand() {
        context().receivesCommand(createBoard());
    }

    @Test
    @DisplayName("issue addition commands for all default columns")
    void issuesCommands() {
        CommandSubject assertCommands = assertCommands(AddColumn.class);
        int expectedCount = DefaultColumns.count();
        assertCommands.hasSize(expectedCount);

        ImmutableList<AddColumn> expectedCommands =
                DefaultColumns.additionCommands(board())
                              .stream()
                              .map(AddColumnCommands::clearId)
                              .collect(toImmutableList());

        for (int i = 0; i < expectedCount; i++) {
            assertCommands.message(i)
                          .comparingExpectedFieldsOnly()
                          .isEqualTo(expectedCommands.get(i));
        }
    }

    @Test
    @DisplayName("add default columns")
    void addsColumns() {
        receivedCommands(AddColumn.class)
                .map(BoardInitProcessTest::toColumn)
                .forEach(this::assertColumnExists);
    }

    private static Column toColumn(AddColumn c) {
        return Column
                .newBuilder()
                .setId(c.getColumn())
                .setBoard(c.getBoard())
                .setName(c.getName())
                .vBuild();
    }

    private void assertColumnExists(Column column) {
        context().assertEntityWithState(column.getId(), Column.class)
                 .hasStateThat()
                 .comparingExpectedFieldsOnly()
                 .isEqualTo(column);
    }



    @Test
    @DisplayName("emit the `BoardInitialized` event when terminated")
    void emitsEvent() {
        EventSubject assertEvents = assertEvents(BoardInitialized.class);
        assertEvents.hasSize(1);

        BoardInitialized expected =
                BoardInitialized.newBuilder()
                                .setBoard(board())
                                .vBuild();
        assertEvents.message(0)
                    .comparingExpectedFieldsOnly()
                    .isEqualTo(expected);
    }

    @Test
    @DisplayName("be deleted when terminated")
    void deletesItself() {
        context().assertEntity(board(), BoardInitProcess.class)
                 .deletedFlag()
                 .isTrue();
    }
}
