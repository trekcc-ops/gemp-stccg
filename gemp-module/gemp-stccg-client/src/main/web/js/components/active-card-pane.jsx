import Card from './card.jsx';
import decipher_card_logo_only from '../../images/decipher_card_logo_only.svg';

export default function ActiveCardPane( {card} ) {
    // TODO: add action map and action buttons
    if (!card) {
        card = {imageUrl: decipher_card_logo_only};
    }

    return(
        <Card card={card}/>
    );
}