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

import { ActionContext, ActionTree } from "vuex";
import {Action, BoardCreated, ColumnAdded, KanbanState, Mutation} from "@/store/types";
import { client } from "@/dependency/container";
import { AnyPacker } from "spine-web/client/any-packer";
import { Type } from "spine-web/client/typed-message";
import { Event } from "spine-web/proto/spine/core/event_pb";
import { v4 as newUuid } from "uuid";

const actions: ActionTree<KanbanState, any> = {
  [Action.CREATE_BOARD]: (ctx: ActionContext<KanbanState, any>): void => {
    client
      .subscribeToEvent(proto.spine_examples.kanban.BoardCreated)
      .post()
      .then(({ eventEmitted, unsubscribe }) => {
        eventEmitted.subscribe((e: Event) => {
          unsubscribe();
          const innerEvent: BoardCreated = AnyPacker.unpack(e.getMessage()).as(
            Type.forClass(proto.spine_examples.kanban.BoardCreated)
          );
          ctx.commit(Mutation.BOARD_CREATED, innerEvent);
        });
      });

    client
        .subscribeToEvent(proto.spine_examples.kanban.ColumnAdded)
        .post()
        .then(({ eventEmitted }) => {
          eventEmitted.subscribe((e: Event) => {
            const innerEvent: ColumnAdded = AnyPacker.unpack(e.getMessage()).as(
                Type.forClass(proto.spine_examples.kanban.ColumnAdded)
            );
            ctx.commit(Mutation.COLUMN_ADDED, innerEvent);
          });
        });

    const command = new proto.spine_examples.kanban.CreateBoard();
    const board = new proto.spine_examples.kanban.BoardId();
    board.setUuid(newUuid());
    command.setBoard(board);

    client.command(command).post();
  },
};

export default actions;
