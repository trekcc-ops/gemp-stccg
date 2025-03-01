import {expect, test} from '@jest/globals';
import rand_splash from "../gemp-022/splashes.js";
import { SPLASHES } from "../gemp-022/splashes.js";

test('splashes.js is valid syntax', () => {
    // noop
});

test('rand_splash returns text from the array', () => {
    let result = rand_splash();
    expect(SPLASHES.includes(result)).toBe(true);
});