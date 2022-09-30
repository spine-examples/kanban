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
import { Action } from "@/store/board/types";
import { AddColumnActionPayload } from "@/store/board/actions/add-column-action";

const { mapActions } = createNamespacedHelpers(Board.MODULE_NAME);

/**
 * Displays the form to add a new column.
 */
export default defineComponent({
  name: "AddColumnForm",
  data() {
    return {
      name: "",
    };
  },
  methods: {
    ...mapActions({
      addColumn: Action.ADD_COLUMN,
    }),
    submit() {
      if (this.name.length > 0) {
        const payload: AddColumnActionPayload = {
          name: this.name,
        };
        this.addColumn(payload);
        this.close();
      }
    },
    close() {
      this.$emit("closed");
    },
  },
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
