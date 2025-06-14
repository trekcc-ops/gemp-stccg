import Card from './jCards.js';
import { getCardDivFromId } from './jCards.js';
import { openSizeDialog } from "./common.js";


export default class gameDecision {

    gameUi; // GameTableUI object
    decisionType; // string; this is being deprecated
    decisionId; // integer; unique identifier for decision object in server
    decisionText; // string; prompt to display to user (for example, "Select a card to discard")
    elementType; // string representing the type of object that's being selected (ACTION or CARD)

    displayedCards; // JSON
    allCardIds; // string array
    selectableCardIds; // string array
    selectedElementIds; // string array

    min; // integer; smallest number of elements that can be selected
    max; // integer; largest number of elements that can be selected
    useDialog; // boolean; if true, cards will be shown in a pop-up dialog
    jsonCombinations; // JSON
    trackSelectionOrder; // boolean; if true, decision UI shows what order cards were selected in using badges

    constructor(decisionJson, gameUi) {
        this.decisionType = decisionJson.decisionType;
        this.gameUi = gameUi;
        this.decisionId = decisionJson.decisionId;
        this.allCardIds = decisionJson.cardIds;
        this.decisionText = decisionJson.text;
        this.selectedElementIds = new Array();
        this.min = decisionJson.min;
        this.max = decisionJson.max;
        this.elementType = decisionJson.elementType;

        this.trackSelectionOrder = (this.decisionType === "CARD_SELECTION");

        if (this.decisionType != "CARD_SELECTION") {
            this.displayedCards = decisionJson.displayedCards;
        }

        if (decisionJson.elementType === "ACTION") {
            this.selectableCardIds = decisionJson.cardIds;
            this.useDialog = (decisionJson.context === "SELECT_REQUIRED_RESPONSE_ACTION");
        }

        if (this.decisionType === "ARBITRARY_CARDS") {
            this.selectableCardIds = new Array();
            this.useDialog = true;
            for (let i = 0; i < this.displayedCards.length; i++) {
                if (displayedCard.selectable === "true") {
                    this.selectableCardIds.push(cardId);
                }
            }
        } else if (this.decisionType === "CARD_SELECTION") {
            this.selectableCardIds = decisionJson.cardIds;
            this.useDialog = false;
        } else if (this.decisionType === "CARD_SELECTION_FROM_COMBINATIONS") {
            this.selectableCardIds = new Array();
            this.jsonCombinations = JSON.parse(decisionJson.validCombinations);
            this.decisionText = "Select " + this.min + " to " + this.max + " cards";
            this.useDialog = true;
            for (let i = 0; i < this.displayedCards.length; i++) {
                if (displayedCard.selectable === "true") {
                    this.selectableCardIds.push(cardId);
                }
            }
        }
    }

    createUiElements() {
        if (this.useDialog) {
            this.gameUi.cardActionDialog
                .html("<div id='cardSelectionDialog'></div>")
                .dialog("option", "title", this.decisionText);
            break;
        } else {
            this.gameUi.alertText.html(this.decisionText);
            let alertBoxClass = this.elementType === "ACTION" ? "alert-box-highlight" : "alert-box-card-selection";
            this.gameUi.alertBox.addClass(alertBoxClass);
        }
        if (this.decisionType != "CARD_SELECTION") {
            this.createSelectableDivs();
        }
        if (this.elementType === "ACTION" && !this.useDialog) {
            this.gameUi.hand.layoutCards();
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
        $(':button').blur();
        if (this.useDialog) {
            $('.ui-dialog').blur();
        }
    }

    processDecision() {
        if (this.elementType === "ACTION" && this.displayedCards.length == 0 && this.gameUi.gameSettings.get("autoPass") && !this.gameUi.replayMode) {
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

        this.gameUi.decisionFunction(this.decisionId, "" + this.selectedElementIds);
    }

    processButtons() {
        var that = this;

        if (!useDialog) {
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
            let selectActionFunction = function (actionId) {
                that.selectedElementIds.push(actionId);
                if (that.gameUi.gameSettings.get("autoAccept")) {
                   that.finishChoice();
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
        }

        if (this.elementType === "CARD") {
            // If the cardId is already selected, remove it.
            if (this.selectedElementIds.includes(cardId)) {
                let index = this.selectedElementIds.indexOf(cardId);
                this.selectedElementIds.splice(index, 1);
                if (this.trackSelectionOrder) {
                    getCardDivFromId(cardId).removeClass("selectedCard").addClass("selectableCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                }
            }
            // Otherwise, if the cardId is not already selected, add it.
            else {
                that.selectedElementIds.push(cardId);
                if (this.trackSelectionOrder) {
                    getCardDivFromId(cardId).removeClass("selectableCard").addClass("selectedCard").addClass("selectedBadge");
                }
            }

            that.gameUi.recalculateCardSelectionOrder(that.selectedElementIds);

            if (this.useDialog) {
                if (this.decisionType === "ARBITRARY_CARDS") {
                    that.gameUi.recalculateAllowedSelectionFromMaxCSS(that.selectableCardIds, that.selectedElementIds, that.max);
                } else if (this.decisionType === "SELECT_CARDS_FROM_COMBINATIONS") {
                    that.gameUi.recalculateAllowedCombinationsAndCSS(that.allCardIds, that.selectedElementIds, that.jsonCombinations, that.max);
                }
            } else {
                // If the max number of cards are selected and the user has auto accept on, we're done.
                if ((that.selectedElementIds.length == that.max) && (that.gameSettings.get("autoAccept"))) {
                    that.finishChoice();
                    return;
                }
            }

            that.processButtons();
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
        for (let i = 0; i < this.displayedCards.length; i++) {
            // Create the cards and fill the dialog with them
            let displayedCard = this.displayedCards[i];
            let cardId = displayedCard.cardId; // for selections using the dialog, this is always a "temp" value

            // For action selections from visible cards, each relevant card is associated with a list of its
            //      available actions
            if (this.elementType === "ACTION" && !this.useDialog) {
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

            if (this.useDialog) {
                let blueprintId = displayedCard.blueprintId;
                let imageUrl = displayedCard.imageUrl;
                let blueprintId = displayedCard.blueprintId;
                let imageUrl = displayedCard.imageUrl;
                let zone = "SPECIAL";
                let noOwner = "";
                let noLocationIndex = "";
                let upsideDown = false;
                let card = new Card(blueprintId, zone, cardId, noOwner, imageUrl, noLocationIndex, upsideDown);
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
}

export function processDecision(decision, animate, gameUi) {
    $("#main").queue(
        function (next) {
            let elementType = decision.elementType;
            switch(elementType) {
                case "ACTION":
                case "CARD":
                    let decisionObject = new gameDecision(decision, gameUi);
                    decisionObject.processDecision();
                    break;
                case "INTEGER":
                    integerDecision(decision, gameUi);
                    break;
                case "STRING":
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