import Box from '@mui/material/Box';
import { RichTreeView } from '@mui/x-tree-view/RichTreeView';
import CardTreeModel from '../lib/cardTreeModel.js';

function get_your_player_id(gamestate) {
    return gamestate["requestingPlayer"];
}

function get_opponent_player_id(gamestate) {
    let your_player_id = gamestate["requestingPlayer"];
    let opponent_names = [];
    for (const playerId of Object.keys(gamestate["playerMap"])) {
        if (playerId != your_player_id) {
            opponent_names.push(playerId);
        }
    }
    return opponent_names[0]; // assume 1 opponent
}

function cards_to_treeitems (gamestate) {
    let player_id = get_your_player_id(gamestate);
    let player_data = gamestate.playerMap[player_id];
    if (player_data == null) {
        console.error(`player with id ${player_id} not found`);
        return [{id: 'error', label: 'error'}];
    }

    let opponent_player_id = get_opponent_player_id(gamestate);
    let opponent_player_data = gamestate.playerMap[opponent_player_id];
    if (opponent_player_data == null) {
        console.error(`player with id ${opponent_player_id} not found`);
        return [{id: 'error', label: 'error'}];
    }

    let card_ids_in_your_hand = player_data["cardGroups"]["HAND"]["cardIds"];
    let card_ids_in_your_core = player_data["cardGroups"]["CORE"]["cardIds"];
    let card_ids_in_your_discard = player_data["cardGroups"]["DISCARD"]["cardIds"];
    let card_ids_in_your_removed = player_data["cardGroups"]["REMOVED"]["cardIds"];
    let card_ids_in_opponent_core = opponent_player_data["cardGroups"]["CORE"]["cardIds"];
    let card_ids_in_opponent_discard = opponent_player_data["cardGroups"]["DISCARD"]["cardIds"];
    let card_ids_in_opponent_removed = opponent_player_data["cardGroups"]["REMOVED"]["cardIds"];

    let visible_cards_obj = gamestate["visibleCardsInGame"];
    let visible_cards_in_game = Object.values(visible_cards_obj); // array

    let card_ids_on_table = [];
    for (const visible_card of visible_cards_in_game) {
        if( card_ids_in_your_hand.indexOf(visible_card["cardId"]) === -1 &&
            card_ids_in_your_core.indexOf(visible_card["cardId"]) === -1 &&
            card_ids_in_your_discard.indexOf(visible_card["cardId"]) === -1 &&
            card_ids_in_your_removed.indexOf(visible_card["cardId"]) === -1 &&
            card_ids_in_opponent_core.indexOf(visible_card["cardId"]) === -1 &&
            card_ids_in_opponent_discard.indexOf(visible_card["cardId"]) === -1 &&
            card_ids_in_opponent_removed.indexOf(visible_card["cardId"]) === -1
        ){
            card_ids_on_table.push(visible_card["cardId"]);
        }
    }

    let card_tree = [];

    card_tree.push(build_cards_on_table_treeitems(card_ids_on_table, visible_cards_obj));
    card_tree.push(build_your_hand_treeitems(card_ids_in_your_hand, visible_cards_obj));
    card_tree.push(build_your_core_treeitems(card_ids_in_your_core, visible_cards_obj));
    card_tree.push(build_your_discard_treeitems(card_ids_in_your_discard, visible_cards_obj));
    card_tree.push(build_your_removed_treeitems(card_ids_in_your_removed, visible_cards_obj));
    card_tree.push(build_opponent_core_treeitems(card_ids_in_opponent_core, visible_cards_obj));
    card_tree.push(build_opponent_discard_treeitems(card_ids_in_opponent_discard, visible_cards_obj));
    card_tree.push(build_opponent_removed_treeitems(card_ids_in_opponent_removed, visible_cards_obj));

    return card_tree;
}

function build_cards_on_table_treeitems(table_arr, visible_cards) {
    const treemap = CardTreeModel.cardFlatMapToTreeMap(visible_cards, table_arr);

    let table_item = {id: 'table', label: 'On Table', children: []};

    for (const table_card_id in treemap) {
        // don't show known cards that aren't in play, e.g. in Draw Deck, etc.
        if (treemap[table_card_id].isInPlay) {
            table_item.children.push(recurseTreeMapToTreeItem(treemap[table_card_id]));
        }
    }

    return table_item;
}

