Bike Auction Platform
A motorcycle auction web application built with an Angular frontend, Spring Boot REST API, and PostgreSQL.
Links
- Live frontend: https://bike-auction-platform-1-lwq6.onrender.com
- Backend: https://bike-auction-platform667.onrender.com
- Backend health: https://bike-auction-platform667.onrender.com/actuator/health
- Source: https://github.com/minlee667/bike-auction-platform
Features
- Browse auction listings and see status, current price, minimum increment, and start/end times.
- Register and log in.
- Create auction listings.
- Place bids and view bid history.
- Start and close auctions where authorized.
Architecture
- Angular standalone frontend.
- Spring Boot REST backend.
- PostgreSQL persistence.
- Flyway database migrations.
- JWT authentication flow.
- Render hosting for frontend, backend, and database.
Local setup
Prerequisites
- Java 21
- Node.js and npm compatible with the Angular project
- PostgreSQL
Backend
1. Open bike-auction-platform in IntelliJ IDEA or a terminal.
2. Configure database URL, username, password, and JWT signing secret using the application's supported configuration/environment variables.
3. Start PostgreSQL and ensure the configured database exists.
4. Run the Spring Boot application from IntelliJ or using the Maven wrapper if included.
Frontend
1. Open a terminal in bike-auction-frontend.
2. Run npm ci (or npm install if no lockfile is present).
3. Run npm run build to create a production build.
4. Use the project's configured Angular serve command for local development.
Confirm the actual configuration keys and npm scripts in the repository before running in a new environment.
Deployment
- Frontend Render root directory: bike-auction-frontend
- Build command: npm ci && npm run build
- Publish directory used: dist/bike-auction-frontend/browser
- Backend is deployed as a Render web service.
- PostgreSQL is hosted by Render.
- Backend CORS configuration must allow the deployed frontend origin.
- Health endpoint: /actuator/health
Assumptions and trade-offs
- The scope prioritizes the core auction lifecycle and bidding experience; payments and transfer logistics are out of scope.
- UI validation improves usability, but bid and auction-state rules must be enforced by the backend as the source of truth.
- Unless WebSocket/SSE is implemented and configured, describe price changes as refresh-based rather than guaranteed push-based real-time updates.
- Render simplifies deployment for the assignment, but free-tier services can have cold starts.
- Secrets belong in environment variables, never in source control.
Verification checklist
- [ ] Open the deployed frontend in a private/incognito window.
- [ ] Register and log in with a dedicated reviewer account.
- [ ] Create an auction.
- [ ] Place a valid bid and verify price/history.
- [ ] Confirm a below-minimum bid is rejected by the backend.
- [ ] Close an auction and confirm further bids are rejected.
- [ ] Check browser DevTools and Render logs for errors.
Reviewer credentials
Create a dedicated reviewer account in the live application and share login details separately with the reviewer. Do not commit passwords or JWT secrets.
