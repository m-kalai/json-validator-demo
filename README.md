# JSON Validator Demo

Simple demo application used to practice and showcase some of ZIO tools.
Application provides REST service for validation of JSON documents.

## Endpoints
```
POST    /schema/SCHEMAID        - Upload a JSON Schema with unique `SCHEMAID`
GET     /schema/SCHEMAID        - Download a JSON Schema with unique `SCHEMAID`

POST    /validate/SCHEMAID      - Validate a JSON document against the JSON Schema identified by `SCHEMAID`
```

## How to run
Application can be run by executing `jvd.Main` either from your favourite IDE or from SBT.

Example:
```shell
sbt run
```
or
```shell
sbt runMain jvd.Main

```

## Requirements
* sbt
* JDK
