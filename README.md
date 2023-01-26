## Deploy

Requirements:

- JDK 17
- Docker, Docker Compose

Run `./deploy.sh` file in this directory. Sample user with ID `72a0cfd0-8f38-4939-8100-72332f65b91f` and balance `100` will be created in the system.

Atm-service Swagger docs will be available at http://localhost:8081/swagger-ui/index.html.

Exposed ports:

- atm-service: 8081
- postgresql: 15432
- kafka: 9092

Run tests:

- `./atm-service/gradlew clean test -p ./atm-service`
- `./audit-service/gradlew clean test -p ./audit-service`

## atm-service:

API for letting users withdraw and deposit money.

Env variables:

| Name                                 | Description                                                |
|--------------------------------------|------------------------------------------------------------|
| JDBC_URL                             | URL to connect to DB                                       |
| JDBC_USERNAME                        | Username to access DB                                      |
| JDBC_PASSWORD                        | Password to access DB                                      |
| KAFKA_BOOTSTRAP_SERVERS              | Host and port to establish initial connection to the Kafka |
| NOTIFICATION_TRANSACTION_ADDED_TOPIC | Topic name for transaction added events                    |
| NOTIFICATION_BATCH_SIZE              | How many events to send to the topic in scheduled task     |
| NOTIFICATION_SEND_CRON               | How often send events to the topic                         |

Example values for environment variables can be found in `docker-compose.yml` file.

## audit-service:

Service for preserving history about user transactions.

Env variables:

| Name                                    | Description                                                |
|-----------------------------------------|------------------------------------------------------------|
| JDBC_URL                                | URL to connect to DB                                       |
| JDBC_USERNAME                           | Username to access DB                                      |
| JDBC_PASSWORD                           | Password to access DB                                      |
| KAFKA_BOOTSTRAP_SERVERS                 | Host and port to establish initial connection to the Kafka |
| SERVICE_ACCOUNT_TRANSACTION_ADDED_TOPIC | Topic name for transaction added events                    |

Example values for environment variables can be found in `docker-compose.yml` file.