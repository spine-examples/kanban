<template>
  <div v-if="board" id="board">
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
</template>

<script lang="ts">
import { createNamespacedHelpers } from "vuex";
import Board from "@/store/board";
import { defineComponent } from "vue";
import AddColumnForm from "@/components/board/AddColumnForm.vue";
import KanbanColumn from "@/components/board/KanbanColumn.vue";
import { ActionType } from "@/store/board/actions";

const { mapActions, mapState } = createNamespacedHelpers(Board.MODULE_NAME);

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
  created() {
    const boardUuid = this.$route.params.id as string;
    const boardId = new proto.spine_examples.kanban.BoardId();
    boardId.setUuid(boardUuid);
    this.fetchBoard(boardId);
    this.subscribeToBoardChanges(boardId);
  },
  computed: {
    ...mapState(["board"]),
  },
  methods: {
    ...mapActions({
      subscribeToBoardChanges:
        ActionType.Subscription.SUBSCRIBE_TO_BOARD_CHANGES,
      fetchBoard: ActionType.Query.FETCH_BOARD,
    }),
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
#board {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: flex-start;
  padding-left: 30px;
  overflow-x: auto;
}

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

#add-column button:hover {
  background-color: #cdd2d4;
  color: #4d4d4d;
}
</style>
