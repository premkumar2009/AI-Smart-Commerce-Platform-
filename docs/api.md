# API surface

## Authentication

- `POST /api/auth/register` creates a customer account and returns a bearer token.
- `POST /api/auth/login` authenticates a customer or administrator.
- `POST /api/auth/logout` is a stateless client-side token discard endpoint.

Protected commerce routes will use `Authorization: Bearer <token>`. Validation failures use a consistent `{ timestamp, status, error, message, path }` response.
