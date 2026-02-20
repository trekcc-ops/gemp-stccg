import GempClientCommunication from './communication.js';
import { log, getUrlParam, getAffiliationName } from './common.js';
import Card from './jCards.js';
import { createCardDiv, createFullCardDiv, getCardDivFromId } from './jCards.js';
import { NormalCardGroup, PlayPileCardGroup, NormalGameCardGroup, TableCardGroup } from './jCardGroup.js';
import { animateActionResult, communicateActionResult, getSpacelineIndexFromLocationId } from './actionResults.js';
import { processDecision } from './decisions.js';
import GameAnimations from './gameAnimations.js';
import ChatBoxUI from './chat.js';
import { openSizeDialog, showLinkableCardTitle, removeFromArray } from "./common.js";
import Cookies from "js-cookie";
import playImg from "../../images/play.png";
import pauseImg from "../../images/pause.png";
import strengthIconImg from "../../images/o_icon_strength.png";
import vitalityIconImg from "../../images/o_icon_strength.png";
import compassIconImg from "../../images/o_icon_strength.png";
import resistanceIconImg from "../../images/o_icon_strength.png";


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
    hasDecision = false;

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
    lastActionIndex;

    constructor(url, replayMode) {
        this.replayMode = replayMode;

        log("ui initialized");
        var that = this;
        this.mainDiv = $("#main");
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
                return (card.zone === "HAND" || card.zone === "EXTRA" || card.zone === "MISSIONS_PILE" || card.zone === "SEED_DECK");
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

        // Left click on card
        $("body").click(
            function (event) {
                return that.clickCardFunction(event);
            });

        // Right click on card
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
        for (let setting of that.gameSettings.entries()) {
            let settingName = setting[0];
            if (settingName !== "autoPass") { // TODO: currently, autoPass always set to false
                let optionSelection = $("#" + settingName);
                let cookieValue = Cookies.get(settingName);

                    // Multiple choice settings: foilPresentation
                if (settingName === "foilPresentation" && cookieValue !== undefined) {
                    optionSelection.val(cookieValue);
                    that.gameSettings.set(settingName, cookieValue);
                }

                    // True/false settings: autoAccept, alwaysDropDown
                if (cookieValue === "true" || cookieValue === undefined) {
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
        let allPhaseNames = that.gamePhases;
        let autoPassArr = new Array();
        let autoPassArrHashtag = new Array();
        for (let i = 0; i < allPhaseNames.length; i++) {
            autoPassArr.push("autoPass" + allPhaseNames[i]);
            autoPassArrHashtag.push("#autoPass" + allPhaseNames[i]);
        }

        // Load auto-pass settings from cookie, or set to default (current default is all phases auto-pass)
        let currPassedPhases = new Array();
        let currAutoPassCookieValue = Cookies.get("autoPassPhases");
        if (currAutoPassCookieValue === undefined) {
            currPassedPhases = allPhaseNames;
        }
        else {
            currPassedPhases = currAutoPassCookieValue.split("0");
        }

        // Create settings panel for user selection of auto-pass settings
        for (let i = 0; i < allPhaseNames.length; i++) {
            $("#autoPassOptionsDiv").append(
                "<input id='" + autoPassArr[i] + "' type='checkbox' value='selected' />" +
                "<label for='" + autoPassArr[i] + "'>" + allPhaseNames[i] + "</label> "
            );
        }

        // Populate settings panel with current user options
        for (let i = 0; i < currPassedPhases.length; i++) {
            $(autoPassArrHashtag[i]).prop("checked", true);
        }

        // Save user selections to cookie
        $(autoPassArrHashtag.join(",")).bind("change", function () {
            let newAutoPassPhases = "";
            for (let i = 0; i < allPhaseNames.length; i++) {
                if ($("#autoPass" + allPhaseNames[i]).prop("checked")) {
                    newAutoPassPhases += "0" + allPhaseNames[i];
                }
            }
            if (newAutoPassPhases.length > 0) {
                newAutoPassPhases = newAutoPassPhases.substr(1);
            }
            Cookies.set("autoPassPhases", newAutoPassPhases, {expires: 365});
        });

        let chatRoomName = (this.replayMode ? undefined : ("Game" + getUrlParam("gameId")));
        let chatBoxDiv = $("#chatBox");
        let chatBoxUrl = this.communication.url;
        let showList = false;

        let playerListener = function (players) {
            let val = "";
            for (let i = 0; i < players.length; i++) {
                val += players[i] + "<br/>";
            }
            $("a[href='#playersInRoomBox']").html("Players(" + players.length + ")");
            $("#playersInRoomBox").html(val);
        };

        let showHideSystemButton = false;

        let displayChatListener = function(title, message) {
            let dialog = $("<div></div>").dialog({
                title: title,
                resizable: true,
                height: 200,
                modal: true,
                buttons: {}
            }).html(message);
        };

        let allowDiscord = false;
        this.chatBox = new ChatBoxUI(
            chatRoomName,
            chatBoxDiv,
            chatBoxUrl,
            showList,
            playerListener,
            showHideSystemButton,
            displayChatListener,
            allowDiscord
        );
        this.chatBox.chatUpdateInterval = 3000;

        if (!this.spectatorMode && !this.replayMode) {
            $("#concedeGame").button().click(
                function () {
                    that.communication.concede();
                }
            );
            $("#cancelGame").button().click(
                function () {
                    that.communication.cancel();
                }
            );
        }
    }

    clickCardFunction(event) {
        // Return false == handled, return true == not handled
        var tar = $(event.target);

        // Clicked on linked card name in chat, see showLinkableCardTitle()
        if (tar.hasClass("cardHint")) {
            let blueprintId = tar.attr("value");
            let zone = "SPECIAL";
            let cardId = "hint";
            let noOwner = "";
            let imageUrl = tar.attr("card_img_url");
            let title = tar.attr("data-title");
            let emptyLocationIndex = "";
            let upsideDown = false;

            let card = new Card(blueprintId, zone, cardId, noOwner, title, imageUrl, emptyLocationIndex, upsideDown);
            this.displayCard(card, false);
            event.stopPropagation();
            return false;
        }

        if (!this.successfulDrag && this.infoDialog.dialog("isOpen")) {
            this.infoDialog.dialog("close");
            event.stopPropagation();
            return false;
        }

        // Click on card
        if (tar.hasClass("actionArea")) {
            var selectedCardElem = tar.closest(".card");
            if (!this.successfulDrag) {
                if (event.shiftKey || event.which > 1) {
                    this.displayCardInfo(selectedCardElem.data("card"));
                }
                else if ((selectedCardElem.hasClass("selectableCard") ||
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
                let cardElems = getCardDivFromId(this.dragCardId);
                if (cardElems.length > 0) {
                    let cardElem = cardElems[0];
                    let cardData = $(cardElem).data("card");
                    this.draggingHorizontaly = true;
                    let cardGroup = this.getReorganizableCardGroupForCardData(cardData);
                    if (cardGroup != null) {
                        let cardsInGroup = cardGroup.getCardElems();
                        for (let i = 0; i < cardsInGroup.length; i++) {
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
                let cardElems = getCardDivFromId(this.dragCardId);
                if (cardElems.length > 0) {
                    let cardElem = $(cardElems[0]);
                    let cardData = cardElem.data("card");
                    let cardGroup = this.getReorganizableCardGroupForCardData(cardData);
                    if (cardGroup != null) {
                        let cardsInGroup = cardGroup.getCardElems();
                        let width = cardElem.width();
                        let currentIndex;
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

                        let cardIdAtIndex = $(cardsInGroup[currentIndex]).data("card").cardId;
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

    displayCard(cardData, extraSpace) {
        this.infoDialog.html("");
        this.infoDialog.html("<div style='scroll: auto'></div>");
        var floatCardDiv = $("<div style='float: left;'></div>");
        let noborder = false;
        let cardDiv = createFullCardDiv(cardData.imageUrl, cardData.foil, cardData.horizontal, noborder, cardData.title);
        let jqCardDiv = $(cardDiv);
        floatCardDiv.append(jqCardDiv);
        this.infoDialog.append(floatCardDiv);
        if (extraSpace) {
            this.infoDialog.append("<div id='cardEffects'></div>");
        }

        var windowWidth = $(window).width();
        var windowHeight = $(window).height();

        var horSpace = (extraSpace ? 200 : 0) + 30;
        var vertSpace = 45;

        if (cardData.horizontal) {
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

    displayCardInfo(cardData) {
        let showModifiers = false;
        var cardId = cardData.cardId;
        if (cardId == null || (cardId.length >= 5 && cardId.substring(0, 5) == "extra")) {
            showModifiers = false;
        }
        else if (!this.replayMode && (cardId.length < 4 || cardId.substring(0, 4) != "temp")) {
            showModifiers = true;
        }
        // DEBUG: console.log("displayCardInfo for cardId " + cardId);
        // DEBUG: console.log("showModifiers = " + showModifiers);

        this.displayCard(cardData, showModifiers);

        if (showModifiers) {
            this.getCardModifiersFunction(cardId, this.setCardModifiers);
        }
    }

    setCardModifiers(json) {
        // DEBUG: console.log("Calling setCardModifiers");
        let modifiers = json.modifiers; // list of HTML strings
        let isStopped = json.isStopped; // boolean
        let affiliations = json.affiliations; // list of affiliation enum names
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
                if (modifier != "null") {
                    html = html + modifier + "<br/>";
                }
            }
            html = html + "<br/>";
        }

        // Indicate if card is stopped
        if (isStopped) {
            html = html + "<i>Stopped</i><br/>";
        }

        // Show names of affiliation(s)
        if (affiliations != null && affiliations.length > 0) {
            html = html + "<b>Affiliation:</b> ";
            for (const affiliation of affiliations) {
                html = html + getAffiliationName(affiliation);
            }
            html = html + "<br/>";
        }

        // Show card icons
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
                html = html + showLinkableCardTitle(jsonList[i]);
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
                that.initializeGameState(json);
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
        this.hasDecision = false;
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
        let gameState = typeof gameEvent.gameState === "string" ? JSON.parse(gameEvent.gameState) : gameEvent.gameState;

        if (this.allPlayerIds == null || this.allPlayerIds.length == 0) {
            this.initializePlayerOrder(gameState);
        }

        switch(eventType) {
            case "ACTION_RESULT": {
                this.updateGameStats(gameState); // updates count of card piles
                this.animations.gamePhaseChange(gameState); // includes adding cards to seed piles
                this.animations.turnChange(gameState, true);
                let firstActionToReceive = (this.lastActionIndex == null) ? 0 : this.lastActionIndex + 1;
                for (let i = firstActionToReceive; i < gameState.performedActions.length; i++) {
                    let action = gameState.performedActions[i];
                    if (action.status === "completed_success") {
                        animateActionResult(action, gameState, this.animations);
                        communicateActionResult(action, gameState, this);
                    } else if (action.status === "completed_failure" && (action.actionType === "ATTEMPT_MISSION" || action.actionType === "ENCOUNTER_SEED_CARD")) {
                        // Pass a message to the play history if a mission attempt or seed card encounter was failed
                        communicateActionResult(action, gameState, this);
                    }
                    this.lastActionIndex = i;
                }
                break;
            }
            case "M":
                this.animations.message(gameEvent, animate);
                break;
            case "W":
                this.animations.warning(gameEvent, animate);
                break;
            case "CA": // TODO - This game event flashes the border around the card. No longer called in server.
                this.animations.cardActivated(gameEvent, animate);
                break;
            case "CAC": // TODO - This game event was removed from the server side, so will never be called
                this.animations.cardAffectsCard(gameEvent, animate);
                break;
            case "EP": // TODO - This game event was removed from the server side, so will never be called
                this.animations.eventPlayed(gameEvent, animate);
                break;
            default:
                console.error("Unknown game event type: '" + eventType + "'.");
        }
    }

    initializeGameState(jsonNode) {

        try {
            this.channelNumber = jsonNode.channelNumber;

            let gameState = jsonNode.gameState;
            let cardsAdded = new Array();
            let cardsStillToAdd = Object.keys(gameState.visibleCardsInGame);
            let cardToAdd;

            if (gameState.playerMap != null && Object.keys(gameState.playerMap).length > 0) {
                // console.log("Calling initializePlayerOrder from initializeGameState");
                this.initializePlayerOrder(gameState);
                this.updateGameStats(gameState);
            }

            this.animations.gamePhaseChange(gameState); // includes adding cards to seed piles

            for (const player of Object.values(gameState.playerMap)) {
                for (const cardId of player.cardGroups["CORE"].cardIds) {
                    cardToAdd = gameState.visibleCardsInGame[cardId];
                    if (cardToAdd == null) {
                        console.error("Unable to find card of cardId '" + cardId + "'");
                    } else if (cardsAdded.includes(cardId)) {
                        console.error("Trying to add a card to core, but it's already been added");
                    } else {
                        this.animations.putNonMissionIntoPlay(cardToAdd, cardToAdd.owner, gameState, "-1", false);
                        cardsAdded.push(cardId);
                        cardsStillToAdd = removeFromArray(cardsStillToAdd, cardId);
                    }
                }
                for (const cardId of player.cardGroups["HAND"].cardIds) {
                    cardToAdd = gameState.visibleCardsInGame[cardId];
                    if (cardId == "-99") {
                        console.log("Card is hidden information");
                    } else if (cardToAdd == null) {
                        console.error("Unable to find card of cardId '" + cardId + "'");
                    } else if (cardsAdded.includes(cardId)) {
                        console.error("Trying to add a card to hand, but it's already been added");
                    } else {
                        this.animations.addCardToHiddenZone(cardToAdd, "HAND", player.playerId);
                        cardsAdded.push(cardId);
                        cardsStillToAdd = removeFromArray(cardsStillToAdd, cardId);
                    }
                }
                for (const cardId of player.cardGroups["DISCARD"].cardIds) {
                    cardToAdd = gameState.visibleCardsInGame[cardId];
                    if (cardToAdd == null) {
                        console.error("Unable to find card of cardId '" + cardId + "'");
                    } else if (cardsAdded.includes(cardId)) {
                        console.error("Trying to add a card to discard, but it's already been added");
                    } else {
                        this.animations.addCardToHiddenZone(cardToAdd, "DISCARD", player.playerId);
                        cardsAdded.push(cardId);
                        cardsStillToAdd = removeFromArray(cardsStillToAdd, cardId);
                    }
                }
            }

            for (const location of gameState.spacelineLocations) {
                for (let i = 0; i < location.missionCardIds.length; i++) {
                    let missionCardId = location.missionCardIds[i];
                    let missionCard = gameState.visibleCardsInGame[missionCardId];
                    let spacelineIndex = getSpacelineIndexFromLocationId(missionCard.locationId, gameState);
                    let firstMissionAtLocation = (i == 0);
                    this.animations.putMissionIntoPlay(missionCard, false, location, spacelineIndex, firstMissionAtLocation);
                    cardsAdded.push(missionCardId);
                    cardsStillToAdd = removeFromArray(cardsStillToAdd, missionCardId);
                }
            }

            for (let [cardId, cardData] of Object.entries(gameState.visibleCardsInGame)) {
                if (!cardData.isInPlay && cardsStillToAdd.includes(cardId)) {
                    cardsStillToAdd = removeFromArray(cardsStillToAdd, cardId);
                } else if (!cardsAdded.includes(cardId)) {
                    let cardDiv = getCardDivFromId(cardId);
                    if (cardDiv.length == 0) {
                        this.addNonMissionInPlayToClientRecursively(cardData, gameState);
                    }
                    cardsAdded.push(cardId);
                    cardsStillToAdd = removeFromArray(cardsStillToAdd, cardId);
                }
            }

            if (cardsStillToAdd.length > 0) {
                console.error("Was unable to add all cards");
            }

            this.setupClocks(jsonNode.gameState);

            for (const action of gameState.performedActions) {
                communicateActionResult(action, gameState, this);
            }

            this.lastActionIndex = jsonNode.gameState.performedActions.length - 1;

            let pendingDecision = gameState.pendingDecision;
            if (pendingDecision != null) {
                processDecision(pendingDecision, true, this, gameState);
                this.startAnimatingTitle();
            } else {
                this.animations.updateGameState(false);
            }

        } catch (e) {
            console.error(e);
            this.showErrorDialog(
                "Game error",
                "There was an error while initializing the game state",
                true, false, false
            );
        }
    }

    setupClocks(gameState) {
        if (this.allPlayerIds != null) {
            let clocks = gameState.playerClocks;
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

    processGameEvents(jsonNode, animate) {
        try {
            this.channelNumber = jsonNode.channelNumber;

            // Go through all the events
            for (let i = 0; i < jsonNode.gameEvents.length; i++) {
                let gameEvent = jsonNode.gameEvents[i];
                this.processGameEvent(gameEvent, animate);
                if (i === jsonNode.gameEvents.length - 1) {
                    let gameState;
                    if (gameEvent.gameState) {
                        gameState = typeof(gameEvent.gameState) === "string" ? JSON.parse(gameEvent.gameState) : gameEvent.gameState;
                    }
                    else {
                        continue;
                    }
                    let userDecision = gameState.pendingDecision;
                    if (this.hasDecision === false && userDecision != null && typeof userDecision != "undefined") {
                        this.hasDecision = true;
                        processDecision(userDecision, animate, this, gameState);
                        this.startAnimatingTitle();
                    }
                }
            }

            this.setupClocks(jsonNode);

            if (!this.hasDecision) {
                this.animations.updateGameState(animate);
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

    initializePlayerOrder(gameState) {
        this.bottomPlayerId = gameState.requestingPlayer;
        this.allPlayerIds = new Array();

        for (const playerId of Object.keys(gameState.playerMap)) {
            this.allPlayerIds.push(playerId);
            this.createPile(playerId, "'Removed From Game' Pile", "removedPileDialogs", "removedPileGroups");
            this.createPile(playerId, "Discard Pile", "discardPileDialogs", "discardPileGroups");
        }

        var index = this.getPlayerIndex(this.bottomPlayerId);
        if (index == -1) {
            const last_index = Object.keys(gameState.playerMap).length - 1;
            this.bottomPlayerId = Object.values(gameState.playerMap)[last_index].playerId;
            this.spectatorMode = true;
        } else {
            this.spectatorMode = false;
            this.createPile(this.bottomPlayerId, "Draw Deck", "miscPileDialogs", "miscPileGroups");
        }

        this.initializeGameUI(true);
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

    createCardDivWithData(card) {
        let baseCardDiv = createCardDiv(card.imageUrl, card.title, card.isFoil(), card.status_tokens, false, card.hasErrata(), card.isUpsideDown(), card.cardId);
        let cardDiv = $(baseCardDiv); // convert to jQuery object

        cardDiv.data("card", card);

        return cardDiv;
    }

    clearSelection() {
        $(".notSelectableCard").removeClass("notSelectableCard").data("action", null);
        $(".selectableCard").removeClass("selectableCard").data("action", null);
        $(".selectedCard").removeClass("selectedCard").removeClass("selectedBadge").removeAttr("selectedOrder").data("action", null);
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
        for (let i = 0; i < heightScales.length; i++) {
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

        if (!this.spectatorMode) {
            this.hand.setBounds(HAND_LEFT, HAND_TOP, HAND_WIDTH, HAND_HEIGHT);
        }


        for (var playerId in this.discardPileGroups) {
            if (Object.hasOwn(this.discardPileGroups, playerId)) {
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
        let locationBeforeCount = this.locationDivs.length;
        for (let i=locationBeforeCount-1; i>=index; i--) {
            this.locationDivs[i].data( "locationIndex", i+1);
            this.locationDivs[i].removeAttr("id");
            this.locationDivs[i].attr("id", "location" + (i+1));

            let missionCardGroup = this.missionCardGroups[i].getCardElems();
            for (let j=0; j<missionCardGroup.length; j++) {
                let cardData = $(missionCardGroup[j]).data("card");
                cardData.locationIndex = i+1;
            }
            this.missionCardGroups[i].locationIndex = i+1;
            
            let opponentAtLocationCardGroup = this.opponentAtLocationCardGroups[i].getCardElems();
            for (let j=0; j<opponentAtLocationCardGroup.length; j++) {
                let cardData = $(opponentAtLocationCardGroup[j]).data("card");
                cardData.locationIndex = i+1;
            }
            this.opponentAtLocationCardGroups[i].locationIndex = i+1;

            let playerAtLocationCardGroup = this.playerAtLocationCardGroups[i].getCardElems();
            for (let j=0; j<playerAtLocationCardGroup.length; j++) {
                let cardData = $(playerAtLocationCardGroup[j]).data("card");
                cardData.locationIndex = i+1;
            }
            this.playerAtLocationCardGroups[i].locationIndex = i+1;            
        }

        let newDiv = $("<div id='location" + index + "' class='ui-widget-content locationDiv'></div>");
        newDiv.data( "locationIndex", index);
        newDiv.data( "quadrant", quadrant);
        newDiv.data("region", region);
        $("#main").append(newDiv);

        this.locationDivs.splice(index, 0, newDiv);

        let missionOrOnTopOfMission = function(card) {
            if (card.locationIndex == this.locationIndex) {
                if (card.zone == "SPACELINE" || card.zone == "PLACED_ON_MISSION") {
                    return true;
                }
            }
            // else
            return false;
        };
        // TODO - MissionCardGroup class exists for this, but using TableCardGroup to test beaming function
        let missionCardGroup = new TableCardGroup($("#main"), missionOrOnTopOfMission, false, index, this.bottomPlayerId);
        this.missionCardGroups.splice(index, 0, missionCardGroup);

        let opponentAtLocationCardGroup = new TableCardGroup($("#main"), function (card) {
            return (card.zone == "AT_LOCATION" && card.locationIndex == this.locationIndex && card.owner != that.bottomPlayerId);
        }, false, index, this.bottomPlayerId);
        this.opponentAtLocationCardGroups.splice(index, 0, opponentAtLocationCardGroup);

        let playerAtLocationCardGroup = new TableCardGroup($("#main"), function (card) {
            return (card.zone == "AT_LOCATION" && card.locationIndex == this.locationIndex && card.owner == that.bottomPlayerId);
        }, false, index, this.bottomPlayerId);
        this.playerAtLocationCardGroups.splice(index, 0, playerAtLocationCardGroup);

        this.layoutUI(false);
    }

    layoutUI(sizeChanged) {
        let padding = this.padding;

        let width = $(window).width();
        let height = $(window).height();
        if (sizeChanged) {
            this.windowWidth = width;
            this.windowHeight = height;
        } else {
            width = this.windowWidth;
            height = this.windowHeight;
        }

        let BORDER_PADDING = 2;
        let LOCATION_BORDER_PADDING = 4;

        // Defines the relative height of the opponent/player/table areas of the UI.
        let OPPONENT_AREA_HEIGHT_SCALE = 0.15;
        let PLAYER_AREA_HEIGHT_SCALE = 0.3;

        // Defines the minimum/maximum height of the opponent/player/table areas of the UI. No max for table area.
        let MIN_OPPONENT_AREA_HEIGHT = 114;
        let MAX_OPPONENT_AREA_HEIGHT = 140;
        let MIN_PLAYER_AREA_HEIGHT = MIN_OPPONENT_AREA_HEIGHT * Math.floor(PLAYER_AREA_HEIGHT_SCALE / OPPONENT_AREA_HEIGHT_SCALE);
        let MAX_PLAYER_AREA_HEIGHT = MAX_OPPONENT_AREA_HEIGHT * Math.floor(PLAYER_AREA_HEIGHT_SCALE / OPPONENT_AREA_HEIGHT_SCALE);

        // Sets the top and height of the opponent/player/table areas of the UI.
        let OPPONENT_AREA_TOP = 0;
        let OPPONENT_AREA_HEIGHT = Math.min(MAX_OPPONENT_AREA_HEIGHT, Math.max(MIN_OPPONENT_AREA_HEIGHT, Math.floor(height * OPPONENT_AREA_HEIGHT_SCALE)));

        let PLAYER_AREA_HEIGHT = Math.min(MAX_PLAYER_AREA_HEIGHT, Math.max(MIN_PLAYER_AREA_HEIGHT, Math.floor(height * PLAYER_AREA_HEIGHT_SCALE)));
        let PLAYER_AREA_TOP = height - BORDER_PADDING - PLAYER_AREA_HEIGHT;
        let PLAYER_CARD_PILES_AND_SIDE_OF_TABLE_HEIGHT = Math.floor(PLAYER_AREA_HEIGHT / 2);
        let PLAYER_ACTION_AREA_AND_HAND_TOP = PLAYER_AREA_TOP + PLAYER_CARD_PILES_AND_SIDE_OF_TABLE_HEIGHT + BORDER_PADDING - 1;
        let PLAYER_ACTION_AREA_AND_HAND_HEIGHT = PLAYER_AREA_HEIGHT - PLAYER_CARD_PILES_AND_SIDE_OF_TABLE_HEIGHT - BORDER_PADDING;
        let TABLE_AREA_TOP = OPPONENT_AREA_HEIGHT + BORDER_PADDING;
        let TABLE_AREA_HEIGHT = Math.max(0, PLAYER_AREA_TOP - LOCATION_BORDER_PADDING - TABLE_AREA_TOP);

        // Defines the sizes of other items in the UI.
        let LEFT_SIDE = 0;
        let GAME_STATE_AND_CHAT_WIDTH = 300;
        let CARD_PILE_AND_ACTION_AREA_LEFT = GAME_STATE_AND_CHAT_WIDTH + BORDER_PADDING - 1;
        let CARD_PILE_AND_ACTION_AREA_WIDTH = 141;
        let CARD_PILE_WIDTH_1 = Math.floor(CARD_PILE_AND_ACTION_AREA_WIDTH / 3);
        let CARD_PILE_LEFT_2 = CARD_PILE_AND_ACTION_AREA_LEFT + CARD_PILE_WIDTH_1 + BORDER_PADDING - 1;
        let CARD_PILE_WIDTH_2 = CARD_PILE_WIDTH_1;
        let CARD_PILE_LEFT_3 = CARD_PILE_LEFT_2 + CARD_PILE_WIDTH_2 + BORDER_PADDING - 1;
        let CARD_PILE_WIDTH_3 = CARD_PILE_AND_ACTION_AREA_WIDTH - CARD_PILE_WIDTH_1 - CARD_PILE_WIDTH_2;
        let TAB_PANE_HEIGHT = 25;
        let TAB_PANE_WIDTH_PADDING = 4;
        let CHAT_HEIGHT = PLAYER_AREA_HEIGHT - BORDER_PADDING + 1;
        let CHAT_WIDTH = GAME_STATE_AND_CHAT_WIDTH;

        // Sets the hand and side of table left and width
        let HAND_LEFT = CARD_PILE_LEFT_3 + CARD_PILE_WIDTH_3 + BORDER_PADDING - 1;
        let HAND_WIDTH = (width - HAND_LEFT) - BORDER_PADDING;
        let SIDE_OF_TABLE_LEFT = CARD_PILE_LEFT_3 + CARD_PILE_WIDTH_3 + BORDER_PADDING - 1;
        let SIDE_OF_TABLE_WIDTH = (width - SIDE_OF_TABLE_LEFT) - BORDER_PADDING;

        $("#bottomLeftTabs").css({left:LEFT_SIDE, top: PLAYER_AREA_TOP, width: CHAT_WIDTH - 50, height: CHAT_HEIGHT});
        this.tabPane.css({position: "absolute", left:LEFT_SIDE, top: PLAYER_AREA_TOP, width: CHAT_WIDTH, height: CHAT_HEIGHT});
        this.chatBox.setBounds(BORDER_PADDING + TAB_PANE_WIDTH_PADDING, TAB_PANE_HEIGHT,
            CHAT_WIDTH - (2 * TAB_PANE_WIDTH_PADDING), CHAT_HEIGHT - TAB_PANE_HEIGHT);

        // Old LotR gemp code for heightScales
        let heightScales;
        if (this.spectatorMode) {
            heightScales = [6, 10, 10, 10, 6];
        }
        else {
            heightScales = [5, 9, 9, 10, 6, 10];
        }
        let yScales = new Array();
        let scaleTotal = 0;
        for (let i = 0; i < heightScales.length; i++) {
            yScales[i] = scaleTotal;
            scaleTotal += heightScales[i];
        }

        let specialUiWidth = 150;

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
            left: padding * 2,
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

        for (let i = 0; i < 2; i++) {
            let top = 0;
            let height = 0;
            let playerId = this.allPlayerIds[i];
            if (playerId === this.bottomPlayerId) {
                top = PLAYER_AREA_TOP;
                height = PLAYER_CARD_PILES_AND_SIDE_OF_TABLE_HEIGHT;
            } else {
                top = OPPONENT_AREA_TOP;
                height = OPPONENT_AREA_HEIGHT;
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
            if (Object.hasOwn(this.discardPileGroups, playerId)) {
                this.discardPileGroups[playerId].layoutCards();
            }
        }

        for (let playerId in this.adventureDeckGroups) {
            if (Object.hasOwn(this.adventureDeckGroups, playerId)) {
                this.adventureDeckGroups[playerId].layoutCards();
            }
        }

        for (let playerId in this.removedPileGroups) {
            if (Object.hasOwn(this.removedPileGroups, playerId)) {
                this.removedPileGroups[playerId].layoutCards();
            }
        }

        for (let playerId in this.miscPileGroups) {
            if (Object.hasOwn(this.miscPileGroups, playerId)) {
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
        if (locationDiv == null) {
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
                return "Valo Region";
            // 2E regions
            case "QO_NOS_SYSTEM":
                return "Qo'noS Region";
            default:
                return "";
        }
    }

    updateGameStats(gameState) {
        var that = this;
        $("#main").queue(
            function (next) {
                for (const player of Object.values(gameState.playerMap)) {
                    let playerId = player.playerId;
                    let drawDeckSize = player.cardGroups["DRAW_DECK"].cardCount;
                    let handSize = player.cardGroups["HAND"].cardCount;
                    let discardSize = player.cardGroups["DISCARD"].cardCount;
                    let removedSize = player.cardGroups["REMOVED"].cardCount;
                    let score = player.score;

                    $("#deck" + that.getPlayerIndex(playerId)).text(drawDeckSize);
                    $("#hand" + that.getPlayerIndex(playerId)).text(handSize);
                    $("#discard" + that.getPlayerIndex(playerId)).text(discardSize);
                    $("#removedPile" + that.getPlayerIndex(playerId)).text(removedSize);
                    $("#score" + that.getPlayerIndex(playerId)).text(`SCORE ${score}`);
                }
                next();
            });
    }

    addNonMissionInPlayToClientRecursively(card, gameState) {
        let spacelineIndex = getSpacelineIndexFromLocationId(card.locationId, gameState);
        let attachedToCardId = card.attachedToCardId;
        // Assumes that all missions in play already have a div
        if (attachedToCardId != null && typeof attachedToCardId != "undefined") {
            let attachedToCardDiv = getCardDivFromId(attachedToCardId);
            if (attachedToCardDiv.length == 0) {
                let attachedToCard = gameState.visibleCardsInGame[attachedToCardId];
                this.addNonMissionInPlayToClientRecursively(attachedToCard, gameState);
            }
        }
        this.animations.putNonMissionIntoPlay(card, card.owner, gameState, spacelineIndex, false);
    }

}