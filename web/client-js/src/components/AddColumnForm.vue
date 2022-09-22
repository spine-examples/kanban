<template>
  <div id="add-column-form">
    <input type="text" placeholder="Enter column's name" v-model="name" />
    <button v-on:click="submit()">Add column</button>
    <button v-on:click="close()">Close</button>
  </div>
</template>

<script>
import { defineComponent } from "vue";
import { Action } from "@/store/types";

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
    submit() {
      if (this.name.length > 0) {
        this.$store.dispatch(Action.ADD_COLUMN, this.name);
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
