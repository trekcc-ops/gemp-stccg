import Card from "./jCards.js";
import { getCardDivFromId, createSimpleCardDiv } from "./jCards.js";
import { layoutCardElem } from "./jCardGroup.js";

export default class GameAnimations {
    game;
    replaySpeed = 1;
    playEventDuration = 1500;
    putCardIntoPlayDuration = 1500;
    cardAffectsCardDuration = 1200;
    cardActivatedDuration = 1200;
    decisionDuration = 1200;
    removeCardFromPlayDuration = 600;
    cardId;
    cardData;

    constructor(gameUI) {
        this.game = gameUI;
    }

    getAnimationLength(origValue) {
        if (this.game.replayMode)
            return origValue * this.replaySpeed;
        return origValue;
    }

    cardActivated(json, animate) {
        if (animate) {
            var that = this;

            let participantId = json.participantId;
            let cardId = json.cardId;

            // Play-out game event animation only if it's not the player who initiated it
            if (this.game.spectatorMode || this.game.replayMode || (participantId != this.game.bottomPlayerId)) {
                $("#main").queue(
                    function (next) {
                        let cardDiv = getCardDivFromId(cardId);
                        if (cardDiv.length > 0) {
                            $(".borderOverlay", cardDiv)
                                .switchClass("borderOverlay", "highlightBorderOverlay", that.getAnimationLength(that.cardActivatedDuration / 6))
                                .switchClass("highlightBorderOverlay", "borderOverlay", that.getAnimationLength(that.cardActivatedDuration / 6))
                                .switchClass("borderOverlay", "highlightBorderOverlay", that.getAnimationLength(that.cardActivatedDuration / 6))
                                .switchClass("highlightBorderOverlay", "borderOverlay", that.getAnimationLength(that.cardActivatedDuration / 6))
                                .switchClass("borderOverlay", "highlightBorderOverlay", that.getAnimationLength(that.cardActivatedDuration / 6))
                                .switchClass("highlightBorderOverlay", "borderOverlay", that.getAnimationLength(that.cardActivatedDuration / 6), next);
                        }
                        else {
                            next();
                        }
                    });
            }
        }
    }

    eventPlayed(element, animate) {
        if (animate) {
            var that = this;

            let participantId = element.getAttribute("participantId");
            let blueprintId = element.getAttribute("blueprintId");
            let imageUrl = element.getAttribute("imageUrl");

            // Play-out game event animation only if it's not the player who initiated it
            if (this.game.spectatorMode || this.game.replayMode || (participantId != this.game.bottomPlayerId)) {
                let card = new Card(blueprintId, "ANIMATION", "anim", participantId, imageUrl);
                let cardDiv = createSimpleCardDiv(card.imageUrl);

                $("#main").queue(
                    function (next) {
                        cardDiv.data("card", card);
                        $("#main").append(cardDiv);

                        let gameWidth = $("#main").width();
                        let gameHeight = $("#main").height();

                        let cardHeight = (gameHeight / 2);
                        let cardWidth = card.getWidthForHeight(cardHeight);

                        $(cardDiv).css(
                            {
                                position:"absolute",
                                left:(gameWidth / 2 - cardWidth / 4),
                                top:gameHeight * (3 / 8),
                                width:cardWidth / 2,
                                height:cardHeight / 2,
                                "z-index":100,
                                opacity:0});

                        $(cardDiv).animate(
                            {
                                left:"-=" + cardWidth / 4,
                                top:"-=" + (gameHeight / 8),
                                width:"+=" + (cardWidth / 2),
                                height:"+=" + (cardHeight / 2),
                                opacity:1},
                            {
                                duration:that.getAnimationLength(that.playEventDuration / 8),
                                easing:"linear",
                                queue:false,
                                complete:next});
                    }).queue(
                    function (next) {
                        setTimeout(next, that.getAnimationLength(that.playEventDuration * (5 / 8)));
                    }).queue(
                    function (next) {
                        $(cardDiv).animate(
                            {
                                opacity:0},
                            {
                                duration:that.getAnimationLength(that.playEventDuration / 4),
                                easing:"easeOutQuart",
                                queue:false,
                                complete:next});
                    }).queue(
                    function (next) {
                        $(cardDiv).remove();
                        next();
                    });
            }
        }
    }

