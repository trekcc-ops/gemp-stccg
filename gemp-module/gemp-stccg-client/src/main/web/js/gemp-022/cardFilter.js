import GempClientCommunication from "./communication.js";
import { log } from "./common.js";

export default class CardFilter {
    clearCollectionFunc;
    addCardFunc;
    finishCollectionFunc;
    getCollectionFunc;

    filter = "";
    start = 0;
    count = 18;

    collectionDiv;

    previousPageBut;
    nextPageBut;
    countSlider;

    setSelect = $("#setSelect");
    nameInput = $("#nameInput");
    sortSelect = $("#sortSelect");
    raritySelect;
    format;
    comm;

    constructor(pageElem, getCollectionFunc, clearCollectionFunc, addCardFunc, finishCollectionFunc, format) {
        var that = this;
        this.getCollectionFunc = getCollectionFunc;
        this.clearCollectionFunc = clearCollectionFunc;
        this.addCardFunc = addCardFunc;
        this.finishCollectionFunc = finishCollectionFunc;
        this.format = format;
        this.comm = new GempClientCommunication("/gemp-stccg-server", that.processError);

        this.buildUi(pageElem);
        this.updateSetOptions();
    }

    enableDetailFilters(enable) {
        $("#affiliation-buttons").buttonset("option", "disabled", !enable);
        $("#cardType").prop("disabled", !enable);
        $("#keyword").prop("disabled", !enable);
        $("#race").prop("disabled", !enable);
        $("#itemClass").prop("disabled", !enable);
        $("#tribblePower").prop("disabled", !enable);
        $("#phase").prop("disabled", !enable);
    }

    setFilter(filter) {
        this.filter = filter;

        this.start = 0;
        this.getCollection();
    }

    setFormat(format) {
        this.format = format;
    }

    setType(typeValue) {
        $("#type").val(typeValue);
    }

    updateSetOptions() {
        var that = this;
        var currentSet = that.setSelect.val();

        this.comm.getSets(that.format,
            function (json)
            {
                that.setSelect.empty();
                $(json).each(function (index, o) {
                    if (o.code == "disabled") {
                        that.setSelect.append("<option disabled>----------</option>")
                    } else {
                        var $option = $("<option/>")
                            .attr("value", o.code)
                            .text(o.name);
                        that.setSelect.append($option);
                    }
                });

                that.setSelect.val(currentSet);
            },
            {
                "400":function ()
                {
                    alert("Could not retrieve sets.");
                }
            });
    }

    buildUi(pageElem) {
        var that = this;

        this.previousPageBut = $("#previousPage").button({
            text: false,
            icons: {
                primary: "ui-icon-circle-triangle-w"
            },
            disabled: true
        }).click(
            function () {
                that.disableNavigation();
                that.start -= that.count;
                that.getCollection();
            });

        this.nextPageBut = $("#nextPage").button({
            text: false,
            icons: {
                primary: "ui-icon-circle-triangle-e"
            },
            disabled: true
        }).click(
            function () {
                that.disableNavigation();
                that.start += that.count;
                that.getCollection();
            });

        this.countSlider = $("#countSlider").slider({
            value: 18,
            min: 4,
            max: 40,
            step: 1,
            disabled: true,
            slide: function (event, ui) {
                that.start = 0;
                that.count = ui.value;
                that.getCollection();
            }
        });

        $("#affiliation-buttons").buttonset();
/*        this.raritySelect = $("<select style='width: 80px; font-size: 80%;'>"
            + "<option value=''>All Rarities</option>"
            + "<option value='R'>Rare</option>"
            + "<option value='U'>Uncommon</option>"
            + "<option value='C'>Common</option>"
            + "<option value='A'>Alternate Image</option>"
            + "<option value='P'>Promo</option>"
            + "<option value='X'>Rare+</option>"
            + "<option value='S'>Fixed</option>"
            + "<option value='C,U,P,S'>Poorman's</option>"
            + "<option value='V'>Virtual</option>"
            + "</select>"); */


        var setFilterChanged = function () {
            that.filter = that.calculateNormalFilter();
            that.start = 0
            that.getCollection();
            return true;
        };

        var fullFilterChanged = function () {
            that.start = 0;
            that.getCollection();
            return true;
        };

        this.setSelect.change(setFilterChanged);
        this.nameInput.change(fullFilterChanged);
        this.sortSelect.change(fullFilterChanged);
//        this.raritySelect.change(fullFilterChanged);

        var filterOut = function () {
            that.filter = that.calculateNormalFilter();
            that.start = 0;
            that.getCollection();
            return true;
        };
        
        //Hide dynamic filters by default
        $("#race").hide();
        $("#itemClass").hide();
        $("#phase").hide();
        
        var changeDynamicFilters = function () {
            var cardType = $("#cardType option:selected").prop("value");
            if (cardType.includes("COMPANION") || cardType.includes("ALLY") || cardType.includes("MINION")) {
                $("#race").show();
                $("#itemClass").hide();
                $("#itemClass").val("");
                $("#phase").hide();
                $("#phase").val("");
            } else if (cardType.includes("POSSESSION") || cardType.includes("ARTIFACT")) {
                $("#race").hide();
                $("#race").val("");
                $("#itemClass").show();
                $("#phase").hide();
                $("#phase").val("");
            } else if (cardType.includes("EVENT")) {
                $("#race").hide();
                $("#race").val("");
                $("#itemClass").hide();
                $("#itemClass").val("");
                $("#phase").show();
            } else {
                $("#race").hide();
                $("#race").val("");
                $("#itemClass").hide();
                $("#itemClass").val("");
                $("#phase").hide();
                $("#phase").val("")
            }
            that.filter = that.calculateNormalFilter();
            that.start = 0;
            that.getCollection();
            return true;
            
        };

        $("#cardType").change(changeDynamicFilters);
        $("#keyword").change(filterOut);
        $("#type").change(filterOut);
        $("#race").change(filterOut);
        $("#itemClass").change(filterOut);
        $("#phase").change(filterOut);
        $(".affiliationFilter").click(filterOut);

/*        $("#labelDWARVEN,#labelELVEN,#labelGANDALF,#labelGONDOR,#labelROHAN,#labelSHIRE,#labelGOLLUM,#labelDUNLAND,#labelISENGARD,#labelMEN,#labelMORIA,#labelORC,#labelRAIDER,#labelSAURON,#labelURUK_HAI,#labelWRAITH").click(filterOut);
        //Additional Hobbit Draft labels
        $("#labelESGAROTH,#labelGUNDABAD,#labelMIRKWOOD,#labelSMAUG,#labelSPIDER,#labelTROLL").click(filterOut); */
        
        this.collectionDiv = $("#collection-display");
        //collection-display
        pageElem.append(this.collectionDiv);
    }

