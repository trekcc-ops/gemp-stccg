import GempClientCommunication from './communication.js';
import { log, getUrlParam } from './common.js';
import Card from './jCards.js';
import { createCardDiv, createFullCardDiv, getCardDivFromId } from './jCards.js';
import { NormalCardGroup, PlayPileCardGroup, NormalGameCardGroup, TableCardGroup } from './jCardGroup.js';
import GameAnimations from './gameAnimations.js';
import ChatBoxUI from './chat.js';

export default class GameTableUI {
    padding = 5;
    spectatorMode;

    currentPlayerId;
    bottomPlayerId;
    allPlayerIds;

    gameUiInitialized = false
    cardActionDialog;
    smallDialog;
    gameStateElem;
    alertBox;
    alertText;
    alertButtons;
    infoDialog;

//    playPiles;
    hand;
    specialGroup;

    discardPileDialogs;
    discardPileGroups;
    adventureDeckDialogs;
    adventureDeckGroups;
    removedPileDialogs;
    removedPileGroups;
    miscPileDialogs;
    miscPileGroups;

    statsDiv;

    selectionFunction;

    chatBox;
    communication;
    channelNumber;

    windowWidth;
    windowHeight;

    tabPane;

    animations;
    replayPlay = false;

    constructor(url, replayMode) {
        this.replayMode = replayMode;

        log("ui initialized");
        var that = this;
        this.alertBox = $("#alertBox");
        this.alertText = $("#alertText");
        this.alertButtons = $("#alertButtons");
        this.gameStateElem = $("#gameStateElem");
        this.statsDiv = $("#statsDiv");

        this.animations = new GameAnimations(this);
        this.gameSettings = new Map();
        this.gameSettings.set("autoAccept", false);
        this.gameSettings.set("alwaysDropDown", false);
        this.gameSettings.set("foilPresentation", "static");
        this.gameSettings.set("autoPass", false);

            // TODO: LotR-specific. Replace with correct arrays for ST-specific implementations.
        this.gamePhases = new Array("Fellowship", "Shadow", "Maneuver", "Archery", "Assignment", "Skirmish", "Regroup");

        this.communication = new GempClientCommunication(url,
            function (xhr, ajaxOptions, thrownError) {
                if (thrownError != "abort") {
                    var xhr_status = "";
                    if (xhr != null) {
                        if (xhr.status == 401) {
                            that.chatBox.appendNotLoggedIntoGameMessage();
                            return;
                        } else {
                            xhr_status = " (" + xhr.status + ")";
                        }
                    }
                    that.chatBox.appendServerCommunicationProblemMessage(xhr_status);
                }
            }
        );

        $.expr[':'].cardId = function (obj, index, meta, stack) {
            var cardIds = meta[3].split(",");
            var cardData = $(obj).data("card");
            return (cardData != null && ($.inArray(cardData.cardId, cardIds) > -1));
        };

        if (this.replayMode) {
            var slowerBut = $("#slowerButton").button({
                icons: {primary: 'ui-icon-triangle-1-w'},
                text: false
            });
            var fasterBut = $("#fasterButton").button({
                icons: {primary: 'ui-icon-triangle-1-e'},
                text: false
            });
            slowerBut.click(
                function () {
                    that.animations.replaySpeed = Math.min(2, that.animations.replaySpeed + 0.2);
                });
            fasterBut.click(
                function () {
                    that.animations.replaySpeed = Math.max(0.2, that.animations.replaySpeed - 0.2);
                });

            var replayBut = $("#replayButton").button();
        } else {
            $("#replay").remove();
        }

        this.discardPileDialogs = {};
        this.discardPileGroups = {};
        this.adventureDeckDialogs = {};
        this.adventureDeckGroups = {};
        this.removedPileDialogs = {};
        this.removedPileGroups = {};
        this.miscPileDialogs = {};
        this.miscPileGroups = {};
        this.playPiles = {};
        this.onTableAreas = {};
        this.locationDivs = new Array();
        this.missionCardGroups = new Array();
        this.opponentAtLocationCardGroups = new Array();
        this.playerAtLocationCardGroups = new Array();

        this.initializeDialogs();

        this.addBottomLeftTabPane();
    }

    getReorganizableCardGroupForCardData(cardData) {
        if (cardData.zone == "ATTACHED") {
            return this.getReorganizableCardGroupForCardData(cardData.attachedToCard);
        }
        for (var i=0; i < this.missionCardGroups.length; i++) {
            if (this.missionCardGroups[i].cardBelongs(cardData)) {
                return this.missionCardGroups[i];
            }
        }
        for (var i=0; i < this.opponentAtLocationCardGroups.length; i++) {
            if (this.opponentAtLocationCardGroups[i].cardBelongs(cardData)) {
                return this.opponentAtLocationCardGroups[i];
            }
        }
        for (var i=0; i < this.playerAtLocationCardGroups.length; i++) {
            if (this.playerAtLocationCardGroups[i].cardBelongs(cardData)) {
                return this.playerAtLocationCardGroups[i];
            }
        }
        for (var [playerId, cardGroup] of Object.entries(this.playPiles)) {
            if (cardGroup.cardBelongs(cardData)) {
                return cardGroup;
            }
        }
        for (var [playerId, cardGroup] of Object.entries(this.onTableAreas)) {
            if (cardGroup.cardBelongs(cardData)) {
                return cardGroup;
            }
        }
        if (this.hand != null)
            if (this.hand.cardBelongs(cardData)) {
                return this.hand;
            }
        return null;
    }

    layoutGroupWithCard(cardId) {
        var cardData = getCardDivFromId(cardId).data("card");
        var tempGroup = this.getReorganizableCardGroupForCardData(cardData);
        if (tempGroup != null) {
            tempGroup.layoutCards();
            return;
        }
        this.layoutUI(false);
    }

    initializeGameUI(discardPublic) {
        var that = this;

//        this.advPathGroup = new AdvPathCardGroup($("#main"));

        for (var i = 0; i < this.allPlayerIds.length; i++) {

            this.gameStateElem.append(
                "<div class='player'>" + (i + 1) + ". " + this.allPlayerIds[i] +
                "<div id='clock" + i + "' class='clock'></div>" +
                "<div class='playerStats'>" +
                    "<div id='deck" + i + "' class='deckSize'></div>" +
                    "<div id='hand" + i + "' class='handSize'></div>" +
                    "<div id='discard" + i + "' class='discardSize'></div>" +
                    "<div id='score" + i + "' class='playerScore'></div>" +
                "</div></div></div>");

            var showBut = $("<div class='slimButton'>+</div>").button().click(
                (function (playerIndex) {
                    return function () {
                        $(".player").each(
                            function (index) {
                                if (index == playerIndex) {
                                    if ($(this).hasClass("opened")) {
                                        $(this).removeClass("opened").css({width: 150 - that.padding});
                                        $("#adventureDeck" + playerIndex).css({display: "none"});
                                        $("#removedPile" + playerIndex).css({display: "none"});
                                    } else {
                                        $(this).addClass("opened").css({width: 150 - that.padding + 168});
                                        $("#adventureDeck" + playerIndex).css({display: "table-cell"});
                                        $("#removedPile" + playerIndex).css({display: "table-cell"});
                                    }
                                }
                            });
                    };
                })(i));

            $("#showStats" + i).append(showBut);

            this.playPiles[this.allPlayerIds[i]] = new PlayPileCardGroup(
                $("#main"),
                this.allPlayerIds[i],
                function (card) {
                    return (card.zone == "PLAY_PILE");
                },
                "playPileDiv_" + this.allPlayerIds[i]
            );

            this.onTableAreas[this.allPlayerIds[i]] = new NormalGameCardGroup(
                $("#main"),
                this.allPlayerIds[i],
                function (card) {
                    return (card.zone == "TABLE");
                },
                "tableAreaDiv_" + this.allPlayerIds[i]
            );

            $("#removedPile" + i).addClass("clickable").click(
                (function (index) {
                    return function () {
                        var dialog = that.removedPileDialogs[that.allPlayerIds[index]];
                        var group = that.removedPileGroups[that.allPlayerIds[index]];
                        openSizeDialog(dialog);
                        that.dialogResize(dialog, group);
                        group.layoutCards();
                    };
                })(i));

            if(discardPublic) {
                $("#discard" + i).addClass("clickable").click(
                    (function (index) {
                        return function () {
                            var dialog = that.discardPileDialogs[that.allPlayerIds[index]];
                            var group = that.discardPileGroups[that.allPlayerIds[index]];
                            openSizeDialog(dialog);
                            that.dialogResize(dialog, group);
                            group.layoutCards();
                        };
                    })(i));
            }
        }

        this.statsDiv.append("<div class='tribbleSequence'>1</div>");

        this.specialGroup = new NormalCardGroup(this.cardActionDialog, function (card) {
            return (card.zone == "SPECIAL");
        }, false);
        this.specialGroup.setBounds(this.padding, this.padding, 580 - 2 * (this.padding), 250 - 2 * (this.padding))

        if (!this.spectatorMode) {
            this.hand = new NormalCardGroup($("#main"), function (card) {
                return (card.zone == "HAND") || (card.zone == "EXTRA");
            });
            if(!discardPublic) {
                $("#discard" + this.getPlayerIndex(this.bottomPlayerId)).addClass("clickable").click(
                    (function (index) {
                        return function () {
                            var dialog = that.discardPileDialogs[index];
                            var group = that.discardPileGroups[index];
                            openSizeDialog(dialog);
                            that.dialogResize(dialog, group);
                            group.layoutCards();
                        };
                    })(that.bottomPlayerId));
            }
            $("#adventureDeck" + this.getPlayerIndex(this.bottomPlayerId)).addClass("clickable").click(
                (function (index) {
                    return function () {
                        var dialog = that.adventureDeckDialogs[index];
                        var group = that.adventureDeckGroups[index];
                        openSizeDialog(dialog);
                        that.dialogResize(dialog, group);
                        group.layoutCards();
                    };
                })(that.bottomPlayerId));
        }

        var dragFunc = function (event) {
            return that.dragContinuesCardFunction(event);
        };

        $("body").click(
            function (event) {
                return that.clickCardFunction(event);
            });
        var test = $("body");
        $("body")[0].addEventListener("contextmenu",
            function (event) {
                if(!that.clickCardFunction(event))
                {
                    event.preventDefault();
                    return false;
                }
                return true;
            });
        $("body").mousedown(
            function (event) {
                $("body").bind("mousemove", dragFunc);
                return that.dragStartCardFunction(event);
            });
        $("body").mouseup(
            function (event) {
                $("body").unbind("mousemove", dragFunc);
                return that.dragStopCardFunction(event);
            });

        this.gameUiInitialized = true;

    }

