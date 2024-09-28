import { TribblesDeckBuildingUI, ST1EDeckBuildingUI } from "../../js/gemp-022/deckBuildingUi.js";

$(document).ready(
    function () {

        let gameType = prompt("Select the game you are building for. Type 'ST1E' or 'Tribbles'");
        if (gameType == "Tribbles") {
            var ui = new TribblesDeckBuildingUI();
        } else {
            var ui = new ST1EDeckBuildingUI();
        }

        $('body').layout({
            applyDefaultStyles:true,
            onresize:function () {
                ui.layoutUI(true);
            },
            east__minSize:350,
            east__maxSize:"50%"
        });

        $(".ui-layout-pane").css({"background-color":"#000000"});

        $(window).resize(function () {
            ui.layoutUI(true);
        });

        ui.layoutUI(true);
    });