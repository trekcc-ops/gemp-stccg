import Dialog from "@mui/material/Dialog";
import DialogTitle from '@mui/material/DialogTitle';
import DialogActions from '@mui/material/DialogActions';
import Stack from "@mui/material/Stack";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import NumberField from './mui-number-field.jsx';

export default function ChangeDataSourceDialog({open, onCloseFunc, dataSource}) {
    function closeDataSourceDialog() {
        onCloseFunc(false);
    }

    const handleSubmit = (event) => {
        event.preventDefault(); // inform the browser we're handling the event to prevent an onSubmit reload
        const formData = new FormData(event.currentTarget);
        const formJson = Object.fromEntries(formData.entries());
        const gameId = formJson.gameId;
        console.log(`Submitted game ID: ${gameId}`);
        closeDataSourceDialog();
    };

    return(
        <Dialog open={open} onClose={closeDataSourceDialog}>
            <DialogTitle>Data Source: {dataSource}</DialogTitle>
            <Stack>
                <Typography>Set game ID:</Typography>
                <form onSubmit={handleSubmit} id="set-game-id">
                    <NumberField name="gameId" size="small" defaultValue={0}/>
                </form>
                
            </Stack>
            <DialogActions>
                <Button onClick={closeDataSourceDialog}>Cancel</Button>
                <Button type="submit" form="set-game-id">OK</Button>
            </DialogActions>
        </Dialog>
    )
}