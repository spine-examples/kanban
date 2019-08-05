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

package io.spine.examples.kanban.server.view;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.extensions.proto.ProtoSubject;
import com.google.protobuf.Message;
import io.spine.examples.kanban.Card;
import io.spine.examples.kanban.CardId;
import io.spine.examples.kanban.Column;
import io.spine.examples.kanban.ColumnId;
import io.spine.examples.kanban.command.CreateCard;
import io.spine.examples.kanban.command.CreateColumn;
import io.spine.examples.kanban.server.KanbanContextTest;
import io.spine.examples.kanban.view.BoardView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.toList;

@DisplayName("BoardProjection should")
class BoardProjectionTest extends KanbanContextTest {

    private static final ImmutableList<String> INITIAL_COLUMN_NAMES = ImmutableList.of(
            "To Do",
            "In Progress",
            "Review",
            "Done"
    );

    private ImmutableList<ColumnId> columns;
    private ImmutableList<CardId> cards;
    private ProtoSubject<?, Message> entityState;

    @BeforeEach
    void initBoard() {
        context().receivesCommand(createBoard());
        columns = context()
                .commandMessages()
                .stream()
                .filter(c -> c instanceof CreateColumn)
                .map(c -> ((CreateColumn) c).getColumn())
                .collect(toImmutableList());
        cards = context()
                .commandMessages()
                .stream()
                .filter(c -> c instanceof CreateCard)
                .map(c -> ((CreateCard) c).getCard())
                .collect(toImmutableList());
        entityState = assertState();
    }

    @Test
    @DisplayName("has the state with the ID of the board")
    void id() {
        entityState.isInstanceOf(BoardView.class);
        entityState.comparingExpectedFieldsOnly()
                   .isEqualTo(BoardView.newBuilder()
                                       .setId(board())
                                       .build());
    }

    @Test
    @DisplayName("has columns")
    void columns() {
        List<Column> expectedColumns =
                columns.stream()
                       .map(c -> Column.newBuilder()
                                       .setId(c)
                                       .build())
                       .collect(toImmutableList());
        BoardView expected = BoardView
                .newBuilder()
                .setId(board())
                .addAllColumn(expectedColumns)
                .vBuild();

        entityState.comparingExpectedFieldsOnly()
                   .isEqualTo(expected);
    }

    @Test
    @DisplayName("has cards")
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

    @Test
    @DisplayName("has cards in columns")
    void hasCardsInColumns() {

        List<Card> expectedCards = IntStream.range(0, 3)
                                            .mapToObj(i -> {
                                                CardId cardId = CardId.generate();
                                                context().receivesCommand(createCard(cardId));
                                                return Card
                                                        .newBuilder()
                                                        .setBoard(board())
                                                        .setName("Generated card " +
                                                                         cardId.getUuid())
                                                        .setId(cardId)
                                                        .build();
                                            })
                                            .collect(toList());

        List<CardId> cardIds = expectedCards.stream()
                                            .map(Card::getId)
                                            .collect(toList());

        List<Column> expectedColumns = IntStream.range(0, columns.size())
                                                .mapToObj(i -> {
                                                    Column.Builder builder = Column
                                                            .newBuilder()
                                                            .setBoard(board())
                                                            .setName(INITIAL_COLUMN_NAMES.get(i))
                                                            .setId(columns.get(i));
                                                    if (i == 0) {
                                                        builder.addAllCard(cardIds);
                                                    }
                                                    return builder.vBuild();
                                                })
                                                .collect(Collectors.toList());

        BoardView expected = BoardView
                .newBuilder()
                .setId(board())
                .addAllColumn(expectedColumns)
                .addAllCard(expectedCards)
                .vBuild();

        assertState().isEqualTo(expected);
    }

    private ProtoSubject<?, Message> assertState() {
        return context().assertEntity(BoardProjection.class, board())
                        .hasStateThat();
    }
}
