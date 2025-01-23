import {describe, beforeEach, expect, test} from '@jest/globals'
import Cookies from "js-cookie";
import {cardCache, cardScale, packBlueprints, createCardDiv, getFoilPresentation, createFullCardDiv, createSimpleCardDiv, getCardDivFromId} from "../gemp-022/jCards.js";
import Card from "../gemp-022/jCards.js";

beforeEach(() => {
});

describe('validity', () => {
    test('jCards.js is valid syntax', () => {
        // noop
    });
});

describe('Constructor', () => {
    test('constructor saves settings', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
    
        expect(cardUnderTest.blueprintId).toBe("101_312");
        expect(cardUnderTest.zone).toBe("ATTACHED");
        expect(cardUnderTest.cardId).toBe("1");
        expect(cardUnderTest.owner).toBe("andrew2");
        expect(cardUnderTest.imageUrl).toBe("https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg");
        expect(cardUnderTest.locationIndex).toBe(8);
        expect(cardUnderTest.upsideDown).toBe(false);
    });
    
    test('constructor throws error when blueprintId is bad', () => {
        let blueprintId=101312;
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        expect(() => new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });
    
    test('constructor throws error when zone is bad', () => {
        let blueprintId="101_312";
        let zone="BLORG";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        expect(() => new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    
        zone=123;
        expect(() => new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    
        zone={zone:"ATTACHED"};
        expect(() => new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });
    
    test('constructor throws error when cardId is bad', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId=1;
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        expect(() => new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });
    
    test('constructor throws error when owner is bad', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner=false;
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        expect(() => new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });
    
    test('constructor throws error when imageUrl is bad', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl={url: "https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg"};
        let locationIndex="8";
        let upsideDown=false;
    
        expect(() => new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });
    
    test('constructor throws error when locationIndex is bad', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex=8;
        let upsideDown=false;
    
        expect(() => new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });
    
    test('constructor throws error when upsideDown is bad', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown="false";
    
        expect(() => new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });
    
    test('constructor sets foil value based on last blueprintId character', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.foil).toBe(false);
    
        blueprintId="101_312*";
        cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.foil).toBe(true);
    });
    
    test('constructor sets tengwar value based on last blueprintId character', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.tengwar).toBe(false);
    
        blueprintId="101_312T";
        cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.tengwar).toBe(true);
    });
    
    test('constructor sets wiki value based on blueprintId name', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        // apparently everything has a wiki by default
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.hasWiki).toBe(true);
    
        blueprintId="Special-01";
        cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.hasWiki).toBe(false);
    });
    
    test('constructor sets attachedCards to an empty array', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        let empty_array = [];
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.attachedCards).toStrictEqual(empty_array);
    });
    
    test('constructor sets a specific image URL for rules images', () => {
        let blueprintId="rules";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        // BUG: This should probably say "../../images/rules.png" but it's unclear how to get this to work in Jest
        //      so instead I just verify it's stubbed as a file which means it's one of our imports. Close enough.
        expect(cardUnderTest.imageUrl).toBe("test-file-stub");
    });
    
    test('constructor does not add existing cards to the cache', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        // WORKAROUND: Having the cardCache in the global namespace means we can't easily reset it,
        //             so this object needs to match all card types (except foil) created in previous tests.
        //             God forbid we ever run the tests out of order.
        let card_cache_from_previous_tests = {
            "101_312": {
                "errata": false,
                "horizontal": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg",
            },
            "101_312T": {
                "errata": false,
                "horizontal": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg",
            },
            "Special-01": {
                "errata": false,
                "horizontal": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg",
            },
        }
    
        expect(cardCache).toStrictEqual(card_cache_from_previous_tests);
        let _cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardCache).toStrictEqual(card_cache_from_previous_tests);
    });
    
    test('constructor adds a new card to the cache', () => {
        let blueprintId="101_313";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        // WORKAROUND: Having the cardCache in the global namespace means we can't easily reset it,
        //             so this object needs to match all card types (except foil) created in previous tests.
        //             God forbid we ever run the tests out of order.
        let card_cache_from_previous_tests = {
            "101_312": {
                "errata": false,
                "horizontal": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg",
            },
            "101_312T": {
                "errata": false,
                "horizontal": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg",
            },
            "Special-01": {
                "errata": false,
                "horizontal": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg",
            },
        }
    
        let card_cache_with_new_id = {
            "101_312": {
                "errata": false,
                "horizontal": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg",
            },
            "101_312T": {
                "errata": false,
                "horizontal": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg",
            },
            "Special-01": {
                "errata": false,
                "horizontal": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg",
            },
            "101_313": {
                "errata": false,
                "horizontal": false,
                "imageUrl": "https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg",
            }
        }
    
        expect(cardCache).toStrictEqual(card_cache_from_previous_tests);
    
        let _cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
    
        expect(cardCache).toStrictEqual(card_cache_with_new_id)    
    });
});

