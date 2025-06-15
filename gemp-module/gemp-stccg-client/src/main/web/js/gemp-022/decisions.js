import Card from './jCards.js';
import { getCardDivFromId } from './jCards.js';
import { openSizeDialog } from "./common.js";
import { getFriendlyPhaseName } from "./common.js";
import awaitingActionAudio from "../../src/assets/awaiting_decision.mp3";


export function getUserMessage(decision, gameState) {
    if (decision.elementType === "ACTION") {
        let context = decision.context;
        switch(context) {
            case "SELECT_MISSION_FOR_SEED_CARDS":
                return "Select a mission to seed cards under or remove seed cards from";
            case "SELECT_OPTIONAL_RESPONSE_ACTION":
                return "Optional responses";
            case "SELECT_PHASE_ACTION":
                let phaseName = getFriendlyPhaseName(gameState.currentPhase);
                return "Play " + phaseName + " action" + ((decision.min == 0) ? " or pass" : "");
            case "SELECT_REQUIRED_RESPONSE_ACTION":
                return "Required responses";
            case "SELECT_TRIBBLES_ACTION":
                return "Select an action";
            default:
                console.error(`Unknown action selection decision context: ${context}`);
                return;
        }
    } else if (decision.decisionType === "CARD_SELECTION_FROM_COMBINATIONS") {
        return "Select " + decision.min + " to " + decision.max + " cards";
    } else {
        return decision.text; // some user prompts are decided by the server; ideally this will be deprecated over time
    }
}

export function getUseDialog(decision, gameState, gameUi) {
    if (decision.elementType === "ACTION") {
        return decision.context === "SELECT_REQUIRED_RESPONSE_ACTION";
    } else if (decision.elementType === "CARD") {
        let cardIds = decision.cardIds;
        for (let i = 0; i < cardIds.length; i++) {
            let cardId = cardIds[i];
            let yourPlayerName = gameUi.bottomPlayerId;
            let cardData = gameState.visibleCardsInGame[cardId.toString()];
            let cardIsVisible = false;
            if (cardData != null) {
                if (cardData.isInPlay && (cardData.cardType === "FACILITY" || cardData.cardType === "MISSION")) {
                    cardIsVisible = true;
                }
            }
            for (let i = 0; i < gameState.players.length; i++) {
                // Cards in hand don't need the dialog
                if (gameState.players[i].playerName === yourPlayerName && gameState.players[i].cardGroups["HAND"].includes(cardId)) {
                    cardIsVisible = true;
                }
            }
            if (!cardIsVisible) {
                return true;
            }
        }
        return false;
    }
}


export function processDecision(decision, animate, gameUi, gameState) {
    $("#main").queue(
        function (next) {
            let elementType = decision.elementType;
            switch(elementType) {
                case "ACTION":
                case "CARD":
                    if (elementType === "ACTION" && decision.displayedCards.length == 0 && gameUi.gameSettings.get("autoPass") && !gameUi.replayMode) {
                        gameUi.decisionFunction(decision.decisionId, "");
                    } else {
                        let userMessage = getUserMessage(decision, gameState);
                        let useDialog = getUseDialog(decision, gameState, gameUi);
                        let decisionObject = new gameDecision(decision, gameUi, useDialog, gameState);
                        decisionObject.createUiElements(userMessage);
                        decisionObject.allowSelection();
                        goDing(gameUi);
                        if (decisionObject.useDialog) {
                            decisionObject.resizeDialog();
                        }
                        decisionObject.resetFocus();
                    }
                    break;
                case "INTEGER":
                    integerDecision(decision, gameUi);
                    break;
                case "STRING":
                    multipleChoiceDecision(decision, gameUi);
                    break;
                default:
                    console.error(`Unknown elementType: ${elementType}`);
                    next(); // bail out
                    break;
            }
            if (!animate)
                gameUi.layoutUI(false);
            next();
        });
    if (gameUi.replayMode) {
        $("#main").queue(
            function (next) {
                gameUi.animations.setTimeout(next, gameUi.getAnimationLength(gameUi.decisionDuration));
            });
    }
}

