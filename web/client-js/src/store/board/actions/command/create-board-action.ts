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
import { newBoardId } from "@/store/board/id-factory";
import { client } from "@/dependency/container";
import { MutationType } from "@/store/board/mutations";
import { Event } from "spine-web/proto/spine/core/event_pb";
import {
  Board,
  BoardCreated,
  BoardId,
  ColumnAdded,
} from "@/store/board/aliases";
import { AnyPacker } from "spine-web/client/any-packer";
import { Type } from "spine-web/client/typed-message";
import { Filters } from "spine-web";
import { ActionContext, ActionHandler } from "vuex";
import { RootState } from "@/store/root/root-state";
import { BoardState } from "@/store/board/state/board-state";
import { ActionType } from "@/store/board/actions";

type CreateBoard = proto.spine_examples.kanban.CreateBoard;

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
    client.command(command).post();
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
          const innerEvent: BoardCreated = this.unpackBoardCreated(e);
          const board = this.extractBoard(innerEvent);
          this.subscribeToColumnAdded(board);
          this.addBoardToState(board);
        });
      });
  }

  /**
   * Unpacks the {@link BoardCreated} event from the {@link Event}.
   * @private
   */
  private unpackBoardCreated(e: Event) {
    return AnyPacker.unpack(e.getMessage()).as(
      Type.forClass(proto.spine_examples.kanban.BoardCreated)
    );
  }

  /**
   * Extracts the {@link Board} from the {@link BoardCreated} event.
   * @private
   */
  private extractBoard(e: BoardCreated): Board {
    const board = new proto.spine_examples.kanban.BoardView();
    board.setId(e.getBoard());
    return board;
  }

  /**
   * Dispatches action to subscribe for {@link ColumnAdded} produced by
   * the board with the provided ID.
   * @private
   */
  private subscribeToColumnAdded(b: Board): void {
    this.getActionContext().dispatch(
      ActionType.Subscription.SUBSCRIBE_TO_COLUMN_ADDED,
      b.getId()
    );
  }

  private addBoardToState(b: Board): void {
    this.getActionContext().commit(MutationType.SET_BOARD, b);
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
