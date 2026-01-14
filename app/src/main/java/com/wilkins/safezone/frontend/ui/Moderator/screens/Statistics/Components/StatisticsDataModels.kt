package com.wilkins.safezone.frontend.ui.Moderator.screens.Statistics.Components


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class StatCardData(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color,
    val trend: String,
    val trendUp: Boolean
)

data class ReportStatItem(
    val label: String,
    val value: Int,
    val total: Int,
    val color: Color
)

data class NewsStatItem(
    val label: String,
    val value: Int,
    val icon: ImageVector,
    val color: Color
)

data class SurveyStatItem(
    val label: String,
    val value: Int,
    val icon: ImageVector,
    val color: Color,
    val suffix: String = ""
)