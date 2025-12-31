import { useState, useEffect } from 'react';
import Box from '@mui/material/Box';
import Badge from '@mui/material/Badge';
import CircularProgress from '@mui/material/CircularProgress';
import Tooltip from '@mui/material/Tooltip';
import DangerousIcon from '@mui/icons-material/Dangerous';
import { useTrekccImage } from '../hooks/useTrekccImage.jsx';

/*
example card: {
  "cardId": 55,
  "title": "Jadzia Dax",
  "blueprintId": "112_208",
  "owner": "andrew",
  "locationId": 7,
  "attachedToCardId": 48,
  "isStopped": false,
  "imageUrl": "https://www.trekcc.org/1e/cardimages/ds9/jadziadax.gif",
  "cardType": "PERSONNEL",
  "uniqueness": "UNIQUE"
}
*/

function cardTooltip(card, gamestate) {
    if (card.cardType === "PERSONNEL") {
        {/* TODO: Card stats from GameState; waiting on GameState. */}
        let retstring = "";
        if (card.uniqueness === "UNIVERSAL") {
            const universalDiamond = `\u2756`.normalize();
            retstring = `${universalDiamond} ${card.title}`;
        }
        else {
            retstring = `${card.title}`;
        }
        return(retstring);
    }
    else if (card.cardType === "FACILITY") {
        {/* TODO: Card stats from GameState; waiting on GameState. */}
        return(card.title);
    }
    else if (card.cardType === "SHIP") {
        {/* TODO: Card stats from GameState; waiting on GameState. */}
        let retstring = "";
        if (card.uniqueness === "UNIVERSAL") {
            const universalDiamond = `\u2756`.normalize();
            retstring = `${universalDiamond} ${card.title}`;
        }
        else {
            retstring = `${card.title}`;
        }
        return(retstring);
    }
    else if (card.cardType === "MISSION") {
        let locationData;
        for (let spacelineLocation of Object.values(gamestate["spacelineLocations"])) {
            if (spacelineLocation.locationId === card.locationId) {
                locationData = spacelineLocation;
            }
        }

        let retstring = "";
        if (locationData) {
            retstring = `${card.title} (${locationData.quadrant})`;
        }
        return(retstring);
    }
    else {
        // TODO: Do we want to show anything unique for these in the tooltip?
        // Artifact
        // Damage Marker
        // Dilemma
        // Doorway
        // Equipment
        // Event
        // Incident
        // Interrupt
        // Objective
        // Q Artifact
        // Q Dilemma
        // Q Event
        // Q Interrupt
        // Q Mission
        // Site
        // Tactic
        // Time Location
        // Tribble
        // Trouble
        return(card.title);
    }
}

export default function Card( {card, gamestate, index, sx} ) {
    let badge_color = "error";
    let stopped_badge = 0; // hidden by default
    let overlay = {};
    if (card.isStopped) {
        stopped_badge = "Stopped";
        overlay = {filter: "grayscale(80%)"};
    }
    
    const imageUrl = useTrekccImage(card.imageUrl);
    const tooltipText = cardTooltip(card, gamestate);

    const columnPosition = index ? `${index+1}/auto` : undefined;
    const rowPosition = index ? `${index+1}/auto` : undefined;
    const cardZIndex = index ? -index : 0;

    return(
        <Box
            data-cardid={card.cardId}
            sx={{
                gridColumn: columnPosition,
                gridRow: rowPosition,
                zIndex: cardZIndex,
                ...sx //also use incoming styles from parent
            }}
        >
            <Tooltip title={tooltipText}>
            {/*<Badge color={badge_color} badgeContent={stopped_badge}>*/}
                {/* If imageurl is null, show a circular progress spinner, otherwise load the graphic. */}
                {imageUrl ? <img height={"100%"} width={"100%"} src={imageUrl} style={overlay} /> : <CircularProgress/>}
            {/*</Badge>*/}
            </Tooltip>
        </Box>
    );
}