    processGameEnd() {
        var that = this;
        if(this.allPlayerIds == null)
            return;

        $("#deck" + this.getPlayerIndex(this.bottomPlayerId)).addClass("clickable").click(
            (function (index) {
                return function () {
                    var dialog = that.miscPileDialogs[index];
                    var group = that.miscPileGroups[index];
                    openSizeDialog(dialog);
                    that.dialogResize(dialog, group);
                    group.layoutCards();
                };
            })(that.bottomPlayerId));
    }

    addBottomLeftTabPane() {
        var that = this;

        if (this.replayMode) {
            $("#settingsBoxTab").remove();
            $("#gameOptionsTab").remove();
            $("#playersInRoomTab").remove();
            $("#settingsBox").remove();
            $("#gameOptionsBox").remove();
            $("#playersInRoomBox").remove();
        }

        this.tabPane = $("#bottomLeftTabs").tabs();

        // Process game settings
        for (var setting of that.gameSettings.entries()) {
            var settingName = setting[0];
            if (settingName != "autoPass") { // TODO: currently, autoPass always set to false
                var optionSelection = $("#" + settingName);
                var cookie = $.cookie(settingName);

                    // Multiple choice settings: foilPresentation
                if (settingName == "foilPresentation" && cookie != null) {
                    optionSelection.val(cookie);
                    that.gameSettings.set(settingName, cookie);
                }

                    // True/false settings: autoAccept, alwaysDropDown
                if (cookie == "true" || cookie == null) {
                    optionSelection.prop("checked", true);
                    that.gameSettings.set(settingName, true);
                }

                optionSelection.bind("change", function() {
                    var userSelection = null;
                    if (settingName == "foilPresentation") {
                        userSelection = "" + optionSelection.val(); // Multiple choice
                    } else {
                        userSelection = optionSelection.prop("checked"); // True/false
                    }
                    that.gameSettings.set(settingName, userSelection);
                    $.cookie(settingName, "" + userSelection, {expires: 365});
                });
            }
        }

        // Create arrays for phase-specific functions
        var allPhaseNames = that.gamePhases;
        var autoPassArr = new Array();
        var autoPassArrHashtag = new Array();
        for (var i = 0; i < allPhaseNames.length; i++) {
            autoPassArr.push("autoPass" + allPhaseNames[i]);
            autoPassArrHashtag.push("#autoPass" + allPhaseNames[i]);
        }

        // Load auto-pass settings from cookie, or set to default (current default is all phases auto-pass)
        var currPassedPhases = new Array();
        var currAutoPassCookie = $.cookie("autoPassPhases");
        if (currAutoPassCookie == null) {
            currPassedPhases = allPhaseNames;
        } else {
            currPassedPhases = currAutoPassCookie.split("0");
        }

        // Create settings panel for user selection of auto-pass settings
        for (var i = 0; i < allPhaseNames.length; i++) {
            $("#autoPassOptionsDiv").append(
                "<input id='" + autoPassArr[i] + "' type='checkbox' value='selected' />" +
                "<label for='" + autoPassArr[i] + "'>" + allPhaseNames[i] + "</label> "
            );
        }

        // Populate settings panel with current user options
        for (var i = 0; i < currPassedPhases.length; i++) {
            $(autoPassArrHashtag[i]).prop("checked", true);
        }

        // Save user selections to cookie
        $(autoPassArrHashtag.join(",")).bind("change", function () {
            var newAutoPassPhases = "";
            for (var i = 0; i < allPhaseNames.length; i++) {
                if ($("#autoPass" + allPhaseNames[i]).prop("checked"))
                    newAutoPassPhases += "0" + allPhaseNames[i];
            }
            if (newAutoPassPhases.length > 0)
                newAutoPassPhases = newAutoPassPhases.substr(1);
            $.cookie("autoPassPhases", newAutoPassPhases, {expires: 365});
        });

        var playerListener = function (players) {
            var val = "";
            for (var i = 0; i < players.length; i++)
                val += players[i] + "<br/>";
            $("a[href='#playersInRoomBox']").html("Players(" + players.length + ")");
            $("#playersInRoomBox").html(val);
        };

        var displayChatListener = function(title, message) {

            var dialog = $("<div></div>").dialog({
                title: title,
                resizable: true,
                height: 200,
                modal: true,
                buttons: {}
            }).html(message);
        }

        var chatRoomName = (this.replayMode ? null : ("Game" + getUrlParam("gameId")));
        this.chatBox = new ChatBoxUI(
            chatRoomName, $("#chatBox"), this.communication.url, false, playerListener, false, displayChatListener
        );
        this.chatBox.chatUpdateInterval = 3000;

        if (!this.spectatorMode && !this.replayMode) {
            $("#concedeGame").button().click(
                function () {
                    that.communication.concede();
                });
            $("#cancelGame").button().click(
                function () {
                    that.communication.cancel();
                });
        }
    }

    clickCardFunction(event) {
        var tar = $(event.target);

        if (tar.hasClass("cardHint")) {
            var blueprintId = tar.attr("value");
            var imageUrl = tar.attr("card_img_url");
            var card = new Card(blueprintId, "SPECIAL", "hint", "", imageUrl);
            this.displayCard(card, false);
            event.stopPropagation();
            return false;
        }

        if (!this.successfulDrag && this.infoDialog.dialog("isOpen")) {
            this.infoDialog.dialog("close");
            event.stopPropagation();
            return false;
        }

        if (tar.hasClass("actionArea")) {
            var selectedCardElem = tar.closest(".card");
            if (!this.successfulDrag) {
                if (event.shiftKey || event.which > 1) {
                    this.displayCardInfo(selectedCardElem.data("card"));
                } else if (
                        (selectedCardElem.hasClass("selectableCard") || selectedCardElem.hasClass("actionableCard")) &&
                        !this.replayMode
                    )
                    this.selectionFunction(selectedCardElem.data("card").cardId, event);
                event.stopPropagation();
            }
            return false;
        }

        return true;
    }

    dragCardId;
    dragCardIndex;
    draggedCardIndex;
    dragStartX;
    dragStartY;
    successfulDrag;
    draggingHorizontaly = false;

    dragStartCardFunction(event) {
        this.successfulDrag = false;
        var tar = $(event.target);
        if (tar.hasClass("actionArea")) {
            var selectedCardElem = tar.closest(".card");
            if (event.which == 1) {
                var cardData = selectedCardElem.data("card");
                if (cardData) {
                    this.dragCardId = cardData.cardId;
                    this.dragStartX = event.clientX;
                    this.dragStartY = event.clientY;
                    return false;
                }
            }
        }
        return true;
    }

    dragContinuesCardFunction(event) {
        if (this.dragCardId != null) {
            if (!this.draggingHorizontaly && Math.abs(this.dragStartX - event.clientX) >= 20) {
                var cardElems = getCardDivFromId(this.dragCardId);
                if (cardElems.length > 0) {
                    var cardElem = cardElems[0];
                    var cardData = $(cardElem).data("card");
                    this.draggingHorizontaly = true;
                    var cardGroup = this.getReorganizableCardGroupForCardData(cardData);
                    if (cardGroup != null) {
                        var cardsInGroup = cardGroup.getCardElems();
                        for (var i = 0; i < cardsInGroup.length; i++)
                            if (cardsInGroup[i].data("card").cardId == this.dragCardId) {
                                this.dragCardIndex = i;
                                this.draggedCardIndex = i;
                                break;
                            }
                    }
                }
            }
            if (this.draggingHorizontaly && this.dragCardId != null && this.dragCardIndex != null) {
                var cardElems = getCardDivFromId(this.dragCardId);
                if (cardElems.length > 0) {
                    var cardElem = $(cardElems[0]);
                    var cardData = cardElem.data("card");
                    var cardGroup = this.getReorganizableCardGroupForCardData(cardData);
                    if (cardGroup != null) {
                        var cardsInGroup = cardGroup.getCardElems();
                        var width = cardElem.width();
                        var currentIndex;
                        if (event.clientX < this.dragStartX)
                            currentIndex = this.dragCardIndex - Math.floor((this.dragStartX - event.clientX) / width);
                        else
                            currentIndex = this.dragCardIndex + Math.floor((event.clientX - this.dragStartX) / width);

                        if (currentIndex < 0)
                            currentIndex = 0;
                        if (currentIndex >= cardsInGroup.length)
                            currentIndex = cardsInGroup.length - 1;

                        var cardIdAtIndex = $(cardsInGroup[currentIndex]).data("card").cardId;
                        if (cardIdAtIndex != this.dragCardId) {
                            if (currentIndex < this.draggedCardIndex)
                                $(".card:cardId(" + cardIdAtIndex + ")").before(getCardDivFromId(this.dragCardId));
                            else
                                $(".card:cardId(" + cardIdAtIndex + ")").after(getCardDivFromId(this.dragCardId));
                            cardGroup.layoutCards();
                            this.draggedCardIndex = currentIndex;
                        }
                    }
                }
            }
        }
    }

    dragStopCardFunction(event) {
        if (this.dragCardId != null) {
            if (this.dragStartY - event.clientY >= 20 && !this.draggingHorizontaly) {
                var cardElems = getCardDivFromId(this.dragCardId);
                if (cardElems.length > 0) {
                    this.displayCardInfo($(cardElems[0]).data("card"));
                    this.successfulDrag = true;
                }
            }
            this.dragCardId = null;
            this.dragCardIndex = null;
            this.draggedCardIndex = null;
            this.dragStartX = null;
            this.dragStartY = null;
            this.draggingHorizontaly = false;
            return false;
        }
        return true;
    }

    displayCard(card, extraSpace) {
        this.infoDialog.html("");
        this.infoDialog.html("<div style='scroll: auto'></div>");
        var floatCardDiv = $("<div style='float: left;'></div>");
        floatCardDiv.append(createFullCardDiv(card.imageUrl, card.foil, card.horizontal));
        this.infoDialog.append(floatCardDiv);
        if (extraSpace) {
            this.infoDialog.append("<div id='cardEffects'></div>");
        }

        var windowWidth = $(window).width();
        var windowHeight = $(window).height();

        var horSpace = (extraSpace ? 200 : 0) + 30;
        var vertSpace = 45;

        if (card.horizontal) {
            // 500x360
            this.infoDialog.dialog({
                width: Math.min(500 + horSpace, windowWidth),
                height: Math.min(360 + vertSpace, windowHeight)
            });
        } else {
            // 360x500
            this.infoDialog.dialog({
                width: Math.min(360 + horSpace, windowWidth),
                height: Math.min(500 + vertSpace, windowHeight)
            });
        }
        this.infoDialog.dialog("open");
    }

