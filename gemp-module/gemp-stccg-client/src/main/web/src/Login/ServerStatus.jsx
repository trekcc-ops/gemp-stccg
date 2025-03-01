import { Box, Typography } from "@mui/material";

export default function ServerStatus({ comms }) {
    let status_text = comms.getStatus((text) => {
        return text;
    });

    return (
    <Box id="status">
        <Typography>{status_text}</Typography>
    </Box>
    );
}