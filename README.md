# idempiere-micro-session-core
iDempiere Session Microservice Core

[![CircleCI](https://circleci.com/gh/iDempiere-micro/idempiere-micro-session-core.svg?style=svg)](https://circleci.com/gh/iDempiere-micro/idempiere-micro-session-core)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3fbfd5724c934536bf08aebd80ab00f0)](https://www.codacy.com/app/davidpodhola/idempiere-micro-session-core?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=iDempiere-micro/idempiere-micro-session-core&amp;utm_campaign=Badge_Grade)
[![Maintainability](https://api.codeclimate.com/v1/badges/178ddf4a42b0bfbf55dc/maintainability)](https://codeclimate.com/github/iDempiere-micro/idempiere-micro-session-core/maintainability)
[![JitPack](https://jitpack.io/v/iDempiere-micro/idempiere-micro-session-core.svg)](https://jitpack.io/#iDempiere-micro/idempiere-micro-session-core)

The repository for iDempiere Kotlin back-end Session microservice compatible with iDempiere.
You can use it to:

-   login a user and obtain a JWT token
-   use the JWT token with GraphQL calls to [iDempiere Micro Spring backend](https://github.com/iDempiere-micro/idempiere-micro-spring) or [iDempire Micro OSGi backend](https://github.com/iDempiere-micro/idempiere-micro) (later).

## Installation
From [JitPack](https://jitpack.io/v/iDempiere-micro/idempiere-micro-session-core).

## Usage
Needs [HikariCP to be configured](https://github.com/seratch/kotliquery#hikaricp) by the module that is using idempiere-micro-session-core before the first call.
