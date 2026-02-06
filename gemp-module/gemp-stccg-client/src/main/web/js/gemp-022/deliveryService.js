import { openSizeDialog } from "./common.js";
import NormalCardGroup from './jCardGroup.js';
import { createCardDiv } from "./jCards.js";
import Card from "./jCards.js";

export default function deliveryService(xml) {
    var deliveryDialogs = {};
    var deliveryGroups = {};

    console.log("Delivered a package:");
    console.log(xml);

    var root = xml.documentElement;
    if (root.tagName == "delivery") {
        var collections = root.getElementsByTagName("collectionType");
        for (var i = 0; i < collections.length; i++) {
            var collection = collections[i];

            var collectionName = collection.getAttribute("name");
            var deliveryDialogResize = (function (name) {
                return function () {
                    var width = deliveryDialogs[name].width() + 10;
                    var height = deliveryDialogs[name].height() + 10;
                    deliveryGroups[name].setBounds(2, 2, width - 2 * 2, height - 2 * 2);
                };
            })(collectionName);

            if (deliveryDialogs[collectionName] == null) {
                deliveryDialogs[collectionName] = $("<div></div>").dialog({
                    title:"New items - " + collectionName,
                    autoOpen:false,
                    closeOnEscape:true,
                    resizable:true,
                    width:400,
                    height:200,
                    closeText: ''
                });

                deliveryGroups[collectionName] = new NormalCardGroup(deliveryDialogs[collectionName], function (card) {
                    return true;
                }, false);

                deliveryDialogs[collectionName].bind("dialogresize", deliveryDialogResize);
                deliveryDialogs[collectionName].bind("dialogclose",
                    function () {
                        deliveryDialogs[collectionName].html("");
                    });
            }

            let packs = collection.getElementsByTagName("pack");
            for (let j = 0; j < packs.length; j++) {
                let packElem = packs[j];
                let blueprintId = packElem.getAttribute("blueprintId");
                let count = packElem.getAttribute("count");
                let title=""; // Do packs have titles? Are we keeping packs?
                let imageUrl = packElem.getAttribute("imageUrl");
                let emptyLocationIndex = "";
                let upsideDown = false;
                let card = new Card(blueprintId, "delivery", "deliveryPack" + i, "player", title, imageUrl, emptyLocationIndex, upsideDown);
                card.tokens = {"count":count};
                let baseCardDiv = createCardDiv(card.imageUrl, null, card.isFoil(), true, true, false);
                let cardDiv = $(baseCardDiv); // convert to jQuery object
                cardDiv.data("card", card);
                deliveryDialogs[collectionName].append(cardDiv);
            }

            let cards = collection.getElementsByTagName("card");
            for (let j = 0; j < cards.length; j++) {
                let cardElem = cards[j];
                let blueprintId = cardElem.getAttribute("blueprintId");
                let count = cardElem.getAttribute("count");
                let title= cardElem.getAttribute("title");
                let imageUrl = cardElem.getAttribute("imageUrl");
                let emptyLocationIndex = "";
                let upsideDown = false;
                let card = new Card(blueprintId, "delivery", "deliveryCard" + i, "player", title, imageUrl, emptyLocationIndex, upsideDown);
                card.tokens = {"count":count};
                let baseCardDiv = createCardDiv(card.imageUrl, null, card.isFoil(), true, false, card.hasErrata());
                let cardDiv = $(baseCardDiv); // convert to jQuery object
                cardDiv.data("card", card);
                deliveryDialogs[collectionName].append(cardDiv);
            }

            openSizeDialog(deliveryDialogs[collectionName]);
            deliveryDialogResize();
        }
    }
}