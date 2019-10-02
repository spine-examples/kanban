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

package io.spine.examples.kanban.server.column;

import io.spine.examples.kanban.CardId;
import io.spine.examples.kanban.CardTransition;
import io.spine.examples.kanban.ColumnId;
import io.spine.examples.kanban.command.AddCardToColumn;
import io.spine.examples.kanban.command.MoveCard;
import io.spine.examples.kanban.command.RemoveCardFromColumn;
import io.spine.examples.kanban.event.CardAddedToColumn;
import io.spine.examples.kanban.event.CardMoved;
import io.spine.examples.kanban.event.CardRemovedFromColumn;
import io.spine.server.command.Command;
import io.spine.server.event.React;
import io.spine.server.procman.ProcessManager;

/**
 * Handles transition of a card between columns.
 *
 * <p>A target column may have WIP limit value which would not allow to accept one more card.
 * This Process Manager coordinates work of source and target {@link ColumnAggregate}s maintaining
 * consistency of the transition.
 */
final class MoveCardProcess
        extends ProcessManager<ColumnId, CardTransition, CardTransition.Builder> {

    /**
     * When the user asks to move the card, remember the source and target columns, and
     * send the command to add the card to the destination column.
     */
    @Command
    AddCardToColumn startMoving(MoveCard c) {
        ColumnId targetColumn = c.getTargetColumn();
        ColumnId currentColumn = c.getCurrentColumn();
        CardId card = c.getCard();
        builder().setOriginColumn(currentColumn)
                 .setTargetColumn(targetColumn)
                 .setCard(card);
        return AddCardToColumn
                .newBuilder()
                .setColumn(targetColumn)
                .setCard(card)
                .setMoving(true)
                .vBuild();
    }

    /**
     * Whenever the card is successfully added to the target column,
     * issue the command to remove the card from the origin column.
     */
    @Command
    RemoveCardFromColumn completionPolicy(CardAddedToColumn e) {
        CardTransition t = state();
        return RemoveCardFromColumn
                .newBuilder()
                .setColumn(t.getOriginColumn())
                .setCard(t.getCard())
                .setMoving(true)
                .setNewColumn(t.getTargetColumn())
                .vBuild();
    }

    /**
     * Whenever the card is removed from the previous column, terminate the process.
     */
    @React
    CardMoved terminationPolicy(CardRemovedFromColumn e) {
        setDeleted(true);
        CardTransition t = state();
        return CardMoved
                .newBuilder()
                .setCard(t.getCard())
                .setPrevious(t.getOriginColumn())
                .setCurrent(t.getTargetColumn())
                .vBuild();
    }
}
