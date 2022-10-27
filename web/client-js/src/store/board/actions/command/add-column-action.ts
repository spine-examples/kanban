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
import { newColumnId } from "@/store/board/id-factory";
import { ColumnId } from "@/store/board/aliases";
import { Filters } from "spine-web";
import { AnyPacker } from "spine-web/client/any-packer";
import { Type } from "spine-web/client/typed-message";
import { Event } from "spine-web/proto/spine/core/event_pb";
import { ErrorNotification } from "@/store/notifications/state/error-notification";
import { addNotification } from "@/store/notifications/common";
import { ActionHandler, ActionContext } from "vuex";
import { BoardState } from "@/store/board/state/board-state";
import { RootState } from "@/store/root/root-state";

/**
 * Payload of the {@link AddColumnAction} action.
 *
 * Contains the name of a new column.
 */
export type AddColumnActionPayload = {
  name: string;
};

type ColumnPosition = proto.spine_examples.kanban.ColumnPosition;
type AddColumn = proto.spine_examples.kanban.AddColumn;
type ColumnNameAlreadyTaken =
  proto.spine_examples.kanban.ColumnNameAlreadyTaken;

/**
 * Adds a column.
 */
export default class AddColumnAction extends BoardAction<
  AddColumnActionPayload,
  void
> {
  /**
   * Sends the command to add a column.
   */
  protected execute(): void {
    const command = this.command();
    this.subscribeToColumnNameIsAlreadyTaken(command.getColumn()!);
    client.command(command).post();
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
   * Subscribes to {@link ColumnNameAlreadyTaken} rejections.
   *
   * When the first rejection arrives the subscription is deleted and
   * an error notification is added to the notification center.
   * @private
   */
  private subscribeToColumnNameIsAlreadyTaken(column: ColumnId): void {
    client
      .subscribeToEvent(proto.spine_examples.kanban.ColumnNameAlreadyTaken)
      .where(Filters.eq("column", column))
      .post()
      .then(({ eventEmitted, unsubscribe }) => {
        eventEmitted.subscribe((e: Event) => {
          unsubscribe();
          const rejection: ColumnNameAlreadyTaken =
            this.unpackColumnNameAlreadyTaken(e);
          const error = ErrorNotification.of(
            `The name "${rejection.getName()}" is already taken`
          );
          addNotification(this.getActionContext(), error);
        });
      });
  }

  /**
   * Unpacks the {@link ColumnNameAlreadyTaken} rejection from the {@link Event}.
   * @private
   */
  private unpackColumnNameAlreadyTaken(e: Event): ColumnNameAlreadyTaken {
    return AnyPacker.unpack(e.getMessage()).as(
      Type.forClass(proto.spine_examples.kanban.ColumnNameAlreadyTaken)
    );
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
