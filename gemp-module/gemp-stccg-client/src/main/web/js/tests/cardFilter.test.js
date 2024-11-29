import CardFilter from "../gemp-022/cardFilter.js";

beforeEach(() => {
    // clear any stored fetch mock statistics
    fetchMock.resetMocks();
});

test('setCollectionType sets the collectionType', async () => {
    document.body.innerHTML = `
            <label for="collectionSelect"></label><select id="collectionSelect">
                <option value="default">All cards</option>
                <option value="permanent">My cards</option>
                <option value="trophy">Trophies</option>
            </select>
            `;

    const mockCollection = "default";
    const mockClearCollection = jest.fn(() => {return});
    const mockAddCard = jest.fn(() => {return});
    const mockFinishCollection = jest.fn(() => {return});
    const mockFormat = "ST1E";
    
    // CardFilter initialization requires a server call, mock it with data.
    const getSets_retval = JSON.stringify(
        {"updateSetOptions": []}
    );
    fetchMock.mockResponseOnce(getSets_retval);
    
    let cf = await new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);

    expect(cf.collectionType).toBe("default");
    cf.setCollectionType("permanent");
    expect(cf.collectionType).toBe("permanent");
});

test('setCollectionType resets the start point', async () => {
    document.body.innerHTML = `
            <label for="collectionSelect"></label><select id="collectionSelect">
                <option value="default">All cards</option>
                <option value="permanent">My cards</option>
                <option value="trophy">Trophies</option>
            </select>
            `;

    const mockCollection = "default";
    const mockClearCollection = jest.fn(() => {return});
    const mockAddCard = jest.fn(() => {return});
    const mockFinishCollection = jest.fn(() => {return});
    const mockFormat = "ST1E";

    // CardFilter initialization requires a server call, mock it with data.
    const getSets_retval = JSON.stringify(
        {"updateSetOptions": []}
    );
    fetchMock.mockResponseOnce(getSets_retval);
    
    let cf = await new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);

    expect(cf.start).toBe(0);
    cf.start = 18;
    expect(cf.start).toBe(18);
    cf.setCollectionType("permanent");
    expect(cf.start).toBe(0);
});

test('setFilter sets the filter', async () => {
    // I don't know what part of the UI uses this except for merchantUI - unneeded?
    document.body.innerHTML = `
            <label for="collectionSelect"></label><select id="collectionSelect">
                <option value="default">All cards</option>
                <option value="permanent">My cards</option>
                <option value="trophy">Trophies</option>
            </select>
            `;

    const mockCollection = "default";
    const mockClearCollection = jest.fn(() => {return});
    const mockAddCard = jest.fn(() => {return});
    const mockFinishCollection = jest.fn(() => {return});
    const mockFormat = "ST1E";
    
    // CardFilter initialization requires a server call, mock it with data.
    const getSets_retval = JSON.stringify(
        {"updateSetOptions": []}
    );
    fetchMock.mockResponseOnce(getSets_retval);
    
    let cf = await new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);

    expect(cf.filter).toBe("");
    cf.setFilter("type:card");
    expect(cf.filter).toBe("type:card");
});

test('setFilter resets the start point', async () => {
    // I don't know what part of the UI uses this except for merchantUI - unneeded?
    document.body.innerHTML = `
            <label for="collectionSelect"></label><select id="collectionSelect">
                <option value="default">All cards</option>
                <option value="permanent">My cards</option>
                <option value="trophy">Trophies</option>
            </select>
            `;

    const mockCollection = "default";
    const mockClearCollection = jest.fn(() => {return});
    const mockAddCard = jest.fn(() => {return});
    const mockFinishCollection = jest.fn(() => {return});
    const mockFormat = "ST1E";

    // CardFilter initialization requires a server call, mock it with data.
    const getSets_retval = JSON.stringify(
        {"updateSetOptions": []}
    );
    fetchMock.mockResponseOnce(getSets_retval);
    
    let cf = await new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);

    expect(cf.start).toBe(0);
    cf.start = 18;
    expect(cf.start).toBe(18);
    cf.setFilter("type:card");
    expect(cf.start).toBe(0);
});