describe('property getters', () => {
    test('isTengwar function reponds with card property', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.tengwar).toBe(false);
        expect(cardUnderTest.isTengwar()).toBe(false);
    
        cardUnderTest.tengwar = true;
        expect(cardUnderTest.isTengwar()).toBe(true);
    });
    
    test('isFoil function reponds with card property', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.foil).toBe(false);
        expect(cardUnderTest.isFoil()).toBe(false);
    
        cardUnderTest.foil = true;
        expect(cardUnderTest.isFoil()).toBe(true);
    });
    
    test('isUpsideDown function reponds with card property', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.upsideDown).toBe(false);
        expect(cardUnderTest.isUpsideDown()).toBe(false);
    
        cardUnderTest.upsideDown = true;
        expect(cardUnderTest.isUpsideDown()).toBe(true);
    });
    
    test('hasErrata function reponds with card property', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.errata).toBe(false);
        expect(cardUnderTest.hasErrata()).toBe(false);
    
        cardUnderTest.errata = true;
        expect(cardUnderTest.hasErrata()).toBe(true);
    });
    
    test('hasErrata function always reponds true if set is between 50 and 89', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.errata).toBe(false);
        expect(cardUnderTest.hasErrata()).toBe(false);
    
        cardUnderTest.blueprintId="49_312";
        expect(cardUnderTest.errata).toBe(false);
        expect(cardUnderTest.hasErrata()).toBe(false);
    
        // start
        cardUnderTest.blueprintId="50_312";
        expect(cardUnderTest.errata).toBe(false);
        expect(cardUnderTest.hasErrata()).toBe(true);
    
        cardUnderTest.blueprintId="51_312";
        expect(cardUnderTest.errata).toBe(false);
        expect(cardUnderTest.hasErrata()).toBe(true);
    
        cardUnderTest.blueprintId="88_312";
        expect(cardUnderTest.errata).toBe(false);
        expect(cardUnderTest.hasErrata()).toBe(true);
    
        cardUnderTest.blueprintId="89_312";
        expect(cardUnderTest.errata).toBe(false);
        expect(cardUnderTest.hasErrata()).toBe(true);
        // end
    
        cardUnderTest.blueprintId="90_312";
        expect(cardUnderTest.errata).toBe(false);
        expect(cardUnderTest.hasErrata()).toBe(false);
    });
    
    test('isPack function checks blueprintId against static array', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.isPack()).toBe(false);
    
        cardUnderTest.blueprintId="Special-01";
        expect(cardUnderTest.isPack()).toBe(true);
    });

    test('isHorizontal function always returns false', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.isHorizontal()).toBe(false);
    });
});

