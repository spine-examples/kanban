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

import com.google.common.collect.ImmutableList;
import io.spine.base.EventMessage;
import io.spine.examples.kanban.Board;
import io.spine.examples.kanban.BoardId;
import io.spine.examples.kanban.ColumnId;
import io.spine.examples.kanban.ColumnPosition;
import io.spine.examples.kanban.command.AddColumn;
import io.spine.examples.kanban.command.CreateBoard;
import io.spine.examples.kanban.command.PlaceColumn;
import io.spine.examples.kanban.event.BoardCreated;
import io.spine.examples.kanban.event.CardCreated;
import io.spine.examples.kanban.event.CardWaitingPlacement;
import io.spine.examples.kanban.event.ColumnAdditionRequested;
import io.spine.examples.kanban.event.ColumnMoved;
import io.spine.examples.kanban.event.ColumnPlaced;
import io.spine.examples.kanban.rejection.ColumnNameMustBeUnique;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;
import io.spine.server.event.React;

/** An aggregate of a Kanban board. */
final class BoardAggregate extends Aggregate<BoardId, Board, Board.Builder> {

    @Assign
    BoardCreated handle(CreateBoard c) {
        return BoardCreated
                .newBuilder()
                .setBoard(c.getBoard())
                .vBuild();
    }

    @Apply
    private void event(BoardCreated e) {
        builder().setId(e.getBoard());
    }

    @Assign
    ColumnAdditionRequested handle(AddColumn c) throws ColumnNameMustBeUnique {
        if (columnNameIsTaken(c.getName())) {
            throw ColumnNameMustBeUnique
                    .newBuilder()
                    .setColumn(c.getColumn())
                    .setName(c.getName())
                    .build();
        }

        return ColumnAdditionRequested
                .newBuilder()
                .setColumn(c.getColumn())
                .setBoard(c.getBoard())
                .setName(c.getName())
                .vBuild();
    }

    private boolean columnNameIsTaken(String name) {
        return state()
                .getColumnNamesList()
                .stream()
                .anyMatch(entry -> entry.getName().equals(name));
    }

    @Apply
    private void event(ColumnAdditionRequested e) {
        builder().addColumnNames(
                Board.ColumnNamesMapEntry
                        .newBuilder()
                        .setColumn(e.getColumn())
                        .setName(e.getName())
                        .vBuild()
        );
    }

    /**
     * Places a column on a board and notifies all existing columns that the total
     * number of columns has changed.
     */
    @Assign
    Iterable<EventMessage> handle(PlaceColumn c) {
        return new ImmutableList.Builder<EventMessage>()
                .add(placeColumn(c))
                .addAll(updateTotals())
                .build();
    }

    private ColumnPlaced placeColumn(PlaceColumn c) {
        int newTotal = state().getColumnCount() + 1;
        return ColumnPlaced
                .newBuilder()
                .setBoard(c.getBoard())
                .setColumn(c.getColumn())
                .setPosition(
                        ColumnPosition.newBuilder()
                                      .setIndex(newTotal)
                                      .setOfTotal(newTotal)
                                      .vBuild()
                )
                .vBuild();
    }

    private ImmutableList<ColumnMoved> updateTotals() {
        int currentTotal = state().getColumnCount();
        int newTotal = currentTotal + 1;
        ImmutableList.Builder<ColumnMoved> columnsMoved = new ImmutableList.Builder<>();

        for (int i = 0; i < currentTotal; i++) {
            ColumnId column = state().getColumn(i);
            ColumnPosition from = ColumnPosition
                    .newBuilder()
                    .setIndex(i + 1)
                    .setOfTotal(currentTotal)
                    .vBuild();
            ColumnPosition to = ColumnPosition
                    .newBuilder()
                    .setIndex(i + 1)
                    .setOfTotal(newTotal)
                    .vBuild();

            columnsMoved.add(
                    ColumnMoved
                            .newBuilder()
                            .setColumn(column)
                            .setFrom(from)
                            .setTo(to)
                            .vBuild()
            );
        }

        return columnsMoved.build();
    }

    @Apply
    private void event(ColumnPlaced e) {
        builder().addColumn(e.getPosition().getZeroBasedIndex(), e.getColumn());
    }

    @Apply
    private void event(ColumnMoved e) {
        builder().removeColumn(e.getFrom().getZeroBasedIndex())
                 .addColumn(e.getTo().getZeroBasedIndex(), e.getColumn());
    }

    /**
     * Whenever a card created, it is placed to the first column of the board.
     *
     * @implNote This board knows its columns. So the board listens to the events
     *           on new card creation, and emits the event with the references to the
     *           created card and the first column on which the card is to be placed.
     */
    @React
    CardWaitingPlacement cardPlacementPolicy(CardCreated event) {
        ColumnId firstColumn = state().getColumn(0);
        return CardWaitingPlacement
                .newBuilder()
                .setCard(event.getCard())
                .setColumn(firstColumn)
                .vBuild();
    }

    @Apply
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void event(CardWaitingPlacement event) {
        // Do nothing on the board. The corresponding column will handle the event.
    }
}
