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

import { Mutation } from "vuex";
import { BoardState, ColumnAdded } from "@/store/board/types";

/**
 * Mutates the local {@linkplain BoardState board state} in response
 * to the {@link ColumnAdded} event.
 */
export default class ColumnAddedMutation {
  /**
   * Creates the mutation handler to be used by the store.
   *
   * Adds the column extracted from the {@link ColumnAdded} event to the board stored
   * in the {@linkplain BoardState local state}.
   */
  public static newHandler(): Mutation<BoardState> {
    return (s: BoardState, e: ColumnAdded) => {
      const column = new proto.spine_examples.kanban.Column();
      column.setId(e.getColumn());
      column.setBoard(e.getBoard());
      column.setName(e.getName());
      column.setPosition(e.getPosition());
      s.board!.addColumn(column);
    };
  }
}