    cardAffectsCard(element, animate) {
        if (animate) {
            var that = this;

            let participantId = element.getAttribute("participantId");
            let blueprintId = element.getAttribute("blueprintId");
            let imageUrl = element.getAttribute("imageUrl");
            let targetCardIds = element.getAttribute("otherCardIds").split(",");

            // Play-out card affects card animation only if it's not the player who initiated it
            if (this.game.spectatorMode || this.game.replayMode || this.game.replayMode || (participantId != this.game.bottomPlayerId)) {
                $("#main").queue(
                    function (next) {
                        for (let i = 0; i < targetCardIds.length; i++) {
                            let targetCardId = targetCardIds[i];

                            let card = new Card(blueprintId, "ANIMATION", "anim" + i, participantId, imageUrl);
                            let cardDiv = createSimpleCardDiv(card.imageUrl);

                            let targetCard = getCardDivFromId(targetCardId);
                            if (targetCard.length > 0) {
                                cardDiv.data("card", card);
                                $("#main").append(cardDiv);

                                targetCard = targetCard[0];
                                let targetCardWidth = $(targetCard).width();
                                let targetCardHeight = $(targetCard).height();

                                let shadowStartPosX;
                                let shadowStartPosY;
                                let shadowWidth;
                                let shadowHeight;
                                if (card.horizontal != $(targetCard).data("card").horizontal) {
                                    shadowWidth = targetCardHeight;
                                    shadowHeight = targetCardWidth;
                                    shadowStartPosX = $(targetCard).position().left - (shadowWidth - targetCardWidth) / 2;
                                    shadowStartPosY = $(targetCard).position().top - (shadowHeight - targetCardHeight) / 2;
                                } else {
                                    shadowWidth = targetCardWidth;
                                    shadowHeight = targetCardHeight;
                                    shadowStartPosX = $(targetCard).position().left;
                                    shadowStartPosY = $(targetCard).position().top;
                                }

                                $(cardDiv).css(
                                    {
                                        position:"absolute",
                                        left:shadowStartPosX,
                                        top:shadowStartPosY,
                                        width:shadowWidth,
                                        height:shadowHeight,
                                        "z-index":100,
                                        opacity:1});
                                $(cardDiv).animate(
                                    {
                                        opacity:0,
                                        left:"-=" + (shadowWidth / 2),
                                        top:"-=" + (shadowHeight / 2),
                                        width:"+=" + shadowWidth,
                                        height:"+=" + shadowHeight},
                                    {
                                        duration:that.getAnimationLength(that.cardAffectsCardDuration),
                                        easing:"easeInQuart",
                                        queue:false,
                                        complete:null});
                            }
                        }

                        setTimeout(next, that.getAnimationLength(that.cardAffectsCardDuration));
                    }).queue(
                    function (next) {
                        $(".card").each(
                            function () {
                                let cardData = $(this).data("card");
                                if (cardData.zone == "ANIMATION") {
                                    $(this).remove();
                                }
                            }
                        );
                        next();
                    });
            }
        }
    }

