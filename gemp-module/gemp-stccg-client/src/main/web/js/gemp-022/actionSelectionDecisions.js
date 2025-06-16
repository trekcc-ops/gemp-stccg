import Card from './jCards.js';
import { getCardDivFromId } from './jCards.js';
import { openSizeDialog } from "./common.js";
import awaitingActionAudio from "../../src/assets/awaiting_decision.mp3";

export default class ActionSelectionDecision {

    gameUi; // GameTableUI object
    decisionId; // integer; unique identifier for decision object in server
    elementType; // string representing the type of object that's being selected (ACTION or CARD)

    actions;

    // selected element ids uses a different id scheme for action selection decisions
    selectedElementIds = new Array(); // string array (selected elements; ui ids if cards)

    min; // integer; smallest number of elements that can be selected
    max; // integer; largest number of elements that can be selected
    useDialog; // boolean; if true, cards will be shown in a pop-up dialog

    gameState;

    constructor(decisionJson, gameUi, useDialog, gameState) {
        this.gameUi = gameUi;
        this.decisionId = decisionJson.decisionId;
        this.min = decisionJson.min;
        this.max = decisionJson.max;
        this.elementType = decisionJson.elementType;
        this.useDialog = useDialog;
        this.gameState = gameState;
        this.actions = decisionJson.actions;
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
            let cardIdElem = getCardDivFromId(cardId);
            let actionId = cardIdElem.data("actionId");
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

        this.processButtons();
    }

    createSelectableDivs() {
        // For action selections from visible cards, each relevant card is associated with a list of its
        //      available actions
        if (!this.useDialog) {
            for (let i = 0; i < this.actions.length; i++) {
                let action = this.actions[i];
                let acceptAllMappings = true;
                let cardActionMap = getActionInitiationCardActionMap(action, this.gameState, acceptAllMappings);
                for (const [cardId, actionText] of cardActionMap) {
                    let cardIdElem = getCardDivFromId(cardId);
                    if (cardIdElem.data("action") == null) {
                        cardIdElem.data("action", new Array());
                    }
                    let cardActions = cardIdElem.data("action");
                    cardActions.push({actionId: action.actionId, actionText: actionText, actionType: action.actionType});
                    cardIdElem.addClass("selectableCard");
                }
            }
        } else if (this.useDialog) {
            for (let i = 0; i < this.actions.length; i++) {
                let action = this.actions[i];
                let acceptAllMappings = false;
                let cardActionMap = getActionInitiationCardActionMap(action, this.gameState, acceptAllMappings);
                if (cardActionMap.size > 1) {
                    console.error("Created cardActionMap of incorrect size");
                }
                for (const [cardId, actionText] of cardActionMap) {
                    let decisionUiCardId = "temp" + i.toString();
                    let gameStateCard = this.gameState.visibleCardsInGame[cardId];
                    let blueprintId = gameStateCard.blueprintId;
                    let imageUrl = gameStateCard.imageUrl;
                    let zone = "SPECIAL";
                    let noOwner = "";
                    let noLocationIndex = "";
                    let upsideDown = false;
                    let card = new Card(blueprintId, zone, decisionUiCardId, noOwner, imageUrl, noLocationIndex, upsideDown);
                    let cardDiv = this.gameUi.createCardDivWithData(card, actionText);
                    cardDiv.data("actionId", action.actionId);
                    cardDiv.addClass("selectableCard");
                    $("#cardSelectionDialog").append(cardDiv);
                }
            }
        }
    }

