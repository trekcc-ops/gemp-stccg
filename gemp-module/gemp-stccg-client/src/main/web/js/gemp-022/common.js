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
    return Math.floor(price / 100) + "<img src='images/gold.png'/> " + ((silver < 10) ? ("0" + silver) : silver) + "<img src='images/silver.png'/>";
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