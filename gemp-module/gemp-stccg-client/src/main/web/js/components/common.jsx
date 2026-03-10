export function get_your_player_id(gamestate) {
    if (Object.hasOwn(gamestate, "requestingPlayer")) {
        return gamestate["requestingPlayer"];
    }
    else {
        return "";
    }
}

export function get_opponent_player_id(gamestate) {
    let your_player_id = gamestate["requestingPlayer"];
    let opponent_names = [];
    if (Object.hasOwn(gamestate, "playerMap")) {
        for (const playerId of Object.keys(gamestate["playerMap"])) {
            if (playerId != your_player_id) {
                opponent_names.push(playerId);
            }
        }
        return opponent_names[0]; // assume 1 opponent
    }
    else {
        return "";
    }
}