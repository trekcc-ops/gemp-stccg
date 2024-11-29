import GempClientCommunication from "./communication.js";
import { log, getUrlParam } from "./common.js";

export default class CardFilter {
    clearCollectionFunc;
    addCardFunc;
    finishCollectionFunc;

    collectionType = "default";
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
    tribblePowerSelect = document.getElementById("tribblePower");
    raritySelect;
    format;
    comm;

    constructor(pageElem, collectionType, clearCollectionFunc, addCardFunc, finishCollectionFunc, format) {
        var that = this;
        this.collectionType = collectionType;
        this.clearCollectionFunc = clearCollectionFunc;
        this.addCardFunc = addCardFunc;
        this.finishCollectionFunc = finishCollectionFunc;
        this.format = format;
        this.comm = new GempClientCommunication("/gemp-stccg-server", that.processError);

        this.buildUi(pageElem);
        // BUG: This call is async so uhhhhh creating a new CardFilter probably has a race condition.
        this.updateSetOptions();
    }

    setCollectionType(collectionType) {
        this.collectionType = collectionType;
        this.start = 0;
    }

    setFilter(filter) {
        this.filter = filter;
        this.start = 0;
    }

    setFormat(format) {
        this.format = format;

        // Change search URL
        if ((this.format === "debug1e") ||
            (this.format === "st1emoderncomplete")) {
            
            let anchor = document.getElementById("card-search-url");
            if (anchor) {
                anchor.setAttribute("href", "https://www.trekcc.org/1e/?mode=search");
            }
        }
        else if (this.format === "st2e") {
            let anchor = document.getElementById("card-search-url");
            if (anchor) {
                anchor.setAttribute("href", "https://www.trekcc.org/2e/?mode=search");
            }
        }
        else {
            let anchor = document.getElementById("card-search-url");
            if (anchor) {
                anchor.setAttribute("href", "https://www.trekcc.org/");
            }
        }

        // show/hide Tribble Power selector
        if (this.format === "tribbles") {
            if (this.tribblePowerSelect) {
                this.tribblePowerSelect.classList.remove("hidden");
            }
        }
        else {
            if (this.tribblePowerSelect) {
                this.tribblePowerSelect.classList.add("hidden");
            }
        }
    }

    setType(typeValue) {
        $("#type").val(typeValue);
    }

    async updateSetOptions() {
        let promise = this.comm.getSets(this.format);
        return promise.then((json) => {
            this.setSelect.empty();
            let that = this;
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
            return this.setSelect;
        })
        .catch(error => {
            // NOTE: Because comm.getSets() has the response.json() call that catches all possible
            //       "return value is not JSON" errors, and because JQuery tends to error silently,
            //       this line is not testable right now.
            //       Skip it by telling babel-instanbul to ignore it.
            /* istanbul ignore next */
            console.error(error);
        });
    }

    async setFilterChanged() {
        this.filter = this.calculateNormalFilter();
        this.start = 0
        await this.getCollection();
        return true;
    };

    async fullFilterChanged() {
        this.start = 0;
        await this.getCollection();
        return true;
    };

    async filterOut() {
        this.filter = this.calculateNormalFilter();
        this.start = 0;
        await this.getCollection();
        return true;
    };

    async changeDynamicFilters() {
        var cardType = $("#cardType option:selected").prop("value");
        this.filter = this.calculateNormalFilter();
        this.start = 0;
        await this.getCollection();
        return true;
    };

    buildUi(pageElem) {
        var that = this;

        this.previousPageBut = $("#previousPage").button({
            text: false,
            icons: {
                primary: "ui-icon-circle-triangle-w"
            },
            disabled: true
        }).click(
            async function () {
                that.disableNavigation();
                that.start -= that.count;
                await that.getCollection();
            });

        this.nextPageBut = $("#nextPage").button({
            text: false,
            icons: {
                primary: "ui-icon-circle-triangle-e"
            },
            disabled: true
        }).click(
            async function () {
                that.disableNavigation();
                that.start += that.count;
                await that.getCollection();
            });

        this.countSlider = $("#countSlider").slider({
            value: 18,
            min: 4,
            max: 40,
            step: 1,
            disabled: true,
            slide: async function (event, ui) {
                that.start = 0;
                that.count = ui.value;
                await that.getCollection();
            }
        });

        $("#affiliation-buttons").controlgroup();
        $(".affiliationFilter").checkboxradio("option", "icon", false);
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
        
        if (this.tribblePowerSelect) {
            this.tribblePowerSelect.classList.add("hidden");
        }

        this.setSelect.on("change", async () => {await this.setFilterChanged()});
        this.nameInput.on("change", async () => {await this.fullFilterChanged()});
        this.sortSelect.on("change", async () => {await this.fullFilterChanged()});
//        this.raritySelect.on("change", async () => {await this.fullFilterChanged()});

        $("#cardType").on("change", async () => {await this.changeDynamicFilters()});
        $("#type").on("change", async () => {await this.filterOut()});
        $(".affiliationFilter").on("click", async () => {await this.filterOut()});
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
    }

    calculateNormalFilter() {
        var normalFilterArray = new Array("cardType", "affiliation", "type");
        var filterString = "";

        for (var i = 0; i < normalFilterArray.length; i++) {
            if (normalFilterArray[i] == "affiliation") {
                var affiliations = new Array();
                $('.affiliationFilter').each(
                    function (_index, element) {
                        if (element.checked) {
                            affiliations.push(element.id);
                        }
                    }
                );
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

    async getCollection() {
        let promise = this.comm.getCollection(
            this.collectionType,
            getUrlParam("participantId"),
            (this.filter + this.calculateFullFilterPostfix()).trim(),
            this.start,
            this.count
        );
        promise.then((xml) => {
            // convert incoming string to an XML DOM document, since
            // that's what displayCollection expects
            let xmlparser = new DOMParser();
            var xmlDoc = xmlparser.parseFromString(xml, "text/xml");
            this.displayCollection(xmlDoc);
        })
        .catch(error => {
            console.error(error);
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
