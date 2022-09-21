<template>
  <div v-if="board" id="columns">
    <KanbanColumn
      v-for="(column, $columnIndex) of board.getColumnList()"
      :key="$columnIndex"
      :column="column"
    />
    <div id="add-column">
      <button v-if="!addColumnMenuOpened" v-on:click="openAddColumnMenu()">
        Add a column
      </button>
      <AddColumnForm
        v-if="addColumnMenuOpened"
        @closed="closeAddColumnMenu()"
      />
    </div>
  </div>
  <div v-else id="add-board">
    <button v-on:click="createBoard()">Add a board</button>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import KanbanColumn from "@/components/KanbanColumn.vue";
import { mapState, mapActions } from "vuex";
import { Action } from "@/store/types";
import AddColumnForm from "@/components/AddColumn.vue";

/**
 * Displays the Kanban board.
 */
export default defineComponent({
  name: "BoardView",
  components: { AddColumnForm, KanbanColumn },
  data() {
    return {
      addColumnMenuOpened: false,
    };
  },
  computed: {
    ...mapState(["board"]),
  },
  methods: {
    ...mapActions([Action.CREATE_BOARD]),
    openAddColumnMenu() {
      this.addColumnMenuOpened = true;
    },
    closeAddColumnMenu() {
      this.addColumnMenuOpened = false;
    },
  },
});
</script>

<style scoped>
#columns {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: flex-start;
  padding-left: 30px;
  overflow-x: auto;
}

#add-board {
  padding-left: 30px;
}

#add-board button,
#add-column button {
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

#add-board button:hover,
#add-column button:hover {
  background-color: #cdd2d4;
  color: #4d4d4d;
}
</style>
