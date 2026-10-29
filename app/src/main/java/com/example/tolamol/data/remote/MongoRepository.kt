package com.example.tolamol.data.remote

import com.example.tolamol.BuildConfig
import com.example.tolamol.data.local.ItemRate
import com.example.tolamol.viewmodel.CashCounterState
import com.example.tolamol.viewmodel.QuickBillingState
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document
import java.util.UUID

data class UserAccount(
    val userId: String,
    val email: String,
    val shopName: String,
    val shopUpiId: String
)

class MongoRepository {

    private fun getNamedCollection(collectionName: String): MongoCollection<Document>? {
        return try {
            val uri = BuildConfig.MONGODB_URI
            if (uri.isBlank()) return null
            val client = MongoClient.create(uri)
            val database = client.getDatabase("DATA")
            database.getCollection<Document>(collectionName)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    private fun getCollection(): MongoCollection<Document>? {
        return getNamedCollection("mainData")
    }

    suspend fun registerUser(emailInput: String, passwordInput: String, shopNameInput: String, shopUpiInput: String): UserAccount {
        val collection = getNamedCollection("users") ?: throw Exception("Database connection failed")
        val cleanEmail = emailInput.trim().lowercase()
        val existing = collection.find(Filters.eq("email", cleanEmail)).firstOrNull()
        if (existing != null) {
            throw Exception("User with this email already exists!")
        }
        val generatedUserId = "usr_" + UUID.randomUUID().toString().replace("-", "").take(10)
        val doc = Document()
            .append("userId", generatedUserId)
            .append("email", cleanEmail)
            .append("password", passwordInput.trim())
            .append("shopName", shopNameInput.trim())
            .append("shopUpiId", shopUpiInput.trim())
            .append("createdAt", System.currentTimeMillis())

        collection.insertOne(doc)
        return UserAccount(generatedUserId, cleanEmail, shopNameInput.trim(), shopUpiInput.trim())
    }

    suspend fun loginUser(emailInput: String, passwordInput: String): UserAccount {
        val collection = getNamedCollection("users") ?: throw Exception("Database connection failed")
        val cleanEmail = emailInput.trim().lowercase()
        val userDoc = collection.find(Filters.and(
            Filters.eq("email", cleanEmail),
            Filters.eq("password", passwordInput.trim())
        )).firstOrNull() ?: throw Exception("Invalid email or password!")

        val userId = userDoc.getString("userId") ?: ("usr_" + userDoc.getObjectId("_id").toString().take(10))
        val email = userDoc.getString("email") ?: cleanEmail
        val shopName = userDoc.getString("shopName") ?: ""
        val shopUpiId = userDoc.getString("shopUpiId") ?: ""

        return UserAccount(userId, email, shopName, shopUpiId)
    }

    suspend fun syncSavedItemsForUser(userId: String, items: List<ItemRate>) {
        if (userId.isBlank()) {
            syncLocalToRemote(items)
            return
        }
        try {
            val collection = getNamedCollection("savedItems") ?: return
            collection.deleteMany(Filters.eq("userId", userId))

            if (items.isNotEmpty()) {
                val documents = items.map { (id, name, pricePerKg) ->
                    Document()
                        .append("userId", userId)
                        .append("id", id)
                        .append("name", name)
                        .append("pricePerKg", pricePerKg)
                }
                collection.insertMany(documents)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    suspend fun fetchSavedItemsForUser(userId: String): List<ItemRate> {
        if (userId.isBlank()) {
            return fetchRemoteItems()
        }
        return try {
            val collection = getNamedCollection("savedItems") ?: return emptyList()
            val documents = collection.find(Filters.eq("userId", userId)).toList()
            documents.map { doc ->
                ItemRate(
                    id = getInt(doc, "id", "localId"),
                    name = doc.getString("name") ?: "",
                    pricePerKg = getDouble(doc, "pricePerKg"),
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun saveCashSummaryForUser(userId: String, state: CashCounterState) {
        if (userId.isBlank()) return
        try {
            val collection = getNamedCollection("cashSummary") ?: return
            collection.deleteMany(Filters.eq("userId", userId))

            val doc = Document()
                .append("userId", userId)
                .append("count500", state.count500)
                .append("count200", state.count200)
                .append("count100", state.count100)
                .append("count50", state.count50)
                .append("count20", state.count20)
                .append("count10", state.count10)
                .append("coins", state.coins)
                .append("totalNotes", state.totalNotes)
                .append("grandTotal", state.grandTotal)
                .append("updatedAt", System.currentTimeMillis())

            collection.insertOne(doc)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    suspend fun fetchCashSummaryForUser(userId: String): CashCounterState? {
        if (userId.isBlank()) return null
        return try {
            val collection = getNamedCollection("cashSummary") ?: return null
            val doc = collection.find(Filters.eq("userId", userId)).firstOrNull() ?: return null
            CashCounterState(
                count500 = doc.getString("count500") ?: "",
                count200 = doc.getString("count200") ?: "",
                count100 = doc.getString("count100") ?: "",
                count50 = doc.getString("count50") ?: "",
                count20 = doc.getString("count20") ?: "",
                count10 = doc.getString("count10") ?: "",
                coins = doc.getString("coins") ?: "",
                totalNotes = getInt(doc, "totalNotes"),
                grandTotal = getInt(doc, "grandTotal")
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveQuickBillForUser(userId: String, state: QuickBillingState) {
        if (userId.isBlank() || state.items.isEmpty()) return
        try {
            val collection = getNamedCollection("quickBills") ?: return
            val itemsDocs = state.items.map {
                Document().append("name", it.name).append("quantity", it.quantity).append("subtotal", it.subtotal)
            }
            val doc = Document()
                .append("userId", userId)
                .append("items", itemsDocs)
                .append("discount", state.discount)
                .append("grandTotal", state.grandTotal)
                .append("createdAt", System.currentTimeMillis())

            collection.insertOne(doc)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    suspend fun syncLocalToRemote(items: List<ItemRate>) {
        try {
            val collection = getCollection() ?: return
            
            // Clear remote collection to push the new state
            collection.drop()

            if (items.isNotEmpty()) {
                val documents = items.map { (id, name, pricePerKg) ->
                    Document()
                        .append("id", id)
                        .append("name", name)
                        .append("pricePerKg", pricePerKg)
                }
                collection.insertMany(documents)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    suspend fun fetchRemoteItems(): List<ItemRate> {
        return try {
            val collection = getCollection() ?: return emptyList()
            val documents = collection.find().toList()
            documents.map { doc ->
                ItemRate(
                    id = getInt(doc, "id", "localId"),
                    name = doc.getString("name") ?: "",
                    pricePerKg = getDouble(doc, "pricePerKg"),
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun getInt(doc: Document, vararg keys: String): Int {
        for (key in keys) {
            val value = doc[key]
            when (value) {
                is Number -> return value.toInt()
                is String -> value.toIntOrNull()?.let { return it }
            }
        }
        return 0
    }

    private fun getDouble(doc: Document, key: String): Double {
        return when (val value = doc[key]) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }
}