export default class gameDecision {

    gameUi; // GameTableUI object
    decisionType; // string; this is being deprecated
    decisionId; // integer; unique identifier for decision object in server
    elementType; // string representing the type of object that's being selected (ACTION or CARD)

    displayedCards; // JSON map

    /* Card IDs - For dialog decisions, only "allServerCardIds" has the same cardId values shown in the server.
          The other cardID arrays have values that have been modified for use in the UI.
          Creating two card divs with the same card ID in the UI causes some conflicts in the way jCards work,
          so for decisions using the dialog, the "decision UI card IDs" are assigned temporary values.
            ("temp0", "temp1", etc.)
    */
    allServerCardIds = new Array(); // string array (all cards to be shown; server ids)
    allDecisionUiCardIds = new Array(); // string array (all cards to be shown; ui ids)
    selectableCardIds = new Array(); // string array (selectable cards; ui ids)

    // selected element ids uses a different id scheme for action selection decisions
    selectedElementIds = new Array(); // string array (selected elements; ui ids if cards)

    min; // integer; smallest number of elements that can be selected
    max; // integer; largest number of elements that can be selected
    useDialog; // boolean; if true, cards will be shown in a pop-up dialog
    canSelectAll; // boolean; false if selectable cards are interdependent (for example, selecting 5 different cards)
    jsonCombinations; // JSON map

    gameState;

    constructor(decisionJson, gameUi, useDialog, gameState) {
        this.decisionType = decisionJson.decisionType;
        this.gameUi = gameUi;
        this.decisionId = decisionJson.decisionId;
        this.min = decisionJson.min;
        this.max = decisionJson.max;
        this.elementType = decisionJson.elementType;
        this.displayedCards = decisionJson.displayedCards;
        this.useDialog = useDialog;
        this.gameState = gameState;

        for (let i = 0; i < this.displayedCards.length; i++) {
            let cardDivName = (this.useDialog) ? ("temp" + i.toString()) : this.displayedCards[i].cardId.toString();
            this.allServerCardIds.push(this.displayedCards[i].cardId);
            this.allDecisionUiCardIds.push(cardDivName);
            if (this.displayedCards[i].selectable === "true") {
                this.selectableCardIds.push(cardDivName);
            }
        }

        if (this.decisionType === "CARD_SELECTION_FROM_COMBINATIONS") {
            this.canSelectAll = false;
            this.jsonCombinations = JSON.parse(decisionJson.validCombinations);
        } else {
            this.canSelectAll = true;
        }
    }

    createUiElements(userMessage) {
        if (this.useDialog) {
            this.gameUi.cardActionDialog
                .html("<div id='cardSelectionDialog'></div>")
                .dialog("option", "title", userMessage);
        } else {
            this.gameUi.alertText.html(userMessage);
            let alertBoxClass = this.elementType === "ACTION" ? "alert-box-highlight" : "alert-box-card-selection";
            this.gameUi.alertBox.addClass(alertBoxClass);
        }

        if (this.elementType === "ACTION" || this.useDialog) {
            this.createSelectableDivs();
            if (!this.useDialog) {
                this.gameUi.hand.layoutCards();
            }
        }
    }

    resizeDialog() {
        openSizeDialog(this.gameUi.cardActionDialog);
        this.gameUi.arbitraryDialogResize(false);
    }

    resetFocus() {
        $(':button').blur();
        if (this.useDialog) {
            $('.ui-dialog').blur();
        }
    }

