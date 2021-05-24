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
import io.spine.examples.kanban.ColumnId;
import io.spine.examples.kanban.command.CreateColumn;
import io.spine.examples.kanban.server.KanbanContextTest;
import io.spine.testing.server.CommandSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static io.spine.examples.kanban.server.board.BoardInitProcess.defaultColumnCount;
import static io.spine.protobuf.AnyPacker.unpack;
import static java.util.stream.Collectors.toList;

@DisplayName("BoardInitProcess should")
class BoardInitProcessTest extends KanbanContextTest {

    @BeforeEach
    void sendCommand() {
        context().receivesCommand(createBoard());
    }

    @Disabled("validation in `build()` breaks `comparingExpectedFieldsOnly()`")
    @Test
    @DisplayName("issue creation commands for all default columns")
    void issuesCreationCommands() {
        CommandSubject commands = context().assertCommands();
        CreateColumn commandForBoardInit = CreateColumn
                .newBuilder()
                .setBoardInit(true)
                // Call `buildPartial()` instead of `vBuild()` in order to be able to omit setting
                // the `name` name field which is required.
                .buildPartial();
        IntStream.range(0, defaultColumnCount())
                 .forEach(i -> commands.message(i)
                                       .comparingExpectedFieldsOnly()
                                       .isEqualTo(commandForBoardInit)
                 );
    }

    @Test
    @DisplayName("create default columns")
    void createsColumns() {
        Collection<ColumnId> defaultColumns = createdColumns();
        defaultColumns.forEach(
                c -> context().assertEntityWithState(c, Column.class)
                              .exists()
        );
    }

    @Test
    @DisplayName("delete itself when finished")
    void isDeletedWhenFinished() {
        context().assertEntity(board(), BoardInitProcess.class)
                 .deletedFlag()
                 .isTrue();
    }

    private Collection<ColumnId> createdColumns() {
        List<ColumnId> collect = context().assertCommands()
                .withType(CreateColumn.class)
                .actual()
                .stream()
                .map(c -> unpack(c.getMessage(), CreateColumn.class))
                .map(CreateColumn::getColumn)
                .collect(toList());
        return collect;
    }
}
