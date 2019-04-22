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

package io.spine.examples.kanban.server.card;

import io.spine.examples.kanban.Card;
import io.spine.examples.kanban.event.CardCreated;
import io.spine.examples.kanban.server.KanbanContextTest;
import io.spine.testing.server.EventSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Card logic should")
class CardTest extends KanbanContextTest {

    /**
     * Creates a new board for placing cards.
     */
    @BeforeEach
    void setupBoard() {
        context().receivesCommand(createBoard());
    }

    /**
     * Verifies that a command to create a card generates corresponding event.
     */
    @Nested
    @DisplayName("create new card")
    class Creation {

        @BeforeEach
        void setupCard() {
            context().receivesCommand(createCard());
        }


        @Test
        @DisplayName("generating `CardCreated` event")
        void event() {
            EventSubject assertEvents =
                    context().assertEvents()
                             .withType(CardCreated.class);
            assertEvents.hasSize(1);
            CardCreated expected = CardCreated
                    // We use `newBuider()` instead of `vBuilder()` to be able to omit
                    // the `name` and `description` fields that are `required` in the event.
                    .newBuilder()
                    .setBoard(board())
                    .setCard(card())
                    .build();
            assertEvents.message(0)
                        .ignoringFields(3 /* name */, 4 /* description */)
                        .isEqualTo(expected);
        }

        @Test
        @DisplayName("as entity with the `Card` state")
        void entity() {
            context().assertEntityWithState(Card.class, card())
                     .exists();
        }
    }
}
