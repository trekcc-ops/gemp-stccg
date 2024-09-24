import Card from "./jCards.js";
import { getCardDivFromId } from "./jCards.js";
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

    cardActivated(element, animate) {
        if (animate) {
            var that = this;

            var participantId = element.getAttribute("participantId");
            var cardId = element.getAttribute("cardId");

            // Play-out game event animation only if it's not the player who initiated it
            if (this.game.spectatorMode || this.game.replayMode || (participantId != this.game.bottomPlayerId)) {
                $("#main").queue(
                    function (next) {
                        var cardDiv = getCardDivFromId(cardId);
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

            var participantId = element.getAttribute("participantId");
            var blueprintId = element.getAttribute("blueprintId");
            var imageUrl = element.getAttribute("imageUrl");

            // Play-out game event animation only if it's not the player who initiated it
            if (this.game.spectatorMode || this.game.replayMode || (participantId != this.game.bottomPlayerId)) {
                var card = new Card(blueprintId, "ANIMATION", "anim", participantId, imageUrl);
                var cardDiv = createSimpleCardDiv(card.imageUrl);

                $("#main").queue(
                    function (next) {
                        cardDiv.data("card", card);
                        $("#main").append(cardDiv);

                        var gameWidth = $("#main").width();
                        var gameHeight = $("#main").height();

                        var cardHeight = (gameHeight / 2);
                        var cardWidth = card.getWidthForHeight(cardHeight);

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

            var participantId = element.getAttribute("participantId");
            var blueprintId = element.getAttribute("blueprintId");
            var imageUrl = element.getAttribute("imageUrl");
            var targetCardIds = element.getAttribute("otherCardIds").split(",");

            // Play-out card affects card animation only if it's not the player who initiated it
            if (this.game.spectatorMode || this.game.replayMode || this.game.replayMode || (participantId != this.game.bottomPlayerId)) {
                $("#main").queue(
                    function (next) {
                        for (var i = 0; i < targetCardIds.length; i++) {
                            var targetCardId = targetCardIds[i];

                            var card = new Card(blueprintId, "ANIMATION", "anim" + i, participantId, imageUrl);
                            var cardDiv = createSimpleCardDiv(card.imageUrl);

                            var targetCard = getCardDivFromId(targetCardId);
                            if (targetCard.length > 0) {
                                cardDiv.data("card", card);
                                $("#main").append(cardDiv);

                                targetCard = targetCard[0];
                                var targetCardWidth = $(targetCard).width();
                                var targetCardHeight = $(targetCard).height();

                                var shadowStartPosX;
                                var shadowStartPosY;
                                var shadowWidth;
                                var shadowHeight;
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
                                var cardData = $(this).data("card");
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

    putCardIntoPlay(element, animate, eventType) {
        var participantId = element.getAttribute("participantId");
        var cardId = element.getAttribute("cardId");
        var zone = element.getAttribute("zone");
        var imageUrl = element.getAttribute("imageUrl");
        var quadrant = element.getAttribute("quadrant");
        var locationIndex = element.getAttribute("locationIndex");

        var that = this;
        $("#main").queue(
            function (next) {
                var blueprintId = element.getAttribute("blueprintId");
                var imageUrl = element.getAttribute("imageUrl");
                var targetCardId = element.getAttribute("targetCardId");
                var controllerId = element.getAttribute("controllerId");

                if (zone == "SPACELINE") {
                    if (eventType == "PUT_SHARED_MISSION_INTO_PLAY") {
                        that.game.addSharedMission(locationIndex, quadrant);
                    } else {
                        that.game.addLocationDiv(locationIndex, quadrant);
                    }
                }

                if (controllerId != null)
                    participantId = controllerId;

                if (zone == "SPACELINE" && participantId != that.game.bottomPlayerId) {
                    var upsideDown = true;
                } else {
                    var upsideDown = false;
                }

                var card = new Card(blueprintId, zone, cardId, participantId, imageUrl, locationIndex, upsideDown);
                var cardDiv = that.game.createCardDiv(card, null);

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
            var oldValues = {};

            $("#main").queue(
                function (next) {
                    var cardDiv = getCardDivFromId(cardId);
                    var card = cardDiv.data("card");
                    var pos = cardDiv.position();

                    oldValues["zIndex"] = cardDiv.css("zIndex");
                    oldValues["left"] = pos.left;
                    oldValues["top"] = pos.top;
                    oldValues["width"] = cardDiv.width();
                    oldValues["height"] = cardDiv.height();

                    // Now we begin the animation
                    var gameWidth = $("#main").width();
                    var gameHeight = $("#main").height();

                    var cardHeight = (gameHeight / 2);
                    var cardWidth = card.getWidthForHeight(cardHeight);

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
                    setTimeout(next, that.getAnimationLength(that.putCardIntoPlayDuration * (5 / 8)));
                }).queue(
                function (next) {
                    var cardDiv = getCardDivFromId(cardId);
                    var pos = cardDiv.position();

                    var startLeft = pos.left;
                    var startTop = pos.top;
                    var startWidth = cardDiv.width();
                    var startHeight = cardDiv.height();

                    $(cardDiv).animate(
                        {
                            left:oldValues["left"]},
                        {
                            duration:that.getAnimationLength(that.putCardIntoPlayDuration / 4),
                            easing:"linear",
                            step:function (now, fx) {
                                var state = fx.state;
                                layoutCardElem(cardDiv,
                                    startLeft + (oldValues["left"] - startLeft) * state,
                                    startTop + (oldValues["top"] - startTop) * state,
                                    startWidth + (oldValues["width"] - startWidth) * state,
                                    startHeight + (oldValues["height"] - startHeight) * state, 100);
                            },
                            complete:next});
                }).queue(
                function (next) {
                    var cardDiv = getCardDivFromId(cardId);
                    $(cardDiv).css({zIndex:oldValues["zIndex"]});
                    next();
                });
        }
    }

    updateCardImage(element) {
            $("#main").queue(
                function (next) {
                    var cardId = element.getAttribute("cardId");
                    var imageUrl = element.getAttribute("imageUrl");
                    var cardDiv = getCardDivFromId(cardId);
                    images = document.getElementsByClassName("card_img_"+cardId);
                    for (var i = 0; i < images.length; i++) {
                        images[i].src = imageUrl;
                    }
                    if (cardDiv.data("card") != null)
                        cardDiv.data("card").imageUrl = imageUrl;
                    next();
                });
    }

    moveCardInPlay(element) {
        var that = this;
        $("#main").queue(
            function (next) {
                that.cardId = element.getAttribute("cardId");
                var zone = element.getAttribute("zone");
                var targetCardId = element.getAttribute("targetCardId");
                var participantId = element.getAttribute("participantId");
                var controllerId = element.getAttribute("controllerId");
                var locationIndex = element.getAttribute("locationIndex");

                if (controllerId != null)
                    participantId = controllerId;

                var card = getCardDivFromId(that.cardId);
                var cardData = card.data("card");
                cardData.oldGroup = that.game.getReorganizableCardGroupForCardData(cardData);
                if (cardData.zone == "ATTACHED")
                    cardData.oldGroup = that.game.getReorganizableCardGroupForCardData(cardData.attachedToCard);
                else
                    cardData.oldGroup = that.game.getReorganizableCardGroupForCardData(cardData);

                // Remove from where it was already attached
                that.removeFromAttached(that.cardId);

                var card = getCardDivFromId(that.cardId);
                var cardData = card.data("card");
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

    removeCardFromPlay(element, animate) {
        var that = this;
        var cardRemovedIds = element.getAttribute("otherCardIds").split(",");
        var participantId = element.getAttribute("participantId");

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
                for (var i = 0; i < cardRemovedIds.length; i++) {
                    var cardId = cardRemovedIds[i];
                    var card = getCardDivFromId(cardId);

                    if (card.length > 0) {
                        var cardData = card.data("card");
                        if (cardData.zone == "ATTACHED" || cardData.zone == "STACKED") {
                            removeFromAttached(cardId);
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

    gamePhaseChange(element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var phase = element.getAttribute("phase");
                $(".phase").text(phase);
                next();
            });
    }

    tribbleSequence(element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var message = element.getAttribute("message");
                $(".tribbleSequence").html("Next Tribble in sequence:<b>" + message + "</b>");
                next();
            });
    }

    turnChange(element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var playerId = element.getAttribute("participantId");
                var playerIndex = that.game.getPlayerIndex(playerId);
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

    playerScore(element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var participantId = element.getAttribute("participantId");
                var score = element.getAttribute("score");

                if (that.game.playerScores == null)
                    that.game.playerScores = new Array();

                var index = that.game.getPlayerIndex(participantId);
                that.game.playerScores[index] = score;

                next();
            });
    }

    gameStats(element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var charStats = element.getAttribute("charStats");
                if (charStats != null) {
                    var charStatsArr = charStats.split(",");
                    for (var i = 0; i < charStatsArr.length; i++) {
                        var cardStats = charStatsArr[i].split("=");
                        var cardDiv = $(".card:cardId(" + cardStats[0] + ")");
                        that.game.ensureCardHasBoxes(cardDiv);
                        var cardStatArr = cardStats[1].split("|");
                        $(".cardStrength", cardDiv).html(cardStatArr[0]);
                        $(".cardVitality", cardDiv).html(cardStatArr[1]);
                        if (cardStatArr.length > 2) {
                            if (cardStatArr[2].indexOf("R") == 0) {
                                var resistanceDiv = $(".cardResistance", cardDiv);
                                var resistance = cardStatArr[2].substring(1);
                                if (resistance.indexOf("A") == 0) {
                                    resistanceDiv.addClass("aragorn");
                                    resistance = resistance.substring(1);
                                } else if (resistance.indexOf("F") == 0) {
                                    resistanceDiv.addClass("frodo");
                                    resistance = resistance.substring(1);
                                } else if (resistance.indexOf("G") == 0) {
                                    resistanceDiv.addClass("gandalf");
                                    resistance = resistance.substring(1);
                                } else if (resistance.indexOf("T") == 0) {
                                    resistanceDiv.addClass("theoden");
                                    resistance = resistance.substring(1);
                                } else {
                                    $(".cardResistanceBg", cardDiv).css({display:""});
                                }
                                resistanceDiv.html(resistance).css({display:""});
                            } else {
                                $(".cardSiteNumber", cardDiv).html(cardStatArr[2]).css({display:""});
                                $(".cardSiteNumberBg", cardDiv).css({display:""});
                            }
                        }
                    }
                }

                var playerZones = element.getElementsByTagName("playerZones");
                for (var i = 0; i < playerZones.length; i++) {
                    var playerZone = playerZones[i];

                    var playerId = playerZone.getAttribute("name");
                    var hand = playerZone.getAttribute("HAND");
                    var discard = playerZone.getAttribute("DISCARD");
                    var adventureDeck = playerZone.getAttribute("ADVENTURE_DECK"); // TODO - Safe to remove? Have removed Zone.ADVENTURE_DECK from server
                    var deck = playerZone.getAttribute("DRAW_DECK");
                    var removed = playerZone.getAttribute("REMOVED");

                    $("#deck" + that.game.getPlayerIndex(playerId)).text(deck);
                    $("#hand" + that.game.getPlayerIndex(playerId)).text(hand);
                    $("#discard" + that.game.getPlayerIndex(playerId)).text(discard);
                    $("#adventureDeck" + that.game.getPlayerIndex(playerId)).text(adventureDeck);
                    $("#removedPile" + that.game.getPlayerIndex(playerId)).text(removed);
                }

                var playerScores = element.getElementsByTagName("playerScores");
                for (var i = 0; i < playerScores.length; i++) {
                    var playerScore = playerScores[i];
                    var playerId = playerScore.getAttribute("name");
                    var score = playerScore.getAttribute("score");

                    $("#score" + that.game.getPlayerIndex(playerId)).text("SCORE " + Number(score).toLocaleString("en-US"));
                }


                var playerThreats = element.getElementsByTagName("threats")
                for (var i = 0; i < playerThreats.length; i++) {
                    var playerThreat = playerThreats[i];

                    var playerId = playerThreat.getAttribute("name");
                    var value = playerThreat.getAttribute("value");
                    $("#threats" + that.game.getPlayerIndex(playerId)).text(value);
                }
                next();
            });
    }

    message(element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var message = element.getAttribute("message");
                if (that.game.chatBox != null)
                    that.game.chatBox.appendMessage(message, "gameMessage");

                next();
            });
    }

    warning(element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var message = element.getAttribute("message");
                if (that.game.chatBox != null)
                    that.game.chatBox.appendMessage(message, "warningMessage");

                next();
            });
    }

    processDecision(decision, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var decisionType = decision.getAttribute("decisionType");
                if (decisionType == "INTEGER") {
                    that.game.integerDecision(decision);
                } else if (decisionType == "MULTIPLE_CHOICE") {
                    that.game.multipleChoiceDecision(decision);
                } else if (decisionType == "ARBITRARY_CARDS") {
                    that.game.arbitraryCardsDecision(decision);
                } else if (decisionType == "ACTION_CHOICE") {
                    that.game.actionChoiceDecision(decision);
                } else if (decisionType == "CARD_ACTION_CHOICE") {
                    that.game.cardActionChoiceDecision(decision);
                } else if (decisionType == "CARD_SELECTION") {
                    that.game.cardSelectionDecision(decision);
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
                var cardData = $(this).data("card");
                var index = -1;
                for (var i = 0; i < cardData.attachedCards.length; i++)
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
        var targetCardData = getCardDivFromId(targetCardId).data("card");
        targetCardData.attachedCards.push(cardDiv);
        cardDiv.data("card").attachedToCard = targetCardData;
    }

}
