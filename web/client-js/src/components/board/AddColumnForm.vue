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
  <div id="add-column-form">
    <input type="text" placeholder="Enter column's name" v-model="name" />
    <button v-on:click="submit()">Add column</button>
    <button v-on:click="close()">Close</button>
  </div>
</template>

<script lang="ts">
import { createNamespacedHelpers } from "vuex";
import Board from "@/store/board";
import { defineComponent } from "vue";
import { ActionType } from "@/store/board/actions";
import { AddColumnActionPayload } from "@/store/board/actions/command/add-column-action";

const { mapActions } = createNamespacedHelpers(Board.MODULE_NAME);

/**
 * Displays the form to add a new column.
 */
export default defineComponent({
  name: "AddColumnForm",
  data() {
    return {
      name: ""
    };
  },
  methods: {
    ...mapActions({
      addColumn: ActionType.Command.ADD_COLUMN
    }),
    submit() {
      if (this.name.length > 0) {
        const payload: AddColumnActionPayload = {
          name: this.name
        };
        this.addColumn(payload);
        this.close();
      }
    },
    close() {
      this.$emit("closed");
    }
  }
});
</script>

<style scoped>
#add-column-form {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 250px;
  background-color: #e2e4e6;
  border-radius: 0.1rem;
  margin: 0.5rem;
  padding: 10px;
}

#add-column-form input {
  display: flex;
  justify-content: center;
  font-size: 16px;
  width: 250px;
  padding: 2px;
  margin-left: 2px;
}

#add-column-form button {
  all: unset;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 30px;
  width: 250px;
  background-color: #e2e4e6;
  border-radius: 0.1rem;
  margin: 0.5rem;
}

#add-column-form button:hover {
  background-color: #cdd2d4;
  color: #4d4d4d;
}
</style>
