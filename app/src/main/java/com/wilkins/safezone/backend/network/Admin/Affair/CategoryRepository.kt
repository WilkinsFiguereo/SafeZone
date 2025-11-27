package com.wilkins.safezone.backend.network.Admin.Affair

import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class CategoryRepository {
    private val supabase = SupabaseService.getInstance()

    // ========== INCIDENT CATEGORIES ==========
    suspend fun getIncidentCategories(): Result<List<IncidentCategory>> {
        return try {
            val categories = supabase.from("affair_categories")
                .select()
                .decodeList<IncidentCategory>()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createIncidentCategory(name: String): Result<IncidentCategory> {
        return try {
            val category = supabase.from("affair_categories")
                .insert(IncidentCategory(name = name))
                .decodeSingle<IncidentCategory>()
            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateIncidentCategory(id: Int, name: String): Result<Unit> {
        return try {
            supabase.from("affair_categories")
                .update({ IncidentCategory::name setTo name }) {
                    filter { IncidentCategory::id eq id }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteIncidentCategory(id: Int): Result<Unit> {
        return try {
            supabase.from("affair_categories")
                .delete {
                    filter { IncidentCategory::id eq id }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== AFFAIR CATEGORIES ==========
    suspend fun getAffairCategories(): Result<List<AffairCategory>> {
        return try {
            val categories = supabase.from("affair_categories")
                .select()
                .decodeList<AffairCategory>()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAffairCategory(name: String): Result<AffairCategory> {
        return try {
            val category = supabase.from("affair_categories")
                .insert(AffairCategory(name = name))
                .decodeSingle<AffairCategory>()
            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAffairCategory(id: Int, name: String): Result<Unit> {
        return try {
            supabase.from("affair_categories")
                .update({ AffairCategory::name setTo name }) {
                    filter { AffairCategory::id eq id }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAffairCategory(id: Int): Result<Unit> {
        return try {
            supabase.from("affair_categories")
                .delete {
                    filter { AffairCategory::id eq id }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== AFFAIRS ==========
    suspend fun getAffairs(): Result<List<Affair>> {
        return try {
            val affairs = supabase.from("affair")
                .select()
                .decodeList<Affair>()
            Result.success(affairs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAffair(type: String, categoriesId: Int): Result<Affair> {
        return try {
            val affair = supabase.from("affair")
                .insert(Affair(type = type, categoriesId = categoriesId))
                .decodeSingle<Affair>()
            Result.success(affair)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAffair(id: Int, type: String, categoriesId: Int): Result<Unit> {
        return try {
            supabase.from("affair")
                .update({
                    Affair::type setTo type
                    Affair::categoriesId setTo categoriesId
                }) {
                    filter { Affair::id eq id }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAffair(id: Int): Result<Unit> {
        return try {
            supabase.from("affair")
                .delete {
                    filter { Affair::id eq id }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}