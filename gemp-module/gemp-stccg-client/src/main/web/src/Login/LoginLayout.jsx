import LoginRegisterTabs from "./LoginRegisterTabs.jsx";
import ServerStatus from "./ServerStatus.jsx";
import { Stack, Typography } from "@mui/material";
import Splash from "./Splash.jsx";
import { softwareName, versionDescription, versionNumber } from "../../js/gemp-022/common.js";

export default function LoginLayout({ comms }) {
    return (
        <div id="centerContainer">
            <div id="banner">
                <Typography 
                    id="banner-title"
                    variant="h1"
                    color="#63C5DA"
                    align="center"
                    fontSize={"400%"}
                    fontWeight={700}
                    sx={{padding: '10px'}}
                    >Star Trek Customizable Card Game
                </Typography>

                <Splash
                    id="splash"
                    color="#f8f2cb"
                    align="center"
                    fontSize={"150%"}
                    fontWeight={400}
                    sx={{padding: '10px'}}
                />
            </div>
            <Stack direction={"column"} alignItems={"center"}>
                <ServerStatus comms={comms} />
                <div id="error"></div>
                <LoginRegisterTabs comms={comms} />
            </Stack>
            <div id="info">
                <p><i>Star Trek</i> CCG encompasses three card games published by Decipher from 1994-2006.<br />
                    Since 2007, these games have been updated and maintained by the player-run 
                    <a href="https://www.trekcc.org/">Continuing Committee.</a><br />
                    {softwareName} is a free platform to play in your browser.<br />
                    This site is a work in progress.<br />
                    <a href="https://en.wikipedia.org/wiki/Star_Trek_Customizable_Card_Game">Star Trek CCG on Wikipedia</a>
                </p>
                <p>Based on GEMP, created by Marcin Sciesinski<br />
                    <a href="https://gemp.starwarsccg.org/gemp-swccg/">Star Wars CCG GEMP</a> - <a href="https://www.starwarsccg.org/">Star Wars Players Committee</a><br />
                    <a href="https://play.lotrtcgpc.net/gemp-lotr/">Lord of the Rings GEMP</a> - <a href="https://lotrtcgpc.net/">Lord of the Rings Player's Council</a><br />
                </p>
                <p>
                    <Typography
                    id="banner-subtitle"
                    variant="h2"
                    color="#63C5DA"
                    align="right"
                    fontSize={"100%"}
                    fontWeight={700}
                    sx={{padding: '10px'}}
                    >{softwareName} {versionNumber} ({versionDescription})</Typography>
                </p>
            </div>
        </div>
    );
}