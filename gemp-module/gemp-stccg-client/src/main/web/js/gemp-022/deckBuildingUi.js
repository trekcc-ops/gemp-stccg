import "../../js/jquery/jquery-3.7.1.js";
import "../../js/jquery/jquery-ui-1.14.1/jquery-ui.js";
import GempClientCommunication from "./communication.js";
import Card from "./jCards.js";
import { createCardDiv } from "./jCards.js";
import CardFilter from "./cardFilter.js";
import { NormalCardGroup, layoutTokens } from "./jCardGroup.js";
import { openSizeDialog } from "./common.js";

export default class GempLotrDeckBuildingUI {
    comm;
    notes = "";
    selectionFunc;
    start = 0;
    count = 18
    filter;
    deckName;
    filterDirty = false;
    deckValidationDirty = true;
    deckContentsDirty = true;
    checkDirtyInterval = 500;
    deckListDialog;
    selectionDialog;
    selectionGroup;
    packSelectionId;
    deckImportDialog;
    notesDialog;
    collectionType;
    formatSelect;

    constructor() {
        var that = this;

        this.comm = new GempClientCommunication("/gemp-stccg-server", that.processError);

        this.collectionType = this.getCollectionType();

        this.deckDiv = $("#deckDiv");
        this.manageDecksDiv = $("#manageDecks");
        this.formatSelect = $("#formatSelect");


        this.cardFilter = new CardFilter(
                $("#collectionDiv"),
                this.collectionType,
                function () {
                    that.clearCollection();
                },
                function (elem, type, blueprintId, count, imageUrl, title) {
                    that.addCardToCollection(
                        type,
                        blueprintId,
                        count,
                        elem.getAttribute("contents"), // Only sent with packs?
                        imageUrl,
                        title
                    );
                },
                function () {
                    that.finishCollection();
                },
                that.formatSelect.val());

        $("#formatSelect").change(
                async function () {
                    that.deckModified(true);
                    that.cardFilter.setFormat(that.formatSelect.val());
                    await that.cardFilter.updateSetOptions();
                    await that.cardFilter.setFilterChanged();
                });

        var newDeckBut = $("#newDeckBut").button({icon: "ui-icon-document", label: "New"});
        var saveDeckBut = $("#saveDeckBut").button({icon: "ui-icon-disk", label: "Save"});
        var renameDeckBut = $("#renameDeckBut").button({icon: "ui-icon-pencil", label: "Rename"});
        var copyDeckBut = $("#copyDeckBut").button({icon: "ui-icon-copy", label: "Copy"});
        var importDeckBut = $("#importDeckBut").button({icon: "ui-icon-arrowthickstop-1-s", label: "Import"});
        var libraryListBut = $("#libraryListBut").button({icon: "ui-icon-bookmark", label: "Library"});
        var deckListBut = $("#deckListBut").button({icon: "ui-icon-suitcase", label: "My decks"});
        var notesBut = $("#notesBut").button({icon: "ui-icon-plus", label: "Notes"});

        this.deckNameSpan = ("#editingDeck");

        newDeckBut.on("click",
                function () {
                    that.deckName = null;
                    let label = document.getElementById("editingDeck");
                    label.textContent = "New deck";
                    that.clearDeck();
                });

        saveDeckBut.on("click",
                function () {
                    if (that.deckName == null) {
                        let newDeckName = prompt("Enter the name of the deck", "");
                        if (that.validateDeckName(newDeckName)) {
                            that.deckName = newDeckName;
                            let label = document.getElementById("editingDeck");
                            label.textContent = newDeckName;
                            that.saveDeck(true);
                        }
                    } else {
                        that.saveDeck(false);
                    }
                });

        renameDeckBut.on("click",
                function () {
                    if (that.deckName == null) {
                        alert("You can't rename this deck, since it's not named (saved) yet.");
                        return;
                    }
                    that.renameCurrentDeck();
                });

        copyDeckBut.on("click",
                function () {
                    that.deckName = null;
                    let label = document.getElementById("editingDeck");
                    label.textContent = "New deck";
                });

        importDeckBut.on("click",
                function () {
                    that.deckName = null;
                    that.importDecklist();
                });

        deckListBut.on("click", function () { that.loadDeckList(); });
        libraryListBut.on("click", function () { that.loadLibraryList(); });
        notesBut.on("click", function () { that.editNotes(); });

        this.collectionDiv = $("#collectionDiv");

        this.collectionDiv.droppable({
            accept: function(d) {
                return (d.hasClass("cardInDeck"));
            },
            drop: function(event, ui) {
                that.removeCardFromDeck($(ui.draggable).closest(".card"));
            }
        });
        this.normalCollectionDiv = $("#collection-display");
        this.normalCollectionGroup = new NormalCardGroup(this.normalCollectionDiv, function (card) {
            return true;
        });
        this.normalCollectionGroup.maxCardHeight = 200;

        this.statsDiv = $("#statsDiv");

        this.selectionFunc = this.addCardToDeckAndLayout;

        this.infoDialog = $("#cardInfoDiv")
                .dialog({
            autoOpen:false,
            closeOnEscape:true,
            resizable:false,
            title:"Card information"
        });

        $("body").on("click",
                function (event) {
                    return that.clickCardFunction(event);
                });
        $("body")[0].addEventListener("contextmenu",
            function (event) {
                if(!that.clickCardFunction(event))
                {
                    event.preventDefault();
                    return false;
                }
                return true;
            });

        let width = $(window).width();
        let height = $(window).height();

        this.cardFilter.setFilter("");
        this.cardFilter.getCollection();


        setInterval(() => {
            if (that.deckValidationDirty) {
                that.deckValidationDirty = false;
                that.updateDeckStats();
            }
        }, this.checkDirtyInterval);
        
        this.updateFormatOptions();
    }
    
