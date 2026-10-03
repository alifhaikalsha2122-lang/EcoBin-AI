package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.theme.EcoGreenPrimary
import com.example.ui.theme.EcoMint

@Composable
fun UserProfileAvatar(
    imageIdentifier: String,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clickableModifier = if (onClick != null) {
        modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    } else {
        modifier
            .size(size)
            .clip(CircleShape)
    }

    Box(
        modifier = clickableModifier
            .background(
                Brush.linearGradient(
                    listOf(EcoGreenPrimary, EcoMint, Color(0xFF0D9488))
                )
            )
            .border(2.5.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            imageIdentifier == "logo" -> {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Profile Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            imageIdentifier.startsWith("content://") || imageIdentifier.startsWith("file://") -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(imageIdentifier))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                val emoji = when (imageIdentifier) {
                    "avatar:robot" -> "🤖"
                    "avatar:scholar" -> "🧑‍🎓"
                    "avatar:nature" -> "🌿"
                    "avatar:farmer" -> "🧑‍🌾"
                    "avatar:fox" -> "🦊"
                    "avatar:panda" -> "🐼"
                    "avatar:cyclist" -> "🚴"
                    "avatar:earth" -> "🌍"
                    "avatar:butterfly" -> "🦋"
                    "avatar:superhero" -> "🦸"
                    "avatar:hero" -> "🧕"
                    else -> if (imageIdentifier.startsWith("avatar:")) "🌱" else imageIdentifier.ifBlank { "👤" }
                }
                val fontSize = (size.value * 0.48f).sp
                Text(text = emoji, fontSize = fontSize)
            }
        }
    }
}
