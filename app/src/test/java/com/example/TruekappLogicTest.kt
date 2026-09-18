package com.example

import com.example.ui.model.AppConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TruekappLogicTest {

    @Test
    fun testInitialTargetMarketFacatativaIncluded() {
        assertTrue("Facatativá must be in the Colombian cities list", AppConstants.COLOMBIAN_CITIES.contains("Facatativá"))
        assertEquals("Facatativá", AppConstants.COLOMBIAN_CITIES.first())
    }

    @Test
    fun testRequiredCategoriesArePresent() {
        val categoryNames = AppConstants.CATEGORIES.map { it.id }
        assertTrue(categoryNames.contains("Tecnología"))
        assertTrue(categoryNames.contains("Ropa"))
        assertTrue(categoryNames.contains("Hogar"))
        assertTrue(categoryNames.contains("Libros"))
        assertTrue(categoryNames.contains("Deportes"))
        assertTrue(categoryNames.contains("Videojuegos"))
        assertTrue(categoryNames.contains("Servicios"))
        assertTrue(categoryNames.contains("Otros"))
    }

    @Test
    fun testRequiredProductConditions() {
        assertTrue(AppConstants.CONDITIONS.contains("Nuevo"))
        assertTrue(AppConstants.CONDITIONS.contains("Como nuevo"))
        assertTrue(AppConstants.CONDITIONS.contains("Buen estado"))
        assertTrue(AppConstants.CONDITIONS.contains("Usado"))
    }
}