    renameCurrentDeck() {
        var that = this;
        that.renameDeck(that.deckName, function (newDeckName) {
            
            if (that.deckContentsDirty && confirm("Do you wish to save this deck?"))
            {
                that.saveDeck(false);
            }
            that.deckName = newDeckName;
            that.deckModified(that.deckContentsDirty);
        });
    }
    
    renameDeck(oldName, callback) {
        var that = this;
        
        let newDeckName = prompt("Enter new name for the deck", oldName);
        if (that.validateDeckName(newDeckName)) {

            that.comm.renameDeck(oldName, newDeckName, () => callback(newDeckName),
                {
                    "404":function () {
                        alert("Couldn't find the deck to rename on the server.");
                    }
                });
        }
    }

    getCollectionType() {
        // This was previously the value of the "collection select" drop-down, which was removed since we
        //   do not plan to implement a card pack unlock/purchase mechanism.
        // Other options were "permanent" and "trophy", in case we decide to bring it back or need to test.
        return "default";
    }
    
    importDecklist() {
        var that = this;
        if (that.deckImportDialog == null) {
            that.deckImportDialog = $('<div></div>').dialog({
                closeOnEscape:true,
                resizable:true,
                title:"Import deck"
            });
        }
        that.deckImportDialog.html("");
        let deckImport = $("<textarea rows='5' cols='30' id='deckImport' decklist='decklist'></textarea>");
        let getDecklistTextBut = $("<button title='Import'>Import</button>").button();

        let importDialogDiv = $("<div></div>");
        importDialogDiv.append(deckImport);
        importDialogDiv.append(getDecklistTextBut);
        that.deckImportDialog.append(importDialogDiv);

        getDecklistTextBut.on("click",
             function () {
                let decklist = $('textarea[decklist="decklist"]').val()
                that.parseDecklist(decklist);
            }
        );
        that.deckImportDialog.dialog("open");
    }
    
    parseDecklist(rawText) {
        this.clearDeck();
        var that = this;
        let rawTextList = rawText.split("\n");
        let formattedText = "";
        for (let i = 0; i < rawTextList.length; i++) {
            if (rawTextList[i] != "") {
                let line = that.removeNotes(rawTextList[i]).toLowerCase();
                line = line.replace(/[\*•]/g,"").replace(/’/g,"'");
                formattedText = formattedText + line.trim() + "~";
            }
        }
                
        this.importDeckCollection(formattedText, function (json) {

            that.addAllCardsToDeck(json);
            that.deckModified(true);
            that.layoutDeck();
            let label = document.getElementById("editingDeck");
            label.textContent = "Imported Deck (unsaved)";
        });
    }

    removeNotes(line) {
        let processedLine = line;
        let hasNotes = false;
        let start = line.indexOf("(");
        let end = line.indexOf(")", start);
        if (start < 0 && end < 0) {
            start = line.indexOf("[");
            end = line.indexOf("]", start);
        }
        if (start > 0) {
            processedLine = line.slice(0,start)
            if (end > 0) {
                processedLine = processedLine + line.slice(end+1);
            }
        }
        else if (end > 0) {
            processedLine = line.slice(end+1);
        }
        if (processedLine.indexOf("(") > -1 || processedLine.indexOf(")") > -1 ||
            processedLine.indexOf("[") > -1 || processedLine.indexOf("]") > -1) {
                return this.removeNotes(processedLine);
            }
        return processedLine;
    }

    importDeckCollection(decklist, callback) {
        this.comm.importCollection(decklist, function (json) {
            callback(json);
        }, {
            "414":function () {
                alert("Deck too large to import.");
            }
        });
    }
    
    editNotes() {
        var that = this;
        that.notesDialog = $('<div class="notesDialog"></div>')
            .dialog({
                title:"Edit Deck Notes",
                autoOpen:false,
                closeOnEscape:true,
                resizable:true,
                width:700,
                height:400,
                modal:true
            });
            
        let notesElem = $("<textarea class='notesText'></textarea>");
            
        notesElem.val(that.notes);
        that.notesDialog.append(notesElem);
        
        notesElem.change(function() {
            that.notes = notesElem.val();
            that.deckModified(true);
        });
        
        that.notesDialog.dialog("open");
    }

