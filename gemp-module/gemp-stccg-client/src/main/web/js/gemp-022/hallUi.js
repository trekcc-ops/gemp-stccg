import GempClientCommunication from './communication.js';
import { formatPrice, getUrlParam } from './common.js';

export default class GempHallUI {
	comm;
	chat;
	supportedFormatsInitialized = false;
	supportedFormatsSelect;
	decksSelect;
	tableDescInput;
	timerSelect;
	createTableButton;
	isPrivateCheckbox;
	isInviteOnlyCheckbox;

	tablesDiv;
	buttonsDiv;
	adminTab;
	userInfo;

	pocketDiv;
	pocketValue;
	hallChannelId;

	constructor(url, chat) {
		var that = this;
		
		this.chat = chat;
		
		$("#chat").resizable({
			handles: "n",
			minHeight: 100,
			distance: 20
		});
		
		var storedChatSize = $.cookie("chatResize");
		if (storedChatSize == null)
			storedChatSize = 300;
		
		$("#chat").height(storedChatSize);
		
		$("#chat").resize(function() {
			$.cookie("chatResize", $("#chat").height(), { expires:365 });
		});
		
		this.comm = new GempClientCommunication(url, function (xhr, ajaxOptions, thrownError) {
			if (thrownError != "abort") {
				if (xhr != null) {
					if (xhr.status == 401) {
						that.chat.appendMessage("Game hall problem - You're not logged in, go to the <a href='index.html'>main page</a> to log in", "warningMessage");
						return;
					} 
					else if (xhr.status == 504) {
						console.log("HTTP error communicating with server: " + xhr.status);
						return;
					}
					else if (xhr.status != 504) {
						that.chat.appendMessage("The game hall had a problem communicating with the server (" + xhr.status + "), no new updates will be displayed.", "warningMessage");
						that.chat.appendMessage("Reload the browser page (press F5) to resume the game hall functionality.", "warningMessage");
						return;
					}
				}
				that.chat.appendMessage("The game hall had a problem communicating with the server, no new updates will be displayed.", "warningMessage");
				that.chat.appendMessage("Reload the browser page (press F5) to resume the game hall functionality.", "warningMessage");
			}
		});

		this.tablesDiv = $("#tablesDiv");
		this.tableDescInput = $("#tableDescInput");
		this.isPrivateCheckbox = $("#isPrivateCheckbox");
		this.isInviteOnlyCheckbox = $("#isInviteOnlyCheckbox");
		this.pocketDiv = $("#pocketDiv");
		this.supportedFormatsSelect = $("#supportedFormatsSelect");
		this.createTableButton = $("#createTableBut");
		this.decksSelect = $("#decksSelect");
		this.timerSelect = $("#timerSelect");
		this.buttonsDiv = $("#buttonsDiv");
		
		this.adminTab = $("#tabs > ul :nth-child(7)");
		this.adminTab.hide();
		
		this.comm.getPlayerInfo(function(json)
        { 
        	that.userInfo = json;
            if(that.userInfo.type.includes("a") || that.userInfo.type.includes("l"))
			{
				that.adminTab.show();
			}
			else
			{
				that.adminTab.hide();
			}
        });
		

		var hallSettingsStr = $.cookie("hallSettings");
		if (hallSettingsStr == null)
			hallSettingsStr = "1|1|0|0|0";
		var hallSettings = hallSettingsStr.split("|");

		this.initTable(hallSettings[0] == "1", "waitingTablesHeader", "waitingTablesContent");
		this.initTable(hallSettings[1] == "1", "playingTablesHeader", "playingTablesContent");
		this.initTable(hallSettings[2] == "1", "finishedTablesHeader", "finishedTablesContent");
		this.initTable(hallSettings[3] == "1", "tournamentQueuesHeader", "tournamentQueuesContent");
		this.initTable(hallSettings[4] == "1", "activeTournamentsHeader", "activeTournamentsContent");
		
		$("#deckbuilder-button").button();
		$("#bug-button").button();
		$("#report-button").button();
		$("#discord-button").button();
		$("#wiki-button").button();
		$("#merchant-button").button();

		$(this.createTableButton).button().click(
			function () {
				that.createTableButton.attr("disabled", "disabled");
				that.createTableButton.addClass("ui-state-disabled")
				that.createTableButton.removeClass("ui-state-focus")
				var format = that.supportedFormatsSelect.val();
				var deck = that.decksSelect.val();
				var tableDesc = that.tableDescInput.val();
				var timer = that.timerSelect.val();
				var isPrivate = that.isPrivateCheckbox.is(':checked');
				var isInviteOnly = that.isInviteOnlyCheckbox.is(':checked');
				if (deck != null)
					console.log("creating table");
					that.comm.createTable(format, deck, timer, tableDesc, isPrivate, isInviteOnly, function (xml) {
						console.log("received table response");
						that.processResponse(xml);
					});
			});

		this.getHall();
		this.updateDecks();
		
		
	}
	
