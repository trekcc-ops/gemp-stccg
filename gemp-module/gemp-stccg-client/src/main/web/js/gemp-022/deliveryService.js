import { openSizeDialog } from "./common.js";
import NormalCardGroup from './jCardGroup.js';
import { createCardDiv } from "./jCards.js";

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

            var packs = collection.getElementsByTagName("pack");
            for (var j = 0; j < packs.length; j++) {
                var packElem = packs[j];
                var blueprintId = packElem.getAttribute("blueprintId");
                var count = packElem.getAttribute("count");
                var imageUrl = packElem.getAttribute("imageUrl");
                var card = new Card(blueprintId, "delivery", "deliveryPack" + i, "player", imageUrl);
                card.tokens = {"count":count};
                var cardDiv = createCardDiv(card.imageUrl, null, card.isFoil(), true, true, false);
                cardDiv.data("card", card);
                deliveryDialogs[collectionName].append(cardDiv);
            }

            var cards = collection.getElementsByTagName("card");
            for (var j = 0; j < cards.length; j++) {
                var cardElem = cards[j];
                var blueprintId = cardElem.getAttribute("blueprintId");
                var count = cardElem.getAttribute("count");
                var imageUrl = cardElem.getAttribute("imageUrl");
                var card = new Card(blueprintId, "delivery", "deliveryCard" + i, "player", imageUrl);
                card.tokens = {"count":count};
                var cardDiv = createCardDiv(card.imageUrl, null, card.isFoil(), true, false, card.hasErrata());
                cardDiv.data("card", card);
                deliveryDialogs[collectionName].append(cardDiv);
            }

            openSizeDialog(deliveryDialogs[collectionName]);
            deliveryDialogResize();
        }
    }
}