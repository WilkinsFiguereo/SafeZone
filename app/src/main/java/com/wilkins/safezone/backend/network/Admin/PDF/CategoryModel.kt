package com.wilkins.safezone.backend.network.Admin.PDF

import android.util.Log
import com.wilkins.safezone.backend.network.SupabaseService
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo para las categorías principales (affair_categories)
 */
@Serializable
data class AffairCategory(
    val id: Int,
    val name: String
)

/**
 * Modelo combinado de affair con su categoría
 */
data class AffairWithCategory(
    val affair: Affair,
    val category: AffairCategory?,
    val reportCount: Int = 0
)

/**
 * Modelo para reportes agrupados por affair
 */
data class ReportsByAffair(
    val affair: Affair,
    val category: AffairCategory?,
    val reports: List<ReportData>,
    val count: Int = reports.size
)

/**
 * Modelo para reportes agrupados por categoría principal
 */
data class ReportsByCategory(
    val category: AffairCategory,
    val affairs: List<AffairWithCategory>,
    val totalReports: Int = affairs.sumOf { it.reportCount }
)

/**
 * Repositorio para obtener categorías y affairs
 */
class CategoryRepository {
    private val client = SupabaseService.getInstance()

    /**
     * Obtiene todas las categorías principales (affair_categories)
     */
    suspend fun getAllCategories(): List<AffairCategory> {
        return try {
            Log.d("CategoryRepository", "🔍 Obteniendo todas las categorías principales...")

            val categories = client.postgrest
                .from("affair_categories")
                .select()
                .decodeList<AffairCategory>()

            Log.d("CategoryRepository", "✅ Categorías obtenidas: ${categories.size}")

            if (categories.isNotEmpty()) {
                Log.d("CategoryRepository", """
                    📊 Primera categoría: 
                    - ID: ${categories[0].id}
                    - Nombre: ${categories[0].name}
                """.trimIndent())
            }

            categories
        } catch (e: Exception) {
            Log.e("CategoryRepository", "❌ Error obteniendo categorías: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtiene todos los affairs (tipos de incidencias)
     */
    suspend fun getAllAffairs(): List<Affair> {
        return try {
            Log.d("CategoryRepository", "🔍 Obteniendo todos los affairs...")

            val affairs = client.postgrest
                .from("affair")
                .select()
                .decodeList<Affair>()

            Log.d("CategoryRepository", "✅ Affairs obtenidos: ${affairs.size}")

            if (affairs.isNotEmpty()) {
                Log.d("CategoryRepository", """
                    📊 Primer affair: 
                    - ID: ${affairs[0].id}
                    - Tipo: ${affairs[0].type}
                    - Category ID: ${affairs[0].category_Id}
                """.trimIndent())
            }

            affairs
        } catch (e: Exception) {
            Log.e("CategoryRepository", "❌ Error obteniendo affairs: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtiene una categoría por ID
     */
    suspend fun getCategoryById(categoryId: Int): AffairCategory? {
        return try {
            val category = client.postgrest
                .from("affair_categories")
                .select {
                    filter {
                        eq("id", categoryId)
                    }
                }
                .decodeSingleOrNull<AffairCategory>()

            Log.d("CategoryRepository", "✅ Categoría obtenida: ${category?.name ?: "No encontrada"}")
            category
        } catch (e: Exception) {
            Log.e("CategoryRepository", "❌ Error obteniendo categoría $categoryId: ${e.message}", e)
            null
        }
    }

    /**
     * Obtiene un affair por ID
     */
    suspend fun getAffairById(affairId: Int): Affair? {
        return try {
            val affair = client.postgrest
                .from("affair")
                .select {
                    filter {
                        eq("id", affairId)
                    }
                }
                .decodeSingleOrNull<Affair>()

            Log.d("CategoryRepository", "✅ Affair obtenido: ${affair?.type ?: "No encontrado"}")
            affair
        } catch (e: Exception) {
            Log.e("CategoryRepository", "❌ Error obteniendo affair $affairId: ${e.message}", e)
            null
        }
    }

    /**
     * Obtiene affairs con sus categorías y conteo de reportes
     */
    suspend fun getAffairsWithCategories(reportRepository: ReportRepository): List<AffairWithCategory> {
        return try {
            val affairs = getAllAffairs()
            val categories = getAllCategories()
            val allReports = reportRepository.getAllReports()

            val affairsWithCategories = affairs.map { affair ->
                val category = categories.find { it.id == affair.category_Id }
                val reportCount = allReports.count { it.idAffair == affair.id }

                AffairWithCategory(
                    affair = affair,
                    category = category,
                    reportCount = reportCount
                )
            }.sortedByDescending { it.reportCount }

            Log.d("CategoryRepository", "✅ Affairs con categorías: ${affairsWithCategories.size}")
            affairsWithCategories
        } catch (e: Exception) {
            Log.e("CategoryRepository", "❌ Error obteniendo affairs con categorías: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Obtiene reportes agrupados por affair
     */
    suspend fun getReportsGroupedByAffair(reportRepository: ReportRepository): List<ReportsByAffair> {
        return try {
            val affairs = getAllAffairs()
            val categories = getAllCategories()
            val allReports = reportRepository.getAllReports()

            val grouped = affairs.mapNotNull { affair ->
                val affairReports = allReports.filter { it.idAffair == affair.id }
                if (affairReports.isNotEmpty()) {
                    val category = categories.find { it.id == affair.category_Id }
                    ReportsByAffair(
                        affair = affair,
                        category = category,
                        reports = affairReports,
                        count = affairReports.size
                    )
                } else {
                    null
                }
            }.sortedByDescending { it.count }

            Log.d("CategoryRepository", "✅ Affairs con reportes: ${grouped.size}")
            grouped
        } catch (e: Exception) {
            Log.e("CategoryRepository", "❌ Error agrupando reportes por affair: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Obtiene reportes agrupados por categoría principal
     */
    suspend fun getReportsGroupedByCategory(reportRepository: ReportRepository): List<ReportsByCategory> {
        return try {
            val categories = getAllCategories()
            val affairs = getAllAffairs()
            val allReports = reportRepository.getAllReports()

            val grouped = categories.mapNotNull { category ->
                // Obtener todos los affairs de esta categoría
                val categoryAffairs = affairs.filter { it.category_Id == category.id }

                // Para cada affair, contar sus reportes
                val affairsWithCounts = categoryAffairs.map { affair ->
                    val reportCount = allReports.count { it.idAffair == affair.id }
                    AffairWithCategory(
                        affair = affair,
                        category = category,
                        reportCount = reportCount
                    )
                }.filter { it.reportCount > 0 }

                if (affairsWithCounts.isNotEmpty()) {
                    ReportsByCategory(
                        category = category,
                        affairs = affairsWithCounts,
                        totalReports = affairsWithCounts.sumOf { it.reportCount }
                    )
                } else {
                    null
                }
            }.sortedByDescending { it.totalReports }

            Log.d("CategoryRepository", "✅ Categorías con reportes: ${grouped.size}")
            grouped.forEach { group ->
                Log.d("CategoryRepository", "   - ${group.category.name}: ${group.totalReports} reportes en ${group.affairs.size} tipos")
            }

            grouped
        } catch (e: Exception) {
            Log.e("CategoryRepository", "❌ Error agrupando reportes por categoría: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Cuenta reportes por categoría principal
     */
    suspend fun countReportsByCategory(reportRepository: ReportRepository): Map<AffairCategory, Int> {
        return try {
            val categories = getAllCategories()
            val affairs = getAllAffairs()
            val allReports = reportRepository.getAllReports()

            val counts = categories.associateWith { category ->
                val categoryAffairs = affairs.filter { it.category_Id == category.id }
                val affairIds = categoryAffairs.map { it.id }
                allReports.count { it.idAffair in affairIds }
            }

            Log.d("CategoryRepository", "📊 Conteo de reportes por categoría:")
            counts.forEach { (category, count) ->
                Log.d("CategoryRepository", "   - ${category.name}: $count reportes")
            }

            counts
        } catch (e: Exception) {
            Log.e("CategoryRepository", "❌ Error contando reportes por categoría: ${e.message}", e)
            emptyMap()
        }
    }
}