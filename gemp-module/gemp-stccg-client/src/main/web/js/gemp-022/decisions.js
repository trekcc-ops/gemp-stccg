import { getCardDivFromId } from './jCards.js';

export function processDecision(decision, animate, gameUi) {
    $("#main").queue(
        function (next) {
            let decisionType = decision.decisionType;
            if (decisionType === "INTEGER") {
                gameUi.integerDecision(decision);
            } else if (decisionType === "MULTIPLE_CHOICE") {
                gameUi.multipleChoiceDecision(decision);
            } else if (decisionType === "ARBITRARY_CARDS") {
                gameUi.arbitraryCardsDecision(decision);
            } else if (decisionType === "ACTION_CHOICE") {
                actionChoiceDecision(decision, gameUi);
            } else if (decisionType === "CARD_ACTION_CHOICE") {
                cardActionChoiceDecision(decision, gameUi);
            } else if (decisionType === "CARD_SELECTION") {
                gameUi.cardSelectionDecision(decision);
            } else if (decisionType === "CARD_SELECTION_FROM_COMBINATIONS") {
                gameUi.cardSelectionFromCombinations(decision);
            }
            else {
                console.error(`Unknown decisionType: ${decisionType}`);
                next(); // bail out
            }

            if (!animate)
                gameUi.layoutUI(false);
            next();
        });
    if (gameUi.replayMode) {
        $("#main").queue(
            function (next) {
                gameUi.animations.setTimeout(next, gameUi.getAnimationLength(gameUi.decisionDuration));
            });
    }
}

// Choosing one action to resolve, for example phase actions
function cardActionChoiceDecision(decision, gameUi) {
    let id = decision.decisionId;
    let text = decision.text;
    let noPass = decision.noPass; // boolean
    let selectableCards = decision.displayedCards;

    if (selectableCards.length == 0 && gameUi.gameSettings.get("autoPass") && !gameUi.replayMode) {
        gameUi.decisionFunction(id, "");
        return;
    }

    let selectedCardIds = new Array();
    let allCardIds = new Array();

    gameUi.alertText.html(text);
    // ****CCG League****: Border around alert box
    gameUi.alertBox.addClass("alert-box-highlight");

    var processButtons = function () {
        gameUi.alertButtons.html("");
        if (!noPass && selectedCardIds.length == 0) {
            gameUi.alertButtons.append("<button id='Pass'>Pass</button>");
            $("#Pass").button().click(function () {
                finishChoice();
            });
        }
        if (selectedCardIds.length > 0) {
            gameUi.alertButtons.append("<button id='ClearSelection'>Reset choice</button>");
            gameUi.alertButtons.append("<button id='Done'>Done</button>");
            $("#Done").button().click(function () {
                finishChoice();
            });
            $("#ClearSelection").button().click(function () {
                resetChoice();
            });
        }
    };

    var finishChoice = function () {
        gameUi.alertText.html("");
        // ****CCG League****: Border around alert box
        gameUi.alertBox.removeClass("alert-box-highlight");
        gameUi.alertButtons.html("");
        gameUi.clearSelection();
        $(".card").each(
            function () {
                var card = $(this).data("card");
                if (card.zone == "EXTRA") {
                    $(this).remove();
                }
            });
        gameUi.hand.layoutCards();
        gameUi.decisionFunction(id, "" + selectedCardIds);
    };

    var resetChoice = function () {
        selectedCardIds = new Array();
        gameUi.clearSelection();
        allowSelection();
        processButtons();
    };

    var allowSelection = function () {
        var hasVirtual = false;

        for (let i = 0; i < selectableCards.length; i++) {
            let selectableCard = selectableCards[i];
            let actionId = selectableCard.actionId;
            let actionText = selectableCard.actionText;
            let blueprintId = selectableCard.blueprintId;
            let cardId = selectableCard.cardId;
            let actionType = selectableCard.actionType;
            let cardIdElem;

            if (blueprintId === "inPlay") {
                // in play, do not add "extra" to card id value when doing lookup
                cardIdElem = getCardDivFromId(cardId);
                allCardIds.push(cardId);
            } else {
                // not in play, need to add "extra" to card id value when doing lookup
                hasVirtual = true;
                cardIdElem = getCardDivFromId(`extra${cardId}`);
                allCardIds.push(`extra${cardId}`);
                let zone = "EXTRA";
                // No new cardId - interesting that it's going to retrieve an extra{cardId} but
                //                 create one with a regular cardId. Intentional?
                let imageUrl = selectableCard.imageUrl;
                let noOwner = "";
                let noLocationIndex = "";
                let upsideDown = false;
                let card = new Card(blueprintId, zone, cardId, noOwner, imageUrl, noLocationIndex, upsideDown);

                let cardDiv = gameUi.createCardDivWithData(card);
                $(cardDiv).css({opacity: "0.8"});

                $("#main").append(cardDiv);
            }

            if (cardIdElem.data("action") == null) {
                cardIdElem.data("action", new Array());
            }
            var actions = cardIdElem.data("action");
            actions.push({actionId: actionId, actionText: actionText, actionType: actionType});
        }

        if (hasVirtual) {
            gameUi.hand.layoutCards();
        }

        gameUi.selectionFunction = function (cardId, event) {
            // DEBUG: console.log("cardActionChoiceDecision -> allowSelection -> selectionFunction");
            var cardIdElem = getCardDivFromId(cardId);
            var actions = cardIdElem.data("action");

            var selectActionFunction = function (actionId) {
                selectedCardIds.push(actionId);
                if (gameUi.gameSettings.get("autoAccept")) {
                    finishChoice();
                } else {
                    gameUi.clearSelection();
                    getCardDivFromId(cardId).addClass("selectedCard");
                    processButtons();
                }
            };

            // If the only legal action is a card play, perform action automatically by clicking
            // Otherwise show a drop-down menu with the action options by clicking
            if (actions.length == 1 &&
                    (actions[0].actionType == "PLAY_CARD" || actions[0].actionType == "SEED_CARD")) {
                selectActionFunction(actions[0].actionId);
            } else {
                    // TODO - Bind to right-click?
                gameUi.createActionChoiceContextMenu(actions, event, selectActionFunction);
            }
        };

        gameUi.attachSelectionFunctions(allCardIds, false);
    };

    allowSelection();
    if (!gameUi.replayMode) {
        processButtons();
        gameUi.PlayAwaitActionSound();
    }

    $(':button').blur();
}

