package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ExchangeEntity
import com.example.data.model.ListingEntity
import com.example.data.model.ReportedUserEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TruekappDao {

    // --- Listings ---
    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun getAllListingsFlow(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteListingsFlow(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getListingsByOwnerFlow(ownerId: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :id LIMIT 1")
    suspend fun getListingById(id: String): ListingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: ListingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<ListingEntity>)

    @Update
    suspend fun updateListing(listing: ListingEntity)

    @Query("UPDATE listings SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: String, isFav: Boolean)

    @Query("UPDATE listings SET status = :status WHERE id = :id")
    suspend fun updateListingStatus(id: String, status: String)

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteListing(id: String)

    // --- Exchanges ---
    @Query("SELECT * FROM exchanges ORDER BY updatedAt DESC")
    fun getAllExchangesFlow(): Flow<List<ExchangeEntity>>

    @Query("SELECT * FROM exchanges WHERE targetOwnerId = :userId OR proposerId = :userId ORDER BY updatedAt DESC")
    fun getExchangesForUser(userId: String): Flow<List<ExchangeEntity>>

    @Query("SELECT * FROM exchanges WHERE id = :id LIMIT 1")
    suspend fun getExchangeById(id: String): ExchangeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchange(exchange: ExchangeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchanges(exchanges: List<ExchangeEntity>)

    @Update
    suspend fun updateExchange(exchange: ExchangeEntity)

    @Query("UPDATE exchanges SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateExchangeStatus(id: String, status: String, updatedAt: Long)

    @Query("UPDATE exchanges SET status = 'NEGOTIATING', counterOfferNote = :note, updatedAt = :updatedAt WHERE id = :id")
    suspend fun submitCounterOffer(id: String, note: String, updatedAt: Long)

    @Query("UPDATE exchanges SET ratedByProposer = CASE WHEN proposerId = :userId THEN 1 ELSE ratedByProposer END, ratedByTarget = CASE WHEN targetOwnerId = :userId THEN 1 ELSE ratedByTarget END WHERE id = :exchangeId")
    suspend fun markRated(exchangeId: String, userId: String)

    // --- Chat ---
    @Query("SELECT * FROM chat_messages WHERE exchangeId = :exchangeId ORDER BY timestamp ASC")
    fun getMessagesForExchange(exchangeId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(messages: List<ChatMessageEntity>)

    // --- Users ---
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:identifier) OR LOWER(username) = LOWER(:identifier) LIMIT 1")
    suspend fun getUserByEmailOrUsername(identifier: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET rating = :newRating, ratingCount = :newCount WHERE id = :userId")
    suspend fun updateUserRating(userId: String, newRating: Float, newCount: Int)

    @Query("UPDATE users SET isBlocked = 1 WHERE id = :userId")
    suspend fun blockUser(userId: String)

    // --- Reviews ---
    @Query("SELECT * FROM user_reviews WHERE toUserId = :userId ORDER BY createdAt DESC")
    fun getReviewsForUser(userId: String): Flow<List<UserReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: UserReviewEntity)

    // --- Reports ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportedUserEntity)
}