    finishChoice() {
        if (this.useDialog) {
            this.gameUi.cardActionDialog.dialog("close");
            $("#cardSelectionDialog").html("");
            this.gameUi.clearSelection();
        } else {
            this.gameUi.alertText.html("");
            let alertBoxClass = this.elementType === "ACTION" ? "alert-box-highlight" : "alert-box-card-selection";
            this.gameUi.alertBox.removeClass(alertBoxClass);
            this.gameUi.alertButtons.html("");
            this.gameUi.clearSelection();
            if (this.elementType === "ACTION") {
                $(".card").each(
                    function () {
                        var card = $(this).data("card");
                        if (card.zone == "EXTRA") {
                            $(this).remove();
                        }
                    });
                this.gameUi.hand.layoutCards();
            }
        }

        if (this.elementType === "CARD" && this.useDialog) {
            for (let i = 0; i < this.selectedElementIds.length; i++) {
                this.selectedElementIds[i] = this.getRealCardId(this.selectedElementIds[i]);
            }
        }
        this.gameUi.decisionFunction(this.decisionId, "" + this.selectedElementIds);
    }

    processButtons() {
        var that = this;

        if (!this.useDialog) {
            this.gameUi.alertButtons.html("");
            if (this.min == 0 && this.selectedElementIds.length == 0) {
                this.gameUi.alertButtons.append("<button id='Pass'>Pass</button>");
                $("#Pass").button().click(function () {
                    that.finishChoice();
                });
            } else if (this.selectedElementIds.length >= this.min) {
                this.gameUi.alertButtons.append("<button id='Done'>Done</button>");
                $("#Done").button().click(function () {
                    that.finishChoice();
                });
            }
            if (this.selectedElementIds.length > 0) {
                this.gameUi.alertButtons.append("<button id='ClearSelection'>Reset choice</button>");
                $("#ClearSelection").button().click(function () {
                    that.resetChoice();
                });
            }
        } else {
            let buttons = {};
            let selectedElementCount = this.selectedElementIds.length;
            if (this.allServerCardIds.length <= this.max && selectedElementCount != this.max && this.canSelectAll && this.elementType === "CARD") {
                buttons["Select all"] = function() {
                    that.selectAllCards();
                }
            }
            if (selectedElementCount > 0) {
                buttons["Clear selection"] = function () {
                    that.resetChoice();
                };
            }
            if (selectedElementCount >= this.min && selectedElementCount <= this.max) {
                buttons["Done"] = function () {
                    that.finishChoice();
                };
            }
            this.gameUi.cardActionDialog.dialog("option", "buttons", buttons);
        }
    }

    resetChoice() {
        this.selectedElementIds = new Array();
        this.gameUi.clearSelection();
        if (this.elementType === "ACTION" && !this.useDialog) {
            // Selecting cards with this decision removes all the divs, so they need to be re-created
            this.createSelectableDivs();
            this.gameUi.hand.layoutCards();
        }
        this.allowSelection();
        this.processButtons();
    }

