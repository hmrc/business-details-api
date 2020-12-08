Business Details API
========================

## Description 
The business details API allows a developer to:
- list all businesses
- retrieve additional information for a user's business details

## requirements
- Scala 2.12.x
- Java 8
- sbt 1.3.7
- [Service Manager](https://github.com/hmrc/service-manager)         
      
## running the micro-service
Run from the console using: `sbt run` (starts on port 7792 by default)

Start the service manager profile: `sm --start MTDFB_SA`
 
## Run tests
```
sbt test
sbt it:test
```

## To view the RAML
To view documentation locally ensure the Business Details API is running, and run api-documentation-frontend:
`./run_local_with_dependencies.sh`

Then go to http://localhost:9680/api-documentation/docs/api/preview and use this port and version:
`http://localhost:7792/api/conf/1.0/application.raml`



## Reporting Issues
You can create a GitHub issue [here](https://github.com/hmrc/business-details-api/issues)

## API Reference / Documentation 
Available on the [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/business-details-api/1.0)

## License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
