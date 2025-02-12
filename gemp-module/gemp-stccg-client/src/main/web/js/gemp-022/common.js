import goldImg from '../../images/gold.png';
import silverImg from '../../images/silver.png';

import { createTheme } from '@mui/material/styles';

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
    if (no < 10)
        return "0" + no;
    else
        return no;
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
    var size = 0, key;
    for (key in map)
        if (map.hasOwnProperty(key)) size++;
    return size;
}

export function replaceIncludes($) {
    
    var includes = $('[data-include]');
    $.each(includes, function () {
        var file = 'includes/' + $(this).data('include') + '.html'
        $(this).load(file)
        //alert( "Loaded " + file );
    })

}

export function log(text) {
    if (getUrlParam("log") == "true")
        console.log(text);
}

export function openSizeDialog(dialog) {
    var dialogsSized = new Array();
    var sizedDialog = function () {
        for (var i = 0; i < dialogsSized.length; i++)
            if (dialogsSized[i] == dialog)
                return true;
        return false;
    };

    if (!sizedDialog(dialog)) {
        var windowWidth = $(window).width();
        var windowHeight = $(window).height();

        var dialogWidth = windowWidth * 0.8;
        var dialogHeight = windowHeight * 0.8;

        dialogsSized.push(dialog);
        dialog.dialog({width:dialogWidth, height:dialogHeight});
    }
    dialog.dialog("open");
}

// All possible zones, per stccg/common/filterable/Zone.java
export var zones_all = [
    "DRAW_DECK",
    "MISSIONS_PILE",
    "SEED_DECK",
    "CORE",
    "SPACELINE",
    "AT_LOCATION",
    "ATTACHED",
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
    html = html + "<div class='cardHint' value='" + blueprintId + "' card_img_url='" + imageUrl + "'>";
    if (hasUniversalIcon) {
        html = html + "&#x2756&nbsp;"; // unicode for universal symbol
    }
    html = html + title + "</div>";

    return html;
}