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
  <div class="column">
    <div class="column-title">
      <h3>{{ column.getName() }}</h3>
    </div>
    <hr class="separator" />
    <div class="cards"></div>
    <div class="add-card">
      <button>Add a card</button>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

/**
 * Displays the column's name and cards.
 */
export default defineComponent({
  name: "KanbanColumn",
  props: {
    column: {
      type: proto.spine_examples.kanban.Column,
      required: true,
    },
  },
});
</script>

<style scoped>
.column {
  display: flex;
  flex-direction: column;
  width: 250px;
  background-color: #e2e4e6;
  border-radius: 0.1rem;
  margin: 0.5rem;
  padding: 10px;
}

.column-title {
  padding-left: 5px;
  width: 250px;
  text-align: center;
  margin-bottom: 5px;
}

.separator {
  border: 1px solid black;
}

.cards {
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  min-height: 40px;
  max-height: calc(100vh - 80px);
  border-radius: 4px;
  margin-bottom: 20px;
}

.add-card {
  display: flex;
  justify-content: center;
  height: 30px;
  margin-top: auto;
}

.add-card:hover {
  background-color: #cdd2d4;
  color: #4d4d4d;
}

.add-card button {
  all: unset;
}
</style>
