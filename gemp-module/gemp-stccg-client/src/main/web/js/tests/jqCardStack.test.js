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
});