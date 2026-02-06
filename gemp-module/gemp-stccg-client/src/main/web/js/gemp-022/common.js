import goldImg from '../../images/gold.png';
import silverImg from '../../images/silver.png';
import { createTheme } from '@mui/material/styles';
import bajAffilIcon from "../../images/icons/affiliations/1E-BAJ.gif";
import borgAffilIcon from "../../images/icons/affiliations/1E-BORG.gif";
import cardAffilIcon from "../../images/icons/affiliations/1E-CARD.gif";
import domAffilIcon from "../../images/icons/affiliations/1E-DOM.gif";
import fedAffilIcon from "../../images/icons/affiliations/1E-FED.gif";
import ferAffilIcon from "../../images/icons/affiliations/1E-FER.gif";
import hirAffilIcon from "../../images/icons/affiliations/1E-HIR.gif";
import kazAffilIcon from "../../images/icons/affiliations/1E-KAZ.gif";
import klgAffilIcon from "../../images/icons/affiliations/1E-KLG.gif";
import neuAffilIcon from "../../images/icons/affiliations/1E-NEU.gif";
import nonAffilIcon from "../../images/icons/affiliations/1E-NON.gif";
import romAffilIcon from "../../images/icons/affiliations/1E-ROM.gif";
import stfAffilIcon from "../../images/icons/affiliations/1E-STF.gif";
import vidAffilIcon from "../../images/icons/affiliations/1E-VID.gif";
import vulAffilIcon from "../../images/icons/affiliations/1E-VUL.gif";
import xinAffilIcon from "../../images/icons/affiliations/1E-XIN.png";

export const softwareName = "Velara"; // See TNG s01e18 "Home Soil"
export const versionNumber = "0.0.1";
export const versionDescription = "Pre-alpha";
export const userAgent = `${softwareName}/${versionNumber} (${versionDescription})`;

export const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      light: '#c4caea',
      main: '#596ac2',
      dark: '#2b3ea1',
      contrastText: '#fff',
    },
    secondary: {
      light: '#f8f2cb',
      main: '#eadd79',
      dark: '#c2b159',
      contrastText: '#000',
    },
  },
});


export var monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

export var serverDomain = "";

export function formatToTwoDigits(no) {
    if (no < 10) {
        return "0" + no;
    }
    else {
        return no;
    }
}

export function formatDate(date) {
    return monthNames[date.getMonth()] + " " + date.getDate() + " " + formatToTwoDigits(date.getHours()) + ":" + formatToTwoDigits(date.getMinutes()) + ":" + formatToTwoDigits(date.getSeconds());
}

export function formatPrice(price) {
    var silver = (price % 100);
    return Math.floor(price / 100) + `<img src='${goldImg}'/>` + ((silver < 10) ? ("0" + silver) : silver) + `<img src='${silverImg}'/>`;
}

export function getDateString(date) {
    return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
}

export function getUrlParam(param) {
    let urlparams = new URLSearchParams(document.location.search);
    return urlparams.get(param);
}

export function getMapSize(map) {
    let size = 0;
    let key;
    for (key in map) {
        if (Object.hasOwn(map, key)) {
            size++;
        }
    }
    return size;
}

export function replaceIncludes($) {
    let includes = $('[data-include]');
    $.each(includes, function () {
        let file = `includes/${$(this).data('include')}.html`;
        $(this).load(file);
        //alert( "Loaded " + file );
    })

}

export function log(text) {
    if (getUrlParam("log") === "true") {
        console.log(text);
    }
}

export function openSizeDialog(dialog) {
    let dialogsSized = new Array();
    let sizedDialog = function () {
        for (let i = 0; i < dialogsSized.length; i++) {
            if (dialogsSized[i] === dialog) {
                return true;
            }
        }
        return false;
    };

    if (!sizedDialog(dialog)) {
        let windowWidth = $(window).width();
        let windowHeight = $(window).height();

        let dialogWidth = windowWidth * 0.8;
        let dialogHeight = windowHeight * 0.8;

        dialogsSized.push(dialog);
        dialog.dialog({width:dialogWidth, height:dialogHeight});
    }
    dialog.dialog("open");
}

// All possible zones, per stccg/common/filterable/Zone.java
export const zones_all = [
    "DRAW_DECK",
    "MISSIONS_PILE",
    "SEED_DECK",
    "CORE",
    "SPACELINE",
    "AT_LOCATION",
    "ATTACHED",
    "PLACED_ON_MISSION",
    "REMOVED",
    "PLAY_PILE",
    "HAND",
    "DISCARD",
    "VOID",
    "SPECIAL", // dialog boxes


    // deck builder sub decks not represented in game UI
    "MISSIONS",
    "SITES",
    "QS_TENT",
    "QS_TENT_REFEREE",
    "DILEMMA",
    "Q_FLASH",
    "BATTLE_BRIDGE",
    "TRIBBLE"
]

