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

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("produce commands to add 'To Do', 'In Progress', 'Review' and 'Done' columns")
    void additionCommandsProducesRightCommands() {
        BoardId board = BoardId.generate();
        ImmutableList<AddColumn> expected = expectedAdditionCommands(board);
        ImmutableList<AddColumn> actual =
                DefaultColumns.additionCommands(board)
                              .stream()
                              .map(AddColumnCommands::clearId)
                              .collect(toImmutableList());

        assertThat(actual).isEqualTo(expected);
    }

    /**
     * Returns the expected list of commands for adding defaults columns to the
     * provided board that should completely match the output list from the
     * {@link DefaultColumns#additionCommands(BoardId)} considering both methods get
     * the same input.
     *
     * <p> Produced commands do not have column IDs as they are supposed to be used
     * for comparison with an actual output of the mentioned method.
     */
    private static ImmutableList<AddColumn> expectedAdditionCommands(BoardId board) {
        AddColumn toDo = additionCommand(board, "To Do", position(1, 4));
        AddColumn inProgress = additionCommand(board, "In Progress", position(2, 4));
        AddColumn review = additionCommand(board, "Review", position(3, 4));
        AddColumn done = additionCommand(board, "Done", position(4, 4));

        return ImmutableList.of(toDo, inProgress, review, done);
    }

    private static AddColumn additionCommand(
            BoardId board,
            String name,
            ColumnPosition position
    ) {
        return AddColumn
                .newBuilder()
                .setBoard(board)
                .setName(name)
                .setDesiredPosition(position)
                .buildPartial();
    }

    private static ColumnPosition position(int index, int ofTotal) {
        return ColumnPosition
                .newBuilder()
                .setIndex(index)
                .setOfTotal(ofTotal)
                .vBuild();
    }
}