test('setFormat sets the format', async () => {
    document.body.innerHTML = `
            <label for="formatSelect"></label><select id="formatSelect" style="float: right; width: 150px;">
				<option value="st1emoderncomplete">ST1E Modern Complete</option>
                <option value="st2e">2E All</option>
                <option value="tribbles">Tribbles</option>
			</select>
            `;

    const mockCollection = "default";
    const mockClearCollection = jest.fn(() => {return});
    const mockAddCard = jest.fn(() => {return});
    const mockFinishCollection = jest.fn(() => {return});
    const mockFormat = "ST1E";
    
    // CardFilter initialization requires a server call, mock it with data.
    const getSets_retval = JSON.stringify(
        {"updateSetOptions": []}
    );
    fetchMock.mockResponseOnce(getSets_retval);
    
    let cf = await new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);

    expect(cf.format).toBe("ST1E");
    cf.setFormat("tribbles");
    expect(cf.format).toBe("tribbles");
});

test('setFormat does not reset the start point', async () => {
    document.body.innerHTML = `
            <label for="formatSelect"></label><select id="formatSelect" style="float: right; width: 150px;">
				<option value="st1emoderncomplete">ST1E Modern Complete</option>
                <option value="st2e">2E All</option>
                <option value="tribbles">Tribbles</option>
			</select>
            `;

    const mockCollection = "default";
    const mockClearCollection = jest.fn(() => {return});
    const mockAddCard = jest.fn(() => {return});
    const mockFinishCollection = jest.fn(() => {return});
    const mockFormat = "ST1E";

    // CardFilter initialization requires a server call, mock it with data.
    const getSets_retval = JSON.stringify(
        {"updateSetOptions": []}
    );
    fetchMock.mockResponseOnce(getSets_retval);
    
    let cf = await new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);

    expect(cf.start).toBe(0);
    cf.start = 18;
    expect(cf.start).toBe(18);
    cf.setFormat("tribbles");
    expect(cf.start).toBe(18);
});

test('setType can set the type', async () => {
    document.body.innerHTML = 
        '<select id="type" class="cardFilterSelect">' +
            '<option value="200">All physical card types</option>' +
            '<option value="101">Premiere</option>' +
        '</select>';
    
    const mockCollection = jest.fn(() => {return});
    const mockClearCollection = jest.fn(() => {return});
    const mockAddCard = jest.fn(() => {return});
    const mockFinishCollection = jest.fn(() => {return});
    const mockFormat = "ST1E";

    // CardFilter initialization requires a server call, mock it with data.
    const getSets_retval = JSON.stringify(
        {"updateSetOptions": []}
    );
    fetchMock.mockResponseOnce(getSets_retval);
    
    let cf = await new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);

    let selectUnderTest = $('#type');

    expect(selectUnderTest.val()).toBe('200');
    cf.setType("101");
    expect(selectUnderTest.val()).toBe('101');
});

test('updateSetOptions can reset the full list', async () => {
    document.body.innerHTML = '<select id="setSelect">' +
    '<option value="101,103,105,106,109,112,116,123,124,125,127,128,155,159,161,163,172,178,202,204,212,244set,245">All 1E Modern Complete sets</option>' +
    '<option disabled="">----------</option>' +
    '<option value="101" selected="">Premiere</option>' +
    '</select>';

    let beforeclear = '<option value="101,103,105,106,109,112,116,123,124,125,127,128,155,159,161,163,172,178,202,204,212,244set,245">All 1E Modern Complete sets</option>' +
    '<option disabled="">----------</option>' +
    '<option value="101" selected="">Premiere</option>';
    
    const mockCollection = jest.fn(() => {return});
    const mockClearCollection = jest.fn(() => {return});
    const mockAddCard = jest.fn(() => {return});
    const mockFinishCollection = jest.fn(() => {return});
    const mockFormat = "ST1E";

    // CardFilter initialization and every updateSetOptions requires a server call, mock them with data.
    const getSets_retval = JSON.stringify(
        [{"code": "1234", "name": "butts"}, {"code": "3456", "name": "in seats"}]
    );
    fetchMock.mockResponse(getSets_retval);

    var selectUnderTest = $('#setSelect');
    expect(selectUnderTest.html()).toBe(beforeclear);
    expect(selectUnderTest.val()).toBe('101');
    
    let cf = await new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);
    let cf_setsel = await cf.updateSetOptions();

    expect(fetch.mock.calls.length).toEqual(2) // Fetch was called twice, once in instantiation, once in updateSetOptions

    let afterClear = '<option value="1234">butts</option>' +
    '<option value="3456">in seats</option>'

    expect(selectUnderTest.html()).toBe(afterClear);
    expect(selectUnderTest.val()).toBe('1234'); // first obj in list
});

