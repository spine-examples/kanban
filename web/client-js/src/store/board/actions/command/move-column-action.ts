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

import { ColumnPosition } from "@/store/board/aliases";
import { ColumnId } from "@/store/board/aliases";
import { BoardAction } from "@/store/board/actions/base/board-action";
import { client } from "@/dependency/container";
import { Filters } from "spine-web";
import { ErrorNotification } from "@/store/notifications/state/error-notification";
import { addNotification } from "@/store/notifications/common";
import { ActionContext, ActionHandler } from "vuex";
import { BoardState } from "@/store/board/state/board-state";
import { RootState } from "@/store/root/root-state";
import { ActionType } from "@/store/board/actions";

type MoveColumn = proto.spine_examples.kanban.MoveColumn;
type ColumnCannotBeMoved = proto.spine_examples.kanban.ColumnCannotBeMoved;

/**
 * Payload of the {@link MoveColumnAction} action.
 */
export type MoveColumnPayload = {
  column: ColumnId;
  from: ColumnPosition;
  to: ColumnPosition;
};

/**
 * Moves a column.
 */
export default class MoveColumnAction extends BoardAction<MoveColumnPayload, void> {
  /**
   * Sends the command to move a column.
   * @protected
   */
  protected execute(): void {
    this.subscribeToColumnCannotBeMoved(this.getPayload()!.column);
    client.command(this.command()).post();
  }

  /**
   * Assembles the {@link MoveColumn} command using the provided payload and local
   * board state.
   * @private
   */
  private command(): MoveColumn {
    const c = new proto.spine_examples.kanban.MoveColumn();
    c.setColumn(this.getPayload()!.column);
    c.setBoard(this.getBoard()!.getId());
    c.setFrom(this.getPayload()!.from);
    c.setTo(this.getPayload()!.to);
    return c;
  }

  /**
   * Subscribes to {@link ColumnCannotBeMoved} rejections.
   *
   * When the first rejection arrives the subscription is deleted and an error
   * notification is added to the notification center.
   * @private
   */
  private subscribeToColumnCannotBeMoved(column: ColumnId): void {
    client
      .subscribeToEvent(proto.spine_examples.kanban.ColumnCannotBeMoved)
      .where(Filters.eq("column", column))
      .post()
      .then(({ eventEmitted, unsubscribe }) => {
        eventEmitted.subscribe(() => {
          unsubscribe();
          const error = ErrorNotification.of(`The column cannot be moved`);
          addNotification(this.getActionContext(), error);
          this.fetchBoard();
        });
      });
  }

  /**
   * Dispatches the action to fetch the board.
   */
  private fetchBoard(): void {
    this.getActionContext().dispatch(ActionType.Query.FETCH_BOARD, this.getBoard()!.getId());
  }

  /**
   * Creates the {@link ActionHandler} to be used by the store.
   */
  public static newHandler(): ActionHandler<BoardState, RootState> {
    return (ctx: ActionContext<BoardState, RootState>, p: MoveColumnPayload): void => {
      new MoveColumnAction(ctx, p).execute();
    };
  }
}
