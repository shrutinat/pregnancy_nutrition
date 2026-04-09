# 🤰 Pregnancy Nutrition Tracker App

A smart healthcare desktop application that helps pregnant women track nutrition, health progress, and medical schedules across all trimesters.

![Java](https://img.shields.io/badge/Java-17+-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![SQLite](https://img.shields.io/badge/Database-SQLite-green)
![Maven](https://img.shields.io/badge/Build-Maven-red)

---

## ✨ Features

### 1. User Profile
- Register/Login with email & password (SHA-256 hashed)
- Auto-calculate BMI from height and weight
- Auto-detect current trimester based on pregnancy start date
- Due date estimation and days remaining

### 2. Nutrition Calculator
- Personalized daily targets using Harris-Benedict equation
- Trimester-based calorie adjustments (+0/+340/+450 kcal)
- WHO-recommended protein, iron, calcium, vitamin targets
- Doctor-modifiable nutrition goals

### 3. Food Database
- 50+ food items loaded from CSV at runtime
- Includes Indian and international foods
- Searchable by name with live results
- Nutrient data: calories, protein, iron, calcium

### 4. Daily Food Tracker
- Search and add foods to daily log
- Quantity-based nutrient calculation
- Mark foods as consumed/planned
- Real-time progress updates

### 5. AI-Based Food Suggestions
- Rule-based engine analyzing nutrient deficits
- Dynamic suggestions ranked by relevance
- Deficit thresholds: 30% for iron/calcium/protein, 20% for calories
- Top 8 personalized suggestions

### 6. Progress Dashboard
- **Bar Chart** → Daily nutrient completion
- **Line Chart** → Weight gain over time
- **Pie Chart** → Nutrient balance
- **Timeline** → Baby growth milestones (week by week)
- 7-day progress tracking

### 7. Alerts & Reminders
- Meal, medicine, and appointment reminders
- Recurring daily reminders
- Risk alerts for low iron/calcium/calories
- Default reminders created on registration

### 8. Doctor Module
- Add medical notes and observations
- Flag risk conditions (anemia, gestational diabetes, etc.)
- Modify nutrition targets per doctor's orders
- Update history with timestamps

---

## 🛠 Tech Stack

| Component | Technology |
|-----------|-----------|
| Frontend | JavaFX 21 (Programmatic UI) |
| Backend | Core Java 17+ (MVC Architecture) |
| Database | SQLite (via sqlite-jdbc) |
| Charts | JavaFX Charts (built-in) |
| Build | Maven |
| Security | SHA-256 password hashing |

---

## 📋 Prerequisites

1. **JDK 17 or higher** — [Download](https://adoptium.net/)
2. **Apache Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)

Verify installation:
```bash
java -version
mvn -version
```

---

## 🚀 Setup & Run

### Step 1: Clone/Navigate to project
```bash
cd pregnancy-nutrition-tracker
```

### Step 2: Compile the project
```bash
mvn clean compile
```

### Step 3: Run the application
```bash
mvn javafx:run
```

### Alternative: Build JAR
```bash
mvn clean package
java -jar target/pregnancy-nutrition-tracker-1.0-SNAPSHOT.jar
```

---

## 📁 Project Structure

```
pregnancy-nutrition-tracker/
├── pom.xml                          # Maven build configuration
├── README.md                        # This file
├── src/main/java/
│   ├── module-info.java             # Java module descriptor
│   └── com/pregnancy/tracker/
│       ├── App.java                 # Main entry point
│       ├── model/                   # Entity classes (User, FoodItem, etc.)
│       ├── dao/                     # Database access objects
│       ├── service/                 # Business logic (AI engine, calculators)
│       ├── controller/              # JavaFX UI controllers
│       └── util/                    # Utilities (CSV loader, date utils)
└── src/main/resources/
    ├── data/
    │   ├── food_database.csv        # 50+ food items
    │   └── baby_growth.csv          # Weekly growth milestones
    └── com/pregnancy/tracker/
        └── styles/
            └── app.css              # Premium UI styling
```

---

## 🗄 Database Schema

The app uses SQLite with 7 tables:

| Table | Purpose |
|-------|---------|
| `users` | User profiles with pregnancy data |
| `nutrition_targets` | Daily nutrient goals per trimester |
| `food_items` | Food nutrition database |
| `daily_logs` | Daily food consumption log |
| `reminders` | Scheduled reminders and alerts |
| `doctor_updates` | Medical notes and risk conditions |
| `weight_logs` | Weight tracking history |

---

## 🧠 AI Engine Rules

The food suggestion engine follows these rules:

```
IF iron_deficit > 30%     → Suggest iron-rich foods (spinach, lentils, dates)
IF calcium_deficit > 30%  → Suggest calcium-rich foods (milk, cheese, sesame)
IF protein_deficit > 30%  → Suggest protein-rich foods (eggs, chicken, tofu)
IF calorie_deficit > 20%  → Suggest calorie-dense foods (nuts, avocado)
```

Suggestions are ranked by:
1. Nutrient density of the food
2. Severity of the deficit
3. Priority weight (iron > calcium > protein > calories)

---

## 📸 Screens

1. **Login/Register** — Modern tabbed form with validation
2. **Dashboard** — Stats cards, nutrient progress, baby growth, AI suggestions
3. **Food Tracker** — Search, add, track, and manage daily food log
4. **Progress Charts** — Bar, line, pie charts with weekly analysis
5. **Doctor Updates** — Medical notes, risk conditions, target adjustments
6. **Reminders** — Create, manage, toggle meal/medicine/appointment alerts
7. **Profile** — View/edit personal health information

---

## 📊 Datasets Used

1. **Food Database** — Custom CSV with 50+ items (USDA-based values)
2. **Baby Growth** — WHO fetal growth standards (weeks 4-40)
3. **Nutrition Guidelines** — WHO pregnancy recommendations (hardcoded)

---

## 🔮 Future Enhancements

- [ ] PDF/CSV report export
- [ ] Rule-based AI chatbot
- [ ] Multi-language support
- [ ] Cloud sync capability
- [ ] Mobile companion app (Android)



