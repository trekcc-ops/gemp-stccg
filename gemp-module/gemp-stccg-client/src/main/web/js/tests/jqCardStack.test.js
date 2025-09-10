import {describe, beforeEach, expect, test, jest} from '@jest/globals'
import {VerticalBarGroup, NormalCardGroup, PlayPileCardGroup, NormalGameCardGroup, TableCardGroup, MissionCardGroup} from "../gemp-022/jCardGroup.js";
import CardGroup from "../gemp-022/jqCardStack.js";
// import * as jCG from '../gemp-022/jqCardStack.js';

describe('validity', () => {
    test('jqCardStack.js is valid syntax', () => {
        // noop
    });
});

describe('CardGroup', () => {
    test('CardGroup constructor an empty ID id is undefined', () => {
        document.body.innerHTML = `
            <div id='container'/>
        `;
        let jqContainer = document.getElementById("container");
        
        let expected = document.createElement("div");
        expected.id = "";
        expected.classList.add("ui-widget-content", "card-group");
        let belongTestFunc = jest.fn();

        let _groupUnderTest = new CardGroup(jqContainer, belongTestFunc);
        expect(belongTestFunc.mock.calls.length).toEqual(0); // assert not called
        expect(jqContainer.children[0]).toEqual(expected);
    });

    test('CardGroup constructor uses a string as the ID when handed a string', () => {
        document.body.innerHTML = `
            <div id='container'/>
        `;
        let jqContainer = document.getElementById("container");
        let thestring = "thestring";
        
        let expected = document.createElement("div");
        expected.id = "thestring";
        expected.classList.add("ui-widget-content", "card-group");
        let belongTestFunc = jest.fn();

        let _groupUnderTest = new CardGroup(jqContainer, belongTestFunc, thestring);
        expect(belongTestFunc.mock.calls.length).toEqual(0); // assert not called
        expect(jqContainer.children[0]).toEqual(expected);
    });

    test('CardGroup cardBelongs can identify cards in the group', () => {
        document.body.innerHTML = `
            <div id='container'/>
        `;
        let container = document.getElementById("container");

        // Because the card group isn't actually an array containing anything, it has
        // to be told how to determine if a card is in its group. These are things like
        // PlayPileCardGroup (a subclass) being told to check against card.zone == "PLAY_PILE".
        // STUB, ignored
        let belongTestFunc = jest.fn();

        let _groupUnderTest = new CardGroup(container, belongTestFunc, "Tanagra");
        expect(document.getElementById("Tanagra")).not.toEqual(null);

        // manually add cards without using the addCard instance function
        let tanagra = document.getElementById("Tanagra");

        let darmok = document.createElement("div");
        darmok.id = "Darmok";
        darmok.classList.add("card");

        let jalad = document.createElement("div");
        jalad.id = "Jalad";
        jalad.classList.add("card");

        tanagra.append(darmok, jalad);
        expect(tanagra.children.length).toEqual(2);

        // put picard outside the group
        let picard = document.createElement("div");
        picard.id = "Picard";
        picard.classList.add("notacard");

        container.append(picard);

        expect(_groupUnderTest.cardBelongs(document.getElementById("Darmok"))).toEqual(true);
        expect(_groupUnderTest.cardBelongs(document.getElementById("Jalad"))).toEqual(true);
        expect(_groupUnderTest.cardBelongs(document.getElementById("Picard"))).toEqual(false);

        // Asset we never actually called the belongTestFunc
        expect(belongTestFunc.mock.calls.length).toEqual(0);
    });

    test('CardGroup getCardElems returns only elems with the card class', () => {
        document.body.innerHTML = `
            <div id='container'/>
        `;
        let container = document.getElementById("container");

        // Because the card group isn't actually an array containing anything, it has
        // to be told how to determine if a card is in its group. These are things like
        // PlayPileCardGroup (a subclass) being told to check against card.zone == "PLAY_PILE"
        // where "card" is the JSON data stored in JQ.data("card": {})
        // STUB, ignored
        let belongTestFunc = jest.fn();

        let _groupUnderTest = new CardGroup(container, belongTestFunc, "Tanagra");
        expect(document.getElementById("Tanagra")).not.toEqual(null);

        // manually add cards without using the addCard instance function
        let tanagra = document.getElementById("Tanagra");

        let darmok = document.createElement("div");
        darmok.id = "Darmok";
        darmok.classList.add("card");

        let jalad = document.createElement("div");
        jalad.id = "Jalad";
        jalad.classList.add("card");

        tanagra.append(darmok, jalad);
        expect(tanagra.children.length).toEqual(2);

        // put picard in the group but with the notacard class
        let picard = document.createElement("div");
        picard.id = "Picard";
        picard.classList.add("notacard");

        tanagra.append(picard);

        let expected_result = [darmok, jalad];

        expect(_groupUnderTest.groupDiv.children.length).toEqual(3);
        expect(_groupUnderTest.getCardElems()).toEqual(expected_result);
    });

    test('CardGroup addCard lets you append cards by default', () => {
        document.body.innerHTML = `
            <div id='container'/>
        `;
        let container = document.getElementById("container");
        let belongTestFunc = jest.fn();
        let _groupUnderTest = new CardGroup(container, belongTestFunc, "Tanagra");
        expect(document.getElementById("Tanagra")).not.toEqual(null);
        
        let darmok = document.createElement("div");
        darmok.id = "Darmok";
        darmok.classList.add("card");

        let jalad = document.createElement("div");
        jalad.id = "Jalad";
        jalad.classList.add("card");

        expect(_groupUnderTest.groupDiv.children.length).toBe(0);

        _groupUnderTest.addCard(darmok);
        expect(_groupUnderTest.groupDiv.children.length).toBe(1);

        _groupUnderTest.addCard(jalad);
        expect(_groupUnderTest.groupDiv.children.length).toBe(2);

        // check order was append
        expect(_groupUnderTest.groupDiv.children[1]).toEqual(jalad);
    });

    test('CardGroup addCard lets you prepend cards with 0 or negative number', () => {
        document.body.innerHTML = `
            <div id='container'/>
        `;
        let container = document.getElementById("container");
        let belongTestFunc = jest.fn();
        let _groupUnderTest = new CardGroup(container, belongTestFunc, "Tanagra");
        expect(document.getElementById("Tanagra")).not.toEqual(null);
        
        let darmok = document.createElement("div");
        darmok.id = "Darmok";
        darmok.classList.add("card");

        let jalad = document.createElement("div");
        jalad.id = "Jalad";
        jalad.classList.add("card");

        expect(_groupUnderTest.groupDiv.children.length).toBe(0);

        _groupUnderTest.addCard(darmok, 0);
        expect(_groupUnderTest.groupDiv.children.length).toBe(1);

        _groupUnderTest.addCard(jalad, -5);
        expect(_groupUnderTest.groupDiv.children.length).toBe(2);

        // check order was append
        expect(_groupUnderTest.groupDiv.children[1]).toEqual(darmok);
    });

    test('CardGroup addCard lets you insert cards', () => {
        document.body.innerHTML = `
            <div id='container'/>
        `;
        let container = document.getElementById("container");
        let belongTestFunc = jest.fn();
        let _groupUnderTest = new CardGroup(container, belongTestFunc, "Enterprise");
        expect(document.getElementById("Enterprise")).not.toEqual(null);
        
        let archer = document.createElement("div");
        archer.id = "Archer";
        archer.classList.add("card");

        let tripp = document.createElement("div");
        tripp.id = "Tripp";
        tripp.classList.add("card");

        let tpol = document.createElement("div");
        tpol.id = "T'Pol";
        tpol.classList.add("card");

        let reed = document.createElement("div");
        reed.id = "Reed";
        reed.classList.add("card");

        _groupUnderTest.addCard(archer);
        _groupUnderTest.addCard(tripp);
        _groupUnderTest.addCard(tpol);
        _groupUnderTest.addCard(reed);

        expect(_groupUnderTest.groupDiv.children.length).toEqual(4);
        expect(_groupUnderTest.groupDiv.children[2]).toEqual(tpol);

        let hoshi = document.createElement("div");
        hoshi.id = "Hoshi";
        hoshi.classList.add("card");

        _groupUnderTest.addCard(hoshi, 2);

        // check we inserted and tpol moved down
        expect(_groupUnderTest.groupDiv.children.length).toEqual(5);
        expect(_groupUnderTest.groupDiv.children[2]).toEqual(hoshi);
        expect(_groupUnderTest.groupDiv.children[3]).toEqual(tpol);
    });

    test('CardGroup addCard throws on an invalid position', () => {
        document.body.innerHTML = `
            <div id='container'/>
        `;
        let container = document.getElementById("container");
        let belongTestFunc = jest.fn();
        let _groupUnderTest = new CardGroup(container, belongTestFunc, "Tanagra");
        expect(document.getElementById("Tanagra")).not.toEqual(null);
        
        let darmok = document.createElement("div");
        darmok.id = "Darmok";
        darmok.classList.add("card");

        expect(() => _groupUnderTest.addCard(darmok, "string")).toThrow(Error);
    });

    test.skip('CardGroup setBounds assigns mostly static values', () => {
        document.body.innerHTML = `
            <div id='container'/>
        `;
        let jqContainer = $('#container');
        let belongTestFunc = jest.fn();
        let groupUnderTest = new CardGroup(jqContainer, belongTestFunc);
        let layoutCardsTestFunc = jest.spyOn(groupUnderTest, 'layoutCards').mockImplementation(() => null);

        // defaults
        expect(groupUnderTest.x).toBe(undefined);
        expect(groupUnderTest.y).toBe(undefined);
        expect(groupUnderTest.width).toBe(undefined);
        expect(groupUnderTest.height).toBe(undefined);
        expect(groupUnderTest.descDiv.css("left")).toBe("");
        expect(groupUnderTest.descDiv.css("top")).toBe("");
        expect(groupUnderTest.descDiv.css("width")).toBe("0px");
        expect(groupUnderTest.descDiv.css("height")).toBe("0px");
        expect(groupUnderTest.descDiv.css("position")).toBe("");
        expect(layoutCardsTestFunc.mock.calls.length).toEqual(0);

        groupUnderTest.setBounds(1, 1, 20, 40);

        // container padding is 3
        expect(groupUnderTest.x).toBe(4); // x + container padding
        expect(groupUnderTest.y).toBe(4); // y + container padding
        expect(groupUnderTest.width).toBe(14); // width - (3*2)
        expect(groupUnderTest.height).toBe(34); // height - (3*2)
        // BUG: should be x but we don't use the computed value, we used the passed in value
        expect(groupUnderTest.descDiv.css("left")).toBe("1px"); // should be x
        // BUG: should be y but we don't use the computed value, we used the passed in value
        expect(groupUnderTest.descDiv.css("top")).toBe("1px"); // should be y
        // BUG: should be width but we don't use the computed value, we used the passed in value
        expect(groupUnderTest.descDiv.css("width")).toBe("20px");
        // BUG: should be height but we don't use the computed value, we used the passed in value
        expect(groupUnderTest.descDiv.css("height")).toBe("40px");
        expect(groupUnderTest.descDiv.css("position")).toBe("absolute");

        // verify we have called 
        expect(layoutCardsTestFunc.mock.calls.length).toEqual(1);
    });

    test.skip('CardGroup layoutCards complains about inheritance', () => {
        document.body.innerHTML = `
            <div id='container'/>
        `;
        let jqContainer = $('#container');
        
        let belongTestFunc = jest.fn();
        let mockAlert = jest.spyOn(window, 'alert').mockImplementation(() => null);

        let _groupUnderTest = new CardGroup(jqContainer, belongTestFunc);
        expect(mockAlert.mock.calls.length).toEqual(0); // alert not called
        _groupUnderTest.layoutCards();
        expect(mockAlert.mock.calls.length).toEqual(1); // alert called
    });

    // This test is skipped because the mocks do not seem to be called at all by the 
    // function, despite the fact this works in other cases.
    // Perhaps this is due to the functions being outside the default exported class?
    test.skip('CardGroup layoutCard calls layoutCardElem and layoutTokens', () => {
        document.body.innerHTML = `
            <div id='container'>
              <div class='card' id='the_card' />
            </div>
        `;
        let jqContainer = $('#container');
        
        let belongTestFunc = jest.fn();
        let mockLayoutCardElem = jest.spyOn(jCG, 'layoutCardElem').mockImplementation(() => null);
        let mockLayoutTokens = jest.spyOn(jCG, 'layoutTokens').mockImplementation(() => null);

        let _groupUnderTest = new CardGroup(jqContainer, belongTestFunc);
        expect(mockLayoutCardElem.mock.calls.length).toEqual(0);
        expect(mockLayoutTokens.mock.calls.length).toEqual(0);

        let cardElem = $("#the_card");
        let x = 0;
        let y = 0;
        let width = 240;
        let height = 320;
        let index = 0;
        _groupUnderTest.layoutCard(cardElem, x, y, width, height, index);

        // I don't understand why these fail
        expect(mockLayoutCardElem.mock.calls.length).toEqual(1);
        expect(mockLayoutTokens.mock.calls.length).toEqual(1);
    });
});