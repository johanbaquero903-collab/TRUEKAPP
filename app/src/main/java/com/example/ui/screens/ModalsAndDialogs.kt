package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ExchangeEntity
import com.example.data.model.ListingEntity
import com.example.data.model.UserEntity
import com.example.ui.components.SmartListingImage
import com.example.ui.theme.TruekappPrimary
import com.example.ui.theme.TruekappSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- 1. Listing Detail & Propose Swap Sheet ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailSheet(
    listing: ListingEntity,
    myListings: List<ListingEntity>,
    isProposing: Boolean,
    selectedOfferedListing: ListingEntity?,
    pitchMessage: String,
    onPitchMessageChange: (String) -> Unit,
    onSelectOfferedListing: (ListingEntity) -> Unit,
    onStartPropose: () -> Unit,
    onSubmitProposal: () -> Unit,
    onCancelPropose: () -> Unit,
    onDismiss: () -> Unit,
    onReportOwner: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!isProposing) {
                // Large Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.3f)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    SmartListingImage(
                        imageResName = listing.imageResName,
                        category = listing.category,
                        modifier = Modifier.fillMaxSize()
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "${listing.category} • ${listing.condition}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Title & Location
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TruekappPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${listing.neighborhood}, ${listing.city}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Report button
                    IconButton(onClick = { onReportOwner(listing.ownerId) }) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Reportar publicación",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Owner row
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Publicado por: ${listing.ownerName}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${String.format("%.1f", listing.ownerRating)} estrellas",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // What they want in exchange box
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TruekappPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = TruekappPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "¿Qué busca a cambio?",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TruekappPrimary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = listing.seekingExchangeFor,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }

                // Description
                Column {
                    Text(
                        text = "Descripción",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = listing.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action: Proponer Trueque
                Button(
                    onClick = onStartPropose,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TruekappPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("detail_proponer_trueque_button")
                ) {
                    Icon(imageVector = Icons.Default.Handshake, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Proponer trueque a ${listing.ownerName}")
                }
            } else {
                // --- Step 2: Proposing Swap Modal ---
                Text(
                    text = "Proponer Trueque",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    text = "Elige uno de tus artículos o servicios para ofrecerle a ${listing.ownerName} a cambio de '${listing.title}'.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (myListings.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Aún no tienes publicaciones propias para intercambiar.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ve a la pestaña '+ Publicar' para agregar lo que tienes antes de proponer un trueque.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Selecciona tu artículo para ofrecer:",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(myListings) { myItem ->
                            val isSelected = selectedOfferedListing?.id == myItem.id
                            Card(
                                onClick = { onSelectOfferedListing(myItem) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .width(160.dp)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) TruekappPrimary else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(90.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        SmartListingImage(
                                            imageResName = myItem.imageResName,
                                            category = myItem.category,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = myItem.title,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 2
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "✓ Seleccionado",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TruekappPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = pitchMessage,
                        onValueChange = onPitchMessageChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("proposal_pitch_message_input"),
                        label = { Text("Mensaje o propuesta inicial") },
                        placeholder = { Text("Hola! Me interesa mucho tu publicación...") },
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                        maxLines = 5
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancelPropose,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Atrás")
                        }

                        Button(
                            onClick = onSubmitProposal,
                            enabled = selectedOfferedListing != null,
                            colors = ButtonDefaults.buttonColors(containerColor = TruekappPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("submit_proposal_button")
                        ) {
                            Text("Enviar propuesta")
                        }
                    }
                }
            }
        }
    }
}

// --- 2. Counter-Offer Dialog ---
@Composable
fun CounterOfferDialog(
    exchange: ExchangeEntity,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hacer Contraoferta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Proponle un ajuste a la oferta (ejemplo: agregar otro accesorio, cambiar punto de encuentro o acordar entrega diferente).",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Ej: Me interesa, pero ¿podrías incluir el cable original o vernos en el Centro?") },
                    shape = RoundedCornerShape(10.dp),
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (note.isNotBlank()) onSubmit(note)
                },
                enabled = note.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TruekappPrimary)
            ) {
                Text("Enviar contraoferta")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// --- 3. Full-Screen Chat Dialog ---
@Composable
fun ChatDialog(
    exchange: ExchangeEntity,
    messages: List<ChatMessageEntity>,
    currentUserId: String,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Chat Header
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Chat de Trueque",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${exchange.proposerListingTitle} ⇄ ${exchange.targetListingTitle}",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        if (msg.isSystemEvent) {
                            // System notification bubble
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = msg.message,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TruekappPrimary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        } else {
                            val isMe = msg.senderId == currentUserId
                            val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(msg.timestamp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {
                                if (!isMe) {
                                    Text(
                                        text = msg.senderName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 2.dp,
                                        bottomEnd = if (isMe) 2.dp else 16.dp
                                    ),
                                    color = if (isMe) TruekappPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Text(
                                            text = msg.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = timeStr,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.align(Alignment.End)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Chat Input Row
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Escribe un mensaje...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    onSendMessage(messageText)
                                    messageText = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(TruekappPrimary, CircleShape)
                                .testTag("send_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Enviar",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 4. Star Rating Dialog (1 to 5 Stars) ---
@Composable
fun RatingDialog(
    exchange: ExchangeEntity,
    currentUserId: String,
    onDismiss: () -> Unit,
    onSubmit: (stars: Int, comment: String) -> Unit
) {
    val counterpartName = if (exchange.proposerId == currentUserId) exchange.targetOwnerName else exchange.proposerName
    var stars by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calificar a $counterpartName") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "¿Cómo fue tu experiencia en este trueque?",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Interactive 1-5 Stars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = { stars = i }) {
                            Icon(
                                imageVector = if (i <= stars) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$i estrellas",
                                tint = if (i <= stars) Color(0xFFF59E0B) else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Text(
                    text = when (stars) {
                        5 -> "¡Excelente truequero! 100% recomendado"
                        4 -> "Muy buen intercambio"
                        3 -> "Intercambio correcto"
                        2 -> "Hubo inconvenientes"
                        else -> "Mala experiencia"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TruekappPrimary
                )

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Escribe un breve comentario de retroalimentación...") },
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rating_comment_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(stars, comment) },
                colors = ButtonDefaults.buttonColors(containerColor = TruekappPrimary),
                modifier = Modifier.testTag("submit_rating_button")
            ) {
                Text("Guardar calificación")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Más tarde")
            }
        }
    )
}

// --- 5. Report & Block Dialog ---
@Composable
fun ReportBlockDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, shouldBlock: Boolean) -> Unit
) {
    var selectedReason by remember { mutableStateOf("Contenido inapropiado o falso") }
    var shouldBlock by remember { mutableStateOf(false) }

    val reasons = listOf(
        "Contenido inapropiado o falso",
        "No asistió al punto de encuentro acordado",
        "Artículo en mal estado no advertido",
        "Comportamiento sospechoso o fraude",
        "Spam o publicidad no autorizada"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = Color(0xFFDC2626))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reportar a ${user.name}")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ayúdanos a mantener la comunidad de TRUEKAPP en Facatativá segura y confiable.",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(4.dp))

                reasons.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = r }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isSelected = selectedReason == r
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) TruekappPrimary else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isSelected) TruekappPrimary else Color.Gray),
                            modifier = Modifier.size(16.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(r, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = shouldBlock,
                        onCheckedChange = { shouldBlock = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFDC2626))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bloquear también a este usuario",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedReason, shouldBlock) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Enviar reporte")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
