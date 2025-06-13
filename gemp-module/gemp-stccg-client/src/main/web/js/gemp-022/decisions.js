import Card from './jCards.js';
import { getCardDivFromId } from './jCards.js';
import { openSizeDialog } from "./common.js";


export default class gameDecision {

    gameUi;
    decisionType;
    decisionId;
    decisionText;
    selectedElementIds;
    allCardIds;
    displayedCards;
    min; // smallest number of elements that can be selected
    max; // largest number of elements that can be selected
    selectableCardIds;
    cardIds;
    jsonCombinations;
    useDialog;
    elementType;

    constructor(decisionJson, gameUi) {
        this.decisionType = decisionJson.decisionType;
        this.gameUi = gameUi;
        this.decisionId = decisionJson.decisionId;
        this.allCardIds = new Array();
        this.decisionText = decisionJson.text;
        this.selectedElementIds = new Array();

        if (this.decisionType === "CARD_ACTION_CHOICE") {
            this.min = decisionJson.noPass ? 1 : 0;
            this.max = 1;
            this.displayedCards = decisionJson.displayedCards;
            this.useDialog = false;
            this.elementType = "ACTION";
        } else if (this.decisionType === "ACTION_CHOICE") {
            this.min = 1;
            this.max = 1;
            this.displayedCards = decisionJson.displayedCards;
            this.useDialog = true;
            this.elementType = "ACTION";
        } else if (this.decisionType === "ARBITRARY_CARDS") {
            this.min = parseInt(decisionJson.min);
            this.max = parseInt(decisionJson.max);
            this.selectableCardIds = new Array();
            this.displayedCards = decisionJson.displayedCards;
            this.useDialog = true;
            this.elementType = "CARD";
        } else if (this.decisionType === "CARD_SELECTION") {
            // no displayedCards
            this.min = parseInt(decisionJson.min);
            this.max = parseInt(decisionJson.max);
            this.allCardIds = decisionJson.cardIds;
            this.useDialog = false;
            this.elementType = "CARD";
        } else if (this.decisionType === "CARD_SELECTION_FROM_COMBINATIONS") {
            this.min = parseInt(decisionJson.min);
            this.max = parseInt(decisionJson.max);
            this.displayedCards = decisionJson.displayedCards;
            this.selectableCardIds = new Array();
            this.allCardIds = decisionJson.cardIds;
            this.jsonCombinations = JSON.parse(decisionJson.validCombinations);
            this.decisionText = "Select " + this.min + " to " + this.max + " cards";
            this.useDialog = true;
            this.elementType = "CARD";
        }
    }

    createUiElements() {
        switch(this.decisionType) {
            case "CARD_ACTION_CHOICE":
                this.gameUi.alertText.html(this.decisionText);
                this.gameUi.alertBox.addClass("alert-box-highlight");
                break;
            case "ACTION_CHOICE":
            case "ARBITRARY_CARDS":
            case "CARD_SELECTION_FROM_COMBINATIONS":
                this.gameUi.cardActionDialog
                    .html("<div id='cardSelectionDialog'></div>")
                    .dialog("option", "title", this.decisionText);
                break;
            case "CARD_SELECTION":
                this.gameUi.alertText.html(this.decisionText);
                this.gameUi.alertBox.addClass("alert-box-card-selection");
                break;
        }

        if (this.decisionType != "CARD_SELECTION") {
            this.createSelectableDivs();
        }
    }

    goDing() {
        if (!this.gameUi.replayMode) {
            this.gameUi.PlayAwaitActionSound();
        }
    }

    resizeDialog() {
        openSizeDialog(this.gameUi.cardActionDialog);
        this.gameUi.arbitraryDialogResize(false);
    }

    resetFocus() {
        switch(this.decisionType) {
            case "CARD_ACTION_CHOICE":
                $(':button').blur();
                break;
            case "ACTION_CHOICE":
            case "ARBITRARY_CARDS":
            case "CARD_SELECTION_FROM_COMBINATIONS":
                $('.ui-dialog :button').blur();
                break;
            case "CARD_SELECTION":
                break; // Not sure why this one doesn't do anything
        }
    }

