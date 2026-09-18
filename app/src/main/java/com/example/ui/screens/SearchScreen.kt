package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ListingEntity
import com.example.ui.components.CategorySelectorRow
import com.example.ui.components.ListingCard
import com.example.ui.model.AppConstants
import com.example.ui.theme.TruekappPrimary
import com.example.ui.theme.TruekappSecondary

@Composable
fun SearchScreen(
    listings: List<ListingEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    selectedCondition: String?,
    onConditionSelected: (String?) -> Unit,
    onlyFavorites: Boolean,
    onToggleFavoritesOnly: () -> Unit,
    onListingClick: (ListingEntity) -> Unit,
    onProposeClick: (ListingEntity) -> Unit,
    onToggleFavorite: (ListingEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var isCityMenuOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Header & Input
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Buscar Trueques",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Encuentra lo que necesitas o busca a alguien interesado en lo que ofreces",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_input"),
                    placeholder = { Text("Ej: bicicleta, libro cálculo, guitarra, clases...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = TruekappPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TruekappPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        // City & Favorites Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // City Dropdown Button
                Box {
                    FilterChip(
                        selected = selectedCity != "Todas",
                        onClick = { isCityMenuOpen = true },
                        label = { Text("Ciudad: $selectedCity") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    DropdownMenu(
                        expanded = isCityMenuOpen,
                        onDismissRequest = { isCityMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas las ciudades") },
                            onClick = {
                                onCitySelected("Todas")
                                isCityMenuOpen = false
                            }
                        )
                        AppConstants.COLOMBIAN_CITIES.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    onCitySelected(city)
                                    isCityMenuOpen = false
                                }
                            )
                        }
                    }
                }

                // Only Favorites Filter
                FilterChip(
                    selected = onlyFavorites,
                    onClick = onToggleFavoritesOnly,
                    label = { Text("Favoritos") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (onlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (onlyFavorites) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        // Category Selector Chips
        item {
            CategorySelectorRow(
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }

        // Condition Filter Row
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCondition == null,
                        onClick = { onConditionSelected(null) },
                        label = { Text("Cualquier estado") }
                    )
                }
                items(AppConstants.CONDITIONS) { cond ->
                    val isSelected = selectedCondition == cond
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConditionSelected(if (isSelected) null else cond) },
                        label = { Text(cond) }
                    )
                }
            }
        }

        // Results count badge
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${listings.size} trueques disponibles",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Listing Cards
        if (listings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Sin resultados para tu búsqueda",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Prueba cambiando las palabras clave, ciudad o categoría.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(listings) { listing ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ListingCard(
                        listing = listing,
                        onCardClick = { onListingClick(listing) },
                        onProposeClick = { onProposeClick(listing) },
                        onToggleFavorite = { onToggleFavorite(listing) }
                    )
                }
            }
        }
    }
}
