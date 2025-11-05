import {describe, beforeEach, expect, jest, test} from '@jest/globals';
import {monthNames, serverDomain, formatToTwoDigits, formatDate, formatPrice, getDateString, getUrlParam, getMapSize, replaceIncludes, log, openSizeDialog, getAffiliationHtml} from "../gemp-022/common.js";
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
        expected.src = "test-file-stub";
        expected.classList.add("inline-icon");
        expected.title = "Bajoran";

        let actual = getAffiliationHtml("BAJORAN");
        expect(actual).toStrictEqual(expected);
    });
});