    layoutUi(x, y, width, height) {
        //$("#filter-main").css({position: "absolute", left: x, top: y, width: width, height: 34});
        //this.countSlider.css({width: width - 100});
        //$("#filter-inputs").css({position: "absolute", left: x, top: y + 34, width: width, height: 34});
        //this.filterDiv.css({position: "absolute", left: x, top: y + 68, width: width, height: 80});
    }

    layoutPageUi(x, y, width) {
        //$("#filter-main").css({left: x, top: y, width: width, height: 36});
        //this.countSlider.css({width: width - 100});
    }

    disableNavigation() {
        this.previousPageBut.button("option", "disabled", true);
        this.nextPageBut.button("option", "disabled", true);
        this.countSlider.button("option", "disabled", true);
    }

    calculateNormalFilter() {
        var normalFilterArray = new Array("cardType", "affiliation", "keyword", "type", "race", "itemClass", "phase");
        var filterString = "";

        for (var i = 0; i < normalFilterArray.length; i++) {
            if (normalFilterArray[i] == "affiliation") {
                var affiliations = new Array();
                $("label", $("#affiliation-buttons")).each(
                    function () {
                        if ($(this).hasClass("ui-state-active"))
                            affiliations.push($(this).prop("id").substring(5));
                    });
                if (affiliations.length > 0)
                    filterString = filterString + "|affiliation:" + affiliations;
            } else {
                var filterResult = $("#" + normalFilterArray[i] + " option:selected").prop("value");
                if (filterResult != "")
                    filterString = filterString + "|" + normalFilterArray[i] + ":" + filterResult;
            }
        }
        return filterString;
    }

    calculateFullFilterPostfix() {
        var filterString = "";

        var setNo = $("option:selected", this.setSelect).prop("value");
        if (setNo != "")
            filterString = filterString + "|set:" + setNo;

        var sort = $("option:selected", this.sortSelect).prop("value");
        if (sort != "")
            filterString = filterString + "|sort:" + sort;

/*        var cardName = this.nameInput.val();
        var cardNameElems = cardName.split(" ");
        cardName = "";
        for (var i = 0; i < cardNameElems.length; i++)
            filterString = filterString + "|name:" + cardNameElems[i];*/

        var cardNameRegexp = /[^\s"]+|"([^"]*)"/gi;
        var cardName = this.nameInput.val();
        var cardNameArray = [];
        do {
            var match = cardNameRegexp.exec(cardName);
            if (match != null)
            {
                cardNameArray.push(match[1] ? match[1] : match[0]);
            }
        } while (match != null);

        for (var i = 0; i < cardNameArray.length; i++)
            filterString = filterString + "|name:" + cardNameArray[i];


//        var rarity = $("option:selected", this.raritySelect).prop("value");
//        if (rarity != "")
//            filterString = filterString + "|rarity:" + rarity;

        return filterString;
    }

    getCollection() {
        var that = this;
        this.getCollectionFunc((this.filter + this.calculateFullFilterPostfix()).trim(), this.start, this.count, function (xml) {
            that.displayCollection(xml);
        });
    }

    displayCollection(xml) {
        log(xml);
        var root = xml.documentElement;

        this.clearCollectionFunc(root);

        var packs = root.getElementsByTagName("pack");
        for (var i = 0; i < packs.length; i++) {
            var packElem = packs[i];
            var blueprintId = packElem.getAttribute("blueprintId");
            var count = packElem.getAttribute("count");
            this.addCardFunc(packElem, "pack", blueprintId, count);
        }

        var cards = root.getElementsByTagName("card");
        for (var i = 0; i < cards.length; i++) {
            var cardElem = cards[i];
            var blueprintId = cardElem.getAttribute("blueprintId");
            var count = cardElem.getAttribute("count");
            var imageUrl = cardElem.getAttribute("imageUrl");
            this.addCardFunc(cardElem, "card", blueprintId, count, imageUrl);
        }

        this.finishCollectionFunc();

        $("#previousPage").button("option", "disabled", this.start == 0);
        var cnt = parseInt(root.getAttribute("count"));
        $("#nextPage").button("option", "disabled", (this.start + this.count) >= cnt);
        $("#countSlider").slider("option", "disabled", false);
    }
}
