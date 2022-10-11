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

import { newBoardId } from "@/store/board/id-factory";
import { BoardAction } from "@/store/board/actions/base/board-action";
import { client } from "@/dependency/container";
import { ActionContext, ActionHandler } from "vuex";
import {
  BoardCreated,
  BoardState,
  ColumnAdded,
  Mutation,
} from "@/store/board/types";
import { RootState } from "@/store/root/types";
import { Event } from "spine-web/proto/spine/core/event_pb";
import { AnyPacker } from "spine-web/client/any-packer";
import { Type } from "spine-web/client/typed-message";
import { Filters } from "spine-web";

type CreateBoard = proto.spine_examples.kanban.CreateBoard;
type BoardId = proto.spine_examples.kanban.BoardId;

/**
 * Creates a board.
 */
export default class CreateBoardAction extends BoardAction<null, void> {
  /**
   * Sends a command to create a board and subscribes to the {@link BoardCreated} and
   * {@link ColumnAdded} events.
   *
   * @protected
   */
  protected execute(): void {
    const command = this.command();
    this.subscribeToBoardCreated(command.getBoard()!);
    this.subscribeToColumnAdded(command.getBoard()!);
    client.command(command).post();
  }

  /**
   * Subscribes to {@link BoardCreated} events.
   *
   * The subscription is deleted after the first {@link BoardCreated} event arrived,
   * as a board can be created only once during a session.
   * @private
   */
  private subscribeToBoardCreated(board: BoardId): void {
    client
      .subscribeToEvent(proto.spine_examples.kanban.BoardCreated)
      .where(Filters.eq("board", board))
      .post()
      .then(({ eventEmitted, unsubscribe }) => {
        eventEmitted.subscribe((e: Event) => {
          unsubscribe();
          const innerEvent: BoardCreated = AnyPacker.unpack(e.getMessage()).as(
            Type.forClass(proto.spine_examples.kanban.BoardCreated)
          );
          this.getActionContext().commit(Mutation.BOARD_CREATED, innerEvent);
        });
      });
  }

  /**
   * Subscribes to {@link ColumnAdded} events.
   * @private
   */
  private subscribeToColumnAdded(board: BoardId): void {
    client
      .subscribeToEvent(proto.spine_examples.kanban.ColumnAdded)
      .where(Filters.eq("board", board))
      .post()
      .then(({ eventEmitted }) => {
        eventEmitted.subscribe((e: Event) => {
          const innerEvent: BoardCreated = AnyPacker.unpack(e.getMessage()).as(
            Type.forClass(proto.spine_examples.kanban.ColumnAdded)
          );
          this.getActionContext().commit(Mutation.COLUMN_ADDED, innerEvent);
        });
      });
  }

  /**
   * Assembles the {@link CreateBoard} command.
   * @private
   */
  private command(): CreateBoard {
    const c = new proto.spine_examples.kanban.CreateBoard();
    c.setBoard(newBoardId());
    return c;
  }

  /**
   * Creates the {@link ActionHandler} to be used by the store.
   */
  public static newHandler(): ActionHandler<BoardState, RootState> {
    return (ctx: ActionContext<BoardState, RootState>, p: null): void => {
      new CreateBoardAction(ctx, p).execute();
    };
  }
}
