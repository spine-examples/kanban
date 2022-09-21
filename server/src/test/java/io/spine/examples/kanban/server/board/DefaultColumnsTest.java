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
import io.spine.examples.kanban.BoardId;
import io.spine.examples.kanban.ColumnPosition;
import io.spine.examples.kanban.command.AddColumn;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.examples.kanban.BoardInit.DefaultColumn;

@DisplayName("`DefaultColumns` should")
class DefaultColumnsTest extends UtilityClassTest<DefaultColumns> {

    DefaultColumnsTest() {
        super(DefaultColumns.class);
    }

    @Test
    @DisplayName("convert an enum value to the Title Case")
    void nameForConvertsToTitleCase() {
        String expected = "To Do";
        String actual = DefaultColumns.nameFor(DefaultColumn.TO_DO);

        assertThat(actual)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("produce commands to add 'To Do', 'In Progress', 'Review' and 'Done' columns")
    void additionCommandsProducesRightCommands() {
        BoardId board = BoardId.generate();
        AddColumn toDo =
                AddColumn.newBuilder()
                         .setBoard(board)
                         .setName("To Do")
                         .setDesiredPosition(
                                 ColumnPosition.newBuilder()
                                               .setIndex(1)
                                               .setOfTotal(4)
                                               .vBuild()
                         )
                         .buildPartial();

        AddColumn inProgress =
                AddColumn.newBuilder()
                         .setBoard(board)
                         .setName("In Progress")
                         .setDesiredPosition(
                                 ColumnPosition.newBuilder()
                                               .setIndex(2)
                                               .setOfTotal(4)
                                               .vBuild()
                         )
                         .buildPartial();

        AddColumn review =
                AddColumn.newBuilder()
                         .setBoard(board)
                         .setName("Review")
                         .setDesiredPosition(
                                 ColumnPosition.newBuilder()
                                               .setIndex(3)
                                               .setOfTotal(4)
                                               .vBuild()
                         )
                         .buildPartial();

        AddColumn done =
                AddColumn.newBuilder()
                         .setBoard(board)
                         .setName("Done")
                         .setDesiredPosition(
                                 ColumnPosition.newBuilder()
                                               .setIndex(4)
                                               .setOfTotal(4)
                                               .vBuild()
                         )
                         .buildPartial();

        ImmutableList<AddColumn> expected = ImmutableList.of(toDo, inProgress, review, done);
        ImmutableList<AddColumn> actual = DefaultColumns.additionCommands(board)
                                                        .stream()
                                                        .map(AddColumnCommands::clearId)
                                                        .collect(toImmutableList());

        assertThat(actual).isEqualTo(expected);
    }
}
