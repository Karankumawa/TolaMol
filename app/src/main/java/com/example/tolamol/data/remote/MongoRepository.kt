package com.example.tolamol.data.remote

import com.example.tolamol.BuildConfig
import com.example.tolamol.data.local.ItemRate
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.toList
import org.bson.Document

class MongoRepository {

    private fun getCollection(): MongoCollection<Document>? {
        return try {
            val uri = BuildConfig.MONGODB_URI
            if (uri.isBlank()) return null
            val client = MongoClient.create(uri)
            val database = client.getDatabase("DATA")
            database.getCollection<Document>("mainData")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    suspend fun syncLocalToRemote(items: List<ItemRate>) {
        if (items.isEmpty()) return
        try {
            val collection = getCollection() ?: return
            
            // Clear remote collection to push the new state
            collection.drop()
            
            val documents = items.map { (id, name, pricePerKg) ->
                Document()
                    .append("id", id)
                    .append("name", name)
                    .append("pricePerKg", pricePerKg)
            }
            
            collection.insertMany(documents)
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
