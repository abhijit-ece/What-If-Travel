# AI "What-If" Travel Simulator

A comprehensive full-stack application built to simulate alternative travel scenarios. This project enables users to model different travel parameters (destination, date, budget, group composition, transportation modes) and dynamically compare results side-by-side using real-time weather forecasts, coordinate distance calculations, and AI reasoning.

---

## 🛠️ Technology Stack
- **Backend**: Spring Boot 3.3.4, Java 17, Spring Security + JJWT, Hibernate/JPA, H2 In-Memory Database (with MySQL compatibility driver).
- **Frontend**: React 18, Vite 5, Tailwind CSS, Recharts (for score distributions), Lucide Icons.
- **Third-Party Integrations**: Gemini API (via HTTP JSON endpoints), OpenWeatherMap API.
- **Reporting**: LibrePDF (OpenPDF) for side-by-side PDF comparison downloads.

---

## ⚙️ Environment Configuration

Create a `.env` file in the root directory. You can copy the template from `.env.example`:

```env
# Server Port Configuration
PORT=8080

# Active Spring Profile
SPRING_PROFILES_ACTIVE=dev

# JWT Security
JWT_SECRET=YourSuperSecretKeyForSigningJWTsThatMustBeAtLeast512BitsLongToPreventExceptions
JWT_EXPIRATION_MS=86400000

# OpenWeatherMap API
OPENWEATHERMAP_API_KEY=your_openweathermap_api_key_here

# Google Gemini API
GEMINI_API_KEY=your_gemini_api_key_here
```

---

## 🚀 Setup & Execution

### 1. Backend Server Setup
Navigate to the `backend/` directory:
```bash
cd backend
```

Ensure environment variables are loaded (or set them in your local system) and execute the Spring Boot Maven wrapper:
```bash
# Windows PowerShell
.\mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```
The server will run on `http://localhost:8080`.
The in-memory H2 Console is accessible at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:travel_simulator_dev`, User: `sa`, Password: empty).

### 2. Frontend React Client Setup
Navigate to the `frontend/` directory:
```bash
cd frontend
```

Install node dependencies:
```bash
npm install
```

Launch the Vite local dev server:
```bash
npm run dev
```
The application will be accessible at `http://localhost:5173`.

---

## 🧠 How the "What-If" Engine Works

### 1. Session & Scenario Creation
When a user defines a base trip and sets of what-ifs (e.g. *"What if rainy season?"*, *"What if travel by train?"*), the backend:
- Serializes the base specifications.
- Automatically spawns a **Base Plan** scenario.
- Translates the natural language "What-If" variables using **Regex & Parameter Parsers** (matching keywords like *rainy/monsoon/winter*, *family/solo/friends*, *flight/train/car*, and numeric values like *₹25000*) to modify parameters for the corresponding What-If Scenario branches before persisting to the DB.

### 2. Compute Pipeline Execution
For each scenario in a session, the computing engine executes sequentially:
- **Weather Service**: Fetches 30-day average forecasts from OpenWeatherMap for the destination month (or falls back to built-in climate averages if the API key is not configured or queries fail).
- **Distance Service**: Calculates Haversine distances between base hubs (e.g. Mumbai) and destination coordinates, compiling expected travel durations for each mode (Flight, Train, Car).
- **AI Reasoning (Gemini)**: Invokes the Gemini API with structured instructions, enforcing strict JSON responses containing crowd level estimates, safety notes, hidden costs (e.g., peak local tourist surcharges), and day-by-day itineraries.
- **Scoring Engine**: Evaluates the metrics and maps them to a transparent 0-100 scale using the following exact mathematical weights:

| Metric Factor | Weight | Evaluation Logic |
| :--- | :---: | :--- |
| **Budget Fit** | **25%** | Evaluatesstated budget against projections. Penalized proportionally if projection exceeds budget or hidden expenses are high. |
| **Weather Suitability** | **20%** | Optimal score at 20-28°C. Steep penalties for monsoons, snowstorms, or extreme heat. |
| **Crowd Level** | **15%** | Inverse scale: lower crowd levels rank higher. Penalized during peak holiday season ranges. |
| **Safety Index** | **15%** | Scored out of 100. Deducted for terrain hazards (monsoon landslides/winter blizzards) and security keywords. |
| **Travel Convenience** | **10%** | Calculated via speed indices: flight is fast (+), road-trips suffer duration penalties (-) scaled by group comfort factors. |
| **Hidden Expense Risk** | **10%** | Ratios hidden extra estimates against stated budgets. Higher ratios trigger severe deductions. |
| **Preference Match** | **5%** | Matches destination features to group types (e.g., solo travel vs. family comfort requirements). |

---

## 🌐 Deployed Split-Deployment Architecture

This project is deployed across two platforms to optimize resource requirements and runtime performance:
1. **Frontend**: Deployed on **Vercel** as a static Single Page Application (SPA). Vercel provides fast global CDN hosting and handles client-side routing rewrites (`vercel.json`) for smooth route transitions.
2. **Backend**: Deployed on **Railway** as a containerized Java Spring Boot service connected to a managed **MySQL** database. Railway hosts long-running JVM instances and automatically maps database ports and credentials.

### Why Split-Deployment?
- **Server constraints**: Vercel does not support long-running processes (like Java Spring Boot servers). By hosting the static assets on Vercel and the backend service on Railway, we ensure the backend can listen to requests continuously while avoiding platform runtime limitations.
- **Dedicated Database**: Railway provides an integrated managed MySQL service out-of-the-box, simplifying schema updates and avoiding server database maintenance.

---

## 🛠️ Vercel Deployment Guide (Fixing 404 NOT_FOUND)

If you deploy this monorepo repository directly to Vercel, it may result in a **404: NOT_FOUND** error on the home page because Vercel looks for files at the repository root by default. 

To configure your Vercel deployment correctly:

1. **Set the Root Directory**:
   - Go to your **Vercel Dashboard** and open your project.
   - Go to **Settings** > **General**.
   - Locate the **Root Directory** field, and set it to **`frontend`**.
   - Save the changes.
   - *This tells Vercel to change directories into the React project (`frontend`) before runing the build and server configuration.*

2. **Configure the API Backend URL**:
   - In **Settings** > **Environment Variables**.
   - Add a new environment variable:
     - **Key**: `VITE_API_BASE_URL`
     - **Value**: `https://your-backend-railway-url.app` (replace with your active Railway/Render Spring Boot backend URL).
   - Save the environment variable.

3. **Deploy**:
   - Go to the **Deployments** tab and click **Redeploy** or push a new commit to your git repository to trigger a build.
   - Vercel will now install packages, build the client app with Vite, and serve the single-page application correctly using the rewrite rules defined in `frontend/vercel.json`.


---

## 📝 Disclaimer
*Estimates based on historical data and AI reasoning — always verify with official sources before travel.*
