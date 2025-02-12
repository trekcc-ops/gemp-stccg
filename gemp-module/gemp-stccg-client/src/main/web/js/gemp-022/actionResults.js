import { showLinkableCardTitle, showLinkableCardTitles, getAffiliationHtml } from "./common.js";

export function animateActionResult(jsonAction, jsonGameState, gameAnimations) {
    let actionType = jsonAction.actionType;
    console.log("Calling animateActionResult for " + actionType);
    let cardList = new Array();

    switch(actionType) {
        case "CHANGE_AFFILIATION":
            let targetCardId = jsonAction.targetCardId;
            console.log("targetCardId: " + targetCardId);
            let card = jsonGameState.visibleCardsInGame[targetCardId];
            console.log("card: " + card);
            gameAnimations.updateCardImage(card);
            break;
        case "DISCARD":
            cardList.push(jsonAction.targetCardId);
            gameAnimations.removeCardFromPlay(cardList, jsonAction.performingPlayerId, true);
            let discardedCard = jsonGameState.visibleCardsInGame[jsonAction.targetCardId];
            gameAnimations.addCardToHiddenZone(discardedCard, "DISCARD", discardedCard.owner);
            break;
        case "REMOVE_CARD_FROM_GAME":
            cardList.push(jsonAction.targetCardId);
            gameAnimations.removeCardFromPlay(cardList, jsonAction.performingPlayerId, true);
            let removedCard = jsonGameState.visibleCardsInGame[jsonAction.targetCardId];
            gameAnimations.addCardToHiddenZone(removedCard, "REMOVED", removedCard.owner);
            break;
        case "ATTEMPT_MISSION":
        case "DOWNLOAD_CARD":
        case "DRAW_CARD":
        case "ENCOUNTER_SEED_CARD":
        case "MAKE_DECISION":
        case "MOVE_CARDS":
        case "OVERCOME_DILEMMA":
        case "PLACE_CARD":
        case "PLAY_CARD":
        case "REVEAL_SEED_CARD":
        case "SCORE_POINTS":
        case "SEED_CARD":
        case "STOP_CARDS":
            break;
            // Actions that are just wrappers for decisions
        case "SELECT_ACTION":
        case "SELECT_AFFILIATION":
        case "SELECT_AWAY_TEAM":
        case "SELECT_CARDS":
        case "SELECT_SKILL":
            break;
            // Actions that will currently not be performed in a 1E game
        case "ACTIVATE_TRIBBLE_POWER":
        case "ALL_PLAYERS_DISCARD":
        case "BATTLE":
            break;
            // Actions that don't need a specific animation
        case "ADD_MODIFIER": // No notifications sent when adding modifiers
        case "FAIL_DILEMMA":
        case "KILL": // only the kill part of the action; typically this will result in a separate discard action
        case "SYSTEM_QUEUE": // Under-the-hood subaction management, does not represent a change to gamestate
        case "USAGE_LIMIT": // Payment of a usage cost, like normal card play or "once per turn" limit
        case "USE_GAME_TEXT":
            break;
        default:
            console.error("Unknown game action type: '" + actionType + "'.");
    }
}

export function communicateActionResult(jsonAction, jsonGameState, gameChat) {
    let actionType = jsonAction.actionType;
    let performingPlayerId = jsonAction.performingPlayerId;
    let message;
    console.log("Calling communicateActionResult for " + actionType);

    switch(actionType) {
        case "CHANGE_AFFILIATION":
            let targetCardId = jsonAction.targetCardId;
            console.log("targetCardId: " + targetCardId);
            let card = jsonGameState.visibleCardsInGame[targetCardId];
            console.log("card: " + card.title);
                // getCardLink and change selectedAffiliation to HTML
            message = performingPlayerId + " changed " + showLinkableCardTitle(card) + "'s affiliation to " +
                getAffiliationHtml(card.affiliation);
            gameChat.appendMessage(message, "gameMessage");
            break;
        case "DISCARD":
            let discardedCard = jsonGameState.visibleCardsInGame[jsonAction.targetCardId];
            message = performingPlayerId + " discarded " + showLinkableCardTitle(discardedCard);
            gameChat.appendMessage(message, "gameMessage");
            break;
        case "REMOVE_CARD_FROM_GAME":
            let removedCard = jsonGameState.visibleCardsInGame[jsonAction.targetCardId];
            message = performingPlayerId + " removed " + showLinkableCardTitle(removedCard) + " from the game";
            gameChat.appendMessage(message, "gameMessage");
            break;
        case "ADD_MODIFIER": // No notifications sent when adding modifiers
        case "ATTEMPT_MISSION":
        case "DOWNLOAD_CARD":
        case "DRAW_CARD":
        case "ENCOUNTER_SEED_CARD":
        case "FAIL_DILEMMA":
        case "KILL":
        case "MAKE_DECISION":
        case "MOVE_CARDS":
        case "OVERCOME_DILEMMA":
        case "PLACE_CARD":
        case "PLAY_CARD":
        case "REVEAL_SEED_CARD":
        case "SCORE_POINTS":
        case "SEED_CARD":
        case "STOP_CARDS":
        case "SYSTEM_QUEUE": // Under-the-hood subaction management, does not represent a change to gamestate
        case "USAGE_LIMIT": // Payment of a usage cost, like normal card play or "once per turn" limit
        case "USE_GAME_TEXT":
            break;
            // Actions that are just wrappers for decisions
        case "SELECT_ACTION":
        case "SELECT_AFFILIATION":
        case "SELECT_AWAY_TEAM":
        case "SELECT_CARDS":
        case "SELECT_SKILL":
            // Actions that will currently not be performed in a 1E game
        case "ACTIVATE_TRIBBLE_POWER":
        case "ALL_PLAYERS_DISCARD":
        case "BATTLE":
            break;
        default:
            console.error("Unknown game action type: '" + actionType + "'.");
    }
}