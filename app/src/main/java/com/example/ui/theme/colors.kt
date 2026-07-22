package com.example.ui.theme

import androidx.compose.ui.graphics.Color

object TechHorizonColors {
    object Light {
        val background = Color(0xFFF8FAFC)
        val surface = Color(0xFFFFFFFF)
        val surfaceVariant = Color(0xFFF1F5F9)
        val textPrimary = Color(0xFF0F172A)
        val textSecondary = Color(0xFF64748B)
        val primary = Color(0xFF4F46E5) // Electric Indigo
        val secondary = Color(0xFF0EA5E9) // Vivid Cyan
        val accent = Color(0xFFF59E0B) // Amber Gold
        val success = Color(0xFF10B981) // Emerald Green
        val border = Color(0xFFE2E8F0)
        val borderFocused = Color(0xFF818CF8)
        val error = Color(0xFFEF4444)
    }

    object Dark {
        val background = Color(0xFF0B0F19) // Obsidian Deep
        val surface = Color(0xFF111827) // Slate Charcoal
        val surfaceVariant = Color(0xFF1F2937)
        val textPrimary = Color(0xFFF9FAFB)
        val textSecondary = Color(0xFF9CA3AF)
        val primary = Color(0xFF6366F1) // Bright Indigo
        val secondary = Color(0xFF38BDF8) // Electric Cyan
        val accent = Color(0xFFFBBF24) // Bright Amber
        val success = Color(0xFF34D399) // Mint Emerald
        val border = Color(0xFF374151)
        val borderFocused = Color(0xFF818CF8)
        val error = Color(0xFFF87171)
    }

    object Roles {
        val studentLight = Color(0xFF0284C7)
        val studentDark = Color(0xFF38BDF8)

        val tutorLight = Color(0xFF7C3AED)
        val tutorDark = Color(0xFFA78BFA)

        val adminLight = Color(0xFFD97706)
        val adminDark = Color(0xFFFBBF24)
    }

    object Gradients {
        val PrimaryIndigo = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
        val OceanCyan = listOf(Color(0xFF0EA5E9), Color(0xFF2563EB))
        val SunsetAmber = listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
        val EmeraldMint = listOf(Color(0xFF10B981), Color(0xFF059669))
        val DarkGlass = listOf(Color(0xFF1F2937).copy(alpha = 0.9f), Color(0xFF111827).copy(alpha = 0.95f))
    }
}

