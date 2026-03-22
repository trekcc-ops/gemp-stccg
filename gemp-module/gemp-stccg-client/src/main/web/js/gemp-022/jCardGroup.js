import Card from "./jCards.js";
import { cardScale } from "./jCards.js";

export default class CardGroup {
    container;
    x;
    y;
    width;
    height;
    belongTestFunc;
    padding = 5;
    containerPadding = 3;
    maxCardHeight = 497;
    maxCardWidth = 357;
    descDiv;

    constructor(container, belongTest, divId) {
        this.container = container;
        this.belongTestFunc = belongTest;

        if (divId != undefined) divId = '';
        this.descDiv = $("<div id='" + divId + "' class='ui-widget-content card-group'></div>");
        container.append(this.descDiv);
    }

    getCardElems() {
        let cardsToLayout = new Array();
        var that = this;
        $(".card", this.container).each(function (index) {
            let card = $(this).data("card");
            
            // Don't run a cardBelongs test if we find a .card element without associated data
            // Seems to occur on temporary cards with no on-table div, like dilemmas after a reveal.
            if (card == null) {
                console.log(`getCardElems: card data is undefined or null`);
            }
            else {
                if (that.cardBelongs(card)) {
                    cardsToLayout.push($(this));
                }
            }
        });
        return cardsToLayout;
    }

    cardBelongs(cardData) {
        return this.belongTestFunc(cardData);
    }

    setBounds(x, y, width, height) {
        this.x = x + this.containerPadding;
        this.y = y + this.containerPadding;
        this.width = width - (this.containerPadding * 2);
        this.height = height - (this.containerPadding * 2);
        if (this.descDiv != null)
            this.descDiv.css({left:x + "px", top:y + "px", width:width, height:height, position:"absolute"});
        this.layoutCards();
    }

    layoutCards() {
        alert("This should be overridden by the extending classes");
    }

    layoutCard(cardElem, x, y, width, height, index) {
        layoutCardElem(cardElem, x, y, width, height, index);

        layoutTokens(cardElem);
    }
}

export class VerticalBarGroup extends CardGroup {
    constructor(container, belongTest, createDiv) {
        super(container, belongTest, createDiv);
    }

    layoutCards() {
        var cardsToLayout = this.getCardElems();

        var cardCount = cardsToLayout.length;
        var totalHeight = 0;

        for (var cardIndex in cardsToLayout)
            totalHeight += cardsToLayout[cardIndex].data("card").getHeightForWidth(this.width);

        var topGap = 20;

        var resultPadding = Math.min(this.padding, (this.height - totalHeight - topGap) / (cardCount - 1));

        var x = this.x;
        var y = this.y + topGap;
        var index = 10;
        for (var cardIndex in cardsToLayout) {
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardHeight = (cardElem.data("card").getHeightForWidth(this.width));

            if (cardData.attachedCards.length == 1) {
                this.layoutCard(cardData.attachedCards[0], x + (this.width - cardHeight) / 2, y - (this.width - cardHeight) / 2, cardHeight, this.width, index);
                index++;
            } else {
                for (var i = 0; i < cardData.attachedCards.length; i++) {
                    this.layoutCard(cardData.attachedCards[i], x + i * (this.width - cardHeight) / (cardData.attachedCards.length - 1), y - (this.width - cardHeight) / 2, cardHeight, this.width, index);
                    index++;
                }
            }

            this.layoutCard(cardElem, x, y, this.width, cardHeight, index);

            y += cardHeight + resultPadding;
            index++;
        }
    }
}

export class NormalCardGroup extends CardGroup {

    constructor(container, belongTest, createDiv) {
        super(container, belongTest, createDiv);
    }

    layoutCards() {
        var cardsToLayout = this.getCardElems();

        var proportionsArray = this.getCardsWithAttachmentWidthProportion(cardsToLayout);

        var rows = 0;
        var result = false;
        do {
            rows++;
            result = this.layoutInRowsIfPossible(cardsToLayout, proportionsArray, rows);
        } while (!result);
    }

