export default class PQueue {
    constructor() {
      return this;
    }
    add(fn) {
      return fn();
    }
    pause() {}
    clear() {}
}