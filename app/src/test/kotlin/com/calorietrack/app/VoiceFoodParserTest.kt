package com.calorietrack.app

import com.calorietrack.app.model.PortionUnit
import com.calorietrack.app.nutrition.VoiceFoodParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceFoodParserTest {

    @Test
    fun testParseSpokenMeal_TwoEggsAndMilk() {
        val text = "I had two boiled eggs and a glass of milk"
        val parsed = VoiceFoodParser.parse(text)

        assertEquals(2, parsed.size)

        val egg = parsed.find { it.foodItem.id == "boiled_egg" }
        assertNotNull("Should detect boiled egg", egg)
        assertEquals(2.0f, egg!!.quantity, 0.01f)
        assertTrue("Calories for 2 eggs should be ~155 kcal", egg.nutrition.calories in 150..165)

        val milk = parsed.find { it.foodItem.id == "milk_whole" }
        assertNotNull("Should detect milk", milk)
        assertEquals(PortionUnit.CUPS, milk!!.unit)
    }

    @Test
    fun testParseSpokenMeal_RiceAndChicken() {
        val text = "one bowl of cooked white rice with chicken breast"
        val parsed = VoiceFoodParser.parse(text)

        assertEquals(2, parsed.size)

        val rice = parsed.find { it.foodItem.id.contains("rice") }
        assertNotNull("Should detect rice", rice)
        assertEquals(PortionUnit.BOWLS, rice!!.unit)

        val chicken = parsed.find { it.foodItem.id.contains("chicken") }
        assertNotNull("Should detect chicken", chicken)
    }

    @Test
    fun testParseSpokenMeal_RotisAndDal() {
        val text = "three rotis and a bowl of dal"
        val parsed = VoiceFoodParser.parse(text)

        assertEquals(2, parsed.size)
        val roti = parsed.find { it.foodItem.id == "roti_chapati" }
        assertNotNull(roti)
        assertEquals(3.0f, roti!!.quantity, 0.01f)
    }
}

