import { getUrlParam, userAgent } from './common.js';

export default class GempClientCommunication {
    constructor(url, failure) {
        this.url = url;
        this.failure = failure;
    }

    errorCheck(errorMap) {
        var that = this;
        return function (xhr, status, request) {
            if (xhr.readyState === 4) { // Don't throw errors unless the XMLHttpRequest is done
                let errorStatus = "" + xhr.status;
                if (errorMap != null && errorMap[errorStatus] != null) {
                    errorMap[errorStatus](xhr, status, request);
                } else if (""+xhr.status != "200") {
                    that.failure(xhr, status, request);
                }
            }
        }
    }

    logout(callback, errorMap) {
        /* TODO ES6 */
        // TODO - As of 10 Feb 2025, this method doesn't have any related server-side functionality.
        $.ajax({
            type:"POST",
            url:this.url + "/logout",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap)
        });
    }

    // TODO - As of 9 Feb 2025, this method will never be called.
    getDelivery(callback) {
        /* TODO ES6 */
        $.ajax({
            type:"GET",
            url:this.url + "/delivery",
            cache:false,
            success:callback,
            error:null,
            dataType:"xml"
        });
    }

    deliveryCheck(callback) {
        var that = this;
        return function (xml, status, request) {
            // TODO - As of 9 Feb 2025, delivery will always be false. This response header is no longer used.
            var delivery = request.getResponseHeader("Delivery-Service-Package");
            if (delivery == "true" && window.deliveryService != null)
                that.getDelivery(window.deliveryService);
            callback(xml);
        };
    }
    
    deliveryCheckStatus(callback) {
        var that = this;
        return function (xml, status, request) {
            // TODO - As of 9 Feb 2025, delivery will always be false. This response header is no longer used.
            var delivery = request.getResponseHeader("Delivery-Service-Package");
            if (delivery == "true" && window.deliveryService != null)
                that.getDelivery(window.deliveryService);
            callback(xml, request.status);
        };
    }

    getGameHistory(start, count, callback, errorMap) {
        /* TODO ES6 */
        $.ajax({
            type:"GET",
            url:this.url + "/gameHistory",
            cache:false,
            data:{
                start:start,
                count:count
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    getStats(startDay, length, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/serverStats",
            cache:false,
            data:{
                startDay:startDay,
                length:length
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    getPlayerStats(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/playerStats",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    getLiveTournaments(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/currentTournaments",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    getHistoryTournaments(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/tournamentHistory",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    getLeagues(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/currentLeagues",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getLeague(type, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/getLeague",
            data:{
                leagueType:type
            },
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    joinLeague(code, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/joinLeague",
            data:{
                leagueType:code
            },
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getReplay(replayId, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/replay/" + replayId,
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    async getGameState() {
        const url = this.url + "/getGameState/" + getUrlParam("gameId") + "/thisPlayer";
        try {
            const parameters = new URLSearchParams({
                "gameId": getUrlParam("gameId")
            }).toString();

            const fullUrl = url + parameters;


            let response = await fetch(fullUrl, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            if (!response.ok) {
                if (response.status == 404) {
                    /* Thrown if server cannot identify a game with this id number, or if the server has no handler
                        for the provided URL */
                    alert("Unable to find game.");
                }
                else if (response.status == 401) {
                    /* Users who have admin privileges should not encounter this error. Non-admin users get this if:
                        - They try to access another player's game state view
                        - They try to access the "complete" game state with no hidden information
                    */
                    alert("User cannot access this game state data.");
                }
                else if (response.status == 500) {
                    // Thrown if the gamestate encounters errors while being serialized
                    alert("Cannot create game state data.");
                }
                else {
                    throw new Error(response.statusText);
                }
            }
            else {
                let retval = await response.json();
                return retval;
            }
        }
        catch(error) {
            console.error({"getGameState fetch error": error.message});
        }
    }

    startGameSession(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/startGameSession",
            cache:false,
            data:{
                gameId:getUrlParam("gameId")
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    updateGameState(channelNumber, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/updateGameState",
            cache:false,
            data:{
                gameId:getUrlParam("gameId"),
                channelNumber:channelNumber
            },
            success:this.deliveryCheck(callback),
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    getCardInfo(cardId, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/gameCardInfo/",
            cache:false,
            data:{
                cardId:cardId,
                gameId:getUrlParam("gameId")
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    gameDecisionMade(decisionId, response, channelNumber, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/decisionResponse",
            cache:false,
            data:{
                gameId:getUrlParam("gameId"),
                channelNumber:channelNumber,
                decisionId:decisionId,
                decisionValue:response },
            success:this.deliveryCheck(callback),
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    concede(errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/concedeGame",
            cache:false,
            data:{
                gameId:getUrlParam("gameId")
            },
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    cancel(errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/cancelGame",
            cache:false,
            data:{
                gameId:getUrlParam("gameId")
            },
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    listUserDecks(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/listUserDecks",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    listLibraryDecks(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/listLibraryDecks",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    async getCollection(collectionType, participantId, filter, start, count) {
        const url = this.url + "/collection/" + collectionType + "?";
        const parameters = new URLSearchParams({
            "participantId": participantId,
            "filter": filter,
            "start": start,
            "count": count
        }).toString();

        const fullurl = url + parameters;

        try {
            let response = await fetch(fullurl, {
                method: "GET",
                headers: {
                    "Content-Type": "application/xml"
                }
            });

            if (!response.ok) {
                if (response.status == 404) {
                    alert("You don't have collection of that type.");
                }
                else {
                    throw new Error(response.statusText);
                }
            }
            else {
                return response.text();
            }
        }
        catch(error) {
            console.error({"getCollection fetch error": error.message});
        }
    }

    importCollection(deckList, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/importDeck",
            cache:false,
            data:{
                deckList:deckList
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    openPack(collectionType, pack, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/openPack",
            cache:false,
            data:{
                packId:pack,
                collectionType:collectionType
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    openSelectionPack(collectionType, pack, selection, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/openPack",
            cache:false,
            data:{
                packId:pack,
                selection:selection,
                collectionType:collectionType
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    saveDeck(deckName, targetFormat, notes, contents, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/saveDeck",
            cache:false,
            async:false,
            data:{
                deckName:deckName,
                targetFormat:targetFormat,
                notes:notes,
                deckContents:contents
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    renameDeck(oldDeckName, deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/renameDeck",
            cache:false,
            data:{
                oldDeckName:oldDeckName,
                deckName:deckName
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    deleteDeck(deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deleteDeck",
            cache:false,
            data:{
                deckName:deckName
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getDeckStats(contents, targetFormat, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deckStats",
            cache:false,
            data:{
                targetFormat:targetFormat,
                deckContents:contents
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    async getSets(format) {
        const url = this.url + "/getSets";
        try {
            let response = await fetch(url, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: `format=${format}`
            });
            
            if (!response.ok) {
                if (response.status == 400) {
                    alert("Could not retrieve sets.");
                }
                else {
                    throw new Error(response.statusText);
                }
            }
            else {
                let retval = await response.json();
                return retval;
            }
        }
        catch(error) {
            console.error({"getSets fetch error": error.message});
        }
    }

    getFormats(includeEvents, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deckFormats",
            cache:true,
            data:{
                includeEvents:includeEvents
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    startChat(room, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/getChat",
            cache:false,
            data:{
                roomName: room
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    updateChat(room, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/postChat",
            cache:false,
            data:{
                roomName:room
            },
            success:callback,
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    sendChatMessage(room, messages, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/sendChatMessage",
            cache:false,
            async:false,
            data:{
                roomName:room,
                message:messages
            },
            traditional:true,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }

    getHall(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/getHall",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    updateHall(callback, channelNumber, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/updateHall",
            cache:false,
            data:{
                channelNumber:channelNumber
            },
            success:this.deliveryCheck(callback),
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    joinQueue(queueId, deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/joinQueue",
            cache:false,
            data:{
                queueId:queueId,
                deckName:deckName,
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    leaveQueue(queueId, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/leaveQueue",
            cache:false,
            data:{
                queueId:queueId
            },
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    dropFromTournament(tournamentId, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/leaveTournament",
            cache:false,
            data:{
                tournamentId:tournamentId
            },
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    joinTable(tableId, deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/joinTable",
            cache:false,
            data:{
                tableId:tableId,
                deckName:deckName
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    leaveTable(tableId, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/leaveTable",
            cache:false,
            data:{
                tableId:tableId
            },
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    createTable(format, deckName, timer, desc, isPrivate, isInviteOnly, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/createTable",
            cache:false,
            data:{
                format:format,
                deckName:deckName,
                timer:timer,
                desc:desc,
                isPrivate:isPrivate,
                isInviteOnly:isInviteOnly
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getErrata(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/getErrata",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    addTesterFlag(callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/setTesterFlag",
            data:{
                testerFlag:true
            },
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    removeTesterFlag(callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/setTesterFlag",
            data:{
                testerFlag:false
            },
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    getRecentReplays(format, count, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/playtestReplays",
            cache:false,
            data:{
                format:format,
                count:count
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    setShutdownMode(shutdown, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/setShutdown",
            cache:false,
            data:{
                shutdown:shutdown
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    clearServerCache(callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/clearCache",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    reloadCardDefinitions(callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/reloadCardLibrary",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    getDailyMessage(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/getDailyMessage",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    setDailyMessage(motd, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/setDailyMessage",
            cache:false,
            data:{
                newMessage:motd
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    resetUserPassword(login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/resetUserPassword",
            cache:false,
            data:{
                userName:login
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    permabanUser(login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/banUser",
            cache:false,
            data:{
                userName:login
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    tempbanUser(login, duration, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/banUserTemporary",
            cache:false,
            data:{
                userName:login,
                duration:duration
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    unbanUser(login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/unBanUser",
            cache:false,
            data:{
                userName:login
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    susUserSearch(login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/findMultipleAccounts",
            cache:false,
            data:{
                userName:login
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    banMultiple(login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/banUserMultiple",
            cache:false,
            data:{
                userNames:login
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    previewSealedLeague(format, start, seriesDuration, maxMatches, name, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/previewSealedLeague",
            cache:false,
            data:{
                format:format,
                start:start,
                seriesDuration:seriesDuration,
                maxMatches:maxMatches,
                name:name
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    addSealedLeague(format, start, seriesDuration, maxMatches, name, cost, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/addSealedLeague",
            cache:false,
            data:{
                format:format,
                start:start,
                seriesDuration:seriesDuration,
                maxMatches:maxMatches,
                name:name,
                cost:cost
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    previewSoloDraftLeague(format, start, seriesDuration, maxMatches, name, cost, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/leagueAdminDraft",
            cache:false,
            data:{
                format:format,
                start:start,
                seriesDuration:seriesDuration,
                maxMatches:maxMatches,
                name:name,
                cost:cost,
                preview:true
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }
    
    addSoloDraftLeague(format, start, seriesDuration, maxMatches, name, cost, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/leagueAdminDraft",
            cache:false,
            data:{
                format:format,
                start:start,
                seriesDuration:seriesDuration,
                maxMatches:maxMatches,
                name:name,
                cost:cost,
                preview:false
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    previewConstructedLeague(start, collectionType, prizeMultiplier, name, cost, formats, seriesDurations, maxMatches, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/leagueAdminConstructed",
            cache:false,
            data:{
                start:start,
                collectionType:collectionType,
                prizeMultiplier:prizeMultiplier,
                name:name,
                cost:cost,
                format:formats,
                seriesDuration:seriesDurations,
                maxMatches:maxMatches,
                preview:true
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }
    
    addConstructedLeague(start, collectionType, prizeMultiplier, name, cost, formats, seriesDurations, maxMatches, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/leagueAdminConstructed",
            cache:false,
            data:{
                start:start,
                collectionType:collectionType,
                prizeMultiplier:prizeMultiplier,
                name:name,
                cost:cost,
                format:formats,
                seriesDuration:seriesDurations,
                maxMatches:maxMatches,
                preview:false
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    
    
    //NEVER EVER EVER use this for actual authentication
    // This is strictly to simplify things like auto-hiding
    // of the admin panel.  If you actually need functionality
    // gated behind authorization, it goes on the server
    // and not in here.
    
    getPlayerInfo(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/playerInfo",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    getStatus(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/hallStatus",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }

    login(login, password, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/login",
            cache:false,
            async:false,
            data:{
                login:login,
                password:password
            },
            success:this.deliveryCheckStatus(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }

    register(login, password, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/register",
            cache:false,
            data:{
                login:login,
                password:password
            },
            success:this.deliveryCheckStatus(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }

    getDraft(leagueType, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/getAvailableDraftPicks",
            cache:false,
            data:{
                leagueType:leagueType
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    makeDraftPick(leagueType, choiceId, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/makeDraftPick",
            cache:false,
            data:{
                leagueType:leagueType,
                choiceId:choiceId
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }
};

export async function fetchImage(url) {
    try {
        // HACK, TODO: Temporarily override URLs to point at TrekCC.dev
        // Long term we should update the .json files to the intended host.
        let newUrl;
        if (url.startsWith("https://www.trekcc.org/1e/cardimages/")) {
            newUrl = url.replace("https://www.trekcc.org/1e/cardimages/", "https://trekcc.dev/1e/images/imgp.php?src=")
            newUrl = newUrl + "&w=330&q=100&sharpen&sa=webp"
        }
        else {
            newUrl = url;
        }

        const imgReqHeaders = new Headers();
        imgReqHeaders.append("Accept", "image/webp,image/png,image/jpeg,image/gif");
        imgReqHeaders.append("User-Agent", userAgent);
        let response = await fetch(newUrl, {
            method: "GET",
            headers: imgReqHeaders
        });
        
        if (!response.ok) {
            throw new Error(response.statusText);
        }
        else {
            let blob = await response.blob();
            // NOTE: It is up to the caller to revoke this object URL via
            //       URL.revokeObjectURL(url) if they want to reclaim the memory.
            let url = URL.createObjectURL(blob);
            return url;
        }
    }
    catch(error) {
        console.error({"fetchImage fetch error": error.message});
        return;
    }
};