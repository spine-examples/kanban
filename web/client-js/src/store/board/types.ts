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

/**
 * Aliases for frequently used types.
 */

/**
 * An alias for the {@code proto.spine_examples.kanban.BoardView} projection.
 */
export type Board = proto.spine_examples.kanban.BoardView;

/**
 * An alias for the {@code proto.spine_examples.kanban.BoardCreated} event.
 */
export type BoardCreated = proto.spine_examples.kanban.BoardCreated;

/**
 * An alias for the {@code proto.spine_examples.kanban.ColumnAdded} event.
 */
export type ColumnAdded = proto.spine_examples.kanban.ColumnAdded;

/**
 * The local state of the Kanban board.
 */
export interface BoardState {
  board: Board | null;
}

/**
 * Exposes mutations of the local {@linkplain BoardState board state}.
 */
export const Mutation = {
  /**
   * Adds the board extracted from the {@link BoardCreated} event to the state.
   */
  BOARD_CREATED: "boardCreated",

  /**
   * Adds the column extracted from the {@link ColumnAdded} event to the board stored
   * in the state.
   */
  COLUMN_ADDED: "columnAdded",
};

/**
 * Exposes interactions with the remote board state.
 */
export const Action = {
  /**
   * Subscribes to the {@link BoardCreated} and {@link ColumnAdded} events and sends
   * the {@code proto.spine_examples.kanban.CreateBoard} command to create a board.
   */
  CREATE_BOARD: "createBoard",

  /**
   * Sends the {@code proto.spine_examples.kanban.AddColumn} command to add the column.
   */
  ADD_COLUMN: "addColumn",
};