    getAttachedCardsWidth(maxDimension, cardData) {
        var result = 0;
        for (var i = 0; i < cardData.attachedCards.length; i++) {
            var attachedCardData = cardData.attachedCards[i].data("card");
            result += attachedCardData.getWidthForMaxDimension(maxDimension);
            result += this.getAttachedCardsWidth(maxDimension, attachedCardData);
        }
        return result;
    }

    getCardsWithAttachmentWidthProportion(cardsToLayout) {
        var proportionsArray = new Array();
        for (var cardIndex in cardsToLayout) {
            var cardData = cardsToLayout[cardIndex].data("card");
            var cardWithAttachmentWidth = cardData.getWidthForMaxDimension(1000);
            cardWithAttachmentWidth += this.getAttachedCardsWidth(1000, cardData) * 0.2;
            proportionsArray.push(cardWithAttachmentWidth / 1000);
        }
        return proportionsArray;
    }

    layoutInRowsIfPossible(cardsToLayout, proportionsArray, rowCount) {
        if (rowCount == 1) {
            var oneRowHeight = this.getHeightForLayoutInOneRow(proportionsArray);
            if (oneRowHeight * 2 + this.padding > this.height) {
                this.layoutInRow(cardsToLayout, oneRowHeight);
                return true;
            } else {
                return false;
            }
        } else {
            if (this.tryIfCanLayoutInRows(rowCount, proportionsArray)) {
                this.layoutInRows(rowCount, cardsToLayout);
                return true;
            } else {
                return false;
            }
        }
    }

    getHeightForLayoutInOneRow(proportionsArray) {
        var totalWidth = 0;
        for (var cardIndex in proportionsArray)
            totalWidth += proportionsArray[cardIndex] * this.height;

        var widthWithoutPadding = this.width - (this.padding * (proportionsArray.length - 1));
        if (totalWidth > widthWithoutPadding) {
            return Math.floor(this.height / (totalWidth / widthWithoutPadding));
        } else {
            return this.height;
        }
    }

    tryIfCanLayoutInRows(rowCount, proportionsArray) {
        var rowHeight = (this.height - (this.padding * (rowCount - 1))) / rowCount;
        if (this.maxCardHeight != null)
            rowHeight = Math.min(this.maxCardHeight, rowHeight);
        var totalWidth = 0;
        var row = 0;
        for (var cardIndex in proportionsArray) {
            var cardWidthWithAttachments = proportionsArray[cardIndex] * rowHeight;
            totalWidth += cardWidthWithAttachments;
            if (totalWidth > this.width) {
                row++;
                if (row >= rowCount)
                    return false;
                totalWidth = cardWidthWithAttachments;
            }
            totalWidth += this.padding;
        }
        return true;
    }

    layoutAttached(cardData, y, height, layoutVars) {
        for (var i = 0; i < cardData.attachedCards.length; i++) {
            var attachedCardData = cardData.attachedCards[i].data("card");
            var attachedCardWidth = attachedCardData.getWidthForMaxDimension(height);
            this.layoutAttached(attachedCardData, y, height, layoutVars);
            this.layoutCard(cardData.attachedCards[i], this.x + layoutVars.x, this.y + y, attachedCardWidth, attachedCardData.getHeightForWidth(attachedCardWidth), layoutVars.index);
            layoutVars.x += Math.floor(attachedCardWidth * 0.2);
            layoutVars.index++;
        }
    }

    layoutInRow(cardsToLayout, height) {
        if (this.maxCardHeight != null)
            height = Math.min(this.maxCardHeight, height);
        var layoutVars = {};
        layoutVars.x = 0;
        var y = Math.floor((this.height - height) / 2);

        for (var cardIndex in cardsToLayout) {
            layoutVars.index = 10;
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardWidth = cardData.getWidthForMaxDimension(height);

            this.layoutAttached(cardData, y, height, layoutVars)

            this.layoutCard(cardElem, this.x + layoutVars.x, this.y + y, cardWidth, cardData.getHeightForWidth(cardWidth), layoutVars.index);
            layoutVars.x += cardWidth;
            layoutVars.x += this.padding;
        }
    }

