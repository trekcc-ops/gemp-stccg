var LeagueResultsUI = Class.extend({
    communication: null,

    init: function(url) {
        this.communication = new GempLotrCommunication(url,
                function(xhr, ajaxOptions, thrownError) {
                });
        this.loadResults();
    },

    loadResults: function() {
        var that = this;
        this.communication.getLeagues(
                function(xml) {
                    that.loadedLeagueResults(xml);
                });
    },

    getDateString: function(date) {
        return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
    },

    loadedLeagueResults: function(xml) {
        log(xml);
        var root = xml.documentElement;
        if (root.tagName == 'leagues') {
            $("#leagueResults").html("");

            var leagues = root.getElementsByTagName("league");
            for (var i = 0; i < leagues.length; i++) {
                var league = leagues[i];
                var leagueName = league.getAttribute("name");
                var start = league.getAttribute("start");
                var end = league.getAttribute("end");

                var leagueText = leagueName + " - " + this.getDateString(start) + " to " + this.getDateString(end);
                $("#leagueResults").append("<h1 class='leagueName'>" + leagueText + "</h1>");

                var series = league.getElementsByTagName("serie");
                for (var j = 0; j < series.length; j++) {
                    var serie = series[j];
                    var serieName = serie.getAttribute("type");
                    var serieStart = serie.getAttribute("start");
                    var serieEnd = serie.getAttribute("end");

                    var serieText = serieName + " - " + this.getDateString(serieStart) + " to " + this.getDateString(serieEnd);
                    $("#leagueResults").append("<h2 class='serieName'>" + serieText + "</h2>");

                    var standings = serie.getElementsByTagName("standing");
                    if (standings.length > 0) {
                        var standingsTable = $("<table class='standings'></table>");

                        standingsTable.append("<tr><th>Player</th><th>Points</th></tr>");

                        for (var k = 0; k < standings.length; k++) {
                            var standing = standings[k];
                            var player = standing.getAttribute("player");
                            var points = standing.getAttribute("points");

                            standingsTable.append("<tr><td>" + player + "</td><td>" + points + "</td></tr>");
                        }

                        $("#leagueResults").append(standingsTable);
                    }
                }
            }
        }
    }
});