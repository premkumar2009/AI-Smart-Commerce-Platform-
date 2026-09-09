# ShopSense AI

ShopSense AI is a thoughtful e-commerce experience that turns natural-language intent into confident product discovery. This repository contains the Phase 1 foundation: a polished responsive React storefront, a Spring Boot authentication API, PostgreSQL wiring, Docker services, and architecture documentation.

## Product direction

- AI-first discovery with natural language prompts
- Curated product browsing and recommendation surfaces
- Responsive, accessible customer experience
- Secure backend boundary ready for catalog, cart, orders, and admin features

## Stack

React + TypeScript + Vite, Spring Boot 3, Java 21, Spring Security, JWT, BCrypt, Spring Data JPA, PostgreSQL, Maven, Docker Compose.

## Run locally

1. Copy `.env.example` to `.env` and replace secrets.
2. Start PostgreSQL with `docker compose up postgres -d`, or run the complete stack with `docker compose up --build`.
3. Run the frontend with `npm run dev`.
4. Run the backend from `backend/` with `mvn spring-boot:run`.
5. Open `http://localhost:5173` for the storefront and `http://localhost:8080/swagger-ui/index.html` for API docs.

## Verification

- Frontend build: `npm run build`
- Frontend lint: `npm run lint`
- Backend package: `cd backend && mvn -q -DskipTests package`

See [docs/architecture.md](docs/architecture.md) and [docs/api.md](docs/api.md) for current contracts. Gemini keys belong only in backend environment variables and are intentionally not used by browser code.
