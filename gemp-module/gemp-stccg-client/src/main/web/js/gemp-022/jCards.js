export var cardCache = {};
export var cardScale = 357 / 497;

export var packBlueprints = {
    "Special-01": "/gemp-module/images/boosters/special-01.png"
};

export default class Card {
    blueprintId;
    foil;
    tengwar;
    hasWiki;
    horizontal;
    locationIndex;
    zone;
    cardId;
    owner;
    siteNumber = 1;
    attachedCards;
    errata;

    constructor(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown) {
        this.blueprintId = blueprintId;
        this.imageUrl = imageUrl;
        this.upsideDown = upsideDown;


        var imageBlueprint = blueprintId;
        var len = imageBlueprint.length;
        this.foil = imageBlueprint.substring(len - 1, len) == "*";
        if (this.foil)
            imageBlueprint = imageBlueprint.substring(0, len - 1);

        var bareBlueprint = imageBlueprint;
        len = bareBlueprint.length;
        this.tengwar = bareBlueprint.substring(len - 1, len) == "T";
        if (this.tengwar)
            bareBlueprint = bareBlueprint.substring(0, len - 1);

        this.hasWiki = packBlueprints[imageBlueprint] == null;

        this.zone = zone;
        this.cardId = cardId;
        this.owner = owner;
        if (locationIndex !== undefined) {
            this.locationIndex = parseInt(locationIndex);
        }
        this.attachedCards = new Array();
        if (imageBlueprint == "rules") {
            this.imageUrl = "/gemp-module/images/rules.png";
        } else {
            if (cardCache[imageBlueprint] != null) {
                var cardFromCache = cardCache[imageBlueprint];
                this.horizontal = cardFromCache.horizontal;
//                this.imageUrl = cardFromCache.imageUrl;
                this.errata = cardFromCache.errata;
            } else {
//                this.imageUrl = this.getUrlByBlueprintId(bareBlueprint);
                this.horizontal = this.isHorizontal(bareBlueprint);

                var separator = bareBlueprint.indexOf("_");
                var setNo = parseInt(bareBlueprint.substr(0, separator));
                var cardNo = parseInt(bareBlueprint.substr(separator + 1));

                this.errata = this.getErrata(setNo, cardNo) != null;
                cardCache[imageBlueprint] = {
                    imageUrl: this.imageUrl,
                    horizontal: this.horizontal,
                    errata: this.errata
                };
            }
        }
    }

    isTengwar() {
        return this.tengwar;
    }

    isFoil() {
        return this.foil;
    }

    isUpsideDown() {
        return this.upsideDown;
    }

    hasErrata() {
        var separator = this.blueprintId.indexOf("_");
        var setNo = parseInt(this.blueprintId.substr(0, separator));
        
        if(setNo >= 50 && setNo <= 89)
            return true;
        
        return this.errata;
    }

    isPack() {
        return packBlueprints[this.blueprintId] != null;
    }

    isHorizontal(blueprintId) {
        return false;
    }

    getUrlByBlueprintId(blueprintId, ignoreErrata) {
        if (packBlueprints[blueprintId] != null)
            return packBlueprints[blueprintId];

        var separator = blueprintId.indexOf("_");
        var setNo = parseInt(blueprintId.substr(0, separator));
        var cardNo = parseInt(blueprintId.substr(separator + 1));

        var errata = this.getErrata(setNo, cardNo);
        if (errata != null && (ignoreErrata === undefined || !ignoreErrata))
            return errata;

        var mainLocation = this.getMainLocation(setNo, cardNo);

        var cardStr;

        if (this.isMasterworks(setNo, cardNo))
            cardStr = this.formatSetNo(setNo) + "O0" + (cardNo - this.getMasterworksOffset(setNo));
        else
            cardStr = this.formatCardNo(setNo, cardNo);

        return mainLocation + "LOTR" + cardStr + (this.isTengwar() ? "T" : "") + ".jpg";
    }

    getWikiLink() {
        var imageUrl = this.getUrlByBlueprintId(this.blueprintId, true);
        var afterLastSlash = imageUrl.lastIndexOf("/") + 1;
        var countAfterLastSlash = imageUrl.length - 4 - afterLastSlash;
        return "http://wiki.lotrtcgpc.net/wiki/" + imageUrl.substr(afterLastSlash, countAfterLastSlash);
    }

    hasWikiInfo() {
        return this.hasWiki;
    }

    formatSetNo(setNo) {
        var setNoStr;
        if (setNo < 10)
            setNoStr = "0" + setNo;
        else
            setNoStr = setNo;
        return setNoStr;
    }

    formatCardNo(setNo, cardNo) {
        var setNoStr = this.formatSetNo(setNo);

        var cardStr;
        if (cardNo < 10)
            cardStr = setNoStr + "00" + cardNo;
        else if (cardNo < 100)
            cardStr = setNoStr + "0" + cardNo;
        else
            cardStr = setNoStr + "" + cardNo;

        return cardStr;
    }

    getMainLocation(setNo, cardNo) {
        return "https://i.lotrtcgpc.net/decipher/";
    }

    getMasterworksOffset(setNo) {
        if (setNo == 17)
            return 148;
        if (setNo == 18)
            return 140;
        return 194;
    }

    isMasterworks(setNo, cardNo) {
        if (setNo == 12)
            return cardNo > 194;
        if (setNo == 13)
            return cardNo > 194;
        if (setNo == 15)
            return cardNo > 194 && cardNo < 204;
        if (setNo == 17)
            return cardNo > 148;
        if (setNo == 18)
            return cardNo > 140;
        return false;
    }

