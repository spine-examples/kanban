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

import com.google.common.collect.ImmutableList;
import io.spine.examples.kanban.Column;
import io.spine.examples.kanban.command.AddColumn;
import io.spine.examples.kanban.command.CreateColumn;
import io.spine.examples.kanban.event.BoardInitialized;
import io.spine.examples.kanban.server.KanbanContextTest;
import io.spine.testing.server.CommandSubject;
import io.spine.testing.server.EventSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.protobuf.AnyPacker.unpack;

@DisplayName("BoardInitProcess should")
class BoardInitProcessTest extends KanbanContextTest {

    @BeforeEach
    void sendCommand() {
        context().receivesCommand(createBoard());
    }

    @Test
    @DisplayName("issue addition commands for all default columns")
    void issuesCommands() {
        CommandSubject issuedCommands = context().assertCommands()
                                                 .withType(AddColumn.class);
        int expectedCount = DefaultColumns.count();
        issuedCommands.hasSize(expectedCount);

        ImmutableList<AddColumn> expectedCommands =
                DefaultColumns.additionCommands(board())
                              .stream()
                              .map(BoardInitProcessTest::clearId)
                              .collect(toImmutableList());

        for (int i = 0; i < expectedCount; i++) {
            issuedCommands.message(i)
                          .comparingExpectedFieldsOnly()
                          .isEqualTo(expectedCommands.get(i));
        }
    }

    private static AddColumn clearId(AddColumn c) {
        return c.toBuilder()
                .clearColumn()
                .buildPartial();
    }

    @Test
    @DisplayName("add default columns")
    void addsColumns() {
        ImmutableList<Column> expectedColumns = expectedColumns();
        expectedColumns.forEach(
                c -> context().assertEntityWithState(c.getId(), Column.class)
                              .hasStateThat()
                              .comparingExpectedFieldsOnly()
                              .isEqualTo(c)
        );
    }

    private ImmutableList<Column> expectedColumns() {
        return context().assertCommands()
                        .withType(AddColumn.class)
                        .actual()
                        .stream()
                        .map(c -> unpack(c.getMessage(), AddColumn.class))
                        .map(BoardInitProcessTest::toColumn)
                        .collect(toImmutableList());
    }

    private static Column toColumn(AddColumn c) {
        return Column
                .newBuilder()
                .setId(c.getColumn())
                .setBoard(c.getBoard())
                .setName(c.getName())
                .vBuild();
    }

    @Test
    @DisplayName("emit the `BoardInitialized` event when terminated")
    void emitsEvent() {
        EventSubject events = context().assertEvents()
                                       .withType(BoardInitialized.class);
        events.hasSize(1);
        BoardInitialized expected = BoardInitialized
                .newBuilder()
                .setBoard(board())
                .vBuild();
        events.message(0)
              .comparingExpectedFieldsOnly()
              .isEqualTo(expected);
    }

    @Test
    @DisplayName("delete itself when terminated")
    void deletesItself() {
        context().assertEntity(board(), BoardInitProcess.class)
                 .deletedFlag()
                 .isTrue();
    }
}
