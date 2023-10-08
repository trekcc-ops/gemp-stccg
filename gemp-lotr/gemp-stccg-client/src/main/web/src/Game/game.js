var ui;
var communication;

$(document).ready(
    function () {
        var replay = getUrlParam("replayId");
        var gameType = getUrlParam("gameType");

        if (gameType == "tribbles") {
            ui = new TribblesGameTableUI("/gemp-lotr-server", replay != null);
        } else if (gameType == "st1e") {
            ui = new ST1EGameTableUI("/gemp-lotr-server", replay != null);
        } else {
            ui = new GameTableUI("/gemp-lotr-server", replay != null);
        }

        $(window).resize(function () {
            ui.windowResized();
        });

        ui.layoutUI(true);

        if (replay == null)
            ui.startGameSession();
        else
            ui.startReplaySession(replay);
    });