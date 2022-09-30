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

import { newColumnId } from "@/store/board/id-factory";
import { BoardAction } from "@/store/board/actions/base/board-action";
import { client } from "@/dependency/container";
import { ActionContext, ActionHandler } from "vuex";
import { BoardState } from "@/store/board/types";
import { RootState } from "@/store/root/types";

/**
 * Payload of the `AddColumnAction` action.
 *
 * <p> Contains the name of a new column.
 */
export type AddColumnActionPayload = {
  name: string;
};

type ColumnPosition = proto.spine_examples.kanban.ColumnPosition;
type AddColumn = proto.spine_examples.kanban.AddColumn;

/**
 * Adds a column.
 */
export default class AddColumnAction extends BoardAction<
  AddColumnActionPayload,
  void
> {
  /**
   * Sends the command to add a column.
   *
   * <p> It is assumed that the subscription to {@link ColumnAdded} events
   * already exists after {@linkplain CreateBoardAction board creation}.
   * @protected
   */
  protected execute(): void {
    client.command(this.command()).post();
  }

  /**
   * Assembles the {@link AddColumn} command using the provided payload and local
   * board state.
   * @private
   */
  private command(): AddColumn {
    const c = new proto.spine_examples.kanban.AddColumn();
    c.setColumn(newColumnId());
    c.setBoard(this.getBoard()!.getId());
    c.setName(this.getPayload()!.name);
    c.setDesiredPosition(this.nextPosition());
    return c;
  }

  /**
   * Assembles a {@link ColumnPosition} representing the position next to
   * existing columns.
   * @private
   */
  private nextPosition(): ColumnPosition {
    const numberOfColumns = this.getBoard()!.getColumnList().length;
    const nextPosition = new proto.spine_examples.kanban.ColumnPosition();
    nextPosition.setIndex(numberOfColumns + 1);
    nextPosition.setOfTotal(numberOfColumns + 1);
    return nextPosition;
  }

  /**
   * Creates the {@link ActionHandler} to be used by the store.
   */
  public static newHandler(): ActionHandler<BoardState, RootState> {
    return (
      ctx: ActionContext<BoardState, RootState>,
      p: AddColumnActionPayload
    ): void => {
      new AddColumnAction(ctx, p).execute();
    };
  }
}
