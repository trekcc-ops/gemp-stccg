import {describe, beforeEach, expect, test} from '@jest/globals';
//import React from 'react';
//import {render, fireEvent, screen} from '@testing-library/react';
import {card_to_treeitem} from '../card-tree-view.jsx';
//import CardTreeView from '../card-tree-view.jsx';


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
