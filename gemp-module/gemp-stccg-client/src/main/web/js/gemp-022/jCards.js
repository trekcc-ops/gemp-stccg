import { zones_all } from "./common.js";
import { fetchImage } from "./communication.js";
import Cookies from "js-cookie";
import special01Img from "../../images/boosters/special-01.png";
import rulesImg from "../../images/rules.png";
import errataVerticalImg from "../../images/errata-vertical.png";
import pixelImg from "../../images/pixel.png";
import cardBackImg from "../../images/decipher_card_back.svg?url";
import stoppedImg from "../../images/emblem-error.svg?url";
import disabledImg from "../../images/emblem-locked.svg?url";
import capturedImg from "../../images/emblem-symbolic-link.svg?url";

export var cardCache = {};
export var cardScale = 357 / 497;

export var packBlueprints = {
    "Special-01": special01Img
};

// TODO: corresponds to the list of tokens we have icons for, not the full 1E list
export const STATUS_TOKENS = [
    "STOPPED"
];

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
    title;
    siteNumber = 1;
    attachedCards;
    errata;
    status_tokens;

    constructor(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown, tokens) {
        if (typeof(blueprintId) != 'string') {
            throw new TypeError(`blueprintId '${blueprintId}' must be a string.`);
        }

        if (zones_all.indexOf(zone) === -1) {
            throw new TypeError(`zone '${zone}' is not in the zones_all array.`);
        }

        if (typeof(cardId) != 'string' && !(Number.isInteger(cardId))) {
            throw new TypeError(`cardId '${cardId}' must be a string or integer.`);
        }

        if (typeof(owner) != 'string') {
            throw new TypeError(`owner '${owner}' must be a string.`);
        }

        if (typeof(title) != 'string') {
            throw new TypeError(`title '${title}' must be a string.`);
        }

        if (typeof(imageUrl) != 'string') {
            throw new TypeError(`imageUrl '${imageUrl}' must be a string.`);
        }

        if (typeof(locationIndex) != 'string' && !(Number.isInteger(locationIndex))) {
            throw new TypeError(`locationIndex '${locationIndex}' must be a string or integer.`);
        }
        else if (locationIndex === '') {
            // empty string is allowed but needs to return a number
            this.locationIndex = -1;
        }
        else {
            // is a string, try to parse it
            let parsedLocationIndex = parseInt(locationIndex);
            if(Number.isNaN(parsedLocationIndex)) {
                throw new TypeError(`locationIndex '${locationIndex}' string is not parseable into an integer.`);
            }

            this.locationIndex = parsedLocationIndex;
        }

        if (typeof(upsideDown) != 'boolean') {
            throw new TypeError(`upsideDown '${upsideDown}' must be a boolean.`);
        }

        this.status_tokens = (tokens instanceof Set) ? tokens : new Set();

        this.blueprintId = blueprintId;
        this.title = title;
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
        this.cardId = cardId.toString();
        this.owner = owner;
        
        this.attachedCards = new Array();
        if (imageBlueprint == "rules") {
            this.imageUrl = rulesImg;
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
        // no set match
        if (Object.hasOwn(this.remadeErratas, "" + setNo) === false) {
            return null;
        }

        // no card match
        let set_cards = this.remadeErratas["" + setNo];
        if (set_cards.indexOf(cardNo) === -1) {
            return null;
        }

        // match found
        return "/gemp-module/images/erratas/LOTR" + this.formatCardNo(setNo, cardNo) + ".jpg";
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
        let cardDiv = createFullCardDiv(that.imageUrl, that.foil, that.horizontal, that.isPack(), that.title);
        let jqCardDiv = $(cardDiv);
        container.append(jqCardDiv);
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

    addStatusToken(status) {
        if (STATUS_TOKENS.indexOf(status) === -1) {
            throw new TypeError(`Status '${status}' is not in the STATUS_TOKENS array.`);
        }
        else {
            this.status_tokens.add(status);
        }
    }

    removeStatusToken(status) {
        this.status_tokens.delete(status);
    }

    hasStatus(status) {
        return this.status_tokens.has(status);
    }
}

// TODO: This should be an instance function.
export function createCardDiv(image, text, foil, tokens, noBorder, errata, upsideDown, cardId) {
    let baseCardDiv = document.createElement("div");
    baseCardDiv.classList.add("card");
    if (cardId != null) {
        baseCardDiv.id = cardId.toString(); // coerce to string just in case
    }

    let threeDScene = document.createElement("div");
    threeDScene.classList.add("three-d-card-scene");

    let threeDCardObject = document.createElement("div");
    threeDCardObject.classList.add("three-d-card");

    // Card back
    let back_face = document.createElement("div");
    back_face.classList.add("card__face", "card__face--back");

    let back_img = document.createElement("img");
    back_img.src = cardBackImg;
    back_img.style.width = "100%";
    back_img.style.height = "100%";
    back_face.appendChild(back_img);

    threeDCardObject.appendChild(back_face);

    // Card front with everything else
    let front_face = document.createElement("div");
    front_face.classList.add("card__face", "card__face--front");

    let imageTag = document.createElement("img");
    imageTag.classList.add("card_img");

    if (cardId != null) {
        let img_class_id = "card_img_" + cardId;
        imageTag.classList.add(img_class_id);
    }

    if (upsideDown) {
        imageTag.classList.add("upside-down");
    }

    let loadingSpinner = document.createElement("div");
    loadingSpinner.classList.add("card-load-spinner");
    front_face.appendChild(loadingSpinner);

    fetchImage(image).then((url) => {
        front_face.removeChild(loadingSpinner);
        if (url == null) {
            imageTag.src = "";
        }
        else {
            imageTag.src = url;
        }
    });
    
    imageTag.style.width = "100%";
    imageTag.style.height = "100%";
    imageTag.alt = (text) ? text : "";

    front_face.appendChild(imageTag);

    if (errata) {
        let errataDiv = document.createElement("div");
        errataDiv.classList.add("errataOverlay");

        let errataImageTag = document.createElement("img");
        errataImageTag.src = errataVerticalImg;
        errataImageTag.style.width = "100%";
        errataImageTag.style.height = "100%";

        errataDiv.appendChild(errataImageTag);
        front_face.appendChild(errataDiv);
    }

    let foilPresentation = getFoilPresentation();
    if (foil && foilPresentation !== 'none') {
        let foilDiv = document.createElement("div");
        foilDiv.classList.add("foilOverlay");

        let foilImageSrc = (foilPresentation === 'animated') ? "foil.gif" : "holo.jpg";
        let foilImageTag = document.createElement("img");
        foilImageTag.src = `gemp-module/images/${foilImageSrc}`;
        foilImageTag.style.width = "100%";
        foilImageTag.style.height = "100%";

        foilDiv.appendChild(foilImageTag);
        front_face.appendChild(foilDiv);
    }

    if (tokens) {
        let overlayDiv = document.createElement("div");
        overlayDiv.classList.add("tokenOverlay");

        if (tokens instanceof Set) { // handle back-compat case where tokens could be bool true
            for (const status of tokens) {
                switch(status) {
                    case "STOPPED": {
                        let stoppedImgTag = document.createElement("img");
                        stoppedImgTag.src = stoppedImg;
                        overlayDiv.appendChild(stoppedImgTag);
                        break;
                    }
                    case "STASIS": {
                        let disabledImgTag = document.createElement("img");
                        disabledImgTag.src = disabledImg;
                        overlayDiv.appendChild(disabledImgTag);
                        break;
                    }
                    case "CAPTURED": {
                        let capturedImgTag = document.createElement("img");
                        capturedImgTag.src = capturedImg;
                        overlayDiv.appendChild(capturedImgTag);
                        break;
                    }
                    default:
                        break;
                }
            }
        }

        front_face.appendChild(overlayDiv);
    }

    let borderDiv = document.createElement("div");
    borderDiv.classList.add("borderOverlay");
    if (noBorder) {
        borderDiv.classList.add("noBorder");
    }
    
    let borderImageTag = document.createElement("img");
    borderImageTag.classList.add("actionArea");
    borderImageTag.src = pixelImg;
    borderImageTag.style.width = "100%";
    borderImageTag.style.height = "100%";

    borderDiv.appendChild(borderImageTag);
    front_face.appendChild(borderDiv);

    //threeDScene.appendChild(baseCardDiv);
    threeDCardObject.appendChild(front_face);
    threeDScene.appendChild(threeDCardObject);
    baseCardDiv.appendChild(threeDScene);
    
    return baseCardDiv;
}

export function getFoilPresentation() {
    if (Cookies.get("foilPresentation") === "true") {
        return "animated";
    }
    return "static";
}

export function createFullCardDiv(image, foil, horizontal, noBorder, text) {
    let alt = (text) ? text : "";
    let tokens = false;
    let errata = false;
    let upsideDown = false;
    let cardId;
    let fullCardDiv = createCardDiv(image, alt, foil, tokens, noBorder, errata, upsideDown, cardId);
    if (horizontal) {
        fullCardDiv.classList.add("fullCardDivHorizontal");
    }
    else {
        fullCardDiv.classList.add("fullCardDivVertical");
    }

    return fullCardDiv;
}

export function createSimpleCardDiv(image, alt_text="") {
    let cardDiv = document.createElement("div");
    cardDiv.classList.add("card");

    let imageElem = document.createElement("img");
    imageElem.src = image;
    imageElem.alt = alt_text;
    imageElem.width = "100%";
    imageElem.height = "100%";

    cardDiv.appendChild(imageElem);

    let jqCardDiv = $(cardDiv);

    return jqCardDiv;
}

// TODO: Work on replacing this with standard DOM ID lookups.
export function getCardDivFromId(cardId) {
    // This depends on the $.expr[':'].cardId variable set in gameUi.js
    return $(".card:cardId(" + cardId + ")");
}