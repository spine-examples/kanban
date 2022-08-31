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
import io.spine.examples.kanban.event.ColumnCreated;
import io.spine.examples.kanban.event.ColumnPlaced;
import io.spine.examples.kanban.rejection.ColumnNameMustBeUnique;
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

        ColumnPosition desiredPosition = c.getDesiredPosition();
        if(!c.hasDesiredPosition()) {
            int columnCount = state().getColumnCount();
            desiredPosition =
                    ColumnPosition
                            .newBuilder()
                            .setIndex(columnCount)
                            .setOfTotal(columnCount)
                            .build();
        }

        return ColumnAdditionRequested
                .newBuilder()
                .setColumn(c.getColumn())
                .setBoard(c.getBoard())
                .setName(c.getName())
                .setDesiredPosition(desiredPosition)
                .vBuild();
    }

    private boolean columnNameIsTaken(String name) {
        return state()
                .getColumnNamesList()
                .stream()
                .anyMatch(entry -> entry.getName()
                                        .equals(name));
    }

    @Apply
    private void event(ColumnCreated e) {
        builder().addColumnNames(Board.ColumnNamesMapEntry
                                         .newBuilder()
                                         .setColumn(e.getColumn())
                                         .setName(e.getName())
                                         .build());
    }

    @Assign
    ColumnPlaced handle(PlaceColumn c) {
        return ColumnPlaced
                .newBuilder()
                .setBoard(c.getBoard())
                .setColumn(c.getColumn())
                .setPosition(c.getPosition())
                .vBuild();
    }

    @Apply
    private void event(ColumnPlaced e) {
        builder().addColumn(e.getPosition().getIndex(), e.getColumn());
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
}