    loadDeckList() {
        var that = this;
        this.comm.listUserDecks(function (json) {
            if (that.deckListDialog == null) {
                that.deckListDialog = $("<div></div>")
                        .dialog({
                    title:"Your Saved Decks",
                    autoOpen:false,
                    closeOnEscape:true,
                    resizable:true,
                    width:700,
                    height:400,
                    modal:true
                });
            }
            that.deckListDialog.html("");
            
            function formatDeckName(formatName, deckName)
            {
                return "<b>[" + formatName + "]</b> - " + deckName;
            }

            let deckNames = [];
            for (let i = 0; i < json.length; i++) {
                let deck = json[i];
                let deckName = deck.deckName;
                deckNames[i] = deckName;
                let formatName = deck.targetFormat.formatName;
                let openDeckBut = $("<button title='Open deck'><span class='ui-icon ui-icon-folder-open'></span></button>").button();
                let renameDeckBut = $("<button title='Rename deck'><span class='ui-icon ui-icon-pencil'></span></button>").button();
                let deckListBut = $("<button title='Share deck list'><span class='ui-icon ui-icon-extlink'></span></button>").button();
                let deleteDeckBut = $("<button title='Delete deck'><span class='ui-icon ui-icon-trash'></span></button>").button();

                let deckElem = $("<div class='deckItem'></div>");
                deckElem.append(openDeckBut);
                deckElem.append(renameDeckBut);
                deckElem.append(deckListBut);
                deckElem.append(deleteDeckBut);
                let deckNameDiv = $("<span/>").html(formatDeckName(formatName, deckName));
                deckElem.append(deckNameDiv);

                that.deckListDialog.append(deckElem);

                openDeckBut.on("click",
                        (function (i) {
                            return function () {
                                that.setupDeck(json[i]);
                            };
                        })(i));


                deckListBut.on("click",
                        (function (i) {
                            return function () {
                                that.showLibraryDeckInHtml(json[i]);
                            };
                        })(i));

                renameDeckBut.on("click",
                        (function (i, formatName, deckNameDiv) {
                            return function () {
                                that.renameDeck(deckNames[i], function (newDeckName) {
                                    deckNameDiv.html(formatDeckName(formatName, newDeckName));

                                    if (that.deckName === deckNames[i])
                                    {
                                        that.deckName = newDeckName;
                                        that.deckModified(that.deckContentsDirty);
                                    }
                                    deckNames[i] = newDeckName;
                                })
                            };
                        })(i, formatName, deckNameDiv));

                deleteDeckBut.on("click",
                        (function (i) {
                            return function () {
                                if (confirm("Are you sure you want to delete this deck?")) {
                                    that.comm.deleteDeck(deckNames[i],
                                            function () {
                                                if (that.deckName === deckNames[i]) {
                                                    that.deckName = null;
                                                    $("#editingDeck").text("New deck");
                                                    that.clearDeck();
                                                }

                                                that.loadDeckList();
                                            });
                                }
                            };
                        })(i));
            }
            that.deckListDialog.dialog("open");
        });
    }
    
    loadLibraryList() {
        var that = this;
        this.comm.listLibraryDecks(function (json) {
            if (that.deckListDialog == null) {
                that.deckListDialog = $("<div></div>")
                        .dialog({
                    title:"Library Decks",
                    autoOpen:false,
                    closeOnEscape:true,
                    resizable:true,
                    width:700,
                    height:400,
                    modal:true
                });
            }
            that.deckListDialog.html("");
            
            function formatDeckName(formatName, deckName)
            {
                return "<b>[" + formatName + "]</b> - " + deckName;
            }

            let deckNames = [];
            for (let i = 0; i < json.length; i++) {
                let deck = json[i];
                let deckName = deck.deckName;
                deckNames[i] = deckName;
                let formatName = deck.targetFormat.formatName;
                let openDeckBut = $("<button title='Open deck'><span class='ui-icon ui-icon-folder-open'></span></button>").button();
                let deckListBut = $("<button title='Deck list'><span class='ui-icon ui-icon-clipboard'></span></button>").button();

                let deckElem = $("<div class='deckItem'></div>");
                deckElem.append(openDeckBut);
                deckElem.append(deckListBut);
                let deckNameDiv = $("<span/>").html(formatDeckName(formatName, deckName));
                deckElem.append(deckNameDiv);

                that.deckListDialog.append(deckElem);

                openDeckBut.on("click",
                        (function (i) {
                            return function () {
                                that.setupDeck(json[i]);
                                that.deckModified(true);
                            };
                        })(i));


                deckListBut.on("click",
                        (function (i) {
                            return function () {
                                that.showLibraryDeckInHtml(json[i]);
                            };
                        })(i));
            }

            that.deckListDialog.dialog("open");
        });
    }