    displayCardInfo(card) {
        var showModifiers = false;
        var cardId = card.cardId;
        if (!this.replayMode && (cardId.length < 4 || cardId.substring(0, 4) != "temp"))
            showModifiers = true;

        this.displayCard(card, showModifiers);

        if (showModifiers)
            this.getCardModifiersFunction(cardId, this.setCardModifiers);
    }

    setCardModifiers(html) {
        $("#cardEffects").append(html);
        $("#cardEffects").addClass("cardInfoText");
    }

    initializeDialogs() {
        this.smallDialog = $("<div></div>")
            .dialog({
                autoOpen: false,
                closeOnEscape: false,
                resizable: false,
                width: 400,
                height: 200
            });

        this.cardActionDialog = $("<div></div>")
            .dialog({
                autoOpen: false,
                closeOnEscape: false,
                resizable: true,
                width: 600,
                height: 300
            });

        var that = this;

        this.cardActionDialog.bind("dialogresize", function () {
            that.arbitraryDialogResize();
        });

        $(".ui-dialog-titlebar-close").hide();

        var width = $(window).width();
        var height = $(window).height();

        this.infoDialog = $("<div></div>")
            .dialog({
                autoOpen: false,
                closeOnEscape: true,
                resizable: false,
                title: "Card information"
            });

        var swipeOptions = {
            threshold: 20,
            swipeUp: function (event) {
                that.infoDialog.prop({scrollTop: that.infoDialog.prop("scrollHeight")});
                return false;
            },
            swipeDown: function (event) {
                that.infoDialog.prop({scrollTop: 0});
                return false;
            }
        };
        this.infoDialog.swipe(swipeOptions);
    }

    windowResized() {
        this.animations.windowResized();
    }

    startReplaySession(replayId) {
        var that = this;
        this.communication.getReplay(replayId, function (xml) { that.processXmlReplay(xml, true); });
    }

    startGameSession() {
        var that = this;
        this.communication.startGameSession(
            function (xml) {
                that.processXml(xml, false);
            }, this.gameErrorMap());
    }

    updateGameState() {
        var that = this;
        this.communication.updateGameState(
            this.channelNumber,
            function (xml) {
                that.processXml(xml, true);
            }, this.gameErrorMap());
    }

    decisionFunction(decisionId, result) {
        var that = this;
        this.stopAnimatingTitle();
        this.communication.gameDecisionMade(decisionId, result,
            this.channelNumber,
            function (xml) {
                that.processXml(xml, true);
            }, this.gameErrorMap());
    }

    gameErrorMap() {
        var that = this;
        return {
            "0": function () {
                that.showErrorDialog(
                    "Server connection error",
                    "Unable to connect to server. " +
                        "Either server is down or there is a problem with your internet connection.",
                    true, false, false
                );
            },
            "401": function () {
                that.showErrorDialog("Authentication error", "You are not logged in", false, true, false);
            },
            "403": function () {
                that.showErrorDialog(
                    "Game access forbidden", "This game is private and does not allow spectators.", false, false, true
                );
            },
            "409": function () {
                that.showErrorDialog(
                    "Concurrent access error",
                    "You are observing this Game Hall from another browser or window. " +
                        "Close this window or if you wish to observe it here, click \"Refresh page\".",
                    true, false, false
                );
            },
            "410": function () {
                that.showErrorDialog(
                    "Inactivity error",
                    "You were inactive for too long and have been removed from observing this game. " +
                        "If you wish to start again, click \"Refresh page\".",
                    true, false, false
                );
            }
        };
    }

    showErrorDialog(title, text, reloadButton, mainPageButton, gameHallButton) {
        var buttons = {};
        if (reloadButton) {
            buttons["Refresh page"] =
                function () {
                    location.reload(true);
                };
        }
        if (mainPageButton) {
            buttons["Go to main page"] =
                function () {
                    location.href = "/gemp-module/";
                };
        }
        if (gameHallButton) {
            buttons["Go to Game Hall"] =
                function () {
                    location.href = "/gemp-module/hall.html";
                };
        }

        var dialog = $("<div></div>").dialog({
            title: title,
            resizable: false,
            height: 160,
            modal: true,
            buttons: buttons
        }).text(text);
    }

    getCardModifiersFunction(cardId, func) {
        var that = this;
        this.communication.getGameCardModifiers(cardId,
            function (html) {
                that.setCardModifiers(html);
            });
    }

    processXml(xml, animate) {
        log(xml);
        var root = xml.documentElement;
        if (root.tagName == 'gameState' || root.tagName == 'update')
            this.processGameEventsXml(root, animate);
    }

    replayGameEventNextIndex = 0;
    replayGameEvents;

    processXmlReplay(xml, animate) {
        var that = this;
        log(xml);
        var root = xml.documentElement;
        if (root.tagName == 'gameReplay') {
            this.replayGameEvents = root.getElementsByTagName("ge");
            this.replayGameEventNextIndex = 0;

            $("#replayButton").click(
                function () {
                    if (that.replayPlay) {
                        that.replayPlay = false;
                        $("#replayButton").attr("src", "images/play.png");
                    } else {
                        that.replayPlay = true;
                        $("#replayButton").attr("src", "images/pause.png");
                        that.playNextReplayEvent();
                    }
                });

            this.playNextReplayEvent();
        }
    }

    shouldPlay() {
        return this.replayPlay;
    }

    playNextReplayEvent() {
        if (this.shouldPlay()) {
            var that = this;
            if (this.replayGameEventNextIndex < this.replayGameEvents.length) {
                $("#main").queue(
                    function (next) {
                        that.cleanupDecision();
                        next();
                    });
                var gameEvent = this.replayGameEvents[this.replayGameEventNextIndex];
                this.processGameEvent(gameEvent, true);

                this.replayGameEventNextIndex++;

                $("#main").queue(
                    function (next) {
                        that.playNextReplayEvent();
                        next();
                    });
            }
        }
    }

    processGameEvent(gameEvent, animate) {
        var eventType = gameEvent.getAttribute("type");
        if (eventType == "PCIP") {
            this.animations.putCardIntoPlay(gameEvent, animate, eventType);
        } else if (eventType == "PUT_SHARED_MISSION_INTO_PLAY") {
            this.animations.putCardIntoPlay(gameEvent, animate, eventType);
        } else if (eventType == "MCIP") {
            this.animations.moveCardInPlay(gameEvent); // No animation exists for this event
        } else if (eventType == "P") {
            this.participant(gameEvent);
        } else if (eventType == "RCFP") {
            this.animations.removeCardFromPlay(gameEvent, animate);
        } else if (eventType == "UPDATE_CARD_IMAGE") {
            this.animations.updateCardImage(gameEvent);
        } else if (eventType == "GPC") {
            this.animations.gamePhaseChange(gameEvent, animate);
        } else if (eventType == "TC") {
            this.animations.turnChange(gameEvent, animate);
        } else if (eventType == "GS") {
            this.animations.gameStats(gameEvent, animate);
        } else if (eventType == "M") {
            this.animations.message(gameEvent, animate);
        } else if (eventType == "W") {
            this.animations.warning(gameEvent, animate);
        } else if (eventType == "CAC") {
            this.animations.cardAffectsCard(gameEvent, animate);
        } else if (eventType == "EP") {
            this.animations.eventPlayed(gameEvent, animate);
        } else if (eventType == "CA") {
            this.animations.cardActivated(gameEvent, animate);
        } else if (eventType == "D") {
            this.animations.processDecision(gameEvent, animate);
        } else if (eventType = "TSEQ") {
            this.animations.tribbleSequence(gameEvent, animate);
        } else if (eventType = "PLAYER_SCORE") {
            this.animations.playerScore(gameEvent, animate);
        }
        else if (eventType == "EG") {
            this.processGameEnd();
        }
    }

    processGameEventsXml(element, animate) {
        try {
            this.channelNumber = element.getAttribute("cn");

            var gameEvents = element.getElementsByTagName("ge");

            var hasDecision = false;

            // Go through all the events
            for (var i = 0; i < gameEvents.length; i++) {
                var gameEvent = gameEvents[i];
                this.processGameEvent(gameEvent, animate);
                var eventType = gameEvent.getAttribute("type");
                if (eventType == "D")
                    hasDecision = true;
            }

            if (this.allPlayerIds != null) {
                var clocksXml = element.getElementsByTagName("clocks");
                if (clocksXml.length > 0) {
                    var clocks = clocksXml[0].getElementsByTagName("clock");
                    for (var i = 0; i < clocks.length; i++) {
                        var clock = clocks[i];
                        var participantId = clock.getAttribute("participantId");
                        var index = this.getPlayerIndex(participantId);

                        var value = parseInt(clock.childNodes[0].nodeValue);

                        var sign = (value < 0) ? "-" : "";
                        value = Math.abs(value);
                        var hours = Math.floor(value / 3600);
                        var minutes = Math.floor(value / 60) % 60;
                        var seconds = value % 60;

                        if (hours > 0)
                            $("#clock" + index).text(
                                sign + hours + ":" +
                                ((minutes < 10) ? ("0" + minutes) : minutes) + ":" +
                                ((seconds < 10) ? ("0" + seconds) : seconds)
                            );
                        else
                            $("#clock" + index).text(
                                sign + minutes + ":" +
                                ((seconds < 10) ? ("0" + seconds) : seconds)
                            );
                    }
                }
            }

            if (!hasDecision) {
                this.animations.updateGameState(animate);
            } else {
                this.startAnimatingTitle();
            }
        } catch (e) {
            console.error(e);
            this.showErrorDialog(
                "Game error",
                "There was an error while processing game events in your browser. Reload the game to continue",
                true, false, false
            );
        }
    }

    keepAnimating = false;

    startAnimatingTitle() {
        var that = this;
        this.keepAnimating = true;
        setTimeout(function () {
            that.setAlternatingTitle();
        }, 500);
    }

    setAlternatingTitle() {
        if (this.keepAnimating) {
            if (window.document.title == "Game of Star Trek CCG") {
                window.document.title = "Waiting for your decision";
            } else {
                window.document.title = "Game of Star Trek CCG";
            }
            var that = this;
            setTimeout(function () {
                that.setAlternatingTitle();
            }, 500);
        }
    }

    stopAnimatingTitle() {
        this.keepAnimating = false;
        window.document.title = "Game of Star Trek CCG";
    }

    getPlayerIndex(playerId) {
        for (var plId = 0; plId < this.allPlayerIds.length; plId++)
            if (this.allPlayerIds[plId] == playerId)
                return plId;
        return -1;
    }

    layoutZones() {
//        this.advPathGroup.layoutCards();
        for (var [playerId, cardGroup] of Object.entries(this.playPiles)) {
            cardGroup.layoutCards();
        }
        if (!this.spectatorMode)
            this.hand.layoutCards();
    }