describe('wiki stuff', () => {
    test('getUrlByBlueprintId returns the test stub if the card is special', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
        let ignoreErrata = true;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        cardUnderTest.blueprintId = "Special-01";

        // BUG: This should probably say "../../images/rules.png" but it's unclear how to get this to work in Jest
        //      so instead I just verify it's stubbed as a file which means it's one of our imports. Close enough.
        expect(cardUnderTest.getUrlByBlueprintId(cardUnderTest.blueprintId, ignoreErrata)).toBe('test-file-stub');
    });
    
    test('getUrlByBlueprintId returns a local URL if we have locally overriding errata and ignore is false', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
        let ignoreErrata = false;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        cardUnderTest.blueprintId = "7_10"; // has errata

        expect(cardUnderTest.getUrlByBlueprintId(cardUnderTest.blueprintId, ignoreErrata)).toBe('/gemp-module/images/erratas/LOTR07010.jpg');
    });

    test('getUrlByBlueprintId returns the remote URL', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
        let ignoreErrata = true;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        cardUnderTest.blueprintId = "7_10"; // has errata

        expect(cardUnderTest.getUrlByBlueprintId(cardUnderTest.blueprintId, ignoreErrata)).toBe('https://i.lotrtcgpc.net/decipher/LOTR07010.jpg');
    });

    test('getUrlByBlueprintId returns the remote URL if we have locally overriding errata but ignore is true', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
        let ignoreErrata = true;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        cardUnderTest.blueprintId = "7_10"; // has errata

        expect(cardUnderTest.getUrlByBlueprintId(cardUnderTest.blueprintId, ignoreErrata)).toBe('https://i.lotrtcgpc.net/decipher/LOTR07010.jpg');

        cardUnderTest.blueprintId = "1_3"; // has errata
        expect(cardUnderTest.getUrlByBlueprintId(cardUnderTest.blueprintId, ignoreErrata)).toBe('https://i.lotrtcgpc.net/decipher/LOTR01003.jpg');

        cardUnderTest.blueprintId = "1_300";
        expect(cardUnderTest.getUrlByBlueprintId(cardUnderTest.blueprintId, ignoreErrata)).toBe('https://i.lotrtcgpc.net/decipher/LOTR01300.jpg');
    });

    test('getUrlByBlueprintId returns the remote URL with a capital O and the set number reduced if the card is masterwork', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
        let ignoreErrata = true;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        cardUnderTest.blueprintId = "12_195"; // masterwork

        expect(cardUnderTest.getUrlByBlueprintId(cardUnderTest.blueprintId, ignoreErrata)).toBe('https://i.lotrtcgpc.net/decipher/LOTR12O01.jpg');

        cardUnderTest.blueprintId = "12_196"; // masterwork
        expect(cardUnderTest.getUrlByBlueprintId(cardUnderTest.blueprintId, ignoreErrata)).toBe('https://i.lotrtcgpc.net/decipher/LOTR12O02.jpg');
    });

    test('getWikiLink', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
        let ignoreErrata = true;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        cardUnderTest.blueprintId = "7_10"; // has errata
        
        // URL starts as https://i.lotrtcgpc.net/decipher/LOTR07010.jpg
        // they strip off .jpg and prepend http://wiki.lotrtcgpc.net/wiki/
        expect(cardUnderTest.getWikiLink()).toBe('http://wiki.lotrtcgpc.net/wiki/LOTR07010');
    });

    test('hasWikiInfo function reponds with card property', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.hasWikiInfo()).toBe(true);
    
        cardUnderTest.hasWiki = false;
        expect(cardUnderTest.hasWikiInfo()).toBe(false);
    });

    test('getMainLocation always returns the same string', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);

        // real card values
        expect(cardUnderTest.getMainLocation(7, 10)).toBe('https://i.lotrtcgpc.net/decipher/');

        // undefined
        let undef_set;
        let undef_card;
        expect(cardUnderTest.getMainLocation(undef_set, undef_card)).toBe('https://i.lotrtcgpc.net/decipher/');
        
        // strings
        expect(cardUnderTest.getMainLocation("7", "10")).toBe('https://i.lotrtcgpc.net/decipher/');

        // other
        expect(cardUnderTest.getMainLocation(["7"], {"10": "lol"})).toBe('https://i.lotrtcgpc.net/decipher/');
    });

    test('getMasterworksOffset return values', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);

        expect(cardUnderTest.getMasterworksOffset(17)).toBe(148);
        expect(cardUnderTest.getMasterworksOffset(18)).toBe(140);
        expect(cardUnderTest.getMasterworksOffset(30)).toBe(194);
        // garbage
        expect(cardUnderTest.getMasterworksOffset("asdf")).toBe(194);
        expect(cardUnderTest.getMasterworksOffset({"number": 17})).toBe(194);
    });

    test('isMasterworks return values', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);

        // possible, and true conditions
        expect(cardUnderTest.isMasterworks(12, 195)).toBe(true);
        expect(cardUnderTest.isMasterworks(13, 195)).toBe(true);
        expect(cardUnderTest.isMasterworks(15, 195)).toBe(true);
        expect(cardUnderTest.isMasterworks(17, 150)).toBe(true);
        expect(cardUnderTest.isMasterworks(18, 150)).toBe(true);

        // possible, but false conditions
        expect(cardUnderTest.isMasterworks(12, 20)).toBe(false);
        expect(cardUnderTest.isMasterworks(13, 20)).toBe(false);
        expect(cardUnderTest.isMasterworks(15, 20)).toBe(false);
        expect(cardUnderTest.isMasterworks(15, 206)).toBe(false);
        expect(cardUnderTest.isMasterworks(17, 20)).toBe(false);
        expect(cardUnderTest.isMasterworks(18, 20)).toBe(false);

        // set has no masterworks
        expect(cardUnderTest.isMasterworks(1, 195)).toBe(false);

        // garbage
        expect(cardUnderTest.isMasterworks("asdf", 195)).toBe(false);
        expect(cardUnderTest.isMasterworks({"number": 17}, "150")).toBe(false);
    });
});