    clickCardFunction(event) {
        var that = this;

        let tar = $(event.target);
        if (tar.length === 1 && tar[0].tagName === "A")
            return true;

        if (this.infoDialog.dialog("isOpen")) {
            this.infoDialog.dialog("close");
            event.stopPropagation();
            return false;
        }

        if (tar.hasClass("actionArea")) {
            var selectedCardElem = tar.closest(".card");
            if (event.which >= 1) {
                if (!this.successfulDrag) {
                    if (event.shiftKey || event.which > 1) {
                        selectedCardElem.data("card").displayCardInfo(that.infoDialog);
                        return false;
                    } else if (selectedCardElem.hasClass("cardInCollection")) {
                        this.selectionFunc(selectedCardElem, selectedCardElem.data("card").imageUrl, "DRAW_DECK");
                        // TODO: Refers to packs in collection
                    } else if (selectedCardElem.hasClass("packInCollection")) {
                        // if (confirm("Would you like to open this pack?")) {
                            this.comm.openPack(this.getCollectionType(), selectedCardElem.data("card").blueprintId, function () {
                                that.cardFilter.getCollection();
                            }, {
                                "404":function () {
                                    alert("You have no pack of this type in your collection.");
                                }
                            });
                        //}
                    } else if (selectedCardElem.hasClass("cardToSelect")) {
                        this.comm.openSelectionPack(this.getCollectionType(), this.packSelectionId,
                                selectedCardElem.data("card").blueprintId, function () {
                            that.cardFilter.getCollection();
                        }, {
                            "404":function () {
                                alert("You have no pack of this type in your collection or that selection is not available for this pack.");
                            }
                        });
                        this.selectionDialog.dialog("close");
                    } else if (selectedCardElem.hasClass("selectionInCollection")) {
                        var selectionDialogResize = function () {
                            let width = that.selectionDialog.width() + 10;
                            let height = that.selectionDialog.height() + 10;
                            that.selectionGroup.setBounds(2, 2, width - 2 * 2, height - 2 * 2);
                        };

                        if (this.selectionDialog == null) {
                            this.selectionDialog = $("<div></div>")
                                    .dialog({
                                title:"Choose one",
                                autoOpen:false,
                                closeOnEscape:true,
                                resizable:true,
                                width:400,
                                height:200,
                                modal:true
                            });

                            this.selectionGroup = new NormalCardGroup(this.selectionDialog, function (card) {
                                return true;
                            }, false);

                            this.selectionDialog.bind("dialogresize", selectionDialogResize);
                        }
                        this.selectionDialog.html("");
                        let cardData = selectedCardElem.data("card");
                        this.packSelectionId = cardData.blueprintId;
                        let selection = selectedCardElem.data("selection");
                        let blueprintIds = selection.split("|");
                        for (let i = 0; i < blueprintIds.length; i++) {
                            let title = (cardData.title) ? cardData.title : "";
                            let emptyImageUrl = "";
                            let emptyLocationIndex = "";
                            let upsideDown = false;
                            let card = new Card(blueprintIds[i], "selection", "selection" + i, "player", title, emptyImageUrl, emptyLocationIndex, upsideDown);
                            let baseCardDiv = createCardDiv(
                                card.imageUrl,
                                card.title, //text
                                card.isFoil(), //foil
                                card.status_tokens, //tokens
                                card.isPack(), //border
                                card.hasErrata(), //errata
                                card.upsideDown, // upside down
                                card.cardId // id
                            );
                            let cardDiv = $(baseCardDiv); // convert to jQuery object
                            cardDiv.data("card", card);
                            cardDiv.addClass("cardToSelect");
                            this.selectionDialog.append(cardDiv);
                        }
                        openSizeDialog(that.selectionDialog);
                        selectionDialogResize();
                    } else if (selectedCardElem.hasClass("cardInDeck")) {
                        this.removeCardFromDeck(selectedCardElem);
                    }
                    event.stopPropagation();
                }
            }
            return false;
        }
        return true;
    }

    saveDeck(reloadList) {
        var that = this;

        let deckContents = this.getDeckContents();
        if (deckContents == null) {
            alert("Cannot save an empty deck.");
        }
        else {
            this.comm.saveDeck(this.deckName, that.formatSelect.val(), this.notes, deckContents, function (json) {
                that.deckModified(false);
                alert("Deck was saved.  Refresh the Game Hall to see it!");
            }, {
                "200":function () {
                    // work around server sending 200 but invalid XML
                    that.deckModified(false);
                    alert("Deck was saved.  Refresh the Game Hall to see it!");
                },
                "400":function () {
                    alert("Invalid deck format.");
                }
            });
        }
    }

    addCardToContainer(blueprintId, title, imageUrl, subDeck, container, tokens) {
        let emptyLocationIndex = "";
        let upsideDown = false;
        let card = new Card(blueprintId, subDeck, "deck", "player", title, imageUrl, emptyLocationIndex, upsideDown);
        let baseCardDiv = createCardDiv(card.imageUrl, card.title, card.isFoil(), tokens, card.isPack(), card.hasErrata());
        let cardDiv = $(baseCardDiv); // convert to jQuery object
        cardDiv.data("card", card);
        container.append(cardDiv);
        return cardDiv;
    }
    
    addCardToDeckAndLayout(cardElem, imageUrl, zone) {
        var that = this;
        let cardData = cardElem.data("card");
        let blueprintId = cardData.blueprintId;
        let title = cardData.title;
        this.addCardToDeck(blueprintId, title, imageUrl, zone);
        that.layoutDeck();
        that.deckModified(true);
        cardData.tokens = {count:(parseInt(cardData.tokens["count"]) + 1)};
        layoutTokens(cardElem);
    }

    deckModified(value) {
        let name = (this.deckName == null) ? "New deck" : this.deckName;
        if (value)
        {
            this.deckValidationDirty = true;
            this.deckContentsDirty = true;
            let label = document.getElementById("editingDeck");
            label.innerHTML = "<font color='orange'>*" + name + " - modified</font>";
        }
        else
        {
            this.deckContentsDirty = false;
            let label = document.getElementById("editingDeck");
            label.textContent = name;
        }
    }

