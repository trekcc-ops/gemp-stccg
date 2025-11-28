import {JSONPath} from 'jsonpath-plus';

export default class CardTreeModel {
    constructor() {
        return {};
    }
    
    /**
     * Creates a CardTreeModel from a flat JSON map {"id": {"card_data": ""}},
     *  such as GameState's visibleCardsInGame object.
     */
    static cardFlatMapToTreeMap(card_data, filter_by_ids) {
        // Type check card_data for Map, Set or Object, otherwise throw error
        if (! ((card_data instanceof Map) || 
            (card_data instanceof Set) || 
            (card_data.constructor === Object))) {

                throw new TypeError(`card_data '${card_data}' must be a Map, Set, or Object that is addressable with object[]`);
        }

        if (typeof filter_by_ids === 'undefined') {
            // No filter given to us, just iterate over the whole map
            let new_tree = {};
            if ((typeof card_data instanceof Map) || (typeof card_data instanceof Set)) {
                console.log("undefined, Map/Set");
                for (const card_id of card_data.keys()) {
                    CardTreeModel.addCardToTreeMap(card_id, card_data, new_tree);
                }
            }
            else {
                for (const card_id of Object.keys(card_data)) {
                    //console.log(`Processing card_id ${card_id}`);
                    CardTreeModel.addCardToTreeMap(card_id, card_data, new_tree);
                }
            }
            
            return new_tree;
        }
        else {
            // Type check filter_by_ids parameter.
            if (!(filter_by_ids instanceof Array)) {
                throw new TypeError(`filter_by_ids '${filter_by_ids}' must be an Array.`);
            }

            let new_tree = {};
            // Only use ids from the filter
            for (const card_id of filter_by_ids) {
                CardTreeModel.addCardToTreeMap(card_id, card_data, new_tree);
            }
            return new_tree;
        }
    }

    static addCardToTreeMap(card_id, card_data, tree) {
        // do we even have the requested data?
        if (!card_data[card_id]) {
            return;
        }

        // already in the tree? skip
        // NOTE: Use == not === here for consistent matching
        //   $..*  means "all objects in the tree"
        //   [?(@.cardId)]  means "if they have a card id attribute"
        //   == '${cardid}'  means "matching passed in card_id"
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
            //   $..*  means "all objects in the tree"
            //   [?(@.cardId)]  means "if they have a card id attribute"
            //   == '${parent_card_id}'  means "matching passed in parent_card_id"
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