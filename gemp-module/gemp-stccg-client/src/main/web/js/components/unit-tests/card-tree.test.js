import {describe, beforeEach, expect, test} from '@jest/globals';
//import React from 'react';
//import {render, fireEvent, screen} from '@testing-library/react';
import {card_to_treeitem, cardFlatMapToTreeMap} from '../card-tree.jsx';
//import CardTree from '../card-tree.jsx';


describe('internal funcs', () => {
    test('card_to_treeitem turns a card into a new tree friendly object', () => {
        let cardInputData = {
            "cardId": 30,
            "title": "Jean-Luc Picard",
            "blueprintId": "161_041",
            "owner": "andrew2",
            "affiliation": "ROMULAN",
            "isStopped": false,
            "imageUrl": "https://www.trekcc.org/1e/cardimages/engage/41.jpg",
            "cardType": "PERSONNEL",
            "uniqueness": "UNIQUE",
            "hasUniversalIcon": false,
            "isInPlay": false
        }

        let expected = {
            id: "30",
            label: "Jean-Luc Picard"
        }

        let actual = card_to_treeitem(cardInputData);

        expect(actual).toMatchObject(expected);
    });

    test('card_to_treeitem handles not getting passed a card', () => {
        let expected = {
            id: "-1",
            label: "ERROR"
        }

        let actual = card_to_treeitem();

        expect(actual).toMatchObject(expected);
    });
});

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
        expect(() => cardFlatMapToTreeMap(not_array, card_map)).toThrow(TypeError);

        not_array = null;
        expect(() => cardFlatMapToTreeMap(not_array, card_map)).toThrow(TypeError);
        
        not_array = "";
        expect(() => cardFlatMapToTreeMap(not_array, card_map)).toThrow(TypeError);

        not_array = 16;
        expect(() => cardFlatMapToTreeMap(not_array, card_map)).toThrow(TypeError);

        not_array = {};
        expect(() => cardFlatMapToTreeMap(not_array, card_map)).toThrow(TypeError);
    });

    test('errors if not given a card data map', () => {
        let card_id_array = [16];
        let not_map;
        expect(() => cardFlatMapToTreeMap(card_id_array, not_map)).toThrow(TypeError);

        not_map = null;
        expect(() => cardFlatMapToTreeMap(card_id_array, not_map)).toThrow(TypeError);

        not_map = "";
        expect(() => cardFlatMapToTreeMap(card_id_array, not_map)).toThrow(TypeError);

        not_map = 16;
        expect(() => cardFlatMapToTreeMap(card_id_array, not_map)).toThrow(TypeError);

        not_map = [];
        expect(() => cardFlatMapToTreeMap(card_id_array, not_map)).toThrow(TypeError);
    });

    test('can build a flat tree', () => {
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
            }
        };
        //expect(typeof full_card_data_no_nesting).toBe('object');
        //expect(typeof full_card_data_no_nesting["16"]).toBe('object');
        //expect(Object.entries(full_card_data_no_nesting).length).toBe(3);//  Object.hasOwnProp (full_card_data_no_nesting, 'entries')).toBe(true);
        //expect(typeof full_card_data_no_nesting.entries).toBe('function');
        
        let actual = cardFlatMapToTreeMap(card_id_array, full_card_data_no_nesting);

        // no change because it's already flat
        let expected = full_card_data_no_nesting;

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
        
        let actual = cardFlatMapToTreeMap(card_id_array, full_card_data_no_nesting);

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
        
        let actual = cardFlatMapToTreeMap(card_id_array, full_card_data_no_nesting);

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
        
        let actual = cardFlatMapToTreeMap(card_id_array, full_card_data_no_nesting);

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

    test('can build a nested tree when cards are in parent-child order', () => {
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
        
        let actual = cardFlatMapToTreeMap(card_id_array, full_card_data_no_nesting);

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

    test('can build a nested tree when cards are not in parent-child order', () => {
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
        
        let actual = cardFlatMapToTreeMap(card_id_array, full_card_data_no_nesting);

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

    test('can build a nested tree when card array does not match card_data order', () => {
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
        
        let actual = cardFlatMapToTreeMap(card_id_array, full_card_data_no_nesting);

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
});