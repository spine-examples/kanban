<!--
  - Copyright 2022, TeamDev. All rights reserved.
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  - http://www.apache.org/licenses/LICENSE-2.0
  -
  - Redistribution and use in source and/or binary forms, with or without
  - modification, must retain the above copyright notice and the following
  - disclaimer.
  -
  - THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  - "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  - LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  - A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  - OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  - SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  - LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  - DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  - THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  - (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  - OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  -->

<template>
  <div class="notification" :class="{ error: isError() }">
    <p>{{ notification.getMessage() }}</p>
    <div class="close-button">
      <button v-on:click="this.close()">
        <span aria-hidden="true">&times;</span>
      </button>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { Notification } from "@/store/notifications/types/notification";
import { Mutation } from "@/store/notifications/types/mutations";
import { createNamespacedHelpers } from "vuex";
import Notifications from "@/store/notifications";
import { Error } from "@/store/notifications/types/error";

const { mapMutations } = createNamespacedHelpers(Notifications.MODULE_NAME);

/**
 * Displays the notification message.
 */
export default defineComponent({
  props: {
    notification: {
      type: Notification,
      required: true,
    },
  },
  methods: {
    ...mapMutations({
      removeNotification: Mutation.REMOVE_NOTIFICATION,
    }),
    isError() {
      return this.notification instanceof Error;
    },
    close() {
      this.removeNotification(this.notification.getId());
    },
  },
});
</script>

<style scoped>
.notification {
  width: 250px;
  margin-bottom: 10px;
  padding: 10px;
  border-radius: 3px;
  background-color: #1378da;
  color: white;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  box-shadow: 1px 0 3px 0 black;
}

.error {
  background-color: #e57373;
  color: white;
}

.close-button {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  right: 0;
  top: 0;
}

.close-button button {
  all: unset;
  display: flex;
  justify-content: center;
  align-items: center;
}

.close-button button:hover {
  color: #4d4d4d;
}
</style>
