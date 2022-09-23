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

package io.spine.examples.kanban.server.view;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.extensions.proto.ProtoSubject;
import io.spine.base.CommandMessage;
import io.spine.examples.kanban.Card;
import io.spine.examples.kanban.CardId;
import io.spine.examples.kanban.Column;
import io.spine.examples.kanban.ColumnId;
import io.spine.examples.kanban.command.AddColumn;
import io.spine.examples.kanban.command.CreateCard;
import io.spine.examples.kanban.server.KanbanContextTest;
import io.spine.examples.kanban.view.BoardView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.protobuf.AnyPacker.unpack;

@DisplayName("BoardProjection should")
class BoardProjectionTest extends KanbanContextTest {

    private ImmutableList<ColumnId> columns;
    private ImmutableList<CardId> cards;
    private ProtoSubject entityState;

    @BeforeEach
    void initBoard() {
        context().receivesCommand(createBoard());
        columns = commands(AddColumn.class)
                .map(AddColumn::getColumn)
                .collect(toImmutableList());
        cards = commands(CreateCard.class)
                .map(CreateCard::getCard)
                .collect(toImmutableList());
        entityState = assertState();
    }

    @Test
    @DisplayName("have the state with the ID of the board")
    void id() {
        entityState.isInstanceOf(BoardView.class);
        entityState.comparingExpectedFieldsOnly()
                   .isEqualTo(BoardView.newBuilder()
                                       .setId(board())
                                       .build());
    }

    @Test
    @DisplayName("have columns")
    void columns() {
        List<Column> expectedColumns =
                columns.stream()
                       .map(c -> Column.newBuilder()
                                       .setId(c)
                                       .build())
                       .collect(toImmutableList());

        assertThat(expectedColumns).isNotEmpty();

        BoardView expected = BoardView
                .newBuilder()
                .setId(board())
                .addAllColumn(expectedColumns)
                .vBuild();

        entityState.comparingExpectedFieldsOnly()
                   .isEqualTo(expected);
    }

    @Test
    @DisplayName("have cards")
    void cards() {
        List<Card> expectedCards =
                cards.stream()
                     .map(c -> Card.newBuilder()
                                   .setId(c)
                                   .build())
                     .collect(toImmutableList());
        BoardView expected = BoardView
                .newBuilder()
                .setId(board())
                .addAllCard(expectedCards)
                .vBuild();

        entityState.comparingExpectedFieldsOnly()
                   .isEqualTo(expected);
    }

    private ProtoSubject assertState() {
        return context().assertEntity(board(), BoardProjection.class)
                        .hasStateThat();
    }

    private <T extends CommandMessage> Stream<T> commands(Class<T> commandClass) {
        return context()
                .assertCommands()
                .actual()
                .stream()
                .filter(c -> c.is(commandClass))
                .map(c -> unpack(c.getMessage(), commandClass));
    }
}
