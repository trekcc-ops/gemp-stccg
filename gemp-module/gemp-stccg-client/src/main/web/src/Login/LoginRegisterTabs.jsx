import {useState} from 'react';
import '@fontsource/roboto/300.css';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/700.css';
import Box from '@mui/material/Box';
import Tab from '@mui/material/Tab';
import TabContext from '@mui/lab/TabContext';
import TabList from '@mui/lab/TabList';
import TabPanel from '@mui/lab/TabPanel';
import LoginForm from './LoginForm.jsx';
import RegistrationForm from "./RegistrationForm.jsx";
import { theme } from '../../js/gemp-022/common.js';
import { ThemeProvider } from '@mui/material/styles';

export default function LoginRegisterTabs({ comms }) {
    const [value, setValue] = useState('1');

    const handleChange = (event, newValue) => {
      setValue(newValue);
    };
  
    return (
        <ThemeProvider theme={theme}>
            <Box id="login">
                <TabContext value={value}>
                    <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                        <TabList onChange={handleChange} >
                        <Tab label="Login" value="1" />
                        <Tab label="Register" value="2" />
                        </TabList>
                    </Box>
                    <TabPanel value="1">
                        <LoginForm value={value} index={0} comms={comms} />
                    </TabPanel>
                    <TabPanel value="2">
                        <RegistrationForm value={value} index={2} comms={comms} />
                    </TabPanel>
                </TabContext>
            </Box>
        </ThemeProvider>
    );
}