## Overview

Service consists of 3 AWS lambdas:
  - rates-api-refresh:
    Triggered by ApiGateway to submit a message to SQS queue that will be processed by `refresh-rates` lambda.
  - rates-api-get:
    Frontend uses this lambda via ApiGateway
  - refresh-rates:
    Reads from SQS queue and stores data in Redis if required

## Deploy

Note: It is assumed that Redis host is already created and is up and running.

1. Install and setup [Serverless](https://www.serverless.com/framework/docs/providers/aws/guide/installation/), Java, Maven.
2. Build project `mvn clean install`
3. Create resources in AWS via `sls deploy --stage=prod --region=us-east-1`