    recalculateCardSelectionOrder() {
        for (const [index, cardId] of this.selectedElementIds.entries()) {
            let divToChange = getCardDivFromId(cardId);
            divToChange.attr("selectedOrder", index + 1); // use a 1-index
        }
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

}

export function getActionInitiationCardActionMap(action, gameState, acceptAllMappings) {
    /* acceptAllMappings - if true, the map returned may have multiple options for each action
       For example, if a card can be played by using another card's gametext:
            acceptAllMappings = true: the action will be mapped to both the card being played and the card whose
                gametext enabled the card play
            acceptAllMappings = false: the action will only be mapped to one of these cards

        [CL] The majority of mappings as of 6/16/25 only have one option.
    */

    let cardActionMap = new Map();
    let actionType = action.actionType;

    // Most action JSON data has one or both of the following.
    let targetCardId = action.targetCardId;
    let performingCardId = action.performingCardId;

    switch(actionType) {
        case "ADD_CARDS_TO_PRESEED_STACK":
            // TODO - Will need additional implementation for this with a card like Empok Nor
            cardActionMap.set(getTopMissionCardIdForLocation(action.locationId), "Add seed cards");
            return cardActionMap;
        case "ADD_MODIFIER":
            // performingCardId for this action represents the card whose gametext adds the modifier
            cardActionMap.set(performingCardId, "Add modifier");
            return cardActionMap;
        case "ATTEMPT_MISSION":
            // targetCardId for this action represents the mission being attempted
            cardActionMap.set(targetCardId, "Attempt mission");
            return cardActionMap;
        case "BATTLE":
            // TODO - Will need additional implementation for this once we have multiple battle options
            cardActionMap.set(getTopMissionCardIdForLocation(action.locationId), "Initiate battle");
            return cardActionMap;
        case "BEAM_CARDS":
            // performingCardId for this action represents the card whose transporters are used
            cardActionMap.set(performingCardId, "Beam cards"); // TODO (Beam/walk cards to/from X)
            return cardActionMap;
        case "CHANGE_AFFILIATION":
            // targetCardId for this action represents the personnel or ship changing affiliation
            cardActionMap.set(targetCardId, "Change affiliation");
            return cardActionMap;
        case "DISCARD":
            // performingCardId for this action represents the card whose gametext enables the discard action
            cardActionMap.set(performingCardId, "Discard");
            return cardActionMap;
        case "DOCK_SHIP":
            // targetCardId for this action represents the ship that is docking
            cardActionMap.set(targetCardId, "Dock");
            return cardActionMap;
        case "DOWNLOAD_CARD":
            // performingCardId for this action represents the card whose gametext enables the download action
            cardActionMap.set(performingCardId, "Download card"); // TODO - May download multiple cards
            return cardActionMap;
        case "DRAW_CARD":
            // performingCardId for this action represents the card whose gametext enables the download action.
            // The end-of-turn normal card draw should not come through here.
            if (performingCardId != null && typeof performingCardId != "undefined") {
                cardActionMap.set(performingCardId, "Draw card(s)"); // TODO - Specify how many cards to draw
            } else {
                console.log("Could not select draw card action with no performingCardId");
            }
            return cardActionMap;
        case "FLY_SHIP":
            // targetCardId for this action represents the ship that is flying
            cardActionMap.set(targetCardId, "Fly ship");
            return cardActionMap;
        case "KILL":
            // performingCardId for this action represents the card whose gametext enables the kill action
                // TODO - Name personnel if personnel's name is known? Also multi-kill action
            cardActionMap.set(performingCardId, "Kill a personnel");
            return cardActionMap;
        case "NULLIFY":
            // performingCardId for this action represents the card that is nullifying another
            cardActionMap.set(performingCardId, "Nullify a card");
            return cardActionMap;
        case "PLAY_CARD":
            /* targetCardId for this action represents the card that is being played. This may not be defined yet
                    for an action where the card to play needs to be selected.
               performingCardId for this action is the card whose gametext is enabling the card play.
               If this is the normal card play (i.e. no card is enabling it because it is allowed by the rules),
                    performingCardId and targetCardId are the same.
            */
            // TODO - Get more specific for a card like Attention All Hands (e.g., "report a personnel for free")
            if (targetCardId != null && typeof targetCardId != "undefined") {
                cardActionMap.set(targetCardId, "Play card");
            }
            if (acceptAllMappings || cardActionMap.size === 0) {
                if (performingCardId != null && typeof performingCardId != "undefined" && performingCardId != targetCardId) {
                    cardActionMap.set(performingCardId, "Play card");
                }
            }
            return cardActionMap;
        case "REMOVE_CARDS_FROM_PRESEED_STACK":
            // TODO - Will need additional implementation for this with a card like Empok Nor
            cardActionMap.set(getTopMissionCardIdForLocation(action.locationId), "Remove seed cards");
            return cardActionMap;
        case "SCORE_POINTS":
            // performingCardId for this action represents the card whose gametext enables the point scoring
            cardActionMap.set(performingCardId, "Score points");
            return cardActionMap;
        case "SEED_CARD":
            // targetCardId for this action represents the card being seeded
            cardActionMap.set(targetCardId, "Seed card");
            return cardActionMap;
        case "SHUFFLE_CARDS_INTO_DRAW_DECK":
            // performingCardId for this action represents the card whose gametext enables the action
            cardActionMap.set(performingCardId, "Shuffle cards into draw deck");
            return cardActionMap;
        case "UNDOCK_SHIP":
            // targetCardId for this action represents the ship that is undocking
            cardActionMap.set(targetCardId, "Undock");
            return cardActionMap;
        case "USE_GAME_TEXT":
            // performingCardId for this action represents the card whose gametext you're using
            if (action.actionText != null && typeof action.actionText != "undefined") {
                cardActionMap.set(performingCardId, action.actionText);
            } else {
                cardActionMap.set(performingCardId, "Use game text");
            }
            return cardActionMap;
        case "WALK_CARDS":
            // performingCardId for this action represents the origin (ship, location, or facility cards are walking from)
            cardActionMap.set(performingCardId, "Walk cards"); // TODO (Beam/walk cards to/from X)
            return cardActionMap;

        // Actions that will currently not be performed in a 1E game
        case "ACTIVATE_TRIBBLE_POWER":
        case "ALL_PLAYERS_DISCARD":
        case "PLACE_CARD_IN_PLAY_PILE":

        // Actions that don't need to be selected by user in current 1E implementation, with examples of future cards
        case "ENCOUNTER_SEED_CARD": // Nebula
        case "REMOVE_CARD_FROM_GAME": // Chula: The Door; Habit of Disappearing
        case "STOP_CARDS": // What Does God Need with a Starship?

        // Action types that are expected to always be a sub-action or cost of another action
        case "FAIL_DILEMMA":
        case "MAKE_DECISION":
        case "OVERCOME_DILEMMA":
        case "PLACE_CARD_ON_MISSION":
        case "PLACE_CARD_ON_TOP_OF_DRAW_DECK":
        case "PLACE_CARDS_BENEATH_DRAW_DECK":
        case "REVEAL_SEED_CARD":
        case "SELECT_ACTION":
        case "SELECT_AFFILIATION":
        case "SELECT_AWAY_TEAM":
        case "SELECT_CARDS":
        case "SELECT_SKILL":
        case "SYSTEM_QUEUE": // Under-the-hood subaction management, does not represent a change to gamestate
        case "USAGE_LIMIT": // Payment of a usage cost, like normal card play or "once per turn" limit
        default:
            console.error("No action initiation user message available for action type: '" + actionType + "'.");
            return cardActionMap;
    }
}

function export getTopMissionCardIdForLocation(gameState, targetLocationId) {
    for (let i = 0; i < gameState.spacelineLocations.length; i++) {
        let spacelineLocation = gameState.spacelineLocations[i];
        if (spacelineLocation.locationId === targetLocationId) {
            let missionCards = spacelineLocation.missionCards;
            return missionCards[(missionCards.length - i)];
        }
    }
    console.error("Unable to identify top mission card for location with id " + targetLocationId);
    return -1;
}