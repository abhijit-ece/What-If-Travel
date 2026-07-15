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

## 📝 Disclaimer
*Estimates based on historical data and AI reasoning — always verify with official sources before travel.*
