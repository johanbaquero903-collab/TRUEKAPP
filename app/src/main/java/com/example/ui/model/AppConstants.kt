package com.example.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val badgeColorHex: Long
)

object AppConstants {
    val CATEGORIES = listOf(
        CategoryItem("all", "Todos", Icons.Default.AutoAwesome, 0xFF047857),
        CategoryItem("Tecnología", "Tecnología", Icons.Default.Devices, 0xFF0284C7),
        CategoryItem("Ropa", "Ropa y Moda", Icons.Default.Checkroom, 0xFFE11D48),
        CategoryItem("Hogar", "Hogar", Icons.Default.Home, 0xFFD97706),
        CategoryItem("Libros", "Libros y Estudio", Icons.Default.Book, 0xFF7C3AED),
        CategoryItem("Deportes", "Deportes", Icons.Default.FitnessCenter, 0xFF059669),
        CategoryItem("Videojuegos", "Videojuegos", Icons.Default.SportsEsports, 0xFF4F46E5),
        CategoryItem("Servicios", "Servicios", Icons.Default.Handyman, 0xFFEA580C),
        CategoryItem("Otros", "Otros", Icons.Default.AutoAwesome, 0xFF64748B)
    )

    val CONDITIONS = listOf(
        "Nuevo",
        "Como nuevo",
        "Buen estado",
        "Usado"
    )

    val COLOMBIAN_CITIES = listOf(
        "Facatativá",
        "Bogotá D.C.",
        "Madrid (Cund.)",
        "Mosquera",
        "Funza",
        "Chía",
        "Zipaquirá",
        "Medellín",
        "Cali"
    )

    val FACATATIVA_SECTORS = listOf(
        "Centro",
        "Manablanca",
        "Tisquesusa",
        "Cartagenita",
        "Santa Rita",
        "Dos Caminos",
        "Omnicentro",
        "Universidad de Cundinamarca",
        "Pueblo Viejo",
        "Copihue"
    )
}