test('updateSetOptions adds a line for disabled options', async () => {
    document.body.innerHTML = '<select id="setSelect">' +
    '<option value="101,103,105,106,109,112,116,123,124,125,127,128,155,159,161,163,172,178,202,204,212,244set,245">All 1E Modern Complete sets</option>' +
    '<option disabled="">----------</option>' +
    '<option value="101" selected="">Premiere</option>' +
    '</select>';

    let beforeclear = '<option value="101,103,105,106,109,112,116,123,124,125,127,128,155,159,161,163,172,178,202,204,212,244set,245">All 1E Modern Complete sets</option>' +
    '<option disabled="">----------</option>' +
    '<option value="101" selected="">Premiere</option>';
    
    const mockCollection = jest.fn(() => {return});
    const mockClearCollection = jest.fn(() => {return});
    const mockAddCard = jest.fn(() => {return});
    const mockFinishCollection = jest.fn(() => {return});
    const mockFormat = "ST1E";

    // CardFilter initialization and every updateSetOptions requires a server call, mock them with data.
    const getSets_retval = JSON.stringify(
        [{"code": "1234", "name": "butts"}, {"code": "disabled", "name": "in seats"}]
    );
    fetchMock.mockResponse(getSets_retval);

    var selectUnderTest = $('#setSelect');
    expect(selectUnderTest.html()).toBe(beforeclear);
    expect(selectUnderTest.val()).toBe('101');
    
    let cf = await new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);
    let cf_setsel = await cf.updateSetOptions();

    expect(fetch.mock.calls.length).toEqual(2) // Fetch was called twice, once in instantiation, once in updateSetOptions

    let afterClear = '<option value="1234">butts</option>' +
    '<option disabled="">----------</option>'

    expect(selectUnderTest.html()).toBe(afterClear);
    expect(selectUnderTest.val()).toBe('1234'); // first obj in list
});

test('calculateNormalFilter none checked', async () => {
    document.body.innerHTML = 
    '<select id="type" class="cardFilterSelect">' +
        '<option value="200">All physical card types</option>' +
        '<option value="101">Premiere</option>' +
    '</select>' +
    '<div id="affiliation-buttons">' +
        '<div id="affiliations">' +
            '<input type="checkbox" class="affiliationFilter" id="BAJORAN"/><label for="BAJORAN" id="labelBAJORAN"><img src="images/icons/affiliations/1E-BAJ.gif" alt="Bajoran"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="BORG"/><label for="BORG" id="labelBORG"><img src="images/icons/affiliations/1E-BORG.gif" alt="Borg"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="CARDASSIAN"/><label for="CARDASSIAN" id="labelCARDASSIAN"><img src="images/icons/affiliations/1E-CARD.gif" alt="Cardassian"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="DOMINION"/><label for="DOMINION" id="labelDOMINION"><img src="images/icons/affiliations/1E-DOM.gif" alt="Dominion"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="FEDERATION"/><label for="FEDERATION" id="labelFEDERATION"><img src="images/icons/affiliations/1E-FED.gif" alt="Federation"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="FERENGI"/><label for="FERENGI" id="labelFERENGI"><img src="images/icons/affiliations/1E-FER.gif" alt="Ferengi"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="HIROGEN"/><label for="HIROGEN" id="labelHIROGEN"><img src="images/icons/affiliations/1E-HIR.gif" alt="Hirogen"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="KAZON"/><label for="KAZON" id="labelKAZON"><img src="images/icons/affiliations/1E-KAZ.gif" alt="Kazon"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="KLINGON"/><label for="KLINGON" id="labelKLINGON"><img src="images/icons/affiliations/1E-KLG.gif" alt="Klingon"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="NEUTRAL"/><label for="NEUTRAL" id="labelNEUTRAL"><img src="images/icons/affiliations/1E-NEU.gif" alt="Neutral"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="NON_ALIGNED"/><label for="NON_ALIGNED" id="labelNON_ALIGNED"><img src="images/icons/affiliations/1E-NON.gif" alt="Non-Aligned""/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="ROMULAN"/><label for="ROMULAN" id="labelROMULAN"><img src="images/icons/affiliations/1E-ROM.gif" alt="Romulan"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="STARFLEET"/><label for="STARFLEET" id="labelSTARFLEET"><img src="images/icons/affiliations/1E-STF.gif" alt="Starfleet"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="VIDIIAN"/><label for="VIDIIAN" id="labelVIDIIAN"><img src="images/icons/affiliations/1E-VID.gif" alt="Vidiian"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="VULCAN"/><label for="VULCAN" id="labelVULCAN"><img src="images/icons/affiliations/1E-VUL.gif" alt="Vulcan"/></label>' +
        '</div>' +
    '</div>';

    const mockCollection = jest.fn(() => {return});
    const mockClearCollection = jest.fn(() => {return});
    const mockAddCard = jest.fn(() => {return});
    const mockFinishCollection = jest.fn(() => {return});
    const mockFormat = "ST1E";

    // CardFilter initialization requires a server call, mock it with data.
    const getSets_retval = JSON.stringify(
        {"updateSetOptions": []}
    );
    fetchMock.mockResponseOnce(getSets_retval);
    
    let cf = await new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);

    let expectedResult = "|cardType:undefined|keyword:undefined|type:200";
    let actualResult = cf.calculateNormalFilter();

    expect(actualResult).toBe(expectedResult);
});