describe('card sizing', () => {
    test('getHeightForColumnWidth', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);

        // Card scale is 0.718309859155; all input values get divided by it, then rounded down.
        // exact division
        expect(cardUnderTest.getHeightForColumnWidth(0.718309859155)).toBe(1);
        expect(cardUnderTest.getHeightForColumnWidth(1.43661971831)).toBe(2);
        // remainders get rounded down
        expect(cardUnderTest.getHeightForColumnWidth(1)).toBe(1);
        expect(cardUnderTest.getHeightForColumnWidth(2)).toBe(2);
        expect(cardUnderTest.getHeightForColumnWidth(2.5)).toBe(3);

        // unless you're horizontal in which case we just return the input value
        cardUnderTest.horizontal = true;
        expect(cardUnderTest.getHeightForColumnWidth(0.718309859155)).toBe(0.718309859155);
        expect(cardUnderTest.getHeightForColumnWidth(1.43661971831)).toBe(1.43661971831);
        // remainders get rounded down
        expect(cardUnderTest.getHeightForColumnWidth(1)).toBe(1);
        expect(cardUnderTest.getHeightForColumnWidth(2)).toBe(2);
        expect(cardUnderTest.getHeightForColumnWidth(2.5)).toBe(2.5);
    });

    test('getHeightForWidth', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);

        // Card scale is 0.718309859155; all input values get multiplied by it, then rounded down.
        // exact division
        expect(cardUnderTest.getHeightForWidth(0.718309859155)).toBe(1);
        expect(cardUnderTest.getHeightForWidth(1.43661971831)).toBe(2);
        // remainders get rounded down
        expect(cardUnderTest.getHeightForWidth(1)).toBe(1);
        expect(cardUnderTest.getHeightForWidth(2)).toBe(2);
        expect(cardUnderTest.getHeightForWidth(2.5)).toBe(3);

        // unless you're horizontal in which case we divide by the value
        cardUnderTest.horizontal = true;
        expect(cardUnderTest.getHeightForWidth(0.718309859155)).toBe(0);
        expect(cardUnderTest.getHeightForWidth(1.43661971831)).toBe(1);
        // remainders get rounded down
        expect(cardUnderTest.getHeightForWidth(1)).toBe(0);
        expect(cardUnderTest.getHeightForWidth(2)).toBe(1);
        expect(cardUnderTest.getHeightForWidth(2.5)).toBe(1);
    });

    test('getWidthForHeight', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);

        // Card scale is 0.718309859155; all input values get divided by it, then rounded down.
        // exact division
        expect(cardUnderTest.getWidthForHeight(0.718309859155)).toBe(0);
        expect(cardUnderTest.getWidthForHeight(1.43661971831)).toBe(1);
        // remainders get rounded down
        expect(cardUnderTest.getWidthForHeight(1)).toBe(0);
        expect(cardUnderTest.getWidthForHeight(2)).toBe(1);
        expect(cardUnderTest.getWidthForHeight(2.5)).toBe(1);

        // unless you're horizontal in which case we multiply by the value
        cardUnderTest.horizontal = true;
        expect(cardUnderTest.getWidthForHeight(0.718309859155)).toBe(1);
        expect(cardUnderTest.getWidthForHeight(1.43661971831)).toBe(2);
        // remainders get rounded down
        expect(cardUnderTest.getWidthForHeight(1)).toBe(1);
        expect(cardUnderTest.getWidthForHeight(2)).toBe(2);
        expect(cardUnderTest.getWidthForHeight(2.5)).toBe(3);
    });

    test('getWidthForMaxDimension', () => {
        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);

        // Card scale is 0.718309859155; all input values get multiplied by it, then rounded down.
        // exact division
        expect(cardUnderTest.getWidthForMaxDimension(0.718309859155)).toBe(0);
        expect(cardUnderTest.getWidthForMaxDimension(1.43661971831)).toBe(1);
        // remainders get rounded down
        expect(cardUnderTest.getWidthForMaxDimension(1)).toBe(0);
        expect(cardUnderTest.getWidthForMaxDimension(2)).toBe(1);
        expect(cardUnderTest.getWidthForMaxDimension(2.5)).toBe(1);

        // unless you're horizontal in which case we just return the input value
        cardUnderTest.horizontal = true;
        expect(cardUnderTest.getWidthForMaxDimension(0.718309859155)).toBe(0.718309859155);
        expect(cardUnderTest.getWidthForMaxDimension(1.43661971831)).toBe(1.43661971831);
        // remainders get rounded down
        expect(cardUnderTest.getWidthForMaxDimension(1)).toBe(1);
        expect(cardUnderTest.getWidthForMaxDimension(2)).toBe(2);
        expect(cardUnderTest.getWidthForMaxDimension(2.5)).toBe(2.5);
    });
});