// Choosing one action to resolve, for example required triggered actions
function actionChoiceDecision(decision, gameUi) {
    var id = decision.decisionId;
    var text = decision.text;

    let displayedCards = decision.displayedCards;

    var selectedActionIds = new Array();

    gameUi.cardActionDialog
        .html("<div id='arbitraryChoice'></div>")
        .dialog("option", "title", text);

    var cardIds = new Array();

    for (let i = 0; i < displayedCards.length; i++) {
        let displayedCard = displayedCards[i];
        let blueprintId = displayedCard.blueprintId;
        let zone = "SPECIAL";
        let cardId = `temp${i}`;
        let noOwner = "";
        let imageUrl = displayedCard.imageUrl;
        let noLocationIndex = "";
        let upsideDown = false;

        cardIds.push("temp" + i);
        let card = new Card(blueprintId, zone, cardId, noOwner, imageUrl, noLocationIndex, upsideDown);

        let cardDiv = gameUi.createCardDivWithData(card, displayedCard.actionText);

        $("#arbitraryChoice").append(cardDiv);
    }

    var finishChoice = function () {
        gameUi.cardActionDialog.dialog("close");
        $("#arbitraryChoice").html("");
        gameUi.clearSelection();
        gameUi.decisionFunction(id, "" + selectedActionIds);
    };

    var resetChoice = function () {
        selectedActionIds = new Array();
        gameUi.clearSelection();
        allowSelection();
        processButtons();
    };

    var processButtons = function () {
        var buttons = {};
        if (selectedActionIds.length > 0) {
            buttons["Clear selection"] = function () {
                resetChoice();
                processButtons();
            };
            buttons["Done"] = function () {
                finishChoice();
            };
        }
        gameUi.cardActionDialog.dialog("option", "buttons", buttons);
    };

    var allowSelection = function () {
        gameUi.selectionFunction = function (cardId) {
            // DEBUG: console.log("actionChoiceDecision -> allowSelection -> selectionFunction");
            var actionId = displayedCards[parseInt(cardId.substring(4))].actionId;
            selectedActionIds.push(actionId);

            gameUi.clearSelection();

            if (gameUi.gameSettings.get("autoAccept")) {
                finishChoice();
            } else {
                processButtons();
                getCardDivFromId(cardId).addClass("selectedCard");
            }
        };

        gameUi.attachSelectionFunctions(cardIds, true);
    };

    allowSelection();
    if (!gameUi.replayMode) {
        processButtons();
        gameUi.PlayAwaitActionSound();
    }

    openSizeDialog(gameUi.cardActionDialog);
    gameUi.arbitraryDialogResize(false);
    $('.ui-dialog :button').blur();
}