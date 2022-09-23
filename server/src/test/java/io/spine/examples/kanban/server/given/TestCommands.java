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

package io.spine.examples.kanban.server.given;

import io.spine.examples.kanban.BoardId;
import io.spine.examples.kanban.CardId;
import io.spine.examples.kanban.ColumnId;
import io.spine.examples.kanban.ColumnPosition;
import io.spine.examples.kanban.WipLimit;
import io.spine.examples.kanban.command.AddColumn;
import io.spine.examples.kanban.command.CreateBoard;
import io.spine.examples.kanban.command.CreateCard;
import io.spine.examples.kanban.command.SetWipLimit;

import static io.spine.testing.TestValues.randomString;

/**
 * Provides factory methods for test commands.
 */
public final class TestCommands {

    /**
     * Prevents utility class instantiation.
     */
    private TestCommands() {
    }

    /**
     * Creates the {@link CreateBoard} command with the provided board ID.
     */
    public static CreateBoard createBoard(BoardId board) {
        return CreateBoard
                .newBuilder()
                .setBoard(board)
                .vBuild();
    }

    /**
     * Creates the {@link AddColumn} command with the provided board ID,
     * column ID, position and a generated column name.
     */
    public static AddColumn addColumn(
            BoardId board,
            ColumnId column,
            ColumnPosition columnPosition
    ) {
        return AddColumn
                .newBuilder()
                .setBoard(board)
                .setColumn(column)
                .setName(randomString())
                .setDesiredPosition(columnPosition)
                .vBuild();
    }

    /**
     * Creates the {@link CreateCard} command with the provided board ID,
     * card ID and a generated card name.
     */
    public static CreateCard createCard(BoardId board, CardId card) {
        return CreateCard
                .newBuilder()
                .setCard(card)
                .setBoard(board)
                .setName(randomString())
                .vBuild();
    }

    /**
     * Creates the {@link SetWipLimit} command with the provided column ID
     * and WIP limit.
     */
    public static SetWipLimit setWipLimit(ColumnId column, WipLimit limit) {
        return SetWipLimit
                .newBuilder()
                .setColumn(column)
                .setLimit(limit)
                .vBuild();
    }
}
