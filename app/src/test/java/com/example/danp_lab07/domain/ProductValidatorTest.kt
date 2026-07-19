package com.example.danp_lab07.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductValidatorTest {

    @Test
    fun `valid form returns parsed price without errors`() {
        val result = ProductValidator.validate(
            name = "Laptop",
            price = "3499.90",
            description = "Equipo para desarrollo",
            category = "Tecnología"
        )

        assertTrue(result.isValid)
        assertEquals(3499.90, result.parsedPrice ?: 0.0, 0.001)
        assertNull(result.nameError)
        assertNull(result.priceError)
    }

    @Test
    fun `blank required fields and non positive price are rejected`() {
        val result = ProductValidator.validate(
            name = " ",
            price = "0",
            description = "",
            category = ""
        )

        assertFalse(result.isValid)
        assertEquals("El nombre es obligatorio", result.nameError)
        assertEquals("El precio debe ser mayor que cero", result.priceError)
        assertEquals("La descripción es obligatoria", result.descriptionError)
        assertEquals("La categoría es obligatoria", result.categoryError)
    }

    @Test
    fun `invalid decimal price is rejected`() {
        val result = ProductValidator.validate(
            name = "Mouse",
            price = "precio",
            description = "Inalámbrico",
            category = "Tecnología"
        )

        assertFalse(result.isValid)
        assertEquals("Ingresa un precio válido", result.priceError)
        assertNull(result.parsedPrice)
    }
}
