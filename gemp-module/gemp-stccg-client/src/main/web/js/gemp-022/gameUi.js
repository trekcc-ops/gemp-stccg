import GempClientCommunication from './communication.js';
import { log, getUrlParam } from './common.js';
import Card from './jCards.js';
import { createCardDiv, createFullCardDiv, getCardDivFromId } from './jCards.js';
import { NormalCardGroup, PlayPileCardGroup, NormalGameCardGroup, TableCardGroup } from './jCardGroup.js';
import GameAnimations from './gameAnimations.js';
import ChatBoxUI from './chat.js';
import { openSizeDialog, showLinkableCardTitle } from "./common.js";
import Cookies from "js-cookie";
import playImg from "../../images/play.png";
import pauseImg from "../../images/pause.png";
import strengthIconImg from "../../images/o_icon_strength.png";
import vitalityIconImg from "../../images/o_icon_strength.png";
import compassIconImg from "../../images/o_icon_strength.png";
import resistanceIconImg from "../../images/o_icon_strength.png";
import awaitingActionAudio from "../../src/assets/awaiting_decision.mp3";


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

        this.gamePhases = new Array("Execute Orders");

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

        // REQUIRED FOR jCards.getCardDivFromId()
        $.expr[':'].cardId = function (obj, index, meta, stack) {
            var cardIds = meta[3].split(",");
            var cardData = $(obj).data("card");
            return (cardData != null && ($.inArray(cardData.cardId, cardIds) > -1));
        };

        if (this.replayMode) {
            var slowerBut = $("#slowerButton").button({
                icon: "ui-icon-triangle-1-w",
                text: false
            });
            var fasterBut = $("#fasterButton").button({
                icon: "ui-icon-triangle-1-e",
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
        for (let i=0; i < this.missionCardGroups.length; i++) {
            if (this.missionCardGroups[i].cardBelongs(cardData)) {
                return this.missionCardGroups[i];
            }
        }
        for (let i=0; i < this.opponentAtLocationCardGroups.length; i++) {
            if (this.opponentAtLocationCardGroups[i].cardBelongs(cardData)) {
                return this.opponentAtLocationCardGroups[i];
            }
        }
        for (let i=0; i < this.playerAtLocationCardGroups.length; i++) {
            if (this.playerAtLocationCardGroups[i].cardBelongs(cardData)) {
                return this.playerAtLocationCardGroups[i];
            }
        }
        for (let [playerId, cardGroup] of Object.entries(this.playPiles)) {
            if (cardGroup.cardBelongs(cardData)) {
                return cardGroup;
            }
        }
        for (let [playerId, cardGroup] of Object.entries(this.onTableAreas)) {
            if (cardGroup.cardBelongs(cardData)) {
                return cardGroup;
            }
        }
        if (this.hand != null) {
            if (this.hand.cardBelongs(cardData)) {
                return this.hand;
            }
        }
        return null;
    }

    layoutGroupWithCard(cardId) {
        let cardData = getCardDivFromId(cardId).data("card");
        let tempGroup = this.getReorganizableCardGroupForCardData(cardData);
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
                "<div class='playerStats'>" +
                    `<div id='player${i}' class='player'>${(i+1)}. ${this.allPlayerIds[i]}</div>` +
                    `<div id='clock${i}' class='clock'></div>` +
                    `<div id='deck${i}' class='deckSize' title='Draw deck size'>0</div>` +
                    `<div id='hand${i}' class='handSize' title='Hand size'>0</div>` +
                    `<div id='discard${i}' class='discardSize' title='Discard size'>0</div>` +
                    `<div id='score${i}' class='playerScore'>SCORE 0</div>` +
                "</div>");

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
                    return (card.zone == "CORE");
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
                if(!that.clickCardFunction(event)) {
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
        $("#currentPhase").text("Game over");
        var that = this;
        if(this.allPlayerIds == null) {
            return;
        }

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
                var cookieValue = Cookies.get(settingName);

                    // Multiple choice settings: foilPresentation
                if (settingName == "foilPresentation" && cookieValue != null) {
                    optionSelection.val(cookieValue);
                    that.gameSettings.set(settingName, cookieValue);
                }

                    // True/false settings: autoAccept, alwaysDropDown
                if (cookieValue == "true" || cookieValue == null) {
                    optionSelection.prop("checked", true);
                    that.gameSettings.set(settingName, true);
                }

                optionSelection.bind("change", function() {
                    var userSelection;
                    if (settingName === "foilPresentation") {
                        userSelection = "" + optionSelection.val(); // Multiple choice
                    } else {
                        userSelection = optionSelection.prop("checked"); // True/false
                    }
                    that.gameSettings.set(settingName, userSelection);
                    Cookies.set(settingName, "" + userSelection, {expires: 365});
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
        var currAutoPassCookieValue = Cookies.get("autoPassPhases");
        if (currAutoPassCookieValue == null) {
            currPassedPhases = allPhaseNames;
        } else {
            currPassedPhases = currAutoPassCookieValue.split("0");
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
                if ($("#autoPass" + allPhaseNames[i]).prop("checked")) {
                    newAutoPassPhases += "0" + allPhaseNames[i];
                }
            }
            if (newAutoPassPhases.length > 0) {
                newAutoPassPhases = newAutoPassPhases.substr(1);
            }
            Cookies.set("autoPassPhases", newAutoPassPhases, {expires: 365});
        });

        var playerListener = function (players) {
            var val = "";
            for (var i = 0; i < players.length; i++) {
                val += players[i] + "<br/>";
            }
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
            var card = new Card(blueprintId, "SPECIAL", "hint", "", imageUrl, "", false);
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
                }
                else if ((selectedCardElem.hasClass("selectableCard") || 
                          selectedCardElem.hasClass("actionableCard") ||
                          selectedCardElem.hasClass("selectedCard"))
                        &&
                        !this.replayMode
                    ) {
                        this.selectionFunction(selectedCardElem.data("card").cardId, event);
                }
                    
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
                        for (var i = 0; i < cardsInGroup.length; i++) {
                            if (cardsInGroup[i].data("card").cardId == this.dragCardId) {
                                this.dragCardIndex = i;
                                this.draggedCardIndex = i;
                                break;
                            }
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
                        if (event.clientX < this.dragStartX) {
                            currentIndex = this.dragCardIndex - Math.floor((this.dragStartX - event.clientX) / width);
                        }
                        else {
                            currentIndex = this.dragCardIndex + Math.floor((event.clientX - this.dragStartX) / width);
                        }

                        if (currentIndex < 0) {
                            currentIndex = 0;
                        }
                        if (currentIndex >= cardsInGroup.length) {
                            currentIndex = cardsInGroup.length - 1;
                        }

                        var cardIdAtIndex = $(cardsInGroup[currentIndex]).data("card").cardId;
                        if (cardIdAtIndex != this.dragCardId) {
                            if (currentIndex < this.draggedCardIndex) {
                                $(".card:cardId(" + cardIdAtIndex + ")").before(getCardDivFromId(this.dragCardId));
                            }
                            else {
                                $(".card:cardId(" + cardIdAtIndex + ")").after(getCardDivFromId(this.dragCardId));
                            }
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
        let showModifiers = false;
        var cardId = card.cardId;
        if (cardId == null || (cardId.length >= 5 && cardId.substring(0, 5) == "extra")) {
            showModifiers = false;
        }
        else if (!this.replayMode && (cardId.length < 4 || cardId.substring(0, 4) != "temp")) {
            showModifiers = true;
        }
        // DEBUG: console.log("displayCardInfo for cardId " + cardId);
        // DEBUG: console.log("showModifiers = " + showModifiers);

        this.displayCard(card, showModifiers);

        if (showModifiers) {
            this.getCardModifiersFunction(cardId, this.setCardModifiers);
        }
    }

    setCardModifiers(json) {
        // DEBUG: console.log("Calling setCardModifiers");
        let modifiers = json.modifiers; // list of HTML strings
        let affiliations = json.affiliations; // list of HTML strings
        let icons = json.icons; // list of HTML strings
        let crew = json.crew; // list of other cards with specific properties
        let dockedCards = json.dockedCards; // list of other cards with specific properties

        // Ships only
        let staffingRequirements = json.staffingRequirements; // list of HTML strings
        let isStaffed = json.isStaffed; // boolean
        let printedRange = json.printedRange; // int
        let rangeAvailable = json.rangeAvailable; // int

        // Missions only
        let missionRequirements = json.missionRequirements; // string
        let awayTeams = json.awayTeams; // list of nodes with two elements (playerId and cardsInAwayTeam)

        let html = "";

        // List active modifiers
        if (modifiers != null && modifiers.length > 0) {
            html = html + "<b>Active Modifiers:</b><br/>";
            for (const modifier of modifiers) {
                html = html + modifier + "<br/>";
            }
            html = html + "<br/>";
        }

        // Show icons for affiliation(s)
        if (affiliations != null && affiliations.length > 0) {
            html = html + "<b>Affiliation:</b> ";
            for (const affiliation of affiliations) {
                html = html + affiliation;
            }
            html = html + "<br/>";
        }

        // Show other card icons
        if (icons != null && icons.length > 0) {
            html = html + "<b>Icons:</b> ";
            for (const icon of icons) {
                html = html + icon;
            }
            html = html + "<br/>";
        }

        // Show staffing requirements (if this card is a ship)
        if (staffingRequirements != null) {
            html = html + "<b>Staffing requirements:</b> ";
            if (staffingRequirements.length == 0) {
                html = html + "none";
            } else {
                for (const staffingRequirement of staffingRequirements) {
                    html = html + staffingRequirement;
                }
            }
            html = html + "<br/>";
            if (isStaffed) {
                html = html + "<i>(Ship is staffed)</i>";
            } else {
                html = html + "<i>(Ship is not staffed)</i>";
            }
            html = html + "<br/><br/>";
        }

        // Show RANGE (if this card is a ship)
        if (printedRange != null && rangeAvailable != null) {
            html = html + "<b>Printed RANGE:</b> " + printedRange + "<br/>";
            html = html + "<b>RANGE available</b> " + rangeAvailable + "<br/>";
        }

        // Show crew and docked ships if this card has them
        html = html + this.showCommaDelimitedListOfCardLinks(crew, "Crew");
        html = html + this.showCommaDelimitedListOfCardLinks(dockedCards, "Docked ships");

        // Show mission requirements and away teams if this card has them
        if (missionRequirements != null) {
            let redMissionReqs = missionRequirements.replaceAll(" OR ", " <a style='color:red'>OR</a> ");
            html = html + "<b>Mission requirements:</b> " + redMissionReqs + "<br/><br/>";
        }

        if (awayTeams != null && awayTeams.length > 0) {
            html = html + "<b><u>Away Teams</u></b>";
            for (const team of awayTeams) {
                html = html + this.showCommaDelimitedListOfCardLinks(team.cardsInAwayTeam, team.playerId);
            }
        }

        $("#cardEffects").append(html);
        $("#cardEffects").addClass("cardInfoText");
    }

    showCommaDelimitedListOfCardLinks(jsonList, listTitle) {
        let html = "";
        if (jsonList != null && jsonList.length > 0) {
            html = html + "<b>" + listTitle + " (" + jsonList.length + "):</b> ";
            for (let i = 0; i < jsonList.length; i++) {
                if (i > 0) {
                    html = html + ", ";
                }
                html = html + this.showLinkableCardTitle(jsonList[i]);
            }
            html = html + "<br/><br/>";
        }
        return html;
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
            function (json) {
                that.processGameEvents(json, false);
            }, this.gameErrorMap());
    }

    updateGameState() {
        var that = this;
        this.communication.updateGameState(
            this.channelNumber,
            function (json) {
                that.processGameEvents(json, true);
            }, this.gameErrorMap());
    }

    decisionFunction(decisionId, result) {
        var that = this;
        this.stopAnimatingTitle();
        this.communication.gameDecisionMade(decisionId, result,
            this.channelNumber,
            function (json) {
                that.processGameEvents(json, true);
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
        this.communication.getCardInfo(cardId,
            function (json) {
                // DEBUG: console.log("Calling getCardModifiersFunction");
                that.setCardModifiers(json);
            });
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
                        $("#replayButton").attr("src", playImg);
                    } else {
                        that.replayPlay = true;
                        $("#replayButton").attr("src", pauseImg);
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
            // gameEvent is a json node
        var eventType = gameEvent.type;

        switch(eventType) {
            case "CA":
                this.animations.cardActivated(gameEvent, animate);
                break;
            case "CAC": // TODO - This game event was removed from the server side, so will never be called
                this.animations.cardAffectsCard(gameEvent, animate);
                break;
            case "D":
                this.animations.processDecision(gameEvent, animate);
                break;
            case "EG":
                this.processGameEnd();
                break;
            case "EP": // TODO - This game event was removed from the server side, so will never be called
                this.animations.eventPlayed(gameEvent, animate);
                break;
            case "GPC":
                this.animations.gamePhaseChange(gameEvent, animate);
                break;
            case "GS":
                this.animations.gameStats(gameEvent, animate);
                break;
            case "M":
                this.animations.message(gameEvent, animate);
                break;
            case "MCIP":
                this.animations.moveCardInPlay(gameEvent); // No animation exists for this event
                break;
            case "PCIP":
            case "PUT_SHARED_MISSION_INTO_PLAY":
                this.animations.putCardIntoPlay(gameEvent, animate, eventType);
                break;
            case "PLAYER_SCORE":
                this.animations.playerScore(gameEvent, animate);
                break;
            case "P":
                this.participant(gameEvent);
                break;
            case "RCFP":
                this.animations.removeCardFromPlay(gameEvent, animate);
                break;
            case "TC":
                this.animations.turnChange(gameEvent, animate);
                break;
            case "TSEQ":
                this.animations.tribbleSequence(gameEvent, animate);
                break;
            case "UPDATE_CARD_IMAGE":
                this.animations.updateCardImage(gameEvent);
                break;
            case "W":
                this.animations.warning(gameEvent, animate);
                break;
            default:
                console.error("Unknown game event type: '" + eventType + "'.");
        }
    }

    processGameEvents(jsonNode, animate) {
        try {
            this.channelNumber = jsonNode.channelNumber;
            var gameEvents = jsonNode.gameEvents;

            var hasDecision = false;

            // Go through all the events
            for (var i = 0; i < gameEvents.length; i++) {
                var gameEvent = gameEvents[i];
                this.processGameEvent(gameEvent, animate);
                var eventType = gameEvent.type;
                if (eventType == "D") {
                    hasDecision = true;
                }
            }

            if (this.allPlayerIds != null) {
                let clocks = jsonNode.clocks;
                if (clocks.length > 0) {
                    for (var i = 0; i < clocks.length; i++) {
                        let clock = clocks[i];
                        let playerId = clock.playerId;
                        let value = clock.timeRemaining;

                        let index = this.getPlayerIndex(playerId);

                        let sign = (value < 0) ? "-" : "";
                        value = Math.abs(value);
                        let hours = Math.floor(value / 3600);
                        let minutes = Math.floor(value / 60) % 60;
                        let seconds = value % 60;

                        if (hours > 0) {
                            $("#clock" + index).text(
                                sign + hours + ":" +
                                ((minutes < 10) ? ("0" + minutes) : minutes) + ":" +
                                ((seconds < 10) ? ("0" + seconds) : seconds)
                            );
                        }
                        else {
                            $("#clock" + index).text(
                                sign + minutes + ":" +
                                ((seconds < 10) ? ("0" + seconds) : seconds)
                            );
                        }
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
        for (var plId = 0; plId < this.allPlayerIds.length; plId++) {
            if (this.allPlayerIds[plId] == playerId) {
                return plId;
            }
        }
        return -1;
    }

    layoutZones() {
//        this.advPathGroup.layoutCards();
        for (var [playerId, cardGroup] of Object.entries(this.playPiles)) {
            cardGroup.layoutCards();
        }
        if (!this.spectatorMode) {
            this.hand.layoutCards();
        }
    }

    participant(json) {
        var participantId = json.participantId;
        this.allPlayerIds = json.allParticipantIds.split(",");
        var discardPublic = json.discardPublic;

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
        for (var i = 0; i < parameters.length; i++) {
            if (parameters[i].getAttribute("name") == name) {
                return parameters[i].getAttribute("value");
            }
        }

        return null;
    }

    getDecisionParameters(decision, name) {
        var result = new Array();
        var parameters = decision.getElementsByTagName("parameter");
        for (var i = 0; i < parameters.length; i++) {
            if (parameters[i].getAttribute("name") == name) {
                result.push(parameters[i].getAttribute("value"));
            }
        }

        return result;
    }

    cleanupDecision() {
        this.smallDialog.dialog("close");
        this.cardActionDialog.dialog("close");
        this.clearSelection();
        if (this.alertText != null) {
            this.alertText.html("");
        }
        if (this.alertButtons != null) {
            this.alertButtons.html("");
        }
        if (this.alertBox != null) {
            this.alertBox.removeClass("alert-box-highlight");
            this.alertBox.removeClass("alert-box-card-selection");
        }

        $(".card").each(
            function () {
                var card = $(this).data("card");
                if (card.zone == "EXTRA") {
                    $(this).remove();
                }
            });
        if (this.hand != null) {
            this.hand.layoutCards();
        }
    }

    integerDecision(decision) {
        let id = decision.decisionId;
        let text = decision.text;
        var val = 0;

        var min = decision.min;
        if (min == null) {
            min = 0;
        }
        var max = decision.max;
        if (max == null) {
            max = 1000;
        }

        var defaultValue = decision.defaultValue;
        if (defaultValue != null) {
            val = parseInt(defaultValue);
        }

        var that = this;
        this.smallDialog.html(text + `<br /><input id='integerDecision' value='${val}'>`);

        if (!this.replayMode) {
            this.smallDialog.dialog("option", "buttons",
                {
                    "OK": function () {
                        let retval = document.getElementById("integerDecision").value
                        $(this).dialog("close");
                        that.decisionFunction(id, retval);
                    }
                });
        }

        $("#integerDecision").spinner({
            min: parseInt(min),
            max: parseInt(max)
        });

        this.smallDialog.dialog("open");
        $('.ui-dialog :button').blur();
    }

    multipleChoiceDecision(decision) {
        var id = decision.decisionId;
        var text = decision.text;

        var results = decision.results;

        var that = this;
        this.smallDialog
            .html(text);

        if (results.length > 2 || this.gameSettings.get("alwaysDropDown")) {
            var html = "<br /><select id='multipleChoiceDecision' selectedIndex='0'>";
            for (var i = 0; i < results.length; i++) {
                html += "<option value='" + i + "'>" + results[i] + "</option>";
            }
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
                if (i > 0) {
                    this.smallDialog.append(" ");
                }

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
            if (!this.replayMode) {
                this.smallDialog.dialog("option", "buttons", {});
                this.PlayAwaitActionSound();
            }
        }

        this.smallDialog.dialog("open");
        $('.ui-dialog :button').blur();
    }

    ensureCardHasBoxes(cardDiv) {
        if ($(".cardStrength", cardDiv).length == 0) {
            var tokenOverlay = $(".tokenOverlay", cardDiv);

            var cardStrengthBgDiv = $(
                `<div class='cardStrengthBg'><img src='${strengthIconImg}' width='100%' height='100%'></div>`
            );
            tokenOverlay.append(cardStrengthBgDiv);

            var cardStrengthDiv = $("<div class='cardStrength'></div>");
            tokenOverlay.append(cardStrengthDiv);

            var cardVitalityBgDiv = $(
                `<div class='cardVitalityBg'><img src='${vitalityIconImg}' width='100%' height='100%'></div>`
            );
            tokenOverlay.append(cardVitalityBgDiv);

            var cardVitalityDiv = $("<div class='cardVitality'></div>");
            tokenOverlay.append(cardVitalityDiv);

            var cardSiteNumberBgDiv = $(
                `<div class='cardSiteNumberBg'><img src='${compassIconImg}' width='100%' height='100%'></div>`
            );
            cardSiteNumberBgDiv.css({display: "none"});
            tokenOverlay.append(cardSiteNumberBgDiv);

            var cardSiteNumberDiv = $("<div class='cardSiteNumber'></div>");
            cardSiteNumberDiv.css({display: "none"});
            tokenOverlay.append(cardSiteNumberDiv);

            var cardResistanceBgDiv = $(
                `<div class='cardResistanceBg'><img src='${resistanceIconImg}' width='100%' height='100%'></div>`
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
                    var lotrSiteNumberY = 0.905 * maxDimension - size / 2;

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
                        top: lotrSiteNumberY + "px",
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
                        top: lotrSiteNumberY + "px",
                        width: size,
                        height: size
                    });
                }
            };

            cardDiv.data("sizeListeners", sizeListeners);
            sizeListeners[0].sizeChanged(cardDiv, $(cardDiv).width(), $(cardDiv).height());
        }
    }

    createCardDivWithData(card, text) {
        var cardDiv = createCardDiv(card.imageUrl, text, card.isFoil(), true, false, card.hasErrata(), card.isUpsideDown(), card.cardId);

        cardDiv.data("card", card);

        return cardDiv;
    }

    attachSelectionFunctions(cardIds, selection) {
        if (selection) {
            if (cardIds.length > 0) {
                $(".card:cardId(" + cardIds + ")").addClass("selectableCard");
            }
        } else {
            if (cardIds.length > 0) {
                $(".card:cardId(" + cardIds + ")").addClass("actionableCard");
            }
        }
    }

    // Choosing cards from a predefined selection (for example stating fellowship)
    arbitraryCardsDecision(decision) {
        var id = decision.decisionId;
        var text = decision.text;

        var min = parseInt(decision.min);
        var max = parseInt(decision.max);

        var displayedCards = decision.displayedCards;

        var that = this;

        let allCardIds = new Array();
        var selectedCardIds = new Array();

        var selectableCardIds = new Array();

        this.cardActionDialog
            .html("<div id='arbitraryChoice'></div>")
            .dialog("option", "title", text);

        // Create the action cards and fill the dialog with them
        for (var i = 0; i < displayedCards.length; i++) {
            let selectableCard = displayedCards[i];
            var cardId = selectableCard.cardId;
            var blueprintId = selectableCard.blueprintId;
            var imageUrl = selectableCard.imageUrl;

            if (selectableCard.selectable == "true") {
                selectableCardIds.push(cardId);
            }
            allCardIds.push(cardId);
            var card = new Card(blueprintId, "SPECIAL", cardId, "", imageUrl, "", false);

            var cardDiv = this.createCardDivWithData(card);

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

        var selectAllCards = function () {
            selectedCardIds = Array.from(selectableCardIds);
            that.recalculateCardSelectionOrder(selectedCardIds);
            that.recalculateAllowedSelectionFromMaxCSS(selectableCardIds, selectedCardIds, max);
            allowSelection();
            processButtons();
        }

        var processButtons = function () {
            var buttons = {};
            if ((allCardIds.length <= max) &&
                (selectedCardIds.length != max)) {
                buttons["Select all"] = function() {
                    selectAllCards();
                    processButtons();
                }
            }
            if (selectedCardIds.length > 0) {
                buttons["Clear selection"] = function () {
                    resetChoice();
                    processButtons();
                };
            }

            if (selectedCardIds.length >= min) {
                buttons["Done"] = function () {
                    finishChoice();
                };
            }
            that.cardActionDialog.dialog("option", "buttons", buttons);
        };

        var allowSelection = function () {
            // this.selectionFunction is called when a card is clicked
            //   thanks to the code in clickCardFunction()
            that.selectionFunction = function (cardId) {
                // DEBUG: console.log("arbitraryCardsDecision -> allowSelection -> selectionFunction");
                // If the cardId is already selected, remove it.
                if (selectedCardIds.includes(cardId)) {
                    let index = selectedCardIds.indexOf(cardId);
                    selectedCardIds.splice(index, 1);
                }
                // Otherwise, if the cardId is not already selected, add it.
                else {
                    selectedCardIds.push(cardId);
                }
                
                that.recalculateCardSelectionOrder(selectedCardIds);
                that.recalculateAllowedSelectionFromMaxCSS(selectableCardIds, selectedCardIds, max);

                processButtons();
            };

            that.attachSelectionFunctions(selectableCardIds, true);
        };

        allowSelection();
        if (!this.replayMode) {
            processButtons();
            this.PlayAwaitActionSound();
        }

        openSizeDialog(this.cardActionDialog);
        this.arbitraryDialogResize(false);
        $('.ui-dialog :button').blur();
    }

    cardSelectionFromCombinations(decision) {
        var id = decision.decisionId;

        var min = parseInt(decision.min);
        var max = parseInt(decision.max);
        let displayedCards = decision.displayedCards;
        var cardIds = decision.cardIds;

        var jsonCombinations = decision.validCombinations;

        var that = this;

        var selectedCardIds = new Array();
        var selectableCardIds = new Array();

        this.cardActionDialog
            .html("<div id='cardSelectionFromCombinations'></div>")
            .dialog("option", "title", `Select ${min} to ${max} cards`);

        // Create the action cards and fill the dialog with them
        for (var i = 0; i < displayedCards.length; i++) {
            let displayedCard = displayedCards[i];
            var cardId = displayedCard.cardId;
            var blueprintId = displayedCard.blueprintId;
            var imageUrl = displayedCard.imageUrl;

            if (displayedCard.selectable == "true") {
                selectableCardIds.push(cardId);
            }

            var card = new Card(blueprintId, "SPECIAL", cardId, "", imageUrl, "", false);

            var cardDiv = this.createCardDivWithData(card);

            $("#cardSelectionFromCombinations").append(cardDiv);
        }

        var finishChoice = function () {
            that.cardActionDialog.dialog("close");
            $("#cardSelectionFromCombinations").html("");
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
            if (selectedCardIds.length > 0) {
                buttons["Clear selection"] = function () {
                    resetChoice();
                    processButtons();
                };
            }
            if ((selectedCardIds.length >= min) &&
                (selectedCardIds.length <= max)) {
                buttons["Done"] = function () {
                    finishChoice();
                };
            }
            that.cardActionDialog.dialog("option", "buttons", buttons);
        };

        var allowSelection = function () {
            // this.selectionFunction is called when a card is clicked
            //   thanks to the code in clickCardFunction()
            that.selectionFunction = function (cardId) {
                // DEBUG: console.log("arbitraryCardsDecision -> allowSelection -> selectionFunction");
                // If the cardId is already selected, remove it.
                if (selectedCardIds.includes(cardId)) {
                    let index = selectedCardIds.indexOf(cardId);
                    selectedCardIds.splice(index, 1);
                }
                // Otherwise, if the cardId is not already selected, add it.
                else {
                    selectedCardIds.push(cardId);
                }

                that.recalculateCardSelectionOrder(selectedCardIds);
                that.recalculateAllowedCombinationsAndCSS(cardIds, selectedCardIds, jsonCombinations, max);

                processButtons();
            };

            that.attachSelectionFunctions(selectableCardIds, true);
        };

        allowSelection();
        if (!this.replayMode) {
            processButtons();
            this.PlayAwaitActionSound();
        }

        openSizeDialog(this.cardActionDialog);
        this.arbitraryDialogResize(false);
        $('.ui-dialog :button').blur();
    }

    // Choosing one action to resolve, for example phase actions
    cardActionChoiceDecision(decision) {
        var id = decision.decisionId;
        var text = decision.text;
        let noPass = decision.noPass;
        let selectableCards = decision.displayedCards;

        var that = this;

        if (selectableCards.length == 0 && this.gameSettings.get("autoPass") && !this.replayMode) {
            that.decisionFunction(id, "");
            return;
        }

        var selectedCardIds = new Array();
        var allCardIds = new Array();

        this.alertText.html(text);
        // ****CCG League****: Border around alert box
        this.alertBox.addClass("alert-box-highlight");

        var processButtons = function () {
            that.alertButtons.html("");
            if (noPass != "true" && selectedCardIds.length == 0) {
                that.alertButtons.append("<button id='Pass'>Pass</button>");
                $("#Pass").button().click(function () {
                    finishChoice();
                });
            }
            if (selectedCardIds.length > 0) {
                that.alertButtons.append("<button id='ClearSelection'>Reset choice</button>");
                that.alertButtons.append("<button id='Done'>Done</button>");
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
                    if (card.zone == "EXTRA") {
                        $(this).remove();
                    }
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

            for (var i = 0; i < selectableCards.length; i++) {
                let selectableCard = selectableCards[i];
                var cardId = selectableCard.cardId;
                var actionId = selectableCard.actionId;
                var actionText = selectableCard.actionText;
                var blueprintId = selectableCard.blueprintId;
                var imageUrl = selectableCard.imageUrl;
                var actionType = selectableCard.actionType;

                if (blueprintId == "inPlay") {
                    var cardIdElem = getCardDivFromId(cardId);
                    allCardIds.push(cardId);
                } else {
                    hasVirtual = true;
                    allCardIds.push("extra" + cardId);
                    var card = new Card(blueprintId, "EXTRA", "extra" + cardId, null, imageUrl);

                    var cardDiv = that.createCardDivWithData(card);
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
                // DEBUG: console.log("cardActionChoiceDecision -> allowSelection -> selectionFunction");
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

            that.attachSelectionFunctions(allCardIds, false);
        };

        allowSelection();
        if (!this.replayMode) {
            processButtons();
            this.PlayAwaitActionSound();
        }

        $(':button').blur();
    }

    PlayAwaitActionSound() {
        let audio = new Audio(awaitingActionAudio);
        if(!document.hasFocus() || document.hidden || document.msHidden || document.webkitHidden)
        {
		    audio.play();
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
        var id = decision.decisionId;
        var text = decision.text;

        let displayedCards = decision.displayedCards;

        var that = this;

        var selectedActionIds = new Array();

        this.cardActionDialog
            .html("<div id='arbitraryChoice'></div>")
            .dialog("option", "title", text);

        var cardIds = new Array();

        for (var i = 0; i < displayedCards.length; i++) {
            let displayedCard = displayedCards[i];
            var blueprintId = displayedCard.blueprintId;
            var imageUrl = displayedCard.imageUrl;

            cardIds.push("temp" + i);
            var card = new Card(blueprintId, "SPECIAL", "temp" + i, "", imageUrl, "", false);

            var cardDiv = this.createCardDivWithData(card, displayedCard.actionText);

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
                // DEBUG: console.log("actionChoiceDecision -> allowSelection -> selectionFunction");
                var actionId = displayedCards[parseInt(cardId.substring(4))].actionId;
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
        if (!this.replayMode) {
            processButtons();
            this.PlayAwaitActionSound();
        }

        openSizeDialog(this.cardActionDialog);
        this.arbitraryDialogResize(false);
        $('.ui-dialog :button').blur();
    }

    // Choosing some number of cards, for example to wound
    cardSelectionDecision(decision) {
        var id = decision.decisionId;
        var text = decision.text;

        var min = decision.min;
        var max = decision.max;
        var cardIds = decision.cardIds;

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
                that.alertButtons.append("<button id='Done'>Done</button>");
                $("#Done").button().click(function () {
                    finishChoice();
                });
            }
        };

        var allowSelection = function () {
            // this.selectionFunction is called when a card is clicked
            //   thanks to the code in clickCardFunction()
            that.selectionFunction = function (cardId) {
                // DEBUG: console.log("cardSelectionDecision -> allowSelection -> selectionFunction");
                // If the cardId is already selected, remove it.
                if (selectedCardIds.includes(cardId)) {
                    let index = selectedCardIds.indexOf(cardId);
                    selectedCardIds.splice(index, 1);
                    getCardDivFromId(cardId).removeClass("selectedCard").addClass("selectableCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                }
                // Otherwise, if the cardId is not already selected, add it.
                else {
                    selectedCardIds.push(cardId);
                    getCardDivFromId(cardId).removeClass("selectableCard").addClass("selectedCard").addClass("selectedBadge");
                }

                that.recalculateCardSelectionOrder(selectedCardIds);
                
                // If the max number of cards are selected and the user has auto accept on, we're done.
                if ((selectedCardIds.length == max) && (that.gameSettings.get("autoAccept"))) {
                    finishChoice();
                    return;
                }

                processButtons();
            };

            that.attachSelectionFunctions(cardIds, true);
        };

        allowSelection();
        if (!this.replayMode) {
            processButtons();
            this.PlayAwaitActionSound();
        }
    }

    recalculateCardSelectionOrder(cardArray) {
        for (const [index, cardId] of cardArray.entries()) {
            let divToChange = getCardDivFromId(cardId);
            divToChange.attr("selectedOrder", index + 1); // use a 1-index
        }
    }

    recalculateAllowedSelectionFromMaxCSS(cardIds, selectedCardIds, max) {
        if (max === 0) {
            console.error("Max is 0, setting all cards to not selectable. This is probably a server bug.");
            for (const cardId of cardIds.values()) {
                getCardDivFromId(cardId).removeClass("selectableCard").removeClass("selectedCard").addClass("notSelectableCard").removeClass("selectedBadge").removeAttr("selectedOrder");
            }
            return;
        }
        else {
            for (const cardId of cardIds.values()) {
                if (selectedCardIds.length === 0) {
                    // everything is selectable
                    getCardDivFromId(cardId).addClass("selectableCard").removeClass("selectedCard").removeClass("notSelectableCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                }
                else {
                    // selected
                    if (selectedCardIds.includes(cardId)) {
                        getCardDivFromId(cardId).removeClass("selectableCard").removeClass("notSelectableCard").addClass("selectedCard").addClass("selectedBadge");
                    }
                    // not selected
                    else {
                        // we hit the max, gray out unselected cards since we can't add more
                        if (selectedCardIds.length === max) {
                            getCardDivFromId(cardId).addClass("notSelectableCard").removeClass("selectableCard").removeClass("selectedCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                            continue;
                        }
                        else {
                            getCardDivFromId(cardId).addClass("selectableCard").removeClass("selectedCard").removeClass("notSelectableCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                        }
                    }
                }
            }
            return;
        }
    }

    recalculateAllowedCombinationsAndCSS(cardIds, selectedCardIds, jsonCombinations, max) {
        let allowedCombinationsRemaining = new Set();
        
        if (selectedCardIds.length === 0) {
            // DEBUG: console.log("No selected cards.");
            allowedCombinationsRemaining = new Set(cardIds);
            // DEBUG: console.log(`Allowed combinations remaining: ${Array.from(allowedCombinationsRemaining)}`);
        }
        else if (selectedCardIds.length === 1) {
            // selected one card
            // DEBUG: console.log(`Selected cards: ${selectedCardIds}`);
            const cardId = selectedCardIds[0];
            let this_card_allowed = new Array();
            // DEBUG: console.log("let_this_card_allowed created");
            // DEBUG: console.log(jsonCombinations[cardId]);
            for (const compatible_cardId of jsonCombinations[cardId]) {
                // DEBUG: console.log("iterating through cards in jsonCombinations[" + cardId + "]");
                this_card_allowed.push(compatible_cardId);
            }
            const this_allowed_as_set = new Set(this_card_allowed);
            allowedCombinationsRemaining = this_allowed_as_set;
            
            // DEBUG: console.log(`Allowed combinations remaining: ${Array.from(allowedCombinationsRemaining)}`);
        }
        else {
            // selected two or more cards
            // DEBUG: console.log(`Selected cards: ${selectedCardIds}`);
            for (const [index, cardId] of selectedCardIds.entries()) {
                let this_card_allowed = new Array();
                // DEBUG: console.log("let_this_card_allowed created");
                // DEBUG: console.log(jsonCombinations[cardId]);
                for (const compatible_cardId of jsonCombinations[cardId]) {
                    // DEBUG: console.log("iterating through cards in jsonCombinations[" + cardId + "]");
                    this_card_allowed.push(compatible_cardId);
                }
                const this_allowed_as_set = new Set(this_card_allowed);

                if (index === 0) {
                    // Don't use .intersection on the first pass, since the intersection of empty set and valid choices is nothing.
                    allowedCombinationsRemaining = this_allowed_as_set;
                }
                else {
                    allowedCombinationsRemaining = allowedCombinationsRemaining.intersection(this_allowed_as_set);
                }
                // DEBUG: console.log(`Allowed combinations remaining: ${Array.from(allowedCombinationsRemaining)}`);
            }
        }


        // Apply CSS
        // BUG: Normally I'd split this into another function but when I did, JQuery
        //      didn't pass the Sets around properly. IDK. One big function it is.
        for (const cardId of cardIds.values()) {
            if (selectedCardIds.length === 0) {
                // everything is selectable
                // DEBUG: console.log("Everything is selectable.");
                getCardDivFromId(cardId).addClass("selectableCard").removeClass("selectedCard").removeClass("notSelectableCard").removeClass("selectedBadge").removeAttr("selectedOrder");
            }
            else {
                // selected
                if (selectedCardIds.includes(cardId)) {
                    getCardDivFromId(cardId).removeClass("selectableCard").removeClass("notSelectableCard").addClass("selectedCard").addClass("selectedBadge");
                }
                // not selected
                else {
                    // we hit the max, treat unselected cards as if they are not compatible
                    if (selectedCardIds.length === max) {
                        getCardDivFromId(cardId).addClass("notSelectableCard").removeClass("selectableCard").removeClass("selectedCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                        continue;
                    }

                    // Not selected, not at the max, and compatible with other selected cards
                    if (allowedCombinationsRemaining.has(cardId)) {
                        // DEBUG: console.log(`Not selected, compatible: ${cardId}`);
                        getCardDivFromId(cardId).addClass("selectableCard").removeClass("selectedCard").removeClass("notSelectableCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                    }
                    // Not selected, not at the max, but not compatible with other selected cards
                    else {
                        // DEBUG: console.log(`Not selected, not compatible: ${cardId}`);
                        // same as above but w/o selectableCard
                        getCardDivFromId(cardId).addClass("notSelectableCard").removeClass("selectableCard").removeClass("selectedCard").removeClass("selectedBadge").removeAttr("selectedOrder");
                    }
                }
            }
            
        }
        
    }


    clearSelection() {
        $(".selectableCard").removeClass("selectableCard").data("action", null);
        $(".actionableCard").removeClass("actionableCard").data("action", null);
        $(".selectedCard").removeClass("selectedCard").removeClass("selectedBadge").removeAttr("selectedOrder");
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
        this.statsDiv.append("<div id='tribbleSequence'></div>");
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
        if (this.spectatorMode) {
            heightScales = [6, 10, 10, 10, 6];
        }
        else {
            heightScales = [5, 9, 9, 10, 6, 10];
        }
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

        if (!this.spectatorMode) {
            this.hand.setBounds(HAND_LEFT, HAND_TOP, HAND_WIDTH, HAND_HEIGHT);
        }


        for (var playerId in this.discardPileGroups) {
            if (this.discardPileGroups.hasOwnProperty(playerId)) {
                this.discardPileGroups[playerId].layoutCards();
            }
        }

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

    addSharedMission(index, quadrant, region) {
        // TODO - no code here yet
    }

    addLocationDiv(index, quadrant, region) {
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
        newDiv.data("region", region);
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
        if (this.spectatorMode) {
            heightScales = [6, 10, 10, 10, 6];
        }
        else {
            heightScales = [5, 9, 9, 10, 6, 10];
        }
        var yScales = new Array();
        var scaleTotal = 0;
        for (var i = 0; i < heightScales.length; i++) {
            yScales[i] = scaleTotal;
            scaleTotal += heightScales[i];
        }

        var heightPerScale = (height - (padding * (heightScales.length + 1))) / scaleTotal;

        var advPathWidth = Math.min(150, width * 0.1);
        var specialUiWidth = 150;
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
            left: $("#bottomLeftTabs").offset().left + $("#bottomLeftTabs").width() + BORDER_PADDING * 5,
            top: $("#bottomLeftTabs").offset().top,
            width: HAND_LEFT - (this.tabPane.offset().left + this.tabPane.width() + BORDER_PADDING * 5) - BORDER_PADDING * 4,
            height: 40
        });
        this.gameStateElem.css({
            position: "absolute",
            left: padding * 2, // + advPathWidth,
            top: padding,
            width: specialUiWidth - padding + 75,
            //height: TABLE_AREA_TOP - padding * 2
            height: 117
        });
        this.alertBox.css({
            position: "absolute",
            left: $("#bottomLeftTabs").offset().left + $("#bottomLeftTabs").width() + BORDER_PADDING * 5,
            top: $("#bottomLeftTabs").offset().top + $("#statsDiv").height() + BORDER_PADDING * 5,
            width: HAND_LEFT - (this.tabPane.offset().left + this.tabPane.width() + BORDER_PADDING * 5) - BORDER_PADDING * 4,
            height: $("#bottomLeftTabs").height() - $("#statsDiv").height() - (BORDER_PADDING * 5)
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
            if (locationIndex === 0) {
                this.locationDivs[locationIndex].addClass("first-in-quadrant");
            } else if (currQuadrant != this.locationDivs[locationIndex - 1].data("quadrant")) {
                this.locationDivs[locationIndex].addClass("first-in-quadrant");
            } else if (this.locationDivs[locationIndex].hasClass("first-in-quadrant")) {
                this.locationDivs[locationIndex].removeClass("first-in-quadrant");
            }

            if (locationIndex === locationsCount - 1) {
                this.locationDivs[locationIndex].addClass("last-in-quadrant");
            } else if (currQuadrant != this.locationDivs[locationIndex + 1].data("quadrant")) {
                this.locationDivs[locationIndex].addClass("last-in-quadrant");
            } else if (this.locationDivs[locationIndex].hasClass("last-in-quadrant")) {
                this.locationDivs[locationIndex].removeClass("last-in-quadrant");
            }

            let currentRegion = this.locationDivs[locationIndex].data("region");
            let friendly_region_name = this._friendly_region_name(currentRegion);
            if (friendly_region_name !== "") {
                if (locationIndex === 0) {
                    this.locationDivs[locationIndex].addClass("first-in-region");
                } else if (currentRegion != this.locationDivs[locationIndex - 1].data("region")) {
                    this.locationDivs[locationIndex].addClass("first-in-region");
                } else if (this.locationDivs[locationIndex].hasClass("first-in-region")) {
                    this.locationDivs[locationIndex].removeClass("first-in-region");
                }
    
                if (locationIndex === locationsCount - 1) {
                    this.locationDivs[locationIndex].addClass("last-in-region");
                } else if (currentRegion != this.locationDivs[locationIndex + 1].data("region")) {
                    this.locationDivs[locationIndex].addClass("last-in-region");
                } else if (this.locationDivs[locationIndex].hasClass("last-in-region")) {
                    this.locationDivs[locationIndex].removeClass("last-in-region");
                }
            }

            if (currQuadrant === "ALPHA" ) {
                this.locationDivs[locationIndex].addClass("alpha-quadrant");

                if (friendly_region_name === "") {
                    this.locationDivs[locationIndex].attr("title", "Alpha Quadrant");
                }
                else {
                    this.locationDivs[locationIndex].attr("title", `${friendly_region_name} (Alpha Quadrant)`);
                }
            }
            if (currQuadrant === "GAMMA" ) {
                this.locationDivs[locationIndex].addClass("gamma-quadrant");

                if (friendly_region_name === "") {
                    this.locationDivs[locationIndex].attr("title", "Gamma Quadrant");
                }
                else {
                    this.locationDivs[locationIndex].attr("title", `${friendly_region_name} (Gamma Quadrant)`);
                }
            }
            if (currQuadrant === "DELTA" ) {
                this.locationDivs[locationIndex].addClass("delta-quadrant");
                
                if (friendly_region_name === "") {
                    this.locationDivs[locationIndex].attr("title", "Delta Quadrant");
                }
                else {
                    this.locationDivs[locationIndex].attr("title", `${friendly_region_name} (Delta Quadrant)`);
                }
            }
            if (currQuadrant === "MIRROR" ) {
                this.locationDivs[locationIndex].addClass("mirror-quadrant");
                
                if (friendly_region_name === "") {
                    this.locationDivs[locationIndex].attr("title", "Mirror Quadrant");
                }
                else {
                    this.locationDivs[locationIndex].attr("title", `${friendly_region_name} (Mirror Quadrant)`);
                }
            }

            this.missionCardGroups[locationIndex].setBounds(x, y + locationDivHeight/3, locationDivWidth, locationDivHeight/3);
            this.missionCardGroups[locationIndex].layoutCards();
            this.opponentAtLocationCardGroups[locationIndex].setBounds(x, y, locationDivWidth, locationDivHeight / 3);
            this.opponentAtLocationCardGroups[locationIndex].layoutCards();
            this.playerAtLocationCardGroups[locationIndex].setBounds(x, y + 2 * locationDivHeight/3, locationDivWidth, locationDivHeight / 3);
            this.playerAtLocationCardGroups[locationIndex].layoutCards();

            x = (x + locationDivWidth + (LOCATION_BORDER_PADDING / 2));
        }

        for (let playerId in this.discardPileGroups) {
            if (this.discardPileGroups.hasOwnProperty(playerId)) {
                this.discardPileGroups[playerId].layoutCards();
            }
        }

        for (let playerId in this.adventureDeckGroups) {
            if (this.adventureDeckGroups.hasOwnProperty(playerId)) {
                this.adventureDeckGroups[playerId].layoutCards();
            }
        }

        for (let playerId in this.removedPileGroups) {
            if (this.removedPileGroups.hasOwnProperty(playerId)) {
                this.removedPileGroups[playerId].layoutCards();
            }
        }

        for (let playerId in this.miscPileGroups) {
            if (this.miscPileGroups.hasOwnProperty(playerId)) {
                this.miscPileGroups[playerId].layoutCards();
            }
        }

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

    // Converts region enum sent from Region.java to a user friendly string
    _friendly_region_name(locationDiv) {
        if (locationDiv === null ||
            locationDiv === undefined) {
            return "";
        }
        switch (locationDiv) {
            // 1E regions
            case "ARGOLIS_CLUSTER":
                return "Argolis Cluster Region";
            case "BADLANDS":
                return "Badlands Region";
            case "BAJOR":
                return "Bajor Region";
            case "BRIAR_PATCH":
                return "Briar Patch Region";
            case "CARDASSIA":
                return "Cardassia Region";
            case "CETI_ALPHA":
                return "Ceti Alpha Region";
            case "CHIN_TOKA":
                return "Chin'toka Region";
            case "DELPHIC_EXPANSE":
                return "Delphic Expanse Region";
            case "DEMILITARIZED_ZONE":
                return "Demilitarized Zone Region";
            case "GREAT_BARRIER":
                return "Great Barrier Region";
            case "MCALLISTER":
                return "McAllister Region";
            case "MURASAKI":
                return "Murasaki Region";
            case "MUTARA":
                return "Mutara Region";
            case "NEKRIT_EXPANSE":
                return "Nekrit Expanse Region";
            case "NEUTRAL_ZONE":
                return "Neutral Zone Region";
            case "NORTHWEST_PASSAGE":
                return "Northwest Passage Region";
            case "ROMULUS_SYSTEM":
                return "Romulus System Region";
            case "SECTOR_001":
                return "Sector 001 Region";
            case "TELLUN":
                return "Tellun Region";
            case "VALO":
                return "Valo Region Region";
            // 2E regions
            case "QO_NOS_SYSTEM":
                return "Qo'noS Region";
            default:
                return "";
        }
    }

}