    layoutInRows(rowCount, cardsToLayout) {
        var rowHeight = (this.height - ((rowCount - 1) * this.padding)) / rowCount;
        if (this.maxCardHeight != null)
            rowHeight = Math.min(this.maxCardHeight, rowHeight);
        var yBias = Math.floor((this.height - (rowHeight * rowCount) - (this.padding * (rowCount - 1))) / 2);
        var layoutVars = {};
        layoutVars.x = 0;
        var row = 0;
        var y = yBias;

        for (var cardIndex in cardsToLayout) {
            layoutVars.index = 10;
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardWidth = cardData.getWidthForMaxDimension(rowHeight);

            var attachmentWidths = this.getAttachedCardsWidth(rowHeight, cardData) * 0.2;
            var cardWidthWithAttachments = cardWidth + attachmentWidths;
            if (layoutVars.x + cardWidthWithAttachments > this.width) {
                row++;
                layoutVars.x = 0;
                y = yBias + row * (rowHeight + this.padding);
            }

            this.layoutAttached(cardData, y, rowHeight, layoutVars);
            this.layoutCard(cardElem, this.x + layoutVars.x, this.y + y, cardWidth, cardData.getHeightForWidth(cardWidth), layoutVars.index);
            layoutVars.x += cardWidth;
            if (layoutVars.x > this.width)
                return false;
            layoutVars.x += this.padding;
        }

        return true;
    }
}

export class PlayPileCardGroup extends CardGroup {
    // PlayPileCardGroup assumes all cards are vertical

    overlap;
    player;

        // Stacked implementation
    constructor(container, player, belongTest, createDiv) {
        super(container, belongTest, createDiv);
        this.player = player;
        this.overlap = 6;
        this.maxCardHeight = 150;
    }

    cardBelongs(cardData) {
        if (cardData.owner != this.player) {
            return false;
        } else {
            return this.belongTestFunc(cardData);
        }
    }

        // Stacked implementation
    layoutCards() {
        var cardsToLayout = this.getCardElems();

        var proportionsArray = new Array();
        for (var cardIndex in cardsToLayout) {
            proportionsArray.push(cardScale); // cardScale defined in jCards.js
        }

        this.overlap = this.scaleOverlapToFit(proportionsArray);
        var oneRowHeight = this.getHeightForLayoutInOneRow(proportionsArray);
        this.layoutInStack(cardsToLayout, oneRowHeight);

    }

        // Stacked implementation
    scaleOverlapToFit(proportionsArray) {
        var newOverlap = this.overlap;
        var cardCount = proportionsArray.length - 1;
        var maxCardWidth = this.maxCardHeight * cardScale; // cardScale defined in jCards.js
        var heightAvailable = this.height - (this.containerPadding * 2);
        var widthAvailable = this.width - (this.containerPadding * 2);

        if ((this.maxCardHeight + newOverlap * cardCount) > heightAvailable) {
            newOverlap = (heightAvailable - this.maxCardHeight) / cardCount;
        }
        if ((this.maxCardWidth + newOverlap * cardCount) > widthAvailable) {
            newOverlap = (widthAvailable - this.maxCardWidth) / cardCount;
        }

        // Don't allow overlap to get smaller than 1
        return Math.max(newOverlap, 1);
    }

        // Stacked implementation
    getHeightForLayoutInOneRow(proportionsArray) {
        var maxHeightNeeded = this.maxCardHeight;
        var maxWidthNeeded = this.maxCardHeight * cardScale; // cardScale defined in jCards.js

            // Remove padding from this calculation because overlap will not be scaled
        var maxWidthAvailable = this.width - (this.overlap * (proportionsArray.length - 1));
        var maxHeightAvailable = this.height - (this.overlap * (proportionsArray.length - 1));

        var scalingFactor = Math.min(1, (maxWidthAvailable / maxWidthNeeded), (maxHeightAvailable / maxHeightNeeded));
        return Math.floor(maxHeightNeeded * scalingFactor);
    }

