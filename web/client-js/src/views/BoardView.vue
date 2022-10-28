<template>
  <div v-if="board" id="board">
    <div id="columns">
      <div
        v-for="(column, $columnIndex) of board.getColumnList()"
        :key="$columnIndex"
      >
        <div
          @dragenter="swapColumns($columnIndex)"
          @dragleave.prevent
          @dragover.prevent
          @dragend="stopMovingColumn"
          @drop="stopMovingColumn"
          :class="{
            'drop-target': this.isDropTarget($columnIndex),
          }"
          class="column-container"
        >
          <KanbanColumn
            :column="column"
            draggable="true"
            @dragstart="startMovingColumn($event, $columnIndex)"
          />
        </div>
      </div>
    </div>
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
import { ColumnId, ColumnPosition } from "@/store/board/aliases";
import { MutationType } from "@/store/board/mutations";
import { SwapColumnsMutationPayload } from "@/store/board/mutations/swap-columns-mutation";
import { MoveColumnPayload } from "@/store/board/actions/command/move-column-action";

const { mapActions, mapMutations, mapState } = createNamespacedHelpers(
  Board.MODULE_NAME
);

/**
 * Displays the Kanban board.
 */
export default defineComponent({
  name: "BoardView",
  components: { AddColumnForm, KanbanColumn },
  data() {
    return {
      columnMovement: {
        initialIndex: -1,
        currentIndex: -1,
      },
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
      moveColumn: ActionType.Command.MOVE_COLUMN,
    }),
    ...mapMutations({
      swapColumnsMutation: MutationType.SWAP_COLUMNS,
    }),
    startMovingColumn(event: DragEvent, columnIndex: number) {
      event.dataTransfer!.dropEffect = "move";
      event.dataTransfer!.effectAllowed = "move";
      this.columnMovement.initialIndex = columnIndex;
      this.columnMovement.currentIndex = columnIndex;
    },
    swapColumns(columnIndex: number) {
      const payload: SwapColumnsMutationPayload = {
        firstIndex: this.columnMovement.currentIndex,
        secondIndex: columnIndex,
      };
      this.swapColumnsMutation(payload);
      this.columnMovement.currentIndex = columnIndex;
    },
    isDropTarget(index: number): boolean {
      return index == this.columnMovement.currentIndex;
    },
    stopMovingColumn() {
      if (this.hasColumnMoved()) {
        const column: ColumnId = this.board
          .getColumnList()
          [this.columnMovement.currentIndex].getId();
        const from: ColumnPosition = this.positionOf(
          this.toOneBasedIndex(this.columnMovement.initialIndex)
        );
        const to: ColumnPosition = this.positionOf(
          this.toOneBasedIndex(this.columnMovement.currentIndex)
        );
        const payload: MoveColumnPayload = {
          column: column,
          from: from,
          to: to,
        };
        this.moveColumn(payload);
      }
      this.resetColumnMovement();
    },
    toOneBasedIndex(index: number): number {
      return index + 1;
    },
    hasColumnMoved(): boolean {
      return (
        this.columnMovement.initialIndex != this.columnMovement.currentIndex
      );
    },
    positionOf(index: number): ColumnPosition {
      const p: ColumnPosition =
        new proto.spine_examples.kanban.ColumnPosition();
      p.setIndex(index);
      p.setOfTotal(this.board.getColumnList().length);
      return p;
    },
    resetColumnMovement() {
      this.columnMovement.initialIndex = -1;
      this.columnMovement.currentIndex = -1;
    },
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

#columns {
  display: flex;
  flex-direction: row;
}

.column-container {
  height: calc(100vh - 80px);
}

.drop-target {
  opacity: 30%;
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
