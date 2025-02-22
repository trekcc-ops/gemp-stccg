import "../../js/jquery/jquery-3.7.1.js";
import "../../js/jquery/jquery-ui-1.14.1/jquery-ui.js";
import { getUrlParam } from "../../js/gemp-022/common.js";
import GameTableUI from "../../js/gemp-022/gameUi.js";
import { TribblesGameTableUI, ST1EGameTableUI } from "../../js/gemp-022/gameUi.js";

var ui;
var communication;

document.addEventListener("DOMContentLoaded",
    function () {
        var replay = getUrlParam("replayId");
        var gameType = getUrlParam("gameType");

        if (gameType == "TRIBBLES") {
            ui = new TribblesGameTableUI("/gemp-stccg-server", replay != null);
        } else if (gameType == "FIRST_EDITION") {
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