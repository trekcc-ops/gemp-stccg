import { getUrlParam } from "../../js/gemp-022/common.js";
import GameTableUI from "../../js/gemp-022/gameUi.js";
import { TribblesGameTableUI, ST1EGameTableUI } from "../../js/gemp-022/gameUi.js";

var ui;
var communication;

$(document).ready(
    function () {
        var replay = getUrlParam("replayId");
        var gameType = getUrlParam("gameType");

        if (gameType == "tribbles") {
            ui = new TribblesGameTableUI("/gemp-stccg-server", replay != null);
        } else if (gameType == "st1e") {
            ui = new ST1EGameTableUI("/gemp-stccg-server", replay != null);
        } else {
            ui = new GameTableUI("/gemp-stccg-server", replay != null);
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