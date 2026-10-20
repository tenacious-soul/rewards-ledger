# Rewards Points Ledger

A small Spring Boot service for a rewards points program. Members earn points (purchases, referrals, cashback) and spend them (redemptions). Every change is stored as a ledger entry, and a member's balance is the sum of their entries.

## Tech stack

- Java 17, Spring Boot 3.3
- Spring Web, Spring Data JPA, Bean Validation
- H2 (in-memory) with Flyway migrations
- JUnit 5, Mockito, MockMvc

## Getting started
```bash
docker compose up --build
```

Data is kept in memory, so restarting the container starts from an empty database.

### Postman

The `postman/Rewards Ledger` folder is a Postman collection in the v3 YAML format. Open it from Postman's Local Mode by selecting that folder. It has every endpoint, the example from the brief and the error cases. Run the requests from top to bottom, or run the whole collection with the Collection Runner. Creating the member saves its id, and later requests reuse it.

## API

### Point types

| `point_type_id` | Type | Direction |
|---|---|---|
| 1 | Purchase Earning | Credit (+) |
| 2 | Referral Bonus | Credit (+) |
| 3 | Cashback | Credit (+) |
| 4 | Redemption | Debit (−) |

Clients always send `points` as a positive number. The service stores it as negative when the type is a debit.

### `POST /members` — create a member

```bash
curl -i -X POST localhost:8080/members \
  -H 'Content-Type: application/json' \
  -d '{"name": "Alice Johnson", "email": "alice@example.com"}'
```

`201 Created`

```json
{
  "member_id": 1,
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "created_at": "2024-01-15T09:00:00Z"
}
```

### `GET /members/{memberId}` — member with current balance

```bash
curl localhost:8080/members/1
```

`200 OK`

```json
{
  "member_id": 1,
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "points_balance": 450,
  "created_at": "2024-01-15T09:00:00Z"
}
```

### `POST /rewards` — add a ledger entry

```bash
curl -i -X POST localhost:8080/rewards \
  -H 'Content-Type: application/json' \
  -d '{"member_id": 1, "point_type_id": 1, "points": 500, "description": "Purchase at Store A"}'
```

`201 Created`

```json
{
  "reward_id": 1,
  "member_id": 1,
  "point_type_id": 1,
  "points": 500,
  "description": "Purchase at Store A",
  "event_date": "2024-02-01T14:22:10Z"
}
```

`description` is optional. We can also send `event_date`. If we leave it out, the current time is used.

### `GET /members/{memberId}/rewards` — list a member's entries

```bash
curl localhost:8080/members/1/rewards
```

`200 OK` returns an array of reward entries, oldest first.

## Errors

| Situation | Status |
|---|---|
| Missing or invalid fields, `points` ≤ 0, malformed JSON | `400 Bad Request` |
| `point_type_id` outside 1–4 | `400 Bad Request` |
| Member does not exist | `404 Not Found` |
| Email already registered (case-insensitive) | `409 Conflict` |
| Redemption larger than the available balance | `422 Unprocessable Entity` |

## Design notes

- **The ledger is append-only.** The balance is not stored anywhere. It is always calculated as `SUM(points)` over a member's entries, so it can never drift from the history.
- **The sign comes from the point type.** `PointType` is an enum that knows its id and its direction (credit or debit). Adding a new type means adding one enum constant.
- **Redemptions are safe under concurrency.** Creating a reward takes a row lock on the member (`SELECT ... FOR UPDATE`) before it checks the balance. Two redemptions running at the same time can't both pass the check and overdraw the account.
- **Emails are normalized.** They are trimmed and lowercased before they are saved. The database also has a unique constraint as a backstop.
- **Time comes from an injectable `Clock`.** This keeps timestamps deterministic in tests. Timestamps are truncated to whole seconds to match the API examples.
- **The schema is owned by Flyway** (`src/main/resources/db/migration`). Hibernate only validates it.
- **Data lives in memory.** H2 keeps the setup to zero dependencies, so data is lost on restart. To switch to PostgreSQL, add the driver and change the `spring.datasource.*` settings. The migration is plain SQL.
