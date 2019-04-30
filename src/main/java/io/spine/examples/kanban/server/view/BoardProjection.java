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
import io.spine.examples.kanban.BoardAware;
import io.spine.examples.kanban.BoardId;
import io.spine.examples.kanban.Card;
import io.spine.examples.kanban.Column;
import io.spine.examples.kanban.event.BoardCreated;
import io.spine.examples.kanban.view.BoardView;
import io.spine.examples.kanban.view.BoardViewVBuilder;
import io.spine.server.projection.Projection;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
        setId(column);
        boolean replaced =
                replace(builder().getColumn(), column, Column::getId, builder()::addColumn);
        if (!replaced) {
            builder().addColumn(column);
        }
    }

    @Subscribe
    void updated(Card card) {
        setId(card);
        boolean replaced = replace(builder().getCard(), card, Card::getId, builder()::addCard);
        if (!replaced) {
            builder().addCard(card);
        }
    }

    /**
     * Ensures that the board ID is set from the passed board element.
     *
     * <p>This method takes care of setting the required state field when the updates to the
     * entities to which the projection is subscribed are dispatched <em>before</em>
     * the {@link #on(BoardCreated) BoardCreated}, which normally would set the ID.
     */
    void setId(BoardAware e) {
        builder().setId(e.getBoard());
    }

    /**
     * Replaces an item of the passed list which property obtained by the passed function is
     * equal to that of the passed new item.
     *
     * @param items
     *         the list in which to replace the item
     * @param item
     *         the new item
     * @param prop
     *         the function to obtain a property from items
     * @param replaceFn
     *         method reference for replacing an item in the list
     * @param <T>
     *         the type of items
     * @param <P>
     *         the type of the item property
     */
    private static <T extends Message, P>
    boolean replace(List<T> items, T item, Function<T, P> prop, BiConsumer<Integer, T> replaceFn) {
        int index = indexOf(items, item, prop);
        if (index != -1) {
            replaceFn.accept(index, item);
            return true;
        }
        return false;
    }

    /**
     * Obtains an index of the item in the list which property obtained through the passed function
     * is equal to the one of the existing item.
     *
     * @return the index of the matching item, or -1 if there is no such item
     */
    private static <T extends Message, P>
    int indexOf(List<T> items, T item, Function<T, P> prop) {
        int i = 0;
        P itemProp = prop.apply(item);
        for (T existing : items) {
            P existingProp = prop.apply(existing);
            if (existingProp.equals(itemProp)) {
                return i;
            }
            ++i;
        }
        return -1;
    }
}