function recurseTreeMapToTreeItem(card) {
    let thiscard_treeitem = card_to_treeitem(card);
    if (card.children) {
        for (const child_id in card.children) {
            let child_treeitem = recurseTreeMapToTreeItem(card.children[child_id]);
            thiscard_treeitem.children.push(child_treeitem);
        }
    }
    return thiscard_treeitem;
}

function build_your_hand_treeitems(hand_arr, visible_cards) {
    let hand_item = {id: 'your_hand', label: 'Your Hand', children: []};

    for (const hand_cardid of hand_arr) {
        if (Object.hasOwn(visible_cards, hand_cardid)) {
            hand_item.children.push(card_to_treeitem(visible_cards[hand_cardid]));
        }
        else {
            console.error(`Could not find card matching id ${hand_cardid}`);
        }
    }

    return hand_item;
}

function build_your_core_treeitems(core_arr, visible_cards) {
    let core_item = {id: 'your_core', label: 'Your Core', children: []};

    for (const core_cardid of core_arr) {
        if (Object.hasOwn(visible_cards, core_cardid)) {
            core_item.children.push(card_to_treeitem(visible_cards[core_cardid]));
        }
        else {
            console.error(`Could not find card matching id ${core_cardid}`);
        }
    }

    return core_item;
}

function build_your_discard_treeitems(discard_arr, visible_cards) {
    let discard_item = {id: 'your_discard', label: 'Your Discard', children: []};

    for (const discard_cardid of discard_arr) {
        if (Object.hasOwn(visible_cards, discard_cardid)) {
            discard_item.children.push(card_to_treeitem(visible_cards[discard_cardid]));
        }
        else {
            console.error(`Could not find card matching id ${discard_cardid}`);
        }
    }

    return discard_item;
}

function build_your_removed_treeitems(removed_arr, visible_cards) {
    let removed_item = {id: 'your_removed', label: 'Your Removed', children: []};

    for (const removed_cardid of removed_arr) {
        if (Object.hasOwn(visible_cards, removed_cardid)) {
            removed_item.children.push(card_to_treeitem(visible_cards[removed_cardid]));
        }
        else {
            console.error(`Could not find card matching id ${removed_cardid}`);
        }
    }

    return removed_item;
}

function build_opponent_core_treeitems(core_arr, visible_cards) {
    let core_item = {id: 'opponent_core', label: 'Opponent Core', children: []};

    for (const core_cardid of core_arr) {
        if (Object.hasOwn(visible_cards, core_cardid)) {
            core_item.children.push(card_to_treeitem(visible_cards[core_cardid]));
        }
        else {
            console.error(`Could not find card matching id ${core_cardid}`);
        }
    }

    return core_item;
}

function build_opponent_discard_treeitems(discard_arr, visible_cards) {
    let discard_item = {id: 'opponent_discard', label: 'Opponent Discard', children: []};

    for (const discard_cardid of discard_arr) {
        if (Object.hasOwn(visible_cards, discard_cardid)) {
            discard_item.children.push(card_to_treeitem(visible_cards[discard_cardid]));
        }
        else {
            console.error(`Could not find card matching id ${discard_cardid}`);
        }
    }

    return discard_item;
}

function build_opponent_removed_treeitems(removed_arr, visible_cards) {
    let removed_item = {id: 'opponent_removed', label: 'Opponent Removed', children: []};

    for (const removed_cardid of removed_arr) {
        if (Object.hasOwn(visible_cards, removed_cardid)) {
            removed_item.children.push(card_to_treeitem(visible_cards[removed_cardid]));
        }
        else {
            console.error(`Could not find card matching id ${removed_cardid}`);
        }
    }

    return removed_item;
}

export function card_to_treeitem(card) {
    const id = card ? card.cardId : -1;
    const title = card ? card.title : "ERROR";
    const children = [];

    return {
        id: `${id}`,
        label: title,
        children: []
    }
}

export default function CardTreeView ( {gamestate, visible} ) {
    return(
        <Box>
            <RichTreeView items={cards_to_treeitems(gamestate)} />
        </Box>
    );
}