describe('jquery-card-details-dialog', () => {
    test('displayCardInfo creates a jQueryUI dialog box', () => {
        document.body.innerHTML = `
            <div id='container'/>
        `;

        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);

        let expected = `<div style="scroll: auto"></div><div class="fullCardDivVertical"><div class="fullCardWrapper"><img class="fullCardImgVertical" src="${imageUrl}"></div><div class="borderOverlayVertical"><img class="actionArea" src="test-file-stub" width="100%" height="100%"></div></div>`;

        let container_jq = $('#container');
        
        cardUnderTest.displayCardInfo(container_jq);
        expect($('#container').html()).toBe(expected);
    });

    test('displayCardInfo creates a jQueryUI dialog box when card is horizontal', () => {
        document.body.innerHTML = `
            <div id='container'/>
        `;

        let blueprintId="101_312"; // start with 101 set so as not to upset cache tests, above
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown);
        cardUnderTest.horizontal = true;

        let expected = `<div style="scroll: auto"></div><div class="fullCardDivHorizontal"><div class="fullCardWrapper"><img class="fullCardImgHorizontal" src="${imageUrl}"></div><div class="borderOverlayHorizontal"><img class="actionArea" src="test-file-stub" width="100%" height="100%"></div></div>`;

        let container_jq = $('#container');
        
        cardUnderTest.displayCardInfo(container_jq);
        expect($('#container').html()).toBe(expected);
    });
});

