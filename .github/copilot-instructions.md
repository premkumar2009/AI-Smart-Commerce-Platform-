# ShopSense AI workspace

- Frontend: React, TypeScript, Vite. Run `npm run build` and `npm run lint` from the workspace root.
- Backend: Java 21, Spring Boot, Maven. Run `mvn -q -DskipTests package` from `backend/`.
- Keep secrets in environment variables. Never commit `.env` or API keys.
- Preserve the layered backend boundary: controller -> service -> repository; use DTOs at the API edge.
- Keep customer-facing interactions responsive and accessible across mobile and desktop.