    addCardToDeck(blueprintId, title, imageUrl, subDeck) {
        var that = this;
        let added = false;
        $(".card.cardInDeck", this.drawDeckDiv).each(
                function () {
                    let cardData = $(this).data("card");
                    if (cardData.blueprintId === blueprintId) {
                        let attDiv = that.addCardToContainer(blueprintId, title, imageUrl, "ATTACHED", that.drawDeckDiv, false);
                        cardData.attachedCards.push(attDiv);
                        added = true;
                    }
                });
        if (!added) {
            let div = this.addCardToContainer(blueprintId, title, imageUrl, subDeck, this.drawDeckDiv, false)
            div.addClass("cardInDeck");
            div.draggable({
                helper: "clone",
                opacity: 0.6,
                appendTo: "body"
            });
        }

        this.deckModified(true);
    }

    updateDeckStats() {
        var that = this;
        let deckContents = this.getDeckContents();
        if (deckContents != null && deckContents != "") 
        {
            this.comm.getDeckStats(deckContents, 
                   that.formatSelect.val(),
                    function (html) 
                    {
                        let deckStatsDiv = document.getElementById("deckStats");
                        deckStatsDiv.innerHTML = html;
                    }, 
                    {
                        "400":function () 
                        {
                            alert("Invalid deck for getting stats.");
                        }
                    });
        } else {
            let deckStatsDiv = document.getElementById("deckStats");
            deckStatsDiv.innerHTML = "Deck is empty";
        }
    }
    
    updateFormatOptions() {
        var that = this;
        let currentFormat = that.formatSelect.val();
        
        this.comm.getFormats(false,
            function (json) 
            {
                that.formatSelect.empty();
                //var formats = JSON.parse(json);
                $(json).each(function (index, o) {    
                    let option = $("<option/>")
                        .attr("value", o.code)
                        .text(o.name);
                    that.formatSelect.append(option);
                });
                
                that.formatSelect.val(currentFormat);

            }, 
            {
                "400":function () 
                {
                    alert("Could not retrieve formats.");
                }
            });
    }

    removeCardFromDeck(cardDiv) {
        let cardData = cardDiv.data("card");
        if (cardData.attachedCards.length > 0) {
            cardData.attachedCards[0].remove();
            cardData.attachedCards.splice(0, 1);
        } else {
            cardDiv.remove();
        }
        let cardInCollectionElem = null;
        $(".card", this.normalCollectionDiv).each(
                function () {
                    let tempCardData = $(this).data("card");
                    if (tempCardData.blueprintId === cardData.blueprintId)
                        cardInCollectionElem = $(this);
                });
        if (cardInCollectionElem != null) {
            let cardInCollectionData = cardInCollectionElem.data("card");
            cardInCollectionData.tokens = {count:(parseInt(cardInCollectionData.tokens["count"]) - 1)};
            layoutTokens(cardInCollectionElem);
        }
        this.layoutDeck();
        this.deckModified(true);
    }

    clearDeck() {
        $(".cardInDeck").each(
                function () {
                    let cardData = $(this).data("card");
                    for (let i = 0; i < cardData.attachedCards.length; i++)
                        cardData.attachedCards[i].remove();
                });
        $(".cardInDeck").remove();

        this.layoutUI(false);

        this.deckValidationDirty = true;
    }

    setupDeck(deckJson) {
    // Load a deck into the deck builder based on a Json object received from the server
        this.clearDeck();
        this.deckName = deckJson.deckName;
        let editingDeck = document.getElementById("editingDeck");
        editingDeck.textContent = this.deckName;

        let formatName = deckJson.targetFormat.formatName;
        let formatCode = deckJson.targetFormat.formatCode;
        if (formatCode != null) {
            this.formatSelect.val(formatCode);
        }
        this.notes = deckJson.notes;
        this.addAllCardsToDeck(deckJson);
        this.layoutUI(false);
        this.cardFilter.getCollection();
        this.deckModified(false);
    }

    addAllCardsToDeck(deckJson) {
        for (const key in deckJson.cards) {
            if (Object.prototype.hasOwnProperty.call(deckJson.cards, key)) {
                let subDeck = key;
                let value = deckJson.cards[key];
                for (const card of value) {
                    let blueprintId = card.blueprintId;
                    let imageUrl = card.imageUrl;
                    let count = card.count;
                    let title = card.cardTitle;
                    for (let i = 0; i < count; i++) {
                        this.addCardToDeck(blueprintId, title, imageUrl, subDeck);
                    }
                }
            }
        }
    }

    clearCollection() {
        $(".card", this.normalCollectionDiv).remove();
    }

