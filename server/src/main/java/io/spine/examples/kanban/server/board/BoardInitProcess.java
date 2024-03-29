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

import io.spine.examples.kanban.BoardId;
import io.spine.examples.kanban.BoardInit;
import io.spine.examples.kanban.command.AddColumn;
import io.spine.examples.kanban.event.BoardCreated;
import io.spine.examples.kanban.event.BoardInitialized;
import io.spine.examples.kanban.event.ColumnAdded;
import io.spine.server.command.Command;
import io.spine.server.event.React;
import io.spine.server.model.Nothing;
import io.spine.server.procman.ProcessManager;
import io.spine.server.tuple.EitherOf2;

/**
 * Creates default columns on a newly created board.
 */
public final class BoardInitProcess
        extends ProcessManager<BoardId, BoardInit, BoardInit.Builder> {

    /**
     * Whenever a new board is created, issue commands for adding default columns.
     */
    @Command
    Iterable<AddColumn> on(BoardCreated e) {
        return DefaultColumns.additionCommands(e.getBoard());
    }

    /**
     * Whenever all default columns are added to the board, terminate the process.
     */
    @React
    EitherOf2<BoardInitialized, Nothing> on(ColumnAdded e) {
        builder().addAddedColumn(e.getColumn());

        if (builder().getAddedColumnCount() == DefaultColumns.count()) {
            setDeleted(true);

            return EitherOf2.withA(
                    BoardInitialized
                            .newBuilder()
                            .setBoard(state().getId())
                            .vBuild()
            );
        }

        return EitherOf2.withB(nothing());
    }
}