        // Stacked implementation
    tryIfCanLayoutInRows(rowCount, proportionsArray) {
        var rowHeight = (this.height - (this.padding * (rowCount - 1))) / rowCount;
        if (this.maxCardHeight != null)
            rowHeight = Math.min(this.maxCardHeight, rowHeight);
        var totalWidth = 0;
        var row = 0;
        for (var cardIndex in proportionsArray) {
            var cardWidthWithAttachments = proportionsArray[cardIndex] * rowHeight;
            totalWidth += cardWidthWithAttachments;
            if (totalWidth > this.width) {
                row++;
                if (row >= rowCount)
                    return false;
                totalWidth = cardWidthWithAttachments;
            }
            totalWidth += this.padding;
        }
        return true;
    }

        // Stacked implementation
    layoutInStack(cardsToLayout, height) {
        if (this.maxCardHeight != null)
            height = Math.min(this.maxCardHeight, height);
        var layoutVars = {};
        layoutVars.x = this.x;
        layoutVars.y = this.y;

        for (var cardIndex in cardsToLayout) {
            layoutVars.index = 10;
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardWidth = cardData.getWidthForMaxDimension(height);
            this.layoutCard(cardElem, layoutVars.x, layoutVars.y, cardWidth, cardData.getHeightForWidth(cardWidth), layoutVars.index);
            layoutVars.x += this.overlap;
            layoutVars.y += this.overlap;
        }
    }

        // Stacked implementation
    layoutInRows(rowCount, cardsToLayout) {
        var rowHeight = (this.height - ((rowCount - 1) * this.padding)) / rowCount;
        if (this.maxCardHeight != null)
            rowHeight = Math.min(this.maxCardHeight, rowHeight);
        var yBias = Math.floor((this.height - (rowHeight * rowCount) - (this.padding * (rowCount - 1))) / 2);
        var layoutVars = {};
        layoutVars.x = 0;
        var row = 0;
        var y = yBias;

        for (var cardIndex in cardsToLayout) {
            layoutVars.index = 10;
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardWidth = cardData.getWidthForMaxDimension(rowHeight);

            if (layoutVars.x + cardWidth > this.width) {
                row++;
                layoutVars.x = 0;
                y = yBias + row * (rowHeight + this.padding);
            }

            this.layoutCard(cardElem, this.x + layoutVars.x, this.y + y, cardWidth, cardData.getHeightForWidth(cardWidth), layoutVars.index);
            layoutVars.x += cardWidth;
            if (layoutVars.x > this.width)
                return false;
            layoutVars.x += this.padding;
        }

        return true;
    }
}

export class NormalGameCardGroup extends NormalCardGroup {

    player;

    constructor(container, player, belongTest, createDiv) {
        super(container, belongTest, createDiv);
        this.player = player;
    }

    cardBelongs(cardData) {
        if (cardData.owner != this.player) {
            return false;
        } else {
            return this.belongTestFunc(cardData);
        }
    }

}

export class TableCardGroup extends CardGroup {

    locationIndex;
    bottomPlayerId;
    /**
     * Initializes variables
     */
     constructor(container, belongTest, createDiv, locationIndex, bottomPlayerId) {
        super(container, belongTest, createDiv);
        this.descDiv.removeClass("card-group");
        this.descDiv.addClass("st1e-card-group");
        this.locationIndex = locationIndex;
        this.bottomPlayerId = bottomPlayerId;
        this.heightPadding = 1;
        this.widthPadding = 5;
        this.columnWidthToAttachedHeightAboveRatio = 0.17;
    }

   /**
    * Performs laying out the cards in the group.
    */
    layoutCards(){
        // Get the cards to layout
        var cardsToLayout = this.getCardElems();
        if (cardsToLayout.length == 0) {
            return;
        }

        var columns;
        var columnCount = 0;
        if (((this.width / this.height) / 1) > (this.maxCardWidth / this.maxCardHeight)) {
            columns = Math.floor(cardsToLayout.length / 3);
        }

        // Attempt to layout the cards in as few columns as possible, until enough columns to layout cards nicely
        var result = false;
        do {
            columnCount++;
            result = this.layoutInColumnsIfPossible(cardsToLayout, columnCount);
        } while (!result);
    }

