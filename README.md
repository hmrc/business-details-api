Business Details API
========================
The business details API allows a developer to:

- list all businesses
- retrieve additional information for a user's business details

## Requirements

- Scala 2.13.x
- Java 11
- sbt 1.7.x
- [Service Manager V2](https://github.com/hmrc/sm2)

## Running the micro-service

Run from the console using: `sbt run` (starts on port 7792 by default)
Start the service manager profile: `sm --start MTDFB_SA`

## Running tests

```
sbt test
sbt it:test
```

## Viewing Open API Spec (OAS) docs

To view documentation locally ensure the Business Details API is running, and run api-documentation-frontend:
`./run_local_with_dependencies.sh`
Then go to http://localhost:9680/api-documentation/docs/openapi/preview and use this port and version:
`http://localhost:7792/api/conf/1.0/application.yaml`

## Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog)

## Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

## API Reference / Documentation

Available
at [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/business-details-api)

## License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")