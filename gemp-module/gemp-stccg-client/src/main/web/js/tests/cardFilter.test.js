import CardFilter from "../gemp-022/cardFilter.js";

beforeEach(() => {
    // clear any stored fetch mock statistics
    fetchMock.resetMocks();
});

test('setType can set the type', () => {
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
    
    let cf = new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);

    let selectUnderTest = $('#type');

    expect(selectUnderTest.val()).toBe('200');
    cf.setType("101");
    expect(selectUnderTest.val()).toBe('101');
});

test('calculateNormalFilter none checked', () => {
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
    
    let cf = new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);

    let expectedResult = "|cardType:undefined|keyword:undefined|type:200|phase:undefined";
    let actualResult = cf.calculateNormalFilter();

    expect(actualResult).toBe(expectedResult);
});

test('calculateNormalFilter Federation checked', () => {
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
    
    let cf = new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);
    
    // set up a watch on the button, verify it is unchecked
    let fedInput = $("#FEDERATION");
    let fedLabel = $('#labelFEDERATION');
    expect(fedLabel.prop("classList").contains("ui-checkboxradio-checked")).toBe(false);

    // simulate a click, verify it is now checked
    fedInput.trigger("click");
    expect(fedLabel.prop("classList").contains("ui-checkboxradio-checked")).toBe(true);
    
    // run the full filter and verify it returns what we want
    let expectedResult = "|cardType:undefined|affiliation:FEDERATION|keyword:undefined|type:200|phase:undefined";
    let actualResult = cf.calculateNormalFilter();

    expect(actualResult).toBe(expectedResult);
});

test('calculateNormalFilter Federation + Kazon checked', () => {
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
    
    let cf = new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);
    
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
    let expectedResult = "|cardType:undefined|affiliation:FEDERATION,KAZON|keyword:undefined|type:200|phase:undefined";
    let actualResult = cf.calculateNormalFilter();

    expect(actualResult).toBe(expectedResult);
});