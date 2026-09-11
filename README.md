# ShopSense AI

ShopSense AI is a recruiter-ready AI commerce platform with a live 12-category catalog, dependent filters, grounded product discovery, cart, wishlist, checkout, orders, reviews, and admin workflows.

## Product direction

- AI-first discovery with natural language prompts
- Curated product browsing and recommendation surfaces
- Responsive, accessible customer experience
- Cascading category, product type, and brand filters sourced from real products
- Grounded AI search, assistant, recommendations, similar products, comparison, and budget bundles
- JWT authentication with customer and admin roles

## Stack

React + TypeScript + Vite, Spring Boot 3, Java 21, Spring Security, JWT, BCrypt, Spring Data JPA, PostgreSQL, Maven, Docker Compose.

## Run locally

1. Copy `.env.example` to `.env` and replace secrets.
2. Start PostgreSQL with `docker compose up postgres -d`, or run the complete stack with `docker compose up --build`.
3. Run the frontend with `npm run dev`.
4. Run the backend from `backend/` with `mvn spring-boot:run`.
5. Open `http://localhost:5173` for the storefront and `http://localhost:8080/swagger-ui/index.html` for API docs.

## Catalog

The seed contains exactly 12 canonical categories, 10 product types per category, and at least three real catalog products per type. Product type previews use three distinct stored product images. Brands are derived from the selected category and type.

## AI examples

- `best headphones under 10000`
- `Samsung smartphone under 30000`
- `dog food for puppies`
- `laptop for coding under 70000`

AI responses are grounded in database product records and return the stored product image and link.

## Environment

Use `.env.example` for `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`, `FRONTEND_URL`, `GEMINI_API_KEY`, and `VITE_DEMO_MODE`. Set `VITE_DEMO_MODE=false` for production deployments.

## Verification

- Frontend build: `npm run build`
- Frontend lint: `npm run lint`
- Backend tests: `cd backend && mvn clean test`
- Backend package: `cd backend && mvn clean package`
- Docker: `docker compose build && docker compose up`

See [docs/architecture.md](docs/architecture.md) and [docs/api.md](docs/api.md) for current contracts. Gemini keys belong only in backend environment variables and are intentionally not used by browser code.
