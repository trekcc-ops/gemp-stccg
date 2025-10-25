import {describe, beforeEach, expect, jest, test} from '@jest/globals';
import {monthNames, serverDomain, formatToTwoDigits, formatDate, formatPrice, getDateString, getUrlParam, getMapSize, replaceIncludes, log, openSizeDialog, getAffiliationHtml, getAffiliationHtmlAsync} from "../gemp-022/common.js";
import bajoranImage from "../../images/icons/affiliations/1E-BAJ.gif";
import GempClientCommunication from '../gemp-022/communication.js';

beforeEach(() => {
    // clear any stored fetch mock statistics
    fetchMock.resetMocks();
});

describe('validity', () => {
    test('common.js is valid syntax', () => {
        // noop
    });
});

describe('getAffiliationHtml', () => {
    test('returns an image tag', () => {

        let expected = new Image();
        expected.src = "https://www.trekcc.org/images/icons/1e/1E-BAJ.gif";
        expected.classList.add("inline-icon");
        expected.title = "Bajoran";

        let actual = getAffiliationHtml("BAJORAN");
        expect(actual).toStrictEqual(expected);
    });
});

describe('getAffiliationHtmlAsync', () => {
    test('returns an image tag', async () => {
        document.body.innerHTML = `
            <div id='container'/>
        `;

        // CardFilter initialization requires a server call, mock it with data.
        const getAffiliationRetVal = bajoranImage;
        fetchMock.mockResponseOnce(getAffiliationRetVal);

        //console.log(URL);
        
        
        // The createObjectURL function is not created by default inside the JSDOM environment
        //   so I have to create it; I do so as a jest function so we can ask it questions with .mock.
        URL.createObjectURL = (blobObj) => {
            //`blob: ${blobObj.type}`
            let textPromise = blobObj.text();
            return textPromise.then((result) => {
                //console.log(`createObjectURL: ${result}`);
                return result;
            });
        };

        let dCEMock = jest.spyOn(document, "createElement");
        let cOUMock = jest.spyOn(URL, "createObjectURL");

        let actual = await getAffiliationHtmlAsync("BAJORAN");

        expect(fetchMock.mock.calls.length).toEqual(1);
        expect(dCEMock.mock.calls.length).toEqual(1);
        expect(cOUMock.mock.calls.length).toEqual(1);

        let expectedUrl = "http://localhost/test-file-stub";
        expect(actual.src).toEqual(expectedUrl);
    });
});