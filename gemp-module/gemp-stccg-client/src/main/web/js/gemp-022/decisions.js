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
        }
    }

    processDecision() {
        if (this.decisionType === "CARD_ACTION_CHOICE") {
            if (this.displayedCards.length == 0 && this.gameUi.gameSettings.get("autoPass") && !this.gameUi.replayMode) {
                this.gameUi.decisionFunction(this.decisionId, "");
            } else {
                this.gameUi.alertText.html(this.decisionText);
                // ****CCG League****: Border around alert box
                this.gameUi.alertBox.addClass("alert-box-highlight");
                this.allowSelection();
                if (!this.gameUi.replayMode) {
                    this.processButtons();
                    this.gameUi.PlayAwaitActionSound();
                }
                $(':button').blur();
            }
        } else if (this.decisionType === "ACTION_CHOICE") {

            this.gameUi.cardActionDialog
                .html("<div id='arbitraryChoice'></div>")
                .dialog("option", "title", this.decisionText);
            this.createSelectableDivs();
            this.allowSelection();
            if (!this.gameUi.replayMode) {
                this.processButtons();
                this.gameUi.PlayAwaitActionSound();
            }

            openSizeDialog(this.gameUi.cardActionDialog);
            this.gameUi.arbitraryDialogResize(false);
            $('.ui-dialog :button').blur();
        } else if (this.decisionType === "ARBITRARY_CARDS") {
            // Selecting cards from a dialog box
    
            this.gameUi.cardActionDialog
                .html("<div id='arbitraryChoice'></div>")
                .dialog("option", "title", this.decisionText);
            this.createSelectableDivs();
    
            this.allowSelection();
            if (!this.gameUi.replayMode) {
                this.processButtons();
                this.gameUi.PlayAwaitActionSound();
            }
    
            openSizeDialog(this.gameUi.cardActionDialog);
            this.gameUi.arbitraryDialogResize(false);
            $('.ui-dialog :button').blur();
        } else if (this.decisionType === "CARD_SELECTION") {

            this.gameUi.alertText.html(this.decisionText);
            this.gameUi.alertBox.addClass("alert-box-card-selection");

            this.allowSelection();
            if (!this.replayMode) {
                this.processButtons();
                this.gameUi.PlayAwaitActionSound();
            }
        } else if (this.decisionType === "CARD_SELECTION_FROM_COMBINATIONS") {

            this.gameUi.cardActionDialog
                .html("<div id='cardSelectionFromCombinations'></div>")
                .dialog("option", "title", `Select ${min} to ${max} cards`);

            this.createSelectableDivs();

            this.allowSelection();
            if (!this.replayMode) {
                this.processButtons();
                this.gameUi.PlayAwaitActionSound();
            }

            openSizeDialog(this.cardActionDialog);
            this.arbitraryDialogResize(false);
            $('.ui-dialog :button').blur();
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
                this.gameUi.decisionFunction(this.decisionId, "" + this.selectedElementIds);
                break;
            case "ACTION_CHOICE":
            case "ARBITRARY_CARDS":
                this.gameUi.cardActionDialog.dialog("close");
                $("#arbitraryChoice").html("");
                this.gameUi.clearSelection();
                this.gameUi.decisionFunction(this.decisionId, "" + this.selectedElementIds);
                break;
            case "CARD_SELECTION":
                this.gameUi.alertText.html("");
                this.gameUi.alertBox.removeClass("alert-box-card-selection");
                this.gameUi.alertButtons.html("");
                this.gameUi.clearSelection();
                this.gameUi.decisionFunction(this.decisionId, "" + this.selectedElementIds);
                break;
            case "CARD_SELECTION_FROM_COMBINATIONS":
                this.gameUi.cardActionDialog.dialog("close");
                $("#cardSelectionFromCombinations").html("");
                this.gameUi.clearSelection();
                this.gameUi.decisionFunction(this.decisionId, "" + this.selectedElementIds);
                break;
        }
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
                    that.processButtons();
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
                    that.processButtons();
                    this.selectedElementIds = Array.from(this.selectableCardIds);
                    that.gameUi.recalculateCardSelectionOrder(this.selectedElementIds);
                    that.gameUi.recalculateAllowedSelectionFromMaxCSS(this.selectableCardIds, this.selectedElementIds, this.max);
                    this.allowSelection();
                    this.processButtons();
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
                    gameUi.integerDecision(decision);
                    break;
                case "MULTIPLE_CHOICE":
                    gameUi.multipleChoiceDecision(decision);
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