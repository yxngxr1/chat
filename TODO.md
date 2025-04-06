# TODO List

---

### Back
- [x] Сделать DTO и Mapper для всех сущностей
- [x] Создать все контроллеры
  - [x] Чат
  - [x] Сообщения
  - [x] Пользователи
  - [x] Авторизация
- [x] Создать все сервисы
    - [x] Чат
    - [x] Сообщения
    - [x] Пользователи

- [x] Авторизация spring
    - [x] Генерация токена
    - [x] Проверка доступа к ресурсу

- [x] Задокументировать swagger

- [x] добавление owner'а чата
- [x] проверка прав доступа к операциям
  - [x] чаты
  - [x] пользователи
  - [x] сообщения

- [x] документировать авторизацию (через токены)
- [x] валидация
- [x] Реализовать WebSocket для чатов
- [x] сесурити на вебсокет
- [x] контроль доступа на вебсокете
- [x] перейти на maven

- [x] сборка на один порт
- [x] readme на maven и gradle
- [ ] проблема с .env (должен быть в общей и chat-backend модуле при сборке воедино)
- [x] докер
- [x] пагинация сообщений
- [x] лимит поиска людей (10)
- [x] тест на больших данных

---

### Front
- [x] создать базис SPA
- [x] реализовать все сервисы
- [x] все компоненты отображения
  - [x] список чатов
  - [x] список сообщений
  - [x] создание чата 
  - [x] создание сообщения
  - [x] удаление чата
  - [x] удаление сообщения
  - [x] редактирование чата
  - [x] редактирование сообщения
  - [x] добавление/удаление пользователя из чата
  - [x] профиль/редактирование
    
- [x] проверка работы с загрузкой
- [x] страницы авторизации
- [x] работа с сессией
- [x] Сгенерировать по swagger angular-client-service

- [x] Внедрить сгенерированные сервисы и dto
    - [x] auth
    - [x] user
    - [x] chat
    - [x] message

- [x] фикс сервисов
- [x] вывод всех ошибок
- [x] проблема с подписками на сообщение между диалогов
- [x] получение сообщения от нового чата (должен появится в списке)
- [ ] для телефона
- [ ] утечка памяти
---


### Спецификация API для мессенджера

#### **WebSocket (STOMP)**

- `/user/{userId}/queue/messages` — Подписка на новые сообщения у пользователя userId
- `/app/chat` — Отправка сообщения


# Что то как то отпимиzация sql
### Для включающего поиска по никам пользователей (надо прописать для быстрой работы в psql)
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_username_trgm ON app_user USING GIN (username gin_trgm_ops);

данные: app_users (1 миллион)

| Метод                      | Время в psql | Время API запроса |
|----------------------------|--------------|-------------------|
| **С индексом по`pg_trgm`** | ~0.9 мс      | ~20 мс            |
| **Без индекса**            | ~400 мс      | ~1 с              |


### Для полнотекстового поиска
ALTER TABLE app_user ADD COLUMN username_tsv tsvector;
UPDATE app_user SET username_tsv = to_tsvector('simple', username);
CREATE INDEX idx_username_tsv ON app_user USING GIN (username_tsv);

### Для быстрой выдачи сообщений по чату

### psql
index по chat_id и времени desc

запрос: explain analyze select * from message where chat_id=100002;
данные: message (69 965 001), chat (100 000)

| Метод                             | в чате < 100                                   | в чате < 200000           |
|-----------------------------------|------------------------------------------------|---------------------------|
| просто                            | ~10 с, Parallel Seq Scan                       | ~10 с, Parallel Seq Scan  |
| Индекс по chat_id (26с)           | first ~5мс, seconds ~0.5мс, Index Scan chat_id | ~30мс, Index Scan chat_id |
| Индекс по sent_at desc            |                                                | -                         |
| Индекс по sent_at asc             |                                                | -                         |
| Индекс по (chat_id, sent_at desc) | ~0.4 мс                                        | -                         | 

### api
- запрос: explain analyze SELECT * FROM message m WHERE m.chat_id = 100002 AND m.sent_at< to_timestamp(1743943408) ORDER BY m.sent_at DESC LIMIT 1000;
- чат на 200000к
- последние сообщения 1743943408
- первые сообщения 1714986286

| Метод                             | запрос на последние сообщения                                          | запрос на первые сообщения                                             |
|-----------------------------------|------------------------------------------------------------------------|------------------------------------------------------------------------|
| Индекс по sent_at desc            | 50 мс, Index Scan using message_chat_id, Sort Method: top-N heapsort   | 30 мс, Index Scan using message_chat_id, Sort Method: top-N heapsort   |
| Индекс по sent_at asc             | 30 мс, Index Scan using message_chat_id, Sort Method: top-N heapsort   | 40 мс, Index Scan using message_chat_id, Sort Method: top-N heapsort   |
| Индекс по (chat_id, sent_at desc) | ~1.2 мс, Index Scan using message_chat_id_sent_at (0.5 мс - limit 100) | ~1.2 мс, Index Scan using message_chat_id_sent_at (0.5 мс - limit 100) |
| Индекс по (chat_id, sent_at asc)  | ~1,5 мс, Index Scan Backward using message_chat_id_sent_at             | ~1.2 мс, Index Scan Backward using message_chat_id_sent_at             |

лучший варик
- create index message_chat_id_sent_at on message(chat_id, sent_at desc);