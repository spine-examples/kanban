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

package io.spine.examples.kanban.server;

import io.spine.examples.kanban.BoardId;
import io.spine.examples.kanban.CardId;
import io.spine.examples.kanban.ColumnId;
import io.spine.examples.kanban.ColumnPosition;
import io.spine.examples.kanban.WipLimit;
import io.spine.examples.kanban.command.AddColumn;
import io.spine.examples.kanban.command.CreateBoard;
import io.spine.examples.kanban.command.CreateCard;
import io.spine.examples.kanban.command.SetWipLimit;
import io.spine.examples.kanban.server.board.ColumnPositions;
import io.spine.examples.kanban.server.given.TestCommands;
import org.junit.jupiter.api.BeforeEach;

/**
 * Abstract base for test suites working with Kanban Board domain.
 */
public abstract class KanbanTest {

    private BoardId board;
    private ColumnId column;
    private final ColumnPosition defaultPosition = ColumnPositions.of(1, 1);
    private CardId card;

    /**
     * Generates identifiers used by the test suite.
     */
    @BeforeEach
    void generateIdentifiers() {
        board = BoardId.generate();
        column = ColumnId.generate();
        card = CardId.generate();
    }

    protected final BoardId board() {
        return board;
    }

    protected final ColumnId column() {
        return column;
    }

    protected final CardId card() {
        return card;
    }

    protected final ColumnPosition defaultPosition() {
        return defaultPosition;
    }

    protected final CreateBoard createBoard() {
        return TestCommands.createBoard(board);
    }

    protected final AddColumn addColumn() {
        return TestCommands.addColumn(board, column, defaultPosition);
    }

    protected final AddColumn addColumn(ColumnId column) {
        return TestCommands.addColumn(board, column, defaultPosition);
    }

    protected final CreateCard createCard() {
        return TestCommands.createCard(board, card);
    }

    protected final CreateCard createCard(CardId card) {
        return TestCommands.createCard(board, card);
    }
}
