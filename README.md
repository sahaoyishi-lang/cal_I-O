# NutriTrack - Native Android Calorie & Diet Companion 🥗📱

A modern, native Android application built with **Kotlin** and **Jetpack Compose (Material 3)** designed to empower users to track their daily calories, analyze nutrition, generate personalized healthy diet charts, and effortlessly log meals using camera photo recognition with portion clarification, barcode scanning, or natural speech voice input.

---

## 🌟 Key Features & Requirements Met

### 1. 🔐 User Account & Authentication
- **Email Registration & Sign In**: Users can register using their email, name, and a secure password.
- **Session Persistence**: Current authentication state and user details are securely maintained.
- **One-Tap Demo Account**: Quick test login pre-configured with realistic profile and meal data for immediate exploration.
- **Sign Out & Switch Account**: Users can easily sign out and manage their credentials.

---

### 2. 📊 Smart Onboarding & Personalized Healthy Diet Chart
- **Personal Detail Collection**:
  - **Age** (years)
  - **Gender** (Male / Female / Other)
  - **Current Weight** (kg)
  - **Height** (cm)
  - **Activity Level** (Sedentary, Lightly Active, Moderately Active, Very Active, Extra Active)
  - **Fitness Goal** (Weight Loss, Mild Weight Loss, Maintain Weight, Muscle Gain)
  - **Dietary Preference** (Balanced, High-Protein, Vegetarian, Vegan, Low-Carb / Keto)
- **Science-Backed Nutrition Engine**:
  - Calculates **BMR (Basal Metabolic Rate)** using the **Mifflin-St Jeor formula**.
  - Calculates **TDEE (Total Daily Energy Expenditure)** based on activity multipliers.
  - Automatically computes daily calorie targets with safety floors.
  - Splits macronutrients: **Protein** (1.4g–2.0g per kg bodyweight), **Fats** (25–30% of calories), **Carbohydrates** (clean energy remainder), and **Daily Water Intake Target** (35 mL per kg body weight).
- **Customized Meal-by-Meal Diet Chart**:
  - **Breakfast** (25% of calories): Energy-dense, fiber & protein-rich morning options.
  - **Lunch** (35% of calories): Primary fuel with balanced grain-to-greens ratios (e.g. brown rice, roti, dal, lean proteins).
  - **Afternoon Snack** (15% of calories): Low-glycemic snacks to sustain afternoon energy.
  - **Dinner** (25% of calories): Lean, easily digestible evening meals.
  - Includes exact portion guidelines, healthy alternatives, and registered dietitian tips.

---

### 3 & 4. 📸 3 Smart Food Upload & Intake Logging Options

Users have **3 dedicated options** to upload and log their food:

#### Option (i): 📷 Camera Photo with Interactive Portion Clarifier
- **Camera & Gallery Snapshot**: Users snap a photo of their plate or choose from gallery.
- **Visual Food Recognition**: Recognizes food categories (e.g., Cooked Rice, Grilled Chicken, Roti/Bread, Salad, Dal, Oatmeal).
- **Interactive Portion & Measurement Clarifier**:
  - Specifically handles when portions aren't clear (e.g., *"like amount of rice she takes"*):
  - **Unit Selector**: Switch between **Bowls (150g)**, **Cups (200g)**, **Grams (exact weight)**, **Slices**, or **Pieces**.
  - **Portion Stepper / Multiplier**: Choose **0.5x, 1x, 1.5x, 2x** or fine-tune with `+` and `-` buttons.
  - **Live Dynamic Nutrition Recalculation**: Watch calories, protein, carbs, and fat update in real time as the portion is adjusted.
  - Select meal slot (Breakfast, Lunch, Dinner, Snack) and confirm log in one tap.

#### Option (ii): 🔍 Barcode Scanner
- **UPC / EAN Scanner Viewfinder**: Viewfinder simulation with animated laser targeting beam.
- **Quick-Scan Presets**: Preloaded with popular verified packaged items (Quaker Oats, Optimum Nutrition Whey, Amul Milk, Chobani Greek Yogurt, Kind Dark Chocolate Bar, Pintola Peanut Butter).
- **Manual Barcode Lookup**: Direct entry of 13-digit EAN or 12-digit UPC barcodes.
- **Serving Multiplier & Breakdown**: Customize number of servings before logging.

#### Option (iii): 🎙️ Voice / Speech-to-Text Food Diary
- **Hands-Free Speech Input**: One-tap microphone button integrating Android's `RecognizerIntent.ACTION_RECOGNIZE_SPEECH`.
- **Natural Language Food Parser (NLP)**:
  - Intelligently extracts multiple foods, quantities, and units from natural sentences:
    - *"I had two boiled eggs and a glass of milk"* ➔ Extracts: 2 boiled eggs + 1 glass milk.
    - *"One bowl of white rice with chicken breast"* ➔ Extracts: 1 bowl rice + 1 chicken fillet.
    - *"Three rotis and a bowl of dal"* ➔ Extracts: 3 rotis + 1 bowl yellow dal.
  - Shows an editable itemized breakdown with nutrition before confirming.

---

### 5. 📱 Daily Diary Dashboard & Hydration Tracker
- **Calorie Budget Ring**: Dynamic circular progress meter showing Consumed vs Goal vs Remaining calories.
- **Macronutrient Progress**: Individual progress bars for Protein, Carbs, and Fats.
- **Hydration Tracker**: Daily water log with "+250ml Glass" quick button.
- **Meal-by-Meal Timeline**: Breakdown of logged items under Breakfast, Lunch, Snack, Dinner with delete actions.
- **Bottom Navigation**: Seamless switching between Diary, Diet Chart, and Profile.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.3+
- **UI Toolkit**: Jetpack Compose with Material Design 3
- **Navigation**: AndroidX Navigation 3 runtime & UI
- **Architecture**: MVVM with Repository pattern and Kotlin Coroutines / StateFlow
- **Image & Voice Contracts**:
  - `ActivityResultContracts.TakePicturePreview` (Camera)
  - `ActivityResultContracts.GetContent` (Gallery)
  - `ActivityResultContracts.StartActivityForResult` with `RecognizerIntent` (Voice)
- **Design Tokens**: Custom energetic health palette (Emerald Green `#0D7E46`, Fresh Teal `#007577`, Coral `#D85542`)

---

## 🚀 Running the Project

1. Open this directory in **Android Studio** (Hedgehog, Iguana, Jellyfish, or newer) or build with Gradle:
   ```bash
   ./gradlew assembleDebug
   ```
2. Run on an Android emulator or physical device (Android 7.0+ / API 24 to 35).
3. Tap **Quick Demo Account** to start immediately, or **Create Account** with your email.

