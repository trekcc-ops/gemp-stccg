import Box from '@mui/material/Box';
import { RichTreeView } from '@mui/x-tree-view/RichTreeView';

function cards_to_treeitems (gamestate) {
    let player_id = gamestate["requestingPlayer"];
    let player_data = gamestate["players"].filter((data) => data["playerId"] === player_id);
    if (player_data.length != 1) {
        console.error(`player with id ${player_id} not found`);
        return [{id: 'error', label: 'error'}];
    }

    let opponent_player_data = gamestate["players"].filter((data) => data["playerId"] != player_id);
    if (opponent_player_data.length != 1) {
        console.error(`player with id ${player_id} not found`);
        return [{id: 'error', label: 'error'}];
    }

    let card_ids_in_your_hand = player_data[0]["cardGroups"]["HAND"]["cardIds"];
    let card_ids_in_your_core = player_data[0]["cardGroups"]["CORE"]["cardIds"];
    let card_ids_in_your_discard = player_data[0]["cardGroups"]["DISCARD"]["cardIds"];
    let card_ids_in_your_removed = player_data[0]["cardGroups"]["REMOVED"]["cardIds"];
    let card_ids_in_opponent_core = opponent_player_data[0]["cardGroups"]["CORE"]["cardIds"];
    let card_ids_in_opponent_discard = opponent_player_data[0]["cardGroups"]["DISCARD"]["cardIds"];
    let card_ids_in_opponent_removed = opponent_player_data[0]["cardGroups"]["REMOVED"]["cardIds"];

    let visible_cards_in_game = gamestate["visibleCardsInGame"];

    let card_tree = [];

    card_tree.push(build_your_hand_treeitems(card_ids_in_your_hand, visible_cards_in_game));
    card_tree.push(build_your_core_treeitems(card_ids_in_your_core, visible_cards_in_game));
    card_tree.push(build_your_discard_treeitems(card_ids_in_your_discard, visible_cards_in_game));
    card_tree.push(build_your_removed_treeitems(card_ids_in_your_removed, visible_cards_in_game));
    card_tree.push(build_opponent_core_treeitems(card_ids_in_opponent_core, visible_cards_in_game));
    card_tree.push(build_opponent_discard_treeitems(card_ids_in_opponent_discard, visible_cards_in_game));
    card_tree.push(build_opponent_removed_treeitems(card_ids_in_opponent_removed, visible_cards_in_game));

    return card_tree;
}

function build_your_hand_treeitems(hand_arr, visible_cards) {
    let hand_item = {id: 'your_hand', label: 'Your Hand', children: []};

    for (const hand_cardid of hand_arr) {
        let matching_card = visible_cards.filter((card) => card["cardId"] === hand_cardid);
        if (matching_card.length === 1) {
            hand_item.children.push(card_to_treeitem(matching_card[0]));
        }
    }

    return hand_item;
}

function build_your_core_treeitems(core_arr, visible_cards) {
    let core_item = {id: 'your_core', label: 'Your Core', children: []};

    for (const core_cardid of core_arr) {
        let matching_card = visible_cards.filter((card) => card["cardId"] === core_cardid);
        if (matching_card.length === 1) {
            core_item.children.push(card_to_treeitem(matching_card[0]));
        }
    }

    return core_item;
}

function build_your_discard_treeitems(discard_arr, visible_cards) {
    let discard_item = {id: 'your_discard', label: 'Your Discard', children: []};

    for (const discard_cardid of discard_arr) {
        let matching_card = visible_cards.filter((card) => card["cardId"] === discard_cardid);
        if (matching_card.length === 1) {
            discard_item.children.push(card_to_treeitem(matching_card[0]));
        }
    }

    return discard_item;
}

function build_your_removed_treeitems(removed_arr, visible_cards) {
    let removed_item = {id: 'your_removed', label: 'Your Removed', children: []};

    for (const removed_cardid of removed_arr) {
        let matching_card = visible_cards.filter((card) => card["cardId"] === removed_cardid);
        if (matching_card.length === 1) {
            removed_item.children.push(card_to_treeitem(matching_card[0]));
        }
    }

    return removed_item;
}

function build_opponent_core_treeitems(core_arr, visible_cards) {
    let core_item = {id: 'opponent_core', label: 'Opponent Core', children: []};

    for (const core_cardid of core_arr) {
        let matching_card = visible_cards.filter((card) => card["cardId"] === core_cardid);
        if (matching_card.length === 1) {
            core_item.children.push(card_to_treeitem(matching_card[0]));
        }
    }

    return core_item;
}

function build_opponent_discard_treeitems(discard_arr, visible_cards) {
    let discard_item = {id: 'opponent_discard', label: 'Opponent Discard', children: []};

    for (const discard_cardid of discard_arr) {
        let matching_card = visible_cards.filter((card) => card["cardId"] === discard_cardid);
        if (matching_card.length === 1) {
            discard_item.children.push(card_to_treeitem(matching_card[0]));
        }
    }

    return discard_item;
}

function build_opponent_removed_treeitems(removed_arr, visible_cards) {
    let removed_item = {id: 'opponent_removed', label: 'Opponent Removed', children: []};

    for (const removed_cardid of removed_arr) {
        let matching_card = visible_cards.filter((card) => card["cardId"] === removed_cardid);
        if (matching_card.length === 1) {
            removed_item.children.push(card_to_treeitem(matching_card[0]));
        }
    }

    return removed_item;
}

function card_to_treeitem (card) {
    const id = card ? card.cardId : -1;
    const title = card ? card.title : "ERROR";

    return {
        id: `${id}`,
        label: title
    }
}

export default function CardTree ( {gamestate} ) {
    return(
        <Box>
            <RichTreeView items={cards_to_treeitems(gamestate)} />
        </Box>
    );
}