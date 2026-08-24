import * as THREE from "three";

export function createScene(container) {
  const scene = new THREE.Scene();

  scene.background = new THREE.Color(0x0b1020);

  const camera = new THREE.PerspectiveCamera(
    60,
    container.clientWidth / container.clientHeight,
    0.1,
    2000
  );

  camera.position.set(0, 0, 30);

  const renderer = new THREE.WebGLRenderer({
    antialias: true,
  });

  renderer.setPixelRatio(window.devicePixelRatio);
  renderer.setSize(container.clientWidth, container.clientHeight);

  container.appendChild(renderer.domElement);

  const ambientLight = new THREE.AmbientLight(0xffffff, 1);
  scene.add(ambientLight);

  return {
    scene,
    camera,
    renderer,
  };
}