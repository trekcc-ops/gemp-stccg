import Card from './jCards.js';
import { getCardDivFromId } from './jCards.js';
import { openSizeDialog } from "./common.js";
import { getFriendlyPhaseName } from "./common.js";
import awaitingActionAudio from "../../src/assets/awaiting_decision.mp3";
import ActionSelectionDecision from './actionSelectionDecisions.js';


export function getUserMessage(decision, gameState) {
    if (decision.elementType === "ACTION") {
        let context = decision.context;
        switch(context) {
            case "SELECT_MISSION_FOR_SEED_CARDS":
                return "Select a mission to seed cards under or remove seed cards from";
            case "SELECT_OPTIONAL_RESPONSE_ACTION":
                return "Optional responses";
            case "SELECT_PHASE_ACTION": {
                let phaseName = getFriendlyPhaseName(gameState.currentPhase);
                return "Play " + phaseName + " action" + ((decision.min == 0) ? " or pass" : "");
            }
            case "SELECT_REQUIRED_RESPONSE_ACTION":
                return "Required responses";
            case "SELECT_TRIBBLES_ACTION":
                return "Select an action";
            default:
                console.error(`Unknown action selection decision context: ${context}`);
                return;
        }
    } else {
        return decision.text; // some user prompts are decided by the server; ideally this will be deprecated over time
    }
}

export function getUseDialog(decision, gameState, gameUi) {
    if (decision.elementType === "CARD") {
        let displayedCards = decision.displayedCards;
        for (let i = 0; i < displayedCards.length; i++) {
            let cardId = displayedCards[i].cardId;
            let yourPlayerName = gameUi.bottomPlayerId;
            let cardData = gameState.visibleCardsInGame[cardId.toString()];
            let cardIsVisible = false;
            if (cardData != null) {
                if (cardData.isInPlay && (cardData.cardType === "FACILITY" || cardData.cardType === "MISSION")) {
                    cardIsVisible = true;
                }
            }

            for (const playerData of Object.values(gameState.playerMap)) {
                if (playerData.playerId === yourPlayerName && playerData.cardGroups["HAND"].includes(cardId)) {
                    cardIsVisible = true;
                }
            }
            
            if (!cardIsVisible) {
                return true;
            }
        }
        return false;
    } else {
        console.error("Unexpected elementType in getUseDialog: '" + decision.elementType + "'");
        return false;
    }
}


