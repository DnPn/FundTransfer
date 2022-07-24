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
`/src/main/resources/application.properties`.



