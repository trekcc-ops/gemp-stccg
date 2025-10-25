import {describe, beforeEach, expect, test, beforeAll} from '@jest/globals';
import Cookies from "js-cookie";
import {cardCache, cardScale, packBlueprints, createCardDiv, getFoilPresentation, createFullCardDiv, createSimpleCardDiv, getCardDivFromId} from "../gemp-022/jCards.js";
import Card from "../gemp-022/jCards.js";

beforeAll(() => {
    // The createObjectURL function is not created by default inside the JSDOM environment
    //   so I have to create it; I do so as a jest function so we can ask it questions with .mock.
    URL.createObjectURL = (blobObj) => {
        let textPromise = blobObj.text();
        return textPromise.then((result) => {
            return result;
        });
    };
});

beforeEach(() => {
    // clear any stored fetch mock statistics
    fetchMock.resetMocks();
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
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
    
        expect(cardUnderTest.blueprintId).toBe("101_312");
        expect(cardUnderTest.zone).toBe("ATTACHED");
        expect(cardUnderTest.cardId).toBe("1");
        expect(cardUnderTest.owner).toBe("andrew2");
        expect(cardUnderTest.imageUrl).toBe("https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg");
        expect(cardUnderTest.locationIndex).toBe(8);
        expect(cardUnderTest.upsideDown).toBe(false);
        expect(cardUnderTest.title).toBe("N'Vek");
    });
    
    test('constructor throws error when blueprintId is bad', () => {
        let blueprintId=101312;
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
        let title="N'Vek";
    
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });
    
    test('constructor throws error when zone is bad', () => {
        let blueprintId="101_312";
        let zone="BLORG";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
        let title="N'Vek";
    
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    
        zone=123;
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    
        zone={zone:"ATTACHED"};
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });
    
    test('constructor throws error when cardId is not integer or string', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId=["array", "of", "strings"];
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
        let title="N'Vek";
    
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);

        cardId = undefined;
        
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });

    test('constructor accepts an integer as cardId and saves it as a string', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId=15;
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
        let title="N'Vek";

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
    
        expect(cardUnderTest.blueprintId).toBe("101_312");
        expect(cardUnderTest.zone).toBe("ATTACHED");
        expect(cardUnderTest.cardId).toBe("15");
        expect(cardUnderTest.owner).toBe("andrew2");
        expect(cardUnderTest.imageUrl).toBe("https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg");
        expect(cardUnderTest.locationIndex).toBe(8);
        expect(cardUnderTest.upsideDown).toBe(false);
        expect(cardUnderTest.title).toBe("N'Vek");
    });

    test('constructor accepts a non-numeric string as cardId and saves it as-is', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="thecardid";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
    
        expect(cardUnderTest.blueprintId).toBe("101_312");
        expect(cardUnderTest.zone).toBe("ATTACHED");
        expect(cardUnderTest.cardId).toBe("thecardid");
        expect(cardUnderTest.owner).toBe("andrew2");
        expect(cardUnderTest.imageUrl).toBe("https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg");
        expect(cardUnderTest.locationIndex).toBe(8);
        expect(cardUnderTest.upsideDown).toBe(false);
        expect(cardUnderTest.upsideDown).toBe(false);
    });
    
    test('constructor throws error when owner is bad', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner=false;
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
        let title="N'Vek";
    
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });

    test('constructor throws error when title is not a string', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;

        let title;
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);

        title = null
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);

        title=12;
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);

        title={title: "N'Vek"};
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);

        title=["N'Vek"];
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });
    
    test('constructor throws error when imageUrl is bad', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl={url: "https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg"};
        let locationIndex="8";
        let upsideDown=false;
        let title="N'Vek";
    
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });
    
    test('constructor throws error when locationIndex is not an integer or a string', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex=["array", "of", "strings"];
        let upsideDown=false;
        let title="N'Vek";
    
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);

        locationIndex = undefined;

        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });

    test('constructor accepts a numeric string as locationIndex and saves it as a number', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
    
        expect(cardUnderTest.blueprintId).toBe("101_312");
        expect(cardUnderTest.zone).toBe("ATTACHED");
        expect(cardUnderTest.cardId).toBe("1");
        expect(cardUnderTest.owner).toBe("andrew2");
        expect(cardUnderTest.imageUrl).toBe("https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg");
        expect(cardUnderTest.locationIndex).toBe(8);
        expect(cardUnderTest.upsideDown).toBe(false);

        locationIndex="1453.23";

        cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.locationIndex).toBe(1453);

        locationIndex="1499,23"; // euro style
        cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.locationIndex).toBe(1499);
    });

    test('constructor accepts an integer as locationIndex and saves it as-is', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex=8;
        let upsideDown=false;
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
    
        expect(cardUnderTest.blueprintId).toBe("101_312");
        expect(cardUnderTest.zone).toBe("ATTACHED");
        expect(cardUnderTest.cardId).toBe("1");
        expect(cardUnderTest.owner).toBe("andrew2");
        expect(cardUnderTest.imageUrl).toBe("https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg");
        expect(cardUnderTest.locationIndex).toBe(8);
        expect(cardUnderTest.upsideDown).toBe(false);
        expect(cardUnderTest.title).toBe("N'Vek");
    });

    test('constructor accepts an empty string as locationIndex and returns it as -1', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="";
        let upsideDown=false;
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
    
        expect(cardUnderTest.blueprintId).toBe("101_312");
        expect(cardUnderTest.zone).toBe("ATTACHED");
        expect(cardUnderTest.cardId).toBe("1");
        expect(cardUnderTest.owner).toBe("andrew2");
        expect(cardUnderTest.imageUrl).toBe("https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg");
        expect(cardUnderTest.locationIndex).toBe(-1);
        expect(cardUnderTest.upsideDown).toBe(false);
        expect(cardUnderTest.title).toBe("N'Vek");
    });

    test('constructor throws error when locationIndex is a not-empty string but not translatable to an int', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="invalidLocationIndex";
        let upsideDown=false;
        let title="N'Vek";
    
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });
    
    test('constructor throws error when upsideDown is bad', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown="false";
        let title="N'Vek";
    
        expect(() => new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown)).toThrow(Error);
    });
    
    test('constructor sets foil value based on last blueprintId character', () => {
        let blueprintId="101_312";
        let zone="ATTACHED";
        let cardId="1";
        let owner="andrew2";
        let imageUrl="https://www.trekcc.org/1e/cardimages/premiere/nvek95.jpg";
        let locationIndex="8";
        let upsideDown=false;
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.foil).toBe(false);
    
        blueprintId="101_312*";
        cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.tengwar).toBe(false);
    
        blueprintId="101_312T";
        cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
    
        // apparently everything has a wiki by default
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
        expect(cardUnderTest.hasWiki).toBe(true);
    
        blueprintId="Special-01";
        cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
    
        let empty_array = [];
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
    
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
        let _cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
    
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
    
        let _cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
    
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
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
        let ignoreErrata = true;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
        let ignoreErrata = false;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
        let ignoreErrata = true;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
        let ignoreErrata = true;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
        let ignoreErrata = true;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
        let ignoreErrata = true;

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";
    
        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
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
        let title="N'Vek";

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);

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
        let title="N'Vek";

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);

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
        let title="N'Vek";

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);

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
        let title="N'Vek";

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);

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
        let title="N'Vek";

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);

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
        let title="N'Vek";

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);

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
        let title="N'Vek";

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);

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
        let title="N'Vek";

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
        // TODO: Fill in alt text with title
        let expected = `<div style=\"scroll: auto\"></div><div class=\"card fullCardDivVertical\"><div class=\"three-d-card-scene\"><div class=\"three-d-card\"><div class=\"card__face card__face--back\"><img src=\"test-file-stub\" style=\"width: 100%; height: 100%;\"></div><div class=\"card__face card__face--front\"><div class=\"card-load-spinner\"></div><img class=\"card_img\" style=\"width: 100%; height: 100%;\" alt=\"N'Vek\"><div class=\"borderOverlay\"><img class=\"actionArea\" src=\"test-file-stub\" style=\"width: 100%; height: 100%;\"></div></div></div></div></div>`;

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
        let title="N'Vek";

        let cardUnderTest = new Card(blueprintId, zone, cardId, owner, title, imageUrl, locationIndex, upsideDown);
        cardUnderTest.horizontal = true;
        // TODO: Fill in alt text with title
        let expected = `<div style=\"scroll: auto\"></div><div class=\"card fullCardDivHorizontal\"><div class=\"three-d-card-scene\"><div class=\"three-d-card\"><div class=\"card__face card__face--back\"><img src=\"test-file-stub\" style=\"width: 100%; height: 100%;\"></div><div class=\"card__face card__face--front\"><div class=\"card-load-spinner\"></div><img class=\"card_img\" style=\"width: 100%; height: 100%;\" alt=\"N'Vek\"><div class=\"borderOverlay\"><img class=\"actionArea\" src=\"test-file-stub\" style=\"width: 100%; height: 100%;\"></div></div></div></div></div>`;

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

        // card - used for backwards compatibility
        //   three-d-card-scene - used to impl the 3d effect
        //     three-d-card - composed of front and back faces
        //       card_face - one side of the card
        //         img
        //         tokenOverlay
        //         borderOverlay
        
        let divUnderTest = createCardDiv(image, text, foil, tokens, noBorder, errata, upsideDown, cardId);
        let threeDScene = divUnderTest.children[0];
        let threeDCard = threeDScene.children[0];
        let front_face = threeDCard.children[1];
        let loading_spinner = front_face.children[0];
        let image_tag = front_face.children[1];
        let errata_tag = front_face.children[2];
        let errata_img = errata_tag.children[0];
        let foil_tag = front_face.children[3];
        let foil_img = foil_tag.children[0];
        let tokens_tag = front_face.children[4];
        let border_tag = front_face.children[5];
        let border_img = border_tag.children[0];

        // null value set to an empty string
        expect(divUnderTest.textContent).toBe("");

        // image tags
        expect(image_tag.src).toBe(""); // we dynamically fetch now, no src until loaded
        expect(fetchMock.mock.calls.length).toEqual(1); // verify we tried to fetch
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

        let divUnderTest = createCardDiv(image, text, foil, tokens, noBorder, errata, upsideDown, cardId);
        let threeDScene = divUnderTest.children[0];
        let threeDCard = threeDScene.children[0];
        let front_face = threeDCard.children[1];
        let foil_tag = front_face.children[3];
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
        
        let divUnderTest = createCardDiv(image, text, foil, tokens, noBorder, errata, upsideDown, cardId);
        let threeDScene = divUnderTest.children[0];
        let threeDCard = threeDScene.children[0];
        let front_face = threeDCard.children[1];
        let image_tag = front_face.children[1];

        expect(image_tag.classList.contains("card_img_1234")).toBe(true);
    });

    test('card id also set as top level id', () => {
        // valid data
        var image = "img_filepath.jpg";
        var text; //null or str
        var foil = true; //bool
        var tokens = true; //undef or bool
        var noBorder = true; //bool
        var errata = true; //bool
        var upsideDown = true; //bool
        var cardId = "1234"; // null or str
        
        let divUnderTest = createCardDiv(image, text, foil, tokens, noBorder, errata, upsideDown, cardId);

        expect(divUnderTest.id).toStrictEqual("1234");
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
        
        let divUnderTest = createCardDiv(image, text, foil, tokens, noBorder, errata, upsideDown, cardId);
        let threeDScene = divUnderTest.children[0];
        let threeDCard = threeDScene.children[0];
        let front_face = threeDCard.children[1];
        let loading_spinner = front_face.children[0];
        let image_tag = front_face.children[1];

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
        
        let divUnderTest = createCardDiv(image, text, foil, tokens, noBorder, errata, upsideDown, cardId);
        let threeDScene = divUnderTest.children[0];
        let threeDCard = threeDScene.children[0];
        let front_face = threeDCard.children[1];
        let loading_spinner = front_face.children[0];
        let image_tag = front_face.children[1];

        // null value set to an empty string
        expect(divUnderTest.textContent).toBe("");

        // image tags
        expect(image_tag.src).toBe(""); // we dynamically fetch now, no src until loaded
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
        
        let divUnderTest = createCardDiv(image, text, foil, tokens, noBorder, errata, upsideDown, cardId);
        let threeDScene = divUnderTest.children[0];
        let threeDCard = threeDScene.children[0];
        let front_face = threeDCard.children[1];
        let loading_spinner = front_face.children[0];
        let image_tag = front_face.children[1];

        // null value set to an empty string
        expect(divUnderTest.textContent).toBe("");

        // image tags
        expect(image_tag.src).toBe(""); // we dynamically fetch now, no src until loaded
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
        var foil = false; //bool
        var horizontal = false; //bool
        var noBorder = false; //bool
        let divUnderTest = createFullCardDiv(image, foil, horizontal, noBorder);


        // other assumed values
        let text = "";
        let tokens = false;
        let errata = false;
        let upsideDown = false;
        let cardId; // null

        // card - used for backwards compatibility
        //   three-d-card-scene - used to impl the 3d effect
        //     three-d-card - composed of front and back faces
        //       card_face - one side of the card
        //         img
        //         tokenOverlay
        //         borderOverlay
        let threeDScene = divUnderTest.children[0];
        let threeDCard = threeDScene.children[0];
        let front_face = threeDCard.children[1];
        let loading_spinner = front_face.children[0];
        let image_tag = front_face.children[1];
        let border_tag = front_face.children[2];
        let border_img = border_tag.children[0];

        // null value set to an empty string
        expect(divUnderTest.textContent).toBe("");
        expect(divUnderTest.classList.contains("fullCardDivVertical")).toBe(true);

        // image tags
        expect(image_tag.src).toBe(""); // we dynamically fetch now, no src until loaded
        expect(fetchMock.mock.calls.length).toEqual(1); // verify we tried to fetch
        expect(image_tag.classList.contains("card_img")).toBe(true);
        expect(image_tag.classList.contains("upside-down")).toBe(false);
        expect(image_tag.classList.contains("card_img_")).toBe(false);

        // border tag
        expect(border_tag.classList.contains('borderOverlay')).toBe(true);
        expect(border_tag.classList.contains('noBorder')).toBe(false);
        expect(border_img.classList.contains('actionArea')).toBe(true);
        expect(border_img.src).toBe("http://localhost/" + "test-file-stub");
    });

    test('default data but horizontal', () => {
        // valid data
        var image = "img_filepath.jpg";
        var foil = false; //bool
        var horizontal = true; //bool
        var noBorder = false; //bool
        let divUnderTest = createFullCardDiv(image, foil, horizontal, noBorder);


        // other assumed values
        let text = "";
        let tokens = false;
        let errata = false;
        let upsideDown = false;
        let cardId; // null

        // card - used for backwards compatibility
        //   three-d-card-scene - used to impl the 3d effect
        //     three-d-card - composed of front and back faces
        //       card_face - one side of the card
        //         img
        //         tokenOverlay
        //         borderOverlay
        let threeDScene = divUnderTest.children[0];
        let threeDCard = threeDScene.children[0];
        let front_face = threeDCard.children[1];
        let loading_spinner = front_face.children[0];
        let image_tag = front_face.children[1];
        let border_tag = front_face.children[2];
        let border_img = border_tag.children[0];

        // null value set to an empty string
        expect(divUnderTest.textContent).toBe("");
        expect(divUnderTest.classList.contains("fullCardDivHorizontal")).toBe(true);

        // image tags
        expect(image_tag.src).toBe(""); // we dynamically fetch now, no src until loaded
        expect(image_tag.classList.contains("card_img")).toBe(true);
        expect(image_tag.classList.contains("upside-down")).toBe(false);
        expect(image_tag.classList.contains("card_img_")).toBe(false);

        // border tag
        expect(border_tag.classList.contains('borderOverlay')).toBe(true);
        expect(border_tag.classList.contains('noBorder')).toBe(false);
        expect(border_img.classList.contains('actionArea')).toBe(true);
        expect(border_img.src).toBe("http://localhost/" + "test-file-stub");
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