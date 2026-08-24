import * as THREE from "three";

export function createGraph(graphData) {
  const group = new THREE.Group();

  const nodeObjects = new Map();

  // Create nodes
  graphData.nodes.forEach((node, index) => {
    const geometry = new THREE.SphereGeometry(0.8, 32, 32);

    const material = new THREE.MeshStandardMaterial({
      color: node.isGcRoot ? 0xff6b6b : 0x4dabf7,
    });

    const mesh = new THREE.Mesh(geometry, material);

    // Temporary positions.
    // The force-directed layout will replace these later.
    const angle = (index / graphData.nodes.length) * Math.PI * 2;

    mesh.position.set(
      Math.cos(angle) * 5,
      Math.sin(angle) * 5,
      0
    );

    // Keep the backend node information attached to the Three.js object.
    mesh.userData = {
      id: node.id,
      className: node.className,
      retainedSize: node.retainedSize,
      isGcRoot: node.isGcRoot,
    };

    group.add(mesh);
    nodeObjects.set(node.id, mesh);
  });

  // Create edges
  graphData.edges.forEach((edge) => {
    const source = nodeObjects.get(edge.source);
    const target = nodeObjects.get(edge.target);

    if (!source || !target) {
      return;
    }

    const points = [
      source.position,
      target.position,
    ];

    const geometry = new THREE.BufferGeometry().setFromPoints(points);

    const material = new THREE.LineBasicMaterial({
      color: 0x888888,
    });

    const line = new THREE.Line(geometry, material);

    group.add(line);
  });

  return {
    group,
    nodeObjects,
  };
}