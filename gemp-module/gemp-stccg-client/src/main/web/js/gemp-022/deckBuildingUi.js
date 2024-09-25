import GempClientCommunication from "./communication.js";
import Card from "./jCards.js";
import { createCardDiv } from "./jCards.js";
import CardFilter from "./cardFilter.js";
import { NormalCardGroup, layoutTokens } from "./jCardGroup.js";

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

        this.collectionType = "default";
        this.deckDiv = $("#deckDiv");
        this.manageDecksDiv = $("#manageDecks");
        this.formatSelect = $("#formatSelect");


        this.cardFilter = new CardFilter($("#collectionDiv"),
                function (filter, start, count, callback) {
                    that.comm.getCollection(that.collectionType, filter, start, count, function (xml) {
                        callback(xml);
                    }, {
                        "404":function () {
                            alert("You don't have collection of that type.");
                        }
                    });
                },
                function () {
                    that.clearCollection();
                },
                function (elem, type, blueprintId, count, imageUrl) {
                    that.addCardToCollection(type, blueprintId, count, elem.getAttribute("side"),
                        elem.getAttribute("contents"), elem.getAttribute("imageUrl"));
                },
                function () {
                    that.finishCollection();
                },
                that.formatSelect.val());

        $("#formatSelect").change(
                function () {
                    that.deckModified(true);
                    that.cardFilter.setFormat(that.formatSelect.val());
                    that.cardFilter.updateSetOptions();
                });

        var collectionSelect = $("#collectionSelect");
        var newDeckBut = $("#newDeckBut").button();
        var saveDeckBut = $("#saveDeckBut").button();
        var renameDeckBut = $("#renameDeckBut").button();
        var copyDeckBut = $("#copyDeckBut").button();
        var importDeckBut = $("#importDeckBut").button();
        var libraryListBut = $("#libraryListBut").button();
        var deckListBut = $("#deckListBut").button();
        var notesBut = $("#notesBut").button();

        this.deckNameSpan = ("#editingDeck");

        newDeckBut.click(
                function () {
                    that.deckName = null;
                    $("#editingDeck").text("New deck");
                    that.clearDeck();
                });

        saveDeckBut.click(
                function () {
                    if (that.deckName == null) {
                        var newDeckName = prompt("Enter the name of the deck", "");
                        if (that.validateDeckName(newDeckName)) {
                            that.deckName = newDeckName;
                            $("#editingDeck").text(newDeckName);
                            that.saveDeck(true);
                        }
                    } else {
                        that.saveDeck(false);
                    }
                });

        renameDeckBut.click(
                function () {
                    if (that.deckName == null) {
                        alert("You can't rename this deck, since it's not named (saved) yet.");
                        return;
                    }
                    that.renameCurrentDeck();
                });

        copyDeckBut.click(
                function () {
                    that.deckName = null;
                    $("#editingDeck").text("New deck");
                });

        importDeckBut.click(
                function () {
                    that.deckName = null;
                    that.importDecklist();
                });

        deckListBut.click(function () { that.loadDeckList(); });
        libraryListBut.click(function () { that.loadLibraryList(); });
        notesBut.click(function () { that.editNotes(); });

        this.collectionDiv = $("#collectionDiv");

        $("#collectionSelect").change(
                function () {
                    that.collectionType = that.getCollectionType();
                    that.cardFilter.getCollection();
                });
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

        $("body").click(
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

        var width = $(window).width();
        var height = $(window).height();

        var swipeOptions = {
            threshold:20,
            swipeUp:function (event) {
                that.infoDialog.prop({ scrollTop:that.infoDialog.prop("scrollHeight") });
                return false;
            },
            swipeDown:function (event) {
                that.infoDialog.prop({ scrollTop:0 });
                return false;
            }
        };
        this.infoDialog.swipe(swipeOptions);

        this.getCollectionTypes();

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
        
        var newDeckName = prompt("Enter new name for the deck", oldName);
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
        return $("#collectionSelect option:selected").prop("value");
    }

    getCollectionTypes() {
        var that = this;
        this.comm.getCollectionTypes(
                function (xml) {
                    var root = xml.documentElement;
                    if (root.tagName == "collections") {
                        var collections = root.getElementsByTagName("collection");
                        for (var i = 0; i < collections.length; i++) {
                            var collection = collections[i];
                            $("#collectionSelect").append(
                                "<option value='" + collection.getAttribute("type") + "'>" +
                                    collection.getAttribute("name") + "</option>"
                            );
                        }
                    }
                });
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
        var deckImport = $("<textarea rows='5' cols='30' id='deckImport' decklist='decklist'></textarea>");
        var getDecklistTextBut = $("<button title='Import'>Import</button>").button();

        var importDialogDiv = $("<div></div>");
        importDialogDiv.append(deckImport);
        importDialogDiv.append(getDecklistTextBut);
        that.deckImportDialog.append(importDialogDiv);

        getDecklistTextBut.click(
             function () {
                var decklist = $('textarea[decklist="decklist"]').val()
                that.parseDecklist(decklist);
            }
        );
        that.deckImportDialog.dialog("open");
    }
    
    parseDecklist(rawText) {
        this.clearDeck();
        var that = this;
        var rawTextList = rawText.split("\n");
        var formattedText = "";
        for (var i = 0; i < rawTextList.length; i++) {
            if (rawTextList[i] != "") {
                var line = that.removeNotes(rawTextList[i]).toLowerCase();
                line = line.replace(/[\*•]/g,"").replace(/’/g,"'")
                        .replace(/starting|start|ring-bearer:|ring:/g,"")
                formattedText = formattedText + line.trim() + "~";
            }
        }
                
        this.importDeckCollection(formattedText, function (xml) {
            var cards = xml.documentElement.getElementsByTagName("card");
            for (var i = 0; i < cards.length; i++) {
                var cardElem = cards[i];
                var blueprintId = cardElem.getAttribute("blueprintId");
                var subDeck = cardElem.getAttribute("subDeck");
                var group = cardElem.getAttribute("group");
                var imageUrl = cardElem.getAttribute("imageUrl");
                var cardCount = parseInt(cardElem.getAttribute("count"));
                for (var j = 0; j < cardCount; j++) {
                    that.addCardToDeckDontLayout(blueprintId, imageUrl, subDeck);
                }
            }
            that.deckModified(true);
            that.layoutDeck();
            $("#editingDeck").text("Imported Deck (unsaved)");
        });
    }

    removeNotes(line) {
        var processedLine = line;
        var hasNotes = false;
        var start = line.indexOf("(");
        var end = line.indexOf(")", start);
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
        this.comm.importCollection(decklist, function (xml) {
            callback(xml);
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
            
        var notesElem = $("<textarea class='notesText'></textarea>");
            
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
        this.comm.getDecks(function (xml) {
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

            var root = xml.documentElement;
            if (root.tagName == "decks") {
                var decks = root.getElementsByTagName("deck");
                var deckNames = [];
                for (var i = 0; i < decks.length; i++) {
                    var deck = decks[i];
                    var deckName = deck.childNodes[0].nodeValue;
                    deckNames[i] = deckName;
                    var formatName = deck.getAttribute("targetFormat");
                    var openDeckBut = $("<button title='Open deck'><span class='ui-icon ui-icon-folder-open'></span></button>").button();
                    var renameDeckBut = $("<button title='Rename deck'><span class='ui-icon ui-icon-pencil'></span></button>").button();
                    var deckListBut = $("<button title='Share deck list'><span class='ui-icon ui-icon-extlink'></span></button>").button();
                    var deleteDeckBut = $("<button title='Delete deck'><span class='ui-icon ui-icon-trash'></span></button>").button();

                    var deckElem = $("<div class='deckItem'></div>");
                    deckElem.append(openDeckBut);
                    deckElem.append(renameDeckBut);
                    deckElem.append(deckListBut);
                    deckElem.append(deleteDeckBut);
                    var deckNameDiv = $("<span/>").html(formatDeckName(formatName, deckName));
                    deckElem.append(deckNameDiv);

                    that.deckListDialog.append(deckElem);

                    openDeckBut.click(
                            (function (i) {
                                return function () {
                                    that.comm.getDeck(deckNames[i],
                                            function (xml) {
                                                that.setupDeck(xml, deckNames[i]);
                                            });
                                };
                            })(i));


                    deckListBut.click(
                            (function (i) {
                                return function () {
                                    that.comm.shareDeck(deckNames[i],
                                        function(html) {
                                            window.open('/share/deck?id=' + html, "_blank");
                                        });
                                };
                            })(i));
                    
                    renameDeckBut.click(
                            (function (i, formatName, deckNameDiv) {
                                return function () {
                                    that.renameDeck(deckNames[i], function (newDeckName) {
                                        deckNameDiv.html(formatDeckName(formatName, newDeckName));
                                        
                                        if (that.deckName == deckNames[i]) 
                                        {
                                            that.deckName = newDeckName;
                                            that.deckModified(that.deckContentsDirty);
                                        }
                                        deckNames[i] = newDeckName;
                                    })
                                };
                            })(i, formatName, deckNameDiv));

                    deleteDeckBut.click(
                            (function (i) {
                                return function () {
                                    if (confirm("Are you sure you want to delete this deck?")) {
                                        that.comm.deleteDeck(deckNames[i],
                                                function () {
                                                    if (that.deckName == deckNames[i]) {
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
            }

            that.deckListDialog.dialog("open");
        });
    }
    
    loadLibraryList() {
        var that = this;
        this.comm.getLibraryDecks(function (xml) {
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

            var root = xml.documentElement;
            if (root.tagName == "decks") {
                var decks = root.getElementsByTagName("deck");
                var deckNames = [];
                for (var i = 0; i < decks.length; i++) {
                    var deck = decks[i];
                    var deckName = deck.childNodes[0].nodeValue;
                    deckNames[i] = deckName;
                    var formatName = deck.getAttribute("targetFormat");
                    var openDeckBut = $("<button title='Open deck'><span class='ui-icon ui-icon-folder-open'></span></button>").button();
                    var deckListBut = $("<button title='Deck list'><span class='ui-icon ui-icon-clipboard'></span></button>").button();

                    var deckElem = $("<div class='deckItem'></div>");
                    deckElem.append(openDeckBut);
                    deckElem.append(deckListBut);
                    var deckNameDiv = $("<span/>").html(formatDeckName(formatName, deckName));
                    deckElem.append(deckNameDiv);

                    that.deckListDialog.append(deckElem);

                    openDeckBut.click(
                            (function (i) {
                                return function () {
                                    that.comm.getLibraryDeck(deckNames[i],
                                        function (xml) {
                                            that.setupDeck(xml, deckNames[i]);
                                            that.deckModified(true);
                                        });
                                };
                            })(i));


                    deckListBut.click(
                            (function (i) {
                                return function () {
                                    window.open('/gemp-stccg-server/deck/libraryHtml?deckName=' + encodeURIComponent(deckNames[i]), "_blank");
                                };
                            })(i));
                }
            }

            that.deckListDialog.dialog("open");
        });
    }

    clickCardFunction(event) {
        var that = this;

        var tar = $(event.target);
        if (tar.length == 1 && tar[0].tagName == "A")
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
                            var width = that.selectionDialog.width() + 10;
                            var height = that.selectionDialog.height() + 10;
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
                        var cardData = selectedCardElem.data("card");
                        this.packSelectionId = cardData.blueprintId;
                        var selection = selectedCardElem.data("selection");
                        var blueprintIds = selection.split("|");
                        for (var i = 0; i < blueprintIds.length; i++) {
                                // TODO - This call of new Card() doesn't have an imageUrl parameter, is this a problem?
                            var card = new Card(blueprintIds[i], "selection", "selection" + i, "player");
                            var cardDiv = createCardDiv(
                                card.imageUrl, null, card.isFoil(), false, card.isPack(), card.hasErrata()
                            );
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

        var deckContents = this.getDeckContents();
        if (deckContents == null)
            alert("Cannot save an empty deck.");
        else
            this.comm.saveDeck(this.deckName, that.formatSelect.val(), this.notes, deckContents, function (xml) {
                that.deckModified(false);
                alert("Deck was saved.  Refresh the Game Hall to see it!");
            }, {
                "400":function () {
                    alert("Invalid deck format.");
                }
            });
    }

    addCardToContainer(blueprintId, imageUrl, subDeck, container, tokens) {
        var card = new Card(blueprintId, subDeck, "deck", "player", imageUrl);
        var cardDiv = createCardDiv(card.imageUrl, null, card.isFoil(), tokens, card.isPack(), card.hasErrata());
        cardDiv.data("card", card);
        container.append(cardDiv);
        return cardDiv;
    }

    addCardToDeckDontLayout(blueprintId, imageUrl, zone) {
        var that = this;
        this.addCardToDeck(blueprintId, imageUrl, zone);
    }

    addCardToDeckAndLayout(cardElem, imageUrl, zone) {
        var that = this;
        var cardData = cardElem.data("card");
        var blueprintId = cardData.blueprintId;
        this.addCardToDeck(blueprintId, imageUrl, zone);
        that.layoutDeck();
        that.deckModified(true);
        cardData.tokens = {count:(parseInt(cardData.tokens["count"]) + 1)};
        layoutTokens(cardElem);
    }

    deckModified(value) {
        
        var name = (this.deckName == null) ? "New deck" : this.deckName;
        if (value)
        {
            this.deckValidationDirty = true;
            this.deckContentsDirty = true;
            $("#editingDeck").html("<font color='orange'>*" + name + " - modified</font>");
        }
        else
        {
            this.deckContentsDirty = false;
            $("#editingDeck").text(name);
        }
    }

    addCardToDeck(blueprintId, imageUrl, subDeck) {
        var that = this;
        var added = false;
        $(".card.cardInDeck", this.drawDeckDiv).each(
                function () {
                    var cardData = $(this).data("card");
                    if (cardData.blueprintId == blueprintId) {
                        var attDiv = that.addCardToContainer(blueprintId, imageUrl, "attached", that.drawDeckDiv, false);
                        cardData.attachedCards.push(attDiv);
                        added = true;
                    }
                });
        if (!added) {
            var div = this.addCardToContainer(blueprintId, imageUrl, subDeck, this.drawDeckDiv, false)
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
        var deckContents = this.getDeckContents();
        if (deckContents != null && deckContents != "") 
        {
            this.comm.getDeckStats(deckContents, 
                   that.formatSelect.val(),
                    function (html) 
                    {
                        $("#deckStats").html(html);
                    }, 
                    {
                        "400":function () 
                        {
                            alert("Invalid deck for getting stats.");
                        }
                    });
        } else {
            $("#deckStats").html("Deck is empty");
        }
    }
    
    updateFormatOptions() {
        var that = this;
        var currentFormat = that.formatSelect.val();
        
        this.comm.getFormats(false,
            function (json) 
            {
                that.formatSelect.empty();
                //var formats = JSON.parse(json);
                $(json).each(function (index, o) {    
                    var $option = $("<option/>")
                        .attr("value", o.code)
                        .text(o.name);
                    that.formatSelect.append($option);
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
        var cardData = cardDiv.data("card");
        if (cardData.attachedCards.length > 0) {
            cardData.attachedCards[0].remove();
            cardData.attachedCards.splice(0, 1);
        } else {
            cardDiv.remove();
        }
        var cardInCollectionElem = null;
        $(".card", this.normalCollectionDiv).each(
                function () {
                    var tempCardData = $(this).data("card");
                    if (tempCardData.blueprintId == cardData.blueprintId)
                        cardInCollectionElem = $(this);
                });
        if (cardInCollectionElem != null) {
            var cardInCollectionData = cardInCollectionElem.data("card");
            cardInCollectionData.tokens = {count:(parseInt(cardInCollectionData.tokens["count"]) - 1)};
            layoutTokens(cardInCollectionElem);
        }
        this.layoutDeck();
        this.deckModified(true);
    }

    clearDeck() {
        $(".cardInDeck").each(
                function () {
                    var cardData = $(this).data("card");
                    for (var i = 0; i < cardData.attachedCards.length; i++)
                        cardData.attachedCards[i].remove();
                });
        $(".cardInDeck").remove();

        this.layoutUI(false);

        this.deckValidationDirty = true;
    }

    setupDeck(xml, deckName) {
        var root = xml.documentElement;
        if (root.tagName == "deck") {
            this.clearDeck();
            this.deckName = deckName;
            $("#editingDeck").text(deckName);
            
            var targetFormat = root.getElementsByTagName("targetFormat");
            if (targetFormat.length > 0)
            {
                var formatName = targetFormat[0].getAttribute("formatName");
                var formatCode = targetFormat[0].getAttribute("formatCode");

                this.formatSelect.val(formatCode);
            }
            
            var notes = root.getElementsByTagName("notes");
            this.notes = notes[0].innerHTML;

            var cards = root.getElementsByTagName("card");
            for (var i = 0; i < cards.length; i++)
                this.addCardToDeck(cards[i].getAttribute("blueprintId"), cards[i].getAttribute("imageUrl"), cards[i].getAttribute("subDeck"));

            this.layoutUI(false);

            this.cardFilter.getCollection();
        }
        this.deckModified(false);
    }

    clearCollection() {
        $(".card", this.normalCollectionDiv).remove();
    }

    addCardToCollection(type, blueprintId, count, side, contents, imageUrl) {
        if (type == "pack") {
            if (blueprintId.substr(0, 3) == "(S)") {
                var card = new Card(blueprintId, "pack", "collection", "player", imageUrl);
                card.tokens = {"count":count};
                var cardDiv = createCardDiv(card.imageUrl, null, false, true, true, false);
                cardDiv.data("card", card);
                cardDiv.data("selection", contents);
                cardDiv.addClass("selectionInCollection");
            } else {
                var card = new Card(blueprintId, "pack", "collection", "player", imageUrl);
                card.tokens = {"count":count};
                var cardDiv = createCardDiv(card.imageUrl, null, false, true, true, false);
                cardDiv.data("card", card);
                cardDiv.addClass("packInCollection");
            }
            this.normalCollectionDiv.append(cardDiv);
        } else if (type == "card") {
            let locationIndex;
            let upsideDown; 
            var card = new Card(blueprintId, side, "collection", "player", imageUrl, locationIndex, upsideDown);
            var countInDeck = 0;
            $(".card", this.deckDiv).each(
                    function () {
                        var tempCardData = $(this).data("card");
                        if (blueprintId == tempCardData.blueprintId)
                            countInDeck++;
                    });
            card.tokens = {"count":countInDeck};
            var cardDiv = createCardDiv(card.imageUrl, null, card.isFoil(), true, false, card.hasErrata());
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
        deckBuildingUI = this;
        this.drawDeckDiv = $("#decksRegion");
        this.drawDeckGroup = new NormalCardGroup(this.drawDeckDiv, function (card) {
            return (card.zone == "DRAW_DECK");
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
            var manageHeight = 23;

            var padding = 5;
            var collectionWidth = this.collectionDiv.width();
            var collectionHeight = this.collectionDiv.height();

            var deckWidth = this.deckDiv.width();
            var deckHeight = this.deckDiv.height() - (manageHeight + padding);

            var rowHeight = Math.floor((deckHeight - 6 * padding) / 5);
            var sitesWidth = Math.floor(1.5 * deckHeight / 5);
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

        var result = "";
        result += "DRAW_DECK|";

        var cards = new Array();
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
        var deckBuildingUI = this;
        this.drawDeckDiv = $("#decksRegion");
        this.drawDeckGroup = new NormalCardGroup(this.drawDeckDiv, function (card) {
            return (card.zone == "DRAW_DECK");
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
            return (card.zone == "MISSIONS");
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
            return (card.zone == "SEED_DECK");
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
            var padding = 5;
            var manageHeight = 23;
            var statsHeight = 70;
            var statsTop = this.deckDiv.height() - statsHeight - padding;
            var deckDivWidth = this.deckDiv.width();
            var deckDivHeight = this.deckDiv.height();

            this.manageDecksDiv.css({position:"absolute", left:padding, top:padding,
                width:deckDivWidth - padding, height:manageHeight});
            this.statsDiv.css({ position:"absolute", left:padding, top:statsTop,
                width:deckDivWidth - padding, height:statsHeight });

            var collectionWidth = this.collectionDiv.width();
            var collectionHeight = this.collectionDiv.height();
            this.normalCollectionGroup.setBounds(0, 0, collectionWidth - padding * 2, collectionHeight - 160);
            this.cardFilter.layoutUi(padding, 0, collectionWidth - padding, 160);

            var subDeckTop = manageHeight + padding * 2;
            var subDeckBottom = statsTop - padding;
            var subDeckLeft = padding;
            var subDeckRight = deckDivWidth;
            var subDeckWidth = subDeckRight - subDeckLeft;

            var deckRowHeight = (subDeckBottom - subDeckTop - padding) / 2;
            var drawDeckTop = subDeckTop + deckRowHeight + padding;
            var missionsWidth = subDeckWidth * 0.3;
            var seedDeckLeft = missionsWidth + padding * 2;
            var seedDeckWidth = subDeckRight - seedDeckLeft;
            
            this.missionsDiv.css({ position:"absolute", left:padding, top:subDeckTop, width:missionsWidth,
                height:deckRowHeight});
            this.missionsGroup.setBounds(0, 0, missionsWidth, deckRowHeight);

            this.seedDeckDiv.css({ position:"absolute", left:seedDeckLeft, top:subDeckTop, width:seedDeckWidth,
                height:deckRowHeight});
            this.seedDeckGroup.setBounds(0, 0, seedDeckWidth, deckRowHeight);

            this.drawDeckDiv.css({ position:"absolute", left:padding, top:drawDeckTop, width:subDeckWidth,
                height:deckRowHeight });
            this.drawDeckGroup.setBounds(0, 0, subDeckWidth, deckRowHeight);

        } else {
            this.layoutDeck();
            this.normalCollectionGroup.layoutCards();
        }
    }

    getDeckContents() {

        var result = "";
        var cards = new Array();
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

    addCardToDeck(blueprintId, imageUrl, subDeck) {
        var that = this;
        var added = false;
        var container = null;
        if (subDeck == "MISSIONS") {
            container = that.missionsDiv;
        } else if (subDeck == "SEED_DECK") {
            container = that.seedDeckDiv;
        } else {
            container = that.drawDeckDiv;
        }
        $(".card.cardInDeck", container).each(
                function () {
                    var cardData = $(this).data("card");
                    if (cardData.blueprintId == blueprintId) {
                        var attDiv = that.addCardToContainer(blueprintId, imageUrl, "attached", container, false);
                        cardData.attachedCards.push(attDiv);
                        added = true;
                    }
                });
        if (!added) {
            var div = this.addCardToContainer(blueprintId, imageUrl, subDeck, container, false)
            div.addClass("cardInDeck");
            div.draggable({
                helper: "clone",
                opacity: 0.6,
                appendTo: "body"
            });
        }

        this.deckModified(true);
    }
}