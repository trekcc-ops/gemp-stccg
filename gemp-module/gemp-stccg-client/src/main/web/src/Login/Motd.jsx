import { Box, Typography } from "@mui/material";
import rand_motd from "../../js/gemp-022/motds.js";

export default function Motd() {
    return (
        <Box sx={{
            display: "inline-block",
            position: "relative",
            left: "60%",
            top: "-50%",
            transform: "rotate(0.95turn)",
            "text-align": "center",
            color: "#f8f2cb",
            margin: 0,
        }}>
            <Typography variant="h5">{rand_motd()}</Typography>
        </Box>
    )
};