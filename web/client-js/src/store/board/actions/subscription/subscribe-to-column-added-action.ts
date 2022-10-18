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

import { BoardAction } from "@/store/board/actions/base/board-action";
import { BoardId, ColumnAdded } from "@/store/board/aliases";
import { ActionContext, ActionHandler } from "vuex";
import { BoardState } from "@/store/board/state/board-state";
import { RootState } from "@/store/root/root-state";
import { client } from "@/dependency/container";
import { Event } from "spine-web/proto/spine/core/event_pb";
import { AnyPacker } from "spine-web/client/any-packer";
import { Type } from "spine-web/client/typed-message";
import { MutationType } from "@/store/board/mutations";
import { Filters } from "spine-web";

type Column = proto.spine_examples.kanban.Column;

/**
 * Subscribes to {@link ColumnAdded} events produced by the provided board.
 */
export default class SubscribeToColumnAddedAction extends BoardAction<
  BoardId,
  void
> {
  /**
   * Creates a subscription for {@link ColumnAdded} events produced by the board with
   * the provided ID.
   *
   * Extracts columns from arrived events and adds them to the local state.
   * @protected
   */
  protected execute(): void {
    client
      .subscribeToEvent(proto.spine_examples.kanban.ColumnAdded)
      .where(Filters.eq("board", this.boardId()))
      .post()
      .then(({ eventEmitted }) => {
        eventEmitted.subscribe((e: Event) => {
          const innerEvent: ColumnAdded = this.unpackColumnAdded(e);
          const column = this.extractColumn(innerEvent);
          this.addColumnToState(column);
        });
      });
  }

  /**
   * Retrieves the {@link BoardId} from the action payload.
   * @private
   */
  private boardId(): BoardId {
    return this.getPayload()!;
  }

  /**
   * Unpacks the {@link ColumnAdded} event from the {@link Event}.
   * @private
   */
  private unpackColumnAdded(e: Event): ColumnAdded {
    return AnyPacker.unpack(e.getMessage()).as(
      Type.forClass(proto.spine_examples.kanban.ColumnAdded)
    );
  }

  /**
   * Extracts the {@link Column} from the {@link ColumnAdded} event.
   * @private
   */
  private extractColumn(e: ColumnAdded): Column {
    const column = new proto.spine_examples.kanban.Column();
    column.setId(e.getColumn());
    column.setBoard(e.getBoard());
    column.setName(e.getName());
    column.setPosition(e.getPosition());
    return column;
  }

  /**
   * Commits the mutation to add the provided {@link Column} to the state.
   * @private
   */
  private addColumnToState(c: Column): void {
    this.getActionContext().commit(MutationType.ADD_COLUMN, c);
  }

  /**
   * Creates the {@link ActionHandler} to be used by the store.
   */
  public static newHandler(): ActionHandler<BoardState, RootState> {
    return (ctx: ActionContext<BoardState, RootState>, p: BoardId): void => {
      new SubscribeToColumnAddedAction(ctx, p).execute();
    };
  }
}
