import { useState, useEffect } from 'react';
import { styled, useTheme } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import MuiDrawer from '@mui/material/Drawer';
import MuiAppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import List from '@mui/material/List';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';
import MenuIcon from '@mui/icons-material/Menu';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import InboxIcon from '@mui/icons-material/MoveToInbox';
import MailIcon from '@mui/icons-material/Mail';
import ManageAccountsIcon from '@mui/icons-material/ManageAccounts';
import AccountTreeIcon from '@mui/icons-material/AccountTree';
import CardTreeView from './card-tree-view.jsx';
import BookmarksIcon from '@mui/icons-material/Bookmarks';
import ChatIcon from '@mui/icons-material/Chat';
import HistoryIcon from '@mui/icons-material/History';
import SourceIcon from '@mui/icons-material/Source';
import Tooltip from '@mui/material/Tooltip';
import ChangeDataSourceDialog from './change-data-source-dialog.jsx';
import ActiveCardPane from './active-card-pane.jsx';
import PlayerScorePane from './player-score-pane.jsx';
import Card from './card.jsx';
import MainLayoutGrid from './main-layout-grid.jsx';
import player_state from '../../player_state.json?url';

function get_your_player_id(gamestate) {
    return gamestate["requestingPlayer"];
}

function get_opponent_player_id(gamestate) {
    let your_player_id = gamestate["requestingPlayer"];
    let opponent_names = [];
    for (const playerId of Object.keys(gamestate["playerMap"])) {
        if (playerId != your_player_id) {
            opponent_names.push(playerId);
        }
    }
    return opponent_names[0]; // assume 1 opponent
}

const drawerWidth = 240;

// CSS theming overrides for opening
const openedMixin = (theme) => ({
    width: drawerWidth,
    transition: theme.transitions.create('width', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.enteringScreen,
    }),
    overflowX: 'hidden',
});

// CSS theming overrides for closing
const closedMixin = (theme) => ({
    transition: theme.transitions.create('width', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen,
    }),
    overflowX: 'hidden',
    width: `calc(${theme.spacing(7)} + 1px)`,
    [theme.breakpoints.up('sm')]: {
        width: `calc(${theme.spacing(8)} + 1px)`,
    },
});

// React defintion of a <DrawerHeader> for layout purposes.
const DrawerHeader = styled('div')(
    ({ theme }) => ({
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'flex-end',
        padding: theme.spacing(0, 1),
        // necessary for content to be below app bar
        ...theme.mixins.toolbar,
    })
);

// React definition of the AppBar to override MUI defaults.
const AppBar = styled(MuiAppBar, { shouldForwardProp: (prop) => prop !== 'open', })(
    ({ theme }) => ({
        zIndex: theme.zIndex.drawer + 1,
        transition: theme.transitions.create(['width', 'margin'], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
        }),
        variants: [
            {
                props: ({ open }) => open,
                style: {
                    marginLeft: drawerWidth,
                    width: `calc(100% - ${drawerWidth}px)`,
                    transition: theme.transitions.create(['width', 'margin'], {
                        easing: theme.transitions.easing.sharp,
                        duration: theme.transitions.duration.enteringScreen,
                    }),
                },
            },
        ],
    })
);

// React definition of the Drawer to override MUI defaults.
const Drawer = styled(MuiDrawer, { shouldForwardProp: (prop) => prop !== 'open' })(
    ({ theme }) => ({
        width: drawerWidth,
        flexShrink: 0,
        whiteSpace: 'nowrap',
        boxSizing: 'border-box',
        variants: [
            {
                props: ({ open }) => open,
                style: {
                    ...openedMixin(theme),
                    '& .MuiDrawer-paper': openedMixin(theme),
                },
            },
            {
                props: ({ open }) => !open,
                style: {
                    ...closedMixin(theme),
                    '& .MuiDrawer-paper': closedMixin(theme),
                },
            },
        ],
    }),
);

