import Dialog from "@mui/material/Dialog";
import DialogTitle from '@mui/material/DialogTitle';
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import NumberField from './mui-number-field.jsx';

export default function ChangeDataSourceDialog({open, onCloseFunc, dataSource}) {
    function closeDataSourceDialog() {
        onCloseFunc(false);
    }
    
    return(
        <Dialog open={open} onClose={closeDataSourceDialog}>
            <DialogTitle>Data Source: {dataSource}</DialogTitle>
            <Stack>
                <Typography>Set game ID:</Typography>
                <NumberField size="small" defaultValue={0}/>
            </Stack>
        </Dialog>
    )
}