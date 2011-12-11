var GempLotrHallUI = Class.extend({
    div : null,
    comm: null,
    chat: null,
    supportedFormatsInitialized: false,
    supportedFormatsSelect: null,
    decksSelect: null,
    createTableButton: null,
    leaveTableButton: null,

    tablesDiv: null,

    init: function(div, url, chat) {
        this.div = div;
        this.comm = new GempLotrCommunication(url, function(xhr, ajaxOptions, thrownError) {
            if (thrownError != "abort") {
                if (xhr != null) {
                    if (xhr.status == 401) {
                        chat.appendMessage("Game hall problem - You're not logged in, go to the <a href='index.html'>main page</a> to log in", "warningMessage");
                        return;
                    } else {
                        chat.appendMessage("The game hall had a problem communicating with the server (" + xhr.status + "), no new updates will be displayed.", "warningMessage");
                        chat.appendMessage("Reload the browser page (press F5) to resume the game hall functionality.", "warningMessage");
                        return;
                    }
                }
                chat.appendMessage("The game hall had a problem communicating with the server, no new updates will be displayed.", "warningMessage");
                chat.appendMessage("Reload the browser page (press F5) to resume the game hall functionality.", "warningMessage");
            }
        });
        this.chat = chat;

        var width = $(div).width();
        var height = $(div).height();

        this.tablesDiv = $("<div></div>");
        this.tablesDiv.css({overflow: "auto", left: "0px", top: "0px", width: width + "px", height: (height - 30) + "px"});
        this.div.append(this.tablesDiv);

        var buttonsDiv = $("<div></div>");
        buttonsDiv.css({left: "0px", top: (height - 30) + "px", width: width + "px", height: 29 + "px", align: "right", backgroundColor: "#000000", "border-top-width": "1px", "border-top-color": "#ffffff", "border-top-style": "solid"});

        var that = this;

        var editDeck = $("<button>Deck builder</button>");
        editDeck.button().click(
                function() {
                    location.href = 'deckBuild.html';
                });

        buttonsDiv.append(editDeck);
        buttonsDiv.append(" | ");

        var editLeagueDeck = $("<button>League deck builder</button>");
        editLeagueDeck.button().click(
                function() {
                    location.href = 'deckBuild.html?collectionType=test_league';
                });

        buttonsDiv.append(editLeagueDeck);
        buttonsDiv.append(" | ");

        this.supportedFormatsSelect = $("<select style='width: 220px'></select>");
        this.supportedFormatsSelect.hide();

        this.createTableButton = $("<button>Create table</button>");
        $(this.createTableButton).button().click(
                function() {
                    that.supportedFormatsSelect.hide();
                    that.decksSelect.hide();
                    that.createTableButton.hide();
                    var format = that.supportedFormatsSelect.val();
                    var deck = that.decksSelect.val();
                    if (deck != null)
                        that.comm.createTable(format, deck, function(xml) {
                            that.processResponse(xml);
                        });
                });
        this.createTableButton.hide();

        this.decksSelect = $("<select style='width: 220px'></select>");
        this.decksSelect.hide();

        buttonsDiv.append(this.supportedFormatsSelect);
        buttonsDiv.append(this.decksSelect);
        buttonsDiv.append(this.createTableButton);

        this.leaveTableButton = $("<button>Leave table</button>");
        $(this.leaveTableButton).button().click(
                function() {
                    that.leaveTableButton.hide();
                    that.comm.leaveTable();
                });
        this.leaveTableButton.hide();

        buttonsDiv.append(this.leaveTableButton);

        this.div.append(buttonsDiv);

        this.updateHall();
        this.updateDecks();
    },

    updateHall: function() {
        var that = this;

        this.comm.getHall(function(xml) {
            that.processHall(xml);
        });
    },

    updateDecks: function() {
        var that = this;
        this.comm.getDecks(function(xml) {
            that.processDecks(xml);
        });
    },

    processResponse: function(xml) {
        if (xml != null) {
            var root = xml.documentElement;
            if (root.tagName == "error") {
                var message = root.getAttribute("message");
                this.chat.appendMessage(message, "warningMessage");
            }
        }
    },

    processDecks: function(xml) {
        var root = xml.documentElement;
        if (root.tagName == "decks") {
            this.decksSelect.html("");
            var decks = root.getElementsByTagName("deck");
            for (var i = 0; i < decks.length; i++) {
                var deck = decks[i];
                var deckName = decks[i].childNodes[0].nodeValue;
                var deckElem = $("<option></option>");
                deckElem.attr("value", deckName);
                deckElem.html(deckName);
                this.decksSelect.append(deckElem);
            }
            this.decksSelect.css("display", "");
        }
    },

    processHall: function(xml) {
        var root = xml.documentElement;
        if (root.tagName == "hall") {
            this.tablesDiv.html("");

            var motd = root.getAttribute("motd");
            if (motd != null)
                $("#motd").html(motd);

            var waiting = root.getAttribute("waiting") == "true";

            var tables = root.getElementsByTagName("table");
            for (var i = 0; i < tables.length; i++) {
                var table = tables[i];
                var id = table.getAttribute("id");
                var gameId = table.getAttribute("gameId");
                var status = table.getAttribute("status");
                var playersAttr = table.getAttribute("players");
                var formatName = table.getAttribute("format");
                var players = new Array();
                if (playersAttr.length > 0)
                    players = playersAttr.split(",");
                var winner = table.getAttribute("winner");

                var tableDiv = this.createTableDiv(id, gameId, status, formatName, players, waiting, winner);
                this.tablesDiv.append(tableDiv);
            }

            var games = root.getElementsByTagName("game");
            if (games.length > 0) {
                var waitingGameId = games[0].getAttribute("id");
                var participantId = getUrlParam("participantId");
                var participantIdAppend = "";
                if (participantId != null)
                    participantIdAppend = "&participantId=" + participantId;
                location.href = "/gemp-lotr/game.html?gameId=" + waitingGameId + participantIdAppend;
            }

            if (!this.supportedFormatsInitialized) {
                var formats = root.getElementsByTagName("format");
                for (var i = 0; i < formats.length; i++) {
                    var format = formats[i].childNodes[0].nodeValue;
                    var type = formats[i].getAttribute("type");
                    this.supportedFormatsSelect.append("<option value='" + type + "'>" + format + "</option>");
                }
                this.supportedFormatsInitialized = true;
            }

            if (waiting) {
                this.supportedFormatsSelect.hide();
                this.decksSelect.hide();
                this.createTableButton.hide();
                this.leaveTableButton.css("display", "");
            } else {
                this.supportedFormatsSelect.css("display", "");
                this.decksSelect.css("display", "");
                this.createTableButton.css("display", "");
                this.leaveTableButton.hide();
            }

            var that = this;

            setTimeout(function() {
                that.updateHall();
            }, 1000);
        }
    },

    createTableDiv: function(id, gameId, status, formatName, players, waiting, winner) {
        var tableDiv = $("<div></div>");
        tableDiv.css({ display: "inline-table", width: "120px", height: "120px", margin: "5px", "background-color": "#333300", color: "#ffffff"});
        tableDiv.append("<div class='tableFormatName'>" + formatName + "</div>");
        tableDiv.append("<div class='tableStatus'>" + status + "</div>");
        tableDiv.append("<hr/>");
        for (var i = 0; i < players.length; i++) {
            if (winner == players[i])
                tableDiv.append("<div class='tablePlayer winningPlayer'>" + players[i] + "</div>");
            else if (winner != null)
                tableDiv.append("<div class='tablePlayer losingPlayer'>" + players[i] + "</div>");
            else
                tableDiv.append("<div class='tablePlayer'>" + players[i] + "</div>");
        }

        if (players.length < 2) {
            var that = this;

            if (!waiting) {
                var but = $("<button>Join table</button>");
                $(but).button().click(
                        function(event) {
                            var deck = that.decksSelect.val();
                            if (deck != null)
                                that.comm.joinTable(id, deck, function(xml) {
                                    that.processResponse(xml);
                                });
                        });
                tableDiv.append(but);
            }
        }

        if (status == "Playing" || status == "Preparation") {
            var but = $("<button>Watch game</button>");
            $(but).button().click(
                    function(event) {
                        var participantId = getUrlParam("participantId");
                        var participantIdAppend = "";
                        if (participantId != null)
                            participantIdAppend = "&participantId=" + participantId;
                        location.href = "/gemp-lotr/game.html?gameId=" + gameId + participantIdAppend;
                    });
            tableDiv.append(but);
        }

        return tableDiv;
    }
});