    putCardIntoPlay(json, animate, eventType) {
        let participantId = json.participantId;
        let cardId = json.cardId;
        let zone = json.zone;
        let imageUrl = json.imageUrl;
        let region = json.region;
        let quadrant = json.quadrant;
        let locationIndex = json.locationIndex;

        var that = this;
        $("#main").queue(
            function (next) {
                let blueprintId = json.blueprintId;
                let imageUrl = json.imageUrl;
                let targetCardId = json.targetCardId;
                let controllerId = json.controllerId;
                let upsideDown;

                if (zone == "SPACELINE") {
                    if (eventType == "PUT_SHARED_MISSION_INTO_PLAY") {
                        that.game.addSharedMission(locationIndex, quadrant, region);
                    } else {
                        that.game.addLocationDiv(locationIndex, quadrant, region);
                    }
                }

                if (controllerId != null)
                    participantId = controllerId;

                // If card is listed as public knowledge in server -> common/filterable/Zone.java
                // and the card belongs to the opponent, inform UI to invert it.
                let visible_opponent_zones = [
                    "CORE",
                    "SPACELINE",
                    "AT_LOCATION",
                    "ATTACHED"
                ];

                if (visible_opponent_zones.includes(zone) &&
                    (participantId != that.game.bottomPlayerId)
                ) {
                    upsideDown = true;
                } else {
                    upsideDown = false;
                }

                let card = new Card(blueprintId, zone, cardId, participantId, imageUrl, locationIndex, upsideDown);
                let cardDiv = that.game.createCardDivWithData(card, null);

                if (zone == "DISCARD")
                    that.game.discardPileDialogs[participantId].append(cardDiv);
                else if (zone == "ADVENTURE_DECK") // Todo - Safe to remove? Have removed the ADVENTURE_DECK zone from server
                    that.game.adventureDeckDialogs[participantId].append(cardDiv);
                else if (zone == "REMOVED")
                    that.game.removedPileDialogs[participantId].append(cardDiv);
                else if (zone == "DRAW_DECK") {
                    that.game.miscPileDialogs[participantId].append(cardDiv);
                    animate = false;
                }
                else
                    $("#main").append(cardDiv);

                if (targetCardId != null)
                    that.attachCardDivToTargetCardId(cardDiv, targetCardId);
                next();
            });

        $("#main").queue(
            function (next) {
                that.game.layoutGroupWithCard(cardId);
                next();
            });

        if (animate && (this.game.spectatorMode || this.game.replayMode || (participantId != this.game.bottomPlayerId))
            && zone != "DISCARD" && zone != "HAND" && zone != "DRAW_DECK") {
            let final_position = {};

            $("#main").queue(
                // Display the card in the center of the screen
                function (next) {
                    // Calculate expected final position.
                    let cardDiv = getCardDivFromId(cardId);
                    let card = cardDiv.data("card");
                    let pos = cardDiv.position();
                    let card_img = $(cardDiv).children(".card_img").first();

                    final_position["left"] = pos.left;
                    final_position["top"] = pos.top;
                    final_position["width"] = cardDiv.width();
                    final_position["height"] = cardDiv.height();
                    final_position["z-index"] = cardDiv.css("z-index");
                    final_position["upside-down"] = card.upsideDown;

                    if (final_position["upside-down"]) {
                        // Don't animate a card upside down even if that's set. Class is restored in final step.
                        $(card_img).removeClass("upside-down");
                    }
                    

                    // Now we begin the animation
                    let gameWidth = $("#main").width();
                    let gameHeight = $("#main").height();

                    let cardHeight = (gameHeight / 2);
                    let cardWidth = card.getWidthForHeight(cardHeight);

                    $(cardDiv).css(
                        {
                            position:"absolute",
                            left: "" + (gameWidth / 2 - cardWidth / 4) + "px",
                            top: "" + (gameHeight * (3 / 8)) + "px",
                            width: "" + (cardWidth / 2) + "px",
                            height: "" + (cardHeight / 2) + "px",
                            "z-index":100,
                            opacity:0});
                    
                    $(cardDiv).animate(
                        {
                            opacity:1},
                        {
                            duration:that.getAnimationLength(that.putCardIntoPlayDuration / 8),
                            easing:"linear",
                            step:function (now, fx) {
                                    layoutCardElem(cardDiv,
                                    (gameWidth / 2 - cardWidth / 4) - now * (cardWidth / 4),
                                    gameHeight * (3 / 8) - now * (gameHeight / 8),
                                    cardWidth / 2 + now * (cardWidth / 2),
                                    cardHeight / 2 + now * (cardHeight / 2), 100);
                            },
                            complete:next});
                }).queue(
                function (next) {
                    // Hold display in the center of the screen.
                    setTimeout(next, that.getAnimationLength(that.putCardIntoPlayDuration * (5 / 8)));
                }).queue(
                function (next) {
                    // Animate the card towards the final position on the play mat.
                    let cardDiv = getCardDivFromId(cardId);
                    $(cardDiv).animate(
                        // properties
                        {
                            left:final_position["left"],
                            top:final_position["top"],
                            width:final_position["width"],
                            height:final_position["height"]
                        },
                        // duration
                        that.getAnimationLength(that.putCardIntoPlayDuration / 4),
                        // easing
                        "linear",
                        // complete
                        next
                        );
                }).queue(
                function (next) {
                    // Set final resting values for the card, including upside-down status.
                    // TODO: This is required in order to ensure the border overlay and
                    //       token overlay display correctly after the animation.
                    //       This may not be necessary if the overlays are contained inside the
                    //       cardDiv that is being animated, as opposed to applied in layoutCardElem.
                    let cardDiv = getCardDivFromId(cardId);
                    let card_img = $(cardDiv).children(".card_img").first();
                    layoutCardElem(cardDiv,
                        final_position["left"],
                        final_position["top"],
                        final_position["width"],
                        final_position["height"],
                        final_position["z-index"]);
                    
                    if (final_position["upside-down"]) {
                        $(card_img).addClass("upside-down");
                    }
                    next();
                });
        }
    }

