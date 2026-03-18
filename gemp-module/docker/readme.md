# Velara Docker Setup
Welcome to the wonderful world of containerized installation!

Using Docker, all the fiddly setup and installation details can be coded into scripts so that people like you looking to set up an instance of the software don't have to worry about the database, server, Java installation, and all the hokey prerequisites that go along with it.  Just copy the files, open docker, run the docker-compose, and your instance is ready to be accessed.  

## Container Overview

The entry point is the docker-compose.yml YAML file, which defines two containers and all of their interfaces that are exposed to the outside world and to each other.  These files call gemp_app.Dockerfile and gemp_db.Dockerfile which are concerned with actually constructing the environments on these two containers. 

gemp_db is straightforward: it's a bare-bones linux instance using the official MariaDB LTS docker image. It hosts the database and doesn't do anything else.

gemp_app is slightly more complicated. Velara is a Java logic engine and http server with a Javascript front-end. These are automatically built and installed for you as part of the Docker Compose build process.

## Prerequisites
* Required:
  - [Docker/Docker Desktop](https://www.docker.com/products/docker-desktop/)
  - A Git client
    - [Git](https://git-scm.com/)
    - [GitHub Desktop](https://desktop.github.com/download/)
* Optional:
  - [DBeaver](https://dbeaver.io/) - optional, but you will need something to manage your DB

## Installation Steps

1. Install Docker.
	* Windows Users:
      - Visit https://docs.docker.com/desktop/setup/install/windows-install/
      - Select the WSL 2 backend.
    * Linux Users:
      - Visit https://docs.docker.com/desktop/setup/install/linux/
2. Install Git.
3. Use Git to pull the repository down to your host machine; you may have already done this.
	* Open a command line window and navigate to the folder that you want to put Velara in.
	* Run the following command: 'git clone https://github.com/trekcc-ops/gemp-stccg.git'
4. In the repository folder, copy the `env.example` file and paste it as `.env`.
5. Edit the `.env` file to suit your preferred ports and internal Docker network IP addresses.
6. In the repository folder, copy the `initial_player_accounts.example` file and paste it as `initial_player_accounts.json`.
7. Edit the `initial_player_accounts.json` file to add users to the system. See **Initial User Setup** below for more details.
8. In the main gemp-stccg folder, create the following text files for user/password info. Each file should contain only a single string (no quotes).

    | Filename                    | Contents                 |
    |-----------------------------|--------------------------|
    | mariadb_root_password.txt   | Database root password   |
    | mariadb_client_username.txt | Database client username |
    | mariadb_client_password.txt | Database client password |

9. Open a command line and navigate to the main gemp-stccg folder. 
   * Run the command `docker-compose up --build -d`
   * You should see `Starting gemp_app....done` and `Starting gemp_db....done` at the end.  
   * This process will take a while the first time you do it, and will be near instantaneous every time after. 
10. The database should have automatically created the tables that are needed.
   * You can verify this by connecting to the database on your host machine with your DB manager of choice (I recommend [DBeaver](https://dbeaver.io/)).  
   * It is exposed on localhost:35001 (unless modified in .env) and can be accessed with the credentials you specified in Step 7.
   * If you can see the `gemp_db` database with `league_participation` and other tables, you're golden.
11. If all has gone as planned, you should now be able to navigate to your own personal instance of Velara.  
   * Open your browser of choice and navigate to http://localhost:17001.
12. If you're presented with the home page, register a new user and log in. If you can log in to the game hall, congrats, you now have a working local version of Velara.

At this point, editing the code is a matter of changing the files on your local machine and re-running `docker-compose down` and `docker-compose up --build -d`.

## Initial User Setup ##

When the `gemp_app` container is started up, it queries the database to see if any player accounts exist. If there are none, it will create player accounts that line up with the properties specified in `gemp-stccg/initial_player_accounts.json`.

If you make manual adjustments to this file, it is recommended to include at least one admin account (`isAdmin` = true). You must also include the `Librarian` account to manage the Velara deck library. If `Librarian` is not included, the server will throw a runtime error and be unable to start. If any players in this file have empty strings as passwords, that password can be set by registering as that player name in the browser.