import GempClientCommunication from './communication.js';
import { getUrlParam } from './common.js';
import Card from './jCards.js';
import { createCardDiv } from './jCards.js';
import NormalCardGroup from './jCardGroup.js';

export default class GempLotrSoloDraftUI {
    comm;

    topDiv;
    bottomDiv;

    messageDiv;
    picksDiv;
    draftedDiv;

    picksCardGroup;
    draftedCardGroup;

    leagueType;

    constructor() {
        var that = this;

        this.comm = new GempClientCommunication("/gemp-stccg-server", that.processError);

        this.leagueType = getUrlParam("leagueType");

        this.topDiv = $("#topDiv");
        this.bottomDiv = $("#bottomDiv");

        this.messageDiv = $("#messageDiv");
        this.picksDiv = $("#picksDiv");
        this.draftedDiv = $("#draftedDiv");

        this.picksCardGroup = new NormalCardGroup(this.picksDiv, function (card) {
            return true;
        });
        this.picksCardGroup.maxCardHeight = 200;

        this.draftedCardGroup = new NormalCardGroup(this.draftedDiv, function (card) {
            return true;
        });
        this.draftedCardGroup.maxCardHeight = 200;

        this.selectionFunc = this.addCardToDeckAndLayout;

        $("body").click(
                function (event) {
                    return that.clickCardFunction(event);
                });
        $("body").mousedown(
                function (event) {
                    return that.dragStartCardFunction(event);
                });
        $("body").mouseup(
                function (event) {
                    return that.dragStopCardFunction(event);
                });

        var width = $(window).width();
        var height = $(window).height();

        this.infoDialog = $("<div></div>")
                .dialog({
            autoOpen:false,
            closeOnEscape:true,
            resizable:false,
            title:"Card information"
        });

        var swipeOptions = {
            threshold:20,
            swipeUp:function (event) {
                that.infoDialog.prop({ scrollTop:that.infoDialog.prop("scrollHeight") });
                return false;
            },
            swipeDown:function (event) {
                that.infoDialog.prop({ scrollTop:0 });
                return false;
            }
        };
        this.infoDialog.swipe(swipeOptions);

        this.getDraftState();
    }

    getDraftState() {
        var that = this;
        this.comm.getDraft(this.leagueType,
            function (xml) {
                var root = xml.documentElement;
                if (root.tagName == "availablePicks") {
                    var availablePicks = root.getElementsByTagName("availablePick");
                    for (var i = 0; i < availablePicks.length; i++) {
                        var availablePick = availablePicks[i];
                        var id = availablePick.getAttribute("id");
                        var url = availablePick.getAttribute("url");
                        var imageUrl = availablePick.getAttribute("imageUrl");
                        var blueprintId = availablePick.getAttribute("blueprintId");

                        if (blueprintId != null) {
                            var card = new Card(blueprintId, "picks", "deck", "player", imageUrl);
                            var cardDiv = createCardDiv(card.imageUrl, null, card.isFoil(), false, false, card.hasErrata());
                            cardDiv.data("card", card);
                            cardDiv.data("choiceId", id);
                            that.picksDiv.append(cardDiv);
                        } else {
                            var card = new Card("rules", "picks", "deck", "player", url);
                            var cardDiv = createCardDiv(url, null, false, false, true, false);
                            cardDiv.data("card", card);
                            cardDiv.data("choiceId", id);
                            that.picksDiv.append(cardDiv);
                        }
                    }
                    that.picksCardGroup.layoutCards();
                    if (availablePicks.length > 0)
                        that.messageDiv.text("Make a pick");
                    else
                        that.messageDiv.text("Draft is finished");
                }
            });

        let promise = that.comm.getCollectionv2(this.leagueType, "sort:cardType,name", 0, 1000);
        promise.then((xml) => {
            let xmlparser = new DOMParser();
            var xmlDoc = xmlparser.parseFromString(xml, "text/xml");
            var root = xmlDoc.documentElement;
            if (root.tagName == "collection") {
                var cards = root.getElementsByTagName("card");
                for (var i=0; i<cards.length; i++) {
                    var card = cards[i];
                    var count = card.getAttribute("count");
                    var blueprintId = card.getAttribute("blueprintId");
                    var imageUrl = card.getAttribute("imageUrl");
                    for (var no = 0; no < count; no++) {
                        var card = new Card(blueprintId, "drafted", "deck", "player", imageUrl);
                        var cardDiv = createCardDiv(card.imageUrl, null, card.isFoil(), false, false, card.hasErrata());
                        cardDiv.data("card", card);
                        that.draftedDiv.append(cardDiv);
                    }
                }
                that.draftedCardGroup.layoutCards();
            }
        })
        .catch(error => {
            console.error(error);
        });
    }