    participant(element) {
        var participantId = element.getAttribute("participantId");
        this.allPlayerIds = element.getAttribute("allParticipantIds").split(",");
        var discardPublic = element.getAttribute("discardPublic") === 'true';

        this.bottomPlayerId = participantId;

        var that = this;

        var index = this.getPlayerIndex(this.bottomPlayerId);
        if (index == -1) {
            this.bottomPlayerId = this.allPlayerIds[1];
            this.spectatorMode = true;
        } else {
            this.spectatorMode = false;

            if(!discardPublic) {
                this.createPile(participantId, "Discard Pile", "discardPileDialogs", "discardPileGroups");
            }

            this.createPile(participantId, "Draw Deck", "miscPileDialogs", "miscPileGroups");
        }

        for (var i = 0; i < this.allPlayerIds.length; i++) {

            participantId = this.allPlayerIds[i];

            this.createPile(participantId, "'Removed From Game' Pile", "removedPileDialogs", "removedPileGroups");

            if(discardPublic) {
                this.createPile(participantId, "Discard Pile", "discardPileDialogs", "discardPileGroups");
            }
        }

        this.initializeGameUI(discardPublic);
        this.layoutUI(true);
    }

    createPile(playerId, name, dialogsName, groupsName) {
        var dialog = $("<div></div>").dialog({
            autoOpen: false,
            closeOnEscape: true,
            resizable: true,
            title: name + " - " + playerId,
            minHeight: 80,
            minWidth: 200,
            width: 600,
            height: 300
        });

        this[dialogsName][playerId] = dialog;
        this[groupsName][playerId] = new NormalCardGroup(dialog, function (card) {
            return true;
        }, false);

        this[groupsName][playerId].setBounds(
            this.padding, this.padding, 580 - 2 * (this.padding), 250 - 2 * (this.padding)
        );

        var that = this;

        dialog.bind("dialogresize", function () {
            that.dialogResize(dialog, that[groupsName][playerId]);
        });
    }

    getDecisionParameter(decision, name) {
        var parameters = decision.getElementsByTagName("parameter");
        for (var i = 0; i < parameters.length; i++)
            if (parameters[i].getAttribute("name") == name)
                return parameters[i].getAttribute("value");

        return null;
    }

    getDecisionParameters(decision, name) {
        var result = new Array();
        var parameters = decision.getElementsByTagName("parameter");
        for (var i = 0; i < parameters.length; i++)
            if (parameters[i].getAttribute("name") == name)
                result.push(parameters[i].getAttribute("value"));

        return result;
    }

    cleanupDecision() {
        this.smallDialog.dialog("close");
        this.cardActionDialog.dialog("close");
        this.clearSelection();
        if (this.alertText != null)
            this.alertText.html("");
        if (this.alertButtons != null)
            this.alertButtons.html("");
        if (this.alertBox != null) {
            this.alertBox.removeClass("alert-box-highlight");
            this.alertBox.removeClass("alert-box-card-selection");
        }

        $(".card").each(
            function () {
                var card = $(this).data("card");
                if (card.zone == "EXTRA")
                    $(this).remove();
            });
        if (this.hand != null)
            this.hand.layoutCards();
    }

    integerDecision(decision) {
        var id = decision.getAttribute("id");
        var text = decision.getAttribute("text");
        var val = 0;

        var min = this.getDecisionParameter(decision, "min");
        if (min == null)
            min = 0;
        var max = this.getDecisionParameter(decision, "max");
        if (max == null)
            max = 1000;

        var defaultValue = this.getDecisionParameter(decision, "defaultValue");
        if (defaultValue != null)
            val = parseInt(defaultValue);

        var that = this;
        this.smallDialog
            .html(text + "<br /><input id='integerDecision' type='text' value='0'>");

        if (!this.replayMode) {
            this.smallDialog.dialog("option", "buttons",
                {
                    "OK": function () {
                        $(this).dialog("close");
                        that.decisionFunction(id, $("#integerDecision").val());
                    }
                });
        }

        $("#integerDecision").SpinnerControl({
            type: 'range',
            typedata: {
                min: parseInt(min),
                max: parseInt(max),
                interval: 1,
                decimalplaces: 0
            },
            defaultVal: val,
            width: '50px',
            backColor: "#000000"
        });

        this.smallDialog.dialog("open");
        $('.ui-dialog :button').blur();
    }

    multipleChoiceDecision(decision) {
        var id = decision.getAttribute("id");
        var text = decision.getAttribute("text");

        var results = this.getDecisionParameters(decision, "results");

        var that = this;
        this.smallDialog
            .html(text);

        if (results.length > 2 || this.gameSettings.get("alwaysDropDown")) {
            var html = "<br /><select id='multipleChoiceDecision' selectedIndex='0'>";
            for (var i = 0; i < results.length; i++)
                html += "<option value='" + i + "'>" + results[i] + "</option>";
            html += "</select>";
            this.smallDialog.append(html);

            if (!this.replayMode) {
                this.smallDialog.dialog("option", "buttons",
                    {
                        "OK": function () {
                            that.smallDialog.dialog("close");
                            that.decisionFunction(id, $("#multipleChoiceDecision").val());
                        }
                    });
            }
        } else {
            this.smallDialog.append("<br />");
            for (var i = 0; i < results.length; i++) {
                if (i > 0)
                    this.smallDialog.append(" ");

                var but = $("<button></button>").html(results[i]).button();
                if (!this.replayMode) {
                    but.click(
                        (function (ind) {
                            return function () {
                                that.smallDialog.dialog("close");
                                that.decisionFunction(id, "" + ind);
                            }
                        })(i));
                }
                this.smallDialog.append(but);
            }
            if (!this.replayMode)
            {
                this.smallDialog.dialog("option", "buttons", {});
                this.PlaySound("awaitAction");
            }
        }

        this.smallDialog.dialog("open");
        $('.ui-dialog :button').blur();
    }

    ensureCardHasBoxes(cardDiv) {
        if ($(".cardStrength", cardDiv).length == 0) {
            var tokenOverlay = $(".tokenOverlay", cardDiv);

            var cardStrengthBgDiv = $(
                "<div class='cardStrengthBg'><img src='images/o_icon_strength.png' width='100%' height='100%'></div>"
            );
            tokenOverlay.append(cardStrengthBgDiv);

            var cardStrengthDiv = $("<div class='cardStrength'></div>");
            tokenOverlay.append(cardStrengthDiv);

            var cardVitalityBgDiv = $(
                "<div class='cardVitalityBg'><img src='images/o_icon_vitality.png' width='100%' height='100%'></div>"
            );
            tokenOverlay.append(cardVitalityBgDiv);

            var cardVitalityDiv = $("<div class='cardVitality'></div>");
            tokenOverlay.append(cardVitalityDiv);

            var cardSiteNumberBgDiv = $(
                "<div class='cardSiteNumberBg'><img src='images/o_icon_compass.png' width='100%' height='100%'></div>"
            );
            cardSiteNumberBgDiv.css({display: "none"});
            tokenOverlay.append(cardSiteNumberBgDiv);

            var cardSiteNumberDiv = $("<div class='cardSiteNumber'></div>");
            cardSiteNumberDiv.css({display: "none"});
            tokenOverlay.append(cardSiteNumberDiv);

            var cardResistanceBgDiv = $(
                "<div class='cardResistanceBg'><img src='images/o_icon_resistance.png' width='100%' height='100%'></div>"
            );
            cardResistanceBgDiv.css({display: "none"});
            tokenOverlay.append(cardResistanceBgDiv);

            var cardResistanceDiv = $("<div class='cardResistance'></div>");
            cardResistanceDiv.css({display: "none"});
            tokenOverlay.append(cardResistanceDiv);

            var sizeListeners = new Array();
            sizeListeners[0] = {
                sizeChanged: function (cardElem, width, height) {
                    var maxDimension = Math.max(width, height);

                    var size = 0.0865 * maxDimension;

                    var x = 0.09 * maxDimension - size / 2;
                    var strengthY = 0.688 * maxDimension - size / 2;
                    var vitalityY = 0.800 * maxDimension - size / 2;
                    var minionSiteNumberY = 0.905 * maxDimension - size / 2;

                    var fontPerc = (size * 5.5) + "%";
                    var borderRadius = Math.ceil(size / 5) + "px";

                    var strBgX = 0.03800 * maxDimension;
                    var strBgY = 0.60765 * maxDimension;
                    var strBgWidth = 0.1624 * width;
                    var strBgHeight = 0.1650 * height;

                    var vitBgX = 0.0532 * width;
                    var vitBgY = 0.7465 * height;
                    var vitalityBgSize = 0.105 * height;

                    var thirdBoxX = 0.0532 * width;
                    var thirdBoxY = 0.845 * height;
                    var thirdBoxSize = 0.115 * height;

                    $(".cardStrengthBg", cardElem).css({
                        position: "absolute",
                        left: strBgX + "px",
                        top: strBgY + "px",
                        width: strBgWidth,
                        height: strBgHeight
                    });
                    $(".cardStrength", cardElem).css({
                        position: "absolute",
                        "font-size": fontPerc,
                        left: x + "px",
                        top: strengthY + "px",
                        width: size,
                        height: size
                    });
                    $(".cardVitalityBg", cardElem).css({
                        position: "absolute",
                        left: vitBgX + "px",
                        top: vitBgY + "px",
                        width: vitalityBgSize,
                        height: vitalityBgSize
                    });
                    $(".cardVitality", cardElem).css({
                        position: "absolute",
                        "font-size": fontPerc,
                        left: x + "px",
                        top: vitalityY + "px",
                        width: size,
                        height: size
                    });
                    $(".cardSiteNumberBg", cardElem).css({
                        position: "absolute",
                        left: thirdBoxX + "px",
                        top: thirdBoxY + "px",
                        width: thirdBoxSize,
                        height: thirdBoxSize
                    });
                    $(".cardSiteNumber", cardElem).css({
                        position: "absolute",
                        "border-radius": borderRadius,
                        "font-size": fontPerc,
                        left: x + "px",
                        top: minionSiteNumberY + "px",
                        width: size,
                        height: size
                    });
                    $(".cardResistanceBg", cardElem).css({
                        position: "absolute",
                        left: thirdBoxX + "px",
                        top: thirdBoxY + "px",
                        width: thirdBoxSize,
                        height: thirdBoxSize
                    });
                    $(".cardResistance", cardElem).css({
                        position: "absolute",
                        "border-radius": borderRadius,
                        "font-size": fontPerc,
                        left: x + "px",
                        top: minionSiteNumberY + "px",
                        width: size,
                        height: size
                    });
                }
            };

            cardDiv.data("sizeListeners", sizeListeners);
            sizeListeners[0].sizeChanged(cardDiv, $(cardDiv).width(), $(cardDiv).height());
        }
    }

