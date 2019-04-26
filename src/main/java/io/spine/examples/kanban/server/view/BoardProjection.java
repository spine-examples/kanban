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

import com.google.protobuf.Message;
import io.spine.core.Subscribe;
import io.spine.examples.kanban.BoardId;
import io.spine.examples.kanban.Card;
import io.spine.examples.kanban.Column;
import io.spine.examples.kanban.event.BoardCreated;
import io.spine.examples.kanban.view.BoardView;
import io.spine.examples.kanban.view.BoardViewVBuilder;
import io.spine.server.projection.Projection;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builds display information for a board.
 */
final class BoardProjection extends Projection<BoardId, BoardView, BoardViewVBuilder> {


    @Subscribe
    void on(BoardCreated e) {
        builder().setId(e.getBoard());
    }

    @Subscribe
    void updated(Column column) {
        boolean replaced = replace(builder().getColumn(), column, Column::getId);
        if(!replaced) {
            builder().addColumn(column);
        }
    }

    @Subscribe
    void updated(Card card) {
        boolean replaced = replace(builder().getCard(), card, Card::getId);
        if (!replaced) {
            builder().addCard(card);
        }
    }

    /**
     * Replaces an item of the passed list which property obtained by the passed function is
     * equal to that of the passed new item.
     *
     * <p>The common use for this method is to replace an item in a {@code List} which represents
     * a builder of a repeated field:
     *
     * <pre>{@code
     *      replace(builder().getItem(), item, Item::getId);
     * }</pre>
     *
     * @param items
     *         the list in which to replace the item
     * @param item
     *         the new item
     * @param prop
     *         the function to obtain a property from items
     * @param <T>
     *         the type of items
     * @param <P>
     *         the type of the item property
     */
    private static <T extends Message, P>
    boolean replace(List<T> items, T item, Function<T, P> prop) {
        Predicate<T> propsEqual = t -> prop.apply(t).equals(prop.apply(item));
        boolean found = items.stream().anyMatch(propsEqual);
        if (found) {
            items.replaceAll(i -> propsEqual.test(i) ? item : i);
        }
        return found;
    }
}
