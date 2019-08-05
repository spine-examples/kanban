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

import io.spine.core.Subscribe;
import io.spine.examples.kanban.BoardId;
import io.spine.examples.kanban.Card;
import io.spine.examples.kanban.Column;
import io.spine.examples.kanban.event.BoardCreated;
import io.spine.examples.kanban.view.BoardView;
import io.spine.server.projection.Projection;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Builds display information for a board.
 */
public final class BoardProjection extends Projection<BoardId, BoardView, BoardView.Builder> {

    @Subscribe
    void on(BoardCreated e) {
        builder().setId(e.getBoard());
    }

    @Subscribe
    void updated(Column column) {
        // Find index of the column in the board by matching a column id.
        // We can't rely on `equals` method of the column case it was updated.
        int index = indexOf(
                state().getColumnList(),
                (c) -> Objects.equals(c.getId(), column.getId())
        );

        if (index != -1) {
            builder().setColumn(index, column);
        } else {
            builder().addColumn(column);
        }
    }

    @Subscribe
    void updated(Card card) {
        int index = state().getCardList()
                           .indexOf(card);
        if (index != -1) {
            builder().setCard(index, card);
        } else {
            builder().addCard(card);
        }
    }

    /**
     * Finds an index of needed element in the list by testing each element with
     * a specified predicate.
     *
     * @param list
     *         of elements to search in
     * @param predicate
     *         that tests whether a specified element is needed
     * @return an integer index of the found element if it was found or -1 otherwise
     */
    private static <T> int indexOf(List<T> list, Predicate<T> predicate) {
        for (int i = 0; i < list.size(); i++) {
            if (predicate.test(list.get(i))) {
                return i;
            }
        }
        return -1;
    }
}