    createCardDiv(card, text) {
        var cardDiv = createCardDiv(card.imageUrl, text, card.isFoil(), true, false, card.hasErrata(), card.isUpsideDown(), card.cardId);

        cardDiv.data("card", card);

        var that = this;
        var swipeOptions = {
            threshold: 20,
            fallbackToMouseEvents: false,
            swipeUp: function (event) {
                var tar = $(event.target);
                if (tar.hasClass("actionArea")) {
                    var selectedCardElem = tar.closest(".card");
                    that.displayCardInfo(selectedCardElem.data("card"));
                }
                return false;
            },
            click: function (event) {
                return that.clickCardFunction(event);
            }
        };
        cardDiv.swipe(swipeOptions);

        return cardDiv;
    }

    attachSelectionFunctions(cardIds, selection) {
        if (selection) {
            if (cardIds.length > 0)
                $(".card:cardId(" + cardIds + ")").addClass("selectableCard");
        } else {
            if (cardIds.length > 0)
                $(".card:cardId(" + cardIds + ")").addClass("actionableCard");
        }
    }

    // Choosing cards from a predefined selection (for example stating fellowship)
    arbitraryCardsDecision(decision) {
        var id = decision.getAttribute("id");
        var text = decision.getAttribute("text");

        var min = this.getDecisionParameter(decision, "min");
        var max = this.getDecisionParameter(decision, "max");
        var cardIds = this.getDecisionParameters(decision, "cardId");
        var blueprintIds = this.getDecisionParameters(decision, "blueprintId");
        var imageUrls = this.getDecisionParameters(decision, "imageUrl");
        var selectable = this.getDecisionParameters(decision, "selectable");

        var that = this;

        var selectedCardIds = new Array();

        var selectableCardIds = new Array();

        this.cardActionDialog
            .html("<div id='arbitraryChoice'></div>")
            .dialog("option", "title", text);

        // Create the action cards and fill the dialog with them
        for (var i = 0; i < blueprintIds.length; i++) {
            var cardId = cardIds[i];
            var blueprintId = blueprintIds[i];
            var imageUrl = imageUrls[i];

            if (selectable[i] == "true")
                selectableCardIds.push(cardId);

            var card = new Card(blueprintId, "SPECIAL", cardId, null, imageUrl);

            var cardDiv = this.createCardDiv(card);

            $("#arbitraryChoice").append(cardDiv);
        }

        var finishChoice = function () {
            that.cardActionDialog.dialog("close");
            $("#arbitraryChoice").html("");
            that.clearSelection();
            that.decisionFunction(id, "" + selectedCardIds);
        };

        var resetChoice = function () {
            selectedCardIds = new Array();
            that.clearSelection();
            allowSelection();
            processButtons();
        };

        var processButtons = function () {
            var buttons = {};
            if (selectedCardIds.length > 0)
                buttons["Clear selection"] = function () {
                    resetChoice();
                    processButtons();
                };
            if (selectedCardIds.length >= min)
                buttons["Done"] = function () {
                    finishChoice();
                };
            that.cardActionDialog.dialog("option", "buttons", buttons);
        };

        var allowSelection = function () {
            that.selectionFunction = function (cardId) {
                selectedCardIds.push(cardId);

                if (selectedCardIds.length == max) {
                    if (that.gameSettings.get("autoAccept")) {
                        finishChoice();
                        return;
                    } else {
                        that.clearSelection();
                        if (selectedCardIds.length > 0)
                            $(".card:cardId(" + selectedCardIds + ")").addClass("selectedCard");
                    }
                } else {
                    getCardDivFromId(cardId).removeClass("selectableCard").addClass("selectedCard");
                }

                processButtons();
            };

            that.attachSelectionFunctions(selectableCardIds, true);
        };

        allowSelection();
        if (!this.replayMode)
        {
            processButtons();
            this.PlaySound("awaitAction");
        }

        openSizeDialog(this.cardActionDialog);
        this.arbitraryDialogResize(false);
        $('.ui-dialog :button').blur();
    }

    // Choosing one action to resolve, for example phase actions
    cardActionChoiceDecision(decision) {
        var id = decision.getAttribute("id");
        var text = decision.getAttribute("text");

        var cardIds = this.getDecisionParameters(decision, "cardId");
        var blueprintIds = this.getDecisionParameters(decision, "blueprintId");
        var imageUrls = this.getDecisionParameters(decision, "imageUrl");
        var actionIds = this.getDecisionParameters(decision, "actionId");
        var actionTexts = this.getDecisionParameters(decision, "actionText");
        var actionTypes = this.getDecisionParameters(decision, "actionType");

        var that = this;

        if (cardIds.length == 0 && this.gameSettings.get("autoPass") && !this.replayMode) {
            that.decisionFunction(id, "");
            return;
        }

        var selectedCardIds = new Array();

        this.alertText.html(text);
        // ****CCG League****: Border around alert box
        this.alertBox.addClass("alert-box-highlight");

        var processButtons = function () {
            that.alertButtons.html("");
            if (selectedCardIds.length == 0) {
                that.alertButtons.append("<button id='Pass'>Pass</button>");
                $("#Pass").button().click(function () {
                    finishChoice();
                });
            }
            if (selectedCardIds.length > 0) {
                that.alertButtons.append("<button id='ClearSelection'>Reset choice</button>");
                that.alertButtons.append("<button id='Done' style='float: right'>Done</button>");
                $("#Done").button().click(function () {
                    finishChoice();
                });
                $("#ClearSelection").button().click(function () {
                    resetChoice();
                });
            }
        };

        var finishChoice = function () {
            that.alertText.html("");
            // ****CCG League****: Border around alert box
            that.alertBox.removeClass("alert-box-highlight");
            that.alertButtons.html("");
            that.clearSelection();
            $(".card").each(
                function () {
                    var card = $(this).data("card");
                    if (card.zone == "EXTRA")
                        $(this).remove();
                });
            that.hand.layoutCards();
            that.decisionFunction(id, "" + selectedCardIds);
        };

        var resetChoice = function () {
            selectedCardIds = new Array();
            that.clearSelection();
            allowSelection();
            processButtons();
        };

        var allowSelection = function () {
            var hasVirtual = false;

            for (var i = 0; i < cardIds.length; i++) {
                var cardId = cardIds[i];
                var actionId = actionIds[i];
                var actionText = actionTexts[i];
                var blueprintId = blueprintIds[i];
                var imageUrl = imageUrls[i];
                var actionType = actionTypes[i];

                if (blueprintId == "inPlay") {
                    var cardIdElem = getCardDivFromId(cardId);
                } else {
                    hasVirtual = true;
                    cardIds[i] = "extra" + cardId;
                    var card = new Card(blueprintId, "EXTRA", "extra" + cardId, null, imageUrl);

                    var cardDiv = that.createCardDiv(card);
                    $(cardDiv).css({opacity: "0.8"});

                    $("#main").append(cardDiv);

                    var cardIdElem = $(".card:cardId(extra" + cardId + ")");
                }

                if (cardIdElem.data("action") == null) {
                    cardIdElem.data("action", new Array());
                }
                var actions = cardIdElem.data("action");
                actions.push({actionId: actionId, actionText: actionText, actionType: actionType});
            }

            if (hasVirtual) {
                that.hand.layoutCards();
            }

            that.selectionFunction = function (cardId, event) {
                var cardIdElem = getCardDivFromId(cardId);
                var actions = cardIdElem.data("action");

                var selectActionFunction = function (actionId) {
                    selectedCardIds.push(actionId);
                    if (that.gameSettings.get("autoAccept")) {
                        finishChoice();
                    } else {
                        that.clearSelection();
                        getCardDivFromId(cardId).addClass("selectedCard");
                        processButtons();
                    }
                };

                // If the only legal action is a card play, perform action automatically by clicking
                // Otherwise show a drop-down menu with the action options by clicking
                if (actions.length == 1 &&
                        (actions[0].actionType == "PLAY_CARD" || actions[0].actionType == "SEED_CARD")) {
                    selectActionFunction(actions[0].actionId);
                } else {
                        // TODO - Bind to right-click?
                    that.createActionChoiceContextMenu(actions, event, selectActionFunction);
                }
            };

            that.attachSelectionFunctions(cardIds, false);
        };

        allowSelection();
        if (!this.replayMode)
        {
            processButtons();
            this.PlaySound("awaitAction");
        }

        $(':button').blur();
    }

    PlaySound(soundObj) {
        var myAudio = document.getElementById(soundObj);
        if(!document.hasFocus() || document.hidden || document.msHidden || document.webkitHidden)
        {
            myAudio.play();
        }
    }

