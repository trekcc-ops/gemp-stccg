import CardFilter from "../gemp-022/cardFilter.js";

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
    
    let cf = new CardFilter(document.body, mockCollection, mockClearCollection, mockAddCard, mockFinishCollection, mockFormat);

    let selectUnderTest = $('#type');

    expect(selectUnderTest.val()).toBe('200');
    cf.setType("101");
    expect(selectUnderTest.val()).toBe('101');
});