test('calculateNormalFilter Federation checked', async () => {
    document.body.innerHTML = 
    '<select id="type" class="cardFilterSelect">' +
        '<option value="200">All physical card types</option>' +
        '<option value="101">Premiere</option>' +
    '</select>' +
    '<div id="affiliation-buttons">' +
        '<div id="affiliations">' +
            '<input type="checkbox" class="affiliationFilter" id="BAJORAN"/><label for="BAJORAN" id="labelBAJORAN"><img src="images/icons/affiliations/1E-BAJ.gif" alt="Bajoran"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="BORG"/><label for="BORG" id="labelBORG"><img src="images/icons/affiliations/1E-BORG.gif" alt="Borg"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="CARDASSIAN"/><label for="CARDASSIAN" id="labelCARDASSIAN"><img src="images/icons/affiliations/1E-CARD.gif" alt="Cardassian"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="DOMINION"/><label for="DOMINION" id="labelDOMINION"><img src="images/icons/affiliations/1E-DOM.gif" alt="Dominion"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="FEDERATION"/><label for="FEDERATION" id="labelFEDERATION"><img src="images/icons/affiliations/1E-FED.gif" alt="Federation"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="FERENGI"/><label for="FERENGI" id="labelFERENGI"><img src="images/icons/affiliations/1E-FER.gif" alt="Ferengi"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="HIROGEN"/><label for="HIROGEN" id="labelHIROGEN"><img src="images/icons/affiliations/1E-HIR.gif" alt="Hirogen"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="KAZON"/><label for="KAZON" id="labelKAZON"><img src="images/icons/affiliations/1E-KAZ.gif" alt="Kazon"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="KLINGON"/><label for="KLINGON" id="labelKLINGON"><img src="images/icons/affiliations/1E-KLG.gif" alt="Klingon"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="NEUTRAL"/><label for="NEUTRAL" id="labelNEUTRAL"><img src="images/icons/affiliations/1E-NEU.gif" alt="Neutral"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="NON_ALIGNED"/><label for="NON_ALIGNED" id="labelNON_ALIGNED"><img src="images/icons/affiliations/1E-NON.gif" alt="Non-Aligned""/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="ROMULAN"/><label for="ROMULAN" id="labelROMULAN"><img src="images/icons/affiliations/1E-ROM.gif" alt="Romulan"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="STARFLEET"/><label for="STARFLEET" id="labelSTARFLEET"><img src="images/icons/affiliations/1E-STF.gif" alt="Starfleet"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="VIDIIAN"/><label for="VIDIIAN" id="labelVIDIIAN"><img src="images/icons/affiliations/1E-VID.gif" alt="Vidiian"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="VULCAN"/><label for="VULCAN" id="labelVULCAN"><img src="images/icons/affiliations/1E-VUL.gif" alt="Vulcan"/></label>' +
        '</div>' +
    '</div>';

    const mockCollection = jest.fn(() => {return});
    const mockClearCollection = jest.fn(() => {return});
    const mockAddCard = jest.fn(() => {return});
    const mockFinishCollection = jest.fn(() => {return});
    const mockFormat = "ST1E";

    // CardFilter initialization requires a server call, mock it with data.
    const getSets_retval = JSON.stringify(
        {"updateSetOptions": []}
    );
    fetchMock.mockResponseOnce(getSets_retval);
    
    let cf = await new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);
    
    // set up a watch on the button, verify it is unchecked
    let fedInput = $("#FEDERATION");
    let fedLabel = $('#labelFEDERATION');
    expect(fedLabel.prop("classList").contains("ui-checkboxradio-checked")).toBe(false);

    // simulate a click, verify it is now checked
    fedInput.trigger("click");
    expect(fedLabel.prop("classList").contains("ui-checkboxradio-checked")).toBe(true);
    
    // run the full filter and verify it returns what we want
    let expectedResult = "|cardType:undefined|affiliation:FEDERATION|keyword:undefined|type:200";
    let actualResult = cf.calculateNormalFilter();

    expect(actualResult).toBe(expectedResult);
});