    processDecision() {
        if (this.decisionType === "CARD_ACTION_CHOICE" && this.displayedCards.length == 0 && this.gameUi.gameSettings.get("autoPass") && !this.gameUi.replayMode) {
            this.gameUi.decisionFunction(this.decisionId, "");
        } else {
            this.createUiElements();
            this.allowSelection();
            this.goDing();
            if (this.useDialog) {
                this.resizeDialog();
            }
            this.resetFocus();
        }
    }

    finishChoice() {
        switch(this.decisionType) {
            case "CARD_ACTION_CHOICE":
                this.gameUi.alertText.html("");
                this.gameUi.alertBox.removeClass("alert-box-highlight");
                this.gameUi.alertButtons.html("");
                this.gameUi.clearSelection();
                $(".card").each(
                    function () {
                        var card = $(this).data("card");
                        if (card.zone == "EXTRA") {
                            $(this).remove();
                        }
                    });
                this.gameUi.hand.layoutCards();
                break;
            case "ACTION_CHOICE":
            case "ARBITRARY_CARDS":
            case "CARD_SELECTION_FROM_COMBINATIONS":
                this.gameUi.cardActionDialog.dialog("close");
                $("#cardSelectionDialog").html("");
                this.gameUi.clearSelection();
                break;
            case "CARD_SELECTION":
                this.gameUi.alertText.html("");
                this.gameUi.alertBox.removeClass("alert-box-card-selection");
                this.gameUi.alertButtons.html("");
                this.gameUi.clearSelection();
                break;
        }

        this.gameUi.decisionFunction(this.decisionId, "" + this.selectedElementIds);
    }

    processButtons() {
        var that = this;

        switch(this.decisionType) {
            case "CARD_ACTION_CHOICE":
            case "CARD_SELECTION":
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
                break;
            case "ACTION_CHOICE":
            case "ARBITRARY_CARDS":
            case "CARD_SELECTION_FROM_COMBINATIONS":
                let buttons = {};
                if (this.allCardIds.length <= this.max && this.selectedElementIds.length != this.max) {
                    buttons["Select all"] = function() {
                        that.selectedElementIds = Array.from(that.selectableCardIds);
                        if (this.decisionType === "ARBITRARY_CARDS") {
                            that.gameUi.recalculateCardSelectionOrder(that.selectedElementIds);
                            that.gameUi.recalculateAllowedSelectionFromMaxCSS(that.selectableCardIds, that.selectedElementIds, that.max);
                        }
                        that.allowSelection();
                    }
                }
                if (this.selectedElementIds.length > 0) {
                    buttons["Clear selection"] = function () {
                        that.resetChoice();
                    };
                }
                if (this.selectedElementIds.length >= this.min) && (this.selectedElementIds.length <= this.max) {
                    buttons["Done"] = function () {
                        that.finishChoice();
                    };
                }
                this.gameUi.cardActionDialog.dialog("option", "buttons", buttons);
                break;
        }
    }

    resetChoice() {
        this.selectedElementIds = new Array();
        this.gameUi.clearSelection();
        if (this.decisionType === "CARD_ACTION_CHOICE") {
            // Selecting cards with this decision removes all the divs, so they need to be re-created
            this.createSelectableDivs();
        }
        this.allowSelection();
        this.processButtons();
    }

    attachSelectionFunctions(cardIds, selection) {
        if (selection) {
            if (cardIds.length > 0) {
                $(".card:cardId(" + cardIds + ")").addClass("selectableCard");
            }
        } else {
            if (cardIds.length > 0) {
                $(".card:cardId(" + cardIds + ")").addClass("actionableCard");
            }
        }
    }

