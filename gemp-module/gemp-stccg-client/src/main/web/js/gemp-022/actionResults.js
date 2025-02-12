import { showLinkableCardTitle, getAffiliationHtml } from "./common.js";

export function animateActionResult(jsonAction, jsonGameState, gameAnimations) {
    let actionType = jsonAction.actionType;
    console.log("Calling animateActionResult for " + actionType);

    switch(actionType) {
        case "CHANGE_AFFILIATION":
            let targetCardId = jsonAction.targetCardId;
            console.log("targetCardId: " + targetCardId);
            let card = jsonGameState.visibleCardsInGame[targetCardId];
            console.log("card: " + card);
            gameAnimations.updateCardImage(card);
            break;
        case "ACTIVATE_TRIBBLE_POWER":
        case "ADD_MODIFIER": // No notifications sent when adding modifiers
        case "ATTEMPT_MISSION":
        case "BATTLE":
        case "DISCARD":
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
        case "REMOVE_CARD_FROM_GAME":
        case "REVEAL_SEED_CARD":
        case "SCORE_POINTS":
        case "SEED_CARD":
        case "SELECT_ACTION":
        case "SELECT_AFFILIATION":
        case "SELECT_AWAY_TEAM":
        case "SELECT_CARDS":
        case "SELECT_SKILL":
        case "STOP_CARDS":
            // The below actions don't currently send anything to the client when completed
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
    console.log("Calling communicateActionResult for " + actionType);

    switch(actionType) {
        case "CHANGE_AFFILIATION":
            let targetCardId = jsonAction.targetCardId;
            console.log("targetCardId: " + targetCardId);
            let card = jsonGameState.visibleCardsInGame[targetCardId];
            console.log("card: " + card.title);
                // getCardLink and change selectedAffiliation to HTML
            let message = performingPlayerId + " changed " + showLinkableCardTitle(card) + "'s affiliation to " +
                getAffiliationHtml(card.affiliation);
            console.log("message: " + message);
            gameChat.appendMessage(message, "gameMessage");
            return message;
        case "ACTIVATE_TRIBBLE_POWER":
        case "ADD_MODIFIER": // No notifications sent when adding modifiers
        case "ATTEMPT_MISSION":
        case "BATTLE":
        case "DISCARD":
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
        case "REMOVE_CARD_FROM_GAME":
        case "REVEAL_SEED_CARD":
        case "SCORE_POINTS":
        case "SEED_CARD":
        case "SELECT_ACTION":
        case "SELECT_AFFILIATION":
        case "SELECT_AWAY_TEAM":
        case "SELECT_CARDS":
        case "SELECT_SKILL":
        case "STOP_CARDS":
        case "SYSTEM_QUEUE": // Under-the-hood subaction management, does not represent a change to gamestate
        case "USAGE_LIMIT": // Payment of a usage cost, like normal card play or "once per turn" limit
        case "USE_GAME_TEXT":
            break;
        default:
            console.error("Unknown game action type: '" + actionType + "'.");
    }
}