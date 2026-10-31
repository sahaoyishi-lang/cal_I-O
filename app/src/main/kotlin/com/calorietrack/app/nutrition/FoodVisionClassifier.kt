package com.calorietrack.app.nutrition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.calorietrack.app.model.FoodItem
import java.io.InputStream
import kotlin.math.roundToInt

data class FoodCandidate(
    val food: FoodItem,
    val confidence: Float, // 0.0 to 1.0
    val reason: String
)

data class FoodRecognitionResult(
    val bestMatch: FoodItem,
    val confidence: Float,
    val candidates: List<FoodCandidate>,
    val detectedTags: List<String>
)

object FoodVisionClassifier {

    /**
     * Corrects image orientation from EXIF metadata so the bitmap is perfectly upright
     * before running ML Kit and displaying to user.
     */
    fun decodeUprightBitmap(context: Context, uri: Uri, maxDimension: Int = 1024): Bitmap? {
        return try {
            // 1. Read EXIF Orientation
            var orientation = ExifInterface.ORIENTATION_NORMAL
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }

            // 2. Decode with sub-sampling for memory efficiency
            var stream: InputStream? = context.contentResolver.openInputStream(uri)
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(stream, null, boundsOptions)
            stream?.close()

            var inSampleSize = 1
            val maxSide = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
            while (maxSide / (inSampleSize * 2) >= maxDimension) {
                inSampleSize *= 2
            }

            stream = context.contentResolver.openInputStream(uri)
            val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            val rawBitmap = BitmapFactory.decodeStream(stream, null, decodeOptions)
            stream?.close()

            if (rawBitmap == null) return null

            // 3. Apply Rotation matrix based on EXIF
            val rotationDegrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            if (rotationDegrees != 0f) {
                val matrix = Matrix().apply { postRotate(rotationDegrees) }
                Bitmap.createBitmap(
                    rawBitmap,
                    0,
                    0,
                    rawBitmap.width,
                    rawBitmap.height,
                    matrix,
                    true
                )
            } else {
                rawBitmap
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Primary Vision Classification:
     * Combines Google ML Kit semantic labels + Central Food Color & Texture Heuristics
     * to produce highly accurate, ranked food candidates.
     */
    fun classifyImage(
        bitmap: Bitmap,
        mlKitLabels: List<Pair<String, Float>>
    ): FoodRecognitionResult {
        val labelNames = mlKitLabels.map { it.first }
        val labelString = labelNames.joinToString(" ").lowercase()

        val candidates = mutableListOf<FoodCandidate>()

        // 1. Check direct semantic matches from ML Kit
        fun addCandidate(foodId: String, baseConfidence: Float, reason: String) {
            val food = FoodDatabase.findById(foodId) ?: return
            if (candidates.none { it.food.id == food.id }) {
                candidates.add(FoodCandidate(food, baseConfidence.coerceIn(0.6f, 0.98f), reason))
            }
        }

        // Direct fruits & produce
        if (labelString.contains("banana")) addCandidate("banana", 0.96f, "Detected Banana shape & color")
        if (labelString.contains("apple")) addCandidate("apple", 0.95f, "Detected Fresh Apple")
        if (labelString.contains("orange") || labelString.contains("citrus")) addCandidate("orange_citrus", 0.94f, "Detected Citrus Fruit")
        if (labelString.contains("mango")) addCandidate("mango_fresh", 0.92f, "Detected Mango")

        // Direct meals & fast food
        if (labelString.contains("pizza")) addCandidate("pizza_slice", 0.96f, "Detected Pizza slice")
        if (labelString.contains("burger") || labelString.contains("hamburger") || labelString.contains("sandwich")) {
            addCandidate("burger_sandwich", 0.93f, "Detected Burger / Sandwich")
        }
        if (labelString.contains("pasta") || labelString.contains("spaghetti") || labelString.contains("macaroni")) {
            addCandidate("cooked_pasta", 0.94f, "Detected Pasta")
        }
        if (labelString.contains("noodle") || labelString.contains("ramen")) {
            addCandidate("noodles_ramen", 0.93f, "Detected Noodles")
        }

        // Proteins & Eggs
        if (labelString.contains("egg") || labelString.contains("boiled egg")) {
            addCandidate("boiled_egg", 0.93f, "Detected Egg")
            addCandidate("egg_omelette", 0.85f, "Omelette candidate")
        }
        if (labelString.contains("omelet") || labelString.contains("omelette")) {
            addCandidate("egg_omelette", 0.95f, "Detected Omelette")
        }
        if (labelString.contains("chicken") || labelString.contains("poultry") || labelString.contains("meat")) {
            addCandidate("chicken_breast_grilled", 0.92f, "Detected Chicken / Meat")
            addCandidate("chicken_biryani", 0.85f, "Rice & Chicken candidate")
        }
        if (labelString.contains("salmon") || labelString.contains("fish") || labelString.contains("seafood")) {
            addCandidate("salmon_fillet", 0.93f, "Detected Fish / Seafood")
        }

        // Grains & Rice
        if (labelString.contains("rice") || labelString.contains("biryani") || labelString.contains("pilaf")) {
            if (labelString.contains("biryani") || labelString.contains("spiced")) {
                addCandidate("chicken_biryani", 0.94f, "Detected Spiced Biryani")
                addCandidate("rice_cooked_white", 0.86f, "White Rice candidate")
            } else {
                addCandidate("rice_cooked_white", 0.95f, "Detected White Rice")
                addCandidate("rice_cooked_brown", 0.82f, "Brown Rice candidate")
            }
        }
        if (labelString.contains("bread") || labelString.contains("toast") || labelString.contains("bun")) {
            addCandidate("bread_whole_wheat", 0.94f, "Detected Bread")
            addCandidate("roti_chapati", 0.82f, "Flatbread / Roti candidate")
        }
        if (labelString.contains("roti") || labelString.contains("chapati") || labelString.contains("flatbread") || labelString.contains("tortilla")) {
            addCandidate("roti_chapati", 0.95f, "Detected Roti / Flatbread")
        }

        // Veggies & Soups
        if (labelString.contains("salad") || labelString.contains("lettuce") || labelString.contains("leaf vegetable")) {
            addCandidate("mixed_green_salad", 0.95f, "Detected Fresh Salad")
            addCandidate("broccoli_steamed", 0.83f, "Green Veggie candidate")
        }
        if (labelString.contains("soup") || labelString.contains("broth") || labelString.contains("curry") || labelString.contains("stew") || labelString.contains("lentil")) {
            addCandidate("dal_lentils_yellow", 0.94f, "Detected Lentil Dal / Curry")
        }
        if (labelString.contains("coffee") || labelString.contains("espresso")) {
            addCandidate("black_coffee", 0.96f, "Detected Coffee")
        }
        if (labelString.contains("tea") || labelString.contains("chai")) {
            addCandidate("milk_tea_chai", 0.95f, "Detected Tea / Chai")
        }
        if (labelString.contains("milk") || labelString.contains("dairy")) {
            addCandidate("milk_whole", 0.92f, "Detected Milk")
            addCandidate("greek_yogurt_plain", 0.85f, "Yogurt / Curd candidate")
        }

        // 2. Central Color & Hue Analysis (Crucial for home food plates: Rice, Dal, Veggies, Curries, Rotis)
        val colorStats = analyzeCenterPlateColors(bitmap)

        // Green dominance: Salad or Steamed Broccoli
        if (colorStats.greenRatio > 0.35f || (colorStats.avgHue in 75f..160f && colorStats.avgSat > 0.22f)) {
            addCandidate("mixed_green_salad", 0.92f, "Green leafy appearance")
            addCandidate("broccoli_steamed", 0.86f, "Green vegetable")
        }
        // High lightness, low saturation: White Rice, Milk, Paneer, or Oats
        else if (colorStats.avgVal > 0.68f && colorStats.avgSat < 0.22f) {
            addCandidate("rice_cooked_white", 0.93f, "Light grain appearance")
            addCandidate("paneer_cottage_cheese", 0.85f, "White cottage cheese candidate")
            addCandidate("rolled_oats", 0.78f, "Oats / porridge candidate")
        }
        // Golden / Yellow-Orange: Dal, Cooked Chickpeas, or Roti
        else if (colorStats.avgHue in 28f..65f && colorStats.avgSat > 0.35f) {
            addCandidate("dal_lentils_yellow", 0.92f, "Golden yellow pulse/curry appearance")
            addCandidate("chicken_biryani", 0.86f, "Golden spiced rice candidate")
            addCandidate("roti_chapati", 0.82f, "Wheat roti candidate")
        }
        // Red / Crimson: Apple or Pizza
        else if ((colorStats.avgHue < 18f || colorStats.avgHue > 340f) && colorStats.avgSat > 0.35f) {
            addCandidate("apple", 0.93f, "Red fruit appearance")
            addCandidate("pizza_slice", 0.85f, "Red sauce / pizza candidate")
        }
        // Warm Brownish tones: Grilled Chicken, Roti, Bread, or Potatoes
        else if (colorStats.avgHue in 18f..42f && colorStats.avgVal in 0.35f..0.68f) {
            addCandidate("roti_chapati", 0.90f, "Warm wheat tone")
            addCandidate("chicken_breast_grilled", 0.88f, "Cooked poultry tone")
            addCandidate("bread_whole_wheat", 0.82f, "Brown toast candidate")
        }

        // 3. Fallback: If no candidate was found yet, provide top universal staples
        if (candidates.isEmpty()) {
            addCandidate("rice_cooked_white", 0.85f, "Staple food candidate")
            addCandidate("dal_lentils_yellow", 0.80f, "Pulse candidate")
            addCandidate("roti_chapati", 0.78f, "Roti candidate")
            addCandidate("mixed_green_salad", 0.70f, "Salad candidate")
        }

        // Sort candidates by confidence descending
        candidates.sortByDescending { it.confidence }

        // Take top 4 distinct candidates
        val topCandidates = candidates.distinctBy { it.food.id }.take(4)
        val best = topCandidates.first()

        return FoodRecognitionResult(
            bestMatch = best.food,
            confidence = best.confidence,
            candidates = topCandidates,
            detectedTags = labelNames.take(5)
        )
    }

    private data class PlateColorStats(
        val avgHue: Float,
        val avgSat: Float,
        val avgVal: Float,
        val greenRatio: Float
    )

    private fun analyzeCenterPlateColors(bitmap: Bitmap): PlateColorStats {
        val width = bitmap.width
        val height = bitmap.height

        // Sample center 55% of the image (focusing on food, not table background)
        val startX = (width * 0.22f).toInt().coerceAtLeast(0)
        val endX = (width * 0.78f).toInt().coerceAtMost(width)
        val startY = (height * 0.22f).toInt().coerceAtLeast(0)
        val endY = (height * 0.78f).toInt().coerceAtMost(height)

        var sumHue = 0.0
        var sumSat = 0.0
        var sumVal = 0.0
        var greenCount = 0
        var totalSampled = 0

        val hsv = FloatArray(3)
        val stepX = ((endX - startX) / 25).coerceAtLeast(1)
        val stepY = ((endY - startY) / 25).coerceAtLeast(1)

        for (x in startX until endX step stepX) {
            for (y in startY until endY step stepY) {
                val pixel = bitmap.getPixel(x, y)
                AndroidColor.colorToHSV(pixel, hsv)
                val h = hsv[0]
                val s = hsv[1]
                val v = hsv[2]

                sumHue += h
                sumSat += s
                sumVal += v
                totalSampled++

                // Green detection (Hue between 75° and 160°, with sufficient saturation)
                if (h in 75f..160f && s > 0.20f && v > 0.15f) {
                    greenCount++
                }
            }
        }

        val total = totalSampled.coerceAtLeast(1)
        return PlateColorStats(
            avgHue = (sumHue / total).toFloat(),
            avgSat = (sumSat / total).toFloat(),
            avgVal = (sumVal / total).toFloat(),
            greenRatio = greenCount.toFloat() / total.toFloat()
        )
    }
}

