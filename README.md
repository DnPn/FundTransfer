# Fund Transfer

## Description

Pet project with Spring Boot modelling a fund transfers between 2 accounts:

- accounts are persisted in an in-memory H2 database (see `/account/impl`)

---

## Running the application

### From IntelliJ Idea

1. Open the project in IntelliJ
2. Run `FundTransferApplication`

### From the terminal (Linux)

1. Verify that `JAVA_HOME` is set and references a JDK with a version greater or equal to 17
    1. To verify the current value: `echo $JAVA_HOME`
    2. To set the JDK path: `export JAVA_HOME=/path/to/jdk-17-or-more/`
2. Run `./mvnw clean spring-boot:run`

### Health check

To verify that the application is running you can execute the following cURL command:

```bash
curl -i http://localhost:8080/ping
```

You should get in return a response similar to

```text
HTTP/1.1 204 
Date: Sun, 24 Jul 2022 13:00:57 GMT
```

If it is not the case verify in the console where you launched the application that there is no exception. Also you
can verify that you are querying the right port by verifying the parameter `server.port` in
`/resources/application.properties`.

### Profiles

If the Spring profile is set to `dev` (see `spring.profiles.active` in `/resources/application.properties`):

- mock accounts are created at startup (see the class `SqlAccountDevSetup`)

### Swagger

Once the application is started you can access the Swagger (OpenAPI version 3) via the following URLs:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Swagger JSON**: http://localhost:8080/v3/api-docs

---

## Troubleshooting

### Identifying a request

Requests can be tracked with their unique ID. This ID is set in MDC (see https://www.slf4j.org/api/org/slf4j/MDC.
html), so the logs of the same request can be easily retrieved.

In the example below we can see in the logs that the request ID is `c05a8315-5475-40ad-82e2-f7ddd9c2a58b`:

```text
2022-07-24T21:40:19,567 INFO  [http-nio-8080-exec-2] [c05a8315-5475-40ad-82e2-f7ddd9c2a58b] i.d.f.t.a.TransferController: Transfer request: TransferApiRequest[fromAccount=123, toAccount=789, amount=587.21]
2022-07-24T21:40:19,576 DEBUG [http-nio-8080-exec-2] [c05a8315-5475-40ad-82e2-f7ddd9c2a58b] i.d.f.t.s.TransferService: Transfer amount 587.21 is valid.
2022-07-24T21:40:19,580 DEBUG [http-nio-8080-exec-2] [c05a8315-5475-40ad-82e2-f7ddd9c2a58b] i.d.f.a.a.i.SqlAccountAccessor: Get account 123
2022-07-24T21:40:19,583 DEBUG [http-nio-8080-exec-2] [c05a8315-5475-40ad-82e2-f7ddd9c2a58b] i.d.f.t.s.TransferService: The balance of the debit account is sufficient for the transfer.
2022-07-24T21:40:19,583 DEBUG [http-nio-8080-exec-2] [c05a8315-5475-40ad-82e2-f7ddd9c2a58b] i.d.f.a.a.i.SqlAccountAccessor: Get account 789
2022-07-24T21:40:19,584 DEBUG [http-nio-8080-exec-2] [c05a8315-5475-40ad-82e2-f7ddd9c2a58b] i.d.f.a.a.i.SqlAccountAccessor: Persist account data: Account(accountId=123, currency=EUR, balance=647.35)
2022-07-24T21:40:19,586 DEBUG [http-nio-8080-exec-2] [c05a8315-5475-40ad-82e2-f7ddd9c2a58b] i.d.f.a.a.i.SqlAccountAccessor: Persist account data: Account(accountId=789, currency=JPY, balance=1000001.00)
2022-07-24T21:40:19,586 DEBUG [http-nio-8080-exec-2] [c05a8315-5475-40ad-82e2-f7ddd9c2a58b] i.d.f.t.s.TransferService: Transfer completed: 587.21 EUR debited from the account 123 | 1 JPY credited to the account 789
```

This request ID is retrieved from the request header `Request-Id` if existing, otherwise it is generated. It is then
set in the same header in the response. 



