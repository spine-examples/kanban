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
import { client } from "@/dependency/container";
import { Board, BoardId } from "@/store/board/aliases";
import { MutationType } from "@/store/board/mutations";
import { ActionContext, ActionHandler } from "vuex";
import { BoardState } from "@/store/board/state/board-state";
import { RootState } from "@/store/root/root-state";

/**
 * Subscribes to changes of the {@link Board} with the provided ID.
 */
export default class SubscribeToBoardChangesAction extends BoardAction<BoardId, void> {
  /**
   * Creates a subscription for changes of the {@link Board} with the provided ID.
   *
   * When changes arrive the board in the local state is overwritten with the
   * arrived one.
   * @protected
   */
  protected execute(): void {
    client
      .subscribeTo(proto.spine_examples.kanban.BoardView)
      .byId(this.boardId())
      .post()
      .then(({ itemChanged }) => {
        itemChanged.subscribe({
          next: (board: Board) => {
            this.overwriteBoardInState(board);
          }
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
   * Commits the mutation to overwrite the {@link Board} in the state with
   * the provided one.
   * @private
   */
  private overwriteBoardInState(b: Board): void {
    this.getActionContext().commit(MutationType.SET_BOARD, b);
  }

  /**
   * Creates the {@link ActionHandler} to be used by the store.
   */
  public static newHandler(): ActionHandler<BoardState, RootState> {
    return (ctx: ActionContext<BoardState, RootState>, p: BoardId): void => {
      return new SubscribeToBoardChangesAction(ctx, p).execute();
    };
  }
}
