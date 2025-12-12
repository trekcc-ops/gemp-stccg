import {describe, expect, test} from '@jest/globals';
import CardTreeModel from '../lib/cardTreeModel.js';

describe('cardFlatMapToTreeMap', () => {
    test('errors if not given a cardid array', () => {
        let not_array;
        let card_map = {
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
        }

        not_array = null;
        expect(() => CardTreeModel.cardFlatMapToTreeMap(card_map, not_array)).toThrow(TypeError);
        
        not_array = "";
        expect(() => CardTreeModel.cardFlatMapToTreeMap(card_map, not_array)).toThrow(TypeError);

        not_array = 16;
        expect(() => CardTreeModel.cardFlatMapToTreeMap(card_map, not_array)).toThrow(TypeError);

        not_array = {};
        expect(() => CardTreeModel.cardFlatMapToTreeMap(card_map, not_array)).toThrow(TypeError);
    });

    test('errors if not given a card data map', () => {
        let card_id_array = [16];
        let not_map;
        expect(() => CardTreeModel.cardFlatMapToTreeMap(not_map, card_id_array)).toThrow(TypeError);

        not_map = null;
        expect(() => CardTreeModel.cardFlatMapToTreeMap(not_map, card_id_array)).toThrow(TypeError);

        not_map = "";
        expect(() => CardTreeModel.cardFlatMapToTreeMap(not_map, card_id_array)).toThrow(TypeError);

        not_map = 16;
        expect(() => CardTreeModel.cardFlatMapToTreeMap(not_map, card_id_array)).toThrow(TypeError);

        not_map = [];
        expect(() => CardTreeModel.cardFlatMapToTreeMap(not_map, card_id_array)).toThrow(TypeError);
    });

    test('can build a flat tree with no filter', () => {
        let full_card_data_no_nesting = {
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            }
        };
        //expect(typeof full_card_data_no_nesting).toBe('object');
        //expect(typeof full_card_data_no_nesting["16"]).toBe('object');
        //expect(Object.entries(full_card_data_no_nesting).length).toBe(3);//  Object.hasOwnProp (full_card_data_no_nesting, 'entries')).toBe(true);
        //expect(typeof full_card_data_no_nesting.entries).toBe('function');

        let actual = CardTreeModel.cardFlatMapToTreeMap(full_card_data_no_nesting);

        // no change because it's already flat
        let expected = full_card_data_no_nesting;

        expect(actual).toMatchObject(expected);
    });

    test('can build a flat tree with a filter', () => {
        let card_id_array = [16, 2];
        let full_card_data_no_nesting = {
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            }
        };
        //expect(typeof full_card_data_no_nesting).toBe('object');
        //expect(typeof full_card_data_no_nesting["16"]).toBe('object');
        //expect(Object.entries(full_card_data_no_nesting).length).toBe(3);//  Object.hasOwnProp (full_card_data_no_nesting, 'entries')).toBe(true);
        //expect(typeof full_card_data_no_nesting.entries).toBe('function');
        
        let actual = CardTreeModel.cardFlatMapToTreeMap(full_card_data_no_nesting, card_id_array);

        // Filter out #23
        let expected = {
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            }
        };

        expect(actual).toMatchObject(expected);
    });

    test('handles being given ids it doesnt have', () => {
        let card_id_array = [32];
        let full_card_data_no_nesting = {
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            }
        }
        
        let actual = CardTreeModel.cardFlatMapToTreeMap(full_card_data_no_nesting, card_id_array);

        let expected = {};

        expect(actual).toMatchObject(expected);
    });

    test('ignores ids its not told about', () => {
        let card_id_array = [16, 2, 23];
        let full_card_data_no_nesting = {
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "29": {
                "cardId": 29,
                "title": "Agyer",
                "blueprintId": "161_038",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/engage/38.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            }
        }
        
        let actual = CardTreeModel.cardFlatMapToTreeMap(full_card_data_no_nesting, card_id_array);

        // should not contain #23
        let expected = {
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
        };

        expect(actual).toMatchObject(expected);
    });

    test('ignores duplicate ids', () => {
        let card_id_array = [16, 16];
        let full_card_data_no_nesting = {
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            }
        }
        
        let actual = CardTreeModel.cardFlatMapToTreeMap(full_card_data_no_nesting, card_id_array);

        let expected = {
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            }
        };

        expect(actual).toMatchObject(expected);
    });

    test('can build a nested tree when cards are in parent-child order, no filter', () => {
        // 1 exists, then 16 which is attached to 1
        let full_card_data_no_nesting = {
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
        };
        //expect(typeof full_card_data_no_nesting).toBe('object');
        //expect(typeof full_card_data_no_nesting["16"]).toBe('object');
        //expect(Object.entries(full_card_data_no_nesting).length).toBe(3);//  Object.hasOwnProp (full_card_data_no_nesting, 'entries')).toBe(true);
        //expect(typeof full_card_data_no_nesting.entries).toBe('function');
        
        let actual = CardTreeModel.cardFlatMapToTreeMap(full_card_data_no_nesting);

        let expected = {
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true,
                "children": {
                    "16": {
                        "cardId": 16,
                        "title": "Varel",
                        "blueprintId": "101_330",
                        "owner": "andrew2",
                        "locationId": 8,
                        "affiliation": "ROMULAN",
                        "attachedToCardId": 1,
                        "isStopped": false,
                        "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                        "cardType": "PERSONNEL",
                        "uniqueness": "UNIVERSAL",
                        "hasUniversalIcon": true,
                        "isInPlay": true
                    }
                }
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            }
        };

        expect(actual).toMatchObject(expected);
    });

    test('can build a nested tree when cards are in parent-child order, with filter', () => {
        let card_id_array = [1, 16, 2, 23];
        // 1 exists, then 16 which is attached to 1
        let full_card_data_no_nesting = {
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
        };
        //expect(typeof full_card_data_no_nesting).toBe('object');
        //expect(typeof full_card_data_no_nesting["16"]).toBe('object');
        //expect(Object.entries(full_card_data_no_nesting).length).toBe(3);//  Object.hasOwnProp (full_card_data_no_nesting, 'entries')).toBe(true);
        //expect(typeof full_card_data_no_nesting.entries).toBe('function');
        
        let actual = CardTreeModel.cardFlatMapToTreeMap(full_card_data_no_nesting, card_id_array);

        let expected = {
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true,
                "children": {
                    "16": {
                        "cardId": 16,
                        "title": "Varel",
                        "blueprintId": "101_330",
                        "owner": "andrew2",
                        "locationId": 8,
                        "affiliation": "ROMULAN",
                        "attachedToCardId": 1,
                        "isStopped": false,
                        "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                        "cardType": "PERSONNEL",
                        "uniqueness": "UNIVERSAL",
                        "hasUniversalIcon": true,
                        "isInPlay": true
                    }
                }
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            }
        };

        expect(actual).toMatchObject(expected);
    });

    test('can build a nested tree when cards are not in parent-child order, no filter', () => {
        // 1 exists, then 16 which is attached to 1
        let full_card_data_no_nesting = {
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            }
        };
        //expect(typeof full_card_data_no_nesting).toBe('object');
        //expect(typeof full_card_data_no_nesting["16"]).toBe('object');
        //expect(Object.entries(full_card_data_no_nesting).length).toBe(3);//  Object.hasOwnProp (full_card_data_no_nesting, 'entries')).toBe(true);
        //expect(typeof full_card_data_no_nesting.entries).toBe('function');
        
        let actual = CardTreeModel.cardFlatMapToTreeMap(full_card_data_no_nesting);

        let expected = {
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true,
                "children": {
                    "16": {
                        "cardId": 16,
                        "title": "Varel",
                        "blueprintId": "101_330",
                        "owner": "andrew2",
                        "locationId": 8,
                        "affiliation": "ROMULAN",
                        "attachedToCardId": 1,
                        "isStopped": false,
                        "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                        "cardType": "PERSONNEL",
                        "uniqueness": "UNIVERSAL",
                        "hasUniversalIcon": true,
                        "isInPlay": true
                    }
                }
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            }
        };

        expect(actual).toMatchObject(expected);
    });

    test('can build a nested tree when cards are not in parent-child order, with filter', () => {
        let card_id_array = [1, 16, 2, 23];
        // 1 exists, then 16 which is attached to 1
        let full_card_data_no_nesting = {
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            }
        };
        //expect(typeof full_card_data_no_nesting).toBe('object');
        //expect(typeof full_card_data_no_nesting["16"]).toBe('object');
        //expect(Object.entries(full_card_data_no_nesting).length).toBe(3);//  Object.hasOwnProp (full_card_data_no_nesting, 'entries')).toBe(true);
        //expect(typeof full_card_data_no_nesting.entries).toBe('function');
        
        let actual = CardTreeModel.cardFlatMapToTreeMap(full_card_data_no_nesting, card_id_array);

        let expected = {
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true,
                "children": {
                    "16": {
                        "cardId": 16,
                        "title": "Varel",
                        "blueprintId": "101_330",
                        "owner": "andrew2",
                        "locationId": 8,
                        "affiliation": "ROMULAN",
                        "attachedToCardId": 1,
                        "isStopped": false,
                        "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                        "cardType": "PERSONNEL",
                        "uniqueness": "UNIVERSAL",
                        "hasUniversalIcon": true,
                        "isInPlay": true
                    }
                }
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            }
        };

        expect(actual).toMatchObject(expected);
    });

    test('can build a nested tree when card array does not match card_data order, no filter', () => {
        // 1 exists, then 16 which is attached to 1
        let full_card_data_no_nesting = {
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
        };
        //expect(typeof full_card_data_no_nesting).toBe('object');
        //expect(typeof full_card_data_no_nesting["16"]).toBe('object');
        //expect(Object.entries(full_card_data_no_nesting).length).toBe(3);//  Object.hasOwnProp (full_card_data_no_nesting, 'entries')).toBe(true);
        //expect(typeof full_card_data_no_nesting.entries).toBe('function');
        
        let actual = CardTreeModel.cardFlatMapToTreeMap(full_card_data_no_nesting);

        let expected = {
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true,
                "children": {
                    "16": {
                        "cardId": 16,
                        "title": "Varel",
                        "blueprintId": "101_330",
                        "owner": "andrew2",
                        "locationId": 8,
                        "affiliation": "ROMULAN",
                        "attachedToCardId": 1,
                        "isStopped": false,
                        "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                        "cardType": "PERSONNEL",
                        "uniqueness": "UNIVERSAL",
                        "hasUniversalIcon": true,
                        "isInPlay": true
                    }
                }
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            }
        };

        expect(actual).toMatchObject(expected);
    });

    test('can build a nested tree when card array does not match card_data order, with filter', () => {
        let card_id_array = [23, 1, 2, 16];
        // 1 exists, then 16 which is attached to 1
        let full_card_data_no_nesting = {
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
        };
        //expect(typeof full_card_data_no_nesting).toBe('object');
        //expect(typeof full_card_data_no_nesting["16"]).toBe('object');
        //expect(Object.entries(full_card_data_no_nesting).length).toBe(3);//  Object.hasOwnProp (full_card_data_no_nesting, 'entries')).toBe(true);
        //expect(typeof full_card_data_no_nesting.entries).toBe('function');
        
        let actual = CardTreeModel.cardFlatMapToTreeMap(full_card_data_no_nesting, card_id_array);

        let expected = {
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true,
                "children": {
                    "16": {
                        "cardId": 16,
                        "title": "Varel",
                        "blueprintId": "101_330",
                        "owner": "andrew2",
                        "locationId": 8,
                        "affiliation": "ROMULAN",
                        "attachedToCardId": 1,
                        "isStopped": false,
                        "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                        "cardType": "PERSONNEL",
                        "uniqueness": "UNIVERSAL",
                        "hasUniversalIcon": true,
                        "isInPlay": true
                    }
                }
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                //"attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            }
        };

        expect(actual).toMatchObject(expected);
    });

    // Well now idk why this succeeds, lmao
    test('can build a four level nested tree when cards are in parent-child order, no filter', () => {
        // 1 exists, then 16 which is attached to 1
        let full_card_data_no_nesting = {
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                "attachedToCardId": 16,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "attachedToCardId": 2,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
        };
        //expect(typeof full_card_data_no_nesting).toBe('object');
        //expect(typeof full_card_data_no_nesting["16"]).toBe('object');
        //expect(Object.entries(full_card_data_no_nesting).length).toBe(3);//  Object.hasOwnProp (full_card_data_no_nesting, 'entries')).toBe(true);
        //expect(typeof full_card_data_no_nesting.entries).toBe('function');
        
        let actual = CardTreeModel.cardFlatMapToTreeMap(full_card_data_no_nesting);

        let expected = {
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true,
                "children": {
                    "16": {
                        "cardId": 16,
                        "title": "Varel",
                        "blueprintId": "101_330",
                        "owner": "andrew2",
                        "locationId": 8,
                        "affiliation": "ROMULAN",
                        "attachedToCardId": 1,
                        "isStopped": false,
                        "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                        "cardType": "PERSONNEL",
                        "uniqueness": "UNIVERSAL",
                        "hasUniversalIcon": true,
                        "isInPlay": true,
                        "children": {
                            "2": {
                                "cardId": 2,
                                "title": "Baran",
                                "blueprintId": "101_290",
                                "owner": "andrew2",
                                "locationId": 8,
                                "affiliation": "NON_ALIGNED",
                                "attachedToCardId": 16,
                                "isStopped": false,
                                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                                "cardType": "PERSONNEL",
                                "uniqueness": "UNIQUE",
                                "hasUniversalIcon": false,
                                "isInPlay": true,
                                "children": {
                                    "23": {
                                        "cardId": 23,
                                        "title": "Mirok",
                                        "blueprintId": "101_310",
                                        "owner": "andrew2",
                                        "locationId": 8,
                                        "affiliation": "ROMULAN",
                                        "attachedToCardId": 2,
                                        "isStopped": false,
                                        "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                                        "cardType": "PERSONNEL",
                                        "uniqueness": "UNIQUE",
                                        "hasUniversalIcon": false,
                                        "isInPlay": true
                                    },
                                }
                            },
                        }
                    }
                }
            },
        };

        expect(actual).toMatchObject(expected);
    });

    test('can build a four level nested tree when cards are in parent-child order, with filter', () => {
        let card_id_array = [1, 16, 2, 23];
        // 1 exists, then 16 which is attached to 1
        let full_card_data_no_nesting = {
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "16": {
                "cardId": 16,
                "title": "Varel",
                "blueprintId": "101_330",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "attachedToCardId": 1,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true
            },
            "2": {
                "cardId": 2,
                "title": "Baran",
                "blueprintId": "101_290",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "NON_ALIGNED",
                "attachedToCardId": 16,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
            "23": {
                "cardId": 23,
                "title": "Mirok",
                "blueprintId": "101_310",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "attachedToCardId": 2,
                "isStopped": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                "cardType": "PERSONNEL",
                "uniqueness": "UNIQUE",
                "hasUniversalIcon": false,
                "isInPlay": true
            },
        };
        //expect(typeof full_card_data_no_nesting).toBe('object');
        //expect(typeof full_card_data_no_nesting["16"]).toBe('object');
        //expect(Object.entries(full_card_data_no_nesting).length).toBe(3);//  Object.hasOwnProp (full_card_data_no_nesting, 'entries')).toBe(true);
        //expect(typeof full_card_data_no_nesting.entries).toBe('function');
        
        let actual = CardTreeModel.cardFlatMapToTreeMap(full_card_data_no_nesting, card_id_array);

        let expected = {
            "1": {
                "cardId": 1,
                "title": "Romulan Shuttle",
                "blueprintId": "116_108",
                "owner": "andrew2",
                "locationId": 8,
                "affiliation": "ROMULAN",
                "isStopped": false,
                "rangeAvailable": 2,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/bog/romulanshuttle.gif",
                "cardType": "SHIP",
                "uniqueness": "UNIVERSAL",
                "hasUniversalIcon": true,
                "isInPlay": true,
                "children": {
                    "16": {
                        "cardId": 16,
                        "title": "Varel",
                        "blueprintId": "101_330",
                        "owner": "andrew2",
                        "locationId": 8,
                        "affiliation": "ROMULAN",
                        "attachedToCardId": 1,
                        "isStopped": false,
                        "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR330.jpg",
                        "cardType": "PERSONNEL",
                        "uniqueness": "UNIVERSAL",
                        "hasUniversalIcon": true,
                        "isInPlay": true,
                        "children": {
                            "2": {
                                "cardId": 2,
                                "title": "Baran",
                                "blueprintId": "101_290",
                                "owner": "andrew2",
                                "locationId": 8,
                                "affiliation": "NON_ALIGNED",
                                //"attachedToCardId": 1,
                                "isStopped": false,
                                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR290.jpg",
                                "cardType": "PERSONNEL",
                                "uniqueness": "UNIQUE",
                                "hasUniversalIcon": false,
                                "isInPlay": true,
                                "children": {
                                    "23": {
                                        "cardId": 23,
                                        "title": "Mirok",
                                        "blueprintId": "101_310",
                                        "owner": "andrew2",
                                        "locationId": 8,
                                        "affiliation": "ROMULAN",
                                        //"attachedToCardId": 1,
                                        "isStopped": false,
                                        "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/PR310.jpg",
                                        "cardType": "PERSONNEL",
                                        "uniqueness": "UNIQUE",
                                        "hasUniversalIcon": false,
                                        "isInPlay": true
                                    },
                                }
                            },
                        }
                    }
                }
            },
        };

        expect(actual).toMatchObject(expected);
    });
});