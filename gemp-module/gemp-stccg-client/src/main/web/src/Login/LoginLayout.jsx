import LoginRegisterTabs from "./LoginRegisterTabs.jsx";
import ServerStatus from "./ServerStatus.jsx";
import { Stack } from "@mui/material";

export default function LoginLayout({ comms }) {
    return (
        <div id="centerContainer">
            <div id="banner">
                <h1 id="banner-text">STAR TREK CUSTOMIZABLE CARD GAME</h1>
                <h1 id="banner-sub-text">Project LetVar</h1>
                <div id="banner-sub">
                    
                </div>
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
                    GEMP is a free platform to play in your browser.<br />
                    This site is a work in progress.<br />
                    <a href="https://en.wikipedia.org/wiki/Star_Trek_Customizable_Card_Game">Star Trek CCG on Wikipedia</a><br />
                    <br />
                    Based on GEMP, created by Marcin Sciesinski<br />
                    <a href="https://gemp.starwarsccg.org/gemp-swccg/">Star Wars CCG GEMP</a> - <a href="https://www.starwarsccg.org/">Star Wars Players Committee</a><br />
                    <a href="https://play.lotrtcgpc.net/gemp-lotr/">Lord of the Rings GEMP</a> - <a href="https://lotrtcgpc.net/">Lord of the Rings Player's Council</a><br />
                </p>
            </div>
        </div>
    );
}