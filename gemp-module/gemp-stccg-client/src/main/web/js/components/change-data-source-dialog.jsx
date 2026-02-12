import { useState } from 'react';
import Dialog from "@mui/material/Dialog";
import DialogTitle from '@mui/material/DialogTitle';
import DialogActions from '@mui/material/DialogActions';
import Stack from "@mui/material/Stack";
import Button from "@mui/material/Button";
import FormGroup from '@mui/material/FormGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import Switch from '@mui/material/Switch';
import Typography from "@mui/material/Typography";
import NumberField from './mui-number-field.jsx';

export default function ChangeDataSourceDialog({open, onCloseFunc, dataSource, setDataSource}) {
    const [useLiveData, setUseLiveData] = useState(false);
    const shouldUseLiveData = (event) => {
        setUseLiveData(event.target.checked);
    }

    function closeDataSourceDialog() {
        onCloseFunc(false);
    }

    const handleSubmit = (event) => {
        event.preventDefault(); // inform the browser we're handling the event to prevent an onSubmit reload
        const formData = new FormData(event.currentTarget);
        const formJson = Object.fromEntries(formData.entries());
        const gameId = formJson.gameId;

        if(useLiveData) {
            console.log('Submitted useLiveData');
            console.log(`Submitted game ID: ${gameId}`);
        }
        else {
            console.log('Static data.')
        }
        closeDataSourceDialog();
    };

    // TODO: Get a list of games from comms.getHall()
    //       Display them
    //       Let the user select from the list
    //       Attempt to load that gameId's gamestate via URL

    function gameDataSelector() {
        return(
            <FormGroup>
                <Typography>Set game ID:</Typography>
                <NumberField name="gameId" size="small" defaultValue={0}/>
            </FormGroup>
        )
    }

    return(
        <Dialog open={open} onClose={closeDataSourceDialog}>
            <DialogTitle>Data Source: {dataSource}</DialogTitle>
            <Stack>
                <form onSubmit={handleSubmit} id="set-game-data-source">
                    <FormGroup>
                        <FormControlLabel control={
                            <Switch checked={useLiveData} onChange={shouldUseLiveData} name="useLiveDataSwitch" />
                            }
                            label="Use Live Data (WIP)"
                        />
                        {useLiveData? gameDataSelector() : null}
                    </FormGroup>
                </form>
            </Stack>
            <DialogActions>
                <Button onClick={closeDataSourceDialog}>Cancel</Button>
                <Button type="submit" form="set-game-data-source">OK</Button>
            </DialogActions>
        </Dialog>
    )
}