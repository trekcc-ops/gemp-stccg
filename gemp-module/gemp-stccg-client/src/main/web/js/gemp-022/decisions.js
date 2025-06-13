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
    noPass; // boolean
    displayedCards;
    min; // smallest number of elements that can be selected
    max; // largest number of elements that can be selected
    selectableCardIds;
    cardIds;
    jsonCombinations;

    constructor(decisionJson, gameUi) {
        this.decisionType = decisionJson.decisionType;
        this.gameUi = gameUi;
        this.decisionId = decisionJson.decisionId;
        this.allCardIds = new Array();
        this.decisionText = decisionJson.text;
        this.selectedElementIds = new Array();

        if (this.decisionType === "CARD_ACTION_CHOICE") {
            this.noPass = decisionJson.noPass;
            this.displayedCards = decisionJson.displayedCards;
        } else if (this.decisionType === "ACTION_CHOICE") {
            this.noPass = true;
            this.displayedCards = decisionJson.displayedCards;
        } else if (this.decisionType === "ARBITRARY_CARDS") {
            this.min = parseInt(decisionJson.min);
            this.max = parseInt(decisionJson.max);
            this.selectableCardIds = new Array();
            this.displayedCards = decisionJson.displayedCards;
        } else if (this.decisionType === "CARD_SELECTION") {
            // no displayedCards
            this.min = parseInt(decisionJson.min);
            this.max = parseInt(decisionJson.max);
            this.allCardIds = decisionJson.cardIds;
        } else if (this.decisionType === "CARD_SELECTION_FROM_COMBINATIONS") {
            this.min = parseInt(decisionJson.min);
            this.max = parseInt(decisionJson.max);
            this.displayedCards = decisionJson.displayedCards;
            this.selectableCardIds = new Array();
            this.allCardIds = decisionJson.cardIds;
            this.jsonCombinations = JSON.parse(decisionJson.validCombinations);
            this.decisionText = "Select " + this.min + " to " + this.max + " cards";
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
                this.gameUi.cardActionDialog
                    .html("<div id='arbitraryChoice'></div>")
                    .dialog("option", "title", this.decisionText);
                this.createSelectableDivs();
                break;
            case "CARD_SELECTION":
                this.gameUi.alertText.html(this.decisionText);
                this.gameUi.alertBox.addClass("alert-box-card-selection");
                break;
            case "CARD_SELECTION_FROM_COMBINATIONS":
                this.gameUi.cardActionDialog
                    .html("<div id='cardSelectionFromCombinations'></div>")
                    .dialog("option", "title", this.decisionText);
                this.createSelectableDivs();
                break;
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
        switch(this.decisionType) {
            case "ACTION_CHOICE":
            case "ARBITRARY_CARDS":
            case "CARD_SELECTION_FROM_COMBINATIONS":
                this.createUiElements();
                this.allowSelection();
                this.goDing();
                this.resizeDialog();
                this.resetFocus();
                break;
            case "CARD_ACTION_CHOICE":
                if (this.displayedCards.length == 0 && this.gameUi.gameSettings.get("autoPass") && !this.gameUi.replayMode) {
                    this.gameUi.decisionFunction(this.decisionId, "");
                } else {
                    this.createUiElements();
                    this.allowSelection();
                    this.goDing();
                    this.resetFocus();
                }
                break;
            case "CARD_SELECTION":
                this.createUiElements();
                this.allowSelection();
                this.goDing();
                this.resetFocus();
                break;
        }
    }

    finishChoice() {
        switch(this.decisionType) {
            case "CARD_ACTION_CHOICE":
                this.gameUi.alertText.html("");
                // ****CCG League****: Border around alert box
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
                this.gameUi.cardActionDialog.dialog("close");
                $("#arbitraryChoice").html("");
                this.gameUi.clearSelection();
                break;
            case "CARD_SELECTION":
                this.gameUi.alertText.html("");
                this.gameUi.alertBox.removeClass("alert-box-card-selection");
                this.gameUi.alertButtons.html("");
                this.gameUi.clearSelection();
                break;
            case "CARD_SELECTION_FROM_COMBINATIONS":
                this.gameUi.cardActionDialog.dialog("close");
                $("#cardSelectionFromCombinations").html("");
                this.gameUi.clearSelection();
                break;
        }

        this.gameUi.decisionFunction(this.decisionId, "" + this.selectedElementIds);
    }

    processButtons() {
        var that = this;
        if (this.decisionType === "CARD_ACTION_CHOICE") {
            this.gameUi.alertButtons.html("");
            if (!this.noPass && this.selectedElementIds.length == 0) {
                this.gameUi.alertButtons.append("<button id='Pass'>Pass</button>");
                $("#Pass").button().click(function () {
                    that.finishChoice();
                });
            }
            if (this.selectedElementIds.length > 0) {
                that.gameUi.alertButtons.append("<button id='ClearSelection'>Reset choice</button>");
                this.gameUi.alertButtons.append("<button id='Done'>Done</button>");
                $("#Done").button().click(function () {
                    that.finishChoice();
                });
                $("#ClearSelection").button().click(function () {
                    that.resetChoice();
                });
            }
        } else if (this.decisionType === "ACTION_CHOICE") {
            let buttons = {};
            if (this.selectedElementIds.length > 0) {
                buttons["Clear selection"] = function () {
                    that.resetChoice();
                };
                buttons["Done"] = function () {
                    that.finishChoice();
                };
            }
            this.gameUi.cardActionDialog.dialog("option", "buttons", buttons);
        } else if (this.decisionType === "ARBITRARY_CARDS") {
            let buttons = {};
            if ((this.allCardIds.length <= this.max) &&
                (this.selectedElementIds.length != this.max)) {
                buttons["Select all"] = function() {
                    that.selectedElementIds = Array.from(that.selectableCardIds);
                    that.gameUi.recalculateCardSelectionOrder(that.selectedElementIds);
                    that.gameUi.recalculateAllowedSelectionFromMaxCSS(that.selectableCardIds, that.selectedElementIds, that.max);
                    that.allowSelection();
                    that.selectedElementIds = Array.from(that.selectableCardIds);
                    that.gameUi.recalculateCardSelectionOrder(that.selectedElementIds);
                    that.gameUi.recalculateAllowedSelectionFromMaxCSS(that.selectableCardIds, that.selectedElementIds, that.max);
                    that.allowSelection();
                }
            }
            if (this.selectedElementIds.length > 0) {
                buttons["Clear selection"] = function () {
                    that.resetChoice();
                    that.processButtons();
                };
            }

            if (this.selectedElementIds.length >= this.min) {
                buttons["Done"] = function () {
                    that.finishChoice();
                };
            }
            this.gameUi.cardActionDialog.dialog("option", "buttons", buttons);
        } else if (this.decisionType === "CARD_SELECTION") {
            this.gameUi.alertButtons.html("");
            if (this.selectedElementIds.length > 0) {
                this.gameUi.alertButtons.append("<button id='ClearSelection'>Reset choice</button>");
                $("#ClearSelection").button().click(function () {
                    that.resetChoice();
                });
            }
            if (this.selectedElementIds.length >= this.min) {
                this.gameUi.alertButtons.append("<button id='Done'>Done</button>");
                $("#Done").button().click(function () {
                    that.finishChoice();
                });
            }
        } else if (this.decisionType === "CARD_SELECTION_FROM_COMBINATIONS") {
            let buttons = {};
            if (this.selectedElementIds.length > 0) {
                buttons["Clear selection"] = function () {
                    that.resetChoice();
                    that.processButtons();
                };
            }
            if ((this.selectedElementIds.length >= min) &&
                (this.selectedElementIds.length <= max)) {
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

    allowSelection() {
        var that = this;
        if (this.decisionType === "CARD_ACTION_CHOICE") {
            this.createSelectableDivs();
            this.gameUi.hand.layoutCards();

            this.gameUi.selectionFunction = function (cardId, event) {
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
            };

            this.attachSelectionFunctions(this.allCardIds, false);
        } else if (this.decisionType === "ACTION_CHOICE") {
            this.gameUi.selectionFunction = function (cardId) {
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
            };

            this.attachSelectionFunctions(this.allCardIds, true);
        } else if (this.decisionType === "ARBITRARY_CARDS") {
            // this.selectionFunction is called when a card is clicked
            //   thanks to the code in clickCardFunction()
            this.gameUi.selectionFunction = function (cardId) {
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
            };

            this.attachSelectionFunctions(this.selectableCardIds, true);
        } else if (this.decisionType === "CARD_SELECTION") {
            // this.selectionFunction is called when a card is clicked
            //   thanks to the code in clickCardFunction()
            this.gameUi.selectionFunction = function (cardId) {
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
            };

            this.attachSelectionFunctions(this.allCardIds, true);
        } else if (this.decisionType === "CARD_SELECTION_FROM_COMBINATIONS") {
            // this.selectionFunction is called when a card is clicked
            //   thanks to the code in clickCardFunction()
            this.gameUi.selectionFunction = function (cardId) {
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
            };

            this.attachSelectionFunctions(this.selectableCardIds, true);
        }

        this.processButtons();
    }

    createSelectableDivs() {
        if (this.decisionType === "CARD_ACTION_CHOICE") {
            for (let i = 0; i < this.displayedCards.length; i++) {
                let selectableCard = this.displayedCards[i];
                let actionId = selectableCard.actionId;
                let actionText = selectableCard.actionText;
                let blueprintId = selectableCard.blueprintId;
                let cardId = selectableCard.cardId;
                let actionType = selectableCard.actionType;
                let cardIdElem;

                if (blueprintId === "inPlay") {
                    // in play, do not add "extra" to card id value when doing lookup
                    cardIdElem = getCardDivFromId(cardId);
                    this.allCardIds.push(cardId);
                } else {
                    // not in play, need to add "extra" to card id value when doing lookup
                    cardIdElem = getCardDivFromId(`extra${cardId}`);
                    this.allCardIds.push(`extra${cardId}`);
                    let zone = "EXTRA";
                    // No new cardId - interesting that it's going to retrieve an extra{cardId} but
                    //                 create one with a regular cardId. Intentional?
                    let imageUrl = selectableCard.imageUrl;
                    let noOwner = "";
                    let noLocationIndex = "";
                    let upsideDown = false;
                    let card = new Card(blueprintId, zone, cardId, noOwner, imageUrl, noLocationIndex, upsideDown);

                    let cardDiv = this.gameUi.createCardDivWithData(card);
                    $(cardDiv).css({opacity: "0.8"});

                    $("#main").append(cardDiv);
                }

                if (cardIdElem.data("action") == null) {
                    cardIdElem.data("action", new Array());
                }
                let actions = cardIdElem.data("action");
                actions.push({actionId: actionId, actionText: actionText, actionType: actionType});
            }
        } else if (this.decisionType === "ACTION_CHOICE") {
            for (let i = 0; i < this.displayedCards.length; i++) {
                let displayedCard = this.displayedCards[i];
                let blueprintId = displayedCard.blueprintId;
                let zone = "SPECIAL";
                let cardId = `temp${i}`;
                let noOwner = "";
                let imageUrl = displayedCard.imageUrl;
                let noLocationIndex = "";
                let upsideDown = false;

                this.allCardIds.push("temp" + i);
                let card = new Card(blueprintId, zone, cardId, noOwner, imageUrl, noLocationIndex, upsideDown);

                let cardDiv = this.gameUi.createCardDivWithData(card, displayedCard.actionText);

                $("#arbitraryChoice").append(cardDiv);
            }
        } else if (this.decisionType === "ARBITRARY_CARDS") {
            // Create the action cards and fill the dialog with them
            for (let i = 0; i < this.displayedCards.length; i++) {
                let selectableCard = this.displayedCards[i];
                let cardId = selectableCard.cardId;
                let blueprintId = selectableCard.blueprintId;
                let zone = "SPECIAL";
                let noOwner = "";
                let imageUrl = selectableCard.imageUrl;
                let emptyLocationIndex = "";
                let upsideDown = false;

                if (selectableCard.selectable === "true") {
                    this.selectableCardIds.push(cardId);
                }
                this.allCardIds.push(cardId);
                let card = new Card(blueprintId, zone, cardId, noOwner, imageUrl, emptyLocationIndex, upsideDown);

                let cardDiv = this.gameUi.createCardDivWithData(card);

                $("#arbitraryChoice").append(cardDiv);
            }
        } else if (this.decisionType === "CARD_SELECTION_FROM_COMBINATIONS") {
            // Create the action cards and fill the dialog with them
            for (let i = 0; i < this.displayedCards.length; i++) {
                let displayedCard = this.displayedCards[i];

                let blueprintId = displayedCard.blueprintId;
                let zone ="SPECIAL";
                let cardId = displayedCard.cardId;
                let noOwner = "";
                let emptyLocationIndex = "";
                let upsideDown = false;

                let imageUrl = displayedCard.imageUrl;

                if (displayedCard.selectable == "true") {
                    this.selectableCardIds.push(cardId);
                }

                let card = new Card(blueprintId, zone, cardId, noOwner, imageUrl, emptyLocationIndex, upsideDown);

                let cardDiv = this.createCardDivWithData(card);

                $("#cardSelectionFromCombinations").append(cardDiv);
            }
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

        this.smallDialog.dialog("open");
        $('.ui-dialog :button').blur();
    }