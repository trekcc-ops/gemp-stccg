import { createRoot } from 'react-dom/client';
import "../../js/jquery/jquery-3.7.1.js";
import "../../js/jquery/jquery-ui-1.14.1/jquery-ui.js";
import GempClientCommunication from "../../js/gemp-022/communication.js";
import LoginRegisterTabs from "./LoginRegisterTabs.jsx";


var comm = new GempClientCommunication("/gemp-stccg-server", function () {
    alert("Unable to contact the server");
});

document.addEventListener("DOMContentLoaded",
    function () {
        comm.getStatus(
            function (html) {
                $("#serverStatus").append(html);
            });
        
        const domNode = document.getElementById('interaction');
        const root = createRoot(domNode);
        root.render(<LoginRegisterTabs comms={comm} />);
    }
);