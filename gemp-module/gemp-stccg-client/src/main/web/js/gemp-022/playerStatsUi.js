import GempClientCommunication from "./communication.js";

export default class PlayerStatsUI {
    communication;

    constructor(url) {
        this.communication = new GempClientCommunication(url,
            function (xhr, ajaxOptions, thrownError) {
            });

        this.loadPlayerStats();
    }

    loadPlayerStats() {
        var that = this;
        this.communication.getPlayerStats(
            function (json) {
                that.loadedPlayerStats(json);
            });
    }

    loadedPlayerStats(json) {
        $("#playerStats").html("");

        $("#playerStats").append("<div class='playerStatHeader'>Casual statistics</div>");
        this.appendStats(json.casual);
        $("#playerStats").append("<div class='playerStatHeader'>Competitive statistics</div>");
        this.appendStats(json.competitive);
    }

    appendStats(jsonStats) {
        if (jsonStats.length == 0) {
            $("#playerStats").append("<i>You have not played any games counting for this statistics</i>");
        } else {
            var table = $("<table class='tables'></table>");
            table.append("<tr><th>Format name</th><th>Deck name</th><th># of wins</th><th># of losses</th><th>% of wins</th></tr>");
            for (var i = 0; i < jsonStats.length; i++) {
                let entry = jsonStats[i];
                let format = entry.format;
                let deckName = entry.deckName;
                let wins = entry.wins;
                let losses = entry.losses;
                let winPercentage = ((wins / (wins + losses)) * 100).toFixed(1) + "%";


                table.append("<tr><td>" + format + "</td><td>" + deckName + "</td><td>" + wins + "</td><td>" + losses + "</td><td>" + winPercentage + "</td></tr>");
            }

            $("#playerStats").append(table);
        }
    }
}