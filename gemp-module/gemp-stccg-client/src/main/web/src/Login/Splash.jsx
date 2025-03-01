import { Box, Typography } from "@mui/material";
import rand_splash from "../../js/gemp-022/splashes.js";

let style = {
    transform: 'rotate(0.95turn)',
    textShadow: "4px 4px 8px black"
}

export default function Splash({id, align, padding, color, fontSize}) {
    return (
        <Box id={id} style={style}>
            <Typography
                variant="h5"
                align={align}
                padding={padding}
                color={color}
                fontSize={fontSize}
                marginTop={"10px"}
                marginRight={"20px"}
                >{rand_splash()}
            </Typography>
        </Box>
    )
};