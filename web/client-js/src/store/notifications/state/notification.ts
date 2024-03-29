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

import { NotificationId } from "@/store/notifications/state/notification-id";

/**
 * A generic notification to be displayed in the notifications center.
 *
 * Specific notification types should be implemented as subclasses.
 */
export class Notification {
  /**
   * The notification's ID.
   * @private
   */
  private readonly id: NotificationId;

  /**
   * The notification's message.
   * @private
   */
  private readonly message: string;

  public constructor(id: NotificationId, message: string) {
    this.id = id;
    this.message = message;
  }

  /**
   * Returns the notification's ID.
   */
  public getId(): NotificationId {
    return this.id;
  }

  /**
   * Returns the notification's message.
   */
  public getMessage(): string {
    return this.message;
  }

  /**
   * Creates a generic notification with the provided message.
   */
  public static of(message: string): Notification {
    return new Notification(NotificationId.generate(), message);
  }
}
