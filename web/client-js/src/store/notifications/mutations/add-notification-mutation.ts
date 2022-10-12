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
import { NotificationsCenterState } from "@/store/notifications/state/notification-center-state";
import { Notification } from "@/store/notifications/state/notification";

/**
 * Adds a notification to the notification center state.
 *
 * The notification is removed after it expires.
 */
export class AddNotificationMutation {
  /**
   * Default time to live for a notification in milliseconds.
   * @private
   */
  private static readonly DEFAULT_NOTIFICATION_TTL = 5000;

  /**
   * Creates the mutation handler to be used by the store.
   *
   * Adds the notification to the state of the notifications center.
   * The notification is removed from the state after it expires.
   */
  public static newHandler(): Mutation<NotificationsCenterState> {
    return (s: NotificationsCenterState, n: Notification): void => {
      s.notifications.add(n);
      setTimeout(() => {
        s.notifications.remove(n.getId());
      }, this.DEFAULT_NOTIFICATION_TTL);
    };
  }
}
