<template>
  <div v-if="board" id="columns">
    <KanbanColumn
      v-for="(column, $columnIndex) of board.getColumnList()"
      :key="$columnIndex"
      :column="column"
    />
    <div id="add-column">
      <button v-if="!addColumnFormOpened" v-on:click="openAddColumnForm()">
        Add a column
      </button>
      <AddColumnForm
        v-if="addColumnFormOpened"
        @closed="closeAddColumnForm()"
      />
    </div>
  </div>
  <div v-else id="add-board">
    <button v-on:click="createBoard()">Add a board</button>
  </div>
</template>

<script lang="ts">
import { createNamespacedHelpers } from "vuex";
import Board from "@/store/board";
import { defineComponent } from "vue";
import AddColumnForm from "@/components/board/AddColumnForm.vue";
import KanbanColumn from "@/components/board/KanbanColumn.vue";
import { Action } from "@/store/board/types";

const { mapState, mapActions } = createNamespacedHelpers(Board.MODULE_NAME);

/**
 * Displays the Kanban board.
 */
export default defineComponent({
  name: "BoardView",
  components: { AddColumnForm, KanbanColumn },
  data() {
    return {
      addColumnFormOpened: false,
    };
  },
  computed: {
    ...mapState(["board"]),
  },
  methods: {
    ...mapActions([Action.CREATE_BOARD]),
    openAddColumnForm() {
      this.addColumnFormOpened = true;
    },
    closeAddColumnForm() {
      this.addColumnFormOpened = false;
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
