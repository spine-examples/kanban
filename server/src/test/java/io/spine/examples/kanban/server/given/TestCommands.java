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
import io.spine.examples.kanban.command.AddColumn;
import io.spine.examples.kanban.command.CreateBoard;
import io.spine.examples.kanban.command.CreateCard;
import io.spine.examples.kanban.command.SetWipLimit;

import static io.spine.testing.TestValues.randomString;

/**
 * Static factories for test command messages.
 */
public final class TestCommands {

    /**
     * Prevents instantiation.
     */
    private TestCommands() {
    }

    /**
     * Create a board with the passed ID.
     */
    public static CreateBoard createBoard(BoardId board) {
        return CreateBoard
                .newBuilder()
                .setBoard(board)
                .vBuild();
    }

    /**
     * Add the column to the board.
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
                .setName("Generated column" + randomString())
                .setDesiredPosition(columnPosition)
                .vBuild();
    }

    /**
     * Create the card on the specified board.
     */
    public static CreateCard createCard(BoardId board, CardId card) {
        return CreateCard
                .newBuilder()
                .setCard(card)
                .setBoard(board)
                .setName("Generated card " + randomString())
                .vBuild();
    }

    /**
     * Set the WIP limit for the column.
     */
    public static SetWipLimit setWipLimit(ColumnId column, int limit) {
        return SetWipLimit
                .newBuilder()
                .setColumn(column)
                .setLimit(WipLimits.of(limit))
                .vBuild();
    }
}