export function showLinkableCardTitle(cardNode) {
    // Takes a json node of card properties and creates a hyperlink that the user can click on to show the card
    let title = cardNode.title; // string
    let hasUniversalIcon = cardNode.hasUniversalIcon; // boolean
    let blueprintId = cardNode.blueprintId; // string
    let imageUrl = cardNode.imageUrl; // string

    let html = "";
    html = html + `<div class='cardHint' value='${blueprintId}' card_img_url='${imageUrl}' data-title='${title}'>`;
    if (hasUniversalIcon) {
        html = html + "&#x2756&nbsp;"; // unicode for universal symbol
    }
    html = html + title + "</div>";

    return html;
}

export function showLinkableCardTitles(cardNodeArray) {
    let message = "";
    for (let i = 0; i < cardNodeArray.length; i++) {
        if (i > 0) {
            message = message + ", ";
        }
        message = message + showLinkableCardTitle(cardNodeArray[i]);
    }
    return message;
}

export function getAffiliationIcon(affiliationEnum) {
    // Receives the server enum name for an affiliation and provides the TrekCC icon URL
    switch(affiliationEnum) {
        case "BAJORAN":
            return bajAffilIcon;
        case "BORG":
            return borgAffilIcon;
        case "CARDASSIAN":
            return cardAffilIcon;
        case "DOMINION":
            return domAffilIcon;
        case "FEDERATION":
            return fedAffilIcon;
        case "FERENGI":
            return ferAffilIcon;
        case "HIROGEN":
            return hirAffilIcon;
        case "KAZON":
            return kazAffilIcon;
        case "KLINGON":
            return klgAffilIcon;
        case "NEUTRAL":
            return neuAffilIcon;
        case "NON_ALIGNED":
            return nonAffilIcon;
        case "ROMULAN":
            return romAffilIcon;
        case "STARFLEET":
            return stfAffilIcon;
        case "VIDIIAN":
            return vidAffilIcon;
        case "VULCAN":
            return vulAffilIcon;
        case "XINDI":
            return xinAffilIcon;
        default:
            console.error("Cannot recognize affiliation " + affiliationEnum);
            return "";
    }
}

export function getAffiliationName(affiliationEnum) {
    // Receives the server enum name for an affiliation and provides a user-friendly string
    switch(affiliationEnum) {
        case "BAJORAN":
        case "BORG":
        case "CARDASSIAN":
        case "DOMINION":
        case "FEDERATION":
        case "FERENGI":
        case "HIROGEN":
        case "KAZON":
        case "KLINGON":
        case "NEUTRAL":
        case "ROMULAN":
        case "STARFLEET":
        case "VIDIIAN":
        case "VULCAN":
        case "XINDI":
            return affiliationEnum.charAt(0).toUpperCase() + affiliationEnum.substring(1).toLowerCase();
        case "NON_ALIGNED":
            return "Non-Aligned";
        default:
            console.error("Cannot recognize affiliation " + affiliationEnum);
            return "";
    }
}

export function getAffiliationHtml(affiliationEnum) {
    // Receives the server enum name for an affiliation and provides an in-line icon
    let iconURL = getAffiliationIcon(affiliationEnum);
    let userFriendlyName = getAffiliationName(affiliationEnum);

    let affiliationImg = document.createElement("img");
    affiliationImg.src = iconURL;
    affiliationImg.classList.add("inline-icon");
    affiliationImg.title = userFriendlyName;
    return affiliationImg;
}

export function getFriendlyPhaseName(phaseEnum) {
    switch(phaseEnum) {
        case "BETWEEN_TURNS":
            return "Between turns";
        case "CARD_PLAY":
            return "Card play";
        case "END_OF_TURN":
            return "End of turn";
        case "EXECUTE_ORDERS":
            return "Execute orders";
        case "SEED_DILEMMA":
            return "Dilemma seed phase";
        case "SEED_DOORWAY":
            return "Doorway seed phase";
        case "SEED_FACILITY":
            return "Facility seed phase";
        case "SEED_MISSION":
            return "Mission seed phase";
        case "START_OF_TURN":
            return "Start of turn";
        case "TRIBBLES_TURN":
            return "Player turn";
        default:
            console.error("Cannot recognize phase " + phaseEnum);
            return "";
    }
}

export function removeFromArray(arr, value) {
  var index = arr.indexOf(value);
  if (index > -1) {
    arr.splice(index, 1);
  }
  return arr;
}