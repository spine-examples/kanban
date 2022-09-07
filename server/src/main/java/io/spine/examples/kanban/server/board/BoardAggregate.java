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
import io.spine.examples.kanban.event.ColumnMovedOnBoard;
import io.spine.examples.kanban.event.ColumnPlaced;
import io.spine.examples.kanban.rejection.ColumnNameAlreadyTaken;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;
import io.spine.server.event.React;

/**
 * An aggregate of a Kanban board.
 */
final class BoardAggregate extends Aggregate<BoardId, Board, Board.Builder> {

    @Assign
    BoardCreated handle(CreateBoard c) {
        return BoardCreated
                .newBuilder()
                .setBoard(c.getBoard())
                .vBuild();
    }

    @Apply
    private void apply(BoardCreated e) {
        builder().setId(e.getBoard());
    }

    @Assign
    ColumnAdditionRequested handle(AddColumn c) throws ColumnNameAlreadyTaken {
        if (columnNameIsTaken(c.getName())) {
            throw ColumnNameAlreadyTaken
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
                .setDesiredPosition(c.getDesiredPosition())
                .vBuild();
    }

    private boolean columnNameIsTaken(String name) {
        return state()
                .getTakenColumnNamesList()
                .stream()
                .anyMatch(entry -> entry.getName().equals(name));
    }

    @Apply
    private void apply(ColumnAdditionRequested e) {
        builder().addTakenColumnNames(
                Board.TakenColumnName
                        .newBuilder()
                        .setName(e.getName())
                        .setColumn(e.getColumn())
                        .vBuild()
        );
    }

    /**
     * Places a column on the board and notifies all existing columns that the total
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
        ColumnPosition actualPosition =
                ColumnPosition.newBuilder()
                              .setIndex(c.getDesiredPosition().getIndex())
                              .setOfTotal(incrementColumnCount())
                              .vBuild();

        return ColumnPlaced
                .newBuilder()
                .setBoard(c.getBoard())
                .setColumn(c.getColumn())
                .setDesiredPosition(c.getDesiredPosition())
                .setActualPosition(actualPosition)
                .vBuild();
    }

    private int incrementColumnCount() {
        return state().getColumnCount() + 1;
    }

    private ImmutableList<ColumnMovedOnBoard> updateTotals() {
        int currentTotal = state().getColumnCount();
        int newTotal = incrementColumnCount();
        ImmutableList.Builder<ColumnMovedOnBoard> columnsMoved =
                new ImmutableList.Builder<>();

        for (int i = 1; i <= currentTotal; i++) {
            columnsMoved.add(updateTotal(i, currentTotal, newTotal));
        }

        return columnsMoved.build();
    }

    private ColumnMovedOnBoard updateTotal(int index, int currentTotal, int newTotal) {
        ColumnPosition from =
                ColumnPosition.newBuilder()
                              .setIndex(index)
                              .setOfTotal(currentTotal)
                              .vBuild();
        ColumnPosition to =
                ColumnPosition.newBuilder()
                              .setIndex(index)
                              .setOfTotal(newTotal)
                              .vBuild();
        ColumnId column = state().getColumn(from.zeroBasedIndex());

        return ColumnMovedOnBoard
                .newBuilder()
                .setColumn(column)
                .setBoard(state().getId())
                .setFrom(from)
                .setTo(to)
                .vBuild();
    }

    @Apply
    private void apply(ColumnPlaced e) {
        builder().addColumn(e.getActualPosition().zeroBasedIndex(), e.getColumn());
    }

    @Apply
    private void apply(ColumnMovedOnBoard e) {
        builder().removeColumn(e.getFrom().zeroBasedIndex())
                 .addColumn(e.getTo().zeroBasedIndex(), e.getColumn());
    }

    /**
     * Places the created card to the first column of the board.
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
