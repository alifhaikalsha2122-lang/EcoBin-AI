package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Eco-Tech Palette: Crisp Emerald, Forest Green, Fresh Mint, Tech Cyan, Warm Amber
val EcoGreenPrimary = Color(0xFF059669)
val EcoGreenDark = Color(0xFF064E3B)
val EcoGreenLight = Color(0xFF10B981)
val EcoMint = Color(0xFF34D399)
val EcoLime = Color(0xFF84CC16)
val EcoCyan = Color(0xFF06B6D4)

// Light Theme Accessible Eco-Green Palette (High Contrast & WCAG AA Compliant)
val EcoLightPrimary = Color(0xFF047857) // Deep accessible emerald green (Contrast ratio > 4.5:1)
val EcoLightOnPrimary = Color(0xFFFFFFFF)
val EcoLightPrimaryContainer = Color(0xFFE6F4EA) // Soft accessible light green tint
val EcoLightOnPrimaryContainer = Color(0xFF064E3B) // High contrast deep forest text

val EcoLightSecondary = Color(0xFF0D9488) // Accessible Teal
val EcoLightOnSecondary = Color(0xFFFFFFFF)
val EcoLightSecondaryContainer = Color(0xFFCCFBF1)
val EcoLightOnSecondaryContainer = Color(0xFF115E59)

val EcoLightTertiary = Color(0xFF4D7C0F) // Accessible Olive/Lime
val EcoLightOnTertiary = Color(0xFFFFFFFF)
val EcoLightTertiaryContainer = Color(0xFFECFCCB)
val EcoLightOnTertiaryContainer = Color(0xFF1A2E05)

val EcoLightBackground = Color(0xFFF8FAFC) // Clean modern light slate background
val EcoLightOnBackground = Color(0xFF0F172A)
val EcoLightSurface = Color(0xFFFFFFFF) // Pure white card surface
val EcoLightOnSurface = Color(0xFF0F172A)
val EcoLightSurfaceVariant = Color(0xFFF0FDF4) // Light soft eco-green tinted container
val EcoLightOnSurfaceVariant = Color(0xFF334155) // Accessible slate text (contrast > 5.5:1)
val EcoLightOutline = Color(0xFFCBD5E1) // Clean border outline
val EcoLightOutlineVariant = Color(0xFFE2E8F0)

// Neutral & Backgrounds (Legacy aliases for backward compatibility)
val EcoBackgroundLight = EcoLightBackground
val EcoSurfaceLight = EcoLightSurface
val EcoSurfaceVariantLight = EcoLightPrimaryContainer
val EcoOnBackgroundLight = EcoLightOnBackground
val EcoOnSurfaceLight = EcoLightOnSurface
val EcoOutlineLight = EcoLightOutline

// Dark Theme Colors
val EcoBackgroundDark = Color(0xFF0B1712)
val EcoSurfaceDark = Color(0xFF13231C)
val EcoSurfaceVariantDark = Color(0xFF1D352B)
val EcoOnBackgroundDark = Color(0xFFF1F5F9)
val EcoOnSurfaceDark = Color(0xFFE2E8F0)
val EcoOutlineDark = Color(0xFF334155)

// Category Colors
val ColorPlastic = Color(0xFF0284C7) // Blue
val ColorPaper = Color(0xFFF59E0B)   // Amber
val ColorGlass = Color(0xFF10B981)   // Green
val ColorMetal = Color(0xFF8B5CF6)   // Purple
val ColorEWaste = Color(0xFFEC4899)  // Rose
val ColorOrganic = Color(0xFF65A30D) // Olive
val ColorHazardous = Color(0xFFEF4444) // Red
val ColorGeneral = Color(0xFF64748B) // Slate
