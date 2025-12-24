import Stack from '@mui/material/Stack';
import SpacelineLocation from './spaceline-location.jsx';

export default function TableLayout({gamestate}) {
    let spacelineLocations = [];
    gamestate["spacelineLocations"].map((item, index) => {
        if (index === 0) {
            spacelineLocations.push(<SpacelineLocation key={item["locationId"]} gamestate={gamestate} locationid={item["locationId"]} showCore={true} />);
        }
        else {
            spacelineLocations.push(<SpacelineLocation key={item["locationId"]} gamestate={gamestate} locationid={item["locationId"]} />);
        }
    })

    return(
        <Stack id="table"
            direction="row-reverse"
        >
            {spacelineLocations}
        </Stack>
    );
}