describe('createCardDiv', () => {
    test('default data', () => {
        // valid data
        var image = "img_filepath.jpg";
        var text; //null or str
        var foil = true; //bool
        var tokens = true; //undef or bool
        var noBorder = true; //bool
        var errata = true; //bool
        var upsideDown = true; //bool
        var cardId; // null or str
        
        let divUnderTest = createCardDiv(image, text, foil, tokens, noBorder, errata, upsideDown, cardId)[0];
        let image_tag = divUnderTest.children[0];
        let errata_tag = divUnderTest.children[1];
        let errata_img = errata_tag.children[0];
        let foil_tag = divUnderTest.children[2];
        let foil_img = foil_tag.children[0];
        let tokens_tag = divUnderTest.children[3];
        let border_tag = divUnderTest.children[4];
        let border_img = border_tag.children[0];

        // null value set to an empty string
        expect(divUnderTest.textContent).toBe("");

        // image tags
        expect(image_tag.src).toBe("http://localhost/" + image);
        expect(image_tag.classList.contains("card_img")).toBe(true);
        expect(image_tag.classList.contains("upside-down")).toBe(true);
        expect(image_tag.classList.contains("card_img_")).toBe(false);

        // errata tags
        expect(errata_tag.classList.contains("errataOverlay")).toBe(true);
        expect(errata_img.src).toBe("http://localhost/" + "test-file-stub");

        // foil tags
        expect(foil_tag.classList.contains('foilOverlay')).toBe(true);
        expect(foil_img.src).toBe("http://localhost/gemp-module/images/" + "holo.jpg");

        // tokens tag
        expect(tokens_tag.classList.contains('tokenOverlay')).toBe(true);

        // border tag
        expect(border_tag.classList.contains('borderOverlay')).toBe(true);
        expect(border_tag.classList.contains('noBorder')).toBe(true);
        expect(border_img.classList.contains('actionArea')).toBe(true);
        expect(border_img.src).toBe("http://localhost/" + "test-file-stub");
    });

    test('if foil cookie true and foil is true change foil img tag', () => {
        // valid data
        var image = "img_filepath.jpg";
        var text; //null or str
        var foil = true; //bool
        var tokens = true; //undef or bool
        var noBorder = true; //bool
        var errata = true; //bool
        var upsideDown = true; //bool
        var cardId; // null or str

        Cookies.set('foilPresentation', true);
        
        let divUnderTest = createCardDiv(image, text, foil, tokens, noBorder, errata, upsideDown, cardId)[0];
        let foil_tag = divUnderTest.children[2];
        let foil_img = foil_tag.children[0];

        Cookies.remove('foilPresentation'); // clean up cookie before potentially failing

        // foil tags
        expect(foil_tag.classList.contains('foilOverlay')).toBe(true);
        expect(foil_img.src).toBe("http://localhost/gemp-module/images/" + "foil.gif");
    });

    test('card id becomes a class', () => {
        // valid data
        var image = "img_filepath.jpg";
        var text; //null or str
        var foil = true; //bool
        var tokens = true; //undef or bool
        var noBorder = true; //bool
        var errata = true; //bool
        var upsideDown = true; //bool
        var cardId = "1234"; // null or str
        
        let divUnderTest = createCardDiv(image, text, foil, tokens, noBorder, errata, upsideDown, cardId)[0];
        let image_tag = divUnderTest.children[0];

        expect(image_tag.classList.contains("card_img_1234")).toBe(true);
    });

    test('card ids could be any string', () => {
        // valid data
        var image = "img_filepath.jpg";
        var text; //null or str
        var foil = true; //bool
        var tokens = true; //undef or bool
        var noBorder = true; //bool
        var errata = true; //bool
        var upsideDown = true; //bool
        var cardId = "what"; // null or str
        
        let divUnderTest = createCardDiv(image, text, foil, tokens, noBorder, errata, upsideDown, cardId)[0];
        let image_tag = divUnderTest.children[0];

        expect(image_tag.classList.contains("card_img_what")).toBe(true);
    });

    test('upside-down false and cardid null', () => {
        // valid data
        var image = "img_filepath.jpg";
        var text; //null or str
        var foil = true; //bool
        var tokens = true; //undef or bool
        var noBorder = true; //bool
        var errata = true; //bool
        var upsideDown = false; //bool
        var cardId; // null or str
        
        let divUnderTest = createCardDiv(image, text, foil, tokens, noBorder, errata, upsideDown, cardId)[0];
        let image_tag = divUnderTest.children[0];

        // null value set to an empty string
        expect(divUnderTest.textContent).toBe("");

        // image tags
        expect(image_tag.src).toBe("http://localhost/" + image);
        expect(image_tag.classList.contains("card_img")).toBe(true);
        expect(image_tag.classList.contains("upside-down")).toBe(false);
        expect(image_tag.classList.contains("card_img_")).toBe(false);
    });

    test('upside-down true and cardid valid', () => {
        // valid data
        var image = "img_filepath.jpg";
        var text; //null or str
        var foil = true; //bool
        var tokens = true; //undef or bool
        var noBorder = true; //bool
        var errata = true; //bool
        var upsideDown = false; //bool
        var cardId = "1234"; // null or str
        
        let divUnderTest = createCardDiv(image, text, foil, tokens, noBorder, errata, upsideDown, cardId)[0];
        let image_tag = divUnderTest.children[0];

        // null value set to an empty string
        expect(divUnderTest.textContent).toBe("");

        // image tags
        expect(image_tag.src).toBe("http://localhost/" + image);
        expect(image_tag.classList.contains("card_img")).toBe(true);
        expect(image_tag.classList.contains("upside-down")).toBe(false);
        expect(image_tag.classList.contains("card_img_1234")).toBe(true);
    });
});

