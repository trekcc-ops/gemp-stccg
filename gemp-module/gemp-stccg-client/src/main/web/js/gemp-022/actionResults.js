import { showLinkableCardTitle, getAffiliationHtml } from "./common.js";
import { getCardDivFromId } from "./jCards.js";

export function animateActionResult(jsonAction, jsonGameState, gameAnimations) {
    let actionType = jsonAction.actionType;
    // console.log("Calling animateActionResult for " + actionType);
    let cardList = new Array();
    let targetCard;
    let spacelineIndex;

    for (const location of jsonGameState.spacelineLocations) {
        if (location.seedCardCount === 0) {
            for (const missionId of location.missionCardIds) {
                getCardDivFromId(missionId).removeClass("seedCardCountBadge").removeAttr("seedCardCount");
            }
        }
        else {
            for (const missionId of location.missionCardIds) {
                getCardDivFromId(missionId).addClass("seedCardCountBadge").attr("seedCardCount", location.seedCardCount);
            }
        }
    }

    switch(actionType) {
        case "ADD_CARDS_TO_PRESEED_STACK": // preparing for dilemma seeds; only animation is to remove from "hand"
            gameAnimations.removeCardFromPlay(jsonAction.targetCardIds, jsonAction.performingPlayerId, true);
            break;
        case "BEAM_CARDS": // Same animation for both beaming and walking
        case "WALK_CARDS":
            for (const cardId of jsonAction.targetCardIds) {
                targetCard = jsonGameState.visibleCardsInGame[cardId];
                spacelineIndex = getSpacelineIndexFromLocationId(targetCard.locationId, jsonGameState);
                gameAnimations.beamOrWalkCard(targetCard, spacelineIndex);
            }
            break;
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
        case "DOCK_SHIP":
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            gameAnimations.dockShip(targetCard);
            break;
        case "DRAW_CARD":
            gameAnimations.drawCard(jsonAction.performingPlayerId, jsonGameState);
            break;
        case "FLY_SHIP":
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            spacelineIndex = getSpacelineIndexFromLocationId(targetCard.locationId, jsonGameState);
            gameAnimations.flyShip(targetCard, spacelineIndex);
            break;
        case "PLACE_CARD_ON_TOP_OF_DRAW_DECK":
        case "PLACE_CARDS_BENEATH_DRAW_DECK":
        case "SHUFFLE_CARDS_INTO_DRAW_DECK": // same animation
            cardList = jsonAction.targetCardIds;
            gameAnimations.removeCardFromPlay(cardList, jsonAction.performingPlayerId, true);
            for (const cardId of cardList) {
                targetCard = jsonGameState.visibleCardsInGame[cardId];
                gameAnimations.addCardToHiddenZone(targetCard, "DRAW_DECK", targetCard.owner);
            }
            break;
        case "PLAY_CARD":
            cardList.push(jsonAction.targetCardId);
            gameAnimations.removeCardFromPlay(cardList, jsonAction.performingPlayerId, true);
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            spacelineIndex = getSpacelineIndexFromLocationId(targetCard.locationId, jsonGameState);
            gameAnimations.putNonMissionIntoPlay(targetCard, jsonAction.performingPlayerId, jsonGameState, spacelineIndex, true);
            break;
        case "REMOVE_CARD_FROM_GAME":
            cardList.push(jsonAction.targetCardId);
            gameAnimations.removeCardFromPlay(cardList, jsonAction.performingPlayerId, true);
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            gameAnimations.addCardToHiddenZone(targetCard, "REMOVED", targetCard.owner);
            break;
        case "REMOVE_CARDS_FROM_PRESEED_STACK": // preparing for dilemma seeds; returns card to seed deck pile
            if (jsonAction.performingPlayerId === gameAnimations.game.bottomPlayerId) {
                gameAnimations.removeCardFromPlay(jsonAction.targetCardIds, jsonAction.performingPlayerId, true);
                for (const cardId of jsonAction.targetCardIds) {
                    targetCard = jsonGameState.visibleCardsInGame[cardId];
                    gameAnimations.addCardToHiddenZone(targetCard, "SEED_DECK", targetCard.owner);
                }
            }
            break;
        case "SEED_CARD":
            // This action type covers seeding cards in core or at a location, but not under a mission
            cardList.push(jsonAction.seededCardId);
            gameAnimations.removeCardFromPlay(cardList, jsonAction.performingPlayerId, true);
            targetCard = jsonGameState.visibleCardsInGame[jsonAction.seededCardId];
            spacelineIndex = getSpacelineIndexFromLocationId(targetCard.locationId, jsonGameState);
            if (targetCard.cardType == "MISSION") {
                let spacelineLocation = jsonGameState.spacelineLocations[spacelineIndex];
                let missionCards = spacelineLocation.missionCardIds;
                let firstMissionAtLocation = (missionCards[0] == targetCard.cardId);
                gameAnimations.putMissionIntoPlay(targetCard, true, spacelineLocation, spacelineIndex, firstMissionAtLocation);
            } else {
                gameAnimations.putNonMissionIntoPlay(targetCard, jsonAction.performingPlayerId, jsonGameState, spacelineIndex, true);
            }
            break;
        case "UNDOCK_SHIP":
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            gameAnimations.undockShip(targetCard);
            break;
        case "DOWNLOAD_CARD": // no animation, currently this is just a wrapper for PLAY_CARD
        case "ENCOUNTER_SEED_CARD": // no animation
        case "OVERCOME_DILEMMA": // no animation
            break;
        case "PLACE_CARD_ON_MISSION":
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            spacelineIndex = getSpacelineIndexFromLocationId(targetCard.locationId, jsonGameState);
            gameAnimations.putNonMissionIntoPlay(targetCard, jsonAction.performingPlayerId, jsonGameState, spacelineIndex, true);
            break;
        case "REVEAL_SEED_CARD":
            gameAnimations.revealCard(jsonAction.targetCardId, jsonGameState).then(() => {return});
            break;
        case "STOP_CARDS":
            gameAnimations.stopCards(jsonAction.targetCardIds, jsonGameState).then(() => {return});
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
        case "PLACE_CARD_IN_PLAY_PILE":
            break;
        
        // Actions with no specific animations
        case "ADD_MODIFIER": // No notifications sent when adding modifiers
        case "ATTEMPT_MISSION": // Note that ATTEMPT_MISSION is only sent when the mission attempt is ended, either by solving the mission or failing it.
        case "KILL": // only the kill part of the action; typically this will result in a separate discard action
        case "NULLIFY": // only the kill part of the action; typically this will result in a separate discard action
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
    // console.log("Calling communicateActionResult for " + actionType);
    let gameChat = gameUi.chatBox;
    let targetCard;

    switch(actionType) {
        case "ATTEMPT_MISSION":
            if (jsonAction.status === "completed_success") {
                message = performingPlayerId + " solved ";
            } else if (jsonAction.status === "completed_failure") {
                message = performingPlayerId + " failed ";
            }
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            message = message + showLinkableCardTitle(targetCard);
            gameChat.appendMessage(message, "gameMessage");
            break;
        case "BEAM_CARDS":
            message = performingPlayerId + " beamed ";
            message = message + jsonAction.targetCardIds.length + " cards from ";
            message = message + showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.originCardId]);
            message = message + " to " + showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.destinationCardId]);
            gameChat.appendMessage(message, "gameMessage");
            break;
        case "CHANGE_AFFILIATION": {
            let targetCardId = jsonAction.targetCardId;
            // console.log("targetCardId: " + targetCardId);
            let card = jsonGameState.visibleCardsInGame[targetCardId];
            // console.log("card: " + card.title);
                // getCardLink and change selectedAffiliation to HTML
            message = performingPlayerId + " changed " + showLinkableCardTitle(card) + "'s affiliation to " +
                getAffiliationHtml(card.affiliation);
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "DISCARD": {
            let discardedCard = jsonGameState.visibleCardsInGame[jsonAction.targetCardId];
            message = performingPlayerId + " discarded " + showLinkableCardTitle(discardedCard);
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "DRAW_CARD":
            message = performingPlayerId + " drew a card";
            gameChat.appendMessage(message, "gameMessage");
            break;
        case "FLY_SHIP": {
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            let newLocationId = targetCard.locationId;
            let newLocationName;
            for (const location of jsonGameState.spacelineLocations) {
                if (location.locationId == newLocationId) {
                    newLocationName = location.locationName;
                }
            }
            message = showLinkableCardTitle(targetCard) + " flew to " + newLocationName;
            break;
        }
        case "KILL":
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            message = performingPlayerId + " killed ";
            message = message + showLinkableCardTitle(targetCard) + " using ";
            message = showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.performingCardId]);
            gameChat.appendMessage(message, "gameMessage");
            break;
        case "NULLIFY":
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            message = performingPlayerId + " nullified ";
            message = message + showLinkableCardTitle(targetCard) + " using ";
            message = showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.performingCardId]);
            gameChat.appendMessage(message, "gameMessage");
            break;
        case "PLAY_CARD":
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            message = performingPlayerId + " played " + showLinkableCardTitle(targetCard);
            gameChat.appendMessage(message, "gameMessage");
            break;
        case "REMOVE_CARD_FROM_GAME": {
            let removedCard = jsonGameState.visibleCardsInGame[jsonAction.targetCardId];
            message = performingPlayerId + " removed " + showLinkableCardTitle(removedCard) + " from the game";
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "SCORE_POINTS": {
            // Update points for both players because why not
            for (const player of jsonGameState.players) {
                let playerId = player.playerId;
                let playerIndex = gameUi.getPlayerIndex(playerId);
                gameUi.playerScores[playerIndex] = player.score;
            }
            message = performingPlayerId + " scored " + jsonAction.pointsScored + " points from " +
                showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.performingCardId]);
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "SEED_CARD":
            // Same message, whether the card is a mission or not
            targetCard = jsonGameState.visibleCardsInGame[jsonAction.seededCardId];
            message = performingPlayerId + " seeded " + showLinkableCardTitle(targetCard);
            gameChat.appendMessage(message, "gameMessage");
            break;
        case "WALK_CARDS":
            message = performingPlayerId + " walked ";
            message = message + jsonAction.targetCardIds.length + " cards from ";
            message = message + showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.originCardId]);
            message = message + " to " + showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.destinationCardId]);
            gameChat.appendMessage(message, "gameMessage");
            break;
        case "ADD_CARDS_TO_PRESEED_STACK":
        case "ADD_MODIFIER": // No notifications sent when adding modifiers
        case "DOCK_SHIP":
        case "DOWNLOAD_CARD": // currently this is just a wrapper for PLAY_CARD
            break;
        case "ENCOUNTER_SEED_CARD": {
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            if (jsonAction.status === "completed_success") {
                message = performingPlayerId + " overcame " + showLinkableCardTitle(targetCard);
            } else if (jsonAction.status === "completed_failure") {
                message = performingPlayerId + " failed to overcome " + showLinkableCardTitle(targetCard);
            }
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "OVERCOME_DILEMMA":
            break;
        case "PLACE_CARD_ON_MISSION": {
            let cardId = jsonAction.targetCardId;
            targetCard = jsonGameState.visibleCardsInGame[cardId];
            let location;
            for (const spacelineLocation of jsonGameState.spacelineLocations) {
                if (spacelineLocation.locationId == targetCard.locationId) {
                    location = spacelineLocation;
                    break;
                }
            }
            let missionId = location.missionCardIds[0]; // just get the first one
            message = showLinkableCardTitle(targetCard) + " was placed on " + showLinkableCardTitle(jsonGameState.visibleCardsInGame[missionId]) + ".";
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "PLACE_CARD_ON_TOP_OF_DRAW_DECK":
        case "PLACE_CARDS_BENEATH_DRAW_DECK":
        case "REMOVE_CARDS_FROM_PRESEED_STACK":
        case "REVEAL_SEED_CARD":
        case "SHUFFLE_CARDS_INTO_DRAW_DECK":
            break;
        case "STOP_CARDS":
            for (const cardId of jsonAction.targetCardIds) {
                targetCard = jsonGameState.visibleCardsInGame[cardId];
                message = showLinkableCardTitle(targetCard) + " was stopped.";
                gameChat.appendMessage(message, "gameMessage");
            }
            break;
        case "SYSTEM_QUEUE": // Under-the-hood subaction management, does not represent a change to gamestate
        case "UNDOCK_SHIP":
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
        case "PLACE_CARD_IN_PLAY_PILE":
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
    console.log("Spaceline index for locationId " + locationId + " not found"); // normal for core cards
    return -1;
}