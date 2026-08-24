import { create } from "zustand";

export const useGraphStore = create((set) => ({
  nodes: [],
  edges: [],
  selectedNode: null,

  setGraph: (nodes, edges) => {
    set({
      nodes,
      edges,
    });
  },

  selectNode: (node) => {
    set({
      selectedNode: node,
    });
  },

  clearSelectedNode: () => {
    set({
      selectedNode: null,
    });
  },
}));