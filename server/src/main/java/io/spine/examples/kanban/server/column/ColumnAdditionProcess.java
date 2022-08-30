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

import io.spine.examples.kanban.ColumnAddition;
import io.spine.examples.kanban.ColumnId;
import io.spine.examples.kanban.command.CreateColumn;
import io.spine.examples.kanban.command.PlaceColumn;
import io.spine.examples.kanban.event.ColumnAdditionRequested;
import io.spine.examples.kanban.event.ColumnCreated;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Command;
import io.spine.server.procman.ProcessManager;

public final class ColumnAdditionProcess
        extends ProcessManager<ColumnId, ColumnAddition, ColumnAddition.Builder> {

    @Command
    CreateColumn handle(ColumnAdditionRequested e) {
        builder().setColumn(e.getColumn());
        builder().setBoard(e.getBoard());
        builder().setDesiredPosition(e.getDesiredPosition());

        return CreateColumn
               .newBuilder()
               .setBoard(e.getBoard())
               .setColumn(e.getColumn())
               .setName(e.getName())
               .vBuild();
    }


    @Command
    PlaceColumn handle(ColumnCreated e) {
        return PlaceColumn
                .newBuilder()
                .setBoard(builder().getBoard())
                .setColumn(builder().getColumn())
                .setPosition(builder().getDesiredPosition())
                .vBuild();
    }
}