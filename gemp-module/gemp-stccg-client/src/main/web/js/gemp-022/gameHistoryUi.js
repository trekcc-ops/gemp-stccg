import GempClientCommunication from "./communication.js";

export default class GameHistoryUI {
    communication;
    itemStart = 0;
    pageSize = 20;

    constructor(url) {
        this.communication = new GempClientCommunication(url,
            function (xhr, ajaxOptions, thrownError) {
            });
        this.loadHistory();
    }

    loadHistory() {
        var that = this;
        this.communication.getGameHistory(this.itemStart, this.pageSize,
            function (json) {
                that.loadedGameHistory(json);
            });
    }

    loadedGameHistory(json) {
        var historyTable = $("<table class='gameHistory'></table>");
        historyTable.append("<tr><th>Format</th><th>Tournament</th><th>Deck</th><th>Winner</th><th>Loser</th><th>Win reason</th><th>Lose reason</th><th>Finished on</th><th>Replay link</th></tr>");

        for (var i = 0; i < json.games.length; i++) {
            let historyEntry = json.games[i];
            let format = historyEntry.formatName;
            let tournament = historyEntry.tournament;
            let deck = historyEntry.deckName;
            let winner = historyEntry.winner;
            let loser = historyEntry.loser;
            let winReason = historyEntry.winReason;
            let loseReason = historyEntry.loseReason;
            let endTime = historyEntry.endTime;
            let gameRecordingId = historyEntry.gameRecordingId;
            let playerId = json.playerId;

            var row = $("<tr></tr>");
            if (format != null)
                row.append($("<td></td>").html(format));
            else
                row.append($("<td></td>").html("&nbsp;"));
            if (tournament != null)
                row.append($("<td></td>").html(tournament));
            else
                row.append($("<td></td>").html("&nbsp;"));
            if (deck != null)
                row.append($("<td></td>").html(deck));
            else
                row.append($("<td></td>").html("&nbsp;"));
            row.append($("<td></td>").html(winner));
            row.append($("<td></td>").html(loser));
            row.append($("<td></td>").html(winReason));
            row.append($("<td></td>").html(loseReason));
            row.append($("<td></td>").html(endTime));
/*            if (gameRecordingId != null) {
                var link = "game.html?replayId=" + playerId + "$" + gameRecordingId;
                var linkElem = $("<a>replay game</a>");
                linkElem.attr("href", link);
                row.append($("<td></td>").html(linkElem));
            } else {
                row.append($("<td></td>").html("<i>not stored</i>"));
            }*/
                // TODO - Commenting out the game recording links for now since they don't work
            row.append($("<td></td>").html("<i>not available</i>"));

            historyTable.append(row);
        }

        $("#gameHistory").append(historyTable);
    }
}