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
import io.spine.examples.kanban.command.MoveColumn;
import io.spine.examples.kanban.command.PlaceColumn;
import io.spine.examples.kanban.event.BoardCreated;
import io.spine.examples.kanban.event.CardCreated;
import io.spine.examples.kanban.event.CardWaitingPlacement;
import io.spine.examples.kanban.event.ColumnAdditionRequested;
import io.spine.examples.kanban.event.ColumnMovedOnBoard;
import io.spine.examples.kanban.event.ColumnPlaced;
import io.spine.examples.kanban.rejection.ColumnCannotBeMoved;
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
                .addAll(makeSpaceForNewColumn())
                .build();
    }

    private ColumnPlaced placeColumn(PlaceColumn c) {
        ColumnPosition actualPosition =
                ColumnPositions.of(
                    c.getDesiredPosition().getIndex(),
                    incrementColumnCount()
                );

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

    /**
     * Moves all columns on the board to make space for a new column.
     */
    private ImmutableList<ColumnMovedOnBoard> makeSpaceForNewColumn() {
        int currentTotal = state().getColumnCount();
        int newTotal = incrementColumnCount();
        ImmutableList.Builder<ColumnMovedOnBoard> columnsMoved =
                new ImmutableList.Builder<>();

        for (int i = 1; i <= currentTotal; i++) {
            ColumnPosition from = ColumnPositions.of(i, currentTotal);
            ColumnPosition to = ColumnPositions.of(i, newTotal);
            columnsMoved.add(moveColumn(from, to));
        }

        return columnsMoved.build();
    }

    private ColumnMovedOnBoard moveColumn(ColumnPosition from, ColumnPosition to) {
        ColumnId column = state().getColumn(from.zeroBasedIndex());
        BoardId board = state().getId();
        return ColumnMovedOnBoard
                .newBuilder()
                .setColumn(column)
                .setBoard(board)
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
        int index = state().getColumnList().indexOf(e.getColumn());
        int newIndex = e.getTo().zeroBasedIndex();
        if (index != newIndex) {
            builder().removeColumn(index).addColumn(newIndex, e.getColumn());
        }
    }

    /**
     * Moves the column to the desired position and shifts all columns on the
     * way to fill the void left by the column.
     */
    @Assign
    Iterable<ColumnMovedOnBoard> handle(MoveColumn c) throws ColumnCannotBeMoved {
        return new ImmutableList.Builder<ColumnMovedOnBoard>()
                .add(moveColumn(c))
                .addAll(shiftColumns(c))
                .build();
    }

    private ColumnMovedOnBoard moveColumn(MoveColumn c) throws ColumnCannotBeMoved {
        if (!canBeMoved(c.getColumn(), c.getFrom(), c.getTo())) {
            throw ColumnCannotBeMoved
                    .newBuilder()
                    .setColumn(c.getColumn())
                    .setFrom(c.getFrom())
                    .setTo(c.getTo())
                    .build();
        }

        return ColumnMovedOnBoard
                .newBuilder()
                .setColumn(c.getColumn())
                .setBoard(c.getBoard())
                .setFrom(c.getFrom())
                .setTo(c.getTo())
                .vBuild();
    }

    private boolean canBeMoved(ColumnId column, ColumnPosition from, ColumnPosition to) {
        return from.isValid() && to.isValid() && isTotalActual(from) &&
                isTotalActual(to) && isIndexActual(column, from) && !from.equals(to);
    }

    /**
     * Checks whether the total number of columns is coherent with the state.
     *
     * @return {@code true} if the total number of columns is coherent with the state.
     */
    private boolean isTotalActual(ColumnPosition p) {
        return p.getOfTotal() == state().getColumnCount();
    }

    /**
     * Checks whether the provided column is actually placed at the index from
     * the provided position.
     *
     * @return {@code true} if the column is placed at the index from the provided position.
     */
    private boolean isIndexActual(ColumnId c, ColumnPosition p) {
        return p.zeroBasedIndex() == state().getColumnList().indexOf(c);
    }

    /**
     * Shifts columns to fill the void left by the moving column.
     *
     * <p> The shift direction is based on the movement direction. If the column is
     * moving right, then the columns on the way are shifted left and vice versa.
     */
    private ImmutableList<ColumnMovedOnBoard> shiftColumns(MoveColumn c) {
        if (isColumnMovingRight(c.getFrom(), c.getTo())) {
            ColumnPosition rightToFrom = rightTo(c.getFrom());
            return shiftColumnsLeft(rightToFrom, c.getTo());
        } else {
            ColumnPosition leftToFrom = leftTo(c.getFrom());
            return shiftColumnsRight(leftToFrom, c.getTo());
        }
    }

    private static boolean isColumnMovingRight(ColumnPosition from, ColumnPosition to) {
        return from.getIndex() < to.getIndex();
    }

    /**
     * Returns the position right to the provided one.
     *
     * <p> The total number of columns remains unchanged.
     */
    private static ColumnPosition rightTo(ColumnPosition p) {
        return ColumnPositions.of(p.getIndex() + 1, p.getOfTotal());
    }

    /**
     * Returns the position left to the provided one.
     *
     * <p> The total number of columns remains unchanged.
     */
    private static ColumnPosition leftTo(ColumnPosition p) {
        return ColumnPositions.of(p.getIndex() - 1, p.getOfTotal());
    }

    private ImmutableList<ColumnMovedOnBoard> shiftColumnsLeft(
            ColumnPosition from,
            ColumnPosition to
    ) {
        ImmutableList.Builder<ColumnMovedOnBoard> movedColumns = new ImmutableList.Builder<>();

        ColumnPosition current = from;
        while (!current.equals(to)) {
            ColumnPosition newPosition = leftTo(current);
            movedColumns.add(moveColumn(current, newPosition));
            current = rightTo(current);
        }

        return movedColumns.build();
    }

    private ImmutableList<ColumnMovedOnBoard> shiftColumnsRight(
            ColumnPosition from,
            ColumnPosition to
    ) {
        ImmutableList.Builder<ColumnMovedOnBoard> movedColumns = new ImmutableList.Builder<>();

        ColumnPosition current = from;
        while (!current.equals(to)) {
            ColumnPosition newPosition = rightTo(current);
            movedColumns.add(moveColumn(current, newPosition));
            current = leftTo(current);
        }

        return movedColumns.build();
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