   /**
    * Get the total height the attached cards extend above (and below) the specified card.
    * @param {Card} cardData the card
    * @param {Number} columnWidth the column width
    * @param {Number} totalHeightSoFar the combined height of the specified card and any attached cards looked at so far
    * @return {Number} the total height of the attached cards
    */
    getAttachedCardsHeight(cardData, columnWidth, totalHeightNotAboveSoFar) {
        var result = 0;
        for (var i = 0; i < cardData.attachedCards.length; i++) {
            var attachedCardData = cardData.attachedCards[i].data("card");
            var attachedCardHeight = attachedCardData.getHeightForColumnWidth(columnWidth);
            var attachedCardHeightAbove = Math.min(attachedCardHeight, (columnWidth * this.columnWidthToAttachedHeightAboveRatio));
            result += attachedCardHeightAbove;
            var attachedCardHeightBelow = Math.max(0, attachedCardHeight - attachedCardHeightAbove - totalHeightNotAboveSoFar);
            result += attachedCardHeightBelow;
            totalHeightNotAboveSoFar += attachedCardHeightBelow;
            result += this.getAttachedCardsHeight(attachedCardData, columnWidth, totalHeightNotAboveSoFar);
        }
        return result;
    }

   /**
    * Attempts to layout cards in the specified number of columns.
    * @param {Array} cardsToLayout the cards to layout
    * @param {Number} columnCount the number of columns in which to layout cards
    * @return {Boolean} true if layout was performed, otherwise false
    */
    layoutInColumnsIfPossible(cardsToLayout, columnCount) {
        // Determine column width if layout in multiple columns
        var columnWidth = this.getWidthForLayoutInColumns(cardsToLayout, columnCount);

        if (columnCount == 1) {
            // If only one card, or column width is wide enough, then just layout in one column
            if (cardsToLayout.length == 1 || (columnWidth * 2.5 > this.width)) {
                var xOffset = Math.max(this.widthPadding, Math.floor((this.width - columnWidth) / 2));
                this.layoutInColumn(cardsToLayout, columnWidth, xOffset);
                return true;
            } else {
                return false;
            }
        } else {
            // If same number of cards as columns, or cards can layout nicely, then layout cards
            if (cardsToLayout.length == columnCount || (columnWidth * columnCount * 2.5 > this.width)) {
                this.layoutInColumns(cardsToLayout, columnWidth, columnCount);
                return true;
            } else {
                return false;
            }
        }
    }

   /**
    * Determine the layout using the specified number of columns is valid.
    * @param {Array} cardsToLayout the cards to layout
    * @param {Number} columnCount the number of columns in which to layout cards
    * @return {Number} the column width, or 0 if not valid
    */
    getWidthForLayoutInColumns(cardsToLayout, columnCount) {
        var columnWidth = Math.min(this.maxCardWidth, (this.width - (this.widthPadding * columnCount)) / columnCount);
        var maxVerticalCardWidth = Math.min(this.maxCardWidth, columnWidth * cardScale);
        var numColumnsRemainingToLayout = columnCount;
        var numCardsRemainingToLayout = cardsToLayout.length;
        var largestTotalCardHeight = 0;

        var numCardsInColumn = 0;
        var totalCardHeight = this.heightPadding;
        var overlappedHeight = 0;

        for (var cardIndex in cardsToLayout) {
            totalCardHeight -= overlappedHeight;
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardHeight = cardData.getHeightForColumnWidth(maxVerticalCardWidth);
            var cardWidth = cardData.getWidthForHeight(cardHeight);
            var attachmentHeights = this.getAttachedCardsHeight(cardData, maxVerticalCardWidth, cardHeight);
            var cardHeightWithAttachments = cardHeight + attachmentHeights;
            totalCardHeight += cardHeightWithAttachments;
            overlappedHeight = (cardHeight / 2);
            numCardsInColumn++;

            var cardsToPutInColumn = Math.ceil(numCardsRemainingToLayout / numColumnsRemainingToLayout);

            if (numCardsInColumn >= cardsToPutInColumn) {
                largestTotalCardHeight = Math.max(largestTotalCardHeight, totalCardHeight);
                numColumnsRemainingToLayout--;
                numCardsRemainingToLayout -= numCardsInColumn;
                numCardsInColumn = 0;
                totalCardHeight = this.heightPadding;
                overlappedHeight = 0;
            }
        }

        if (largestTotalCardHeight > this.height) {
            return Math.min(Math.floor(columnWidth / (largestTotalCardHeight / this.height)), this.maxCardWidth);
        } else {
            return Math.min(columnWidth, this.maxCardWidth);
        }
    }

