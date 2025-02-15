import { showLinkableCardTitle, showLinkableCardTitles, getAffiliationHtml } from "./common.js";

export function animateActionResult(jsonAction, jsonGameState, gameAnimations) {
    let actionType = jsonAction.actionType;
    console.log("Calling animateActionResult for " + actionType);
    let cardList = new Array();
    let targetCard;

    switch(actionType) {
        case "CHANGE_AFFILIATION":
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            gameAnimations.updateCardImage(targetCard);
            break;
        case "DISCARD":
            cardList.push(jsonAction.targetCardId);
            gameAnimations.removeCardFromPlay(cardList, jsonAction.performingPlayerId, true);
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            gameAnimations.addCardToHiddenZone(targetCard, "DISCARD", targetCard.owner);
            break;
        case "REMOVE_CARD_FROM_GAME":
            cardList.push(jsonAction.targetCardId);
            gameAnimations.removeCardFromPlay(cardList, jsonAction.performingPlayerId, true);
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            gameAnimations.addCardToHiddenZone(targetCard, "REMOVED", targetCard.owner);
            break;
        case "SEED_CARD":
            // This action type covers seeding cards in core or at a location, but not under a mission
            cardList.push(jsonAction.targetCardId);
            gameAnimations.removeCardFromPlay(cardList, jsonAction.performingPlayerId, true);
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            let spacelineIndex = getSpacelineIndexFromLocationId(targetCard.locationId, jsonGameState);
            if (targetCard.cardType == "MISSION") {
                let spacelineLocation = jsonGameState.spacelineLocations[spacelineIndex];
                let missionCards = spacelineLocation.missionCardIds;
                let firstMissionAtLocation = (missionCards[0] == targetCard.cardId);
                gameAnimations.putMissionIntoPlay(targetCard, true, spacelineIndex, firstMissionAtLocation);
            } else {
                gameAnimations.putNonMissionIntoPlay(targetCard, jsonAction.performingPlayerId, jsonGameState, spacelineIndex, true);
            }
            break;
        case "DOWNLOAD_CARD":
        case "DRAW_CARD":
        case "ENCOUNTER_SEED_CARD":
        case "BEAM_CARDS":
        case "WALK_CARDS":
        case "MOVE_SHIP":
        case "OVERCOME_DILEMMA":
        case "PLACE_CARD":
        case "PLAY_CARD":
        case "REVEAL_SEED_CARD":
        case "STOP_CARDS":
            break;
            // Actions that are just wrappers for decisions
        case "MAKE_DECISION":
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
            // Actions with no specific animations
        case "ADD_MODIFIER": // No notifications sent when adding modifiers
        case "ATTEMPT_MISSION":
            /* Note that ATTEMPT_MISSION is only sent when the mission attempt is ended,
                either by solving the mission or failing it. */
        case "FAIL_DILEMMA":
        case "KILL": // only the kill part of the action; typically this will result in a separate discard action
        case "SCORE_POINTS":
        case "SYSTEM_QUEUE": // Under-the-hood subaction management, does not represent a change to gamestate
        case "USAGE_LIMIT": // Payment of a usage cost, like normal card play or "once per turn" limit
        case "USE_GAME_TEXT":
            break;
        default:
            console.error("Unknown game action type: '" + actionType + "'.");
    }
}

export function communicateActionResult(jsonAction, jsonGameState, gameUi) {
    let actionType = jsonAction.actionType;
    let performingPlayerId = jsonAction.performingPlayerId;
    let message;
    console.log("Calling communicateActionResult for " + actionType);
    let gameChat = gameUi.chatBox;
    let targetCard;

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
        case "SCORE_POINTS":
            // Update points for both players because why not
            for (const player of jsonGameState.players) {
                let playerId = player.playerId;
                let playerIndex = gameUi.getPlayerIndex(playerId);
                gameUi.playerScores[index] = player.score;
            }
            message = performingPlayerId + " scored " + jsonAction.pointsScored + " points from " +
                showLinkableCardTitle(jsonAction.performingCard);
            gameChat.appendMessage(message, "gameMessage");
            break;
        case "SEED_CARD":
            // Same message, whether the card is a mission or not
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            message = performingPlayerId + " seeded " + showLinkableCardTitle(targetCard);
            gameChat.appendMessage(message, "gameMessage");
            break;
        case "ADD_MODIFIER": // No notifications sent when adding modifiers
        case "ATTEMPT_MISSION":
        case "DOWNLOAD_CARD":
        case "DRAW_CARD":
        case "ENCOUNTER_SEED_CARD":
        case "FAIL_DILEMMA":
        case "KILL":
        case "BEAM_CARDS":
        case "WALK_CARDS":
        case "MOVE_SHIP":
        case "OVERCOME_DILEMMA":
        case "PLACE_CARD":
        case "PLAY_CARD":
        case "REVEAL_SEED_CARD":
        case "STOP_CARDS":
        case "SYSTEM_QUEUE": // Under-the-hood subaction management, does not represent a change to gamestate
        case "USAGE_LIMIT": // Payment of a usage cost, like normal card play or "once per turn" limit
        case "USE_GAME_TEXT":
            break;
            // Actions that are just wrappers for decisions
        case "MAKE_DECISION":
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
        default:
            console.error("Unknown game action type: '" + actionType + "'.");
    }
}

export function getActionTargetCard(jsonAction, jsonGameState) {

    let targetCardId = jsonAction.targetCardId;
    return jsonGameState.visibleCardsInGame[targetCardId];

}

export function getSpacelineIndexFromLocationId(locationId, gameState) {

    for (let i = 0; i < gameState.spacelineLocations.length; i++) {
        let spacelineLocation = gameState.spacelineLocations[i];
        if (spacelineLocation.locationId == locationId) {
            return i;
        }
    }
    console.log("Spaceline index for locationId " + locationId + " not found");
    return -1;
}