import Card from "./jCards.js";
import { getCardDivFromId, createCardDiv, createSimpleCardDiv } from "./jCards.js";
import { layoutCardElem } from "./jCardGroup.js";
import { getFriendlyPhaseName } from "./common.js";
import PQueue from 'p-queue';
import delay from 'delay';

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
    queue;

    constructor(gameUI) {
        this.game = gameUI;
        this.queue = new PQueue({concurrency: 1});
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
                let zone = "ANIMATION";
                let cardId = "anim";
                let emptyLocationIndex = "";
                let upsideDown = false;
                let title = element.getAttribute("title");
                let card = new Card(blueprintId, zone, cardId, participantId, title, imageUrl, emptyLocationIndex, upsideDown);
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
                            let zone = "ANIMATION";
                            let cardId = `anim${i}`;
                            let emptyLocationIndex = "";
                            let upsideDown = false;
                            let title = element.getAttribute("title");
                            let card = new Card(blueprintId, zone, cardId, participantId, title, imageUrl, emptyLocationIndex, upsideDown);
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

    drawCard(performingPlayerId, gameState) {
        this.game.updateGameStats(gameState); // update card counts in draw decks
        for (const playerData of Object.values(gameState.playerMap)) {
            if (playerData.playerId == performingPlayerId && playerData.playerId == this.game.bottomPlayerId) {
                for (const cardId of playerData.cardGroups["HAND"].cardIds) {
                    let cardDiv = getCardDivFromId(cardId);
                    if (cardDiv.length == 0) {
                        let card = gameState.visibleCardsInGame[cardId];
                        this.addCardToHiddenZone(card, "HAND", performingPlayerId);
                    }
                }
            }
        }
    }

    addCardToHiddenZone(cardJson, zone, zoneOwner) {
        // Adding card to discard, hand, removed, or draw deck
        // console.log("calling addCardToHiddenZone");
        var that = this;
        let cardId = cardJson.cardId;
        let imageUrl = cardJson.imageUrl;
        let blueprintId = cardJson.blueprintId;
        let locationIndex = "-1";
        let upsideDown = false;
        let controllerId = zoneOwner;
        let cardTitle = (cardJson.title) ? cardJson.title : "";

        $("#main").queue(
            function (next) {

                let card = new Card(blueprintId, zone, cardId, controllerId, cardTitle, imageUrl, locationIndex, upsideDown);
                let cardDiv = that.game.createCardDivWithData(card);

                if (zone == "DISCARD")
                    that.game.discardPileDialogs[controllerId].append(cardDiv);
                else if (zone == "DRAW_DECK")
                    that.game.miscPileDialogs[controllerId].append(cardDiv);
                else if (zone == "REMOVED")
                    that.game.removedPileDialogs[controllerId].append(cardDiv);
                else
                    $("#main").append(cardDiv);
                next();
            });

        $("#main").queue(
            function (next) {
                that.game.layoutGroupWithCard(cardId);
                next();
            });
    }

    putMissionIntoPlay(cardJson, animate, spacelineLocation, spacelineIndex, firstMissionAtLocation) {
        // int spacelineIndex: index of mission's location in game state spaceline array
        // boolean firstMissionAtLocation: true if bottom or only mission card; false if card is in top of another mission card
        // console.log("Calling putMissionIntoPlay");
        // console.log(cardJson);
        // console.log(spacelineIndex);
        var that = this;
        let region = spacelineLocation.region;
        let quadrant = spacelineLocation.quadrant;
        let blueprintId = cardJson.blueprintId;
        let zone = "SPACELINE";
        let cardId = cardJson.cardId;
        let participantId = cardJson.owner;
        let imageUrl = cardJson.imageUrl;
        let locationIndex = spacelineIndex.toString();
        let upsideDown = (participantId != that.game.bottomPlayerId);
        let thisGame = this.game;
        let cardTitle = (cardJson.title) ? cardJson.title : "";

        $("#main").queue(
            function (next) {

                if (firstMissionAtLocation) {
                    // console.log("Adding mission " + cardJson.title + " at location index " + spacelineIndex);
                    thisGame.addLocationDiv(locationIndex, quadrant, region);
                } else {
                    // console.log("Adding mission card " + cardJson.title + " to location index " + spacelineIndex);
                    thisGame.addSharedMission(locationIndex, quadrant, region);
                }

                let card = new Card(blueprintId, zone, cardId, participantId, cardTitle, imageUrl, locationIndex, upsideDown);
                let cardDiv = thisGame.createCardDivWithData(card);

                $("#main").append(cardDiv);
                next();
            });

        $("#main").queue(
            function (next) {
                thisGame.layoutGroupWithCard(cardId);
                next();
            });

        if (animate && (thisGame.spectatorMode || thisGame.replayMode || (participantId != thisGame.bottomPlayerId))) {
            this.animateCardPlay(cardId);
        }
    }

    putNonMissionIntoPlay(cardJson, performingPlayerId, gameState, spacelineIndex, animate) {

        var that = this;
        let cardId = cardJson.cardId;
        let imageUrl = cardJson.imageUrl;
        let blueprintId = cardJson.blueprintId;
        let attachedToCardId = cardJson.attachedToCardId;
        let isPlacedOnMission = cardJson.isPlacedOnMission
        let upsideDown = (performingPlayerId != that.game.bottomPlayerId);
        let cardTitle = (cardJson.title) ? cardJson.title : "";

        let zone;
        if (attachedToCardId != null) {
            zone = "ATTACHED";
        } else if (isPlacedOnMission) {
            zone = "PLACED_ON_MISSION";
        } else if (spacelineIndex.toString() === "-1") {
            zone = "CORE";
        } else {
            zone = "AT_LOCATION";
        }

        $("#main").queue(
            function (next) {

                let card = new Card(blueprintId, zone, cardId, performingPlayerId, cardTitle, imageUrl, spacelineIndex, upsideDown);
                let cardDiv = that.game.createCardDivWithData(card);

                $("#main").append(cardDiv);

                if (attachedToCardId != null)
                    that.attachCardDivToTargetCardId(cardDiv, attachedToCardId);
                next();
            });

        $("#main").queue(
            function (next) {
                that.game.layoutGroupWithCard(cardId);
                next();
            });

        if (animate && (this.game.spectatorMode || this.game.replayMode || (performingPlayerId != this.game.bottomPlayerId))) {
            this.animateCardPlay(cardId);
        }
    }

    animateCardPlay(cardId) {
        var that = this;
        let final_position = {};

        $("#main").queue(
            // Display the card in the center of the screen
            function (next) {
                // Calculate expected final position.
                let cardDiv = getCardDivFromId(cardId);
                let card = cardDiv.data("card");
                let pos = cardDiv.position();
                let card_img = $(cardDiv).find(".card_img").first();

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
                let card_img = $(cardDiv).find(".card_img").first();
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

    updateCardImage(cardData) {
            $("#main").queue(
                function (next) {
                    let cardId = cardData.cardId;
                    let imageUrl = cardData.imageUrl;
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

    beamOrWalkCard(cardJson, spacelineIndex) {
        var that = this;
        $("#main").queue(
            function (next) {
                that.cardId = cardJson.cardId;
                let attachedToCardId = cardJson.attachedToCardId;

                let card = getCardDivFromId(that.cardId);
                let cardData = card.data("card");
                cardData.oldGroup = that.game.getReorganizableCardGroupForCardData(cardData.attachedToCard);

                // Remove from where it was already attached
                that.removeFromAttached(that.cardId);

                // move to new zone
                cardData.zone = "ATTACHED";
                cardData.owner = cardJson.owner;
                cardData.locationIndex = spacelineIndex;
                that.cardData = cardData;
                that.attachCardDivToTargetCardId(card, attachedToCardId);
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

    flyShip(cardJson, spacelineIndex) {
        var that = this;
        $("#main").queue(
            function (next) {
                that.cardId = cardJson.cardId;

                let card = getCardDivFromId(that.cardId);
                let cardData = card.data("card");
                cardData.oldGroup = that.game.getReorganizableCardGroupForCardData(cardData);

                // move to new zone
                cardData.locationIndex = spacelineIndex;
                that.cardData = cardData;
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

    dockShip(cardJson) {
        var that = this;
        $("#main").queue(
            function (next) {
                that.cardId = cardJson.cardId;
                let attachedToCardId = cardJson.attachedToCardId;

                let card = getCardDivFromId(that.cardId);
                let cardData = card.data("card");
                cardData.oldGroup = that.game.getReorganizableCardGroupForCardData(cardData);

                // move to new zone
                cardData.zone = "ATTACHED";
                that.cardData = cardData;
                that.attachCardDivToTargetCardId(card, attachedToCardId);

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

    undockShip(json) {
        var that = this;
        $("#main").queue(
            function (next) {
                that.cardId = json.cardId;

                let card = getCardDivFromId(that.cardId);
                let cardData = card.data("card");
                cardData.oldGroup = that.game.getReorganizableCardGroupForCardData(cardData.attachedToCard);

                // Remove from where it was already attached
                that.removeFromAttached(that.cardId);

                card = getCardDivFromId(that.cardId);
                cardData = card.data("card");
                // move to new zone
                cardData.zone = "AT_LOCATION";
                that.cardData = cardData;
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

    async revealCard(targetCardId, jsonGameState) {
        await this.queue.add(async () => {
            // animation layer setup
            // TODO: Create a permanent animation layer that's invisible so I don't have to copy this every time.
            let animation_layer = document.createElement("div");
            animation_layer.id = "animation-layer";

            let card_json = jsonGameState.visibleCardsInGame[targetCardId];
            let blueprintId = card_json.blueprintId;
            let zone = "VOID";
            let cardId = card_json.cardId;
            let noOwner = "";
            let title = (card_json.title) ? card_json.title : "";
            let imageUrl = card_json.imageUrl;
            let emptyLocationIndex = "";
            let upsideDown = false;
            let card = new Card(blueprintId, zone, cardId, noOwner, title, imageUrl, emptyLocationIndex, upsideDown);

            let baseCardDiv = createCardDiv(card.imageUrl, card.title, card.isFoil(), card.status_tokens, false, card.hasErrata(), card.isUpsideDown(), card.cardId);

            let pageWidth = document.body.clientWidth;
            let oneSixthWidthVal = (pageWidth / 6);
            let cardWidth = oneSixthWidthVal + "px"; // 5 width
            let cardHeight = Math.floor(oneSixthWidthVal * 1.5) + "px"; // 3:2 ratio

            baseCardDiv.style.margin = "auto";
            baseCardDiv.style.width = cardWidth;
            baseCardDiv.style.height = cardHeight;
            baseCardDiv.style.flex = `0 1 ${cardWidth}`;
            
            let threeDCardLayer = baseCardDiv.getElementsByClassName("three-d-card")[0];
            threeDCardLayer.classList.add("facedown"); // TODO make this part of the createCardDiv options

            animation_layer.appendChild(baseCardDiv);
            
            let gamediv;
            if (this.game.mainDiv instanceof jQuery) {
                gamediv = this.game.mainDiv[0];
            }
            else {
                gamediv = this.game;
            }
            gamediv.appendChild(animation_layer);

            // fade in
            await this.animateElementAndSaveCSSPromise(
                animation_layer,
                [
                    { // from
                        opacity: 0,
                    },
                    { // to
                        opacity: 1,
                    },
                ],
                {
                    duration: 500, //ms
                    fill: "forwards"
                }
            );

            let cardsToFlip = animation_layer.getElementsByClassName("facedown");
            for (const card of cardsToFlip) {
                card.classList.remove("facedown");
            }
            
            // wait 2s for people to read them
            await delay(2000);
            
            // fade out
            await this.animateElementAndSaveCSSPromise(
                animation_layer,
                [
                    { // from
                        opacity: 1,
                    },
                    { // to
                        opacity: 0,
                    },
                ],
                {
                    duration: 500, //ms
                    fill: "forwards"
                }
            );

            animation_layer.remove();
        });
    }

    async stopCards(targetCardIds, jsonGameState) {
        await this.queue.add(async () => {
            // animation layer setup
            let animation_layer = document.createElement("div");
            animation_layer.id = "animation-layer";

            for (const targetCardId of targetCardIds) {
                let card_json = jsonGameState.visibleCardsInGame[targetCardId];
                let blueprintId = card_json.blueprintId;
                let zone = "VOID";
                let cardId = card_json.cardId;
                let noOwner = "";
                let title = (card_json.title) ? card_json.title : "";
                let imageUrl = card_json.imageUrl;
                let emptyLocationIndex = "";
                let upsideDown = false;
                let card = new Card(blueprintId, zone, cardId, noOwner, title, imageUrl, emptyLocationIndex, upsideDown);

                if (card_json.isStopped) {
                    card.addStatusToken("STOPPED");
                }
                else {
                    card.removeStatusToken("STOPPED");
                }

                let baseCardDiv = createCardDiv(card.imageUrl, card.title, card.isFoil(), card.status_tokens, false, card.hasErrata(), card.isUpsideDown(), card.cardId);

                let pageWidth = document.body.clientWidth;
                let oneSixthWidthVal = (pageWidth / 6);
                let cardWidth = oneSixthWidthVal + "px"; // 5 width
                let cardHeight = Math.floor(oneSixthWidthVal * 1.5) + "px"; // 3:2 ratio

                baseCardDiv.style.margin = "auto";
                baseCardDiv.style.width = cardWidth;
                baseCardDiv.style.height = cardHeight;
                baseCardDiv.style.flex = `0 1 ${cardWidth}`;

                animation_layer.appendChild(baseCardDiv);
            }

            let gamediv;
            if (this.game.mainDiv instanceof jQuery) {
                gamediv = this.game.mainDiv[0];
            }
            else {
                gamediv = this.game;
            }
            gamediv.appendChild(animation_layer);

            // fade in
            await this.animateElementAndSaveCSSPromise(
                animation_layer,
                [
                    { // from
                        opacity: 0,
                    },
                    { // to
                        opacity: 1,
                    },
                ],
                {
                    duration: 1000, //ms
                    fill: "forwards"
                }
            );

            // set up multiple promises, one for each overlay token
            const tokenOverlays = animation_layer.getElementsByClassName("tokenOverlay");
            let animation_promises = [];
            for (const overlay of tokenOverlays) {
                animation_promises.push(
                    overlay.animate([
                        {opacity: 1}, {opacity: 0}],
                        {direction: "alternate", duration: 500, easing: "linear", iterations: 6})
                );
            }
            
            // wait for tokens to animate
            await Promise.all(animation_promises.map((animation) => animation.finished));
            
            // fade out
            await this.animateElementAndSaveCSSPromise(
                animation_layer,
                [
                    { // from
                        opacity: 1,
                    },
                    { // to
                        opacity: 0,
                    },
                ],
                {
                    duration: 500, //ms
                    fill: "forwards"
                }
            );
            
            animation_layer.remove();
        });
    }

    animateElementAndSaveCSSPromise(element, keyframes, options) {
        return new Promise((resolve, _reject) => {
            if (options.fill == null || options.fill == "none") {
                _reject("animateElementAndSaveCSSPromise requires the options object to have fill: forwards, backwards, or both value set or it cannot save style settings.");
            }
            else {
                const animation = element.animate(keyframes, options);
                animation.onfinish = (_evt) => {
                    animation.commitStyles(); // save js settings
                    animation.cancel(); // stop any future or ongoing animations
                    resolve(); // important to put resolve() parens here
                };
            }
        });
    }

    removeCardFromPlay(cardRemovedIds, performingPlayerId, animate) {
        // This method may be called on cards that are not "in play" but visible on the board (like those in hands)
        var that = this;
        // console.log("Calling removeCardFromPlay");
        // console.log(cardRemovedIds);
        // console.log(performingPlayerId);
        // console.log(animate);

        if (animate && (this.game.spectatorMode || this.game.replayMode || (performingPlayerId != this.game.bottomPlayerId))) {
            $("#main").queue(
                function (next) {
                    for (const cardId of cardRemovedIds) {
                        // console.log("Removing card with cardId '" + cardId + "'");
                        let cardDiv = getCardDivFromId(cardId);
                        if (cardDiv.length > 0) {
                            cardDiv.animate(
                                {
                                    opacity:0
                                },
                                {
                                    duration:that.getAnimationLength(that.removeCardFromPlayDuration),
                                    easing:"easeOutQuart",
                                    queue:false
                                }
                            );
                        }
                        setTimeout(next, that.getAnimationLength(that.removeCardFromPlayDuration));
                    }
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

    gamePhaseChange(gameState) {
        var that = this;
        $("#main").queue(
            function (next) {
                let newPhase = gameState.currentPhase;
                let newPhaseName = getFriendlyPhaseName(newPhase);
                let uiPlayer = that.game.bottomPlayerId;
                let currentPhaseName = $("#currentPhase").text();
                if (that.game.allPlayerIds.includes(uiPlayer)) {
                    if (newPhase === "SEED_MISSION" && newPhaseName != currentPhaseName) {
                        // if initializing mission seed phase
                        for (const playerData of Object.values(gameState.playerMap)) {
                            if (playerData.playerId === uiPlayer) {
                                let missionPileCardIds = playerData.cardGroups["MISSIONS_PILE"].cardIds;
                                for (let i = missionPileCardIds.length - 1; i >= 0; i--) {
                                    let card = gameState.visibleCardsInGame[missionPileCardIds[i]];
                                    that.addCardToHiddenZone(card, "MISSIONS_PILE", uiPlayer);
                                }
                            }
                        }
                    } else if ((newPhase === "SEED_DILEMMA" || newPhase === "SEED_FACILITY") &&
                            currentPhaseName != "Facility seed phase" && currentPhaseName != "Dilemma seed phase") {
                            /* All dilemma and facility phase cards are put in the hand group at the beginning of
                                the dilemma seed phase, so this shouldn't be run again when the phase moves from
                                dilemma phase to facility phase. */
                        for (const playerData of Object.values(gameState.playerMap)) {
                            if (playerData.playerId === uiPlayer) {
                                let seedDeckCardIds = playerData.cardGroups["SEED_DECK"].cardIds;
                                for (const cardId of seedDeckCardIds) {
                                    let card = gameState.visibleCardsInGame[cardId];
                                    that.addCardToHiddenZone(card, "SEED_DECK", uiPlayer);
                                }
                            }
                        }
                    }
                }
                $("#currentPhase").text(newPhaseName);
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
                let playerId = json.playerOrder.currentPlayer;
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
                if (cardData != null) {
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
            }
        );
    }

    attachCardDivToTargetCardId(cardDiv, targetCardId) {
        let targetCardData = getCardDivFromId(targetCardId).data("card");
        targetCardData.attachedCards.push(cardDiv);
        cardDiv.data("card").attachedToCard = targetCardData;
    }

}