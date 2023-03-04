# Fund Transfer

## Description

Pet project with Spring Boot modelling a fund transfers between 2 accounts:

- accounts are persisted in an in-memory H2 database (see `/account/accessor/impl`).
- the exchange rates can either be retrieved from an in-memory H2 database or from an external API (see the section
  `Running the application` > `Currency conversion` of this documentation).

---

## Building the application

| Command                            | Unit tests | Integration tests |
|------------------------------------|------------|-------------------|
| `./mvnw clean install`             | Y          | Y                 |
| `./mvnw clean install -DskipITs`   | Y          | N                 |
| `./mvnw clean install -DskipTests` | N          | N                 |

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

### From Docker

1. Build the image: `docker build -t dnpn/fundtransfer .`
2. Run the container: `docker run -p 8080:8080 --name fundtransfer dnpn/fundtransfer`

To stop the container you can either:
* press `Ctrl + C` from the terminal attached to the running container
* from another terminal run `docker stop fundtransfer`

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
`/resources/application-dev.properties`.

### Profiles

The Spring profile is equal to the Maven property `spring.profiles.active` set in the `pom.xml`.

By default, the selected profile is `dev` and implies the following:

- mock accounts are created at startup (see the class `SqlAccountDevSetup`)
- mock exchange rates to USD are created at startup if the SQL implementation for the currency conversion is chosen
  (see the class `SqlExchangeRateDevSetup`)

### Swagger

Once the application is started you can access the Swagger (OpenAPI version 3) via the following URLs:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Swagger JSON**: http://localhost:8080/v3/api-docs

### Monetary representations

All the money amounts are represented with 2 decimals, this means that in certain cases rounding needs to be performed:

- if the given amount to round is part of a request then it is rounded half-up (example: if a transfer of `123.456 USD`
  is requested, the executed transfer will actually be of `123.46 USD`).
- if the given amount to round is to be credited to the customer then it is rounded down (example: if the currency
  conversion returns `123.456 USD`, then the customer will be credited of `123.45 USD`). This means that the
  customer can lose up to 1 cent (excluded) of their own currency when receiving a transfer that required a currency
  conversion.

### Currency conversion

The exchange rate of currencies implementation can be chosen by setting the value of `currencyConversion.mode` in
the file `application-dev.properties`:

- if set to `sql`, the exchange rates to a reference currency (USD) are persisted in an in-memory H2 database (see
  `/currency/service/impl/sql`)
- if set to `api`, the currency conversion is retrieved from the `exchangerates` API (see https://exchangeratesapi.io/).

#### API

If you decide to use the API then you have to set the property `currencyConversion.api.key` in the file `application.
properties`.

You can get an API key
by [subscribing to the `exchangerates` API](https://apilayer.com/marketplace/exchangerates_data-api#pricing).
For testing purpose you can use the free tier and retrieve your API key
in [your account settings](https://apilayer.com/account).

---

## Examples

Here are example of queries executed with the `dev` profile enabled just after the startup of the application:

1. list the accounts:

- run `curl -L -X GET 'http://localhost:8080/accounts'`

2. check a given account:

- run `curl -L -X GET 'http://localhost:8080/account/123'`

3. try to make a transfer to a non-existing account:

- no account is register with the ID `999`
- run `curl -L -X POST 'http://localhost:8080/transfer' -H 'Content-Type: application/json' --data-raw '
  {"fromAccount": 123, "toAccount": 999, "amount": 587.21}'`

4. try to make a transfer between accounts with an unsupported currency (*you cannot test this scenario if you are
   using the API implementation since the API supports the `JPY` currency*):

- the exchange rate of `JPY` is not registered
- the account `789` uses the currency `JPY`
- run `curl -L -X POST 'http://localhost:8080/transfer' -H 'Content-Type: application/json' --data-raw '
  {"fromAccount": 123, "toAccount": 789, "amount": 587.21}'`
- ***Note**: you cannot test this scenario if you are using the API implementation since the API supports the `JPY`
  currency*

5. try to make a transfer between accounts with the same currency (even if the currency is not known by the exchange
   rate system):

- the exchange rate of `JPY` is not registered
- but the accounts `789` and `101` both use the currency `JPY`, therefore no currency conversion is needed
- run `curl -L -X POST 'http://localhost:8080/transfer' -H 'Content-Type: application/json' --data-raw '
  {"fromAccount": 101, "toAccount": 789, "amount": 587.21}'`

6. try to make a transfer with an amount greater than the balance of the debit account:

- the amount `1500` is greater than the initial balance of the account `123`
- run `curl -L -X POST 'http://localhost:8080/transfer' -H 'Content-Type: application/json' --data-raw '
  {"fromAccount": 123, "toAccount": 456, "amount": 1500}'`

7. make a valid transfer:

- the amount `587.21` is less than the initial balance of the account `123`
- the accounts `123` and `456` have their currencies registered in the dev database
- run `curl -L -X POST 'http://localhost:8080/transfer' -H 'Content-Type: application/json' --data-raw '
  {"fromAccount": 123, "toAccount": 456, "amount": 587.21}'`

8. try to make a transfer with an invalid amount:

- run `curl -L -X POST 'http://localhost:8080/transfer' -H 'Content-Type: application/json' --data-raw '
  {"fromAccount": 123, "toAccount": 456, "amount": -587.21}'`
- run `curl -L -X POST 'http://localhost:8080/transfer' -H 'Content-Type: application/json' --data-raw '
  {"fromAccount": 123, "toAccount": 456, "amount": 0}'`
- run `curl -L -X POST 'http://localhost:8080/transfer' -H 'Content-Type: application/json' --data-raw '
  {"fromAccount": 123, "toAccount": 456, "amount": "abc"}'`

9. try to make a transfer within the same account:

- run `curl -L -X POST 'http://localhost:8080/transfer' -H 'Content-Type: application/json' --data-raw '
  {"fromAccount": 123, "toAccount": 123, "amount": 587.21}'`

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
