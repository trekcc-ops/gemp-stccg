import Box from '@mui/material/Box';
import { RichTreeView } from '@mui/x-tree-view/RichTreeView';
import {JSONPath} from 'jsonpath-plus';

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
    const treemap = cardFlatMapToTreeMap(table_arr, visible_cards);

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

export function cardFlatMapToTreeMap(card_ids_to_use, card_data) {
    if (!(card_ids_to_use instanceof Array)) {
        throw new TypeError(`card_ids_to_use '${card_ids_to_use}' must be an Array.`);
    }

    // if one of the cases is true, continue, otherwise throw error
    if (! ((card_data instanceof Map) || 
           (card_data instanceof Set) || 
           (card_data.constructor === Object))) {

            throw new TypeError(`card_data '${card_data}' must be an Object, Set, or Map that is addressable with object[]`);
    }

    let new_tree = {};
    for (const card_id of card_ids_to_use) {
        addCardToTreeMap(card_id, card_data, new_tree);
    }
    return new_tree;
};

export function addCardToTreeMap(card_id, card_data, tree) {
    // do we even have the requested data?
    if (!card_data[card_id]) {
        return;
    }

    // already in the tree? skip
    // NOTE: Use == not === here for consistent matching
    let search_all_cardIds_for_this_id_jsonpath = `$..*[?(@.cardId == '${card_id}')]`;
    const result = JSONPath({path: search_all_cardIds_for_this_id_jsonpath, json: tree});
    if (Object.keys(result).length !== 0) {
        return;
    }

    if (Object.hasOwn(card_data[card_id], "attachedToCardId")) {
        // need to attach to something
        // is it in the tree already?
        let parent_card_id = card_data[card_id].attachedToCardId;
        
        // NOTE: Use == not === here for consistent matching
        let search_all_cardIds_for_parent_id_jsonpath = `$..*[?(@.cardId == '${parent_card_id}')]`;
        const parent_result = JSONPath({path: search_all_cardIds_for_parent_id_jsonpath, json: tree});
        //console.log(`tree: ${JSON.stringify(tree)}`);
        //console.log(`parent_result: ${JSON.stringify(parent_result)}`);

        let parent_card;
        if (parent_result.length > 0) {
            parent_card = parent_result[0]; // HACK: we should only ever get 1 since cardId is unique, but I am not bothering to check
        }
        else {
            parent_card = tree[parent_card_id];
        }

        if (parent_card) {
            if (!parent_card.children) {
                parent_card.children = {};
            }
            parent_card.children[card_id] = card_data[card_id];
        }
        else {
            // recurse!
            addCardToTreeMap(parent_card_id, card_data, tree);
        }
    }
    else {
        // no relationship, put directly in root of tree
        tree[card_id] = card_data[card_id];
    }
}

export default function CardTreeView ( {gamestate, visible} ) {
    return(
        <Box>
            <RichTreeView items={cards_to_treeitems(gamestate)} />
        </Box>
    );
}