	initTable(displayed, headerID, tableID) {
		var header = $("#" + headerID);

		var content = $("#" + tableID);

		var that = this;
		var toggle = function() {
			if (content.hasClass("hidden"))
				content.removeClass("hidden");
			else
				content.addClass("hidden");
			content.toggle("blind", {}, 200);
			that.updateHallSettings();
		};
		
		header.click(toggle);

		if (displayed) {
			content.show();
		} else {
			content.addClass("hidden");
			content.hide();
		}
	}


	updateHallSettings() {
		var visibilityToggle = $(".visibilityToggle", this.tablesDiv);
		var getSettingValue =
			function(index) {
				return $(visibilityToggle[index]).hasClass("hidden") ? "0" : "1";
			};

		var newHallSettings = getSettingValue(0) + "|" + getSettingValue(1) + "|" + getSettingValue(2) + "|" + getSettingValue(3) + "|" + getSettingValue(4);
		console.log("New settings: " + newHallSettings);
		$.cookie("hallSettings", newHallSettings, { expires:365 });
	}

	getHall() {
		var that = this;

		this.comm.getHall(
			function(xml) {
				that.processHall(xml);
			}, this.hallErrorMap());
	}

	updateHall() {
		var that = this;
		this.comm.updateHall(
			function (xml) {
				that.processHall(xml);
			}, this.hallChannelId, this.hallErrorMap());
	}

	hallErrorMap() {
		var that = this;
		return {
			"0": function() {
				that.showErrorDialog("Server connection error", "Unable to connect to server. Either server is down or there is a problem with your internet connection.", true, false);
			},
			"401":function() {
				that.showErrorDialog("Authentication error", "You are not logged in", false, true);
			},
			"409":function() {
				that.showErrorDialog("Concurrent access error", "You are accessing Game Hall from another browser or window. Close this window or if you wish to access Game Hall from here, click \"Refresh page\".", true, false);
			},
			"410":function() {
				that.showErrorDialog("Inactivity error", "You were inactive for too long and have been removed from the Game Hall. If you wish to re-enter, click \"Refresh page\".", true, false);
			}
		};
	}

	showErrorDialog(title, text, reloadButton, mainPageButton) {
		var buttons = {};
		if (reloadButton) {
			buttons["Refresh page"] =
				function () {
					location.reload(true);
				};
		}
		if (mainPageButton) {
			buttons["Go to main page"] =
				function() {
					location.href = "/gemp-module/";
				};
		}

		var dialog = $("<div></div>").dialog({
			title: title,
			resizable: false,
			height: 160,
			modal: true,
			buttons: buttons,
			closeText: ''
		}).text(text);
	}

	updateDecks() {
		var that = this;
		this.comm.getDecks(function (xml) {
			var count = xml.documentElement.getElementsByTagName("deck").length;
			if(count == 0)
			{
				that.comm.getLibraryDecks(function(xml) {
					that.processDecks(xml);
				});
			}
			else
			{
				that.processDecks(xml);
			}
			
		});
	}

	processResponse(xml) {
		if (xml != null) {
			var root = xml.documentElement;
			if (root.tagName == "error") {
				var message = root.getAttribute("message");
				this.chat.appendMessage(message, "warningMessage");
			}
		}
	}

	processDecks(xml) {
		var root = xml.documentElement;
		
		function formatDeckName(formatName, deckName)
		{
			return "[" + formatName + "] - " + deckName;
		}
		if (root.tagName == "decks") {
			this.decksSelect.html("");
			var decks = root.getElementsByTagName("deck");
			for (var i = 0; i < decks.length; i++) {
				var deck = decks[i];
				var deckName = deck.childNodes[0].nodeValue;
				var formatName = deck.getAttribute("targetFormat");
				var deckElem = $("<option/>")
						.attr("value", deckName)
						.text(formatDeckName(formatName, deckName));
				this.decksSelect.append(deckElem);
			}
			this.decksSelect.css("display", "");
		}
	}