   /**
    * Layout the cards in a single column.
    * @param {Array} cardsToLayout the cards to layout
    * @param {Number} the column width
    * @param {Number} the x-offset for the column
    */
    layoutInColumn(cardsToLayout, columnWidth, xOffset) {
        var maxVerticalCardWidth = Math.min(this.maxCardWidth, columnWidth * cardScale);
        var totalCardHeight = this.heightPadding;
        var overlappedHeight = 0;

        // Determine the total height of the all the cards
        for (var cardIndex in cardsToLayout) {
            totalCardHeight -= overlappedHeight;
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardHeight = cardData.getHeightForColumnWidth(maxVerticalCardWidth);
            var cardWidth = cardData.getWidthForHeight(cardHeight);
            var attachmentHeights = this.getAttachedCardsHeight(cardData, maxVerticalCardWidth, cardHeight);
            var cardHeightWithAttachments = cardHeight + attachmentHeights;
            totalCardHeight += cardHeightWithAttachments;
            overlappedHeight = (cardHeight / 2);
        }

        // Initialize layout variables
        var layoutVars = {};
        layoutVars.index = 10;
        layoutVars.x = xOffset;
        layoutVars.y = Math.floor((this.height - (totalCardHeight)) / 2);

        // Layout the cards
        for (var cardIndex in cardsToLayout) {
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardHeight = cardData.getHeightForColumnWidth(maxVerticalCardWidth);
            var cardWidth = cardData.getWidthForHeight(cardHeight);
            var attachmentHeights = this.getAttachedCardsHeight(cardData, maxVerticalCardWidth, cardHeight);
            var cardHeightWithAttachments = cardHeight + attachmentHeights;

            // Layout the card (and attached cards)
            this.layoutAttached(cardData, maxVerticalCardWidth, layoutVars)
            this.layoutCard(cardElem, this.x + layoutVars.x, this.y + layoutVars.y, cardWidth, cardHeight, layoutVars.index, cardData);
            layoutVars.index++;

            // Update layout variables
            layoutVars.index++;
            layoutVars.x += (maxVerticalCardWidth / 10);
            layoutVars.y += (Math.floor(cardHeight) / 2);
        }
    }

   /**
    * Layout the attached cards.
    * @param {Card} cardData the card
    * @param {Number} the column width
    * @param {Object} the layout variables
    */
    layoutAttached(cardData, columnWidth, layoutVars) {
        for (var i = 0; i < cardData.attachedCards.length; i++) {
            var attachedCardData = cardData.attachedCards[i].data("card");
            var attachedCardHeight = attachedCardData.getHeightForColumnWidth(columnWidth);
            var attachedCardWidth = attachedCardData.getWidthForHeight(attachedCardHeight);

            // Layout cards attached to this card
            this.layoutAttached(attachedCardData, columnWidth, layoutVars);

            // Layout the card
            this.layoutCard(cardData.attachedCards[i], this.x + layoutVars.x, this.y + layoutVars.y, attachedCardWidth, attachedCardHeight, layoutVars.index, attachedCardData);

            // Update layout variables
            layoutVars.index++;
            layoutVars.y += Math.floor(columnWidth * this.columnWidthToAttachedHeightAboveRatio);
        }
    }

