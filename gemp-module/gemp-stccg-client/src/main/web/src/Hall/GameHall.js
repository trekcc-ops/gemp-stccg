import ChatBoxUI from "../../js/gemp-022/chat.js";
import GempHallUI from "../../js/gemp-022/hallUi.js";
import GameHistoryUI from "../../js/gemp-022/gameHistoryUi.js";
import PlayerStatsUI from "../../js/gemp-022/playerStatsUi.js";
import StatsUI from "../../js/gemp-022/statsUi.js";
import LeagueResultsUI from "../../js/gemp-022/leagueResultsUi.js";
import { gatherData, sortOptionsByName, leagueErrorMap } from "../../js/gemp-022/leagueAdmin.js";
import { susUserPopulate, banErrorMap } from "../../js/gemp-022/manage.js";
import TournamentResultsUI from "../../js/gemp-022/tournamentResultsUi.js";
import { formatPrice } from "../../js/gemp-022/common.js";

var chat;
var hall;

$(document).ready(function () {

	$("#main").tabs();
	
	let chat = new ChatBoxUI("Game Hall", $("#chat"), "/gemp-stccg-server", true, null, false, null, true);
	chat.showTimestamps = true;
	
	hall = new GempHallUI("/gemp-stccg-server", chat);


	var infoDialog = $("<div></div>")
			.dialog({
				autoOpen:false,
				closeOnEscape:true,
				resizable:false,
				title:"Card information",
				closeText: ""
			});

	$("body").click(
		function (event) {
			var tar = $(event.target);

			if (tar.hasClass("cardHint")) {
				var ids = tar.attr("value").split(",");
				
				infoDialog.html("");
				infoDialog.html("<div style='scroll: auto'></div>");
				var floatCardDiv = $("<div style='float: left; display:flex; gap: 5px;'></div>");
				
				var horiz = false;
				for(var i = 0; i < ids.length; i++) {
					var blueprintId = ids[i];
					var card = new Card(blueprintId, "SPECIAL", "hint", ""); // TODO - Card missing imageUrl and locationIndex
					horiz = horiz || card.horizontal;
					floatCardDiv.append(createFullCardDiv(card.imageUrl, card.foil, card.horizontal));
				}
				
				infoDialog.append(floatCardDiv);

				var windowWidth = $(window).width();
				var windowHeight = $(window).height();

				var horSpace = 30;
				var vertSpace = 45;
				var height = 505;
				var width = 340;

				infoDialog.dialog({
					title:"Card information",
					
				});
				if (horiz) {
					// 500x360
					infoDialog.dialog({width:Math.min((height + 10) * ids.length, windowWidth), 
										height:Math.min((width + 90), windowHeight)});
				} else {
					// 360x500
					infoDialog.dialog({width:Math.min((width + 30) * ids.length, windowWidth), 
										height:Math.min((height + 45), windowHeight)});
				}
				infoDialog.dialog("open");

				event.stopPropagation();
				return false;
			} else if (tar.hasClass("prizeHint")) {
				var prizeDescription = tar.attr("value");

				infoDialog.text(prizeDescription);

				infoDialog.dialog({title:"Prizes details", width:300, height: 150});
				infoDialog.dialog("open");

				event.stopPropagation();
				return false;
			}

			return true;
		});

		$("#main").on("tabsload", function(event, ui) {
			let selected_tab_num = $( "#main" ).tabs( "option", "active" );
			switch(selected_tab_num) {
				case 0:
					// Game hall
					break;
				case 1:
					// Help
					$("#helpMain").tabs().addClass( "ui-tabs-vertical ui-helper-clearfix" );
					$("#help-tabs li").removeClass("ui-corner-top").addClass("ui-corner-left");

					let selected_help_tab_num = $("#helpMain").tabs("option", "active");
					switch(selected_help_tab_num) {
						case 0:
							// How do I play?
							break;
						case 1:
							// Code of Conduct
							break;
						case 2:
							// Format rules
							hall.comm.getFormatRules(
								function(json){
									$("#formatRules").html(json);
								},
								hall.hallErrorMap()
							);
							break;
						case 3:
							// League rules
							break;
						case 4:
							// PC Eratta
							// BUG: This seems to throw a 500 server error;
							//      possibly related to missing cards or wrong errata type.
							//      At the very least, the json passed back is empty.
							hall.comm.getErrata(
								function(json){
									//$("#errata-readout").text(JSON.stringify(json, null, 2));
									
									var top = $("<div style='display:flex; flex-direction:column; gap:20px; justify-content:center; align-items:center;'></div>");
									
									var results = {};
									for(var bp in json) {
										var info = json[bp];
										var eid = info.ErrataIDs.PC;
										var text = info.LinkText;
										text = text.replace(eid, bp + "," + eid);
										//console.log(text);
										//result.append(text);
										
										var parts = bp.split("_");
										var setID = parseInt(parts[0]);
										var cardID = parseInt(parts[1]);
										
										if(setID in results) {
											results[setID][cardID] = text;
										}
										else {
											results[setID] = {};
											results[setID][cardID] = text;
										}
									}
									//debugger;
									var collator = new Intl.Collator(undefined, {numeric: true, sensitivity: 'base'});
									var sets = Object.keys(results).sort(collator.compare);
									
									for(var set in sets) {
										var setdiv = $("<div style='display:flex; flex-direction:column; gap:10px; flex-wrap: wrap; width:70%; row-gap:10px; column-gap:30px; '></div>");
										top.append($("<div style='margin:auto; font-size: 140%; position:relative; top:20px; bottom:10px;'>Set " + sets[set] + "</div>"));
										
										//(# * 28) / 2
										//debugger;
										var cards = Object.keys(results[sets[set]]).sort(collator.compare);
										for(var card in cards) {
											var cardspan = $("<span></span>");
											
											var html = results[sets[set]][cards[card]];
											cardspan.append(results[sets[set]][cards[card]]);
											
											setdiv.append(cardspan);
										}
					
										
										setdiv.css({"height": "" + (((cards.length * 28) / 1.8) + 30) + "px"});
										
										top.append(setdiv);
									}
									
									
									$("#errata-readout").html(top);
								},
								hall.hallErrorMap());
							break;
						default:
							console.error("helpMain.tabs(): Unknown selected tab number" + selected_help_tab_num);
					}
					break;
				case 2:
					// Events
					$("#eventsMain").tabs().addClass("ui-tabs-vertical ui-helper-clearfix");
					$("#event-tabs > ol > li").removeClass("ui-corner-top").addClass("ui-corner-left");
					
					let leagueUI = new LeagueResultsUI("/gemp-stccg-server");
					
					let tourneyUI = new TournamentResultsUI("/gemp-stccg-server");
		
					$(".loadFinishedTournaments").button().click(
						function() {
							tourneyUI.loadHistoryTournaments();
						}
					);
					break;
				case 3:
					// Server info
					$("#infoMain").tabs().addClass("ui-tabs-vertical ui-helper-clearfix");
					$("#info-tabs > ol > li").removeClass("ui-corner-top").addClass("ui-corner-left");
					
					var ui = new StatsUI("/gemp-stccg-server", $("#statsParameters"), $("#stats"));
					$(".getStats").click();
					break;
				case 4:
					// My account
					$("#accountMain").tabs().addClass("ui-tabs-vertical ui-helper-clearfix");
					$("#account-tabs > ol > li").removeClass("ui-corner-top").addClass("ui-corner-left");
					$("#accountName").html(chat.userName);
					$("#pocketDiv").html(formatPrice(hall.pocketValue));

					let selected_acct_tab_num = $("#accountMain").tabs("option", "active");
					switch(selected_acct_tab_num) {
						case 0:
							// My Account
							console.log("My account");
							break;
						case 1:
							// Game History
							console.log("Game history");
							var ui = new GameHistoryUI("/gemp-stccg-server");
							break;
						case 2:
							// My Stats
							console.log("My stats");
							var ui = new PlayerStatsUI("/gemp-stccg-server");
							break;
						case 3:
							// Playtesting
							console.log("Playtesting");
							function drawTable(json) {

								const template = {"<>":"tr","html": [
									{"<>":"td","html":"${format_name}"},
									{"<>":"td","html":"${winner}"},
									{"<>":"td","html":"${loser}"},
									{"<>":"td","html":"${win_reason}"},
									{"<>":"td","html":"${lose_reason}"},
									{"<>":"td","html":function() {
										return "<a href=\"" + window.location.origin + "/gemp-module/game.html?replayId=" + this.winner + "$" + this.win_recording_id + "\">Replay link</a>";
										}},
									{"<>":"td","html":function() {
										return "<a href=\"" + window.location.origin + "/gemp-module/game.html?replayId=" + this.loser + "$" + this.lose_recording_id + "\">Replay link</a>";
										}},
									{"<>":"td","html":function() { return new Date(this.start_date).toLocaleString('sv'); }},
									{"<>":"td","html":function() { return new Date(this.end_date).toLocaleString('sv'); }},
								]};
								
									   
								$("#tblReplays").json2html(json,template);
							}
							
							
							hall.comm.getRecentReplays("PLAYTEST", 100, function(json){drawTable(json);}, hall.hallErrorMap());
							drawTable();
							break;
						default:
							console.error("accountMain.tabs(): Unknown selected tab number" + selected_acct_tab_num);
					}

					break;
				case 5:
					// Admin
					$("#adminMain").tabs().addClass("ui-tabs-vertical ui-helper-clearfix");
					$("#admin-tabs > ol > li").removeClass("ui-corner-top").addClass("ui-corner-left");
					
					$("#landingTab").parent().hide();
					$("#landing").hide();
					
					if(!hall.userInfo.type.includes("a"))
					{
						$("#generalTab").parent().hide();
						$("#banTab").parent().hide();
					}
					/*
					if(!hall.userInfo.type.includes("l"))
					{
						$("#leagueTab").parent().hide();
					}
					*/

					let selected_admin_tab = $("#adminMain").tabs("option", "active");
					switch(selected_admin_tab) {
						case 0:
							// Empty landing page
							// console.log("0");
							break;
						case 1:
							// General Admin
							$("#shutdown-button").button().click(
								function () {
									let execute = confirm("Are you sure you want to enter shutdown mode?	This will cancel all currently waiting tables and send a site-wide chat alert informing players the server will restart soon.	(Currently playing tables will be unaffected.)");
									
									if(!execute)
										return;
									
									$("#shutdown-response").html("Processing...");
										
									hall.comm.setShutdownMode(true, function (string) {
										$("#shutdown-response").html(string);
									});
								});
							
							
							$("#cancel-shutdown-button").button().click(
								function () {
									$("#shutdown-response").html("Processing...");
									
									hall.comm.setShutdownMode(false, function (string) {
										$("#shutdown-response").html(string);
									});
								});
							
							$("#clear-cache-button").button().click(
								function () {
									$("#cache-response").html("Processing...");
									
									hall.comm.clearServerCache(function (string) {
										$("#cache-response").html(string);
									});
								});
							
							$("#reload-cards-button").button().click(
								function () {
									$("#cards-response").html("Processing...");
									
									hall.comm.reloadCardDefinitions(function (string) {
										$("#cards-response").html(string);
									});
								});
							
							$("#motd-button").button().click(
								function () {
									$("#motd-response").html("Processing...");
									
									hall.comm.setDailyMessage($("#motd-text").val(), function (string) {
										$("#motd-response").html("Response: " + string);
									});
								});
							
							hall.comm.getDailyMessage(function (json) {
								$("#motd-text").val(json.response);
								$("#motd-preview").html(json.response);
							});
							
							
							$("#add-items-button").button().click(
								function () {
									let execute = confirm("Are you sure you want to issue these items in these quantities to these players in that collection? THIS CANNOT BE UNDONE, EVEN MANUALLY.");
									
									if(!execute)
										return;
									
									$("#add-items-response").html("Processing...");
									
									hall.comm.addItems($("#collectionType").val(), $("#product").val(), $("#players").val(), function (string) {
										$("#add-items-response").html("Response: " + string);
									});
								});
							break;
						case 2:
							// League admin
							// TODO: Move all of this elsewhere and import.
							var previewDialog = $("<div></div>").dialog({
									autoOpen:false,
									closeOnEscape:true,
									resizable:true,
									modal:true,
									title:"Preview window",
									closeText: ''
								});

							var displayPreview = function (xml) {
								var root = xml.documentElement;
								if(root == null)
								{
									xml = new DOMParser().parseFromString(xml,"text/xml");
									root = xml.documentElement;
								}
								if (root.tagName == 'league') {
									var league = root;

									var leagueName = league.getAttribute("name");
									var cost = parseInt(league.getAttribute("cost"));

									previewDialog.append("<div class='leagueName'>" + leagueName + "</div>");

									var costStr = formatPrice(cost);
									previewDialog.append("<div class='leagueCost'><b>Cost:</b> " + costStr + "</div>");

									var series = league.getElementsByTagName("series");
									for (var j = 0; j < series.length; j++) {

										var serie = series[j];
										var serieName = serie.getAttribute("type");
										var serieStart = serie.getAttribute("start");
										var serieEnd = serie.getAttribute("end");
										var maxMatches = serie.getAttribute("maxMatches");
										var format = serie.getAttribute("format");
										var collection = serie.getAttribute("collection");
										var limited = serie.getAttribute("limited");

										var serieText = serieName + " - " + getDateString(serieStart) + " to " + getDateString(serieEnd);
										previewDialog.append("<div class='serieName'>" + serieText + "</div>");

										previewDialog.append("<div><b>Format:</b> " + ((limited == "true") ? "Limited" : "Constructed") + " " + format + "</div>");
										previewDialog.append("<div><b>Collection:</b> " + collection + "</div>");

										previewDialog.append("<div>Maximum ranked matches in serie: " + maxMatches + "</div>");
									}
								}
							};
		
							var now = new Date();
							var nowStr = "" + now.getFullYear() + String(1 + now.getMonth()).padStart(2, '0') + String(now.getDate()).padStart(2, '0');
							$("#sealed-start").val(nowStr);
							$("#solo-draft-start").val(nowStr);
							$("#constructed-start").val(nowStr);

							var previewError = function (xhr) {
								previewDialog.dialog("close");
								alert("Invalid parameters specified - error code: " + xhr.status);
							};
		
							$("#preview-sealed-league-button").button().click(
								function () {
									let resultdiv = $("#sealed-league-result");
									resultdiv.html("Processing...");
									
									hall.comm.previewSealedLeague(
										$("#sealed-format").val(), 
										$("#sealed-start").val(), 
										$("#sealed-duration").val(),
										$("#sealed-matches").val(),
										$("#sealed-name").val(),
										$("#sealed-cost").val(), 
										function (xml) {
											previewDialog.html("");
											displayPreview(xml);
											resultdiv.html("OK");
											previewDialog.dialog("open");
										}, leagueErrorMap(resultdiv));
									});
		
							$("#add-sealed-league-button").button().click(
								function () {
									let resultdiv = $("#sealed-league-result");
									resultdiv.html("Processing...");
									
									hall.comm.addSealedLeague(
										$("#sealed-format").val(), 
										$("#sealed-start").val(), 
										$("#sealed-duration").val(),
										$("#sealed-matches").val(),
										$("#sealed-name").val(),
										$("#sealed-cost").val(), 
										function (xml) {
											resultdiv.html("OK");
										}, leagueErrorMap(resultdiv));
									});
		
		
							$("#preview-solo-draft-league-button").button().click(
								function () {
									let resultdiv = $("#solo-draft-league-result");
									resultdiv.html("Processing...");
						
									hall.comm.previewSoloDraftLeague(
										$("#solo-draft-format").val(), 
										$("#solo-draft-start").val(), 
										$("#solo-draft-duration").val(),
										$("#solo-draft-matches").val(),
										$("#solo-draft-name").val(),
										$("#solo-draft-cost").val(), 
										function (xml) {
											previewDialog.html("");
											displayPreview(xml);
											resultdiv.html("OK");
											previewDialog.dialog("open");
										}, leagueErrorMap(resultdiv));
									});
		
							$("#add-solo-draft-league-button").button().click(
								function () {
									let resultdiv = $("#solo-draft-league-result");
									resultdiv.html("Processing...");
									
									hall.comm.addSoloDraftLeague(
										$("#solo-draft-format").val(), 
										$("#solo-draft-start").val(), 
										$("#solo-draft-duration").val(),
										$("#solo-draft-matches").val(),
										$("#solo-draft-name").val(),
										$("#solo-draft-cost").val(), 
										function (xml) {
											resultdiv.html("OK");
										}, leagueErrorMap(resultdiv));
									});
		
							$("#preview-constructed-league-button").button().click(
								function () {
									let resultdiv = $("#constructed-league-result");
									resultdiv.html("Processing...");
									
									var data = gatherData($(".series"))
									
									hall.comm.previewConstructedLeague(
										$("#constructed-start").val(), 
										$("#constructed-collection").val(),
										$("#constructed-prize").val(),
										$("#constructed-name").val(),
										$("#constructed-cost").val(),
										data.format,
										data.seriesDuration,
										data.maxMatches,
										function (xml) {
											previewDialog.html("");
											displayPreview(xml);
											resultdiv.html("OK");
											previewDialog.dialog("open");
										}, leagueErrorMap(resultdiv));
									});
		
							$("#add-constructed-league-button").button().click(
								function () {
									let resultdiv = $("#constructed-league-result");
									resultdiv.html("Processing...");
									
									var data = gatherData($(".series"))
									
									hall.comm.addConstructedLeague(
										$("#constructed-start").val(), 
										$("#constructed-collection").val(),
										$("#constructed-prize").val(),
										$("#constructed-name").val(),
										$("#constructed-cost").val(),
										data.format,
										data.seriesDuration,
										data.maxMatches,
										function (xml) {
											resultdiv.html("OK");
										}, leagueErrorMap(resultdiv));
									});


							$("#previewConstructed").click(
									function () {
										submitFormToAddress("/gemp-stccg-server/admin/previewConstructedLeague", $("#addConstructedForm").eq(0), displayPreview, previewError);
										previewDialog.html("");
										previewDialog.dialog("open");
									});

							$("#add-constructed-series-button").button().click(
									function () {
										$(".serieData").last().clone().appendTo(".series");
									});
							
							// BUG: getFormats causes a 500 server error, probably due to LOTR decks or something.
							hall.comm.getFormats(true,
								function (json) 
								{
									//console.log(json);
									let drafts = json.DraftTemplates;
									let formats = json.Formats;
									let sealed = json.SealedTemplates
									//console.log(drafts);
									for (var prop in drafts) {
										if (Object.prototype.hasOwnProperty.call(drafts, prop)) {
											//console.log(prop);
											
											var item = $("<option/>")
												.attr("value", prop)
												.text(prop);
											$("#solo-draft-format").append(item);
										}
									}
									sortOptionsByName("#solo-draft-format");
									
									//console.log(formats);
									for (var prop in formats) {
										if (Object.prototype.hasOwnProperty.call(formats, prop)) {
											//console.log(prop);
											
											if(formats[prop].name.includes("Limited"))
												continue;
											
											let num = ("0000" + formats[prop].order).substr(-4);
											
											var item = $("<option/>")
												.attr("value", prop)
												.text("" + num + " - " + formats[prop].name);
											$("#constructed-format").append(item);
										}
									}
									sortOptionsByName("#constructed-format");
									$("#constructed-format option").each(function(index) {
										//console.log(this);
										let newText = $(this).text().replace(/\d+ - /, '');
										//console.log(newText);
										$(this).text(newText);
									});
									
									//console.log(sealed);
									for (var prop in sealed) {
										if (Object.prototype.hasOwnProperty.call(sealed, prop)) {
											//console.log(prop);
											
											let code = sealed[prop].Format;
											let id = sealed[prop].ID;
											let serieCount = sealed[prop].SeriesProduct.length;
											
											var item = $("<option/>")
												.attr("value", id)
												.text(prop + " - " + serieCount + " Series");
											$("#sealed-format").append(item);
										}
									}
									sortOptionsByName("#sealed-format");
								}, 
								{
									"400":function () 
									{
										alert("Could not retrieve formats.");
									}
								});
							break;
						case 3:
							// Manage Users
							console.log("Manage users");
							$("#reset-button").button().click(
								function () {
									let execute = confirm("Are you sure you want to reset the password for '" + $("#reset-input").val() + "'?  This action cannot be undone.");
										
									if(!execute)
										return;
										
									let resultdiv = $("#reset-result");
									resultdiv.html("Processing...");
									
									hall.comm.resetUserPassword($("#reset-input").val(), function (string) {
										resultdiv.html(string);
									}, banErrorMap(resultdiv));
								});
							
							$("#permaban-button").button().click(
								function () {
									let resultdiv = $("#permaban-result");
									resultdiv.html("Processing...");
									
									hall.comm.permabanUser($("#permaban-input").val(), function (string) {
										resultdiv.html(string);
									}, banErrorMap(resultdiv));
								});
							
							$("#tempban-button").button().click(
								function () {
									let resultdiv = $("#tempban-result");
									resultdiv.html("Processing...");
									
									hall.comm.tempbanUser($("#tempban-input").val(), $("#temp-ban-duration-select").val(), function (string) {
										resultdiv.html(string);
									}, banErrorMap(resultdiv));
								});
							
							$("#unban-button").button().click(
								function () {
									let resultdiv = $("#unban-result");
									resultdiv.html("Processing...");
									
									hall.comm.unbanUser($("#unban-input").val(), function (string) {
										resultdiv.html(string);
									}, banErrorMap(resultdiv));
								});
							
							$("#sus-button").button().click(
								function () {
									let resultdiv = $("#sus-result");
									resultdiv.html("Processing...");
									
									hall.comm.susUserSearch($("#sus-input").val(), susUserPopulate, banErrorMap(resultdiv), function() {
										$("#displayUsers").hide();
									});
								});
							break;
						default:
							console.error("adminMain.tabs(): Unknown selected tab number" + selected_admin_tab);
							break;
					}
					break;
				default:
					console.error("main.tabs(): Unknown selected tab number" + selected_tab_num);
			}
		});
});