    clickCardFunction(event) {
        var that = this;

        var tar = $(event.target);
        if (tar.length == 1 && tar[0].tagName == "A")
            return true;

        if (!this.successfulDrag && this.infoDialog.dialog("isOpen")) {
            this.infoDialog.dialog("close");
            event.stopPropagation();
            return false;
        }

        if (tar.hasClass("actionArea")) {
            var selectedCardElem = tar.closest(".card");
            if (event.which == 1) {
                if (!this.successfulDrag) {
                    if (event.shiftKey) {
                        selectedCardElem.data("card").displayCardInfo(that.infoDialog);
                    } else {
                        if (selectedCardElem.data("card").zone == "picks") {
                            var choiceId = selectedCardElem.data("choiceId");
                            that.comm.makeDraftPick(that.leagueType, choiceId, function (xml) {
                                var root = xml.documentElement;
                                if (root.tagName == "pickResult") {
                                    var pickedCards = root.getElementsByTagName("pickedCard");
                                    for (var i = 0; i < pickedCards.length; i++) {
                                        var pickedCard = pickedCards[i];
                                        var blueprintId = pickedCard.getAttribute("blueprintId");
                                        var count = pickedCard.getAttribute("count");
                                        var imageUrl = pickedCard.getAttribute("imageUrl");
                                        for (var no = 0; no < count; no++) {
                                            var card = new Card(blueprintId, "drafted", "deck", "player", imageUrl);
                                            var cardDiv = createCardDiv(card.imageUrl, null, card.isFoil(), false, false, card.hasErrata());
                                            cardDiv.data("card", card);
                                            that.draftedDiv.append(cardDiv);
                                        }
                                    }
                                    that.draftedCardGroup.layoutCards();

                                    var availablePicks = root.getElementsByTagName("availablePick");
                                    for (var i = 0; i < availablePicks.length; i++) {
                                        var availablePick = availablePicks[i];
                                        var id = availablePick.getAttribute("id");
                                        var url = availablePick.getAttribute("url");
                                        var blueprintId = availablePick.getAttribute("blueprintId");
                                        var imageUrl = availablePick.getAttribute("imageUrl");

                                        if (blueprintId != null) {
                                            var card = new Card(blueprintId, "picks", "deck", "player", imageUrl);
                                            var cardDiv = createCardDiv(card.imageUrl, null, card.isFoil(), false, false, card.hasErrata());
                                            cardDiv.data("card", card);
                                            cardDiv.data("choiceId", id);
                                            that.picksDiv.append(cardDiv);
                                        } else {
                                            var card = new Card("rules", "picks", "deck", "player", url);
                                            var cardDiv = createCardDiv(url, null, false, false, true, false);
                                            cardDiv.data("card", card);
                                            cardDiv.data("choiceId", id);
                                            that.picksDiv.append(cardDiv);
                                        }
                                    }
                                    that.picksCardGroup.layoutCards();
                                    if (availablePicks.length > 0)
                                        that.messageDiv.text("Make a pick");
                                    else
                                        that.messageDiv.text("Draft is finished");
                                }
                            });
                            $(".card", that.picksDiv).remove();
                        }
                    }

                    event.stopPropagation();
                }
            }
            return false;
        }
        return true;
    }

    dragCardData;
    dragStartX;
    dragStartY;
    successfulDrag;

    dragStartCardFunction(event) {
        this.successfulDrag = false;
        var tar = $(event.target);
        if (tar.hasClass("actionArea")) {
            var selectedCardElem = tar.closest(".card");
            if (event.which == 1) {
                this.dragCardData = selectedCardElem.data("card");
                this.dragStartX = event.clientX;
                this.dragStartY = event.clientY;
                return false;
            }
        }
        return true;
    }

    dragStopCardFunction(event) {
        that = this;
        if (this.dragCardData != null) {
            if (this.dragStartY - event.clientY >= 20) {
                this.dragCardData.displayCardInfo(that.infoDialog);
                this.successfulDrag = true;
            }
            this.dragCardData = null;
            this.dragStartX = null;
            this.dragStartY = null;
            return false;
        }
        return true;
    }

    layoutUI(layoutDivs) {
        if (layoutDivs) {
            var messageHeight = 40;
            var padding = 5;

            var topWidth = this.topDiv.width();
            var topHeight = this.topDiv.height();

            var bottomWidth = this.bottomDiv.width();
            var bottomHeight = this.bottomDiv.height();

            this.picksDiv.css({position:"absolute", left:padding, top:messageHeight+padding, width:topWidth-padding*2, height:topHeight-messageHeight-padding*2});
            this.picksCardGroup.setBounds(0, 0, topWidth-padding*2, topHeight-messageHeight-padding*2);

            this.draftedDiv.css({position:"absolute", left:padding, top:padding, width:bottomWidth-padding*2, height:bottomHeight-padding*2});
            this.draftedCardGroup.setBounds(0, 0, bottomWidth-padding*2, bottomHeight-padding*2);
        } else {
            this.picksCardGroup.layoutCards();
            this.draftedCardGroup.layoutCards();
        }
    }

    processError(xhr, ajaxOptions, thrownError) {
        if (thrownError != "abort")
            alert("There was a problem during communication with server");
    }
}
