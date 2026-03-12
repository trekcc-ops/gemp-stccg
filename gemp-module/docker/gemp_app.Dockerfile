#
# Stage 0, Maven and dependency cache
#
FROM maven AS server-build

# Make and switch to a /gemp-module folder.
RUN mkdir -p /gemp-module/
WORKDIR /gemp-module/

# Load the pom files and tell Maven to cache their dependencies.
# Keep these in order as defined in the <modules> section of the main pom.xml file.
COPY gemp-module/pom.xml .
COPY gemp-module/gemp-stccg-server/pom.xml ./gemp-stccg-server/pom.xml
COPY gemp-module/gemp-stccg-logic/pom.xml ./gemp-stccg-logic/pom.xml
COPY gemp-module/gemp-stccg-common/pom.xml ./gemp-stccg-common/pom.xml
COPY gemp-module/gemp-stccg-cards/pom.xml ./gemp-stccg-cards/pom.xml

RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline

# Copy in the rest of the source files and actually run the build.
COPY gemp-module/ .
RUN --mount=type=cache,target=/root/.m2 mvn install -DskipTests


#
# Stage 1, Client build
#
FROM node:22-alpine AS client-build

# Make and switch to a /gemp-client folder
RUN mkdir -p /gemp-client
WORKDIR /gemp-client

# Load the package.json and lock files and tell NPM to cache them
COPY gemp-module/gemp-stccg-client/package.json .
COPY gemp-module/gemp-stccg-client/package-lock.json .

RUN --mount=type=cache,target=/root/.npm npm install

# Copy in and build the client

COPY gemp-module/gemp-stccg-client/babel.config.json .
COPY gemp-module/gemp-stccg-client/src ./src

RUN --mount=type=cache,target=/root/.npm npm run build


#
# Stage 2, new container with just our build files
#
FROM amazoncorretto:21-alpine-jdk AS runtime

RUN mkdir -p /src/gemp-module/gemp-stccg-client/target/
COPY --from=server-build /gemp-module/gemp-stccg-cards/src/main/resources /etc/gemp-module/gemp-stccg-cards/src/main/resources
COPY --from=server-build /gemp-module/gemp-stccg-server/src/main/resources/log4j2.xml /src/gemp-module/gemp-stccg-client/target/log4j2.xml
COPY --from=server-build /gemp-module/gemp-stccg-server/target/gemp-stccg-server-jar-with-dependencies.jar /src/gemp-module/gemp-stccg-client/target/web.jar
COPY --from=client-build /gemp-client/src/main/web/dist /src/gemp-module/web
COPY --from=client-build /gemp-client/src/main/web/includes /src/gemp-module/web/includes