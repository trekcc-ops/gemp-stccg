# GEMP STCCG Docker Setup
Welcome to the wonderful world of containerized installation!

Using Docker, all the fiddly setup and installation details can be coded into scripts so that people like you looking to set up an instance of the software don't have to worry about the database, server, Java installation, and all the hokey prerequisites that go along with it.  Just copy the files, open docker, run the docker-compose, and your instance is ready to be accessed.  


## Container Overview

The entry point is the docker-compose.yml YAML file, which defines two containers and all of their interfaces that are exposed to the outside world and to each other.  These files call gemp_app.Dockerfile and gemp_db.Dockerfile which are concerned with actually constructing the environments on these two containers. 

gemp_db is straightforward: it's a bare-bones linux instance using the official MariaDB docker image (MariaDB is a variant of MySQL).  It hosts the gemp database and doesn't do anything else.  

gemp_app is slightly more complicated.  Gemp is a Java server, is built using Maven, and utilizes Apache to serve http.  All of those have to be installed and available in the arrangement that Gemp expects, so this image starts with the Alpine Linux image (which includes Apache) and frankensteins the rest together to get both Maven and Java installed properly.

## Development Tools Needed/Recomended
* [Docker/Docker Desktop](https://www.docker.com/products/docker-desktop/) - required
* Java 18 - required
* [Maven 3.8.6](https://archive.apache.org/dist/maven/maven-3/3.8.6/) - required 
* [PortainerIO](https://www.portainer.io/)- recommended
* [DBeaver](https://dbeaver.io/) - optional, but you'll likely want something to manage your DB

## Installation Steps

1. Install [Docker](https://www.docker.com/products/docker-desktop/).
	* Windows Users: make sure that when you install Docker Desktop you select the option to use WSL2 instead of Hyper-V. This option will mimic a Linux environment
	* If you're installing this on Linux, I assume you know more than I do about how to set it up properly.
2. Install your container manager of choice.  I would HIGHLY recommend [PortainerIO](https://www.portainer.io/), which itself runs in a docker container and exposes itself as an interactable web page.  This will give you a graphical readout of all your currently running containers, registered images, networks, volumes, and just about anything else you might want, PLUS have interactive command lines for when the GUI just doesn't cut it.  The manager that comes with Docker Desktop by default is pretty much only just barely enough to run portainer with, so don't bother with it otherwise.
3. Pull the git repository down to your host machine; you may have already done this.
4. In the repository folder, copy the `env.example` file and paste it as `.env`.
5. Open a code editor of your choice and navigate to the repository folder.
6. Edit the `.env` file to suit your needs:
	* Note all the username/password fields.  If you are hosting this for something other than personal development, be sure to change all of these to something else.
7. Edit the `docker-compose.yml` file and change the defaults to suit your needs:
	1. Note all the relative paths under each volume/source: these are all paths on your host system.  If you want e.g. the database to be in a different location than what's listed, alter these relative paths to something else on your host system.
	2. Note the two "published" ports: 17001 for the app, and 35001 for the db.  These are the ports that you will be accessing the site with (and the db if you connect with a database manager). If you are hosting this for something other than personal development, consider changing these to something else.  **DO NOT** change the "target" ports, these targets are the ports that are used internally by Docker networking.
8. If you changed SQL credentials in the `.env` file, navigate to [gemp-stccg.properties](../gemp-stccg-common/src/main/resources/gemp-stccg.properties):
   1. **DO NOT CHANGE** the ports here.  These ports listed are the "target" ports in step 7.2, which you didn't edit because you followed the big "DO NOT" imperative.
   2. edit the db.connection.username and db.connection.password items to match the credentials you set in step 6.
   3. note the origin.allowed.pattern.  It is set to allow all connections, but if you are hosting this for something other than personal development, consider changing this to match your DNS hostname exactly.
9. Open a command line and navigate to gemp-stccg/gemp-module/docker. 
	* Run the command `docker-compose up -d`
	* You should see `Starting gemp_app....done` and `Starting gemp_db....done` at the end.  
	* This process will take a while the first time you do it, and will be near instantaneous every time after.
10. The database should have automatically created the gemp databases that are needed.  
	* You can verify this by connecting to the database on your host machine with your DB manager of choice (I recommend [DBeaver](https://dbeaver.io/)).  
	* It is exposed on localhost:35001 (unless you changed this port in step 7.2) and uses the user/pass of `gempuser`/`gemppassword` (unless you changed this in step 6).  
	* If you can see the `gemp_db` database with `league_participation` and other tables, you're golden.  
11. On your host machine cycle your docker container
	* In a terminal navigate to `gemp-module/docker`
	* Run `docker-compose down`
	* After that completes run `docker-compose up -d`	
12. If all has gone as planned, you should now be able to navigate to your own personal instance of Gemp.  
	* Open your browser of choice and navigate to http://localhost:17001/gemp-module/ .  (If you need a different port to be bound to, then repeat step 7 and edit the exposed port, then repeat step 11 to load those changes.)
13. If you're presented with the home page, register a new user and log in. It's possible for the login page to present but login itself to fail if configured incorrectly, so don't celebrate until you see the (empty) lobby.  If you get that far, then congrats, you now have a working local version of Gemp.

At this point, editing the code is a matter of changing the files on your local machine and re-running `docker-compose down` and `docker-compose up --build -d`.