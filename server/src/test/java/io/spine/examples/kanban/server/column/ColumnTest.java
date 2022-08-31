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

package io.spine.examples.kanban.server.column;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.examples.kanban.Card;
import io.spine.examples.kanban.CardId;
import io.spine.examples.kanban.Column;
import io.spine.examples.kanban.ColumnId;
import io.spine.examples.kanban.command.AddCardToColumn;
import io.spine.examples.kanban.event.ColumnCreated;
import io.spine.examples.kanban.event.WipLimitChanged;
import io.spine.examples.kanban.event.WipLimitRemoved;
import io.spine.examples.kanban.event.WipLimitSet;
import io.spine.examples.kanban.rejection.Rejections.WipLimitAlreadySet;
import io.spine.examples.kanban.rejection.Rejections.WipLimitExceeded;
import io.spine.examples.kanban.server.KanbanContextTest;
import io.spine.testing.server.EventSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Tests.repeat;

@DisplayName("Column logic should")
class ColumnTest extends KanbanContextTest {

    @BeforeEach
    void setupColumn() {
        context().receivesCommand(addColumn());
    }

    @Nested
    @DisplayName("support WIP limit")
    class SettingWipLimit {

        @Test
        @DisplayName("setting non-zero value to previously unlimited column")
        void setLimit() {
            int limit = 3;
            context().receivesCommand(setWipLimit(column(), limit));

            EventSubject assertEvents =
                    context().assertEvents()
                             .withType(WipLimitSet.class);

            assertEvents.hasSize(1);
            WipLimitSet expected = WipLimitSet
                    .newBuilder()
                    .setColumn(column())
                    .setLimit(wipLimit(limit))
                    .vBuild();
            assertEvents.message(0)
                        .isEqualTo(expected);
        }

        @Test
        @DisplayName("changing limit value")
        void changeLimit() {
            ColumnId column = column();
            int initialLimit = 5;
            int updatedLimit = 3;

            context().receivesCommand(setWipLimit(column, initialLimit))
                     .receivesCommand(setWipLimit(column, updatedLimit));

            EventSubject assertEvents =
                    context().assertEvents()
                             .withType(WipLimitChanged.class);

            assertEvents.hasSize(1);
            WipLimitChanged expected = WipLimitChanged
                    .newBuilder()
                    .setColumn(column)
                    .setPreviousValue(wipLimit(initialLimit))
                    .setNewValue(wipLimit(updatedLimit))
                    .vBuild();
            assertEvents.message(0)
                        .isEqualTo(expected);
        }

        @Test
        @DisplayName("clearing limit value")
        void clearLimit() {
            ColumnId column = column();
            int initialLimit = 7;
            context().receivesCommand(setWipLimit(column, initialLimit))
                     .receivesCommand(setWipLimit(column, 0));

            EventSubject assertEvents =
                    context().assertEvents()
                             .withType(WipLimitRemoved.class);

            assertEvents.hasSize(1);
            WipLimitRemoved expected = WipLimitRemoved
                    .newBuilder()
                    .setColumn(column)
                    .setPreviousLimit(wipLimit(initialLimit))
                    .vBuild();
            assertEvents.message(0)
                        .isEqualTo(expected);
        }

        @Test
        @DisplayName("not allowing the same value")
        void rejectSameValue() {
            ColumnId column = column();
            int limit = 4;
            context().receivesCommand(setWipLimit(column, limit))
                     .receivesCommand(setWipLimit(column, limit));

            EventSubject assertEvents =
                    context().assertEvents()
                             .withType(WipLimitAlreadySet.class);

            assertEvents.hasSize(1);
            WipLimitAlreadySet expected = WipLimitAlreadySet
                    .newBuilder()
                    .setColumn(column)
                    .setLimit(wipLimit(limit))
                    .vBuild();
            assertEvents.message(0)
                        .isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("protect WIP limit")
    class GuardingWipLimit {

        private static final int LIMIT = 5;
        private ColumnId columnWithLimit;

        @BeforeEach
        void initColumn() {
            columnWithLimit = ColumnId.generate();
            context().receivesCommand(addColumn(columnWithLimit))
                     .receivesCommand(setWipLimit(columnWithLimit, LIMIT));
        }

        private void fillUpToTheLimit() {
            repeat(LIMIT, this::addCard);
        }

        @CanIgnoreReturnValue
        private CardId addCard() {
            CardId newCard = CardId.generate();
            context().receivesCommand(createCard(newCard))
                     .receivesCommand(addCardToColumn(newCard));
            return newCard;
        }

        @Test
        @DisplayName("allowing to add cards up to the limit")
        void addCards() {
            repeat(LIMIT, () -> {
                CardId newCard = addCard();
                context().assertEntityWithState(newCard, Card.class)
                         .exists();
            });
        }

        @Test
        @DisplayName("prohibit adding a card when the limit reached")
        void rejection() {
            fillUpToTheLimit();
            CardId cardToBeRejected = addCard();
            EventSubject assertRejections =
                    context().assertEvents()
                             .withType(WipLimitExceeded.class);
            assertRejections.hasSize(1);
            WipLimitExceeded expectedRejection = WipLimitExceeded
                    .newBuilder()
                    .setColumn(columnWithLimit)
                    .setCard(cardToBeRejected)
                    .setLimit(wipLimit(LIMIT))
                    .vBuild();
            assertRejections.message(0)
                            .isEqualTo(expectedRejection);
        }

        private AddCardToColumn addCardToColumn(CardId card) {
            return AddCardToColumn
                    .newBuilder()
                    .setColumn(columnWithLimit)
                    .setCard(card)
                    .vBuild();
        }
    }

}
