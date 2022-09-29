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

import { ActionContext } from "vuex";
import { RootState } from "@/store/root/types";

/**
 * Abstract base for Vuex actions.
 *
 * @param S
 *         the state of the module where the action belongs to
 * @param P
 *         the payload that is provided to the action
 * @param R
 *         the return type of the action
 */
export default abstract class Action<S, P, R> {
  private readonly actionContext: ActionContext<S, RootState>;
  private readonly payload: P | null;

  /**
   * Creates a new instance of the action with the provided {@link ActionContext}
   * and payload.
   *
   * @param ctx the context of the action that contains reference to the store
   * @param payload the data needed for the action to execute
   * @protected
   */
  protected constructor(ctx: ActionContext<S, RootState>, payload: P | null) {
    this.actionContext = ctx;
    this.payload = payload;
  }

  /**
   * Executes the action.
   * @abstract
   * @protected
   */
  protected abstract execute(): R;

  /**
   * Returns the action's context.
   * @protected
   */
  protected getActionContext(): ActionContext<S, RootState> {
    return this.actionContext;
  }

  /**
   * Returns the action's payload.
   *
   * @return payload or `null` if the action has no payload.
   * @protected
   */
  protected getPayload(): P | null {
    return this.payload;
  }
}