export default function MiniDrawer() {
    const [dataSource, setDataSource] = useState(player_state);
    const [changeDataSourceDialogOpen, setChangeDataSourceDialogOpen] = useState(false);
    const [loadedGameState, setLoadedGameState] = useState(null);
    
    useEffect(() => {
        // Change this function to change the JSON input source.
        const fetchData = async () => {
            const response = await fetch(dataSource);
            const newData = await response.json();
            setLoadedGameState(newData);
        };

        fetchData();
    },[]);

    const theme = useTheme();
    const [open, setOpen] = useState(false);

    const handleDrawerOpen = () => {
        setOpen(true);
    };

    const handleDrawerClose = () => {
        setOpen(false);
    };

    // Don't render unless we have something
    if (loadedGameState){
        return (
            <Box sx={{ display: 'flex' }}>
                {/* top bar */}
                <AppBar position="fixed" open={open}>
                    <Toolbar>
                        <IconButton
                            aria-label="open drawer"
                            onClick={handleDrawerOpen}
                            edge="start"
                            sx={[
                                {
                                    marginRight: 5,
                                },
                                open && { display: 'none' },
                            ]}
                        >
                            <MenuIcon />
                        </IconButton>
                        <Stack
                            direction="row"
                            spacing={2}
                            sx={{
                                justifyContent: "space-between",
                                alignItems: "center",
                                flexGrow: 1
                            }}>

                            <PlayerScorePane id="opponent-player-score-pane" gamestate={loadedGameState} player_id={get_opponent_player_id(loadedGameState)}/>
                            <Tooltip title="Data Source">
                                <IconButton aria-label="Data Source" onClick={() => {setChangeDataSourceDialogOpen(true)}}>
                                    <SourceIcon />
                                </IconButton>
                                <ChangeDataSourceDialog open={changeDataSourceDialogOpen} onCloseFunc={setChangeDataSourceDialogOpen} dataSource={dataSource} setDataSource={setDataSource} />
                            </Tooltip>
                            <PlayerScorePane id="your-player-score-pane" gamestate={loadedGameState} player_id={get_your_player_id(loadedGameState)}/>
                                
                        </Stack>
                        <Divider orientation="vertical" flexItem sx={{padding: "10px"}} />
                        <Tooltip title="Account">
                            <IconButton
                                aria-label="account"
                            >
                                <ManageAccountsIcon />
                            </IconButton>
                        </Tooltip>
                    </Toolbar>
                </AppBar>
                {/* left side drawer */}
                <Drawer variant="permanent" open={open}>
                    <DrawerHeader>{/* box on top of drawer/padding */}
                        {/* Close drawer button */}
                        <IconButton onClick={handleDrawerClose}>
                            {theme.direction === 'rtl' ? <ChevronRightIcon /> : <ChevronLeftIcon />}
                        </IconButton>
                    </DrawerHeader>
                    <Divider />
                    {/* inside the drawer */}
                    <List>
                        {/* Card Tree button */}
                        <ListItem disablePadding sx={{display: 'block'}}
                        >
                            <ListItemButton sx={[
                                        {
                                            minHeight: 48,
                                            px: 2.5,
                                        },
                                        open ? {justifyContent: 'initial',} : {justifyContent: 'center',},
                                    ]}
                                >
                                <ListItemIcon sx={[
                                        {
                                            minWidth: 0,
                                            justifyContent: 'center',
                                        },
                                        /* Adjust right margin when closed */
                                        open ? {mr: 3,} : {mr: 'auto',}, // BUG: Offset with new list item
                                    ]}>
                                    <AccountTreeIcon />
                                </ListItemIcon>
                                <ListItemText sx={[
                                            open ? {opacity: 1,} : {opacity: 0,},
                                        ]} >
                                        <CardTreeView gamestate={loadedGameState} ></CardTreeView>
                                </ListItemText>
                            </ListItemButton>
                        </ListItem>

                        <Divider />
                        
                        {/* Bookmarks button */}
                        <ListItem disablePadding sx={{display: 'block'}}
                        >
                            <ListItemButton sx={[
                                        {
                                            minHeight: 48,
                                            px: 2.5,
                                        },
                                        open ? {justifyContent: 'initial',} : {justifyContent: 'center',},
                                    ]}
                                >
                                <ListItemIcon sx={[
                                        {
                                            minWidth: 0,
                                            justifyContent: 'center',
                                        },
                                        /* Adjust right margin when closed */
                                        open ? {mr: 3,} : {mr: 'auto',},
                                    ]}>
                                    <BookmarksIcon />
                                </ListItemIcon>
                                <ListItemText
                                    primary="Bookmarks"
                                    sx={[
                                            /* Hide text when closed */
                                            open ? {opacity: 1,} : {opacity: 0,},
                                        ]}
                                />
                            </ListItemButton>
                        </ListItem>

                        <Divider />
                        
                        {/* Chat button */}
                        <ListItem disablePadding sx={{display: 'block'}}
                        >
                            <ListItemButton sx={[
                                        {
                                            minHeight: 48,
                                            px: 2.5,
                                        },
                                        open ? {justifyContent: 'initial',} : {justifyContent: 'center',},
                                    ]}
                                >
                                <ListItemIcon sx={[
                                        {
                                            minWidth: 0,
                                            justifyContent: 'center',
                                        },
                                        /* Adjust right margin when closed */
                                        open ? {mr: 3,} : {mr: 'auto',},
                                    ]}>
                                    <ChatIcon />
                                </ListItemIcon>
                                <ListItemText
                                    primary="Chat"
                                    sx={[
                                            /* Hide text when closed */
                                            open ? {opacity: 1,} : {opacity: 0,},
                                        ]}
                                />
                            </ListItemButton>
                        </ListItem>

                        <Divider />
                        
                        {/* Chat button */}
                        <ListItem disablePadding sx={{display: 'block'}}
                        >
                            <ListItemButton sx={[
                                        {
                                            minHeight: 48,
                                            px: 2.5,
                                        },
                                        open ? {justifyContent: 'initial',} : {justifyContent: 'center',},
                                    ]}
                                >
                                <ListItemIcon sx={[
                                        {
                                            minWidth: 0,
                                            justifyContent: 'center',
                                        },
                                        /* Adjust right margin when closed */
                                        open ? {mr: 3,} : {mr: 'auto',},
                                    ]}>
                                    <HistoryIcon />
                                </ListItemIcon>
                                <ListItemText
                                    primary="Card Play History"
                                    sx={[
                                            /* Hide text when closed */
                                            open ? {opacity: 1,} : {opacity: 0,},
                                        ]}
                                />
                            </ListItemButton>
                        </ListItem>
                        
                        {/* Built-in stuff */}
                        
                        {/*
                        {['Inbox', 'Starred', 'Send email', 'Drafts'].map((text, index) => (
                            <ListItem key={text} disablePadding sx={{ display: 'block' }}>
                                <ListItemButton
                                    sx={[
                                        {
                                            minHeight: 48,
                                            px: 2.5,
                                        },
                                        open ? {justifyContent: 'initial',} : {justifyContent: 'center',},
                                    ]}
                                >
                                    <ListItemIcon
                                        sx={[
                                            {
                                                minWidth: 0,
                                                justifyContent: 'center',
                                            },
                                            open ? {mr: 3,} : {mr: 'auto',},
                                        ]}
                                    >
                                        {index % 2 === 0 ? <InboxIcon /> : <MailIcon />}
                                    </ListItemIcon>
                                    <ListItemText
                                        primary={text}
                                        sx={[
                                            open ? {opacity: 1,} : {opacity: 0,},
                                        ]}
                                    />
                                </ListItemButton>
                            </ListItem>
                        ))}
                        */}
                    </List>
                    <Divider />

                    {/*
                    <List>
                        {['All mail', 'Trash', 'Spam'].map((text, index) => (
                            <ListItem key={text} disablePadding sx={{ display: 'block' }}>
                                <ListItemButton
                                    sx={[
                                        {
                                            minHeight: 48,
                                            px: 2.5,
                                        },
                                        open ? {justifyContent: 'initial',} : {justifyContent: 'center',},
                                    ]}
                                >
                                    <ListItemIcon
                                        sx={[
                                            {
                                                minWidth: 0,
                                                justifyContent: 'center',
                                            },
                                            open ? {mr: 3,} : {mr: 'auto',},
                                        ]}
                                    >
                                        {index % 2 === 0 ? <InboxIcon /> : <MailIcon />}
                                    </ListItemIcon>
                                    <ListItemText
                                        primary={text}
                                        sx={[
                                            open ? {opacity: 1,} : {opacity: 0,},
                                        ]}
                                    />
                                </ListItemButton>
                            </ListItem>
                        ))}
                    </List>
                    */}
                </Drawer>
                {/* content */}
                <Box component="main" sx={{ flexGrow: 1 }}>
                    <DrawerHeader />{/* Required for padding to make sure content doesn't slip below AppBar */}
                    <MainLayoutGrid gamestate={loadedGameState} />
                </Box>
            </Box>
        );
    }
    else {
        return(<Box></Box>);
    }
}
