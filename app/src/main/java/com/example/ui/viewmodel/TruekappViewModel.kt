package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ExchangeEntity
import com.example.data.model.ListingEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserReviewEntity
import com.example.data.repository.TruekappRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class MainTab {
    INICIO,
    BUSCAR,
    PUBLICAR,
    INTERCAMBIOS,
    PERFIL
}

data class FilterState(
    val query: String,
    val category: String,
    val city: String,
    val condition: String?
)

class TruekappViewModel(private val repository: TruekappRepository) : ViewModel() {

    // Current logged-in user
    private val _currentUserId = MutableStateFlow("user_camilo")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserEntity?> = combine(allUsers, _currentUserId) { users, id ->
        users.find { it.id == id } ?: users.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Navigation state
    private val _currentTab = MutableStateFlow(MainTab.INICIO)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Search and filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedCity = MutableStateFlow("Facatativá")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _selectedCondition = MutableStateFlow<String?>(null)
    val selectedCondition: StateFlow<String?> = _selectedCondition.asStateFlow()

    private val _onlyFavorites = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    private val _blockedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val blockedUserIds: StateFlow<Set<String>> = _blockedUserIds.asStateFlow()

    private val primaryFilters = combine(
        _searchQuery,
        _selectedCategory,
        _selectedCity,
        _selectedCondition
    ) { query, category, city, condition ->
        FilterState(query, category, city, condition)
    }

    private val secondaryFilters = combine(
        _onlyFavorites,
        _blockedUserIds
    ) { onlyFav, blocked ->
        Pair(onlyFav, blocked)
    }

    // Listings Flow
    val listings: StateFlow<List<ListingEntity>> = combine(
        repository.allListings,
        primaryFilters,
        secondaryFilters
    ) { all, pFilters, sFilters ->
        val (query, cat, city, cond) = pFilters
        val (favOnly, blocked) = sFilters

        all.filter { item ->
            val notBlocked = !blocked.contains(item.ownerId)
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true) ||
                    item.seekingExchangeFor.contains(query, ignoreCase = true)
            val matchesCategory = cat == "all" || item.category.equals(cat, ignoreCase = true)
            val matchesCity = city == "Todas" || item.city.equals(city, ignoreCase = true)
            val matchesCond = cond == null || item.condition.equals(cond, ignoreCase = true)
            val matchesFav = !favOnly || item.isFavorite

            notBlocked && matchesQuery && matchesCategory && matchesCity && matchesCond && matchesFav
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current user's own listings (to propose or manage)
    val myListings: StateFlow<List<ListingEntity>> = combine(
        repository.allListings,
        _currentUserId
    ) { all, userId ->
        all.filter { it.ownerId == userId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Exchanges for current user
    val myExchanges: StateFlow<List<ExchangeEntity>> = combine(
        repository.allExchanges,
        _currentUserId
    ) { all, userId ->
        all.filter { it.proposerId == userId || it.targetOwnerId == userId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active modals & sheets
    private val _activeListingDetail = MutableStateFlow<ListingEntity?>(null)
    val activeListingDetail: StateFlow<ListingEntity?> = _activeListingDetail.asStateFlow()

    private val _isProposingTrueque = MutableStateFlow(false)
    val isProposingTrueque: StateFlow<Boolean> = _isProposingTrueque.asStateFlow()

    private val _selectedOfferedListing = MutableStateFlow<ListingEntity?>(null)
    val selectedOfferedListing: StateFlow<ListingEntity?> = _selectedOfferedListing.asStateFlow()

    private val _pitchMessage = MutableStateFlow("")
    val pitchMessage: StateFlow<String> = _pitchMessage.asStateFlow()

    // Exchange negotiation details & chat
    private val _activeExchangeDetail = MutableStateFlow<ExchangeEntity?>(null)
    val activeExchangeDetail: StateFlow<ExchangeEntity?> = _activeExchangeDetail.asStateFlow()

    private val _activeChatExchange = MutableStateFlow<ExchangeEntity?>(null)
    val activeChatExchange: StateFlow<ExchangeEntity?> = _activeChatExchange.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessageEntity>> = _chatMessages.asStateFlow()

    // Rating & Report
    private val _ratingExchange = MutableStateFlow<ExchangeEntity?>(null)
    val ratingExchange: StateFlow<ExchangeEntity?> = _ratingExchange.asStateFlow()

    private val _reportingUser = MutableStateFlow<UserEntity?>(null)
    val reportingUser: StateFlow<UserEntity?> = _reportingUser.asStateFlow()

    // SnackBar / feedback message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // --- Navigation & Filters ---
    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: String) {
        _selectedCategory.value = category
    }

    fun onCitySelect(city: String) {
        _selectedCity.value = city
    }

    fun onConditionSelect(condition: String?) {
        _selectedCondition.value = condition
    }

    fun toggleFavoritesOnly() {
        _onlyFavorites.value = !_onlyFavorites.value
    }

    fun toggleFavorite(listing: ListingEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(listing.id, listing.isFavorite)
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // --- Switch User (Demo & Multi-User Testing) ---
    fun switchUser(userId: String) {
        _currentUserId.value = userId
        showToast("Sesión cambiada a ${allUsers.value.find { it.id == userId }?.name ?: userId}")
    }

    // --- Listing Detail & Propose Trueque ---
    fun openListingDetail(listing: ListingEntity) {
        _activeListingDetail.value = listing
    }

    fun closeListingDetail() {
        _activeListingDetail.value = null
        _isProposingTrueque.value = false
    }

    fun startProposeTrueque(targetListing: ListingEntity) {
        _activeListingDetail.value = targetListing
        _selectedOfferedListing.value = myListings.value.firstOrNull()
        _pitchMessage.value = "¡Hola ${targetListing.ownerName}! Me interesa tu '${targetListing.title}'. ¿Te gustaría hacer este intercambio?"
        _isProposingTrueque.value = true
    }

    fun selectOfferedListing(listing: ListingEntity) {
        _selectedOfferedListing.value = listing
    }

    fun onPitchMessageChange(msg: String) {
        _pitchMessage.value = msg
    }

    fun submitProposal() {
        val target = _activeListingDetail.value ?: return
        val user = currentUser.value ?: return
        val offered = _selectedOfferedListing.value

        if (offered == null) {
            showToast("Debes tener o seleccionar una publicación propia para ofrecer en trueque.")
            return
        }

        viewModelScope.launch {
            repository.proposeExchange(
                targetListing = target,
                proposer = user,
                proposerListing = offered,
                pitchMessage = _pitchMessage.value
            )
            _isProposingTrueque.value = false
            _activeListingDetail.value = null
            _currentTab.value = MainTab.INTERCAMBIOS
            showToast("¡Propuesta de trueque enviada a ${target.ownerName}!")
        }
    }

    // --- Publish Listing ---
    fun publishListing(
        title: String,
        description: String,
        category: String,
        condition: String,
        city: String,
        neighborhood: String,
        seeking: String,
        isService: Boolean
    ) {
        val user = currentUser.value ?: return
        if (title.isBlank() || description.isBlank() || seeking.isBlank()) {
            showToast("Por favor completa el título, descripción y lo que buscas a cambio.")
            return
        }

        val newListing = ListingEntity(
            id = "list_${UUID.randomUUID()}",
            ownerId = user.id,
            ownerName = user.name,
            ownerAvatar = user.avatarUrl,
            ownerRating = user.rating,
            title = title.trim(),
            description = description.trim(),
            category = category,
            condition = condition,
            city = city,
            neighborhood = neighborhood,
            seekingExchangeFor = seeking.trim(),
            imageResName = if (isService) "servicio_clases" else "seed_item_bicicleta_1789702452748",
            createdAt = System.currentTimeMillis(),
            status = "AVAILABLE",
            isFavorite = false,
            isService = isService
        )

        viewModelScope.launch {
            repository.createListing(newListing)
            _currentTab.value = MainTab.INICIO
            showToast("¡Publicación creada exitosamente en TRUEKAPP!")
        }
    }

    // --- Exchange Details & Negotiation ---
    fun openExchangeDetail(exchange: ExchangeEntity) {
        _activeExchangeDetail.value = exchange
    }

    fun closeExchangeDetail() {
        _activeExchangeDetail.value = null
    }

    fun acceptExchange(exchangeId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateExchangeStatus(exchangeId, "ACCEPTED", user.name)
            refreshActiveExchange(exchangeId)
            showToast("¡Trueque aceptado! Ahora pueden acordar el punto de entrega.")
        }
    }

    fun rejectExchange(exchangeId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateExchangeStatus(exchangeId, "REJECTED", user.name)
            refreshActiveExchange(exchangeId)
            showToast("Has rechazado el intercambio.")
        }
    }

    fun submitCounterOffer(exchangeId: String, note: String) {
        val user = currentUser.value ?: return
        if (note.isBlank()) return
        viewModelScope.launch {
            repository.submitCounterOffer(exchangeId, note, user.name)
            refreshActiveExchange(exchangeId)
            showToast("Contraoferta enviada al otro usuario.")
        }
    }

    fun completeExchange(exchangeId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateExchangeStatus(exchangeId, "COMPLETED", user.name)
            val updated = repository.getExchangeById(exchangeId)
            _activeExchangeDetail.value = updated
            showToast("¡Trueque completado con éxito! Califica a tu contraparte.")
            if (updated != null) {
                openRatingDialog(updated)
            }
        }
    }

    private suspend fun refreshActiveExchange(exchangeId: String) {
        val updated = repository.getExchangeById(exchangeId)
        _activeExchangeDetail.value = updated
    }

    // --- Chat Dialog ---
    fun openChat(exchange: ExchangeEntity) {
        _activeChatExchange.value = exchange
        viewModelScope.launch {
            repository.getMessagesForExchange(exchange.id).collect { msgs ->
                _chatMessages.value = msgs
            }
        }
    }

    fun closeChat() {
        _activeChatExchange.value = null
        _chatMessages.value = emptyList()
    }

    fun sendChatMessage(text: String) {
        val exchange = _activeChatExchange.value ?: return
        val user = currentUser.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            repository.sendChatMessage(
                exchangeId = exchange.id,
                senderId = user.id,
                senderName = user.name,
                text = text.trim()
            )
        }
    }

    // --- Rating ---
    fun openRatingDialog(exchange: ExchangeEntity) {
        _ratingExchange.value = exchange
    }

    fun closeRatingDialog() {
        _ratingExchange.value = null
    }

    fun submitRating(stars: Int, comment: String) {
        val exchange = _ratingExchange.value ?: return
        val user = currentUser.value ?: return
        val toUserId = if (exchange.proposerId == user.id) exchange.targetOwnerId else exchange.proposerId

        viewModelScope.launch {
            repository.submitReview(
                exchangeId = exchange.id,
                fromUser = user,
                toUserId = toUserId,
                stars = stars,
                comment = comment
            )
            _ratingExchange.value = null
            showToast("¡Gracias por calificar! Calificación de $stars estrellas guardada.")
        }
    }

    // --- Report & Block ---
    fun openReportDialog(userId: String) {
        val user = allUsers.value.find { it.id == userId }
        _reportingUser.value = user
    }

    fun closeReportDialog() {
        _reportingUser.value = null
    }

    fun submitReportAndBlock(userId: String, reason: String, shouldBlock: Boolean) {
        val currentUser = currentUser.value ?: return
        viewModelScope.launch {
            repository.reportUser(userId, currentUser.id, reason)
            if (shouldBlock) {
                repository.blockUser(userId)
                _blockedUserIds.value = _blockedUserIds.value + userId
                showToast("Usuario reportado y bloqueado correctamente.")
            } else {
                showToast("Reporte enviado al equipo de seguridad de TRUEKAPP.")
            }
            _reportingUser.value = null
            _activeListingDetail.value = null
        }
    }

    // --- Reviews for Profile ---
    fun getReviewsForUser(userId: String) = repository.getReviewsForUser(userId)
}

class TruekappViewModelFactory(private val repository: TruekappRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TruekappViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TruekappViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