describe('getFoilPresentation', () => {
    test('if cookie unset, returns static', () => {
        // in case other tests bleed into this one, which happens w/cookies
        Cookies.remove('foilPresentation');
        expect(getFoilPresentation()).toBe("static");
    });

    test('if cookie true, returns animated', () => {
        Cookies.set('foilPresentation', true);
        expect(getFoilPresentation()).toBe('animated');
    });

    test('if cookie false, returns static', () => {
        Cookies.set('foilPresentation', false);
        expect(getFoilPresentation()).toBe('static');
    });
});

describe('createFullCardDiv', () => {
    test('default data', () => {
        // valid data
        var image = "img_filepath.jpg";
        var foil = true; //bool
        var horizontal = false; //bool
        var noBorder = true; //bool
        
        let divUnderTest = createFullCardDiv(image, foil, horizontal, noBorder)[0];
        let fullcardwrapper = divUnderTest.children[0];
        let fullcardwrapper_img = fullcardwrapper.children[0];
        let border_tag = divUnderTest.children[1];
        let border_img = border_tag.children[0];
        let foil_tag = divUnderTest.children[2];
        let foil_img = foil_tag.children[0];

        expect(divUnderTest.classList.contains("fullCardDivVertical")).toBe(true);

        expect(fullcardwrapper.classList.contains("fullCardWrapper")).toBe(true);
        expect(fullcardwrapper_img.classList.contains("fullCardImgVertical")).toBe(true);
        expect(fullcardwrapper_img.src).toBe("http://localhost/" + image);

        expect(border_tag.classList.contains("noBorderOverlayVertical")).toBe(true);
        expect(border_img.classList.contains("actionArea")).toBe(true);
        expect(border_img.src).toBe("http://localhost/" + "test-file-stub");

        expect(foil_tag.classList.contains('foilOverlayVertical')).toBe(true);
        expect(foil_img.src).toBe("http://localhost/gemp-module/images/" + "holo.jpg");
    });

    test('horizontal true', () => {
        // valid data
        var image = "img_filepath.jpg";
        var foil = true; //bool
        var horizontal = true; //bool
        var noBorder = true; //bool
        
        let divUnderTest = createFullCardDiv(image, foil, horizontal, noBorder)[0];
        let fullcardwrapper = divUnderTest.children[0];
        let fullcardwrapper_img = fullcardwrapper.children[0];
        let border_tag = divUnderTest.children[1];
        let border_img = border_tag.children[0];
        let foil_tag = divUnderTest.children[2];
        let foil_img = foil_tag.children[0];

        expect(divUnderTest.classList.contains("fullCardDivHorizontal")).toBe(true);

        expect(fullcardwrapper.classList.contains("fullCardWrapper")).toBe(true);
        expect(fullcardwrapper_img.classList.contains("fullCardImgHorizontal")).toBe(true);
        expect(fullcardwrapper_img.src).toBe("http://localhost/" + image);

        expect(border_tag.classList.contains("noBorderOverlayHorizontal")).toBe(true);
        expect(border_img.classList.contains("actionArea")).toBe(true);
        expect(border_img.src).toBe("http://localhost/" + "test-file-stub");

        expect(foil_tag.classList.contains('foilOverlayHorizontal')).toBe(true);
        expect(foil_img.src).toBe("http://localhost/gemp-module/images/" + "holo.jpg");
    });

    test('if foil cookie true and foil is true change foil img tag', () => {
        // valid data
        var image = "img_filepath.jpg";
        var foil = true; //bool
        var horizontal = false; //bool
        var noBorder = true; //bool

        Cookies.set('foilPresentation', true);
        
        let divUnderTest = createFullCardDiv(image, foil, horizontal, noBorder)[0];
        let fullcardwrapper = divUnderTest.children[0];
        let fullcardwrapper_img = fullcardwrapper.children[0];
        let border_tag = divUnderTest.children[1];
        let border_img = border_tag.children[0];
        let foil_tag = divUnderTest.children[2];
        let foil_img = foil_tag.children[0];

        Cookies.set('foilPresentation', false);

        expect(divUnderTest.classList.contains("fullCardDivVertical")).toBe(true);

        expect(fullcardwrapper.classList.contains("fullCardWrapper")).toBe(true);
        expect(fullcardwrapper_img.classList.contains("fullCardImgVertical")).toBe(true);
        expect(fullcardwrapper_img.src).toBe("http://localhost/" + image);

        expect(border_tag.classList.contains("noBorderOverlayVertical")).toBe(true);
        expect(border_img.classList.contains("actionArea")).toBe(true);
        expect(border_img.src).toBe("http://localhost/" + "test-file-stub");

        expect(foil_tag.classList.contains('foilOverlayVertical')).toBe(true);
        expect(foil_img.src).toBe("http://localhost/gemp-module/images/" + "foil.gif");
    });

    test('foil off, no foiloverlay', () => {
        // valid data
        var image = "img_filepath.jpg";
        var foil = false; //bool
        var horizontal = false; //bool
        var noBorder = true; //bool
        
        let divUnderTest = createFullCardDiv(image, foil, horizontal, noBorder)[0];
        let fullcardwrapper = divUnderTest.children[0];
        let fullcardwrapper_img = fullcardwrapper.children[0];
        let border_tag = divUnderTest.children[1];
        let border_img = border_tag.children[0];
        let foil_tag = divUnderTest.children[2];

        expect(divUnderTest.classList.contains("fullCardDivVertical")).toBe(true);

        expect(fullcardwrapper.classList.contains("fullCardWrapper")).toBe(true);
        expect(fullcardwrapper_img.classList.contains("fullCardImgVertical")).toBe(true);
        expect(fullcardwrapper_img.src).toBe("http://localhost/" + image);

        expect(border_tag.classList.contains("noBorderOverlayVertical")).toBe(true);
        expect(border_img.classList.contains("actionArea")).toBe(true);
        expect(border_img.src).toBe("http://localhost/" + "test-file-stub");

        expect(foil_tag).toBe(undefined);
    });
});

describe('createSimpleCardDiv', () => {
    test('default data', () => {
        let image_path = "asdf.jpg";
        let divUnderTest = createSimpleCardDiv(image_path)[0];
        let img = divUnderTest.children[0];
        expect(divUnderTest.classList.contains('card')).toBe(true);
        expect(img.src).toBe("http://localhost/" + image_path);
    });
});

// This test is skipped because it is broken, pending a better
// understanding of the $.expr() call in gameUi.js,
// upon which getCardDivFromId depends. 
describe.skip('getCardDivFromId', () => {
    test('', () => {
        document.body.innerHTML = `
            <div class='card' id="1234"/>
        `;

        let result = getCardDivFromId("1234");
        let expected = document.getElementById("1234");

        expect(result).toBe(expected);
    });
});