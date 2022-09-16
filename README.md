[![Build Status][github-actions-badge]](https://github.com/spine-examples/kanban/actions/workflows/build.yml)
[![License][license-badge]](https://www.apache.org/licenses/LICENSE-2.0)

[github-actions-badge]: https://github.com/spine-examples/kanban/actions/workflows/build.yml/badge.svg
[license-badge]: https://img.shields.io/badge/License-Apache_2.0-blue.svg

# Kanban Board example

This example demonstrates:

* Multiple related Aggregate.
* Orchestrating Aggregates using Process Managers.
* Building a Projection which is subscribed to both events and entity updates.

# Client

This project contains [a Vue.js client](web/client-js). The client was created following 
the [guide](spine-vue-client-setup.md).

# Spine version

The Spine version used in the project is stored[here](buildSrc/src/main/kotlin/io/spine/examples/kanban/dependency/Spine.kt).