    addCardToCollection(type, blueprintId, count, contents, imageUrl, title) {
        if (type === "pack") {
            let cardDiv;
            if (blueprintId.substr(0, 3) === "(S)") {
                let emptyLocationIndex = "";
                let title=""; // Do packs have titles? Are we keeping packs?
                let card = new Card(blueprintId, "pack", "collection", "player", title, imageUrl, emptyLocationIndex, false);
                card.tokens = {"count":count};
                let baseCardDiv = createCardDiv(card.imageUrl, null, false, true, true, false);
                cardDiv = $(baseCardDiv); // convert to jQuery object
                cardDiv.data("card", card);
                cardDiv.data("selection", contents);
                cardDiv.addClass("selectionInCollection");
            } else {
                let emptyLocationIndex = "";
                let title=""; // Do packs have titles? Are we keeping packs?
                let card = new Card(blueprintId, "pack", "collection", "player", title, imageUrl, emptyLocationIndex, false);
                card.tokens = {"count":count};
                let baseCardDiv = createCardDiv(card.imageUrl, null, false, true, true, false);
                cardDiv = $(baseCardDiv); // convert to jQuery object
                cardDiv = $(cardDiv); // convert to jQuery object
                cardDiv.data("card", card);
                cardDiv.addClass("packInCollection");
            }
            this.normalCollectionDiv.append(cardDiv);
        } else if (type === "card") {
            let locationIndex = "";
            let upsideDown = false;
            let card = new Card(blueprintId, "VOID", "collection", "player", title, imageUrl, locationIndex, upsideDown);
            let countInDeck = 0;
            $(".card", this.deckDiv).each(
                    function () {
                        let tempCardData = $(this).data("card");
                        if (blueprintId === tempCardData.blueprintId)
                            countInDeck++;
                    });
            card.tokens = {"count":countInDeck};
            let baseCardDiv = createCardDiv(card.imageUrl, card.title, card.isFoil(), true, false, card.hasErrata());
            let cardDiv = $(baseCardDiv); // convert to jQuery object
            cardDiv.data("card", card);
            cardDiv.addClass("cardInCollection");
            cardDiv.draggable({
                helper: "clone",
                opacity: 0.6,
                appendTo: "body"
            });
            this.normalCollectionDiv.append(cardDiv);
        }
    }

    finishCollection() {
        this.normalCollectionGroup.layoutCards();
    }

    processError(xhr, ajaxOptions, thrownError) {
        if (thrownError != "abort")
        {
            alert("There was a problem during communication with server");
            console.log(xhr)
            console.log(ajaxOptions)
            console.log(thrownError)
        }
    }

    validateDeckName(deckName) {
        if (deckName == null) {
            return false;
        } else if (deckName.length < 3 || deckName.length > 100) {
            alert("Deck name length must be between 3 and 100 characters.");
            return false;
        } else {
            return true;
        }
    }
}


export class TribblesDeckBuildingUI extends GempLotrDeckBuildingUI {
    constructor() {
        super();
        let deckBuildingUI = this;
        this.drawDeckDiv = $("#decksRegion");
        this.drawDeckGroup = new NormalCardGroup(this.drawDeckDiv, function (card) {
            return (card.zone === "DRAW_DECK");
        });
        this.drawDeckDiv.droppable({
            accept: function(d) {
                return (d.hasClass("cardInCollection"));
            },
            drop: function(event, ui) {
                deckBuildingUI.selectionFunc(
                    $(ui.draggable).closest(".card"), $(ui.draggable).closest(".card").data("card").imageUrl, "DRAW_DECK"
                );
            }
        });
        this.drawDeckGroup.maxCardHeight = 200;
    }

    layoutUI(layoutDivs) {
        if (layoutDivs) {
            let manageHeight = 23;

            let padding = 5;
            let collectionWidth = this.collectionDiv.width();
            let collectionHeight = this.collectionDiv.height();

            let deckWidth = this.deckDiv.width();
            let deckHeight = this.deckDiv.height() - (manageHeight + padding);

            let rowHeight = Math.floor((deckHeight - 6 * padding) / 5);
            let sitesWidth = Math.floor(1.5 * deckHeight / 5);
            sitesWidth = Math.min(sitesWidth, 250);

            this.manageDecksDiv.css({position:"absolute", left:padding, top:padding, width:deckWidth, height:manageHeight});
            this.drawDeckDiv.css({ position:"absolute", left:padding, top:manageHeight + 2 * padding, width:deckWidth - padding, height:deckHeight - 2 * padding - 50 });
            this.drawDeckGroup.setBounds(0, 0, deckWidth - padding, (deckHeight - 2 * padding - 50));
            this.cardFilter.layoutUi(padding, 0, collectionWidth - padding, 160);
            this.statsDiv.css({ position:"absolute", left:padding * 2 + sitesWidth, top:manageHeight + padding + deckHeight - 50, width:deckWidth - (sitesWidth + padding) - padding, height:70 });
            this.normalCollectionGroup.setBounds(0, 0, collectionWidth - padding * 2, collectionHeight - 160);
        } else {
            this.layoutDeck();
            this.normalCollectionGroup.layoutCards();
        }
        this.cardFilter.getCollection();
    }

    getDeckContents() {
        let result = "";
        result += "DRAW_DECK|";

        let cards = new Array();
        $(".card", this.drawDeckDiv).each(
                function () {
                    cards.push($(this).data("card").blueprintId);
                });
        if (cards.length > 0) {
            result += cards;
        } else {
            result += ",";
        }

        return result;
    }

    layoutDeck() {
        this.drawDeckGroup.layoutCards();
    }
}

