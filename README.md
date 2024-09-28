# gemp-stccg
GEMP is a platform for running a rules-enforced CCG in a web-hosted fashion, so players can play in their browser.  This instance of GEMP is for the Star Trek CCG.

The engine is adaptable to other games, but essentially requires forking and ripping out all the Star Trek-based stuff and replacing it with context specific to the game of your choice.

# Overview

GEMP is a server written for Java 21, using MariaDB for a MySQL database instance, and serving raw Javascript and barebones HTML/CSS for the client front-end.

GEMP is divided into several modules which each handle a different aspect of the game:

## gemp-stccg-client
This module manages the interface for the Javascript clients.  Each JS operation that contacts the server will eventually terminate in a call to [communication.js](./gemp-module/gemp-stccg-client/src/main/web/js/gemp-022/communication.js), which is ultimately just a wrapper for calls to [RootUriRequestHandler](./gemp-module/gemp-stccg-server/src/main/java/com/gempukku/stccg/async/handler/RootUriRequestHandler.java), which routes each endpoint to the appropriate handler within gemp-stccg-server.  From there the action could go anywhere depending on context.

gemp-stccg-client is also the home for the web portion of the project; everything in [/src/main/web/](./gemp-module/gemp-stccg-client/src/main/web) is served as-is to the browser client, and the bulk of the Javascript can be found in [/js/gemp-022](./gemp-module/gemp-stccg-client/src/main/web/js/gemp-022).  /images contains icons, booster pack images, and other such resources.

## gemp-stccg-cards

This module contains the card definitions in HJSON format. [HJSON](https://hjson.github.io/) is a mutually-convertable dialect of JSON that includes support for comments and does not require redundant quotes in strings or commas in field definitions.  Consult [this list of editor extensions](https://hjson.github.io/users.html) to get syntax highlighting support for your editor of choice. Other data definitions besides cards are also stored in this module, including booster pack definitions, formats, sets, and sealed definitions.

## gemp-stccg-common

This module contains definitions used throughout the project, including enums for card types, affiliations, zones, and the like.  It also contains certain low-level classes for HTTP responses.

## gemp-stccg-logic

The home for classes used in executing game rules and also in generating card objects from JSON card definitions.

## gemp-stccg-server

The entry point for the server and the home for all database interaction, hall management, game running, chat, tournaments, and other services.  

This is also the home for the unit tests for the server, which will fail compilation if they do not pass: [/src/test/java/com/gempukku/stccg](gemp-module/gemp-stccg-server/src/test/java/com/gempukku/stccg).

# Hosting

GEMP includes tools for hosting within [Docker](https://docker-curriculum.com/), which makes setting up a local host a breeze (assuming the maintainers have kept the database scripts up-to-date).  See the readme in the [/docker](/gemp-module/docker) subfolder for more advanced details and instructions on the organization.

# Editing and Contributing

Several GEMP editors recommend editing the code in [IntelliJ IDEA](https://www.jetbrains.com/idea/), but you should be able to use any Java IDE.