   /**
    * Layout the cards in a specified number of columns.
    * @param {Array} cardsToLayout the cards to layout
    * @param {Number} the column width
    * @param {Number} columnCount the number of columns in which to layout cards
    */
    layoutInColumns(cardsToLayout, columnWidth, columnCount) {
        var numCardsRemainingToLayout = cardsToLayout.length;
        var numColumnsRemainingToLayout = columnCount;

        var numCardsInColumn = 0;
        var cardsToLayoutInColumn = new Array();
        var maxCardsPerColumn = Math.ceil(numCardsRemainingToLayout / numColumnsRemainingToLayout);
        var xOffset = Math.max(this.widthPadding, Math.floor((this.width - (columnWidth * columnCount) - (this.widthPadding * columnCount)) / 2));

        for (var cardIndex in cardsToLayout) {
            var cardElem = cardsToLayout[cardIndex];
            cardsToLayoutInColumn.push(cardElem);
            numCardsInColumn++;
            var cardsToPutInColumn = Math.ceil(numCardsRemainingToLayout / numColumnsRemainingToLayout);

            if (numCardsInColumn >= cardsToPutInColumn) {
                // Layout the cards in a column
                this.layoutInColumn(cardsToLayoutInColumn, columnWidth, xOffset);
                xOffset += (columnWidth + this.widthPadding)
                numColumnsRemainingToLayout--;
                numCardsRemainingToLayout -= numCardsInColumn;
                numCardsInColumn = 0;
                cardsToLayoutInColumn = new Array();
            }
        }

        if (cardsToLayoutInColumn.length > 0) {
            xOffset += (columnWidth + this.widthPadding)
            this.layoutInColumn(cardsToLayoutInColumn, columnWidth, xOffset);
        }
    }
}


export class MissionCardGroup extends CardGroup {

    locationIndex;
    bottomPlayerId;
    /**
     * Initializes variables
     */
     constructor(container, belongTest, createDiv, locationIndex, bottomPlayerId) {
        super(container, belongTest, createDiv);
        this.descDiv.removeClass("card-group");
        this.descDiv.addClass("st1e-card-group");
        this.locationIndex = locationIndex;
        this.bottomPlayerId = bottomPlayerId;
        this.sharedOverlap = .60; // Percentage of a bottom shared mission card that will be covered by the mission on top
    }

   /**
    * Performs laying out the cards in the group.
    */
    layoutCards() {
        // Get the cards to layout
        var cardsToLayout = this.getCardElems();
        if (cardsToLayout.length == 0) {
            return;
        }

        // TODO - Make sure the mission on top is always correct. The order isn't specified here

        // Get max card size that will fit in the container
            // cardScale = width / height of card template, defined in JCards.js
        var sharedMissionScale = cardScale / (2 - this.sharedOverlap);
        if ((this.width / this.height) > sharedMissionScale) {
            this.maxCardHeight = this.height / (2 - this.sharedOverlap);
            this.maxCardWidth = this.maxCardHeight * cardScale;
        } else {
            this.maxCardWidth = this.width;
            this.maxCardHeight = this.maxCardWidth / cardScale;
        }

        var index = 10;
        // Layout cards. Assumes no padding and that there will never be more than 2 missions in the group.
        for (var cardIndex in cardsToLayout) {
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardX = this.x + ((this.width - this.maxCardWidth) / 2);
            if (cardsToLayout.length == 1) {
                var cardY = this.y + ((this.height - this.maxCardHeight) / 2);
            } else if (cardData.owner == this.bottomPlayerId) {
                var cardY = this.y + ((this.height - (this.maxCardHeight * this.sharedOverlap)) / 2);
            } else {
                var cardY = this.y + ((this.height - (this.maxCardHeight * (2 - this.sharedOverlap))) / 2);
            }
            cardX = cardX - this.x;
            cardY = cardY - this.y;
            cardElem.detach().appendTo(this.descDiv);

            this.layoutCard(cardElem, cardX, cardY, this.maxCardWidth, this.maxCardHeight, index, cardData);
            index++;
        }
    }
}



