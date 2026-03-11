import { showLinkableCardTitle, getAffiliationHtml } from "./common.js";
import { getCardDivFromId } from "./jCards.js";

export function animateActionResult(jsonAction, jsonGameState, gameAnimations) {
    let actionType = jsonAction.type;
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
        case "ADDED_PRESEEDS": // preparing for dilemma seeds; only animation is to remove from "hand"
            if (jsonAction.performingPlayerId === gameAnimations.game.bottomPlayerId) {
                gameAnimations.removeCardFromPlay(jsonAction.targetCardIds, jsonAction.performingPlayerId, true);
            }
            break;
        case "BEAMED": // Same animation for both beaming and walking
        case "WALKED":
            for (const cardId of jsonAction.targetCardIds) {
                targetCard = jsonGameState.visibleCardsInGame[cardId];
                spacelineIndex = getSpacelineIndexFromLocationId(targetCard.locationId, jsonGameState);
                gameAnimations.beamOrWalkCard(targetCard, spacelineIndex);
            }
            break;
        case "CHANGED_AFFILIATION":
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            gameAnimations.updateCardImage(targetCard);
            break;
        case "DILEMMA_PLACED_ON_CARD":
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            spacelineIndex = getSpacelineIndexFromLocationId(targetCard.locationId, jsonGameState);
            gameAnimations.putNonMissionIntoPlay(targetCard, jsonAction.performingPlayerId, jsonGameState, spacelineIndex, true);
            break;
        case "DISCARDED":
            cardList.push(jsonAction.targetCardId);
            gameAnimations.removeCardFromPlay(cardList, jsonAction.performingPlayerId, true);
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            gameAnimations.addCardToHiddenZone(targetCard, jsonAction.destination, targetCard.owner);
            break;
        case "DOCKED":
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            gameAnimations.dockShip(targetCard);
            break;
        case "DREW_CARDS":
            gameAnimations.drawCard(jsonAction.performingPlayerId, jsonGameState);
            break;
        case "FLEW_SHIP":
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            spacelineIndex = getSpacelineIndexFromLocationId(targetCard.locationId, jsonGameState);
            gameAnimations.flyShip(targetCard, spacelineIndex);
            break;
        case "PLACED_CARDS_IN_DRAW_DECK":
            cardList = jsonAction.targetCardIds;
            gameAnimations.removeCardFromPlay(cardList, jsonAction.performingPlayerId, true);
            for (const cardId of cardList) {
                targetCard = jsonGameState.visibleCardsInGame[cardId];
                gameAnimations.addCardToHiddenZone(targetCard, "DRAW_DECK", targetCard.owner);
            }
            break;
        case "PLAYED_CARD": {
            cardList.push(jsonAction.playedCardId);
            gameAnimations.removeCardFromPlay(cardList, jsonAction.performingPlayerId, true);
            let playedCard = jsonGameState.visibleCardsInGame[jsonAction.playedCardId];
            spacelineIndex = getSpacelineIndexFromLocationId(playedCard.locationId, jsonGameState);
            gameAnimations.putNonMissionIntoPlay(playedCard, jsonAction.performingPlayerId, jsonGameState, spacelineIndex, true);
            break;
        }
        case "REMOVED_CARD_FROM_GAME": {
            for (const cardId of jsonAction.targetCardIds) {
                cardList.push(cardId);
            }
            gameAnimations.removeCardFromPlay(cardList, jsonAction.performingPlayerId, true);
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            gameAnimations.addCardToHiddenZone(targetCard, "REMOVED", targetCard.owner);
            break;
        }
        case "REMOVED_PRESEEDS": // preparing for dilemma seeds; returns card to seed deck pile
            if (jsonAction.performingPlayerId === gameAnimations.game.bottomPlayerId) {
                gameAnimations.removeCardFromPlay(jsonAction.targetCardIds, jsonAction.performingPlayerId, true);
                for (const cardId of jsonAction.targetCardIds) {
                    targetCard = jsonGameState.visibleCardsInGame[cardId];
                    gameAnimations.addCardToHiddenZone(targetCard, "SEED_DECK", targetCard.owner);
                }
            }
            break;
        case "REVEALED_SEED_CARD":
            gameAnimations.revealCard(jsonAction.targetCardId, jsonGameState).then(() => {return});
            break;
        case "SEEDED_INTO_PLAY":
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
        case "STOPPED_CARDS":
            gameAnimations.stopCards(jsonAction.targetCardIds, jsonGameState).then(() => {return});
            break;
        case "UNDOCKED":
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            gameAnimations.undockShip(targetCard);
            break;

        // Actions with no specific animations in a 1E game
        case "ACTIVATED_TRIBBLE_POWER":
        case "ENDED_TURN":
        case "KILLED": // only the kill part of the action; typically this will result in a separate discard action
        case "MISSION_ATTEMPT_ENDED":
        case "MISSION_ATTEMPT_STARTED":
        case "NULLIFIED": // only the nullify part of the action; typically this will result in a separate discard action
        case "PLAYER_WENT_OUT": // Tribbles action
        case "RANDOM_SELECTION_INITIATED":
        case "SCORED_POINTS":
        case "STARTED_TURN":
            break;
        default:
            console.error("Unknown game action type: '" + actionType + "'.");
    }
}

