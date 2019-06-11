/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.examples.kanban.server.column;

import io.spine.examples.kanban.Column;
import io.spine.examples.kanban.ColumnId;
import io.spine.examples.kanban.WipLimit;
import io.spine.examples.kanban.command.AddCardToColumn;
import io.spine.examples.kanban.command.CreateColumn;
import io.spine.examples.kanban.command.RemoveCardFromColumn;
import io.spine.examples.kanban.command.SetWipLimit;
import io.spine.examples.kanban.event.CardAddedToColumn;
import io.spine.examples.kanban.event.CardRemovedFromColumn;
import io.spine.examples.kanban.event.CardWaitingPlacement;
import io.spine.examples.kanban.event.ColumnCreated;
import io.spine.examples.kanban.event.WipLimitChanged;
import io.spine.examples.kanban.event.WipLimitRemoved;
import io.spine.examples.kanban.event.WipLimitSet;
import io.spine.examples.kanban.rejection.WipLimitAlreadySet;
import io.spine.examples.kanban.rejection.WipLimitExceeded;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;
import io.spine.server.event.React;
import io.spine.server.tuple.EitherOf3;

/**
 * The Column aggregate is responsible for managing adding and removing its cards, and
 * protecting the {@linkplain WipLimit WIP limit} constraint, if defined.
 */
final class ColumnAggregate extends Aggregate<ColumnId, Column, Column.Builder> {

    @Assign
    ColumnCreated handle(CreateColumn c) {
        return ColumnCreated
                .newBuilder()
                .setBoard(c.getBoard())
                .setColumn(c.getColumn())
                .setName(c.getName())
                .setBoardInit(c.getBoardInit())
                .build();
    }

    @Apply
    private void event(ColumnCreated e) {
        builder().setBoard(e.getBoard())
                 .setName(e.getName());
    }

    /**
     * Whenever a new card is created on a board, it is automatically placed
     * at the bottom of the first column.
     */
    @React
    CardAddedToColumn newCardPolicy(CardWaitingPlacement e) {
        return CardAddedToColumn
                .newBuilder()
                .setColumn(e.getColumn())
                .setCard(e.getCard())
                .build();
    }

    @Apply
    private void event(CardAddedToColumn e) {
        builder().addCard(e.getCard());
    }

    /**
     * Emits the event, guarding the WIP limit set to the column.
     *
     * @throws WipLimitExceeded
     *         if adding the card would exceed the WIP limit set to the column
     */
    @Assign
    CardAddedToColumn handle(AddCardToColumn c) throws WipLimitExceeded {
        WipLimit limit = state().getWipLimit();
        if (limit.isSet() &&
                limit.getValue() == state().getCardCount()) {
            throw WipLimitExceeded
                    .newBuilder()
                    .setColumn(id())
                    .setCard(c.getCard())
                    .setLimit(limit)
                    .build();
        }

        return CardAddedToColumn
                .newBuilder()
                .setColumn(c.getColumn())
                .setCard(c.getCard())
                .setMoving(c.getMoving())
                .build();
    }

    @Assign
    CardRemovedFromColumn handle(RemoveCardFromColumn c) {
        return CardRemovedFromColumn
                .newBuilder()
                .setColumn(c.getColumn())
                .setCard(c.getCard())
                .setMoving(c.getMoving())
                .setNewColumn(c.getNewColumn())
                .build();
    }

    @Apply
    private void event(CardRemovedFromColumn e) {
        int index = state().getCardList()
                           .indexOf(e.getCard());
        builder().getCardBuilderList()
                 .remove(index);
    }

    /**
     * Updates the WIP limit value for a column.
     *
     * @return <ul>
     *         <li>{@code WipLimitSet} — if the column did not have a limit before;
     *         <li>{@code WipLimitChanged} — if a non-zero limit value changes to another
     *         non-zero value;
     *         <li>{@code WipLimitRemoved} — if the column had a limit before this command.
     *         </ul>
     * @throws WipLimitAlreadySet
     *         if the command attempts to change the limit to the value (zero or non-zero)
     *         which is already set in the column
     */
    @Assign
    EitherOf3<WipLimitSet, WipLimitChanged, WipLimitRemoved> handle(SetWipLimit cmd)
            throws WipLimitAlreadySet {
        WipLimit currentLimit = state().getWipLimit();
        WipLimit newLimit = cmd.getLimit();

        if (newLimit.equals(currentLimit)) {
            throw WipLimitAlreadySet
                    .newBuilder()
                    .setColumn(id())
                    .setLimit(currentLimit)
                    .build();
        }
        if (newLimit.isZero()) {
            return EitherOf3.withC(
                    WipLimitRemoved
                            .newBuilder()
                            .setColumn(id())
                            .setPreviousLimit(currentLimit)
                            .build()
            );
        }

        if (!currentLimit.isSet()) {
            return EitherOf3.withA(
                    WipLimitSet
                            .newBuilder()
                            .setColumn(id())
                            .setLimit(newLimit)
                            .build()
            );
        } else {
            return EitherOf3.withB(
                    WipLimitChanged
                            .newBuilder()
                            .setColumn(id())
                            .setNewValue(newLimit)
                            .setPreviousValue(currentLimit)
                            .build()
            );
        }
    }

    @Apply
    private void event(WipLimitSet e) {
        builder().setWipLimit(e.getLimit());
    }

    @Apply
    private void event(WipLimitChanged e) {
        builder().setWipLimit(e.getNewValue());
    }

    @Apply
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void event(WipLimitRemoved e) {
        builder().setWipLimit(WipLimit.getDefaultInstance());
    }
}
