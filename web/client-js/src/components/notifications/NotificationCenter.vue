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
  <div v-if="notifications.getItems()" id="notifications">
    <NotificationMessage
      v-for="notification of notifications.getItems()"
      :key="notification.id"
      :notification="notification" />
  </div>
</template>

<script lang="ts">
import { createNamespacedHelpers } from "vuex";
import { defineComponent } from "vue";
import Notifications from "@/store/notifications";
import NotificationMessage from "@/components/notifications/NotificationMessage.vue";

const { mapState } = createNamespacedHelpers(Notifications.MODULE_NAME);

/**
 * Displays notifications.
 */
export default defineComponent({
  components: { NotificationMessage },
  computed: {
    ...mapState({
      notifications: "notifications"
    })
  }
});
</script>

<style scoped>
#notifications {
  position: absolute;
  display: flex;
  flex-direction: column;
  z-index: 1;
  right: 0;
  bottom: 0;
  width: 250px;
  margin: 50px;
  justify-content: center;
  align-items: flex-start;
}
</style>
