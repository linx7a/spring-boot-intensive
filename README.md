# spring-boot-intensive

Учебный проект: REST API системы бронирования комнат на Spring Boot. Код написан по ходу интенсива по Spring Boot от Sorokin School вместе с автором курса, чтобы отработать материал на практике.

Это не самостоятельный портфолио-проект. Самостоятельно выполненное задание по тому же курсу: [task-management-system-spring-boot](https://github.com/linx7a/task-management-system-spring-boot).

## Технологии

- Java 17
- Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA, Bean Validation)
- PostgreSQL
- Maven

## Что изучалось в проекте

- REST-контроллер и слой сервиса, модель и сущность JPA (`Reservation` / `ReservationEntity`) с преобразованием через `ReservationMapper`
- переход с хранения в памяти (`HashMap`) на PostgreSQL через Spring Data JPA
- валидация полей (Bean Validation) и централизованная обработка ошибок (`GlobalExceptionHandler`)
- бизнес-правила бронирования: статусы `PENDING`, `APPROVED`, `CANCELLED`, проверка пересечения дат запросом JPQL
- поиск с фильтрацией (`roomId`, `userId`) и пагинацией
- организация кода по функциональным модулям (package-by-feature)

## Эндпоинты

| Метод | URL | Описание |
|---|---|---|
| `GET` | `/reservation/{id}` | получить бронирование по id |
| `GET` | `/reservation` | поиск с фильтрами `roomId`, `userId` и пагинацией (`pageSize`, `pageNumber`) |
| `POST` | `/reservation` | создать бронирование |
| `PUT` | `/reservation/{id}` | обновить бронирование |
| `PATCH` | `/reservation/{id}/cancel` | отменить бронирование |
| `PATCH` | `/reservation/{id}/approve` | подтвердить бронирование |

## Как запустить

Требуется JDK 17+, Docker и Maven (или Maven Wrapper из проекта).

1. Запустить PostgreSQL на порту `5434` (как в `application.properties`):

   ```bash
   docker run --name reservation-postgres -e POSTGRES_PASSWORD=root -p 5434:5432 -d postgres:16
   ```

2. Задать пароль через переменную окружения `DB_PASSWORD` (значение совпадает с `POSTGRES_PASSWORD`):

   ```bash
   # Windows (cmd)
   set DB_PASSWORD=root

   # Linux / macOS
   export DB_PASSWORD=root
   ```

3. Запустить приложение: `./mvnw spring-boot:run` или класс `ReservationSystemApplication` из IDE.