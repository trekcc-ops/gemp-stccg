import {useState} from 'react';
import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';

export default function RegistrationForm({ comms }) {
    // TODO: Each of these fields could be broken out to their own components.
    // variables, onchange function binds, and default values
    const [username, setUsername] = useState('');
    const [password1, setPassword1] = useState('');
    const [password2, setPassword2] = useState('');
    const [usernameErrorMsg, setUsernameErrorMsg] = useState('');
    const [pw1ErrorMsg, setpw1ErrorMsg] = useState('');
    const [pw2ErrorMsg, setpw2ErrorMsg] = useState('');
    const [usernameError, setUsernameError] = useState(false);
    const [pw1Error, setPw1Error] = useState(false);
    const [pw2Error, setPw2Error] = useState(false);
    const [statusMsg, setStatusMsg] = useState('');

    function handleUsernameChange(e) {
        setUsernameError(false);
        setUsernameErrorMsg("");
        setUsername(e.target.value);
    }

    function handlePassword1Change(e) {
        setPw1Error(false);
        setpw1ErrorMsg("");
        setPassword1(e.target.value);
    }

    function handlePassword2Change(e) {
        setPw2Error(false);
        setpw2ErrorMsg("");
        setPassword2(e.target.value);
    }

    function usernameEmpty(e) {
        setUsernameErrorMsg("Username cannot be empty.");
        setUsernameError(true);
    }

    function pw1Empty(e) {
        setpw1ErrorMsg("Password cannot be empty.");
        setPw1Error(true);
    }

    function pw2Empty(e) {
        setpw2ErrorMsg("Password cannot be empty.");
        setPw2Error(true);
    }

    function mismatchedPasswords(e) {
        setpw2ErrorMsg("Passwords do not match! Try again.");
        setPw1Error(true);
        setPw2Error(true);
    }

    function checkForEnter(e) {
        if (e.key === "Enter") {
            handleRegisterButton(e);
        }
    }

    function handleRegisterButton(e) {
        if (username === '') {
            usernameEmpty(e);
            return;
        }

        if (password1 === '') {
            pw1Empty(e);
            return;
        }
        if (password2 === '') {
            pw2Empty(e);
            return;
        }
        if (password1 != password2) {
            mismatchedPasswords(e);
            return;
        }

        comms.register(
            username,
            password1,
            function (_, status) {
                if(status == "202") {
                    setStatusMsg("Your password has successfully been reset!  Please refresh the page and log in.");
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
                "400": function () {
                    setStatusMsg("Login is invalid. Login must be between 2-10 characters long, and contain only<br/>" +
                        " english letters, numbers or _ (underscore) and - (dash) characters.");
                },
                "409": function () {
                    setStatusMsg("User with this login already exists in the system. Try a different one.");
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
                    value={password1}
                    error={pw1Error}
                    helperText={pw1ErrorMsg}
                    onChange={handlePassword1Change}
                    onKeyDown={checkForEnter}
                    />
                <TextField
                    variant="filled"
                    id='password'
                    label='Confirm Password'
                    type='password'
                    value={password2}
                    error={pw2Error}
                    helperText={pw2ErrorMsg}
                    onChange={handlePassword2Change}
                    onKeyDown={checkForEnter}
                    />
                <Button variant="contained" onClick={handleRegisterButton} id='registerButton'>Register</Button>
                <Box id="status">{statusMsg}</Box>
            </Stack>
        </Box>
    );
}
