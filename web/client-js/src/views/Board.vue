<template>
  <div v-if="board" id="board">
    <KanbanColumn
      v-for="(column, $columnIndex) of board.getColumnList()"
      :key="$columnIndex"
      :column="column"
    />
  </div>
  <div v-if="!board" id="add-board">
    <button v-on:click="this[Action.CREATE_BOARD]()">Add a board</button>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import KanbanColumn from "@/components/KanbanColumn.vue";
import { mapState, mapActions } from "vuex";
import { Action } from "@/store/types";

export default defineComponent({
  name: "KanbanBoard",
  components: { KanbanColumn },
  computed: {
    ...mapState(["board"]),
  },
  methods: {
    ...mapActions([Action.CREATE_BOARD]),
  },
});
</script>

<style scoped>
#board {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: flex-start;
}

#add-board {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 30px;
  width: 200px;
  background-color: #e2e4e6;
  border-radius: 0.1rem;
  margin: 0.5rem;
}

#add-board:hover {
  background-color: #cdd2d4;
  color: #4d4d4d;
}

#add-board button {
  all: unset;
  justify-content: center;
}
</style>