    createActionChoiceContextMenu(actions, event, selectActionFunction) {
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
            selectActionFunction(actionId);
            return false;
        });

        // Hide bindings
        setTimeout(function () { // Delay for Mozilla
            $(document).click(getRidOfContextMenu);
        }, 0);
    }

    // Choosing one action to resolve, for example required triggered actions
    actionChoiceDecision(decision) {
        var id = decision.getAttribute("id");
        var text = decision.getAttribute("text");

        var blueprintIds = this.getDecisionParameters(decision, "blueprintId");
        var imageUrls = this.getDecisionParameters(decision, "imageUrl");
        var actionIds = this.getDecisionParameters(decision, "actionId");
        var actionTexts = this.getDecisionParameters(decision, "actionText");

        var that = this;

        var selectedActionIds = new Array();

        this.cardActionDialog
            .html("<div id='arbitraryChoice'></div>")
            .dialog("option", "title", text);

        var cardIds = new Array();

        for (var i = 0; i < blueprintIds.length; i++) {
            var blueprintId = blueprintIds[i];
            var imageUrl = imageUrls[i];

            cardIds.push("temp" + i);
            var card = new Card(blueprintId, "SPECIAL", "temp" + i, null, imageUrl);

            var cardDiv = this.createCardDiv(card, actionTexts[i]);

            $("#arbitraryChoice").append(cardDiv);
        }

        var finishChoice = function () {
            that.cardActionDialog.dialog("close");
            $("#arbitraryChoice").html("");
            that.clearSelection();
            that.decisionFunction(id, "" + selectedActionIds);
        };

        var resetChoice = function () {
            selectedActionIds = new Array();
            that.clearSelection();
            allowSelection();
            processButtons();
        };

        var processButtons = function () {
            var buttons = {};
            if (selectedActionIds.length > 0) {
                buttons["Clear selection"] = function () {
                    resetChoice();
                    processButtons();
                };
                buttons["Done"] = function () {
                    finishChoice();
                };
            }
            that.cardActionDialog.dialog("option", "buttons", buttons);
        };

        var allowSelection = function () {
            that.selectionFunction = function (cardId) {
                var actionId = actionIds[parseInt(cardId.substring(4))];
                selectedActionIds.push(actionId);

                that.clearSelection();

                if (this.gameSettings.get("autoAccept")) {
                    finishChoice();
                } else {
                    processButtons();
                    getCardDivFromId(cardId).addClass("selectedCard");
                }
            };

            that.attachSelectionFunctions(cardIds, true);
        };

        allowSelection();
        if (!this.replayMode)
        {
            processButtons();
            this.PlaySound("awaitAction");
        }

        openSizeDialog(this.cardActionDialog);
        this.arbitraryDialogResize(false);
        $('.ui-dialog :button').blur();
    }

    // Choosing some number of cards, for example to wound
    cardSelectionDecision(decision) {
        var id = decision.getAttribute("id");
        var text = decision.getAttribute("text");

        var min = this.getDecisionParameter(decision, "min");
        var max = this.getDecisionParameter(decision, "max");
        var cardIds = this.getDecisionParameters(decision, "cardId");

        var that = this;

        this.alertText.html(text);
        this.alertBox.addClass("alert-box-card-selection");

        var selectedCardIds = new Array();

        var finishChoice = function () {
            that.alertText.html("");
            that.alertBox.removeClass("alert-box-card-selection");
            that.alertButtons.html("");
            that.clearSelection();
            that.decisionFunction(id, "" + selectedCardIds);
        };

        var resetChoice = function () {
            selectedCardIds = new Array();
            that.clearSelection();
            allowSelection();
            processButtons();
        };

        var processButtons = function () {
            that.alertButtons.html("");
            if (selectedCardIds.length > 0) {
                that.alertButtons.append("<button id='ClearSelection'>Reset choice</button>");
                $("#ClearSelection").button().click(function () {
                    resetChoice();
                });
            }
            if (selectedCardIds.length >= min) {
                that.alertButtons.append("<button id='Done' style='float: right'>Done</button>");
                $("#Done").button().click(function () {
                    finishChoice();
                });
            }
        };

        var allowSelection = function () {
            that.selectionFunction = function (cardId) {
                selectedCardIds.push(cardId);
                if (selectedCardIds.length == max) {
                    if (this.gameSettings.get("autoAccept")) {
                        finishChoice();
                        return;
                    } else {
                        that.clearSelection();
                        if (selectedCardIds.length > 0)
                            $(".card:cardId(" + selectedCardIds + ")").addClass("selectedCard");
                    }
                } else {
                    getCardDivFromId(cardId).removeClass("selectableCard").addClass("selectedCard");
                }

                processButtons();
            };

            that.attachSelectionFunctions(cardIds, true);
        };

        allowSelection();
        if (!this.replayMode)
        {
            processButtons();
            this.PlaySound("awaitAction");
        }
    }

    clearSelection() {
        $(".selectableCard").removeClass("selectableCard").data("action", null);
        $(".actionableCard").removeClass("actionableCard").data("action", null);
        $(".selectedCard").removeClass("selectedCard");
        this.selectionFunction = null;
    }

    dialogResize(dialog, group) {
        var width = dialog.width() + 10;
        var height = dialog.height() + 10;
        group.setBounds(this.padding, this.padding, width - 2 * this.padding, height - 2 * this.padding);
    }

    arbitraryDialogResize(texts) {
        if (texts) {
            var width = this.cardActionDialog.width() + 10;
            var height = this.cardActionDialog.height() - 10;
            this.specialGroup.setBounds(
                this.padding, this.padding, width - 2 * this.padding, height - 2 * this.padding
            );
        } else
            this.dialogResize(this.cardActionDialog, this.specialGroup);
    }
}

export class TribblesGameTableUI extends GameTableUI {
    constructor(url, replayMode) {
        super(url, replayMode);
    }

    layoutUI(sizeChanged) {
        var padding = this.padding;
        var width = $(window).width();
        var height = $(window).height();
        if (sizeChanged) {
            this.windowWidth = width;
            this.windowHeight = height;
        } else {
            width = this.windowWidth;
            height = this.windowHeight;
        }

        var BORDER_PADDING = 2;
        var LOCATION_BORDER_PADDING = 4;

        // Defines the relative height of the opponent/player/table areas of the UI.
        var OPPONENT_AREA_HEIGHT_SCALE = 0.15;
        var PLAYER_AREA_HEIGHT_SCALE = 0.3;

        // Defines the minimum/maximum height of the opponent/player/table areas of the UI. No max for table area.
        var MIN_OPPONENT_AREA_HEIGHT = 114;
        var MAX_OPPONENT_AREA_HEIGHT = 140;
        var MIN_PLAYER_AREA_HEIGHT = MIN_OPPONENT_AREA_HEIGHT * Math.floor(PLAYER_AREA_HEIGHT_SCALE / OPPONENT_AREA_HEIGHT_SCALE);
        var MAX_PLAYER_AREA_HEIGHT = MAX_OPPONENT_AREA_HEIGHT * Math.floor(PLAYER_AREA_HEIGHT_SCALE / OPPONENT_AREA_HEIGHT_SCALE);

        // Sets the top and height of the opponent/player/table areas of the UI.
        var OPPONENT_AREA_TOP = 0;
        var OPPONENT_AREA_HEIGHT = Math.min(MAX_OPPONENT_AREA_HEIGHT, Math.max(MIN_OPPONENT_AREA_HEIGHT, Math.floor(height * OPPONENT_AREA_HEIGHT_SCALE)));
        var OPPONENT_CARD_PILE_TOP_1 = OPPONENT_AREA_TOP;
        var OPPONENT_CARD_PILE_HEIGHT_1 = Math.floor(OPPONENT_AREA_HEIGHT / 2);
        var OPPONENT_CARD_PILE_TOP_2 = OPPONENT_AREA_TOP + OPPONENT_CARD_PILE_HEIGHT_1 + BORDER_PADDING - 1;
        var OPPONENT_CARD_PILE_HEIGHT_2 = OPPONENT_AREA_HEIGHT - OPPONENT_CARD_PILE_HEIGHT_1 - BORDER_PADDING + 1;
        var PLAYER_AREA_HEIGHT = Math.min(MAX_PLAYER_AREA_HEIGHT, Math.max(MIN_PLAYER_AREA_HEIGHT, Math.floor(height * PLAYER_AREA_HEIGHT_SCALE)));
        var PLAYER_AREA_TOP = height - BORDER_PADDING - PLAYER_AREA_HEIGHT;
        var PLAYER_CARD_PILES_AND_SIDE_OF_TABLE_HEIGHT = Math.floor(PLAYER_AREA_HEIGHT / 2);
        var PLAYER_CARD_PILE_TOP_1 = PLAYER_AREA_TOP;
        var PLAYER_CARD_PILE_HEIGHT_1 = Math.floor(PLAYER_CARD_PILES_AND_SIDE_OF_TABLE_HEIGHT / 2);
        var PLAYER_CARD_PILE_TOP_2 = PLAYER_CARD_PILE_TOP_1 + PLAYER_CARD_PILE_HEIGHT_1 + BORDER_PADDING - 1;
        var PLAYER_CARD_PILE_HEIGHT_2 = PLAYER_CARD_PILES_AND_SIDE_OF_TABLE_HEIGHT - PLAYER_CARD_PILE_HEIGHT_1 - BORDER_PADDING + 1;
        var PLAYER_ACTION_AREA_AND_HAND_TOP = PLAYER_AREA_TOP + PLAYER_CARD_PILES_AND_SIDE_OF_TABLE_HEIGHT + BORDER_PADDING - 1;
        var PLAYER_ACTION_AREA_AND_HAND_HEIGHT = PLAYER_AREA_HEIGHT - PLAYER_CARD_PILES_AND_SIDE_OF_TABLE_HEIGHT - BORDER_PADDING;
        var TABLE_AREA_TOP = OPPONENT_AREA_HEIGHT + BORDER_PADDING;
        var TABLE_AREA_HEIGHT = Math.max(0, PLAYER_AREA_TOP - LOCATION_BORDER_PADDING - TABLE_AREA_TOP);

        // Defines the sizes of other items in the UI.
        var LEFT_SIDE = 0;
        var GAME_STATE_AND_CHAT_WIDTH = 400;
        var CARD_PILE_AND_ACTION_AREA_LEFT = GAME_STATE_AND_CHAT_WIDTH + BORDER_PADDING - 1;
        var CARD_PILE_AND_ACTION_AREA_WIDTH = 141;
        var CARD_PILE_LEFT_1 = CARD_PILE_AND_ACTION_AREA_LEFT;
        var CARD_PILE_WIDTH_1 = Math.floor(CARD_PILE_AND_ACTION_AREA_WIDTH / 3);
        var CARD_PILE_LEFT_2 = CARD_PILE_AND_ACTION_AREA_LEFT + CARD_PILE_WIDTH_1 + BORDER_PADDING - 1;
        var CARD_PILE_WIDTH_2 = CARD_PILE_WIDTH_1;
        var CARD_PILE_LEFT_3 = CARD_PILE_LEFT_2 + CARD_PILE_WIDTH_2 + BORDER_PADDING - 1;
        var CARD_PILE_WIDTH_3 = CARD_PILE_AND_ACTION_AREA_WIDTH - CARD_PILE_WIDTH_1 - CARD_PILE_WIDTH_2;
        var STAT_BOX_HEIGHT = 50;
        var LARGE_STAT_BOX_SIZE = 25;
        var SMALL_STAT_BOX_SIZE = 20;
        var STAT_BOX_PADDING = 2;
        var TAB_PANE_HEIGHT = 25;
        var TAB_PANE_WIDTH_PADDING = 4;
        var CHAT_HEIGHT = PLAYER_AREA_HEIGHT - BORDER_PADDING + 1;
        var CHAT_WIDTH = GAME_STATE_AND_CHAT_WIDTH;

        // Sets the hand and side of table left and width
        var HAND_LEFT = CARD_PILE_LEFT_3 + CARD_PILE_WIDTH_3 + BORDER_PADDING - 1;
        var HAND_WIDTH = (width - HAND_LEFT) - BORDER_PADDING;
        var HAND_HEIGHT = CHAT_HEIGHT * .6;
        var HAND_TOP = height - HAND_HEIGHT - BORDER_PADDING;
        var SIDE_OF_TABLE_LEFT = CARD_PILE_LEFT_3 + CARD_PILE_WIDTH_3 + BORDER_PADDING - 1;
        var SIDE_OF_TABLE_WIDTH = (width - SIDE_OF_TABLE_LEFT) - BORDER_PADDING;

        var STATS_TOP = PLAYER_AREA_TOP - STAT_BOX_HEIGHT - BORDER_PADDING - 6;

        $("#bottomLeftTabs").css({left:LEFT_SIDE, top: PLAYER_AREA_TOP, width: CHAT_WIDTH, height: CHAT_HEIGHT});
        this.tabPane.css({position: "absolute", left:LEFT_SIDE, top: PLAYER_AREA_TOP, width: CHAT_WIDTH, height: CHAT_HEIGHT});
        this.chatBox.setBounds(BORDER_PADDING + TAB_PANE_WIDTH_PADDING, TAB_PANE_HEIGHT,
            CHAT_WIDTH - (2 * TAB_PANE_WIDTH_PADDING), CHAT_HEIGHT - TAB_PANE_HEIGHT);

        this.gameStateElem.css({
            position: "absolute",
            left: BORDER_PADDING,
            top: BORDER_PADDING,
            width: CHAT_WIDTH,
            height: STATS_TOP - BORDER_PADDING * 2
        });

        this.statsDiv.css({
            position: "absolute",
            left: BORDER_PADDING,
            top: STATS_TOP,
            width: CHAT_WIDTH,
            height: STAT_BOX_HEIGHT
        });
        this.alertBox.css({
            position: "absolute",
            left: CHAT_WIDTH + BORDER_PADDING * 5,
            top: HAND_TOP,
            width: HAND_LEFT - CHAT_WIDTH - BORDER_PADDING * 8,
            height: HAND_HEIGHT
        });


        var heightScales;
        if (this.spectatorMode)
            heightScales = [6, 10, 10, 10, 6];
        else
            heightScales = [5, 9, 9, 10, 6, 10];
        var yScales = new Array();
        var scaleTotal = 0;
        for (var i = 0; i < heightScales.length; i++) {
            yScales[i] = scaleTotal;
            scaleTotal += heightScales[i];
        }

        var heightPerScale = (height - (padding * (heightScales.length + 1))) / scaleTotal;

        var advPathWidth = Math.min(150, width * 0.1);
        var specialUiWidth = 150;
        var alertHeight = 80;
        var chatHeight = 200;
        var assignmentsCount = 0;

        var charsWidth = width - (advPathWidth + specialUiWidth + padding * 3);
        var charsWidthWithAssignments = 2 * charsWidth / (2 + assignmentsCount);

        var currentPlayerTurn = (this.currentPlayerId == this.bottomPlayerId);

        if (!this.gameUiInitialized) {
            return;
        }

        var playPilesLeft = HAND_LEFT;
        var playPilesRight = width - padding;
        var playPilesHorizCenter = playPilesLeft + (playPilesRight - playPilesLeft) / 2;
        var playPilesTop = BORDER_PADDING;
        var playPilesBottom = HAND_TOP - BORDER_PADDING;

        var playerCount = this.allPlayerIds.length;
        var playerSeatOffset = this.getPlayerIndex(this.bottomPlayerId);

        var playPileXs = new Array();
        var playPileYs = new Array();
        var playPileWidth = null;
        var playPileHeight = null;

        // Array is built with 0 at the bottom player position, and all others proceeding clockwise
        if (playerCount == 2) {
            playPileXs = [playPilesLeft, playPilesLeft];
            playPileYs = [playPilesTop + (playPilesBottom - playPilesTop) / 2, playPilesTop];
            playPileWidth = playPilesRight - playPilesLeft;
            playPileHeight = (playPilesBottom - playPilesTop) / 2 - padding;
        } else if (playerCount == 3) {
            playPileWidth = (playPilesRight - playPilesLeft) / 2 - padding;
            playPileHeight = (playPilesBottom - playPilesTop) / 2 - padding;
            playPileXs = [
                playPilesHorizCenter - playPileWidth / 2,
                playPilesLeft,
                playPilesRight - playPileWidth
            ];
            playPileYs = [
                playPilesTop + playPileHeight + padding,
                playPilesTop,
                playPilesTop
            ];
        }

        for (var i = 0; i < playerCount; i++) {
            var playerIndex = (i + playerSeatOffset) % playerCount;
            this.playPiles[this.allPlayerIds[playerIndex]].setBounds(
                playPileXs[i], playPileYs[i], playPileWidth, playPileHeight
            );
        }

        var i = 0; // I don't think this is used, but not deleting it for now to avoid breaking anything

        if (!this.spectatorMode)
            this.hand.setBounds(HAND_LEFT, HAND_TOP, HAND_WIDTH, HAND_HEIGHT);



        for (var playerId in this.discardPileGroups)
            if (this.discardPileGroups.hasOwnProperty(playerId))
                this.discardPileGroups[playerId].layoutCards();

        if (this.replayMode) {
            $(".replay").css({
                position: "absolute",
                left: width - 66 - 4 - padding,
                top: height - 97 - 2 - padding,
                width: 66,
                height: 97,
                "z-index": 1000
            });
        }
    }
}

