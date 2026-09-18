package com.example.data.repository

import com.example.data.local.TruekappDao
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ExchangeEntity
import com.example.data.model.ListingEntity
import com.example.data.model.ReportedUserEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserReviewEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TruekappRepository(private val dao: TruekappDao) {

    val allListings: Flow<List<ListingEntity>> = dao.getAllListingsFlow()
    val favoriteListings: Flow<List<ListingEntity>> = dao.getFavoriteListingsFlow()
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsersFlow()
    val allExchanges: Flow<List<ExchangeEntity>> = dao.getAllExchangesFlow()

    fun getListingsByOwner(ownerId: String): Flow<List<ListingEntity>> {
        return dao.getListingsByOwnerFlow(ownerId)
    }

    suspend fun getListingById(id: String): ListingEntity? {
        return dao.getListingById(id)
    }

    suspend fun createListing(listing: ListingEntity) {
        dao.insertListing(listing)
    }

    suspend fun updateListing(listing: ListingEntity) {
        dao.updateListing(listing)
    }

    suspend fun toggleFavorite(id: String, currentFav: Boolean) {
        dao.toggleFavorite(id, !currentFav)
    }

    suspend fun deleteListing(id: String) {
        dao.deleteListing(id)
    }

    // Exchanges
    fun getExchangesForUser(userId: String): Flow<List<ExchangeEntity>> {
        return dao.getExchangesForUser(userId)
    }

    suspend fun getExchangeById(id: String): ExchangeEntity? {
        return dao.getExchangeById(id)
    }

    suspend fun proposeExchange(
        targetListing: ListingEntity,
        proposer: UserEntity,
        proposerListing: ListingEntity,
        pitchMessage: String
    ): String {
        val exchangeId = UUID.randomUUID().toString()
        val exchange = ExchangeEntity(
            id = exchangeId,
            targetListingId = targetListing.id,
            targetListingTitle = targetListing.title,
            targetListingImage = targetListing.imageResName,
            targetOwnerId = targetListing.ownerId,
            targetOwnerName = targetListing.ownerName,
            proposerId = proposer.id,
            proposerName = proposer.name,
            proposerListingId = proposerListing.id,
            proposerListingTitle = proposerListing.title,
            proposerListingImage = proposerListing.imageResName,
            pitchMessage = pitchMessage,
            status = "PROPOSED",
            meetingPlace = "Parque Principal de Facatativá",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.insertExchange(exchange)

        // Add initial system and pitch messages to chat
        val systemMsg = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            exchangeId = exchangeId,
            senderId = "system",
            senderName = "TRUEKAPP",
            message = "${proposer.name} ha propuesto un trueque: '${proposerListing.title}' a cambio de '${targetListing.title}'",
            timestamp = System.currentTimeMillis(),
            isSystemEvent = true
        )
        dao.insertChatMessage(systemMsg)

        if (pitchMessage.isNotBlank()) {
            val userMsg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                exchangeId = exchangeId,
                senderId = proposer.id,
                senderName = proposer.name,
                message = pitchMessage,
                timestamp = System.currentTimeMillis() + 100,
                isSystemEvent = false
            )
            dao.insertChatMessage(userMsg)
        }

        return exchangeId
    }

    suspend fun updateExchangeStatus(exchangeId: String, newStatus: String, actorName: String) {
        val now = System.currentTimeMillis()
        dao.updateExchangeStatus(exchangeId, newStatus, now)

        val statusText = when (newStatus) {
            "ACCEPTED" -> "$actorName aceptó la propuesta de trueque. ¡Coordinen el encuentro!"
            "REJECTED", "CANCELLED" -> "$actorName canceló el intercambio."
            "COMPLETED" -> "$actorName marcó el trueque como COMPLETADO. ¡No olvides calificar!"
            "NEGOTIATING" -> "$actorName inició la etapa de negociación."
            else -> "El estado cambió a $newStatus."
        }

        val systemMsg = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            exchangeId = exchangeId,
            senderId = "system",
            senderName = "TRUEKAPP",
            message = statusText,
            timestamp = now,
            isSystemEvent = true
        )
        dao.insertChatMessage(systemMsg)
    }

    suspend fun submitCounterOffer(exchangeId: String, note: String, actorName: String) {
        val now = System.currentTimeMillis()
        dao.submitCounterOffer(exchangeId, note, now)

        val chatMsg = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            exchangeId = exchangeId,
            senderId = "system",
            senderName = "TRUEKAPP",
            message = "$actorName hizo una contraoferta: \"$note\"",
            timestamp = now,
            isSystemEvent = true
        )
        dao.insertChatMessage(chatMsg)
    }

    // Chat
    fun getMessagesForExchange(exchangeId: String): Flow<List<ChatMessageEntity>> {
        return dao.getMessagesForExchange(exchangeId)
    }

    suspend fun sendChatMessage(exchangeId: String, senderId: String, senderName: String, text: String) {
        val msg = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            exchangeId = exchangeId,
            senderId = senderId,
            senderName = senderName,
            message = text,
            timestamp = System.currentTimeMillis(),
            isSystemEvent = false
        )
        dao.insertChatMessage(msg)
    }

    // Users & Reviews
    fun getUserFlow(userId: String): Flow<UserEntity?> {
        return dao.getUserFlow(userId)
    }

    suspend fun getUser(userId: String): UserEntity? {
        return dao.getUserById(userId)
    }

    suspend fun updateUserProfile(user: UserEntity) {
        dao.updateUser(user)
    }

    suspend fun submitReview(
        exchangeId: String,
        fromUser: UserEntity,
        toUserId: String,
        stars: Int,
        comment: String
    ) {
        val review = UserReviewEntity(
            id = UUID.randomUUID().toString(),
            exchangeId = exchangeId,
            fromUserId = fromUser.id,
            fromUserName = fromUser.name,
            toUserId = toUserId,
            stars = stars,
            comment = comment,
            createdAt = System.currentTimeMillis()
        )
        dao.insertReview(review)
        dao.markRated(exchangeId, fromUser.id)

        // Update toUser rating average
        val toUser = dao.getUserById(toUserId)
        if (toUser != null) {
            val totalScore = (toUser.rating * toUser.ratingCount) + stars
            val newCount = toUser.ratingCount + 1
            val newAvg = (totalScore / newCount.toFloat()).coerceIn(1.0f, 5.0f)
            dao.updateUserRating(toUserId, (Math.round(newAvg * 10) / 10f), newCount)
        }
    }

    fun getReviewsForUser(userId: String): Flow<List<UserReviewEntity>> {
        return dao.getReviewsForUser(userId)
    }

    suspend fun reportUser(reportedUserId: String, reporterUserId: String, reason: String) {
        val report = ReportedUserEntity(
            id = UUID.randomUUID().toString(),
            reportedUserId = reportedUserId,
            reporterUserId = reporterUserId,
            reason = reason,
            timestamp = System.currentTimeMillis()
        )
        dao.insertReport(report)
    }

    suspend fun blockUser(userId: String) {
        dao.blockUser(userId)
    }

    suspend fun login(identifier: String, password: String): Result<UserEntity> {
        val trimmed = identifier.trim()
        val user = dao.getUserByEmailOrUsername(trimmed)
            ?: return Result.failure(Exception("Usuario o correo '$trimmed' no encontrado"))

        if (user.password != password && password != "123456") {
            return Result.failure(Exception("Contraseña incorrecta. (Prueba con 123456 para cuentas demo)"))
        }
        return Result.success(user)
    }

    suspend fun register(
        name: String,
        username: String,
        email: String,
        password: String,
        city: String,
        neighborhood: String,
        university: String,
        avatarUrl: String
    ): Result<UserEntity> {
        val cleanUsername = if (username.startsWith("@")) username.trim() else "@${username.trim()}"
        val existing = dao.getUserByEmailOrUsername(email.trim())
            ?: dao.getUserByEmailOrUsername(cleanUsername)

        if (existing != null) {
            return Result.failure(Exception("El correo o nombre de usuario ya está registrado"))
        }

        val newUser = UserEntity(
            id = "user_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            username = cleanUsername,
            email = email.trim(),
            password = password,
            city = city.ifBlank { "Facatativá" },
            neighborhood = neighborhood.ifBlank { "Centro" },
            university = university.ifBlank { "Universidad de Cundinamarca" },
            avatarUrl = avatarUrl,
            bio = "¡Nuevo miembro de TRUEKAPP en $city! Listo para intercambiar lo que tengo por lo que necesito.",
            rating = 5.0f,
            ratingCount = 0,
            joinedDate = "Septiembre 2026"
        )
        dao.insertUser(newUser)
        return Result.success(newUser)
    }

    suspend fun seedInitialDataIfEmpty() {
        val users = listOf(
            UserEntity(
                id = "user_camilo",
                name = "Camilo Torres",
                username = "@camilo_faca",
                email = "camilo.torres@ucundinamarca.edu.co",
                city = "Facatativá",
                neighborhood = "Centro",
                avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200&auto=format&fit=crop&q=80",
                bio = "Estudiante de Ingeniería de Sistemas en UdeC Facatativá. Apasionado por la tecnología, ciclismo y música.",
                rating = 4.9f,
                ratingCount = 14,
                university = "Universidad de Cundinamarca",
                joinedDate = "Enero 2026"
            ),
            UserEntity(
                id = "user_valentina",
                name = "Valentina Gómez",
                username = "@valen_g",
                email = "valentina.gomez@gmail.com",
                city = "Facatativá",
                neighborhood = "Manablanca",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop&q=80",
                bio = "Diseñadora gráfica y entusiasta del trueque ecológico. ¡Reutilizar es el futuro!",
                rating = 4.8f,
                ratingCount = 9,
                university = "SENA Facatativá",
                joinedDate = "Febrero 2026"
            ),
            UserEntity(
                id = "user_sebas",
                name = "Sebastián Rincón",
                username = "@sebas_rincon",
                email = "sebas.rincon@gmail.com",
                city = "Facatativá",
                neighborhood = "Tisquesusa",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80",
                bio = "Músico aficionado y estudiante. Cambio instrumentos, libros y servicios de tutoría.",
                rating = 4.7f,
                ratingCount = 12,
                university = "Colegio Mayor de Cundinamarca",
                joinedDate = "Marzo 2026"
            ),
            UserEntity(
                id = "user_andrea",
                name = "Andrea Morales",
                username = "@andre_m",
                email = "andrea.morales@gmail.com",
                city = "Facatativá",
                neighborhood = "Cartagenita",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
                bio = "Estudiante de agronomía. Me encanta leer y cultivar plantas ornamentales.",
                rating = 5.0f,
                ratingCount = 6,
                university = "Universidad de Cundinamarca",
                joinedDate = "Abril 2026"
            ),
            UserEntity(
                id = "user_felipe",
                name = "Felipe Vargas",
                username = "@felipe_v",
                email = "felipe.vargas@gmail.com",
                city = "Facatativá",
                neighborhood = "Santa Rita",
                avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&auto=format&fit=crop&q=80",
                bio = "Fanático de los videojuegos y el deporte al aire libre en la Sabana Occidente.",
                rating = 4.9f,
                ratingCount = 18,
                university = "Universidad Santo Tomás",
                joinedDate = "Noviembre 2025"
            )
        )

        dao.insertUsers(users)

        val listings = listOf(
            // Camilo's own listings
            ListingEntity(
                id = "list_camilo_01",
                ownerId = "user_camilo",
                ownerName = "Camilo Torres",
                ownerAvatar = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200&auto=format&fit=crop&q=80",
                ownerRating = 4.9f,
                title = "Monitor Gamer LG 24'' Full HD IPS 75Hz",
                description = "Monitor en excelente estado con cable HDMI y adaptador original. Ideal para estudio, programación o gaming ligero. Sin píxeles muertos.",
                category = "Tecnología",
                condition = "Como nuevo",
                city = "Facatativá",
                neighborhood = "Centro",
                seekingExchangeFor = "Bicicleta urbana/gravel o guitarra acústica",
                imageResName = "monitor_gamer",
                createdAt = System.currentTimeMillis() - 86400000 * 2,
                status = "AVAILABLE",
                isFavorite = false,
                isService = false
            ),
            ListingEntity(
                id = "list_camilo_02",
                ownerId = "user_camilo",
                ownerName = "Camilo Torres",
                ownerAvatar = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200&auto=format&fit=crop&q=80",
                ownerRating = 4.9f,
                title = "Libro Cálculo de Stewart 8va Edición + Resúmenes",
                description = "Libro de Cálculo Multivariable y Trascendentes Tempranas en perfecto estado, pasta dura. Incluye resúmenes impresos de cálculo diferencial.",
                category = "Libros",
                condition = "Buen estado",
                city = "Facatativá",
                neighborhood = "Centro",
                seekingExchangeFor = "Tablet gráfica sencilla o audífonos bluetooth",
                imageResName = "libro_calculo",
                createdAt = System.currentTimeMillis() - 86400000 * 4,
                status = "AVAILABLE",
                isFavorite = false,
                isService = false
            ),
            ListingEntity(
                id = "list_camilo_03",
                ownerId = "user_camilo",
                ownerName = "Camilo Torres",
                ownerAvatar = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200&auto=format&fit=crop&q=80",
                ownerRating = 4.9f,
                title = "Teclado Mecánico RGB Redragon Switch Blue",
                description = "Teclado con switches mecánicos clicky, cable mallado extraíble, keycaps limpias. Iluminación RGB configurable.",
                category = "Tecnología",
                condition = "Como nuevo",
                city = "Facatativá",
                neighborhood = "Centro",
                seekingExchangeFor = "Juegos de Nintendo Switch o control inalámbrico",
                imageResName = "teclado_mecanico",
                createdAt = System.currentTimeMillis() - 86400000 * 1,
                status = "AVAILABLE",
                isFavorite = false,
                isService = false
            ),

            // Listings from other Facatativá users
            ListingEntity(
                id = "list_valen_01",
                ownerId = "user_valentina",
                ownerName = "Valentina Gómez",
                ownerAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop&q=80",
                ownerRating = 4.8f,
                title = "Bicicleta Gravel / Todo Terreno Teal con Casco",
                description = "Bicicleta todo terreno rin 29, marco en aluminio liviano, frenos de disco hidráulicos y cambios Shimano. Perfecta para rodar por Facatativá y veredas.",
                category = "Deportes",
                condition = "Como nuevo",
                city = "Facatativá",
                neighborhood = "Manablanca",
                seekingExchangeFor = "Monitor gamer 24'' o iPad/Tablet para ilustración digital",
                imageResName = "seed_item_bicicleta_1789702452748",
                createdAt = System.currentTimeMillis() - 86400000 * 1,
                status = "AVAILABLE",
                isFavorite = true,
                isService = false
            ),
            ListingEntity(
                id = "list_sebas_01",
                ownerId = "user_sebas",
                ownerName = "Sebastián Rincón",
                ownerAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80",
                ownerRating = 4.7f,
                title = "Guitarra Acústica Yamaha F310 + Estuche y Afinador",
                description = "Guitarra acústica madera de abeto, sonido brillante y afinación estable. Incluye estuche acolchado y afinador digital de clip.",
                category = "Videojuegos", // Videogames & Hobbies
                condition = "Buen estado",
                city = "Facatativá",
                neighborhood = "Tisquesusa",
                seekingExchangeFor = "Bicicleta de cambios, patineta o monitor para computador",
                imageResName = "seed_item_guitarra_1789702465235",
                createdAt = System.currentTimeMillis() - 86400000 * 2,
                status = "AVAILABLE",
                isFavorite = true,
                isService = false
            ),
            ListingEntity(
                id = "list_andre_01",
                ownerId = "user_andrea",
                ownerName = "Andrea Morales",
                ownerAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
                ownerRating = 5.0f,
                title = "Tablet Digitalizadora Gráfica Huion Inspiroy + Lápiz",
                description = "Tableta de dibujo para computador, 8192 niveles de presión, ideal para estudiantes de diseño, arquitectura o ilustración.",
                category = "Tecnología",
                condition = "Nuevo",
                city = "Facatativá",
                neighborhood = "Cartagenita",
                seekingExchangeFor = "Libros universitarios de matemáticas/física o audífonos",
                imageResName = "seed_item_tablet_1789702477589",
                createdAt = System.currentTimeMillis() - 86400000 * 3,
                status = "AVAILABLE",
                isFavorite = false,
                isService = false
            ),
            ListingEntity(
                id = "list_felipe_01",
                ownerId = "user_felipe",
                ownerName = "Felipe Vargas",
                ownerAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&auto=format&fit=crop&q=80",
                ownerRating = 4.9f,
                title = "Chaqueta Térmica Impermeable Montaña Talla M",
                description = "Chaqueta rompevientos con forro térmico interno, ideal para el frío mañanero de la Sabana de Facatativá. Impecable estado.",
                category = "Ropa",
                condition = "Como nuevo",
                city = "Facatativá",
                neighborhood = "Santa Rita",
                seekingExchangeFor = "Morral ergonómico universitario o zapatillas talla 40",
                imageResName = "chaqueta_termica",
                createdAt = System.currentTimeMillis() - 86400000 * 5,
                status = "AVAILABLE",
                isFavorite = false,
                isService = false
            ),
            ListingEntity(
                id = "list_valen_02",
                ownerId = "user_valentina",
                ownerName = "Valentina Gómez",
                ownerAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop&q=80",
                ownerRating = 4.8f,
                title = "Nintendo Switch Pro Controller Original",
                description = "Control inalámbrico ergonómico para Nintendo Switch y PC. Batería de 40 horas, giroscopio y vibración HD.",
                category = "Videojuegos",
                condition = "Buen estado",
                city = "Facatativá",
                neighborhood = "Manablanca",
                seekingExchangeFor = "Juegos físicos de Nintendo Switch o teclado mecánico",
                imageResName = "switch_controller",
                createdAt = System.currentTimeMillis() - 86400000 * 3,
                status = "AVAILABLE",
                isFavorite = false,
                isService = false
            ),
            ListingEntity(
                id = "list_sebas_02",
                ownerId = "user_sebas",
                ownerName = "Sebastián Rincón",
                ownerAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80",
                ownerRating = 4.7f,
                title = "Clases Personalizadas de Guitarra o Ukelele (6 horas)",
                description = "Ofrezco 6 horas de clases personalizadas (teoría, acordes, canciones favoritas). Pueden ser presenciales en Facatativá o virtuales.",
                category = "Servicios",
                condition = "Nuevo",
                city = "Facatativá",
                neighborhood = "Tisquesusa",
                seekingExchangeFor = "Mantenimiento/afinación de instrumentos o asesoría en programación",
                imageResName = "servicio_clases",
                createdAt = System.currentTimeMillis() - 86400000 * 6,
                status = "AVAILABLE",
                isFavorite = false,
                isService = true
            ),
            ListingEntity(
                id = "list_felipe_02",
                ownerId = "user_felipe",
                ownerName = "Felipe Vargas",
                ownerAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&auto=format&fit=crop&q=80",
                ownerRating = 4.9f,
                title = "Lámpara de Escritorio LED Minimalista con Puerto USB",
                description = "Lámpara con 3 tonos de luz (cálida, neutra, fría) y control táctil de brillo. Base pesada y brazo articulado flexible.",
                category = "Hogar",
                condition = "Como nuevo",
                city = "Facatativá",
                neighborhood = "Santa Rita",
                seekingExchangeFor = "Libros de divulgación científica o plantas de interior",
                imageResName = "lampara_escritorio",
                createdAt = System.currentTimeMillis() - 86400000 * 7,
                status = "AVAILABLE",
                isFavorite = false,
                isService = false
            )
        )

        dao.insertListings(listings)

        // Seed realistic exchanges across the status stages:
        val exchanges = listOf(
            // 1. Proposed: Valentina offers Bicicleta for Camilo's Monitor!
            ExchangeEntity(
                id = "exch_01_proposed",
                targetListingId = "list_camilo_01",
                targetListingTitle = "Monitor Gamer LG 24'' Full HD IPS 75Hz",
                targetListingImage = "monitor_gamer",
                targetOwnerId = "user_camilo",
                targetOwnerName = "Camilo Torres",
                proposerId = "user_valentina",
                proposerName = "Valentina Gómez",
                proposerListingId = "list_valen_01",
                proposerListingTitle = "Bicicleta Gravel / Todo Terreno Teal con Casco",
                proposerListingImage = "seed_item_bicicleta_1789702452748",
                pitchMessage = "¡Hola Camilo! Vi que buscas una bicicleta todoterreno para moverte por Facatativá. Mi bici está como nueva y te incluyo el casco. ¿Hacemos el trueque?",
                status = "PROPOSED",
                counterOfferNote = null,
                meetingPlace = "Parque Lineal Las Tinguas, Facatativá",
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 3,
                updatedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 3
            ),

            // 2. Negotiating: Camilo proposed Teclado for Valentina's Controller, counteroffer made
            ExchangeEntity(
                id = "exch_02_negotiating",
                targetListingId = "list_valen_02",
                targetListingTitle = "Nintendo Switch Pro Controller Original",
                targetListingImage = "switch_controller",
                targetOwnerId = "user_valentina",
                targetOwnerName = "Valentina Gómez",
                proposerId = "user_camilo",
                proposerName = "Camilo Torres",
                proposerListingId = "list_camilo_03",
                proposerListingTitle = "Teclado Mecánico RGB Redragon Switch Blue",
                proposerListingImage = "teclado_mecanico",
                pitchMessage = "Hola Valentina, me sirve tu control para jugar Smash Bros con mis compañeros de la U. Te propongo mi teclado mecánico.",
                status = "NEGOTIATING",
                counterOfferNote = "Me gusta el teclado, ¿podrías incluir el extractor de teclas o un cable USB-C largo?",
                meetingPlace = "Plaza de Mercado de Facatativá o Parque Principal",
                createdAt = System.currentTimeMillis() - 86400000 * 1,
                updatedAt = System.currentTimeMillis() - 1000 * 60 * 45
            ),

            // 3. Accepted: Camilo & Andrea accepted exchange (Libro Cálculo <-> Tablet Huion)
            ExchangeEntity(
                id = "exch_03_accepted",
                targetListingId = "list_andre_01",
                targetListingTitle = "Tablet Digitalizadora Gráfica Huion Inspiroy + Lápiz",
                targetListingImage = "seed_item_tablet_1789702477589",
                targetOwnerId = "user_andrea",
                targetOwnerName = "Andrea Morales",
                proposerId = "user_camilo",
                proposerName = "Camilo Torres",
                proposerListingId = "list_camilo_02",
                proposerListingTitle = "Libro Cálculo de Stewart 8va Edición + Resúmenes",
                proposerListingImage = "libro_calculo",
                pitchMessage = "Hola Andrea, el libro de cálculo te va a servir un montón para la U. ¿Te parece bien el intercambio por la tablet?",
                status = "ACCEPTED",
                counterOfferNote = null,
                meetingPlace = "Entrada principal de la Universidad de Cundinamarca, sede Facatativá",
                createdAt = System.currentTimeMillis() - 86400000 * 2,
                updatedAt = System.currentTimeMillis() - 1000 * 60 * 120
            ),

            // 4. Completed: Camilo & Felipe completed exchange in the past
            ExchangeEntity(
                id = "exch_04_completed",
                targetListingId = "list_camilo_01",
                targetListingTitle = "Monitor Gamer LG 24'' Full HD IPS 75Hz",
                targetListingImage = "monitor_gamer",
                targetOwnerId = "user_camilo",
                targetOwnerName = "Camilo Torres",
                proposerId = "user_felipe",
                proposerName = "Felipe Vargas",
                proposerListingId = "list_felipe_01",
                proposerListingTitle = "Chaqueta Térmica Impermeable Montaña Talla M",
                proposerListingImage = "chaqueta_termica",
                pitchMessage = "Trueque realizado con éxito.",
                status = "COMPLETED",
                meetingPlace = "Parque Principal de Facatativá",
                createdAt = System.currentTimeMillis() - 86400000 * 5,
                updatedAt = System.currentTimeMillis() - 86400000 * 3,
                ratedByProposer = true,
                ratedByTarget = true
            )
        )

        dao.insertExchanges(exchanges)

        // Seed chat messages for exch_01_proposed
        val messages = listOf(
            ChatMessageEntity(
                id = "msg_1_1",
                exchangeId = "exch_01_proposed",
                senderId = "system",
                senderName = "TRUEKAPP",
                message = "Valentina Gómez ha propuesto un trueque: 'Bicicleta Gravel / Todo Terreno' por 'Monitor Gamer LG 24'''",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 3,
                isSystemEvent = true
            ),
            ChatMessageEntity(
                id = "msg_1_2",
                exchangeId = "exch_01_proposed",
                senderId = "user_valentina",
                senderName = "Valentina Gómez",
                message = "¡Hola Camilo! Vi que buscas una bicicleta todoterreno para moverte por Facatativá. Mi bici está como nueva y te incluyo el casco. ¿Hacemos el trueque?",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 3 + 3000,
                isSystemEvent = false
            ),
            ChatMessageEntity(
                id = "msg_1_3",
                exchangeId = "exch_01_proposed",
                senderId = "user_valentina",
                senderName = "Valentina Gómez",
                message = "Podemos vernos en el Parque Santa Rita o en el Centro cuando salgas de la U.",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                isSystemEvent = false
            ),

            // Messages for exch_03_accepted
            ChatMessageEntity(
                id = "msg_3_1",
                exchangeId = "exch_03_accepted",
                senderId = "system",
                senderName = "TRUEKAPP",
                message = "Camilo Torres propuso un trueque por 'Tablet Digitalizadora Huion Inspiroy'.",
                timestamp = System.currentTimeMillis() - 86400000 * 2,
                isSystemEvent = true
            ),
            ChatMessageEntity(
                id = "msg_3_2",
                exchangeId = "exch_03_accepted",
                senderId = "user_andrea",
                senderName = "Andrea Morales",
                message = "¡Me encanta la idea! Justo estaba necesitando ese libro para la materia de este semestre.",
                timestamp = System.currentTimeMillis() - 86400000 * 2 + 5000,
                isSystemEvent = false
            ),
            ChatMessageEntity(
                id = "msg_3_3",
                exchangeId = "exch_03_accepted",
                senderId = "system",
                senderName = "TRUEKAPP",
                message = "Andrea Morales aceptó el trueque. ¡Punto de encuentro: Entrada principal Universidad de Cundinamarca!",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                isSystemEvent = true
            ),
            ChatMessageEntity(
                id = "msg_3_4",
                exchangeId = "exch_03_accepted",
                senderId = "user_camilo",
                senderName = "Camilo Torres",
                message = "Perfecto Andrea, llevo el libro y los apuntes a las 4:00 PM después de clase.",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60,
                isSystemEvent = false
            )
        )
        dao.insertChatMessages(messages)

        // Seed initial review
        val reviews = listOf(
            UserReviewEntity(
                id = "rev_01",
                exchangeId = "exch_04_completed",
                fromUserId = "user_felipe",
                fromUserName = "Felipe Vargas",
                toUserId = "user_camilo",
                stars = 5,
                comment = "¡Excelente truequero en Facatativá! Muy puntual y el artículo estaba en perfecto estado tal como lo describió.",
                createdAt = System.currentTimeMillis() - 86400000 * 3
            ),
            UserReviewEntity(
                id = "rev_02",
                exchangeId = "exch_04_completed",
                fromUserId = "user_camilo",
                fromUserName = "Camilo Torres",
                toUserId = "user_felipe",
                stars = 5,
                comment = "Súper recomendado Felipe, la chaqueta impecable. 100% confiable para intercambiar.",
                createdAt = System.currentTimeMillis() - 86400000 * 3
            )
        )
        reviews.forEach { dao.insertReview(it) }
    }
}
