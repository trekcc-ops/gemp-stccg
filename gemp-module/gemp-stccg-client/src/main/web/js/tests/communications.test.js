import {describe, beforeEach, expect, test} from '@jest/globals';
import GempClientCommunication from "../gemp-022/communication.js";
import { fetchImage } from '../gemp-022/communication.js';
import { userAgent } from '../gemp-022/common.js';

beforeEach(() => {
    // clear any stored fetch mock statistics
    fetchMock.resetMocks();
});

test('getCollection sends the correct URL to the server', async () => {
    let url = "/gemp-stccg-server";
    let failure = null;
    let comms = new GempClientCommunication(url, failure);

    const participantId = "nsoong";
    const collectionType = "default";
    const filter = "";
    const start = 0;
    const count = 18;

    let expected_call_string = url + "/collection/" + collectionType + "?participantId=" + participantId + "&filter=" + filter + "&start=" + start + "&count=" + count;

    let actual = await comms.getCollection(collectionType, participantId, filter, start, count);
    
    expect(fetch.mock.calls.length).toEqual(1) // Fetch was called once
    let lastcall_firstarg = fetch.mock.lastCall[0];
    expect(lastcall_firstarg).toEqual(expected_call_string); // Fetch called the right URL
});

test('getCollection waits for and gives us a string of what the server gave us', async () => {
    const server_retval = '<?xml version="1.0" encoding="UTF-8" standalone="no"?><collection count="119"><card blueprintId="101_196" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/premiere/alexanderrozhenko95.jpg"/><card blueprintId="101_197" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/premiere/alynnanechayev95.jpg"/><card blueprintId="101_198" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/comingofage/8.jpg"/><card blueprintId="101_289" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/premiere/amarie95.jpg"/><card blueprintId="101_146" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/premiere/avertdisaster95.jpg"/><card blueprintId="101_252" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/premiere/betor95.jpg"/><card blueprintId="101_253" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/premiere/bijik95.jpg"/><card blueprintId="101_290" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/tngsup/33V.jpg"/><card blueprintId="101_254" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/premiere/batrell95.jpg"/><card blueprintId="101_199" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/sfls/18.jpg"/><card blueprintId="101_200" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/premiere/beverlycrusher95.jpg"/><card blueprintId="101_201" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/twtstarters/calloway.jpg"/><card blueprintId="101_202" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/premiere/christopherhobson95.jpg"/><card blueprintId="101_147" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/errata/Cloaked-Mission.jpg"/><card blueprintId="101_148" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/errata/Covert-Installation.jpg"/><card blueprintId="101_149" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/premiere/covertrescue95.jpg"/><card blueprintId="101_150" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/premiere/culturalobservation95.jpg"/><card blueprintId="101_357" count="4" imageUrl="https://www.trekcc.org/1e/cardimages/premiere/dderidex95.jpg"/></collection>';
    fetchMock.mockResponse(server_retval);

    let url = "/gemp-stccg-server";
    let failure = null;
    let comms = new GempClientCommunication(url, failure);

    const participantId = "nsoong";
    const collectionType = "default";
    const filter = "";
    const start = 0;
    const count = 18;

    let actual_string = await comms.getCollection(collectionType, participantId, filter, start, count);
    
    expect(actual_string).toBe(server_retval);
});

test('getCollection handles 404s', async () => {
    const server_retval = new Response("Not Found", {status: 404});
    fetchMock.mockResolvedValue(server_retval);
    window.alert = jest.fn(() => ({}));
    let alertmock = jest.spyOn(window, 'alert');
    let alerttext = "You don't have collection of that type.";
    

    let url = "/gemp-stccg-server";
    let failure = null;
    let comms = new GempClientCommunication(url, failure);

    const participantId = "nsoong";
    const collectionType = "default";
    const filter = "";
    const start = 0;
    const count = 18;

    let actual_string = await comms.getCollection(collectionType, participantId, filter, start, count);
    
    expect(alertmock.mock.calls.length).toEqual(1) // Alert was called once
    let lastcall_firstarg = alertmock.mock.lastCall[0];
    expect(lastcall_firstarg).toEqual(alerttext); // Alert had the expected text.
});

test('getCollection handles non-404s with a console.error', async () => {
    const server_retval = new Response("Forbidden", {status: 403});
    fetchMock.mockResolvedValue(server_retval);
    console.error = jest.fn(() => ({}));
    let errmock = jest.spyOn(console, 'error');
    let errmockobj = {"getCollection fetch error": "Forbidden"};
    

    let url = "/gemp-stccg-server";
    let failure = null;
    let comms = new GempClientCommunication(url, failure);

    const participantId = "nsoong";
    const collectionType = "default";
    const filter = "";
    const start = 0;
    const count = 18;

    let actual_string = await comms.getCollection(collectionType, participantId, filter, start, count);
    
    expect(errmock.mock.calls.length).toEqual(1) // Console.error was called once
    let lastcall_firstarg = errmock.mock.lastCall[0];
    expect(lastcall_firstarg).toEqual(errmockobj); // Console.error had the expected output.
});