export class ST1EGameTableUI extends GameTableUI {

    topPlayerId;

    constructor(url, replayMode) {
        super(url, replayMode);
    }

    addSharedMission(index, quadrant) {
        // TODO - no code here yet
    }

    addLocationDiv(index, quadrant) {
        var that = this;

        // Increment locationIndex for existing cards on the table to the right of the added location
        var locationBeforeCount = this.locationDivs.length;
        for (var i=locationBeforeCount-1; i>=index; i--) {
            this.locationDivs[i].data( "locationIndex", i+1);
            this.locationDivs[i].removeAttr("id");
            this.locationDivs[i].attr("id", "location" + (i+1));

            var missionCardGroup = this.missionCardGroups[i].getCardElems();
            for (var j=0; j<missionCardGroup.length; j++) {
                var cardData = $(missionCardGroup[j]).data("card");
                cardData.locationIndex = i+1;
            }
            this.missionCardGroups[i].locationIndex = i+1;
            
            var opponentAtLocationCardGroup = this.opponentAtLocationCardGroups[i].getCardElems();
            for (var j=0; j<opponentAtLocationCardGroup.length; j++) {
                var cardData = $(opponentAtLocationCardGroup[j]).data("card");
                cardData.locationIndex = i+1;
            }
            this.opponentAtLocationCardGroups[i].locationIndex = i+1;

            var playerAtLocationCardGroup = this.playerAtLocationCardGroups[i].getCardElems();
            for (var j=0; j<playerAtLocationCardGroup.length; j++) {
                var cardData = $(playerAtLocationCardGroup[j]).data("card");
                cardData.locationIndex = i+1;
            }
            this.playerAtLocationCardGroups[i].locationIndex = i+1;            
        }

        var newDiv = $("<div id='location" + index + "' class='ui-widget-content locationDiv'></div>");
        newDiv.data( "locationIndex", index);
        newDiv.data( "quadrant", quadrant);
        $("#main").append(newDiv);

        this.locationDivs.splice(index, 0, newDiv);

            // TODO - MissionCardGroup class exists for this, but using TableCardGroup to test beaming function
        var missionCardGroup = new TableCardGroup($("#main"), function (card) {
            return (card.zone == "SPACELINE" && card.locationIndex == this.locationIndex );
        }, false, index, this.bottomPlayerId);
        this.missionCardGroups.splice(index, 0, missionCardGroup);

        var opponentAtLocationCardGroup = new TableCardGroup($("#main"), function (card) {
            return (card.zone == "AT_LOCATION" && card.locationIndex == this.locationIndex && card.owner != that.bottomPlayerId);
        }, false, index, this.bottomPlayerId);
        this.opponentAtLocationCardGroups.splice(index, 0, opponentAtLocationCardGroup);

        var playerAtLocationCardGroup = new TableCardGroup($("#main"), function (card) {
            return (card.zone == "AT_LOCATION" && card.locationIndex == this.locationIndex && card.owner == that.bottomPlayerId);
        }, false, index, this.bottomPlayerId);
        this.playerAtLocationCardGroups.splice(index, 0, playerAtLocationCardGroup);

        this.layoutUI(false);
    }