export function layoutCardElem(cardElem, x, y, width, height, index) {
    x = Math.floor(x);
    y = Math.floor(y);
    width = Math.floor(width);
    height = Math.floor(height);
    if (cardElem.css("left") == (x + "px") && cardElem.css("top") == (y + "px")
        && cardElem.css("width") == (width + "px") && cardElem.css("height") == (height + "px")
        && cardElem.css("z-index") == index)
        return;
    cardElem.css({position:"absolute", left:x + "px", top:y + "px", width:width, height:height, "z-index":index });

    //var tokenOverlay = $(".tokenOverlay", cardElem);
    //if (tokenOverlay.length > 0)
    //    tokenOverlay.css({position:"absolute", left:0 + "px", top:0 + "px", width:width, height:height});

    $(".errataOverlay", cardElem).css({position:"absolute", left:0 + "px", top:0 + "px", width:width, height:height});
    $(".foilOverlay", cardElem).css({position:"absolute", left:0 + "px", top:0 + "px", width:width, height:height});

    var maxDimension = Math.max(width, height);
    var borderWidth = Math.floor(maxDimension / 30);

    var borderOverlay = $(".borderOverlay", cardElem);
    if (borderOverlay.hasClass("noBorder")) {
        borderWidth = 0;
    }
    //borderOverlay.css({position:"absolute", left:0 + "px", top:0 + "px", width:width - 2 * borderWidth, height:height - 2 * borderWidth, "border-width":borderWidth + "px"});
    borderOverlay.css({width:width - 2 * borderWidth, height:height - 2 * borderWidth, "border-width":borderWidth + "px"});
    

    var sizeListeners = cardElem.data("sizeListeners");
    if (sizeListeners != null)
        for (var i = 0; i < sizeListeners.length; i++)
            sizeListeners[i].sizeChanged(cardElem, width, height);
}

export function layoutTokens(cardElem) {
    var tokenOverlay = $(".tokenOverlay", cardElem);

    if (tokenOverlay.length > 0) {
        var width = cardElem.width();
        var height = cardElem.height();
        var maxDimension = Math.max(width, height);

        var tokenSize = Math.floor(maxDimension / 13) * 2;

        // Remove all existing tokens
        $(".token", tokenOverlay).remove();

        var tokens = cardElem.data("card").tokens;
        if (tokens != null) {
            var tokenInColumnMax = 10;
            var tokenColumns = 0;

            for (const token in tokens)
                if (Object.prototype.hasOwnProperty.call(tokens, token) && tokens[token] > 0) {
                    tokenColumns += (1 + Math.floor((tokens[token] - 1) / tokenInColumnMax));
                }

            var tokenIndex = 1;
            for (const token in tokens)
                if (Object.prototype.hasOwnProperty.call(tokens, token)) {
                    if (token == "count") {
                        if (tokens[token] > 0) {
                            var tokenElem = $("<div class='cardCount token'>" + tokens[token] + "</div>").css({position:"absolute", left:((width - 20) / 2) + "px", top:((height - 18) / 2) + "px"});
                            tokenOverlay.append(tokenElem);
                        }
                    } else if (tokens[token] > 0) {
                        var tokenCount = tokens[token];

                        var tokenX = (tokenIndex * (width / (tokenColumns + 1))) - (tokenSize / 2);
                        var tokenInColumnIndex = 0;
                        for (var i = 0; i < tokenCount; i++) {
                            if (tokenInColumnIndex == tokenInColumnMax) {
                                tokenInColumnIndex = 0;
                                tokenIndex++;
                                tokenX = (tokenIndex * (width / (tokenColumns + 1))) - (tokenSize / 2);
                            }
                            var tokenY = Math.floor((maxDimension / 13) * (1 + tokenInColumnIndex));

                            if (i%2 == 1)
                                tokenX += Math.floor(tokenSize / 5);

                            var tokenElem = $("<img class='token' src='images/tokens/" + token.toLowerCase() + ".png' width='" + tokenSize + "' height='" + tokenSize + "'></img>").css({position:"absolute", left:tokenX + "px", top:tokenY + "px"});
                            tokenOverlay.append(tokenElem);
                            tokenInColumnIndex++;
                        }
                        tokenIndex++;
                    }
                }
        }
    }
}
