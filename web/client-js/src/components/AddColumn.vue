<template>
  <div id="add-column-menu">
    <input type="text" placeholder="Enter column's name" v-model="name" />
    <button v-on:click="submitForm()">Add column</button>
    <button v-on:click="closeForm()">Close</button>
  </div>
</template>

<script>
import { defineComponent } from "vue";
import { Action } from "@/store/types";

/**
 * Displays the form to add a column.
 */
export default defineComponent({
  name: "AddColumn",
  data() {
    return {
      name: "",
    };
  },
  methods: {
    submitForm() {
      if (this.name.length > 0) {
        this.$store.dispatch(Action.ADD_COLUMN, this.name);
        this.closeForm();
      }
    },
    closeForm() {
      this.$emit("closed");
    },
  },
});
</script>

<style scoped>
#add-column-menu {
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

#add-column-menu input {
  display: flex;
  justify-content: center;
  font-size: 16px;
  width: 250px;
  padding: 2px;
  margin-left: 2px;
}

#add-column-menu button {
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

#add-column-menu button:hover {
  background-color: #cdd2d4;
  color: #4d4d4d;
}
</style>
