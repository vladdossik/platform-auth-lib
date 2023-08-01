# platform-auth-lib

## Описание
Библиотека для авторизации пользователя

## Инструкция для подключения
Добавить в pom.xml проекта следующие строчки. Вместо Tag добавить версию последнего релиза
``` xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
``` xml
<dependency>
    <groupId>com.github.vladdossik</groupId>
    <artifactId>platform-auth-lib</artifactId>
    <version>Tag</version>
</dependency>
```

В application.yml добавить следующие настройки. Вместо AUTH_SERVICE_URL указать URL сервиса auth
``` yml
auth-lib:
  web:
    auth:
      baseUrl: AUTH_SERVICE_URL
      responseTimeout: 1000
      maxBodySizeForLog: 512
```
Затем в контроллерах создать
``` java
private final ApiClient authClient;
```
И внутри методов вызвать, предварительно пробросив RequestHeader Authorization. Вместо ROLE указать нужную роль 
``` java
@GetMapping("/something")
public String getSomething(
        @RequestHeader (name="Authorization") String token
) throws ApiClientException {
    authClient.checkAccessByTokenAndRole(ROLE, token);
    return "something";
}
```