    respondToCardSelection(cardId, event) {
        var that = this;
        switch(this.decisionType) {
            case "ACTION_CHOICE":
                // DEBUG: console.log("actionChoiceDecision -> allowSelection -> selectionFunction");
                let actionId = that.displayedCards[parseInt(cardId.substring(4))].actionId;
                that.selectedElementIds.push(actionId);
                that.gameUi.clearSelection();
                if (that.gameUi.gameSettings.get("autoAccept")) {
                    that.finishChoice();
                } else {
                    that.processButtons();
                    getCardDivFromId(cardId).addClass("selectedCard");
                }
                break;
            case "ARBITRARY_CARDS":
                // DEBUG: console.log("arbitraryCardsDecision -> allowSelection -> selectionFunction");
                // If the cardId is already selected, remove it.
                if (that.selectedElementIds.includes(cardId)) {
                    let index = that.selectedElementIds.indexOf(cardId);
                    that.selectedElementIds.splice(index, 1);
                }
                // Otherwise, if the cardId is not already selected, add it.
                else {
                    that.selectedElementIds.push(cardId);
                }

                that.gameUi.recalculateCardSelectionOrder(that.selectedElementIds);
                that.gameUi.recalculateAllowedSelectionFromMaxCSS(that.selectableCardIds, that.selectedElementIds, that.max);

                that.processButtons();
                break;
            case "CARD_SELECTION":
                // DEBUG: console.log("cardSelectionDecision -> allowSelection -> selectionFunction");
                // If the cardId is already selected, remove it.
                if (that.selectedElementIds.includes(cardId)) {
                    let index = that.selectedElementIds.indexOf(cardId);
                    that.selectedElementIds.splice(index, 1);
                    getCardDivFromId(cardId).removeClass("selectedCard").addClass("selectableCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                }
                // Otherwise, if the cardId is not already selected, add it.
                else {
                    that.selectedElementIds.push(cardId);
                    getCardDivFromId(cardId).removeClass("selectableCard").addClass("selectedCard").addClass("selectedBadge");
                }

                that.gameUi.recalculateCardSelectionOrder(that.selectedElementIds);

                // If the max number of cards are selected and the user has auto accept on, we're done.
                if ((that.selectedElementIds.length == that.max) && (that.gameSettings.get("autoAccept"))) {
                    that.finishChoice();
                    return;
                }

                that.processButtons();
                break;
            case "CARD_SELECTION_FROM_COMBINATIONS":
                // DEBUG: console.log("arbitraryCardsDecision -> allowSelection -> selectionFunction");
                // If the cardId is already selected, remove it.
                if (that.selectedElementIds.includes(cardId)) {
                    let index = that.selectedElementIds.indexOf(cardId);
                    that.selectedElementIds.splice(index, 1);
                }
                // Otherwise, if the cardId is not already selected, add it.
                else {
                    that.selectedElementIds.push(cardId);
                }

                that.gameUi.recalculateCardSelectionOrder(that.selectedElementIds);
                that.gameUi.recalculateAllowedCombinationsAndCSS(that.allCardIds, that.selectedElementIds, that.jsonCombinations, that.max);

                that.processButtons();
                break;
            case "CARD_ACTION_CHOICE":
                // DEBUG: console.log("cardActionChoiceDecision -> allowSelection -> selectionFunction");
                let cardIdElem = getCardDivFromId(cardId);
                let actions = cardIdElem.data("action");
                let selectActionFunction = function (actionId) {
                    that.selectedElementIds.push(actionId);
                    if (that.gameUi.gameSettings.get("autoAccept")) {
                       that. finishChoice();
                    } else {
                        that.gameUi.clearSelection();
                        getCardDivFromId(cardId).addClass("selectedCard");
                        that.processButtons();
                    }
                };

                // If the only legal action is a card play, perform action automatically by clicking
                // Otherwise show a drop-down menu with the action options by clicking
                if (actions.length == 1 &&
                        (actions[0].actionType == "PLAY_CARD" || actions[0].actionType == "SEED_CARD")) {
                    selectActionFunction(actions[0].actionId);
                } else {
                        // TODO - Bind to right-click?
                    this.gameUi.createActionChoiceContextMenu(actions, event, selectActionFunction);
                }
                break;
        }
    }

    allowSelection() {
        var that = this;

        // gameUi.selectionFunction is called when a card is clicked
        //   thanks to the code in gameUi.clickCardFunction()
        this.gameUi.selectionFunction = function (cardId, event) {
            that.respondToCardSelection(cardId, event);
        };

        switch(this.decisionType) {
            case "CARD_ACTION_CHOICE":
                this.attachSelectionFunctions(this.allCardIds, false);
                break;
            case "ACTION_CHOICE":
            case "CARD_SELECTION":
                this.attachSelectionFunctions(this.allCardIds, true);
                break;
            case "ARBITRARY_CARDS":
            case "CARD_SELECTION_FROM_COMBINATIONS":
                this.attachSelectionFunctions(this.selectableCardIds, true);
                break;
        }

        this.processButtons();
    }

    createSelectableDivs() {
        for (let i = 0; i < this.displayedCards.length; i++) {
            // Create the cards and fill the dialog with them
            let displayedCard = this.displayedCards[i];
            let blueprintId = displayedCard.blueprintId;
            let imageUrl = displayedCard.imageUrl;
            let zone = "SPECIAL";
            let noOwner = "";
            let noLocationIndex = "";
            let upsideDown = false;
            let cardId;
            let card;
            let cardDiv;

            switch(this.decisionType) {
                case "ACTION_CHOICE":
                    cardId = "temp" + i;

                    this.allCardIds.push(cardId);
                    card = new Card(blueprintId, zone, cardId, noOwner, imageUrl, noLocationIndex, upsideDown);

                    cardDiv = this.gameUi.createCardDivWithData(card, displayedCard.actionText);
                    break;
                case "ARBITRARY_CARDS":
                case "CARD_SELECTION_FROM_COMBINATIONS":
                    cardId = displayedCard.cardId;

                    if (displayedCard.selectable === "true") {
                        this.selectableCardIds.push(cardId);
                    }
                    this.allCardIds.push(cardId);
                    card = new Card(blueprintId, zone, cardId, noOwner, imageUrl, noLocationIndex, upsideDown);

                    cardDiv = this.gameUi.createCardDivWithData(card);
                    break;
                case "CARD_ACTION_CHOICE":
                    let actionId = displayedCard.actionId;
                    let actionText = displayedCard.actionText;
                    cardId = displayedCard.cardId;
                    let actionType = displayedCard.actionType;
                    let cardIdElem;

                    if (blueprintId === "inPlay") {
                        // in play, do not add "extra" to card id value when doing lookup
                        cardIdElem = getCardDivFromId(cardId);
                        this.allCardIds.push(cardId);
                    } else {
                        // not in play, need to add "extra" to card id value when doing lookup
                        cardIdElem = getCardDivFromId(`extra${cardId}`);
                        this.allCardIds.push(`extra${cardId}`);
                        zone = "EXTRA";
                        // No new cardId - interesting that it's going to retrieve an extra{cardId} but
                        //                 create one with a regular cardId. Intentional?
                        card = new Card(blueprintId, zone, cardId, noOwner, imageUrl, noLocationIndex, upsideDown);

                        cardDiv = this.gameUi.createCardDivWithData(card);
                        $(cardDiv).css({opacity: "0.8"});

                        $("#main").append(cardDiv);
                    }

                    if (cardIdElem.data("action") == null) {
                        cardIdElem.data("action", new Array());
                    }
                    let actions = cardIdElem.data("action");
                    actions.push({actionId: actionId, actionText: actionText, actionType: actionType});
                case "CARD_SELECTION":
                    break;
            }

            if (this.useDialog) {
                $("#cardSelectionDialog").append(cardDiv);
            }
        }

        if (this.decisionType === "CARD_ACTION_CHOICE") {
            this.gameUi.hand.layoutCards();
        }
    }
}

export function processDecision(decision, animate, gameUi) {
    $("#main").queue(
        function (next) {
            let decisionType = decision.decisionType;
            switch(decisionType) {
                case "ACTION_CHOICE":
                case "ARBITRARY_CARDS":
                case "CARD_ACTION_CHOICE":
                case "CARD_SELECTION":
                case "CARD_SELECTION_FROM_COMBINATIONS":
                    let decisionObject = new gameDecision(decision, gameUi);
                    decisionObject.processDecision();
                    break;
                case "INTEGER":
                    integerDecision(decision, gameUi);
                    break;
                case "MULTIPLE_CHOICE":
                    multipleChoiceDecision(decision, gameUi);
                    break;
                default:
                    console.error(`Unknown decisionType: ${decisionType}`);
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
                gameUi.PlayAwaitActionSound();
            }
        }

        gameUi.smallDialog.dialog("open");
        $('.ui-dialog :button').blur();
    }