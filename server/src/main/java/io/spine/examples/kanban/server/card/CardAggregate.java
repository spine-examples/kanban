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
import io.spine.examples.kanban.CardId;
import io.spine.examples.kanban.command.CreateCard;
import io.spine.examples.kanban.event.CardCreated;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

/**
 * A Card aggregate handles the logic of creation, picking/joining, and leaving the team
 * of people who work on the card.
 */
final class CardAggregate extends Aggregate<CardId, Card, Card.Builder> {

    /**
     * Handles the command to create a card.
     */
    @Assign
    CardCreated handle(CreateCard c) {
        return CardCreated
                .newBuilder()
                .setCard(c.getCard())
                .setBoard(c.getBoard())
                .setName(c.getName())
                .setDescription(c.getDescription())
                .build();
    }

    @Apply
    private void event(CardCreated e) {
        builder().setBoard(e.getBoard())
                 .setName(e.getName())
                 .setDescription(e.getDescription());
    }
}
