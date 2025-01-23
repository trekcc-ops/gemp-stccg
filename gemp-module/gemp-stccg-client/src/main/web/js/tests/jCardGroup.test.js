import {describe, beforeEach, expect, test, jest} from '@jest/globals'
import {VerticalBarGroup, NormalCardGroup, PlayPileCardGroup, NormalGameCardGroup, TableCardGroup, MissionCardGroup, layoutCardElem, layoutTokens} from "../gemp-022/jCardGroup.js";
import CardGroup from "../gemp-022/jCardGroup.js";

describe('validity', () => {
    test('jCardGroup.js is valid syntax', () => {
        // noop
    });
});

describe('CardGroup', () => {
    // BUG: This almost has to be incorrect - the code probably wants empty string if undefined.
    test('CardGroup constructor gives undefined when id is undefined', () => {
        document.body.innerHTML = `
            <div id='container'/>
        `;
        let jqContainer = $('#container');
        
        // So apparently jQuery hands me all the newlines and whitespace, so that's fun.
        let expected = `\n        <div id="undefined" class="ui-widget-content card-group"></div>`;
        let belongTestFunc = jest.fn();

        let _groupUnderTest = new CardGroup(jqContainer, belongTestFunc);
        expect(belongTestFunc.mock.calls.length).toEqual(0); // assert not called
        expect(jqContainer.html()).toBe(expected);
    });

    // BUG: This almost has to be incorrect - the code probably wants the string as ID if provided.
    test('CardGroup constructor gives an empty ID when handed a string', () => {
        document.body.innerHTML = `
            <div id='container'/>
        `;
        let jqContainer = $('#container');
        let thestring = "thestring";
        
        // So apparently jQuery hands me all the newlines and whitespace, so that's fun.
        let expected = `\n        <div id="" class="ui-widget-content card-group"></div>`;
        let belongTestFunc = jest.fn();

        let _groupUnderTest = new CardGroup(jqContainer, belongTestFunc, thestring);
        expect(belongTestFunc.mock.calls.length).toEqual(0); // assert not called
        expect(jqContainer.html()).toBe(expected);
    });
});