export function processDecision(decision, animate, gameUi, gameState) {
    $("#main").queue(
        function (next) {
            let elementType = decision.elementType;
            let userMessage;
            let useDialog;
            let decisionObject;
            switch(elementType) {
                case "ACTION":
                    if (decision.actions.length == 0 && gameUi.gameSettings.get("autoPass") && !gameUi.replayMode) {
                        gameUi.decisionFunction(decision.decisionId, "");
                    } else {
                        userMessage = getUserMessage(decision, gameState);
                        decisionObject = new ActionSelectionDecision(decision, gameUi, gameState);
                        decisionObject.initializeUi(userMessage);
                    }
                    break;
                case "CARD":
                    userMessage = getUserMessage(decision, gameState);
                    useDialog = getUseDialog(decision, gameState, gameUi);
                    decisionObject = new CardSelectionDecision(decision, gameUi, useDialog, gameState);
                    decisionObject.initializeUi(userMessage);
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

export default class CardSelectionDecision {

    gameUi; // GameTableUI object
    decisionId; // integer; unique identifier for decision object in server

    allCards = new Map();
    orderedCardDivIds = new Array(); // card divs saved in order of how the server sends them
                                        // (although as of June 2025 there are no cases where this matters)
    initiallySelectableCardDivIds = new Array();
    selectedDivIds = new Array();

    // boolean; false if selectable cards are interdependent (for example, selecting 5 different cards)
    independentlySelectable;

    min; // integer; smallest number of elements that can be selected
    max; // integer; largest number of elements that can be selected
    useDialog; // boolean; if true, cards will be shown in a pop-up dialog
    validCombinations = new Map();

    gameState;

    constructor(decisionJson, gameUi, useDialog, gameState) {
        this.gameUi = gameUi;
        this.decisionId = decisionJson.decisionId;
        this.min = decisionJson.min;
        this.max = decisionJson.max;
        this.useDialog = useDialog;
        this.gameState = gameState;
        this.independentlySelectable = decisionJson.independentlySelectable;

        for (let i = 0; i < decisionJson.displayedCards.length; i++) {
            let thisCardMap = new Map();
            let displayedCard = decisionJson.displayedCards[i];
            let cardDivId = this.getDivIdFromCardId(displayedCard.cardId);
            thisCardMap.set("cardDivId", cardDivId);
            thisCardMap.set("cardId", displayedCard.cardId); // integer
            thisCardMap.set("initiallySelectable", displayedCard.selectable);
            if (displayedCard.selectable) {
                this.initiallySelectableCardDivIds.push(cardDivId);
            }
            thisCardMap.set("currentlySelectable", displayedCard.selectable);
            thisCardMap.set("selected", false);
            this.orderedCardDivIds.push(cardDivId);
            if (displayedCard.compatibleCardIds != null && typeof displayedCard.compatibleCardIds != "undefined") {
                let compatibleCardDivIds = new Array();
                for (let j = 0; j < displayedCard.compatibleCardIds.length; j++) {
                    compatibleCardDivIds.push(this.getDivIdFromCardId(displayedCard.compatibleCardIds[j]));
                }
                this.validCombinations.set(cardDivId, compatibleCardDivIds);
            }
            this.allCards.set(cardDivId, thisCardMap);
        }
    }

    getDivIdFromCardId(cardId) {
        let prefix = (this.useDialog) ? "temp" : "";
        return prefix + cardId.toString();
    }

    getCardIdFromDivId(cardDivId) {
        let cardIdString = cardDivId.replace("temp", "");
        return parseInt(cardIdString);
    }

    initializeUi(userMessage) {
        this.createUiElements(userMessage);
        this.allowSelection();
        goDing(this.gameUi);

        if (this.useDialog) {
            // resize dialog
            openSizeDialog(this.gameUi.cardActionDialog);
            this.gameUi.arbitraryDialogResize(false);

            // Reset focus
            $('.ui-dialog :button').blur();
        }
    }

    createUiElements(userMessage) {
        if (this.useDialog) {
            this.gameUi.cardActionDialog
                .html("<div id='cardSelectionDialog'></div>")
                .dialog("option", "title", userMessage);
            this.createSelectableDivs();
        } else {
            this.gameUi.alertText.html(userMessage);
            let alertBoxClass = "alert-box-card-selection";
            this.gameUi.alertBox.addClass(alertBoxClass);
        }
    }

    createSelectableDivs() {
        // Only created if using the card selection dialog. Otherwise this decision relies on existing card divs
        //      already in the game table UI.
        for (let i = 0; i < this.orderedCardDivIds.length; i++) {
            let cardDivId = this.orderedCardDivIds[i];
            let cardId = this.getCardIdFromDivId(cardDivId);
            let gameStateCard = this.gameState.visibleCardsInGame[cardId];
            let blueprintId = gameStateCard.blueprintId;
            let imageUrl = gameStateCard.imageUrl;
            let cardTitle = (gameStateCard.title) ? gameStateCard.title : "";
            let zone = "SPECIAL";
            let noOwner = "";
            let noLocationIndex = "";
            let upsideDown = false;
            let card = new Card(blueprintId, zone, cardDivId, noOwner, cardTitle, imageUrl, noLocationIndex, upsideDown);
            let cardDiv = this.gameUi.createCardDivWithData(card);
            $("#cardSelectionDialog").append(cardDiv);
        }
    }

    allowSelection() {
        var that = this;

        // gameUi.selectionFunction is called when a card is clicked
        //   thanks to the code in gameUi.clickCardFunction()
        this.gameUi.selectionFunction = function (cardDivId, event) {
            that.respondToCardSelection(cardDivId);
        };

        if (this.initiallySelectableCardDivIds.length > 0) {
            $(".card:cardId(" + this.initiallySelectableCardDivIds + ")").addClass("selectableCard");
        }
        this.processButtons();
    }

    processButtons() {
        var that = this;
        let selectedCardCount = this.selectedDivIds.length;

        if (!this.useDialog) {
            this.gameUi.alertButtons.html("");
            if (this.min == 0 && selectedCardCount == 0) {
                this.gameUi.alertButtons.append("<button id='Pass'>Pass</button>");
                $("#Pass").button().click(function () {
                    that.finishChoice();
                });
            } else if (selectedCardCount >= this.min) {
                this.gameUi.alertButtons.append("<button id='Done'>Done</button>");
                $("#Done").button().click(function () {
                    that.finishChoice();
                });
            }
            if (selectedCardCount > 0) {
                this.gameUi.alertButtons.append("<button id='ClearSelection'>Reset choice</button>");
                $("#ClearSelection").button().click(function () {
                    that.resetChoice();
                });
            }
        } else {
            let buttons = {};
            if (this.initiallySelectableCardDivIds.length <= this.max && selectedCardCount < this.max && this.independentlySelectable) {
                buttons["Select all"] = function() {
                    that.selectAllCards();
                }
            }
            if (selectedCardCount > 0) {
                buttons["Clear selection"] = function () {
                    that.resetChoice();
                };
            }
            if (selectedCardCount >= this.min && selectedCardCount <= this.max) {
                buttons["Done"] = function () {
                    that.finishChoice();
                };
            }
            this.gameUi.cardActionDialog.dialog("option", "buttons", buttons);
        }
    }

    finishChoice() {
        let selectedCardIds = new Array();

        if (this.useDialog) {
            this.gameUi.cardActionDialog.dialog("close");
            $("#cardSelectionDialog").html("");
            this.gameUi.clearSelection();
            for (let i = 0; i < this.selectedDivIds.length; i++) {
                let cardId = this.getCardIdFromDivId(this.selectedDivIds[i]);
                selectedCardIds.push(cardId);
            }
        } else {
            this.gameUi.alertText.html("");
            this.gameUi.alertBox.removeClass("alert-box-card-selection");
            this.gameUi.alertButtons.html("");
            this.gameUi.clearSelection();
            selectedCardIds = Array.from(this.selectedDivIds);
        }
        this.gameUi.decisionFunction(this.decisionId, "" + selectedCardIds);
    }

    resetChoice() {
        for (const divId of this.selectedDivIds) {
            this.allCards.get(divId).set("selected", false);
        }
        this.selectedDivIds = new Array();
        this.gameUi.clearSelection();
        this.allowSelection();
        this.processButtons();
    }

    selectAllCards() {
        for (const divId of this.initiallySelectableCardDivIds) {
            this.allCards.get(divId).set("selected", true);
        }
        this.selectedDivIds = Array.from(this.initiallySelectableCardDivIds);
        this.recalculateSelectableDivsBasedOnMaxAllowed();
        this.applyCardDivCSS();
        this.allowSelection();
    }

    respondToCardSelection(cardDivId) {

        if (this.selectedDivIds.includes(cardDivId)) {
            // If the card div id is already selected, remove it.
            let index = this.selectedDivIds.indexOf(cardDivId);
            this.selectedDivIds.splice(index, 1);
            this.allCards.get(cardDivId).set("selected", false);
        } else {
            // Otherwise, if the card div id is not already selected, add it.
            this.selectedDivIds.push(cardDivId);
            this.allCards.get(cardDivId).set("selected", true);
        }

        if (this.independentlySelectable) {
            this.recalculateSelectableDivsBasedOnMaxAllowed();
        } else {
            this.recalculateSelectableDivsBasedOnCombinations();
        }

        this.applyCardDivCSS();

        if (!this.useDialog) {
            // If the max number of cards are selected and the user has auto accept on, we're done.
            if ((this.selectedDivIds.length == this.max) && (this.gameUi.gameSettings.get("autoAccept"))) {
                this.finishChoice();
                return;
            }
        }

        this.processButtons();
    }

    applyCardDivCSS() {
        for (const [cardDivId, cardData] of this.allCards) {
            let cardDiv = getCardDivFromId(cardDivId);
            if (cardData.get("selected")) {
                // selected
                cardDiv.removeClass("selectableCard").removeClass("notSelectableCard");
                cardDiv.addClass("selectedCard").addClass("selectedBadge");
            } else if (cardData.get("currentlySelectable")) {
                // selectable
                cardDiv.removeClass("selectedCard").removeClass("notSelectableCard");
                cardDiv.removeClass("selectedBadge").removeAttr("selectedOrder");
                cardDiv.addClass("selectableCard");
            } else {
                // not selectable
                cardDiv.removeClass("selectableCard").removeClass("selectedCard");
                cardDiv.removeClass("selectedBadge").removeAttr("selectedOrder");
                cardDiv.addClass("notSelectableCard");
            }
        }
        for (const [index, cardDivId] of this.selectedDivIds.entries()) {
            let divToChange = getCardDivFromId(cardDivId);
            divToChange.attr("selectedOrder", index + 1); // use a 1-index
        }
    }

    recalculateSelectableDivsBasedOnMaxAllowed() {
        for (const [cardDivId, cardData] of this.allCards) {
            let consoleErrorReceived = false;
            if (this.max === 0) {
                if (!consoleErrorReceived) {
                    console.error("Max is 0, setting all cards to not selectable. This is probably a server bug.");
                    consoleErrorReceived = true;
                }
                cardData.set("currentlySelectable", false);
            } else if (!cardData.get("initiallySelectable")) {
                // Cards that are not initially selectable will never be selectable regardless
                // No need to do anything
            } else if (this.selectedDivIds.length === 0) {
                // everything is selectable
                cardData.set("currentlySelectable", true);
            } else if (this.selectedDivIds.includes(cardDivId)) {
                // if card is selected
                cardData.set("currentlySelectable", false);
            } else if (this.selectedDivIds.length === this.max) {
                // we hit the max, gray out unselected cards since we can't add more
                cardData.set("currentlySelectable", false);
            } else {
                cardData.set("currentlySelectable", true);
            }
        }
    }

    recalculateSelectableDivsBasedOnCombinations() {

        if (this.validCombinations.size === 0) {
            throw new TypeError(`validCombinations must be a non-empty map`);;
        }

        let allowedCombinationsRemaining = new Set();
        let selectedCardCount = this.selectedDivIds.length;

        if (selectedCardCount === 0) {
            allowedCombinationsRemaining = new Set(this.initiallySelectableCardDivIds);
        } else if (selectedCardCount < this.max) {
            // selected one or more cards
            for (const [index, cardDivId] of this.selectedDivIds.entries()) {
                let this_card_allowed = new Array();
                for (const compatible_divId of this.validCombinations.get(cardDivId)) {
                    this_card_allowed.push(compatible_divId);
                }
                const this_allowed_as_set = new Set(this_card_allowed);

                if (index === 0) {
                    // Don't use .intersection on the first pass, since the intersection of empty set and valid choices is nothing.
                    allowedCombinationsRemaining = this_allowed_as_set;
                }
                else {
                    allowedCombinationsRemaining = allowedCombinationsRemaining.intersection(this_allowed_as_set);
                }
            }
        }

        for (const [divId, cardData] of this.allCards) {
            if (allowedCombinationsRemaining.has(divId) && cardData.get("initiallySelectable")) {
                // Set card to selectable if within the combinations, unless it never was selectable
                cardData.set("currentlySelectable", true);
            } else if (!allowedCombinationsRemaining.has(divId) && cardData.get("currentlySelectable")) {
                // Remove selectable property from card if it's not in the allowed combinations
                cardData.set("currentlySelectable", false);
            }
        }
    }
}

export function integerDecision(decision, gameUi) {
    let id = decision.decisionId;
    let text = decision.text;

    var min = decision.min;
    if (min == null) {
        min = 0;
    }
    var max = decision.max;
    if (max == null) {
        max = 1000;
    }

    // Default value. Used to be sent by the server, but will be up to the client going forward.
    let val = min;

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
    let serverValues = decision.options; // raw strings that are passed from the server
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
        }
        goDing(gameUi);
    }

    gameUi.smallDialog.dialog("open");
    $('.ui-dialog :button').blur();
}

export function goDing(gameUi) {
    if (!gameUi.replayMode) {
        let audio = new Audio(awaitingActionAudio);
        if(!document.hasFocus() || document.hidden || document.msHidden || document.webkitHidden)
        {
            audio.play();
        }
    }
}