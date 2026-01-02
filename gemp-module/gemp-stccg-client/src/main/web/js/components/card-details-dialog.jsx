import { useState } from 'react';
import DialogTitle from '@mui/material/DialogTitle';
import Dialog from '@mui/material/Dialog';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import CardStack from './card-stack';
import Card from './card';
import Stack from '@mui/material/Stack';
import NorthIcon from '@mui/icons-material/North';
import { Typography } from '@mui/material';
import CardRelationshipRow from './card-relationship-row';

export default function CardDetailsDialog( {gamestate, cardId, setCardIdFunc, isOpen, onCloseFunc} ) {
    let cardData = cardId ? gamestate["visibleCardsInGame"][cardId] : undefined;
    if (cardData){
        let cardTitle = cardData.title;
        return(
            <Dialog open={isOpen} onClose={onCloseFunc}>
                <DialogTitle>Card Details: {cardTitle}</DialogTitle>
                <Stack direction={"row"} spacing={2}>
                    <Stack direction={"column"} spacing={2} alignItems={"center"} justifyContent={"center"}>
                        {Object.hasOwn(cardData, "attachedToCardId") &&
                            <Button variant='contained' onClick={() => setCardIdFunc(cardData["attachedToCardId"])}>
                                <Stack direction={"column"} spacing={2} alignItems={"center"} justifyContent={"center"}>
                                    <NorthIcon />
                                    <Typography>UP</Typography>
                                </Stack>
                            </Button>
                        }
                    </Stack>
                    <Stack direction={"column"} spacing={2} alignItems={"center"} justifyContent={"center"}>
                        <Card card={cardData} gamestate={gamestate} openCardDetailsFunc={setCardIdFunc} />
                        <Box><Typography>Card Details</Typography></Box>
                    </Stack>
                    
                    <Stack direction={"column"} spacing={2} alignItems={"center"} justifyContent={"center"}>
                        <CardRelationshipRow title={"Attached"} gamestate={gamestate} cardData={cardData} cardPropertyFilter={"attachedToCardId"} openCardDetailsFunc={setCardIdFunc} />
                    </Stack>
                </Stack>
                {/*<Button onClick={() => {setCardIdFunc(43)}}>Change Card ID</Button>*/}
            </Dialog>
        )
    }
    else {
        return(
            <Dialog open={isOpen} onClose={onCloseFunc}>
                <DialogTitle>Card Details: N/A</DialogTitle>
            </Dialog>
        )
    }
    
}

/*
import PropTypes from 'prop-types';
CardDetailsDialog.propTypes = {
  gamestate: PropTypes.object.isRequired,
  cardId: PropTypes.string.isRequired,
  open: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired
};
*/