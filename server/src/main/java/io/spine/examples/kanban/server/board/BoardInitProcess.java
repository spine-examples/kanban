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

package io.spine.examples.kanban.server.board;

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.kanban.BoardId;
import io.spine.examples.kanban.BoardInit;
import io.spine.examples.kanban.BoardInit.DefaultColumn;
import io.spine.examples.kanban.ColumnId;
import io.spine.examples.kanban.command.CreateColumn;
import io.spine.examples.kanban.event.BoardCreated;
import io.spine.examples.kanban.event.ColumnCreated;
import io.spine.server.command.Command;
import io.spine.server.procman.ProcessManager;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import static io.spine.examples.kanban.server.board.Defaults.nameFor;

/**
 * Creates default columns on a newly created board.
 */
final class BoardInitProcess extends ProcessManager<BoardId, BoardInit, BoardInit.Builder> {

    private final Steps steps = new Steps();

    /**
     * Whenever a new board is created, issue a command for creating the first default column.
     */
    @Command
    CreateColumn startPolicy(BoardCreated event) {
        BoardId board = event.getBoard();
        DefaultColumn first = Defaults.first();
        builder().setStep(first);
        return createColumn(board, nameFor(first));
    }

    /**
     * Create a column with the passed name at the specified board.
     */
    private static CreateColumn createColumn(BoardId board, String columnName) {
        return CreateColumn
                .newBuilder()
                .setBoard(board)
                .setColumn(ColumnId.generate())
                .setName(columnName)
                .setBoardInit(true)
                .build();
    }

    /**
     * Whenever a column is created and it's not the last column,
     * issue a command for creating next default column.
     * Otherwise, terminate the process.
     */
    @Command
    Optional<CreateColumn> defaultColumnPolicy(ColumnCreated event) {
        builder().addCreatedColumn(event.getColumn());

        if (!steps.hasNext()) {
            setDeleted(true);
            return Optional.empty();
        }

        DefaultColumn next = steps.next();

        BoardId boardId = state().getId();
        return Optional.of(createColumn(boardId, nameFor(next)));
    }

    /**
     * Obtains the number of default columns that are to be created by the process.
     *
     * @apiNote
     */
    @VisibleForTesting
    static int defaultColumnCount() {
        int result = BoardInit.DefaultColumn.values().length - 1;
        return result;
    }

    /**
     * Traversal over default column values.
     */
    private final class Steps implements Iterator<DefaultColumn> {

        @Override
        public boolean hasNext() {
            return internalNext() != null;
        }

        /**
         * Advances the process to the next step and returns its value.
         */
        @Override
        public DefaultColumn next() {
            @Nullable DefaultColumn result = internalNext();
            if (result == null) {
                throw new NoSuchElementException("There is no column after " + current());
            }
            builder().setStep(result);
            return result;
        }

        private @Nullable DefaultColumn internalNext() {
            return DefaultColumn.forNumber(current().getNumber() + 1);
        }

        private DefaultColumn current() {
            return state().getStep();
        }
    }
}
