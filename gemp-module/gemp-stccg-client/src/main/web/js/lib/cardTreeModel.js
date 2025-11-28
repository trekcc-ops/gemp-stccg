import {JSONPath} from 'jsonpath-plus';

export default class CardTreeModel {
    constructor() {
        return {};
    }
    
    /**
     * Creates a CardTreeModel from a flat JSON map {"id": {"card_data": ""}},
     *  such as GameState's visibleCardsInGame object.
     */
    static cardFlatMapToTreeMap(card_ids_to_use, card_data) {
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
            CardTreeModel.addCardToTreeMap(card_id, card_data, new_tree);
        }
        return new_tree;
    }

    static addCardToTreeMap(card_id, card_data, tree) {
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
                this.addCardToTreeMap(parent_card_id, card_data, tree);
            }
        }
        else {
            // no relationship, put directly in root of tree
            tree[card_id] = card_data[card_id];
        }
    }
}