    layoutUI(sizeChanged) {
        var padding = this.padding;

        var width = $(window).width();
        var height = $(window).height();
        if (sizeChanged) {
            this.windowWidth = width;
            this.windowHeight = height;
        } else {
            width = this.windowWidth;
            height = this.windowHeight;
        }

        var BORDER_PADDING = 2;
        var LOCATION_BORDER_PADDING = 4;

        // Defines the relative height of the opponent/player/table areas of the UI.
        var OPPONENT_AREA_HEIGHT_SCALE = 0.15;
        var PLAYER_AREA_HEIGHT_SCALE = 0.3;

        // Defines the minimum/maximum height of the opponent/player/table areas of the UI. No max for table area.
        var MIN_OPPONENT_AREA_HEIGHT = 114;
        var MAX_OPPONENT_AREA_HEIGHT = 140;
        var MIN_PLAYER_AREA_HEIGHT = MIN_OPPONENT_AREA_HEIGHT * Math.floor(PLAYER_AREA_HEIGHT_SCALE / OPPONENT_AREA_HEIGHT_SCALE);
        var MAX_PLAYER_AREA_HEIGHT = MAX_OPPONENT_AREA_HEIGHT * Math.floor(PLAYER_AREA_HEIGHT_SCALE / OPPONENT_AREA_HEIGHT_SCALE);

        // Sets the top and height of the opponent/player/table areas of the UI.
        var OPPONENT_AREA_TOP = 0;
        var OPPONENT_AREA_HEIGHT = Math.min(MAX_OPPONENT_AREA_HEIGHT, Math.max(MIN_OPPONENT_AREA_HEIGHT, Math.floor(height * OPPONENT_AREA_HEIGHT_SCALE)));
        var OPPONENT_CARD_PILE_TOP_1 = OPPONENT_AREA_TOP;
        var OPPONENT_CARD_PILE_HEIGHT_1 = Math.floor(OPPONENT_AREA_HEIGHT / 2);
        var OPPONENT_CARD_PILE_TOP_2 = OPPONENT_AREA_TOP + OPPONENT_CARD_PILE_HEIGHT_1 + BORDER_PADDING - 1;
        var OPPONENT_CARD_PILE_HEIGHT_2 = OPPONENT_AREA_HEIGHT - OPPONENT_CARD_PILE_HEIGHT_1 - BORDER_PADDING + 1;
        var PLAYER_AREA_HEIGHT = Math.min(MAX_PLAYER_AREA_HEIGHT, Math.max(MIN_PLAYER_AREA_HEIGHT, Math.floor(height * PLAYER_AREA_HEIGHT_SCALE)));
        var PLAYER_AREA_TOP = height - BORDER_PADDING - PLAYER_AREA_HEIGHT;
        var PLAYER_CARD_PILES_AND_SIDE_OF_TABLE_HEIGHT = Math.floor(PLAYER_AREA_HEIGHT / 2);
        var PLAYER_CARD_PILE_TOP_1 = PLAYER_AREA_TOP;
        var PLAYER_CARD_PILE_HEIGHT_1 = Math.floor(PLAYER_CARD_PILES_AND_SIDE_OF_TABLE_HEIGHT / 2);
        var PLAYER_CARD_PILE_TOP_2 = PLAYER_CARD_PILE_TOP_1 + PLAYER_CARD_PILE_HEIGHT_1 + BORDER_PADDING - 1;
        var PLAYER_CARD_PILE_HEIGHT_2 = PLAYER_CARD_PILES_AND_SIDE_OF_TABLE_HEIGHT - PLAYER_CARD_PILE_HEIGHT_1 - BORDER_PADDING + 1;
        var PLAYER_ACTION_AREA_AND_HAND_TOP = PLAYER_AREA_TOP + PLAYER_CARD_PILES_AND_SIDE_OF_TABLE_HEIGHT + BORDER_PADDING - 1;
        var PLAYER_ACTION_AREA_AND_HAND_HEIGHT = PLAYER_AREA_HEIGHT - PLAYER_CARD_PILES_AND_SIDE_OF_TABLE_HEIGHT - BORDER_PADDING;
        var TABLE_AREA_TOP = OPPONENT_AREA_HEIGHT + BORDER_PADDING;
        var TABLE_AREA_HEIGHT = Math.max(0, PLAYER_AREA_TOP - LOCATION_BORDER_PADDING - TABLE_AREA_TOP);

        // Defines the sizes of other items in the UI.
        var LEFT_SIDE = 0;
        var GAME_STATE_AND_CHAT_WIDTH = 300;
        var CARD_PILE_AND_ACTION_AREA_LEFT = GAME_STATE_AND_CHAT_WIDTH + BORDER_PADDING - 1;
        var CARD_PILE_AND_ACTION_AREA_WIDTH = 141;
        var CARD_PILE_LEFT_1 = CARD_PILE_AND_ACTION_AREA_LEFT;
        var CARD_PILE_WIDTH_1 = Math.floor(CARD_PILE_AND_ACTION_AREA_WIDTH / 3);
        var CARD_PILE_LEFT_2 = CARD_PILE_AND_ACTION_AREA_LEFT + CARD_PILE_WIDTH_1 + BORDER_PADDING - 1;
        var CARD_PILE_WIDTH_2 = CARD_PILE_WIDTH_1;
        var CARD_PILE_LEFT_3 = CARD_PILE_LEFT_2 + CARD_PILE_WIDTH_2 + BORDER_PADDING - 1;
        var CARD_PILE_WIDTH_3 = CARD_PILE_AND_ACTION_AREA_WIDTH - CARD_PILE_WIDTH_1 - CARD_PILE_WIDTH_2;
        var LARGE_STAT_BOX_SIZE = 25;
        var SMALL_STAT_BOX_SIZE = 20;
        var STAT_BOX_PADDING = 2;
        var TAB_PANE_HEIGHT = 25;
        var TAB_PANE_WIDTH_PADDING = 4;
        var CHAT_HEIGHT = PLAYER_AREA_HEIGHT - BORDER_PADDING + 1;
        var CHAT_WIDTH = GAME_STATE_AND_CHAT_WIDTH;

        // Sets the hand and side of table left and width
        var HAND_LEFT = CARD_PILE_LEFT_3 + CARD_PILE_WIDTH_3 + BORDER_PADDING - 1;
        var HAND_WIDTH = (width - HAND_LEFT) - BORDER_PADDING;
        var SIDE_OF_TABLE_LEFT = CARD_PILE_LEFT_3 + CARD_PILE_WIDTH_3 + BORDER_PADDING - 1;
        var SIDE_OF_TABLE_WIDTH = (width - SIDE_OF_TABLE_LEFT) - BORDER_PADDING;

        var BOTTOM_LEFT_TABS_RIGHT = LEFT_SIDE + CHAT_WIDTH;

        $("#bottomLeftTabs").css({left:LEFT_SIDE, top: PLAYER_AREA_TOP, width: CHAT_WIDTH - 50, height: CHAT_HEIGHT});
        this.tabPane.css({position: "absolute", left:LEFT_SIDE, top: PLAYER_AREA_TOP, width: CHAT_WIDTH, height: CHAT_HEIGHT});
        this.chatBox.setBounds(BORDER_PADDING + TAB_PANE_WIDTH_PADDING, TAB_PANE_HEIGHT,
            CHAT_WIDTH - (2 * TAB_PANE_WIDTH_PADDING), CHAT_HEIGHT - TAB_PANE_HEIGHT);

        // Old LotR gemp code for heightScales
        var heightScales;
        if (this.spectatorMode)
            heightScales = [6, 10, 10, 10, 6];
        else
            heightScales = [5, 9, 9, 10, 6, 10];
        var yScales = new Array();
        var scaleTotal = 0;
        for (var i = 0; i < heightScales.length; i++) {
            yScales[i] = scaleTotal;
            scaleTotal += heightScales[i];
        }

        var heightPerScale = (height - (padding * (heightScales.length + 1))) / scaleTotal;

        var advPathWidth = Math.min(150, width * 0.1);
        var specialUiWidth = 150;
        var alertHeight = 80;
        var chatHeight = 200;
        var assignmentsCount = 0;

        var charsWidth = width - (advPathWidth + specialUiWidth + padding * 3);
        var charsWidthWithAssignments = 2 * charsWidth / (2 + assignmentsCount);

        var currentPlayerTurn = (this.currentPlayerId == this.bottomPlayerId);

        if (!this.gameUiInitialized) {
            return;
        }

        this.statsDiv.css({
            position: "absolute",
            left: padding + "px",
            top: height - (padding * 2) - chatHeight - 50 + "px",
            width: advPathWidth - 4,
            height: 30
        });
        this.gameStateElem.css({
            position: "absolute",
            left: padding * 2, // + advPathWidth,
            top: padding,
            width: specialUiWidth - padding + 75,
            height: TABLE_AREA_TOP - padding * 2
//            height: height - padding * 4 - alertHeight - chatHeight
        });
        this.alertBox.css({
            position: "absolute",
            left: $("#bottomLeftTabs").offset().left + $("#bottomLeftTabs").width() + BORDER_PADDING * 5,
//            left: $("#bottomLeftTabs").offset().left + $("#bottomLeftTabs").width() + BORDER_PADDING * 5,
//            left: CARD_PILE_AND_ACTION_AREA_LEFT,
            top: PLAYER_ACTION_AREA_AND_HAND_TOP,
            width: HAND_LEFT - (this.tabPane.offset().left + this.tabPane.width() + BORDER_PADDING * 5) - BORDER_PADDING * 4,
            height: alertHeight
        });

        for (var i = 0; i < 2; i++) {
            var playerId = this.allPlayerIds[i];
            if (playerId == this.bottomPlayerId) {
                var top = PLAYER_AREA_TOP;
                var height = PLAYER_CARD_PILES_AND_SIDE_OF_TABLE_HEIGHT;
            } else {
                var top = OPPONENT_AREA_TOP;
                var height = OPPONENT_AREA_HEIGHT;
            }
            this.onTableAreas[playerId].setBounds(SIDE_OF_TABLE_LEFT, top, SIDE_OF_TABLE_WIDTH, height);
            this.onTableAreas[playerId].layoutCards();
        }
        if (!this.spectatorMode) {
            this.hand.setBounds(HAND_LEFT, PLAYER_ACTION_AREA_AND_HAND_TOP, HAND_WIDTH,
                PLAYER_ACTION_AREA_AND_HAND_HEIGHT);
            this.hand.layoutCards();
        }

            // LOCATION CODE FROM SWCCG GEMP
        var locationsCount = this.locationDivs.length;

        var zoomedInLocationDivWidth = (width / Math.min(3.25, locationsCount)) - (LOCATION_BORDER_PADDING / 2);
        var otherLocationDivWidth = zoomedInLocationDivWidth;
        var locationDivWidth = (width / locationsCount) - (LOCATION_BORDER_PADDING / 2);

        var x = 0;
        var y = TABLE_AREA_TOP;
        var locationDivHeight = TABLE_AREA_HEIGHT;

        for (var locationIndex = 0; locationIndex < locationsCount; locationIndex++) {
            this.locationDivs[locationIndex].css({left:x, top:y, width:locationDivWidth, height:locationDivHeight});
            var currQuadrant = this.locationDivs[locationIndex].data("quadrant");
            if (locationIndex == 0) {
                this.locationDivs[locationIndex].addClass("first-in-quadrant");
            } else if (currQuadrant != this.locationDivs[locationIndex - 1].data("quadrant")) {
                this.locationDivs[locationIndex].addClass("first-in-quadrant");
            } else if (this.locationDivs[locationIndex].hasClass("first-in-quadrant")) {
                this.locationDivs[locationIndex].removeClass("first-in-quadrant");
            }

            if (locationIndex == locationsCount - 1) {
                this.locationDivs[locationIndex].addClass("last-in-quadrant");
            } else if (currQuadrant != this.locationDivs[locationIndex + 1].data("quadrant")) {
                this.locationDivs[locationIndex].addClass("last-in-quadrant");
            } else if (this.locationDivs[locationIndex].hasClass("last-in-quadrant")) {
                this.locationDivs[locationIndex].removeClass("last-in-quadrant");
            }

            this.missionCardGroups[locationIndex].setBounds(x, y + locationDivHeight/3, locationDivWidth, locationDivHeight/3);
            this.missionCardGroups[locationIndex].layoutCards();
            this.opponentAtLocationCardGroups[locationIndex].setBounds(x, y, locationDivWidth, locationDivHeight / 3);
            this.opponentAtLocationCardGroups[locationIndex].layoutCards();
            this.playerAtLocationCardGroups[locationIndex].setBounds(x, y + 2 * locationDivHeight/3, locationDivWidth, locationDivHeight / 3);
            this.playerAtLocationCardGroups[locationIndex].layoutCards();

            x = (x + locationDivWidth + (LOCATION_BORDER_PADDING / 2));
        }
                // END OF SWCCG GEMP LOCATION CODE

        for (var playerId in this.discardPileGroups)
            if (this.discardPileGroups.hasOwnProperty(playerId))
                this.discardPileGroups[playerId].layoutCards();

        for (var playerId in this.adventureDeckGroups)
            if (this.adventureDeckGroups.hasOwnProperty(playerId))
                this.adventureDeckGroups[playerId].layoutCards();

        for (var playerId in this.removedPileGroups)
            if (this.removedPileGroups.hasOwnProperty(playerId))
                this.removedPileGroups[playerId].layoutCards();

        for (var playerId in this.miscPileGroups)
            if (this.miscPileGroups.hasOwnProperty(playerId))
                this.miscPileGroups[playerId].layoutCards();

        if (this.replayMode) {
            $(".replay").css({
                position: "absolute",
                left: width - 66 - 4 - padding,
                top: height - 97 - 2 - padding,
                width: 66,
                height: 97,
                "z-index": 1000
            });
        }
    }
}