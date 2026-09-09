# Architecture

```mermaid
flowchart TD
  Browser[React + Vite storefront] --> Api[Spring Boot REST API]
  Api --> Service[Service layer]
  Service --> JPA[Spring Data JPA]
  JPA --> DB[(PostgreSQL)]
  Service --> Gemini[Gemini via server-side AI services]
```

The current Phase 1 slice establishes the frontend shell and the authentication boundary. All credentials remain server-side; the frontend will call `/api` endpoints through a typed service layer as commerce features are added.
