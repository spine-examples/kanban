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

import { ActionTree } from "vuex";
import { BoardState } from "@/store/board/state/board-state";
import { RootState } from "@/store/root/root-state";
import CreateBoardAction from "@/store/board/actions/command/create-board-action";
import AddColumnAction from "@/store/board/actions/command/add-column-action";
import FetchBoardAction from "@/store/board/actions/query/fetch-board-action";
import SubscribeToColumnAddedAction from "@/store/board/actions/subscription/subscribe-to-column-added-action";

/**
 * Defines action types to interact with the remote board state.
 */
export const ActionType = {
  Query: {
    /**
     * Fetches the {@link Board} with the provided ID.
     */
    FETCH_BOARD: "fetchBoard",
  },
  Command: {
    /**
     * Creates a board.
     */
    CREATE_BOARD: "createBoard",

    /**
     * Adds a column.
     */
    ADD_COLUMN: "addColumn",
  },
  Subscription: {
    /**
     * Subscribes to {@link ColumnAdded} events produced by the provided board.
     */
    SUBSCRIBE_TO_COLUMN_ADDED: "subscribeToColumnAdded",
  },
};

/**
 * Exposes actions to interact with the remote board state.
 */
export const actions: ActionTree<BoardState, RootState> = {
  [ActionType.Query.FETCH_BOARD]: FetchBoardAction.newHandler(),
  [ActionType.Command.CREATE_BOARD]: CreateBoardAction.newHandler(),
  [ActionType.Command.ADD_COLUMN]: AddColumnAction.newHandler(),
  [ActionType.Subscription.SUBSCRIBE_TO_COLUMN_ADDED]:
    SubscribeToColumnAddedAction.newHandler(),
};
