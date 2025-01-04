import {useState} from 'react';

export default function RegistrationForm({ comms }) {
    // variables, onchange function binds, and default values
    const [login, setLogin] = useState('');
    const [password1, setPassword1] = useState('');
    const [password2, setPassword2] = useState('');
    const [errorMsg, setErrorMsg] = useState('');

    function handleLoginChange(e) {
        setLogin(e.target.value);
    }

    function handlePassword1Change(e) {
        setPassword1(e.target.value);
    }

    function handlePassword2Change(e) {
        setPassword2(e.target.value);
    }

    function handleRegisterButton(e) {
        //alert(`Username: ${login}, PW: ${password1}, PW2: ${password2}`);
        if (password1 != password2) {
            setErrorMsg("Passwords do not match! Try again.");
        }
        else {
            comms.register(
                login,
                password1,
                function (_, status) {
                    if(status == "202") {
                        setErrorMsg("Your password has successfully been reset!  Please refresh the page and log in.");
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
                        $(".error").html("Login is invalid. Login must be between 2-10 characters long, and contain only<br/>" +
                            " english letters, numbers or _ (underscore) and - (dash) characters.");
                    },
                    "409": function () {
                        $(".error").html("User with this login already exists in the system. Try a different one.");
                    },
                    "503": function () {
                        $(".error").html("Server is down for maintenance. Please come at a later time.");
                    }
                }
            );
        }
    }

    return(
        <div>
            Login: <input id='login' type='text' value={login} onChange={handleLoginChange} /><br/>
            Password: <input id='password' type='password' value={password1} onChange={handlePassword1Change} /><br/>
            Password repeated: <input id='password2' type='password' value={password2} onChange={handlePassword2Change} /><br/>
            <button onClick={handleRegisterButton} id='registerButton'>Register</button>
            <br /><br />
            <div id="error">{errorMsg}</div>
        </div>
    );
}
