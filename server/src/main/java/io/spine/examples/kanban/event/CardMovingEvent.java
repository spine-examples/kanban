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

package io.spine.examples.kanban.event;

import com.google.errorprone.annotations.Immutable;
import io.spine.annotation.GeneratedMixin;
import io.spine.base.EventMessage;
import io.spine.examples.kanban.ColumnId;

import java.util.Set;
import java.util.function.Supplier;

import static io.spine.server.route.EventRoute.noTargets;
import static io.spine.server.route.EventRoute.withId;

/**
 * Common interface for events participating in moving a card from column to column.
 */
@Immutable
@GeneratedMixin
public interface CardMovingEvent extends EventMessage {

    @SuppressWarnings("override") // in generated code.
    boolean getMoving();

    ColumnId getColumn();

    /**
     * Obtains a singleton set with the ID of the column in the routing of the event.
     *
     * <p>If the event is not because of {@linkplain #getMoving() moving the card},
     * an empty set is returned.
     *
     * @implNote This method allows to save on unnecessary routing if an event was not caused
     *           because of moving the card.
     */
    default Set<ColumnId> routingTarget(Supplier<ColumnId> supplier) {
        return getMoving()
               ? withId(supplier.get())
               : noTargets();
    }
}
