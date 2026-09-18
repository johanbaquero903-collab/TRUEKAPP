package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.local.TruekappDatabase
import com.example.data.model.ExchangeEntity
import com.example.data.repository.TruekappRepository
import com.example.ui.components.TruekappBottomBar
import com.example.ui.components.TruekappTopBar
import com.example.ui.model.AppConstants
import com.example.ui.screens.ChatDialog
import com.example.ui.screens.CounterOfferDialog
import com.example.ui.screens.ExchangesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ListingDetailSheet
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PublishScreen
import com.example.ui.screens.RatingDialog
import com.example.ui.screens.ReportBlockDialog
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.TruekappViewModel
import com.example.ui.viewmodel.TruekappViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = TruekappDatabase.getDatabase(applicationContext)
        val repository = TruekappRepository(database.truekappDao())
        val viewModelFactory = TruekappViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: TruekappViewModel = viewModel(factory = viewModelFactory)
                TruekappApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TruekappApp(viewModel: TruekappViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val listings by viewModel.listings.collectAsStateWithLifecycle()
    val myListings by viewModel.myListings.collectAsStateWithLifecycle()
    val myExchanges by viewModel.myExchanges.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val selectedCondition by viewModel.selectedCondition.collectAsStateWithLifecycle()
    val onlyFavorites by viewModel.onlyFavorites.collectAsStateWithLifecycle()

    val activeListingDetail by viewModel.activeListingDetail.collectAsStateWithLifecycle()
    val isProposingTrueque by viewModel.isProposingTrueque.collectAsStateWithLifecycle()
    val selectedOfferedListing by viewModel.selectedOfferedListing.collectAsStateWithLifecycle()
    val pitchMessage by viewModel.pitchMessage.collectAsStateWithLifecycle()

    val activeChatExchange by viewModel.activeChatExchange.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val ratingExchange by viewModel.ratingExchange.collectAsStateWithLifecycle()
    val reportingUser by viewModel.reportingUser.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    // Local dialogs for TopBar shortcuts
    var isTopCityPickerOpen by remember { mutableStateOf(false) }
    var isTopUserSwitcherOpen by remember { mutableStateOf(false) }
    var counterOfferExchange by remember { mutableStateOf<ExchangeEntity?>(null) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    val pendingExchangesCount = myExchanges.count { it.status == "PROPOSED" && it.targetOwnerId == currentUser?.id }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TruekappTopBar(
                currentCity = selectedCity,
                currentUser = currentUser,
                onCityClick = { isTopCityPickerOpen = true },
                onUserClick = { isTopUserSwitcherOpen = true }
            )
        },
        bottomBar = {
            TruekappBottomBar(
                currentTab = currentTab,
                pendingExchangesCount = pendingExchangesCount,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.INICIO -> HomeScreen(
                    listings = listings,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.onCategorySelect(it) },
                    onListingClick = { viewModel.openListingDetail(it) },
                    onProposeClick = { viewModel.startProposeTrueque(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onPublishClick = { viewModel.selectTab(MainTab.PUBLICAR) },
                    onExploreSearchClick = { viewModel.selectTab(MainTab.BUSCAR) }
                )

                MainTab.BUSCAR -> SearchScreen(
                    listings = listings,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.onCategorySelect(it) },
                    selectedCity = selectedCity,
                    onCitySelected = { viewModel.onCitySelect(it) },
                    selectedCondition = selectedCondition,
                    onConditionSelected = { viewModel.onConditionSelect(it) },
                    onlyFavorites = onlyFavorites,
                    onToggleFavoritesOnly = { viewModel.toggleFavoritesOnly() },
                    onListingClick = { viewModel.openListingDetail(it) },
                    onProposeClick = { viewModel.startProposeTrueque(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                )

                MainTab.PUBLICAR -> PublishScreen(
                    onPublish = { title, desc, cat, cond, city, neigh, seek, isServ ->
                        viewModel.publishListing(title, desc, cat, cond, city, neigh, seek, isServ)
                    }
                )

                MainTab.INTERCAMBIOS -> ExchangesScreen(
                    exchanges = myExchanges,
                    currentUser = currentUser,
                    onAccept = { viewModel.acceptExchange(it) },
                    onReject = { viewModel.rejectExchange(it) },
                    onCounterOffer = { counterOfferExchange = it },
                    onComplete = { viewModel.completeExchange(it) },
                    onOpenChat = { viewModel.openChat(it) },
                    onRate = { viewModel.openRatingDialog(it) },
                    onExploreClick = { viewModel.selectTab(MainTab.INICIO) }
                )

                MainTab.PERFIL -> ProfileScreen(
                    currentUser = currentUser,
                    allUsers = allUsers,
                    myListings = myListings,
                    favoriteListings = listings.filter { it.isFavorite },
                    reviewsFlow = viewModel.getReviewsForUser(currentUser?.id ?: "user_camilo"),
                    onSwitchUser = { viewModel.switchUser(it) },
                    onListingClick = { viewModel.openListingDetail(it) },
                    onProposeClick = { viewModel.startProposeTrueque(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onOpenReportDialog = { viewModel.openReportDialog(it) }
                )
            }
        }
    }

    // --- Modal Sheets & Dialogs ---

    // 1. Listing Detail & Propose Swap Sheet
    activeListingDetail?.let { listing ->
        ListingDetailSheet(
            listing = listing,
            myListings = myListings,
            isProposing = isProposingTrueque,
            selectedOfferedListing = selectedOfferedListing,
            pitchMessage = pitchMessage,
            onPitchMessageChange = { viewModel.onPitchMessageChange(it) },
            onSelectOfferedListing = { viewModel.selectOfferedListing(it) },
            onStartPropose = { viewModel.startProposeTrueque(listing) },
            onSubmitProposal = { viewModel.submitProposal() },
            onCancelPropose = { viewModel.closeListingDetail() },
            onDismiss = { viewModel.closeListingDetail() },
            onReportOwner = { viewModel.openReportDialog(it) }
        )
    }

    // 2. Counter-Offer Dialog
    counterOfferExchange?.let { exch ->
        CounterOfferDialog(
            exchange = exch,
            onDismiss = { counterOfferExchange = null },
            onSubmit = { note ->
                viewModel.submitCounterOffer(exch.id, note)
                counterOfferExchange = null
            }
        )
    }

    // 3. Real-Time Chat Dialog
    activeChatExchange?.let { exch ->
        ChatDialog(
            exchange = exch,
            messages = chatMessages,
            currentUserId = currentUser?.id ?: "user_camilo",
            onSendMessage = { viewModel.sendChatMessage(it) },
            onDismiss = { viewModel.closeChat() }
        )
    }

    // 4. Star Rating Dialog (1 to 5 Stars)
    ratingExchange?.let { exch ->
        RatingDialog(
            exchange = exch,
            currentUserId = currentUser?.id ?: "user_camilo",
            onDismiss = { viewModel.closeRatingDialog() },
            onSubmit = { stars, comment ->
                viewModel.submitRating(stars, comment)
            }
        )
    }

    // 5. Report & Block Dialog
    reportingUser?.let { user ->
        ReportBlockDialog(
            user = user,
            onDismiss = { viewModel.closeReportDialog() },
            onSubmit = { reason, block ->
                viewModel.submitReportAndBlock(user.id, reason, block)
            }
        )
    }

    // 6. Top City Selector Dialog
    if (isTopCityPickerOpen) {
        AlertDialog(
            onDismissRequest = { isTopCityPickerOpen = false },
            title = { Text("Selecciona tu Ciudad") },
            text = {
                Column {
                    Text(
                        text = "TRUEKAPP inició en Facatativá para jóvenes y estudiantes, y se expande a más ciudades de Colombia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AppConstants.COLOMBIAN_CITIES.forEach { city ->
                        val isSelected = selectedCity == city
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.onCitySelect(city)
                                    isTopCityPickerOpen = false
                                }
                        ) {
                            Text(
                                text = if (city == "Facatativá") "📍 $city (Sede Inicial)" else city,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { isTopCityPickerOpen = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // 7. Top User Switcher Dialog
    if (isTopUserSwitcherOpen) {
        AlertDialog(
            onDismissRequest = { isTopUserSwitcherOpen = false },
            title = { Text("Cambiar Usuario Activo") },
            text = {
                Column {
                    Text(
                        text = "Cambia de perfil para probar el flujo peer-to-peer de trueques (enviar propuestas, contraofertas y chat).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    allUsers.forEach { user ->
                        val isSelected = user.id == currentUser?.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.switchUser(user.id)
                                    isTopUserSwitcherOpen = false
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
                Button(onClick = { isTopUserSwitcherOpen = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