export class ST1EDeckBuildingUI extends GempLotrDeckBuildingUI {
    constructor(){
        super();
        let deckBuildingUI = this;
        this.drawDeckDiv = $("#decksRegion");
        this.drawDeckGroup = new NormalCardGroup(this.drawDeckDiv, function (card) {
            return (card.zone === "DRAW_DECK");
        });
        this.drawDeckDiv.droppable({
            accept: function(d) {
                return (d.hasClass("cardInCollection"));
            },
            drop: function(event, ui) {
                deckBuildingUI.selectionFunc(
                    $(ui.draggable).closest(".card"), $(ui.draggable).closest(".card").data("card").imageUrl, "DRAW_DECK"
                );
            }
        });
        this.drawDeckGroup.maxCardHeight = 200;
        this.missionsDiv = $("#missionsDiv");
        this.missionsGroup = new NormalCardGroup(this.missionsDiv, function (card) {
            return (card.zone === "MISSIONS");
        }, true);
        this.missionsDiv.droppable({
            accept: function(d) {
                return (d.hasClass("cardInCollection"));
            },
            drop: function(event, ui) {
                deckBuildingUI.selectionFunc(
                    $(ui.draggable).closest(".card"), $(ui.draggable).closest(".card").data("card").imageUrl, "MISSIONS"
                );
            }
        });

        this.seedDeckDiv = $("#seedDeckDiv");
        this.seedDeckGroup = new NormalCardGroup(this.seedDeckDiv, function (card) {
            return (card.zone === "SEED_DECK");
        });
        this.seedDeckDiv.droppable({
            accept: function(d) {
                return (d.hasClass("cardInCollection"));
            },
            drop: function(event, ui) {
                deckBuildingUI.selectionFunc(
                    $(ui.draggable).closest(".card"), $(ui.draggable).closest(".card").data("card").imageUrl, "SEED_DECK"
                );
            }
        });
    }

    layoutUI(layoutDivs) {
        if (layoutDivs) {
            let padding = 5;
            let manageHeight = 23;
            let statsHeight = 70;
            let statsTop = this.deckDiv.height() - statsHeight - padding;
            let deckDivWidth = this.deckDiv.width();
            let deckDivHeight = this.deckDiv.height();

            this.manageDecksDiv.css({position:"absolute", left:padding, top:padding,
                width:deckDivWidth - padding, height:manageHeight});
            this.statsDiv.css({ position:"absolute", left:padding, top:statsTop,
                width:deckDivWidth - padding, height:statsHeight });

            let collectionWidth = this.collectionDiv.width();
            let collectionHeight = this.collectionDiv.height();
            this.normalCollectionGroup.setBounds(0, 0, collectionWidth - padding * 2, collectionHeight - 160);
            this.cardFilter.layoutUi(padding, 0, collectionWidth - padding, 160);

            let subDeckTop = manageHeight + padding * 2;
            let subDeckBottom = statsTop - padding;
            let subDeckLeft = padding;
            let subDeckRight = deckDivWidth;
            let subDeckWidth = subDeckRight - subDeckLeft;

            let deckRowHeight = (subDeckBottom - subDeckTop - padding) / 2;
            let drawDeckTop = subDeckTop + deckRowHeight + padding;
            let missionsWidth = subDeckWidth * 0.3;
            let seedDeckLeft = missionsWidth + padding * 2;
            let seedDeckWidth = subDeckRight - seedDeckLeft;
            
            this.missionsDiv.css({ position:"absolute", left:padding, top:subDeckTop, width:missionsWidth,
                height:deckRowHeight, "z-index": 9});
            this.missionsGroup.setBounds(0, 0, missionsWidth, deckRowHeight);

            this.seedDeckDiv.css({ position:"absolute", left:seedDeckLeft, top:subDeckTop, width:seedDeckWidth,
                height:deckRowHeight, "z-index": 9});
            this.seedDeckGroup.setBounds(0, 0, seedDeckWidth, deckRowHeight);

            this.drawDeckDiv.css({ position:"absolute", left:padding, top:drawDeckTop, width:subDeckWidth,
                height:deckRowHeight, "z-index": 9 });
            this.drawDeckGroup.setBounds(0, 0, subDeckWidth, deckRowHeight);

        } else {
            this.layoutDeck();
            this.normalCollectionGroup.layoutCards();
        }
    }

    getDeckContents() {
        let result = "";
        let cards = new Array();
        result += "DRAW_DECK|";
        $(".card", this.drawDeckDiv).each(
                function () {
                    cards.push($(this).data("card").blueprintId);
                });
        if (cards.length > 0) {
            result += cards;
        } else {
            result += ",";
        }

        result += "|SEED_DECK|";
        cards = new Array();
        $(".card", this.seedDeckDiv).each(
                function () {
                    cards.push($(this).data("card").blueprintId);
                });
        if (cards.length > 0) {
            result += cards;
        } else {
            result += ",";
        }

        result += "|MISSIONS|";
        cards = new Array();
        $(".card", this.missionsDiv).each(
                function () {
                    cards.push($(this).data("card").blueprintId);
                });
        if (cards.length > 0) {
            result += cards;
        } else {
            result += ",";
        }

        return result;
    }

    layoutDeck() {
        this.missionsGroup.layoutCards();
        this.seedDeckGroup.layoutCards();
        this.drawDeckGroup.layoutCards();
    }

    addCardToDeck(blueprintId, title, imageUrl, subDeck) {
        var that = this;
        let added = false;
        let container = null;
        if (subDeck === "MISSIONS") {
            container = that.missionsDiv;
        } else if (subDeck === "SEED_DECK") {
            container = that.seedDeckDiv;
        } else {
            container = that.drawDeckDiv;
        }
        $(".card.cardInDeck", container).each(
                function () {
                    let cardData = $(this).data("card");
                    if (cardData.blueprintId === blueprintId) {
                        let attDiv = that.addCardToContainer(blueprintId, title, imageUrl, "ATTACHED", container, false);
                        cardData.attachedCards.push(attDiv);
                        added = true;
                    }
                });
        if (!added) {
            let div = this.addCardToContainer(blueprintId, title, imageUrl, subDeck, container, false)
            div.addClass("cardInDeck");
            div.draggable({
                helper: "clone",
                opacity: 0.6,
                appendTo: "body"
            });
        }

        this.deckModified(true);
    }

