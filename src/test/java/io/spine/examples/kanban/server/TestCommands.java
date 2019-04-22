/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.examples.kanban.server;

import io.spine.examples.kanban.BoardId;
import io.spine.examples.kanban.CardId;
import io.spine.examples.kanban.ColumnId;
import io.spine.examples.kanban.WipLimit;
import io.spine.examples.kanban.command.CreateBoard;
import io.spine.examples.kanban.command.CreateCard;
import io.spine.examples.kanban.command.CreateColumn;
import io.spine.examples.kanban.command.MoveCard;
import io.spine.examples.kanban.command.SetWipLimit;
import io.spine.testing.TestValues;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Static factories for test command messages.
 */
class TestCommands {

    /** Prevents instantiation. */
    private TestCommands() {
    }

    /** Create a board with the passed ID. */
    static CreateBoard createBoard(BoardId board) {
        return CreateBoard
                .vBuilder()
                .setBoard(board)
                .build();
    }

    /** Create the column on the specified board. */
    static CreateColumn createColumn(BoardId board, ColumnId column) {
        return CreateColumn
                .vBuilder()
                .setBoard(board)
                .setColumn(column)
                .setName("Generated column" + TestValues.randomString())
                .build();
    }

    /** Create the card on the specified board. */
    static CreateCard createCard(BoardId board, CardId card) {
        return CreateCard
                .vBuilder()
                .setCard(card)
                .setBoard(board)
                .setName("Generated card " + TestValues.randomString())
                .build();
    }

    /** Move a card from the current column to the target one. */
    static MoveCard moveCard(CardId card, ColumnId current, ColumnId target) {
        checkArgument(!current.equals(target),
                      "The `target` column must not be equal to `current`.");
        return MoveCard
                .vBuilder()
                .setCard(card)
                .setCurrentColumn(current)
                .setTargetColumn(target)
                .build();
    }

    /**
     * Set the WIP limit for the column.
     *
     * <p>Passing zero clears the limit.
     *
     * @see WipLimit
     */
    static SetWipLimit setWipLimit(ColumnId column, int limit) {
        return SetWipLimit
                .vBuilder()
                .setColumn(column)
                .setLimit(WipLimit.vBuilder()
                                  .setValue(limit)
                                  .build())
                .build();
    }
}
