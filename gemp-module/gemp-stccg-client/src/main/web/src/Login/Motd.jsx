import { Box, Typography } from "@mui/material";
import rand_motd from "../../js/gemp-022/motds.js";

let style = {
    transform: 'rotate(0.95turn)',
    textShadow: "4px 4px 8px black"
}

export default function Motd({id, align, padding, color, fontSize}) {
    return (
        <Box id={id} style={style}>
            <Typography
                variant="h5"
                align={align}
                padding={padding}
                color={color}
                fontSize={fontSize}
                >{rand_motd()}
            </Typography>
        </Box>
    )
};