    updateCardImage(json) {
            $("#main").queue(
                function (next) {
                    let cardId = json.cardId;
                    let imageUrl = json.imageUrl;
                    let cardDiv = getCardDivFromId(cardId);
                    let images = document.getElementsByClassName("card_img_"+cardId);
                    for (let i = 0; i < images.length; i++) {
                        images[i].src = imageUrl;
                    }
                    if (cardDiv.data("card") != null)
                        cardDiv.data("card").imageUrl = imageUrl;
                    next();
                });
    }

    moveCardInPlay(json) {
        var that = this;
        $("#main").queue(
            function (next) {
                that.cardId = json.cardId;
                let zone = json.zone;
                let targetCardId = json.targetCardId;
                let participantId = json.participantId;
                let controllerId = json.controllerId;
                let locationIndex = json.locationIndex;

                if (controllerId != null)
                    participantId = controllerId;

                let card = getCardDivFromId(that.cardId);
                let cardData = card.data("card");
                cardData.oldGroup = that.game.getReorganizableCardGroupForCardData(cardData);
                if (cardData.zone == "ATTACHED")
                    cardData.oldGroup = that.game.getReorganizableCardGroupForCardData(cardData.attachedToCard);
                else
                    cardData.oldGroup = that.game.getReorganizableCardGroupForCardData(cardData);

                // Remove from where it was already attached
                that.removeFromAttached(that.cardId);

                // move to new zone
                cardData.zone = zone;
                cardData.owner = participantId;
                cardData.locationIndex = locationIndex;
                that.cardData = cardData;

                if (targetCardId != null)
                    that.attachCardDivToTargetCardId(card, targetCardId);
                next();
            });

            $("#main").queue(
                function (next) {
                    that.game.layoutGroupWithCard(that.cardId);
                    that.cardData.oldGroup.layoutCards();
                    that.cardData.oldGroup = null;
                    next();
                });
    }

    removeCardFromPlay(json, animate) {
        var that = this;
        let cardRemovedIds = json.otherCardIds.split(",");
        let participantId = json.participantId;

        if (animate && (this.game.spectatorMode || this.game.replayMode || (participantId != this.game.bottomPlayerId))) {
            $("#main").queue(
                function (next) {
                    $(".card:cardId(" + cardRemovedIds + ")")
                        .animate(
                        {
                            opacity:0},
                        {
                            duration:that.getAnimationLength(that.removeCardFromPlayDuration),
                            easing:"easeOutQuart",
                            queue:false});
                    setTimeout(next, that.getAnimationLength(that.removeCardFromPlayDuration));
                });
        }
        $("#main").queue(
            function (next) {
                for (let i = 0; i < cardRemovedIds.length; i++) {
                    let cardId = cardRemovedIds[i];
                    let card = getCardDivFromId(cardId);

                    if (card.length > 0) {
                        let cardData = card.data("card");
                        if (cardData.zone == "ATTACHED") {
                            that.removeFromAttached(cardId);
                        }

                        card.remove();
                    }
                }

                next();
            });

        if (animate) {
            $("#main").queue(
                function (next) {
                    that.game.layoutUI(false);
                    next();
                });
        }
    }

