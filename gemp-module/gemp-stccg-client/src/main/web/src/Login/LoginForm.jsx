import {useState} from 'react';
import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';

export default function LoginForm({ comms }) {
    // variables, onchange function binds, and default values
    const [username, setUsername] = useState('');
    const [usernameErrorMsg, setUsernameErrorMsg] = useState('');
    const [usernameError, setUsernameError] = useState(false);
    const [password, setPassword] = useState('');
    const [pw1ErrorMsg, setpw1ErrorMsg] = useState('');
    const [pw1Error, setPw1Error] = useState(false);
    const [statusMsg, setStatusMsg] = useState('');

    function handleUsernameChange(e) {
        setUsernameError(false);
        setUsernameErrorMsg("");
        setUsername(e.target.value);
    }

    function handlePasswordChange(e) {
        setPw1Error(false);
        setpw1ErrorMsg("");
        setPassword(e.target.value);
    }

    function usernameEmpty(e) {
        setUsernameErrorMsg("Username cannot be empty.");
        setUsernameError(true);
    }

    function pw1Empty(e) {
        setpw1ErrorMsg("Password cannot be empty.");
        setPw1Error(true);
    }

    function checkForEnter(e) {
        if (e.key === "Enter") {
            handleLoginButton(e);
        }
    }

    function handleLoginButton(e) {
        if (username === '') {
            usernameEmpty(e);
            return;
        }

        if (password === '') {
            pw1Empty(e);
            return;
        }

        comms.login(
            username,
            password,
            function (_, status) {
                if(status === "202") {
                    setStatusMsg("Your password has been reset. Please use the registration form to enter a new password.");
                }
                else {
                    location.href = "/gemp-module/hall.html";
                }
            },
            {
                "0": function () {
                    alert("Unable to connect to server, either server is down or there is a problem" +
                        " with your internet connection");
                },
                "401": function () {
                    setStatusMsg("Invalid username or password. Try again.");
                },
                "403": function () {
                    setStatusMsg("You have been permanently banned. If you think it was a mistake please appeal with the CC on the forums.");
                },
                "409": function () {
                    setStatusMsg("You have been temporarily banned. You can try logging in at a later time. If you think it was a mistake please with the CC on the forums.");
                },
                "503": function () {
                    setStatusMsg("Server is down for maintenance. Please come at a later time.");
                }
            }
        );
    }

    return(
        <Box>
            <Stack spacing={1}>
                <TextField
                    variant='filled'
                    id='username'
                    label='Username'
                    type='text'
                    value={username}
                    error={usernameError}
                    helperText={usernameErrorMsg}
                    onChange={handleUsernameChange}
                    onKeyDown={checkForEnter}
                    />
                <TextField
                    variant="filled"
                    id='password'
                    label='Password'
                    type='password'
                    autoComplete="current-password"
                    value={password}
                    error={pw1Error}
                    helperText={pw1ErrorMsg}
                    onChange={handlePasswordChange}
                    onKeyDown={checkForEnter}
                    />
                <Button variant="contained" onClick={handleLoginButton} id='loginButton'>Login</Button>
                <Box id="status">{statusMsg}</Box>
            </Stack>
        </Box>
    );
}
