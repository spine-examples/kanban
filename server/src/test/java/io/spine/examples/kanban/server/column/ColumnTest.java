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

package io.spine.examples.kanban.server.column;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.examples.kanban.Card;
import io.spine.examples.kanban.CardId;
import io.spine.examples.kanban.ColumnId;
import io.spine.examples.kanban.WipLimit;
import io.spine.examples.kanban.command.AddCardToColumn;
import io.spine.examples.kanban.event.WipLimitChanged;
import io.spine.examples.kanban.event.WipLimitRemoved;
import io.spine.examples.kanban.event.WipLimitSet;
import io.spine.examples.kanban.rejection.Rejections.WipLimitAlreadySet;
import io.spine.examples.kanban.rejection.Rejections.WipLimitExceeded;
import io.spine.examples.kanban.server.KanbanContextTest;
import io.spine.examples.kanban.server.given.WipLimits;
import io.spine.testing.server.EventSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Tests.repeat;

@DisplayName("`Column` should")
class ColumnTest extends KanbanContextTest {

    @BeforeEach
    void setupColumn() {
        context().receivesCommand(addColumn());
    }

    @Nested
    class SettingWipLimit {
        private final WipLimit limit = WipLimits.of(5);

        @BeforeEach
        void setupLimit() {
            context().receivesCommand(setWipLimit(column(), limit));
        }

        @Test
        @DisplayName("set a non-zero WIP limit to previously unlimited column")
        void setLimit() {
            EventSubject assertEvents = assertEvents(WipLimitSet.class);
            assertEvents.hasSize(1);

            WipLimitSet expected =
                    WipLimitSet.newBuilder()
                               .setColumn(column())
                               .setLimit(limit)
                               .vBuild();
            assertEvents.message(0)
                        .isEqualTo(expected);
        }

        @Test
        @DisplayName("change the WIP limit")
        void changeLimit() {
            WipLimit newLimit = WipLimits.of(6);
            context().receivesCommand(setWipLimit(column(), newLimit));

            EventSubject assertEvents = assertEvents(WipLimitChanged.class);
            assertEvents.hasSize(1);

            WipLimitChanged expected =
                    WipLimitChanged.newBuilder()
                                   .setColumn(column())
                                   .setPreviousValue(limit)
                                   .setNewValue(newLimit)
                                   .vBuild();
            assertEvents.message(0)
                        .isEqualTo(expected);
        }

        @Test
        @DisplayName("remove the WIP limit")
        void clearLimit() {
            context().receivesCommand(setWipLimit(column(), WipLimits.of(0)));

            EventSubject assertEvents = assertEvents(WipLimitRemoved.class);
            assertEvents.hasSize(1);

            WipLimitRemoved expected =
                    WipLimitRemoved.newBuilder()
                                   .setColumn(column())
                                   .setPreviousLimit(limit)
                                   .vBuild();
            assertEvents.message(0)
                        .isEqualTo(expected);
        }

        @Test
        @DisplayName("reject setting the WIP limit to the same value")
        void rejectSameValue() {
            context().receivesCommand(setWipLimit(column(), limit));

            EventSubject assertEvents = assertEvents(WipLimitAlreadySet.class);
            assertEvents.hasSize(1);

            WipLimitAlreadySet expected =
                    WipLimitAlreadySet.newBuilder()
                                      .setColumn(column())
                                      .setLimit(limit)
                                      .vBuild();
            assertEvents.message(0)
                        .isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("guard the WIP limit by")
    class GuardingWipLimit {

        private final WipLimit limit = WipLimits.of(5);
        private ColumnId columnWithLimit;

        @BeforeEach
        void initColumn() {
            columnWithLimit = ColumnId.generate();
            context().receivesCommand(addColumn(columnWithLimit))
                     .receivesCommand(setWipLimit(columnWithLimit, limit));
        }

        private void fillUpToTheLimit() {
            repeat(limit.getValue(), this::addCard);
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
            repeat(limit.getValue(), () -> {
                CardId newCard = addCard();
                context().assertEntityWithState(newCard, Card.class)
                         .exists();
            });
        }

        @Test
        @DisplayName("rejecting card addition when the limit reached")
        void rejection() {
            fillUpToTheLimit();
            CardId rejectedCard = addCard();

            EventSubject assertRejections = assertEvents(WipLimitExceeded.class);
            assertRejections.hasSize(1);

            WipLimitExceeded expected =
                    WipLimitExceeded.newBuilder()
                                    .setColumn(columnWithLimit)
                                    .setCard(rejectedCard)
                                    .setLimit(limit)
                                    .vBuild();
            assertRejections.message(0)
                            .isEqualTo(expected);
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