    remadeErratas = {
        "0": [7],
        "1": [3, 12, 43, 46, 55, 109, 113, 138, 162, 211, 235, 263, 309, 318, 331, 338, 343, 360],
        "3": [48, 110],
        "4": [63, 236, 237, 352],
        "6": [39, 46, 85],
        "7": [10, 14, 66, 114, 133, 134, 135, 182, 284, 285, 289, 302, 357],
        "8": [20, 33, 69],
        "17": [15, 87, 96, 118],
        "18": [8, 12, 20, 25, 35, 48, 50, 55, 77, 78, 79, 80, 82, 94, 97, 133]
    }

    getErrata(setNo, cardNo) {
        if (this.remadeErratas["" + setNo] != null && $.inArray(cardNo, this.remadeErratas["" + setNo]) != -1)
            return "/gemp-module/images/erratas/LOTR" + this.formatCardNo(setNo, cardNo) + ".jpg";
        return null;
    }

    getHeightForColumnWidth(columnWidth) {
        if (this.horizontal)
            return columnWidth;
        else
            return Math.floor(columnWidth / cardScale);
    }

    getHeightForWidth(width) {
        if (this.horizontal)
            return Math.floor(width * cardScale);
        else
            return Math.floor(width / cardScale);
    }

    getWidthForHeight(height) {
        if (this.horizontal)
            return Math.floor(height / cardScale);
        else
            return Math.floor(height * cardScale);
    }

    getWidthForMaxDimension(maxDimension) {
        if (this.horizontal)
            return maxDimension;
        else
            return Math.floor(maxDimension * cardScale);
    }

    displayCardInfo(container) {
        let that = this;
        container.html("");
        container.html("<div style='scroll: auto'></div>");
        container.append(createFullCardDiv(that.imageUrl, that.foil, that.horizontal, that.isPack()));
//        if (that.hasWikiInfo())
//            container.append("<div><a href='" + that.getWikiLink() + "' target='_blank'>Wiki</a></div>");

        var horSpace = 30;
        var vertSpace = 65;
        var dialogWidth;
        var dialogHeight;

        if (that.horizontal) {
            dialogWidth = 500 + horSpace;
            dialogHeight = 360 + vertSpace;
        } else {
            dialogWidth = 360 + horSpace;
            dialogHeight = 500 + vertSpace;
        }

        container.dialog(
            {width:Math.min(dialogWidth, $(window).width()), height:Math.min(dialogHeight, $(window).height())}
        );
        container.dialog("open");
    }
}


export function createCardDiv(image, text, foil, tokens, noBorder, errata) {
    return createCardDiv2(image, text, foil, tokens, noBorder, errata, false);
}

export function createCardDiv2(image, text, foil, tokens, noBorder, errata, upsideDown, cardId) {
    if (cardId == null) {
        if (upsideDown)
            var imgClass = "card_img upside-down";
        else
            var imgClass = "card_img";
    } else {
        if (upsideDown)
            var imgClass = "card_img upside-down card_img_" + cardId;
        else
            var imgClass = "card_img card_img_" + cardId;
    }


    var cardDiv = $("<div class='card'><img class='" + imgClass + "' src='" + image + "' width='100%' height='100%'>" + ((text != null) ? text : "") + "</div>");

    if (errata) {
        var errataDiv = $("<div class='errataOverlay'><img src='/gemp-module/images/errata-vertical.png' width='100%' height='100%'></div>");
        cardDiv.append(errataDiv);
    }

    var foilPresentation = getFoilPresentation();

    if (foil && foilPresentation !== 'none') {
        var foilImage = (foilPresentation === 'animated') ? "foil.gif" : "holo.jpg";
        var foilDiv = $("<div class='foilOverlay'><img src='/gemp-module/images/" + foilImage + "' width='100%' height='100%'></div>");
        cardDiv.append(foilDiv);
    }

    if (tokens === undefined || tokens) {
        var overlayDiv = $("<div class='tokenOverlay'></div>");
        cardDiv.append(overlayDiv);
    }
    var borderDiv = $("<div class='borderOverlay'><img class='actionArea' src='/gemp-module/images/pixel.png' width='100%' height='100%'></div>");
    if (noBorder)
        borderDiv.addClass("noBorder");
    cardDiv.append(borderDiv);

    return cardDiv;
}

export function getFoilPresentation() {
    var result = $.cookie("foilPresentation");
    if (result === null)
        result = "static";
    if (result === "true")
        result = "animated";
    if (result === "false")
        result = "static";
    return result;
}

export function createFullCardDiv(image, foil, horizontal, noBorder) {
    var orientation;
    if (horizontal) orientation = "Horizontal";
    else orientation = "Vertical";

    if (noBorder) var borderClass = "noBorderOverlay";
    else var borderClass = "borderOverlay";

    var cardDiv = $("<div class='fullCardDiv" + orientation + "'></div>");
    cardDiv.append($("<div class='fullCardWrapper'>" +
        "<img class='fullCardImg" + orientation + "' src='" + image + "'></div>"));
    cardDiv.append($("<div class='" + borderClass + orientation + "'>" +
        "<img class='actionArea' src='/gemp-module/images/pixel.png' width='100%' height='100%'></div>"));

    if (foil && getFoilPresentation() !== 'none') {
        var foilImage = (getFoilPresentation() === 'animated') ? "foil.gif" : "holo.jpg";
        cardDiv.append($("div class='foilOverlay" + orientation + "'>" +
            "<img src='/gemp-module/images/" + foilImage + "' width='100%' height='100%'></div>"));
    }

    return cardDiv;
}

export function createSimpleCardDiv(image) {
    var cardDiv = $("<div class='card'><img src='" + image + "' width='100%' height='100%'></div>");

    return cardDiv;
}

export function getCardDivFromId(cardId) {
    return $(".card:cardId(" + cardId + ")");
}