test('getCollection handles fetch errors with a console.error', async () => {
    fetchMock.mockReject(new Error('fake error message'));
    console.error = jest.fn(() => ({}));
    let errmock = jest.spyOn(console, 'error');
    let errmockobj = {"getCollection fetch error": "fake error message"};
    

    let url = "/gemp-stccg-server";
    let failure = null;
    let comms = new GempClientCommunication(url, failure);

    const participantId = "nsoong";
    const collectionType = "default";
    const filter = "";
    const start = 0;
    const count = 18;

    let actual_string = await comms.getCollection(collectionType, participantId, filter, start, count);
    
    expect(errmock.mock.calls.length).toEqual(1) // Console.error was called once
    let lastcall_firstarg = errmock.mock.lastCall[0];
    expect(lastcall_firstarg).toEqual(errmockobj); // Console.error had the expected output.
});

test('getSets sends the correct data to the server', async () => {
    const server_retval = JSON.stringify(
        {"updateSetOptions": []}
    );
    fetchMock.mockResponse(server_retval);
    
    let url = "/gemp-stccg-server";
    let failure = null;
    let comms = new GempClientCommunication(url, failure);

    const format = "st1emoderncomplete";

    let expected_call_string = url + "/getSets";
    let expected_call_body = `format=${format}`;

    let actual = await comms.getSets(format);
    
    expect(fetch.mock.calls.length).toEqual(1) // Fetch was called once
    let lastcall_firstarg = fetch.mock.lastCall[0];
    let lastcall_body = fetch.mock.lastCall[1].body;
    expect(lastcall_firstarg).toEqual(expected_call_string); // Fetch called the right URL
    expect(lastcall_body).toEqual(expected_call_body); // Fetch sent the right body
});

test('getSets waits for and returns the server\'s JSON objects', async () => {
    const server_retval = JSON.stringify({
        updateSetOptions: [
            { code: "disabled", name: "disabled" },
            { code: "101", name: "Premiere" },
            { code: "103", name: "Alternate Universe" }
            ]
        }
    );
    fetchMock.mockResponse(server_retval);

    let url = "/gemp-stccg-server";
    let failure = null;
    let comms = new GempClientCommunication(url, failure);

    const format = "st1emoderncomplete";

    let actual = await comms.getSets(format);
    
    expect(actual).toEqual(JSON.parse(server_retval));
});

test('getSets handles 400 error', async () => {
    const server_retval = new Response("Bad Request", {status: 400});
    fetchMock.mockResolvedValue(server_retval);
    window.alert = jest.fn(() => ({}));
    let alertmock = jest.spyOn(window, 'alert');
    let alerttext = "Could not retrieve sets.";
    

    let url = "/gemp-stccg-server";
    let failure = null;
    let comms = new GempClientCommunication(url, failure);

    const format = "st1emoderncomplete";

    let actual = await comms.getSets(format);
    
    expect(alertmock.mock.calls.length).toEqual(1) // Alert was called once
    let lastcall_firstarg = alertmock.mock.lastCall[0];
    expect(lastcall_firstarg).toEqual(alerttext); // Alert had the expected text.
});

test('getSets handles non-404s with a console.error', async () => {
    const server_retval = new Response("Forbidden", {status: 403});
    fetchMock.mockResolvedValue(server_retval);
    console.error = jest.fn(() => ({}));
    let errmock = jest.spyOn(console, 'error');
    let errmockobj = {"getSets fetch error": "Forbidden"};
    

    let url = "/gemp-stccg-server";
    let failure = null;
    let comms = new GempClientCommunication(url, failure);

    const format = "st1emoderncomplete";

    let actual = await comms.getSets(format);
    
    expect(errmock.mock.calls.length).toEqual(1) // Console.error was called once
    let lastcall_firstarg = errmock.mock.lastCall[0];
    expect(lastcall_firstarg).toEqual(errmockobj); // Console.error had the expected output.
});

test('getSets handles fetch errors with a console.error', async () => {
    fetchMock.mockReject(new Error('fake error message'));
    console.error = jest.fn(() => ({}));
    let errmock = jest.spyOn(console, 'error');
    let errmockobj = {"getSets fetch error": "fake error message"};
    

    let url = "/gemp-stccg-server";
    let failure = null;
    let comms = new GempClientCommunication(url, failure);

    const format = "st1emoderncomplete";

    let actual = await comms.getSets(format);
    
    expect(errmock.mock.calls.length).toEqual(1) // Console.error was called once
    let lastcall_firstarg = errmock.mock.lastCall[0];
    expect(lastcall_firstarg).toEqual(errmockobj); // Console.error had the expected output.
});

describe('fetchImage', () => {
    test('sends accept list and useragent in headers', async () => {
        const server_retval = JSON.stringify(
            {"updateSetOptions": []}
        );
        fetchMock.mockResponse(server_retval);
        
        let url = "https://www.trekcc.org/images/icons/1e/1E-BAJ.gif";

        let expected_url = url;
        let expected_headers = new Headers();
        expected_headers.append("Accept", "image/webp,image/png,image/jpeg,image/gif");
        expected_headers.append("User-Agent", userAgent);

        let actual = await fetchImage(url);
        
        expect(fetch.mock.calls.length).toEqual(1) // Fetch was called once
        let lastcall_url = fetch.mock.lastCall[0];
        let lastcall_headers = fetch.mock.lastCall[1]["headers"];

        expect(lastcall_url).toEqual(expected_url); // Fetch called the right URL
        expect(lastcall_headers).toEqual(expected_headers); // Fetch sent the right body
    });
});