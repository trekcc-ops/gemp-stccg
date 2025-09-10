// This is meant to be the JQuery GEMP UI equivalent to the upcoming React CardStack object.
// It is designed to be backwards compatible with our jCardGroup while
//   not supporting future React-only things like chips, opening the stack browser dialog, etc.
//
// At its core, this is an object that remembers which cards it contains,
// and then also has a <div> it manages to display an area for those cards to display within.

import Card from "./jCards.js";
import { cardScale } from "./jCards.js";

export default class CardGroup {
    constructor(_container, _belongTest, divId) {
        // TODO: Remove _container, don't append, return the div and make the container append it itself
        // TODO: Remove _belongTest
        this.container = _container; // jquery element we will attach ourselves to
        this.belongTest = _belongTest; // sent by handler, we're going to ignore it.

        // TODO: assert id is required

        this.groupDiv = document.createElement("div");
        this.groupDiv.id = divId ? divId : ''; // TODO: remove
        this.groupDiv.classList.add("ui-widget-content", "card-group");

        this.container.append(this.groupDiv);
    }

    addCard(cardElement, position = -1) {
        // TODO: assert cardElement is a HTMLElement class

        if (position === -1) {
            // add to end (bottom)
            this.groupDiv.append(cardElement);
        }
        else {
            // add to position, addafter
        }

        //relayout
    }

    removeCardById(cardId) {
        //relayout
        let cardElement = document.getElementById(cardId);
        cardElement.remove();
    }

    replaceCardById(cardElement, cardIdToReplace) {
        // get card id position
        // removecardbyid
        // addcard with position
        //relayout
    }

    // global JQ lookup, can we do this locally?.
    getCardElems() {
        let cardsToLayout = new Array();

        for (let i = 0; i < this.groupDiv.children.length; i++) {
            let elem = this.groupDiv.children.item(i);
            if (elem.classList.contains("card")) {
                cardsToLayout.push(elem);
            }
        }
        
        return cardsToLayout;
    }

    cardBelongs(cardData) {
        // takes in the jquery card.data(), returns bool
        let cardIdToFind = cardData.id;
        let match = document.querySelector(`#${this.groupDiv.id} > #${cardIdToFind}`)

        return match ? true : false;
    }
}