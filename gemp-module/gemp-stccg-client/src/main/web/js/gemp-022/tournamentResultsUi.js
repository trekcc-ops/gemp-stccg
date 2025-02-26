import GempClientCommunication from "./communication.js";

export default class TournamentResultsUI {
    communication;
    formatDialog;

    constructor(url) {
        this.communication = new GempClientCommunication(url,
            function (xhr, ajaxOptions, thrownError) {
            });

        this.formatDialog = $("<div></div>")
            .dialog({
                autoOpen:false,
                closeOnEscape:true,
                resizable:false,
                modal:true,
                title:"Format description"
            });

        this.loadLiveTournaments();
    }

    loadLiveTournaments() {
        var that = this;
        this.communication.getLiveTournaments(
            function (json) {
                that.loadedTournaments(json);
            });
    }

    loadHistoryTournaments() {
        var that = this;
        this.communication.getHistoryTournaments(
            function (json) {
                that.loadedTournaments(json);
            });
    }

    loadedTournament(json) {
        var that = this;
        $("#tournamentExtraInfo").html("");

        let tournamentId = json.tournamentId;
        let tournamentName = json.tournamentName;
        let tournamentFormat = json.formatName;
        let tournamentCollection = json.collectionName;
        let tournamentRound = json.currentRound;
        let tournamentStage = json.currentStage;
        let standings = json.standings;

        $("#tournamentExtraInfo").append("<div class='tournamentName'>" + tournamentName + "</div>");
        $("#tournamentExtraInfo").append("<div class='tournamentFormat'><b>Format:</b> " + tournamentFormat + "</div>");
        $("#tournamentExtraInfo").append("<div class='tournamentCollection'><b>Collection:</b> " + tournamentCollection + "</div>");
        if (tournamentStage == "Playing games")
            $("#tournamentExtraInfo").append("<div class='tournamentRound'><b>Round:</b> " + tournamentRound + "</div>");

        if (standings.length > 0)
            $("#tournamentExtraInfo").append(this.createStandingsTable(standings, tournamentId, tournamentStage));
    }

    loadedTournaments(json) {
        var that = this;
        $("#tournamentResults").html("");
        let tournaments = json;

        for (let i = 0; i < tournaments.length; i++) {
            let tournament = tournaments[i];
            let tournamentId = tournament.tournamentId;
            let tournamentName = tournament.tournamentName;
            let tournamentFormat = tournament.formatName;
            let tournamentCollection = tournament.collectionName;
            let tournamentRound = tournament.currentRound;
            let tournamentStage = tournament.currentStage;

            $("#tournamentResults").append("<div class='tournamentName'>" + tournamentName + "</div>");
            $("#tournamentResults").append("<div class='tournamentRound'><b>Round:</b> " + tournamentRound + "</div>");

            var detailsBut = $("<button>See details</button>").button();
            detailsBut.click(
                (function (tournament) {
                    return function () {
                        that.loadedTournament(tournament);
                    };
                })(tournament));
            $("#tournamentResults").append(detailsBut);
        }
        if (tournaments.length == 0)
            $("#tournamentResults").append("<i>There is no running tournaments at the moment</i>");

        $("#tournamentResults").append("<hr />");
        $("#tournamentResults").append("<div id='tournamentExtraInfo'></div>");
    }

    createStandingsTable(standings, tournamentId, tournamentStage) {
        let standingsTable = $("<table class='standings'></table>");

        standingsTable.append("<tr><th>Standing</th><th>Player</th><th>Points</th><th>Games played</th><th>Opp. Win %</th><th></th><th>Standing</th><th>Player</th><th>Points</th><th>Games played</th><th>Opp. Win %</th></tr>");

        let secondColumnBaseIndex = Math.ceil(standings.length / 2);

        for (let k = 0; k < standings.length; k++) {
            let standing = standings[k];
            let currentStanding = standing.currentStanding;
            let player = standing.player;
            let points = standing.points;
            let gamesPlayed = standing.gamesPlayed;
            let opponentWinPerc = standing.opponentWin;

            if (k < secondColumnBaseIndex) {
               standingsTable.append("<tr><td>" + currentStanding + "</td><td>" + player + "</td><td>" + points + "</td><td>" + gamesPlayed + "</td><td>" + opponentWinPerc + "</td></tr>");
            } else {
                $("tr:eq(" + (k - secondColumnBaseIndex + 1) + ")", standingsTable).append("<td></td><td>" + currentStanding + "</td><td>" + player + "</td><td>" + points + "</td><td>" + gamesPlayed + "</td><td>" + opponentWinPerc + "</td>");
            }
        }

        return standingsTable;
    }
}