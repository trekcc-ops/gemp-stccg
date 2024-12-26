#
# Stage 0, Maven and dependency cache
#
FROM maven AS build

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
COPY gemp-module/gemp-stccg-client/pom.xml ./gemp-stccg-client/pom.xml

RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline

# Copy in the rest of the source files and actually run the build.
COPY gemp-module/ .
RUN --mount=type=cache,target=/root/.m2 mvn install -DskipTests


#
# Stage 1, new container with just our build file
#
FROM amazoncorretto:21-alpine-jdk AS runtime

# Patch any runtime security holes
RUN apk update
RUN apk upgrade

RUN mkdir -p /src/gemp-module/gemp-stccg-client/target/
COPY --from=build /gemp-module/gemp-stccg-server/src/main/resources/log4j2.xml /src/gemp-module/gemp-stccg-client/target/log4j2.xml
COPY --from=build /gemp-module/gemp-stccg-client/target/web.jar /src/gemp-module/gemp-stccg-client/target/web.jar