    gamePhaseChange(json, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                let phase = json.phase;
                $("#currentPhase").text(phase);
                next();
            });
    }

    tribbleSequence(json, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                let message = json.tribbleSequence;
                // if the TribbleSequence object exists, checked via length (lol jQuery), fill it with the phase.
                if ($("#tribbleSequence").length ) {
                    $("#tribbleSequence").html("Next Tribble in sequence:<b>" + message + "</b>");
                }
                next();
            });
    }

    turnChange(json, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                let playerId = json.participantId;
                let playerIndex = that.game.getPlayerIndex(playerId);
                that.game.currentPlayerId = playerId;
                $(".player").each(function (index) {
                    if (index == playerIndex)
                        $(this).addClass("current");
                    else
                        $(this).removeClass("current");
                });
                next();
            });
        if (animate) {
            $("#main").queue(
                function (next) {
                    next();
                });
        }
    }

    playerScore(json, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                let participantId = json.participantId;
                let score = json.score;

                if (that.game.playerScores == null)
                    that.game.playerScores = new Array();

                let index = that.game.getPlayerIndex(participantId);
                that.game.playerScores[index] = score;

                next();
            });
    }

    gameStats(json, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                let playerZones = json.playerZones;
                for (let i = 0; i < playerZones.length; i++) {
                    let playerZone = playerZones[i];

                    let playerId = playerZone.playerId;
                    let hand = playerZone.hand;
                    let discard = playerZone.discard;
                    let deck = playerZone.drawDeck;
                    let removed = playerZone.removed;

                    $("#deck" + that.game.getPlayerIndex(playerId)).text(deck);
                    $("#hand" + that.game.getPlayerIndex(playerId)).text(hand);
                    $("#discard" + that.game.getPlayerIndex(playerId)).text(discard);
                    $("#removedPile" + that.game.getPlayerIndex(playerId)).text(removed);
                }

                let playerScores = json.playerScores;
                for (let i = 0; i < playerScores.length; i++) {
                    let playerScore = playerScores[i];
                    let playerId = playerScore.playerId;
                    let score = playerScore.score;

                    $("#score" + that.game.getPlayerIndex(playerId)).text("SCORE " + Number(score).toLocaleString("en-US"));
                }

                next();
            });
    }

    message(json, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                let message = json.message;
                if (that.game.chatBox != null)
                    that.game.chatBox.appendMessage(message, "gameMessage");

                next();
            });
    }

    warning(json, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                let message = json.message;
                if (that.game.chatBox != null)
                    that.game.chatBox.appendMessage(message, "warningMessage");

                next();
            });
    }

    processDecision(decision, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                let decisionType = decision.decisionType;
                if (decisionType === "INTEGER") {
                    that.game.integerDecision(decision);
                } else if (decisionType === "MULTIPLE_CHOICE") {
                    that.game.multipleChoiceDecision(decision);
                } else if (decisionType === "ARBITRARY_CARDS") {
                    that.game.arbitraryCardsDecision(decision);
                } else if (decisionType === "ACTION_CHOICE") {
                    that.game.actionChoiceDecision(decision);
                } else if (decisionType === "CARD_ACTION_CHOICE") {
                    that.game.cardActionChoiceDecision(decision);
                } else if (decisionType === "CARD_SELECTION") {
                    that.game.cardSelectionDecision(decision);
                } else if (decisionType === "CARD_SELECTION_FROM_COMBINATIONS") {
                    that.game.cardSelectionFromCombinations(decision);
                }
                else {
                    console.error(`Unknown decisionType: ${decisionType}`);
                    next(); // bail out
                }
                

                if (!animate)
                    that.game.layoutUI(false);

                next();
            });
        if (that.game.replayMode) {
            $("#main").queue(
                function (next) {
                    setTimeout(next, that.getAnimationLength(that.decisionDuration));
                });
        }
    }

    updateGameState(animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                setTimeout(
                    function () {
                        that.game.updateGameState();
                    }, 100);

                if (!animate)
                    that.game.layoutUI(false);

                next();
            });
    }

    windowResized() {
        var that = this;
        $("#main").queue(
            function (next) {
                that.game.layoutUI(true);
                next();
            });
    }

    removeFromAttached(cardId) {
            // TODO - This can probably be greatly simplified now that "attachedToCard" has been created, but not messing with it for now
        $(".card").each(
            function () {
                let cardData = $(this).data("card");
                let index = -1;
                for (let i = 0; i < cardData.attachedCards.length; i++)
                    if (cardData.attachedCards[i].data("card").cardId == cardId) {
                        index = i;
                        break;
                    }
                if (index != -1) {
                    cardData.attachedCards.splice(index, 1);
                    getCardDivFromId(cardId).data("card").attachedToCard = null;
                }
            }
        );
    }

    attachCardDivToTargetCardId(cardDiv, targetCardId) {
        let targetCardData = getCardDivFromId(targetCardId).data("card");
        targetCardData.attachedCards.push(cardDiv);
        cardDiv.data("card").attachedToCard = targetCardData;
    }

}