import { getUrlParam } from './common.js';

export default class GempClientCommunication {
    constructor(url, failure) {
        this.url = url;
        this.failure = failure;
    }

    errorCheck(errorMap) {
        var that = this;
        return function (xhr, status, request) {
            var errorStatus = "" + xhr.status;
            if (errorMap != null && errorMap[errorStatus] != null)
                errorMap[errorStatus](xhr, status, request);
            else if (""+xhr.status != "200")
                that.failure(xhr, status, request);
        }
    }

    logout(callback, errorMap) {
        /* TODO ES6 */
        $.ajax({
            type:"POST",
            url:this.url + "/logout",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap)
        });
    }

    getDelivery(callback) {
        /* TODO ES6 */
        $.ajax({
            type:"GET",
            url:this.url + "/delivery",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:callback,
            error:null,
            dataType:"xml"
        });
    }

    deliveryCheck(callback) {
        var that = this;
        return function (xml, status, request) {
            var delivery = request.getResponseHeader("Delivery-Service-Package");
            if (delivery == "true" && window.deliveryService != null)
                that.getDelivery(window.deliveryService);
            callback(xml);
        };
    }
    
    deliveryCheckStatus(callback) {
        var that = this;
        return function (xml, status, request) {
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
                count:count,
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getStats(startDay, length, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/stats",
            cache:false,
            data:{
                startDay:startDay,
                length:length,
                participantId:getUrlParam("participantId") },
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
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }
    
    getLiveTournaments(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/tournament",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getHistoryTournaments(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/tournament/history",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getTournament(tournamentId, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/tournament/" + tournamentId,
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getLeagues(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/league",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getLeague(type, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/league/" + type,
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    joinLeague(code, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/league/" + code,
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
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

    startGameSession(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/game/" + getUrlParam("gameId"),
            cache:false,
            data:{ participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    updateGameState(channelNumber, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/game/" + getUrlParam("gameId"),
            cache:false,
            data:{
                channelNumber:channelNumber,
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getGameCardModifiers(cardId, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/game/" + getUrlParam("gameId") + "/cardInfo",
            cache:false,
            data:{
                cardId:cardId,
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }

    gameDecisionMade(decisionId, response, channelNumber, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/game/" + getUrlParam("gameId"),
            cache:false,
            data:{
                channelNumber:channelNumber,
                participantId:getUrlParam("participantId"),
                decisionId:decisionId,
                decisionValue:response },
            success:this.deliveryCheck(callback),
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }
    
    concede(errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/game/" + getUrlParam("gameId") + "/concede",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    cancel(errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/game/" + getUrlParam("gameId") + "/cancel",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getDeck(deckName, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/deck",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    shareDeck(deckName, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/deck/share",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    getLibraryDeck(deckName, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/deck/library",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getDecks(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/deck/list",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getLibraryDecks(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/deck/libraryList",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getCollectionTypes(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/collection",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }
    
    getMerchant(filter, ownedMin, start, count, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/merchant",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                filter:filter,
                ownedMin:ownedMin,
                start:start,
                count:count},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    buyItem(blueprintId, price, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/merchant/buy",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                blueprintId:blueprintId,
                price:price},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }

    sellItem(blueprintId, price, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/merchant/sell",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                blueprintId:blueprintId,
                price:price},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }

    tradeInFoil(blueprintId, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/merchant/tradeFoil",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                blueprintId:blueprintId},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }

    getCollection(collectionType, filter, start, count, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/collection/" + collectionType,
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                filter:filter,
                start:start,
                count:count},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    importCollection(decklist, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/collection/import/",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                decklist:decklist},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    openPack(collectionType, pack, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/collection/" + collectionType,
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                pack:pack},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    openSelectionPack(collectionType, pack, selection, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/collection/" + collectionType,
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                pack:pack,
                selection:selection},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    saveDeck(deckName, targetFormat, notes, contents, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck",
            cache:false,
            async:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName,
                targetFormat:targetFormat,
                notes:notes,
                deckContents:contents},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    renameDeck(oldDeckName, deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck/rename",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                oldDeckName:oldDeckName,
                deckName:deckName},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    deleteDeck(deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck/delete",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getDeckStats(contents, targetFormat, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck/stats",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                targetFormat:targetFormat,
                deckContents:contents},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }

    getSets(format, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck/sets",
            cache:true,
            data:{ 
                format:format
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }

    getFormats(includeEvents, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck/formats",
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
            url:this.url + "/chat/" + room,
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    updateChat(room, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/chat/" + room,
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:callback,
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    sendChatMessage(room, messages, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/chat/" + room,
            cache:false,
            async:false,
            data:{
                participantId:getUrlParam("participantId"),
                message:messages},
            traditional:true,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getHall(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/hall",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    updateHall(callback, channelNumber, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/update",
            cache:false,
            data:{
                channelNumber:channelNumber,
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    joinQueue(queueId, deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/queue/" + queueId,
            cache:false,
            data:{
                deckName:deckName,
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    leaveQueue(queueId, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/queue/" + queueId + "/leave",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    dropFromTournament(tournamentId, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/tournament/" + tournamentId + "/leave",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    joinTable(tableId, deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/" + tableId,
            cache:false,
            data:{
                deckName:deckName,
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    leaveTable(tableId, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/"+tableId+"/leave",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    createTable(format, deckName, timer, desc, isPrivate, isInviteOnly, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall",
            cache:false,
            data:{
                format:format,
                deckName:deckName,
                timer:timer,
                desc:desc,
                isPrivate:isPrivate,
                isInviteOnly:isInviteOnly,
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    getFormat(formatCode, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/hall/format/" + formatCode,
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }

    getFormatRules(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/hall/formats/html",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    getErrata(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/hall/errata/json",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    addTesterFlag(callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/playtesting/addTesterFlag",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    removeTesterFlag(callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/playtesting/removeTesterFlag",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    getRecentReplays(format, count, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/playtesting/getRecentReplays",
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
            url:this.url + "/admin/shutdown",
            cache:false,
            data:{
                shutdown:shutdown
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }

    clearServerCache(callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/clearCache",
            cache:false,
            data:{},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    reloadCardDefinitions(callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/reloadCards",
            cache:false,
            data:{},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    getDailyMessage(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/admin/getDailyMessage",
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    setDailyMessage(motd, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/setDailyMessage",
            cache:false,
            data:{
                motd:motd
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    addItems(collectionType, product, players, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/addItems",
            cache:false,
            data:{
                collectionType:collectionType,
                product:product,
                players:players
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    resetUserPassword(login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/resetUserPassword",
            cache:false,
            data:{
                login:login
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    permabanUser(login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/banUser",
            cache:false,
            data:{
                login:login
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    tempbanUser(login, duration, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/banUserTemp",
            cache:false,
            data:{
                login:login,
                duration:duration
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    unbanUser(login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/unBanUser",
            cache:false,
            data:{
                login:login
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    susUserSearch(login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/findMultipleAccounts",
            cache:false,
            data:{
                login:login
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    banMultiple(login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/banMultiple",
            cache:false,
            data:{
                login:login
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }
    
    previewSealedLeague(format, start, seriesDuration, maxMatches, name, cost, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/previewSealedLeague",
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
            dataType:"xml"
        });
    }
    
    addSealedLeague(format, start, seriesDuration, maxMatches, name, cost, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/addSealedLeague",
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
            dataType:"html"
        });
    }
    
    previewSoloDraftLeague(format, start, seriesDuration, maxMatches, name, cost, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/previewSoloDraftLeague",
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
            dataType:"xml"
        });
    }
    
    addSoloDraftLeague(format, start, seriesDuration, maxMatches, name, cost, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/addSoloDraftLeague",
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
            dataType:"html"
        });
    }
    
    previewConstructedLeague(start, collectionType, prizeMultiplier, name, cost, formats, seriesDurations, maxMatches, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/previewConstructedLeague",
            cache:false,
            data:{
                start:start,
                collectionType:collectionType,
                prizeMultiplier:prizeMultiplier,
                name:name,
                cost:cost,
                format:formats,
                seriesDuration:seriesDurations,
                maxMatches:maxMatches
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }
    
    addConstructedLeague(start, collectionType, prizeMultiplier, name, cost, formats, seriesDurations, maxMatches, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/addConstructedLeague",
            cache:false,
            data:{
                start:start,
                collectionType:collectionType,
                prizeMultiplier:prizeMultiplier,
                name:name,
                cost:cost,
                format:formats,
                seriesDuration:seriesDurations,
                maxMatches:maxMatches
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
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
            url:this.url + "/player",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    }
    
    getStatus(callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
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
                password:password,
                participantId:getUrlParam("participantId")},
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
                password:password,
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheckStatus(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }

    getRegistrationForm(callback, errorMap) {
        $.ajax({
            type:"POST",
            url:"/gemp-module/includes/registrationForm.html",
            cache:false,
            async:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    }

    getDraft(leagueType, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/soloDraft/"+leagueType,
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }

    makeDraftPick(leagueType, choiceId, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/soloDraft/"+leagueType,
            cache:false,
            data:{
                choiceId:choiceId,
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }
};
