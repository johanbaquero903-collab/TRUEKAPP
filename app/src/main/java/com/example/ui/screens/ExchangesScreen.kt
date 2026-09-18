package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExchangeEntity
import com.example.data.model.UserEntity
import com.example.ui.components.ExchangeStatusBadge
import com.example.ui.components.SmartListingImage
import com.example.ui.theme.TruekappPrimary
import com.example.ui.theme.TruekappSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangesScreen(
    exchanges: List<ExchangeEntity>,
    currentUser: UserEntity?,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onCounterOffer: (ExchangeEntity) -> Unit,
    onComplete: (String) -> Unit,
    onOpenChat: (ExchangeEntity) -> Unit,
    onRate: (ExchangeEntity) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: En curso, 1: Historial
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    val statusFilters = listOf(
        "ALL" to "Todos",
        "PROPOSED" to "Propuesta",
        "NEGOTIATING" to "Negociación",
        "ACCEPTED" to "Aceptado",
        "COMPLETED" to "Completado",
        "CANCELLED" to "Cancelado"
    )

    val activeExchanges = exchanges.filter { it.status in listOf("PROPOSED", "NEGOTIATING", "ACCEPTED") }
    val historyExchanges = exchanges.filter { it.status in listOf("COMPLETED", "CANCELLED", "REJECTED") }

    val displayedList = if (selectedStatusFilter != "ALL") {
        exchanges.filter { 
            if (selectedStatusFilter == "CANCELLED") {
                it.status in listOf("CANCELLED", "REJECTED")
            } else {
                it.status == selectedStatusFilter 
            }
        }
    } else {
        if (selectedTab == 0) activeExchanges else historyExchanges
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Screen Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Tus Intercambios",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Gestiona tus propuestas, negocia y califica tus trueques",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Tabs: En curso vs Historial (when ALL filter is selected)
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { 
                    selectedTab = 0 
                    selectedStatusFilter = "ALL"
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("En curso")
                        if (activeExchanges.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = TruekappPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "${activeExchanges.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { 
                    selectedTab = 1 
                    selectedStatusFilter = "ALL"
                },
                text = {
                    Text("Historial (${historyExchanges.size})")
                }
            )
        }

        // Horizontal Status Chips Filter (Propuesta, Negociación, Aceptado, Completado, Cancelado)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(statusFilters) { (statusKey, statusLabel) ->
                val isSelected = selectedStatusFilter == statusKey
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedStatusFilter = statusKey
                    },
                    label = { Text(statusLabel, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TruekappPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (displayedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Handshake,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedTab == 0) "No tienes trueques activos" else "No hay historial de trueques aún",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Explora las publicaciones disponibles en Facatativá y haz tu primera propuesta.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onExploreClick,
                        colors = ButtonDefaults.buttonColors(containerColor = TruekappPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Explorar trueques")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(displayedList) { exchange ->
                    ExchangeCardItem(
                        exchange = exchange,
                        currentUserId = currentUser?.id ?: "user_camilo",
                        onAccept = { onAccept(exchange.id) },
                        onReject = { onReject(exchange.id) },
                        onCounterOffer = { onCounterOffer(exchange) },
                        onComplete = { onComplete(exchange.id) },
                        onOpenChat = { onOpenChat(exchange) },
                        onRate = { onRate(exchange) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExchangeCardItem(
    exchange: ExchangeEntity,
    currentUserId: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCounterOffer: () -> Unit,
    onComplete: () -> Unit,
    onOpenChat: () -> Unit,
    onRate: () -> Unit
) {
    val isProposer = exchange.proposerId == currentUserId
    val counterpartName = if (isProposer) exchange.targetOwnerName else exchange.proposerName
    val hasAlreadyRated = if (isProposer) exchange.ratedByProposer else exchange.ratedByTarget

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exchange_card_${exchange.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Status & Counterpart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isProposer) "Propuesta enviada a:" else "Propuesta recibida de:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = counterpartName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                ExchangeStatusBadge(status = exchange.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Swap Visual Pair: [Product A] <---> [Product B]
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Item 1 (Proposer's Item)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            SmartListingImage(
                                imageResName = exchange.proposerListingImage,
                                category = "Trueque",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isProposer) "Tu oferta" else "Te ofrece",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TruekappPrimary
                            )
                        )
                        Text(
                            text = exchange.proposerListingTitle,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Swap Icon
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Intercambio",
                            tint = TruekappPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Item 2 (Target's Item)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            SmartListingImage(
                                imageResName = exchange.targetListingImage,
                                category = "Trueque",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isProposer) "Solicitas" else "Tu artículo",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TruekappSecondary
                            )
                        )
                        Text(
                            text = exchange.targetListingTitle,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Pitch message display
            if (exchange.pitchMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "“${exchange.pitchMessage}”",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Counteroffer Note if negotiating
            if (!exchange.counterOfferNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Contraoferta: ${exchange.counterOfferNote}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF92400E),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Meeting Place if accepted
            if (exchange.status == "ACCEPTED" && !exchange.meetingPlace.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TruekappPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Punto de encuentro: ${exchange.meetingPlace}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TruekappPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons based on Status
            when (exchange.status) {
                "PROPOSED" -> {
                    if (!isProposer) {
                        // Current user received the proposal: Can Accept, Counteroffer or Reject
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onAccept,
                                colors = ButtonDefaults.buttonColors(containerColor = TruekappPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aceptar")
                            }

                            OutlinedButton(
                                onClick = onCounterOffer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Contraoferta")
                            }

                            OutlinedButton(
                                onClick = onReject,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        // User proposed it
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Esperando respuesta de $counterpartName...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = onOpenChat,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = "Chat",
                                    tint = TruekappPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Chat", color = TruekappPrimary)
                            }
                        }
                    }
                }

                "NEGOTIATING" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAccept,
                            colors = ButtonDefaults.buttonColors(containerColor = TruekappPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aceptar oferta")
                        }

                        Button(
                            onClick = onOpenChat,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = TruekappSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Negociar en Chat", color = TruekappSecondary)
                        }
                    }
                }

                "ACCEPTED" -> {
                    // Accepted: Ready to coordinate & complete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onComplete,
                            colors = ButtonDefaults.buttonColors(containerColor = TruekappPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Completar trueque")
                        }

                        Button(
                            onClick = onOpenChat,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = TruekappPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat", color = TruekappPrimary)
                        }
                    }
                }

                "COMPLETED" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!hasAlreadyRated) {
                            Button(
                                onClick = onRate,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calificar a $counterpartName (1-5 ★)", color = Color.White)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFD1FAE5)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = TruekappPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ya calificaste este trueque", color = TruekappPrimary, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        IconButton(onClick = onOpenChat) {
                            Icon(Icons.Default.Chat, contentDescription = "Ver conversación", tint = TruekappPrimary)
                        }
                    }
                }

                else -> {
                    // Cancelled or Rejected
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Intercambio finalizado sin acuerdo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = onOpenChat) {
                            Icon(Icons.Default.Chat, contentDescription = "Chat", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