export function communicateActionResult(jsonAction, jsonGameState, gameUi) {
    let actionType = jsonAction.type;
    let performingPlayerId = jsonAction.performingPlayerId;
    let performingPlayerText;
    if (performingPlayerId === gameUi.bottomPlayerId) {
        performingPlayerText = "You";
    } else {
        performingPlayerText = performingPlayerId;
    }
    let message;
    // console.log("Calling communicateActionResult for " + actionType);
    let gameChat = gameUi.chatBox;
    let targetCard;

    switch(actionType) {
        case "ADDED_PRESEEDS": {
            /* Nothing to see here. This action isn't broadcast to your opponent, and it should already be clear
                from the UI what you are doing. */
            break;
        }
        case "BEAMED": {
            message = performingPlayerText + " beamed ";
            message = message + jsonAction.targetCardIds.length + " cards from ";
            message = message + showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.originCardId]);
            message = message + " to " + showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.destinationCardId]);
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "CHANGED_AFFILIATION": {
            let targetCardId = jsonAction.targetCardId;
            let card = jsonGameState.visibleCardsInGame[targetCardId];
            message = performingPlayerText + " changed " + showLinkableCardTitle(card) + "'s affiliation to " +
                getAffiliationHtml(jsonAction.newAffiliation).outerHTML;
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "DILEMMA_PLACED_ON_CARD": {
            let cardId = jsonAction.targetCardId;
            targetCard = jsonGameState.visibleCardsInGame[cardId];
            message = showLinkableCardTitle(targetCard) + " was placed on ";
            message = message + showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.cardPlacedOnId]);
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "DISCARDED": {
            let discardedCard = jsonGameState.visibleCardsInGame[jsonAction.targetCardId];
            message = performingPlayerText + " discarded " + showLinkableCardTitle(discardedCard);
            if (jsonAction.destination === "DISCARD") {
                message = message + " to discard pile";
            } else if (jsonAction.destination === "POINT_AREA") {
                message = message + " to point area";
            } else if (jsonAction.destination === "REMOVED") {
                message = message + " and removed it from the game";
            }
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "DOCKED": {
            message = showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.targetCardId]);
            message = message + " docked at ";
            message = message + showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.dockedAtCardId]);
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "DREW_CARDS": {
            let drawnCardIds = jsonAction.drawnCardIds;

            if (drawnCardIds.length > 1) {
                message = performingPlayerText + " drew " + drawnCardIds.length + " cards";
            } else if (drawnCardIds[0] < 0) {
                // card id will be negative if the card is unknown to this player
                message = performingPlayerText + " drew a card";
            } else {
                let drawnCard = jsonGameState.visibleCardsInGame[drawnCardIds[0]];
                message = performingPlayerText + " drew " + showLinkableCardTitle(drawnCard);
            }
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "FLEW_SHIP": {
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            let oldLocationId = jsonAction.originLocationId;
            let newLocationId = jsonAction.destinationLocationId;
            let oldLocationName;
            let newLocationName;
            for (const location of jsonGameState.spacelineLocations) {
                if (location.locationId == newLocationId) {
                    newLocationName = location.locationName;
                } else if (location.locationId == oldLocationId) {
                    oldLocationName = location.locationName;
                }
            }
            message = showLinkableCardTitle(targetCard) + " flew from " + oldLocationName + " to " + newLocationName;
            break;
        }
        case "KILLED": {
            for (const cardId of jsonAction.killedCardIds) {
                message = showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.killedCardIds[0]]);
                message = message + " was killed by ";
                message = message + showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.performingCardId]);
                gameChat.appendMessage(message, "gameMessage");
            }
            break;
        }
        case "MISSION_ATTEMPT_ENDED": {
            if (jsonAction.wasSuccessful === true) {
                message = performingPlayerText + " solved ";
            } else {
                message = performingPlayerText + " failed ";
            }
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            message = message + showLinkableCardTitle(targetCard);
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "MISSION_ATTEMPT_STARTED": {
            message = performingPlayerText + " started an attempt of ";
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            message = message + showLinkableCardTitle(targetCard);
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "NULLIFIED": {
            targetCard = getActionTargetCard(jsonAction, jsonGameState);
            message = performingPlayerText + " nullified ";
            message = message + showLinkableCardTitle(targetCard) + " using ";
            message = showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.performingCardId]);
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "PLACED_CARDS_IN_DRAW_DECK": {
            let cardDescription;

            if (jsonAction.targetCardIds.length > 1) {
                cardDescription = jsonAction.targetCardIds.length + "cards";
            } else if (jsonAction.drawnCardIds[0] < 0) {
                // card id will be negative if the card is unknown to this player
                cardDescription = "a card";
            } else {
                cardDescription = showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.targetCardIds[0]]);
            }

            let actionDescription;

            if (jsonAction.placement === "SHUFFLE") {
                actionDescription = performingPlayerText + " shuffled " + cardDescription + " into";
            } else if (jsonAction.placement === "TOP") {
                actionDescription = performingPlayerText + " placed " + cardDescription + " on top of";
            } else if (jsonAction.placement === "BOTTOM") {
                actionDescription = performingPlayerText + " placed " + cardDescription + " beneath";
            }

            if (performingPlayerText === "You") {
                message = actionDescription + " your draw deck";
            } else {
                message = actionDescription + " their draw deck";
            }
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "PLAYED_CARD": {
            if (jsonAction.isDownload === true) {
                message = performingPlayerText + " downloaded ";
            } else if (jsonAction.isReport === true) {
                message = performingPlayerText + " reported ";
            } else {
                message = performingPlayerText + " played ";
            }
            let playedCard = jsonGameState.visibleCardsInGame[jsonAction.playedCardId];
            message = message + showLinkableCardTitle(playedCard);
            if (jsonAction.destinationCardId != null && typeof jsonAction.destinationCardId != "undefined") {
                message = message + " to " + showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.destinationCardId]);
            }
            if (jsonAction.performingCardId != null && typeof jsonAction.performingCardId != "undefined") {
                if (jsonAction.performingCardId != jsonAction.playedCardId) {
                    message = message + " using " + showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.performingCardId]);
                }
            }
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "REMOVED_CARD_FROM_GAME": {
            for (const cardId of jsonAction.targetCardIds) {
                targetCard = jsonGameState.visibleCardsInGame[cardId];
                message = performingPlayerText + " removed " + showLinkableCardTitle(targetCard) + " from the game";
                gameChat.appendMessage(message, "gameMessage");
            }
            break;
        }
        case "REMOVED_PRESEEDS": {
            /* Nothing to see here. This action isn't broadcast to your opponent, and it should already be clear
                from the UI what you are doing. */
            break;
        }
        case "REVEALED_SEED_CARD": {
            targetCard = jsonGameState.visibleCardsInGame[jsonAction.targetCardId];
            message = performingPlayerText + " revealed " + showLinkableCardTitle(targetCard);
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "SCORED_POINTS": {
            gameUi.updateGameStats(jsonGameState);
            message = performingPlayerText + " scored " + jsonAction.pointsScored;
            if (jsonAction.pointsAreBonus === true) {
                message = message + " bonus";
            }
            message = message + " points from " + showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.performingCardId]);
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "SEEDED_INTO_PLAY": {
            targetCard = jsonGameState.visibleCardsInGame[jsonAction.seededCardId];
            message = performingPlayerText + " seeded " + showLinkableCardTitle(targetCard);
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "STARTED_TURN": {
            gameChat.appendMessage("--------------------", "gameMessage");
            message = "Start of ";
            if (performingPlayerText === "you") {
                message = message = "your ";
            } else {
                message = message + performingPlayerText + "'s ";
            }

            if (jsonAction.turnNumber === 1) {
                message = message + "1st turn";
            } else if (jsonAction.turnNumber === 2) {
                message = message + "2nd turn";
            } else if (jsonAction.turnNumber === 3) {
                message = message + "3rd turn";
            } else {
                message = message + jsonAction.turnNumber + "th turn";
            }
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "STOPPED_CARDS": {
            for (const cardId of jsonAction.targetCardIds) {
                targetCard = jsonGameState.visibleCardsInGame[cardId];
                message = showLinkableCardTitle(targetCard) + " was stopped.";
                gameChat.appendMessage(message, "gameMessage");
            }
            break;
        }
        case "UNDOCKED": {
            message = showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.targetCardId]);
            message = message + " undocked from ";
            message = message + showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.dockedAtCardId]);
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
        case "WALKED": {
            if (jsonAction.targetCardIds.length > 1) {
                message = jsonAction.targetCardIds.length.toString() + " cards";
            } else {
                message = showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.targetCardIds[0]]);
            }
            message = message + " walked from ";
            message = message + showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.originCardId]);
            message = message + " to " + showLinkableCardTitle(jsonGameState.visibleCardsInGame[jsonAction.destinationCardId]);
            gameChat.appendMessage(message, "gameMessage");
            break;
        }
            // Actions with no specific 1E communication
        case "ACTIVATED_TRIBBLE_POWER": // Tribbles only
        case "ENDED_TURN":
        case "PLAYER_WENT_OUT": // Tribbles only
        case "RANDOM_SELECTION_INITIATED":
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