	animateRowUpdate(rowSelector) {
		$(rowSelector, this.tablesDiv)
			.css({borderTopColor:"#000000", borderLeftColor:"#000000", borderBottomColor:"#000000", borderRightColor:"#000000"})
			.animate({borderTopColor:"#ffffff", borderLeftColor:"#ffffff", borderBottomColor:"#ffffff", borderRightColor:"#ffffff"}, "fast");
	}
	
	PlaySound(soundObj) {
		var myAudio = document.getElementById(soundObj);
		myAudio.play();
	}
	
	AddTesterFlag() {
		var that = this;
		
		that.comm.addTesterFlag(function () {
			window.location.reload(true);
		});
	}
	
	RemoveTesterFlag() {
		var that = this;
		
		that.comm.removeTesterFlag(function () {
			window.location.reload(true);
		});
	}

	processHall(xml) {
		var that = this;
		
		var root = xml.documentElement;
		if (root.tagName == "hall") {
			this.hallChannelId = root.getAttribute("channelNumber");

			var currency = parseInt(root.getAttribute("currency"));
			if (currency != this.pocketValue) {
				this.pocketValue = currency;
			}

			var motd = root.getAttribute("motd");
			if (motd != null)
				$("#motd").html("<b>MOTD:</b> " + motd);

			var serverTime = root.getAttribute("serverTime");
			if (serverTime != null)
				$(".serverTime").text("Server time: " + serverTime);

			var queues = root.getElementsByTagName("queue");
			for (var i = 0; i < queues.length; i++) {
				var queue = queues[i];
				var id = queue.getAttribute("id");
				var action = queue.getAttribute("action");
				if (action == "add" || action == "update") {
					var actionsField = $("<td></td>");

					var joined = queue.getAttribute("signedUp");
					if (joined != "true" && queue.getAttribute("joinable") == "true") {
						var but = $("<button>Join Queue</button>");
						$(but).button().click((
							function(queueId) {
								return function () {
									var deck = that.decksSelect.val();
									if (deck != null)
										that.comm.joinQueue(queueId, deck, function (xml) {
											that.processResponse(xml);
										});
								};
							}
							)(id));
						actionsField.append(but);
					} else if (joined == "true") {
						var but = $("<button>Leave Queue</button>");
						$(but).button().click((
							function(queueId) {
								return function() {
									that.comm.leaveQueue(queueId, function (xml) {
										that.processResponse(xml);
									});
								}
							})(id));
						actionsField.append(but);
					}

					var row = $("<tr class='queue" + id + "'><td>" + queue.getAttribute("format") + "</td>" +
						"<td>" + queue.getAttribute("collection") + "</td>" +
						"<td>" + queue.getAttribute("queue") + "</td>" +
						"<td>" + queue.getAttribute("start") + "</td>" +
						"<td>" + queue.getAttribute("system") + "</td>" +
						"<td>" + queue.getAttribute("playerCount") + "</td>" +
						"<td align='right'>" + formatPrice(queue.getAttribute("cost")) + "</td>" +
						"<td>" + queue.getAttribute("prizes") + "</td>" +
						"</tr>");

					row.append(actionsField);

					if (action == "add") {
						$("table.queues", this.tablesDiv)
							.append(row);
					} else if (action == "update") {
						$(".queue" + id, this.tablesDiv).replaceWith(row);
					}

					this.animateRowUpdate(".queue" + id);
				} else if (action == "remove") {
					$(".queue" + id, this.tablesDiv).remove();
				}
			}

			var tournaments = root.getElementsByTagName("tournament");
			for (var i = 0; i < tournaments.length; i++) {
				var tournament = tournaments[i];
				var id = tournament.getAttribute("id");
				var action = tournament.getAttribute("action");
				if (action == "add" || action == "update") {
					var actionsField = $("<td></td>");

					var joined = tournament.getAttribute("signedUp");
					if (joined == "true") {
						var but = $("<button>Drop from tournament</button>");
						$(but).button().click((
							function(tournamentId) {
								return function () {
									that.comm.dropFromTournament(tournamentId, function (xml) {
										that.processResponse(xml);
									});
								};
							}
							)(id));
						actionsField.append(but);
					}

					var row = $("<tr class='tournament" + id + "'><td>" + tournament.getAttribute("format") + "</td>" +
						"<td>" + tournament.getAttribute("collection") + "</td>" +
						"<td>" + tournament.getAttribute("name") + "</td>" +
						"<td>" + tournament.getAttribute("system") + "</td>" +
						"<td>" + tournament.getAttribute("stage") + "</td>" +
						"<td>" + tournament.getAttribute("round") + "</td>" +
						"<td>" + tournament.getAttribute("playerCount") + "</td>" +
						"</tr>");

					row.append(actionsField);

					if (action == "add") {
						$("table.tournaments", this.tablesDiv)
							.append(row);
					} else if (action == "update") {
						$(".tournament" + id, this.tablesDiv).replaceWith(row);
					}

					this.animateRowUpdate(".tournament" + id);
				} else if (action == "remove") {
					$(".tournament" + id, this.tablesDiv).remove();
				}
			}

			var tables = root.getElementsByTagName("table");
			for (var i = 0; i < tables.length; i++) {
				var table = tables[i];
				var id = table.getAttribute("id");
				var action = table.getAttribute("action");
				if (action == "add" || action == "update") {
					var status = table.getAttribute("status");

					var gameId = table.getAttribute("gameId");
					var statusDescription = table.getAttribute("statusDescription");
					var watchable = table.getAttribute("watchable");
					var playersAttr = table.getAttribute("players");
					var gameType = table.getAttribute("gameType");
					var formatName = table.getAttribute("format");
					var tournamentName = table.getAttribute("tournament");
					var userDesc = table.getAttribute("userDescription");
					var isPrivate = (table.getAttribute("isPrivate") === "true");
					var isInviteOnly = (table.getAttribute("isInviteOnly") === "true");
					var inviteForYou = isInviteOnly && userDesc === chat.userName;
					var players = new Array();
					if (playersAttr.length > 0)
						players = playersAttr.split(",");
					var playing = table.getAttribute("playing");
					var winner = table.getAttribute("winner");

					var row = $("<tr class='table" + id + "'></tr>");

					row.append("<td>" + formatName + "</td>");
					var name = "<td>" + tournamentName;
					if(isPrivate) 
					{
						if(!!userDesc)
						{
							if(isInviteOnly)
							{
								name += " - <i>Private match for user '" + userDesc + "'.";
							}
							else 
							{
								name += " - <i>Private match: [" + userDesc + "]</i>";
							}
						}
						else {
							name += " - <i>Private.</i>";
						}
					}
					else 
					{
						if(!!userDesc)
						{
							if(isInviteOnly)
							{
								name += " - <i>Match for user '" + userDesc + "'.";
							}
							else 
							{
								name += " - <i>[" + userDesc + "]</i>";
							}
						}
					}
					
					name += "</td>";
					row.append(name);
					row.append("<td>" + statusDescription + "</td>");

					var playersStr = "";
					for (var playerI = 0; playerI < players.length; playerI++) {
						if (playerI > 0)
							playersStr += ", ";
						playersStr += players[playerI];
					}
					row.append("<td>" + playersStr + "</td>");

					var lastField = $("<td></td>");
					if (status == "WAITING") {
						if (playing == "true") {
							var that = this;

							var but = $("<button>Leave Table</button>");
							$(but).button().click((
								function(tableId) {
									return function() {
										that.comm.leaveTable(tableId);
									};
								})(id));
							lastField.append(but);
						} 
						else if(!isInviteOnly || inviteForYou) {
							var that = this;

							var but = $("<button>Join Table</button>");
							$(but).button().click((
								function(tableId) {
									return function() {
										var deck = that.decksSelect.val();
										if (deck != null)
											that.comm.joinTable(tableId, deck, function (xml) {
												that.processResponse(xml);
											});
									};
								})(id));
							lastField.append(but);
						}
					} else if (status == "PLAYING") {
						if (playing == "true") {
							var participantId = getUrlParam("participantId");
							var participantIdAppend = "";
							if (participantId != null)
								participantIdAppend = "&participantId=" + participantId;

							var but = $("<button>Play Match</button>");
							var link = $("<a href='game.html?gameType=" + gameType + "&gameId=" + gameId + participantIdAppend + "'></a>");
							link.append(but);
							but.button();
							lastField.append(link);
						} else if (watchable == "true") {
							var participantId = getUrlParam("participantId");
							var participantIdAppend = "";
							if (participantId != null)
								participantIdAppend = "&participantId=" + participantId;

							var but = $("<button>Spectate</button>");
							var link = $("<a target='_blank' href='game.html?gameType=" + gameType + "&gameId=" + gameId + participantIdAppend + "'></a>");
							link.append(but);
							but.button();
							lastField.append(link);
						}
					} else if (status == "FINISHED") {
						if (winner != null) {
							lastField.append(winner);
						}
					}

					row.append(lastField);

					if (action == "add") {
						if (status == "WAITING") {
							$("table.waitingTables", this.tablesDiv)
								.append(row);
						} else if (status == "PLAYING") {
							$("table.playingTables", this.tablesDiv)
								.append(row);
						} else if (status == "FINISHED") {
							$("table.finishedTables", this.tablesDiv)
								.append(row);
						}
					} else if (action == "update") {
						if (status == "WAITING") {
							if ($(".table" + id, $("table.waitingTables")).length > 0) {
								$(".table" + id, this.tablesDiv).replaceWith(row);
							} else {
								$(".table" + id, this.tablesDiv).remove();
								$("table.waitingTables", this.tablesDiv)
									.append(row);
							}
						} else if (status == "PLAYING") {
							if ($(".table" + id, $("table.playingTables")).length > 0) {
								$(".table" + id, this.tablesDiv).replaceWith(row);
							} else {
								$(".table" + id, this.tablesDiv).remove();
								$("table.playingTables", this.tablesDiv)
									.append(row);
							}
						} else if (status == "FINISHED") {
							if ($(".table" + id, $("table.finishedTables")).length > 0) {
								$(".table" + id, this.tablesDiv).replaceWith(row);
							} else {
								$(".table" + id, this.tablesDiv).remove();
								$("table.finishedTables", this.tablesDiv)
									.append(row);
							}
						}

						this.animateRowUpdate(".table" + id);
					}

					if (playing == "true")
						row.addClass("played");
					
					if(inviteForYou)
						row.addClass("privateForPlayer");
				} else if (action == "remove") {
					$(".table" + id, this.tablesDiv).remove();
				}
			}

			$(".count", $(".eventHeader.queues")).html("(" + ($("tr", $("table.queues")).length - 1) + ")");
			$(".count", $(".eventHeader.tournaments")).html("(" + ($("tr", $("table.tournaments")).length - 1) + ")");
			$(".count", $(".eventHeader.waitingTables")).html("(" + ($("tr", $("table.waitingTables")).length - 1) + ")");
			$(".count", $(".eventHeader.playingTables")).html("(" + ($("tr", $("table.playingTables")).length - 1) + ")");
			$(".count", $(".eventHeader.finishedTables")).html("(" + ($("tr", $("table.finishedTables")).length - 1) + ")");

			var games = root.getElementsByTagName("newGame");
			for (var i=0; i<games.length; i++) {
				var waitingGameId = games[i].getAttribute("id");
				var participantId = getUrlParam("participantId");
				var participantIdAppend = "";
				if (participantId != null)
					participantIdAppend = "&participantId=" + participantId;
				window.open("/gemp-module/game.html?gameType=" + gameType + "&gameId=" + waitingGameId + participantIdAppend, "_blank");
			}
			if (games.length > 0) {
			        // Turned off this sound because it was annoying :)
//				this.PlaySound("gamestart");
			}

			if (!this.supportedFormatsInitialized) {
				var formats = root.getElementsByTagName("format");
				for (var i = 0; i < formats.length; i++) {
					var format = formats[i].childNodes[0].nodeValue;
					var type = formats[i].getAttribute("type");
					
					var item = "<option value='" + type + "'>" + format + "</option>"
					
					this.supportedFormatsSelect.append(item);
				}
				this.supportedFormatsInitialized = true;
			}

			that.createTableButton.removeAttr("disabled");
			that.createTableButton.removeClass("ui-state-disabled")

			setTimeout(function () {
				that.updateHall();
			}, 100);
		}
	}
}
