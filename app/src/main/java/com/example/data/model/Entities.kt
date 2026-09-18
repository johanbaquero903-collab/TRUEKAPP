package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val username: String,
    val email: String,
    val city: String = "Facatativá",
    val neighborhood: String = "Centro",
    val avatarUrl: String = "",
    val bio: String = "",
    val rating: Float = 5.0f,
    val ratingCount: Int = 0,
    val university: String = "Universidad de Cundinamarca",
    val joinedDate: String = "Septiembre 2026",
    val isBlocked: Boolean = false
)

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val ownerName: String,
    val ownerAvatar: String = "",
    val ownerRating: Float = 5.0f,
    val title: String,
    val description: String,
    val category: String, // Technology, clothing, home, books, sports, videogames, services, other
    val condition: String, // Nuevo, Como nuevo, Buen estado, Usado
    val city: String = "Facatativá",
    val neighborhood: String = "Centro",
    val seekingExchangeFor: String, // Lo que buscan a cambio
    val imageResName: String = "", // Drawable resource name or uri
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "AVAILABLE", // AVAILABLE, TRADED, RESERVED
    val isFavorite: Boolean = false,
    val isService: Boolean = false
)

@Entity(tableName = "exchanges")
data class ExchangeEntity(
    @PrimaryKey val id: String,
    val targetListingId: String, // Listing that was proposed on
    val targetListingTitle: String,
    val targetListingImage: String,
    val targetOwnerId: String,
    val targetOwnerName: String,
    val proposerId: String, // User who made the proposal
    val proposerName: String,
    val proposerListingId: String, // Listing offered by proposer
    val proposerListingTitle: String,
    val proposerListingImage: String,
    val pitchMessage: String,
    val status: String = "PROPOSED", // PROPOSED, NEGOTIATING, ACCEPTED, COMPLETED, CANCELLED
    val counterOfferNote: String? = null,
    val meetingPlace: String? = "Parque Principal de Facatativá",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val ratedByProposer: Boolean = false,
    val ratedByTarget: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val exchangeId: String,
    val senderId: String,
    val senderName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSystemEvent: Boolean = false
)

@Entity(tableName = "user_reviews")
data class UserReviewEntity(
    @PrimaryKey val id: String,
    val exchangeId: String,
    val fromUserId: String,
    val fromUserName: String,
    val toUserId: String,
    val stars: Int, // 1 to 5
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reported_users")
data class ReportedUserEntity(
    @PrimaryKey val id: String,
    val reportedUserId: String,
    val reporterUserId: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
