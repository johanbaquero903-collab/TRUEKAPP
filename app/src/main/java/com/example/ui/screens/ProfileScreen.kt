package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ListingEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserReviewEntity
import com.example.ui.components.ListingCard
import com.example.ui.theme.TruekappPrimary
import com.example.ui.theme.TruekappSecondary
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: UserEntity?,
    allUsers: List<UserEntity>,
    myListings: List<ListingEntity>,
    favoriteListings: List<ListingEntity>,
    reviewsFlow: Flow<List<UserReviewEntity>>,
    onSwitchUser: (String) -> Unit,
    onListingClick: (ListingEntity) -> Unit,
    onProposeClick: (ListingEntity) -> Unit,
    onToggleFavorite: (ListingEntity) -> Unit,
    onOpenReportDialog: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedProfileTab by remember { mutableStateOf(0) } // 0: Mis publicaciones, 1: Guardados, 2: Opiniones
    var isSwitchUserDialogOpen by remember { mutableStateOf(false) }

    val reviews by reviewsFlow.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            if (currentUser?.avatarUrl?.isNotBlank() == true) {
                                AsyncImage(
                                    model = currentUser.avatarUrl,
                                    contentDescription = currentUser.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = TruekappPrimary,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // User Details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser?.name ?: "Usuario TRUEKAPP",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verificado",
                                    tint = TruekappPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = currentUser?.username ?: "@truekero",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Rating & Location Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFEF3C7)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${String.format("%.1f", currentUser?.rating ?: 5.0f)} (${currentUser?.ratingCount ?: 0})",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF92400E)
                                            )
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${currentUser?.neighborhood}, ${currentUser?.city}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bio
                    if (!currentUser?.bio.isNullOrBlank()) {
                        Text(
                            text = currentUser!!.bio,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Student & Community Badges
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = TruekappPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentUser?.university ?: "Comunidad Universitaria Facatativá",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TruekappPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Multi-User Switcher Button (Demo & testing peer-to-peer exchanges)
                    OutlinedButton(
                        onClick = { isSwitchUserDialogOpen = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("switch_user_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwitchAccount,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cambiar perfil (Modo prueba P2P)")
                    }
                }
            }
        }

        // Sub-tabs: Mis Publicaciones | Guardados | Calificaciones
        item {
            SecondaryTabRow(
                selectedTabIndex = selectedProfileTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedProfileTab == 0,
                    onClick = { selectedProfileTab = 0 },
                    text = { Text("Mis Trueques (${myListings.size})") }
                )
                Tab(
                    selected = selectedProfileTab == 1,
                    onClick = { selectedProfileTab = 1 },
                    text = { Text("Favoritos (${favoriteListings.size})") }
                )
                Tab(
                    selected = selectedProfileTab == 2,
                    onClick = { selectedProfileTab = 2 },
                    text = { Text("Opiniones (${reviews.size})") }
                )
            }
        }

        // Content based on sub-tab
        when (selectedProfileTab) {
            0 -> {
                // My Listings
                if (myListings.isEmpty()) {
                    item {
                        EmptyProfileSection(
                            message = "Aún no has publicado productos o servicios para trueque.",
                            buttonText = "Publicar ahora"
                        )
                    }
                } else {
                    items(myListings) { item ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ListingCard(
                                listing = item,
                                onCardClick = { onListingClick(item) },
                                onProposeClick = { onProposeClick(item) },
                                onToggleFavorite = { onToggleFavorite(item) }
                            )
                        }
                    }
                }
            }

            1 -> {
                // Saved / Favorites
                if (favoriteListings.isEmpty()) {
                    item {
                        EmptyProfileSection(
                            message = "No tienes trueques guardados como favoritos.",
                            buttonText = "Explorar trueques"
                        )
                    }
                } else {
                    items(favoriteListings) { item ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ListingCard(
                                listing = item,
                                onCardClick = { onListingClick(item) },
                                onProposeClick = { onProposeClick(item) },
                                onToggleFavorite = { onToggleFavorite(item) }
                            )
                        }
                    }
                }
            }

            2 -> {
                // Reviews Received
                if (reviews.isEmpty()) {
                    item {
                        EmptyProfileSection(
                            message = "Aún no tienes calificaciones de trueques completados.",
                            buttonText = null
                        )
                    }
                } else {
                    items(reviews) { review ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = review.fromUserName,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            repeat(review.stars) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFF59E0B),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "“${review.comment}”",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Switch User Dialog
    if (isSwitchUserDialogOpen) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { isSwitchUserDialogOpen = false },
            title = { Text("Seleccionar usuario activo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Cambia de usuario para probar ofertas, contraofertas y chats desde ambas perspectivas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    allUsers.forEach { user ->
                        val isSelected = user.id == currentUser?.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSwitchUser(user.id)
                                    isSwitchUserDialogOpen = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray)
                                ) {
                                    if (user.avatarUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = user.avatarUrl,
                                            contentDescription = user.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${user.neighborhood}, ${user.city} • ★ ${String.format("%.1f", user.rating)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { isSwitchUserDialogOpen = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun EmptyProfileSection(
    message: String,
    buttonText: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