    respondToCardSelection(cardId, event) {
        console.log("selected card " + cardId);
        var that = this;

        if (this.elementType === "ACTION" && this.useDialog) {
            let actionId = that.displayedCards[parseInt(cardId.substring(4))].actionId;
            that.selectedElementIds.push(actionId);
            that.gameUi.clearSelection();
            if (that.gameUi.gameSettings.get("autoAccept")) {
                that.finishChoice();
            } else {
                that.processButtons();
                getCardDivFromId(cardId).addClass("selectedCard");
            }
        }

        if (this.elementType === "ACTION" && !this.useDialog) {
            // DEBUG: console.log("cardActionChoiceDecision -> allowSelection -> selectionFunction");
            let cardIdElem = getCardDivFromId(cardId);
            let actions = cardIdElem.data("action");

            // If the only legal action is a card play, perform action automatically by clicking
            // Otherwise show a drop-down menu with the action options by clicking
            if (actions.length == 1 &&
                    (actions[0].actionType == "PLAY_CARD" || actions[0].actionType == "SEED_CARD")) {
                this.respondToActionSelection(actions[0].actionId);
            } else {
                this.createActionChoiceContextMenu(actions, event);
            }
        }

        if (this.elementType === "CARD") {
            // If the cardId is already selected, remove it.
            if (this.selectedElementIds.includes(cardId)) {
                console.log("Removing card " + cardId + " from selectedElementIds");
                let index = this.selectedElementIds.indexOf(cardId);
                this.selectedElementIds.splice(index, 1);
                if (!this.useDialog) {
                    getCardDivFromId(cardId).removeClass("selectedCard").addClass("selectableCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                }
            }
            // Otherwise, if the cardId is not already selected, add it.
            else {
                console.log("Adding card " + cardId + " to selectedElementIds");
                this.selectedElementIds.push(cardId);
                if (!this.useDialog) {
                    getCardDivFromId(cardId).removeClass("selectableCard").addClass("selectedCard").addClass("selectedBadge");
                }
            }

            this.recalculateCardSelectionOrder();

            if (this.useDialog) {
                if (this.canSelectAll) {
                    this.recalculateAllowedSelectionFromMaxCSS(that.selectableCardIds, that.selectedElementIds, that.max);
                } else {
                    this.recalculateAllowedCombinationsAndCSS(that.allDecisionUiCardIds, that.selectedElementIds, that.jsonCombinations, that.max);
                }
                // DEBUG
                let divClasses = new Array();
                let cardDiv = getCardDivFromId(cardId);
                if (cardDiv.hasClass("selectableCard")) {
                    divClasses.push("selectableCard");
                }
                if (cardDiv.hasClass("notSelectableCard")) {
                    divClasses.push("notSelectableCard");
                }
                if (cardDiv.hasClass("selectedCard")) {
                    divClasses.push("selectedCard");
                }
                console.log("Classes for div '" + cardId + "': " + divClasses);
            } else {
                // If the max number of cards are selected and the user has auto accept on, we're done.
                if ((that.selectedElementIds.length == that.max) && (this.gameUi.gameSettings.get("autoAccept"))) {
                    that.finishChoice();
                    return;
                }
            }

            that.processButtons();
        }
    }

    respondToActionSelection(actionId) {
        this.selectedElementIds.push(actionId);
        if (this.gameUi.gameSettings.get("autoAccept")) {
           this.finishChoice();
        } else {
            this.gameUi.clearSelection();
            getCardDivFromId(cardId).addClass("selectedCard");
            this.processButtons();
        }
    }

    allowSelection() {
        var that = this;

        // gameUi.selectionFunction is called when a card is clicked
        //   thanks to the code in gameUi.clickCardFunction()
        this.gameUi.selectionFunction = function (cardId, event) {
            that.respondToCardSelection(cardId, event);
        };

        if (this.selectableCardIds.length > 0) {
            $(".card:cardId(" + this.selectableCardIds + ")").addClass("selectableCard");
        }
        this.processButtons();
    }

    createSelectableDivs() {
        // For action selections from visible cards, each relevant card is associated with a list of its
        //      available actions
        if (this.elementType === "ACTION" && !this.useDialog) {
            for (let i = 0; i < this.displayedCards.length; i++) {
                let displayedCard = this.displayedCards[i];
                let cardId = this.allDecisionUiCardIds[i];
                let actionId = displayedCard.actionId;
                let actionText = displayedCard.actionText;
                let actionType = displayedCard.actionType;
                let cardIdElem = getCardDivFromId(cardId);

                if (cardIdElem.data("action") == null) {
                    cardIdElem.data("action", new Array());
                }
                let actions = cardIdElem.data("action");
                actions.push({actionId: actionId, actionText: actionText, actionType: actionType});
            }
        } else if (this.useDialog) {
            for (let i = 0; i < this.allDecisionUiCardIds) {
                let decisionUiCardId = this.allDecisionUiCardIds[i];
                let serverCardId = this.getRealCardId(decisionUiCardId);
                let gameStateCard = this.gameState.visibleCardsInGame[serverCardId];
                let blueprintId = gameStateCard.blueprintId;
                let imageUrl = gameStateCard.imageUrl;
                let zone = "SPECIAL";
                let noOwner = "";
                let noLocationIndex = "";
                let upsideDown = false;
                let card = new Card(blueprintId, zone, decisionUiCardId, noOwner, imageUrl, noLocationIndex, upsideDown);
                let cardDiv;
                switch(this.elementType) {
                    case "ACTION":
                        cardDiv = this.gameUi.createCardDivWithData(card, displayedCard.actionText);
                    case "CARD":
                        cardDiv = this.gameUi.createCardDivWithData(card);
                }
                $("#cardSelectionDialog").append(cardDiv);
            }
        }
    }

    recalculateCardSelectionOrder() {
        for (const [index, cardId] of this.selectedElementIds.entries()) {
            let divToChange = getCardDivFromId(cardId);
            divToChange.attr("selectedOrder", index + 1); // use a 1-index
        }
    }

    selectAllCards() {
        this.selectedElementIds = Array.from(this.selectableCardIds);
        this.recalculateCardSelectionOrder();
        this.recalculateAllowedSelectionFromMaxCSS(this.selectableCardIds, this.selectedElementIds, this.max);
        this.allowSelection();
    }

    createActionChoiceContextMenu(actions, event) {
        var that = this;
        // Remove context menus that may be showing
        $(".contextMenu").remove();

        var div = $("<ul class='contextMenu'></ul>");
        for (var i = 0; i < actions.length; i++) {
            var action = actions[i];
            var text = action.actionText;
            div.append("<li><a href='#" + action.actionId + "'>" + text + "</a></li>");
        }

        $("#main").append(div);

        var x = event.pageX;
        var y = event.pageY;
        $(div).css({left: x, top: y}).fadeIn(150);

        $(div).find('A').mouseover(
            function () {
                $(div).find('LI.hover').removeClass('hover');
                $(this).parent().addClass('hover');
            }).mouseout(function () {
            $(div).find('LI.hover').removeClass('hover');
        });

        var getRidOfContextMenu = function () {
            $(div).remove();
            $(document).unbind("click", getRidOfContextMenu);
            return false;
        };

        // When items are selected
        $(div).find('A').unbind('click');
        $(div).find('LI:not(.disabled) A').click(function () {
            $(document).unbind('click', getRidOfContextMenu);
            $(".contextMenu").remove();

            var actionId = $(this).attr('href').substr(1);
            that.respondToActionSelection(actionId);
            return false;
        });

        // Hide bindings
        setTimeout(function () { // Delay for Mozilla
            $(document).click(getRidOfContextMenu);
        }, 0);
    }

    recalculateAllowedSelectionFromMaxCSS(cardIds, selectedCardIds, max) {
        if (max === 0) {
            console.error("Max is 0, setting all cards to not selectable. This is probably a server bug.");
            for (const cardId of cardIds.values()) {
                getCardDivFromId(cardId).removeClass("selectableCard").removeClass("selectedCard").addClass("notSelectableCard").removeClass("selectedBadge").removeAttr("selectedOrder");
            }
            return;
        }
        else {
            for (const cardId of cardIds.values()) {
                if (selectedCardIds.length === 0) {
                    // everything is selectable
                    getCardDivFromId(cardId).addClass("selectableCard").removeClass("selectedCard").removeClass("notSelectableCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                }
                else {
                    // selected
                    if (selectedCardIds.includes(cardId)) {
                        getCardDivFromId(cardId).removeClass("selectableCard").removeClass("notSelectableCard").addClass("selectedCard").addClass("selectedBadge");
                    }
                    // not selected
                    else {
                        // we hit the max, gray out unselected cards since we can't add more
                        if (selectedCardIds.length === max) {
                            getCardDivFromId(cardId).addClass("notSelectableCard").removeClass("selectableCard").removeClass("selectedCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                            continue;
                        }
                        else {
                            getCardDivFromId(cardId).addClass("selectableCard").removeClass("selectedCard").removeClass("notSelectableCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                        }
                    }
                }
            }
            return;
        }
    }

    getDecisionUiCardId(realCardId) {
        let indexNum = this.allServerCardIds.indexOf(realCardId);
        return this.allDecisionUiCardIds[indexNum];
    }

    getRealCardId(decisionUiCardId) {
        let indexNum = this.allDecisionUiCardIds.indexOf(decisionUiCardId);
        return this.allServerCardIds[indexNum];
    }

    recalculateAllowedCombinationsAndCSS(cardIds, selectedCardIds, jsonCombinations, max) {
        if (typeof(jsonCombinations) !== 'object' || jsonCombinations == null) {
            let inc_type = typeof(jsonCombinations);
            throw new TypeError(`jsonCombinations must be an Object not a ${inc_type} or null.`);;
        }

        let allowedCombinationsRemaining = new Set();

        if (selectedCardIds.length === 0) {
            // DEBUG: console.log("No selected cards.");
            allowedCombinationsRemaining = new Set(cardIds);
            // DEBUG: console.log(`Allowed combinations remaining: ${Array.from(allowedCombinationsRemaining)}`);
        }
        else if (selectedCardIds.length === 1) {
            // selected one card
            // DEBUG: console.log(`Selected cards: ${selectedCardIds}`);
            const cardId = selectedCardIds[0];
            let this_card_allowed = new Array();
            // DEBUG: console.log("let_this_card_allowed created");
            // DEBUG: console.log(jsonCombinations[cardId]);
            for (const compatible_cardId of jsonCombinations[this.getRealCardId(cardId)]) {
                // DEBUG: console.log("iterating through cards in jsonCombinations[" + cardId + "]");
                this_card_allowed.push(this.getDecisionUiCardId(compatible_cardId));
            }
            const this_allowed_as_set = new Set(this_card_allowed);
            allowedCombinationsRemaining = this_allowed_as_set;

            // DEBUG: console.log(`Allowed combinations remaining: ${Array.from(allowedCombinationsRemaining)}`);
        }
        else {
            // selected two or more cards
            // DEBUG: console.log(`Selected cards: ${selectedCardIds}`);
            for (const [index, cardId] of selectedCardIds.entries()) {
                let this_card_allowed = new Array();
                // DEBUG: console.log("let_this_card_allowed created");
                // DEBUG: console.log(jsonCombinations[cardId]);
                for (const compatible_cardId of jsonCombinations[this.getRealCardId(cardId)]) {
                    // DEBUG: console.log("iterating through cards in jsonCombinations[" + cardId + "]");
                    this_card_allowed.push(this.getDecisionUiCardId(compatible_cardId));
                }
                const this_allowed_as_set = new Set(this_card_allowed);

                if (index === 0) {
                    // Don't use .intersection on the first pass, since the intersection of empty set and valid choices is nothing.
                    allowedCombinationsRemaining = this_allowed_as_set;
                }
                else {
                    allowedCombinationsRemaining = allowedCombinationsRemaining.intersection(this_allowed_as_set);
                }
                // DEBUG: console.log(`Allowed combinations remaining: ${Array.from(allowedCombinationsRemaining)}`);
            }
        }


        // Apply CSS
        // BUG: Normally I'd split this into another function but when I did, JQuery
        //      didn't pass the Sets around properly. IDK. One big function it is.
        for (const cardId of cardIds.values()) {
            if (selectedCardIds.length === 0) {
                // everything is selectable
                // DEBUG: console.log("Everything is selectable.");
                getCardDivFromId(cardId).addClass("selectableCard").removeClass("selectedCard").removeClass("notSelectableCard").removeClass("selectedBadge").removeAttr("selectedOrder");
            }
            else {
                // selected
                if (selectedCardIds.includes(cardId)) {
                    getCardDivFromId(cardId).removeClass("selectableCard").removeClass("notSelectableCard").addClass("selectedCard").addClass("selectedBadge");
                }
                // not selected
                else {
                    // we hit the max, treat unselected cards as if they are not compatible
                    if (selectedCardIds.length === max) {
                        getCardDivFromId(cardId).addClass("notSelectableCard").removeClass("selectableCard").removeClass("selectedCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                        continue;
                    }

                    // Not selected, not at the max, and compatible with other selected cards
                    if (allowedCombinationsRemaining.has(cardId)) {
                        // DEBUG: console.log(`Not selected, compatible: ${cardId}`);
                        getCardDivFromId(cardId).addClass("selectableCard").removeClass("selectedCard").removeClass("notSelectableCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                    }
                    // Not selected, not at the max, but not compatible with other selected cards
                    else {
                        // DEBUG: console.log(`Not selected, not compatible: ${cardId}`);
                        // same as above but w/o selectableCard
                        getCardDivFromId(cardId).addClass("notSelectableCard").removeClass("selectableCard").removeClass("selectedCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                    }
                }
            }

        }

    }
}

export function integerDecision(decision, gameUi) {
    let id = decision.decisionId;
    let text = decision.text;
    var val = 0;

    var min = decision.min;
    if (min == null) {
        min = 0;
    }
    var max = decision.max;
    if (max == null) {
        max = 1000;
    }

    var defaultValue = decision.defaultValue;
    if (defaultValue != null) {
        val = parseInt(defaultValue);
    }

    gameUi.smallDialog.html(text + `<br /><input id='integerDecision' value='${val}'>`);

    if (!gameUi.replayMode) {
        gameUi.smallDialog.dialog("option", "buttons",
            {
                "OK": function () {
                    let retval = document.getElementById("integerDecision").value
                    $(this).dialog("close");
                    gameUi.decisionFunction(id, retval);
                }
            });
    }

    $("#integerDecision").spinner({
        min: parseInt(min),
        max: parseInt(max)
    });

    gameUi.smallDialog.dialog("open");
    $('.ui-dialog :button').blur();
}

export function multipleChoiceDecision(decision, gameUi) {
    var id = decision.decisionId;
    let serverText = decision.text; // raw string from the server for the user message
    let context = decision.context;
    let serverValues = decision.results; // raw strings that are passed from the server
    let results = new Array();
    let text;

    if (context === "SEED_MISSION_INDEX_SELECTION") {
        // For this decision, the serverValues are strings representing the spaceline indices where the mission
        // can be seeded. This array should be in ascending numerical order.
        text = "Seed mission on the left or right?";
        results.push("LEFT");
        results.push("RIGHT");
    } else {
        text = serverText;
        results = serverValues;
    }

    gameUi.smallDialog.html(text);

    if (results.length > 2 || gameUi.gameSettings.get("alwaysDropDown")) {
        var html = "<br /><select id='multipleChoiceDecision' selectedIndex='0'>";
        for (var i = 0; i < results.length; i++) {
            html += "<option value='" + i + "'>" + results[i] + "</option>";
        }
        html += "</select>";
        gameUi.smallDialog.append(html);

        if (!gameUi.replayMode) {
            gameUi.smallDialog.dialog("option", "buttons",
                {
                    "OK": function () {
                        gameUi.smallDialog.dialog("close");
                        gameUi.decisionFunction(id, $("#multipleChoiceDecision").val());
                    }
                });
        }
    } else {
        gameUi.smallDialog.append("<br />");
        for (var i = 0; i < results.length; i++) {
            if (i > 0) {
                gameUi.smallDialog.append(" ");
            }

            var but = $("<button></button>").html(results[i]).button();
            if (!gameUi.replayMode) {
                but.click(
                    (function (ind) {
                        return function () {
                            gameUi.smallDialog.dialog("close");
                            gameUi.decisionFunction(id, "" + ind);
                        }
                    })(i));
            }
            gameUi.smallDialog.append(but);
        }
        if (!gameUi.replayMode) {
            gameUi.smallDialog.dialog("option", "buttons", {});
            PlayAwaitActionSound();
        }
    }

    gameUi.smallDialog.dialog("open");
    $('.ui-dialog :button').blur();
}

export function PlayAwaitActionSound() {
    let audio = new Audio(awaitingActionAudio);
    if(!document.hasFocus() || document.hidden || document.msHidden || document.webkitHidden)
    {
        audio.play();
    }
}

export function goDing(gameUi) {
    if (!gameUi.replayMode) {
        PlayAwaitActionSound();
    }
}