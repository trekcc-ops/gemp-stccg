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

    test('CardGroup cardBelongs can identify cards in the group', () => {
        document.body.innerHTML = `
            <div id='Tanagra'>
                <div class='card' id='Darmok' />
                <div class='card' id='Jalad' />
                <div class='notacard' id='Picard' />
            </div>
        `;
        let jqContainer = $('#Tanagra');

        // Because the card group isn't actually an array containing anything, it has
        // to be told how to determine if a card is in its group. These are things like
        // PlayPileCardGroup (a subclass) being told to check against card.zone == "PLAY_PILE".
        let belongTestFunc = jest.fn(card => card.classList.contains("card"));

        let _groupUnderTest = new CardGroup(jqContainer, belongTestFunc);
        // Assert we called the belongTestFunction twice, once on each card in the div
        // but not on the notacard object.

        expect(_groupUnderTest.cardBelongs(document.getElementById("Darmok"))).toEqual(true);
        expect(_groupUnderTest.cardBelongs(document.getElementById("Jalad"))).toEqual(true);
        expect(_groupUnderTest.cardBelongs(document.getElementById("Picard"))).toEqual(false);

        expect(belongTestFunc.mock.calls.length).toEqual(3);
    });

    test('CardGroup getCardElems will depend on data stored in a jQuery .data object', () => {
        document.body.innerHTML = `
            <div id='Tanagra'>
                <div class='card' id='Darmok' />
                <div class='card' id='Jalad' />
                <div class='notacard' id='Picard' />
            </div>
        `;
        let jqContainer = $('#Tanagra');

        // Because the card group isn't actually an array containing anything, it has
        // to be told how to determine if a card is in its group. These are things like
        // PlayPileCardGroup (a subclass) being told to check against card.zone == "PLAY_PILE"
        // where "card" is the JSON data stored in JQ.data("card": {})
        let belongTestFunc = jest.fn(card_data => card_data.blueprintid < 3);

        let _groupUnderTest = new CardGroup(jqContainer, belongTestFunc);

        // assign jquery card data expected by the func
        $('#Darmok').data("card", {class: "card", "blueprintid": 1});
        $('#Jalad').data("card", {class: "card", "blueprintid": 2});
        $('#Picard').data("card", {class: "notacard", "blueprintid": 3});

        expect(_groupUnderTest.cardBelongs($("#Darmok").data("card"))).toEqual(true);
        expect(_groupUnderTest.cardBelongs($("#Jalad").data("card"))).toEqual(true);
        expect(_groupUnderTest.cardBelongs($("#Picard").data("card"))).toEqual(false);

        expect(belongTestFunc.mock.calls.length).toEqual(3);
    });

    test('CardGroup getCardElems passes card class objects to the belong func', () => {
        document.body.innerHTML = `
            <div id='Tanagra'>
                <div class='card' id='Darmok' />
                <div class='card' id='Jalad' />
                <div class='notacard' id='Picard' />
            </div>
            <div id='Balcony'>
                <div class='card' id='Juliet' />
            </div>
        `;
        let jqContainer = $('#Tanagra');

        // Because the card group isn't actually an array containing anything, it has
        // to be told how to determine if a card is in its group. These are things like
        // PlayPileCardGroup (a subclass) being told to check against card.zone == "PLAY_PILE"
        // where "card" is the JSON data stored in JQ.data("card": {})
        let belongTestFunc = jest.fn(card_data => card_data.blueprintid < 3);
        //let belongTestFunc = jest.fn(card_data => console.log(card_data.blueprintid));

        let _groupUnderTest = new CardGroup(jqContainer, belongTestFunc);

        // assign jquery card data expected by the func
        $('#Darmok').data("card", {class: "card", "blueprintid": 1});
        $('#Jalad').data("card", {class: "card", "blueprintid": 2});
        $('#Picard').data("card", {class: "notacard", "blueprintid": 3});
        $('#Juliet').data("card", {class: "card", "blueprintid": 0});

        // BUG: Because the container is Tanagra, Juliet should not be there even though
        // she matches the belongTestFunc.
        // Unfortunately, the getCardElems func is checking everything on the page not just
        // things in the constructor as expected.
        let expected = [
            $('#Darmok'),
            $('#Jalad'),
            $('#Juliet')
        ];

        let result = _groupUnderTest.getCardElems();

        expect(belongTestFunc.mock.calls.length).toEqual(3);
        expect(result).toEqual(expected);
    });

    test('CardGroup setBounds assigns mostly static values', () => {
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

});