test('calculateNormalFilter Federation + Kazon checked', async () => {
    document.body.innerHTML = 
    '<select id="type" class="cardFilterSelect">' +
        '<option value="200">All physical card types</option>' +
        '<option value="101">Premiere</option>' +
    '</select>' +
    '<div id="affiliation-buttons">' +
        '<div id="affiliations">' +
            '<input type="checkbox" class="affiliationFilter" id="BAJORAN"/><label for="BAJORAN" id="labelBAJORAN"><img src="images/icons/affiliations/1E-BAJ.gif" alt="Bajoran"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="BORG"/><label for="BORG" id="labelBORG"><img src="images/icons/affiliations/1E-BORG.gif" alt="Borg"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="CARDASSIAN"/><label for="CARDASSIAN" id="labelCARDASSIAN"><img src="images/icons/affiliations/1E-CARD.gif" alt="Cardassian"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="DOMINION"/><label for="DOMINION" id="labelDOMINION"><img src="images/icons/affiliations/1E-DOM.gif" alt="Dominion"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="FEDERATION"/><label for="FEDERATION" id="labelFEDERATION"><img src="images/icons/affiliations/1E-FED.gif" alt="Federation"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="FERENGI"/><label for="FERENGI" id="labelFERENGI"><img src="images/icons/affiliations/1E-FER.gif" alt="Ferengi"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="HIROGEN"/><label for="HIROGEN" id="labelHIROGEN"><img src="images/icons/affiliations/1E-HIR.gif" alt="Hirogen"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="KAZON"/><label for="KAZON" id="labelKAZON"><img src="images/icons/affiliations/1E-KAZ.gif" alt="Kazon"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="KLINGON"/><label for="KLINGON" id="labelKLINGON"><img src="images/icons/affiliations/1E-KLG.gif" alt="Klingon"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="NEUTRAL"/><label for="NEUTRAL" id="labelNEUTRAL"><img src="images/icons/affiliations/1E-NEU.gif" alt="Neutral"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="NON_ALIGNED"/><label for="NON_ALIGNED" id="labelNON_ALIGNED"><img src="images/icons/affiliations/1E-NON.gif" alt="Non-Aligned""/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="ROMULAN"/><label for="ROMULAN" id="labelROMULAN"><img src="images/icons/affiliations/1E-ROM.gif" alt="Romulan"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="STARFLEET"/><label for="STARFLEET" id="labelSTARFLEET"><img src="images/icons/affiliations/1E-STF.gif" alt="Starfleet"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="VIDIIAN"/><label for="VIDIIAN" id="labelVIDIIAN"><img src="images/icons/affiliations/1E-VID.gif" alt="Vidiian"/></label>' +
            '<input type="checkbox" class="affiliationFilter" id="VULCAN"/><label for="VULCAN" id="labelVULCAN"><img src="images/icons/affiliations/1E-VUL.gif" alt="Vulcan"/></label>' +
        '</div>' +
    '</div>';

    const mockCollection = jest.fn(() => {return});
    const mockClearCollection = jest.fn(() => {return});
    const mockAddCard = jest.fn(() => {return});
    const mockFinishCollection = jest.fn(() => {return});
    const mockFormat = "ST1E";

    // CardFilter initialization requires a server call, mock it with data.
    const getSets_retval = JSON.stringify(
        {"updateSetOptions": []}
    );
    fetchMock.mockResponseOnce(getSets_retval);
    
    let cf = await new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);
    
    // set up a watch on the button, verify it is unchecked
    let fedInput = $("#FEDERATION");
    let fedLabel = $('#labelFEDERATION');
    expect(fedLabel.prop("classList").contains("ui-checkboxradio-checked")).toBe(false);

    // simulate a click, verify it is now checked
    fedInput.trigger("click");
    expect(fedLabel.prop("classList").contains("ui-checkboxradio-checked")).toBe(true);

    // set up a watch on the button, verify it is unchecked
    let kazInput = $("#KAZON");
    let kazLabel = $('#labelKAZON');
    expect(kazLabel.prop("classList").contains("ui-checkboxradio-checked")).toBe(false);

    // simulate a click, verify it is now checked
    kazInput.trigger("click");
    expect(kazLabel.prop("classList").contains("ui-checkboxradio-checked")).toBe(true);
    
    // run the full filter and verify it returns what we want
    let expectedResult = "|cardType:undefined|affiliation:FEDERATION,KAZON|keyword:undefined|type:200";
    let actualResult = cf.calculateNormalFilter();

    expect(actualResult).toBe(expectedResult);
});