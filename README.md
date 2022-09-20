[![Build Status][github-actions-badge]](https://github.com/spine-examples/kanban/actions/workflows/build.yml)
[![License][license-badge]](https://www.apache.org/licenses/LICENSE-2.0)

[github-actions-badge]: https://github.com/spine-examples/kanban/actions/workflows/build.yml/badge.svg
[license-badge]: https://img.shields.io/badge/License-Apache_2.0-blue.svg

# Kanban Board example application

This repository contains an example Spine project for working with a Kanban board.

## Run locally

### Prerequisites

- Have Docker Desktop [installed](https://docs.docker.com/desktop/);
- Have Java 1.8 installed.
- Have Node.js and `npm` [installed](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm);

### Steps

It is assumed that the terminal is opened in the project's root directory.

1) Generate Java and JS code from Protobuf definitions:
```Bash
./gradlew generateProto
./gradlew generateJsonParsers
```
2) Launch Firebase Database emulator on the `localhost:4000` and Firebase Emulator Suite UI on 
the `localhost:9999`:
```Bash
docker compose --file web/local-dev/firebase-emulator.yml up --detach
```
3) Launch the web server on the `localhost:8080`:
```Bash
./gradlew :web:server:appRun
```
Please wait till the following message shows up in the terminal:
```Bash
INFO   runs at:
INFO    http://localhost:8080/
```
4) Open another terminal in the `web/client-js` and install packages required for the client
application:
```Bash
npm install
```
5) Launch the client application on the `localhost:8081`:
```Bash
npm run serve
```

## Spine version

The Spine version used in the project is stored [here](buildSrc/src/main/kotlin/io/spine/examples/kanban/dependency/Spine.kt).

## Helpful materials

### Spine Vue client setup

Here is the [guide](spine-vue-client-setup.md) to set up a Spine Vue client.