    showLibraryDeckInHtml(deckJson) {
        let deckDetailsDialog = $('<div id="deckDetailsDialog"></div>')
            .dialog({
                title:"Details for '" + deckJson.deckName + "' Deck",
                autoOpen:false,
                closeOnEscape:true,
                resizable:true,
                width:700,
                height:400,
                modal:true
            });

        deckDetailsDialog.on("dialogbeforeclose", this.discardDeckHtml);

        let htmlText = this.getDeckHtml(deckJson);
        deckDetailsDialog.append(htmlText);
        deckDetailsDialog.dialog("open");
    }


    getDeckHtml(deckJson) {
        let cardListDiv = document.createElement("div");
        
        let formatDiv = document.createElement("div");
        let formatH2 = document.createElement("h2");
        formatH2.append(`Format: ${deckJson.targetFormat.formatName}`);
        formatDiv.append(formatH2);
        cardListDiv.append(formatDiv);

        for (const key in deckJson.cards) {
            if (Object.prototype.hasOwnProperty.call(deckJson.cards, key)) {
                let subDeckName = key;

                let subDeckNameHeader = document.createElement("div");
                let subDeckNameBold = document.createElement("b");
                subDeckNameBold.append(`${subDeckName}`);
                subDeckNameHeader.append(subDeckNameBold);
                cardListDiv.append(subDeckNameHeader);

                // TODO: Spans here is pretty terrible and a relic of passing raw html.
                // We need an absolute positioned block item probably.
                
                let cardsInSubDeck = deckJson.cards[key];
                for (const card of cardsInSubDeck) {
                    let cardDiv = document.createElement("div");
                    let cardTitleAndCountSpan = document.createElement("span");
                    cardTitleAndCountSpan.classList.add("tooltip");
                    cardTitleAndCountSpan.append(`${card.cardTitle}`);

                    let cardImageSpan = document.createElement("span");
                    let cardDivTag = createCardDiv(card.imageUrl, card.cardTitle, false, false, false, false, false, null);
                    cardDivTag.classList.add("ttimage");
                    cardImageSpan.append(cardDivTag);
                    cardTitleAndCountSpan.append(cardImageSpan);

                    cardTitleAndCountSpan.append(` x${card.count}`);
                    
                    cardDiv.append(cardTitleAndCountSpan)
                    cardListDiv.append(cardDiv);
                }
            }
        }
        
        if (deckJson.notes != null && deckJson.notes != "null") {
            let notesDiv = document.createElement("div");
            let notesH3 = document.createElement("h3");
            notesH3.append("Notes:")
            notesDiv.append(notesH3);

            let notesText = document.createElement("div");
            notesText.append(`${deckJson.notes.replaceAll("\n", "<br/>")}`); // TODO: Improve somewhat
            notesDiv.append(notesText);

            cardListDiv.append(notesDiv);
        }

        let cardListDivJQ = $(cardListDiv);

        return cardListDivJQ;
    }

    discardDeckHtml(event, _ui) {
        // This function removes the in-memory result of URLs that were stored as blobs via fetch(),
        // returning their memory to the browser. On Firefox, this would correctly only remove
        // the image loaded in the dialog box, but on Chrome, all graphics sharing the same URL are
        // removed.
        //
        // If a card is shown in the dialog and in the loaded deck or in the search
        // results, that image would accidentally be removed too, which we don't want.
        // So, search for any duplicates and leave those in memory.
        //
        // See reasoning and examples of behavior at https://stackoverflow.com/a/73498140

        let deckDetailsDialog = event.target;
        let notDialogSrcURLs = new Set();

        // Collect image urls from deck pane, in background
        let deckPane = document.querySelector("#deckDiv");
        let deckPaneImageMatches = deckPane.querySelectorAll('img[class="card_img"]');
        for (const deckPaneCard of deckPaneImageMatches) {
            let srcAttribute = deckPaneCard.getAttribute("src");
            if (srcAttribute != null && srcAttribute !== "") {
                notDialogSrcURLs.add(srcAttribute);
            }
        }

        // Collect image urls from search pane, in background
        let searchPane = document.querySelector("#collection-display");
        let searchPaneImageMatches = searchPane.querySelectorAll('img[class="card_img"]');
        for (const searchPaneCard of searchPaneImageMatches) {
            let srcAttribute = searchPaneCard.getAttribute("src");
            if (srcAttribute != null && srcAttribute !== "") {
                notDialogSrcURLs.add(srcAttribute);
            }
        }

        // Determine if we can safely remove the image URL used in the dialog, then do so.
        let dialogImageMatches = deckDetailsDialog.querySelectorAll('img[class="card_img"]');
        for (const dialogCard of dialogImageMatches) {
            let srcAttribute = dialogCard.getAttribute("src");
            if (srcAttribute != null && // not undefined
                srcAttribute !== "" && // not empty string
                notDialogSrcURLs.has(srcAttribute) == false) { // and not used elsewhere on the page
                URL.revokeObjectURL(srcAttribute);
            }
        }
    }
}