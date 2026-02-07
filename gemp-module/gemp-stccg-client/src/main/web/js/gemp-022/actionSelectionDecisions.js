import { getCardDivFromId } from './jCards.js';
import { goDing } from "./decisions.js";

export default class ActionSelectionDecision {

    gameUi; // GameTableUI object
    gameState;

    decisionId; // integer; unique identifier for decision object in server
    actions;
    selectedActionIds = new Array(); // integer array
    min; // integer; smallest number of actions that can be selected (either 0 or 1)

    constructor(decisionJson, gameUi, gameState) {
        this.gameUi = gameUi;
        this.decisionId = decisionJson.decisionId;
        this.min = decisionJson.min;
        this.gameState = gameState;
        this.actions = decisionJson.actions;
    }

    initializeUi(userMessage) {
        this.gameUi.alertText.html(userMessage);
        this.gameUi.alertBox.addClass("alert-box-highlight");
        this.createSelectableDivs();
        this.gameUi.hand.layoutCards();
        this.allowSelection();
        goDing(this.gameUi);
        $(':button').blur();
    }

    finishChoice() {
        this.gameUi.alertText.html("");
        this.gameUi.alertBox.removeClass("alert-box-highlight");
        this.gameUi.alertButtons.html("");
        this.gameUi.clearSelection();
        this.gameUi.hand.layoutCards();
        this.gameUi.decisionFunction(this.decisionId, "" + this.selectedActionIds);
    }

    processButtons() {
        var that = this;
        this.gameUi.alertButtons.html("");
        if (this.min == 0 && this.selectedActionIds.length == 0) {
            this.gameUi.alertButtons.append("<button id='Pass'>Pass</button>");
            $("#Pass").button().click(function () {
                that.finishChoice();
            });
        } else if (this.selectedActionIds.length >= this.min) {
            this.gameUi.alertButtons.append("<button id='Done'>Done</button>");
            $("#Done").button().click(function () {
                that.finishChoice();
            });
        }
        if (this.selectedActionIds.length > 0) {
            this.gameUi.alertButtons.append("<button id='ClearSelection'>Reset choice</button>");
            $("#ClearSelection").button().click(function () {
                that.resetChoice();
            });
        }
    }

    resetChoice() {
        this.selectedActionIds = new Array();
        this.gameUi.clearSelection();
        
        // Selecting cards with this decision removes all the divs, so they need to be re-created
        this.createSelectableDivs();
        this.gameUi.hand.layoutCards();
        
        this.allowSelection();
        this.processButtons();
    }

    respondToCardSelection(cardId, event) {

        let cardIdElem = getCardDivFromId(cardId);
        let cardActions = cardIdElem.data("action");

        // If the only legal action is a card play, perform action automatically by clicking
        // Otherwise show a drop-down menu with the action options by clicking
        if (cardActions.length == 1 &&
                (cardActions[0].actionType == "PLAY_CARD" || cardActions[0].actionType == "SEED_CARD")) {
            this.respondToActionSelection(cardId, cardActions[0].actionId);
        } else {
            this.createActionChoiceContextMenu(cardId, cardActions, event);
        }
    }

    respondToActionSelection(cardId, actionId) {
        this.selectedActionIds.push(actionId);
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
        for (let i = 0; i < this.actions.length; i++) {
            let action = this.actions[i];
            let cardActionMap = getActionInitiationCardActionMap(action, this.gameState);
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
    }

    createActionChoiceContextMenu(cardId, actions, event) {
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
        // BUG: possible race condition
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
            that.respondToActionSelection(cardId, actionId);
            return false;
        });

        // Hide bindings
        setTimeout(function () { // Delay for Mozilla
            $(document).click(getRidOfContextMenu);
        }, 0);
    }

}

export function getActionInitiationCardActionMap(action, gameState) {

    let cardActionMap = new Map();
    let actionType = action.actionType;

    // Most action JSON data has one or both of the following.
    let targetCardId = action.targetCardId;
    let performingCardId = action.performingCardId;

    switch(actionType) {
        case "ADD_CARDS_TO_PRESEED_STACK":
            // TODO - Will need additional implementation for this with a card like Empok Nor
            cardActionMap.set(getTopMissionCardIdForLocation(gameState, action.locationId), "Add seed cards");
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
            // BUG: locationId not sent from server side, commenting this out.
            //      cardActionMap.set(getTopMissionCardIdForLocation(gameState, action.locationId), "Initiate battle");
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
            if (performingCardId != null && typeof performingCardId != "undefined" && performingCardId != targetCardId) {
                cardActionMap.set(performingCardId, "Play card");
            }
            return cardActionMap;
        case "REMOVE_CARDS_FROM_PRESEED_STACK":
            // TODO - Will need additional implementation for this with a card like Empok Nor
            cardActionMap.set(getTopMissionCardIdForLocation(gameState, action.locationId), "Remove seed cards");
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

export function getTopMissionCardIdForLocation(gameState, targetLocationId) {
    for (const [locationId, location] of gameState.gameLocations.entries()) {
        if (locationId == targetLocationId) {
            let missionCards = location.missionCardIds;
            return missionCards[(missionCards.length - 1)];
        }
    }
    console.error("Unable to identify top mission card for location with id " + targetLocationId);
    return -1;
}