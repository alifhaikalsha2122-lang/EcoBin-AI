package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.CommunityPost
import com.example.model.CommunityPostType
import com.example.model.Screen
import com.example.ui.EcoViewModel
import com.example.ui.theme.*
import com.example.util.EcoShareUtils

@Composable
fun CommunityScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.repository.language.collectAsState()
    val isBm = language == AppLanguage.BM
    val allPosts by viewModel.repository.communityPosts.collectAsState()
    val selectedFeedType by viewModel.selectedFeedType.collectAsState()

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var expandedCommentsPostId by remember { mutableStateOf<String?>(null) }
    var commentInputs by remember { mutableStateOf(mapOf<String, String>()) }

    // Filter posts by selected type
    val filteredPosts = remember(selectedFeedType, allPosts) {
        if (selectedFeedType == CommunityPostType.ALL) {
            allPosts
        } else {
            allPosts.filter { it.type == selectedFeedType }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("community_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
    ) {
        // Screen Header & Create Post Action
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isBm) "Komuniti Lestari" else "School Eco Feed",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EcoGreenPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "SMK Bintang",
                                    color = EcoGreenPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isBm)
                                "Sertai kempen kitar semula, semak kemas kini papan skor terkini, dan kongsi aktiviti hijau warga sekolah."
                            else
                                "Join recycling campaigns, track live leaderboard updates, and explore environmental student activities.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    FilledTonalButton(
                        onClick = { showCreatePostDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("create_post_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Post", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isBm) "Kongsi" else "Post", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Social Feed Category Filters
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedFeedType == CommunityPostType.ALL,
                        onClick = { viewModel.setFeedTypeFilter(CommunityPostType.ALL) },
                        label = { Text(if (isBm) "Semua Feed (${allPosts.size})" else "All Feed (${allPosts.size})", fontSize = 11.sp) },
                        leadingIcon = { Text("🌐", fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_feed_all")
                    )
                }

                item {
                    FilterChip(
                        selected = selectedFeedType == CommunityPostType.CAMPAIGN,
                        onClick = {
                            viewModel.setFeedTypeFilter(
                                if (selectedFeedType == CommunityPostType.CAMPAIGN) CommunityPostType.ALL else CommunityPostType.CAMPAIGN
                            )
                        },
                        label = { Text(if (isBm) "📢 Kempen Sekolah" else "📢 Campaigns", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_feed_campaign")
                    )
                }

                item {
                    FilterChip(
                        selected = selectedFeedType == CommunityPostType.LEADERBOARD,
                        onClick = {
                            viewModel.setFeedTypeFilter(
                                if (selectedFeedType == CommunityPostType.LEADERBOARD) CommunityPostType.ALL else CommunityPostType.LEADERBOARD
                            )
                        },
                        label = { Text(if (isBm) "🏆 Papan Skor" else "🏆 Leaderboard", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_feed_leaderboard")
                    )
                }

                item {
                    FilterChip(
                        selected = selectedFeedType == CommunityPostType.ACTIVITY,
                        onClick = {
                            viewModel.setFeedTypeFilter(
                                if (selectedFeedType == CommunityPostType.ACTIVITY) CommunityPostType.ALL else CommunityPostType.ACTIVITY
                            )
                        },
                        label = { Text(if (isBm) "🌱 Aktiviti Hijau" else "🌱 Eco Activities", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_feed_activity")
                    )
                }
            }
        }

        // Feed Items List
        items(filteredPosts, key = { it.id }) { post ->
            val isCommentsExpanded = expandedCommentsPostId == post.id
            val currentCommentText = commentInputs[post.id] ?: ""

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("community_post_card_${post.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Author Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        val avatarBg = when (post.type) {
                            CommunityPostType.CAMPAIGN -> Color(0xFFE0F2FE)
                            CommunityPostType.LEADERBOARD -> Color(0xFFFEF3C7)
                            CommunityPostType.ACTIVITY, CommunityPostType.ALL -> EcoGreenPrimary.copy(alpha = 0.15f)
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(avatarBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(post.avatarEmoji, fontSize = 20.sp)
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = post.author,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "${post.role} • ${if (isBm) post.timeAgoBm else post.timeAgo}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Type Pill Badge
                        val (typeBadgeColor, typeBadgeText) = when (post.type) {
                            CommunityPostType.CAMPAIGN -> Color(0xFF0284C7) to (if (isBm) "Kempen" else "Campaign")
                            CommunityPostType.LEADERBOARD -> Color(0xFFD97706) to (if (isBm) "Papan Skor" else "Leaderboard")
                            CommunityPostType.ACTIVITY, CommunityPostType.ALL -> EcoGreenPrimary to (if (isBm) "Aktiviti" else "Activity")
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = typeBadgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = typeBadgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = typeBadgeColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Post Text Content
                    Text(
                        text = if (isBm) post.contentBm else post.contentEn,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    // Hashtag
                    Text(
                        text = post.tag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // ==========================================
                    // TYPE SPECIFIC SIMULATED SOCIAL WIDGETS
                    // ==========================================

                    // 1. School Recycling Campaign Progress Widget
                    if (post.type == CommunityPostType.CAMPAIGN && post.campaignProgress != null) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF0FDF4),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🎯", fontSize = 14.sp)
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = if (isBm) "Kemajuan Sasaran Sekolah" else "School Target Progress",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = EcoGreenPrimary
                                        )
                                    }

                                    post.campaignDaysLeft?.let { days ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFEF3C7)
                                        ) {
                                            Text(
                                                text = if (isBm) "Tinggal $days hari" else "$days days left",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB45309),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { post.campaignProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = EcoGreenPrimary,
                                    trackColor = EcoGreenPrimary.copy(alpha = 0.2f)
                                )

                                Spacer(Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = post.campaignTargetText ?: "",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (isBm) "Jom Kitar Semula!" else "Keep Recycling!",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EcoGreenPrimary
                                    )
                                }
                            }
                        }
                    }

                    // 2. Leaderboard Update Widget
                    if (post.type == CommunityPostType.LEADERBOARD && post.leaderboardPoints != null) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFFBEB),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🥇", fontSize = 24.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isBm) "Kedudukan Terkini Papan Skor" else "Live Leaderboard Standing",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF92400E)
                                        )
                                        Text(
                                            text = post.leaderboardPoints,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = { viewModel.navigateTo(Screen.LEADERBOARD) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.testTag("view_leaderboard_action_${post.id}")
                                ) {
                                    Text(
                                        text = if (isBm) "Papan Skor →" else "View Board →",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Environmental Activity Impact Badge Widget
                    if (post.type == CommunityPostType.ACTIVITY && post.impactBadge != null) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFECFDF5)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌱", fontSize = 12.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${if (isBm) "Impak Hijau" else "Green Impact"}: ${post.impactBadge}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    // Social Action Row: Like, Comment, Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Like Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.repository.likePost(post.id) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("like_button_${post.id}")
                        ) {
                            Icon(
                                imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (post.isLiked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${post.likes} ${if (isBm) "Sorakan" else "Cheers"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (post.isLiked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Comment Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    expandedCommentsPostId = if (isCommentsExpanded) null else post.id
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("comment_button_${post.id}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Comment,
                                contentDescription = "Comments",
                                tint = if (isCommentsExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${post.comments.size} ${if (isBm) "Komen" else "Comments"}",
                                fontSize = 12.sp,
                                color = if (isCommentsExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Share Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    EcoShareUtils.shareCommunityPost(
                                        context = context,
                                        author = post.author,
                                        content = if (isBm) post.contentBm else post.contentEn,
                                        tag = post.tag,
                                        language = language
                                    )
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("share_button_${post.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isBm) "Kongsi" else "Share",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Expandable Comments Section
                    AnimatedVisibility(visible = isCommentsExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .testTag("comments_section_${post.id}")
                        ) {
                            HorizontalDivider()
                            Spacer(Modifier.height(10.dp))

                            Text(
                                text = if (isBm) "Komen Komuniti (${post.comments.size}):" else "Community Comments (${post.comments.size}):",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            )

                            Spacer(Modifier.height(8.dp))

                            if (post.comments.isEmpty()) {
                                Text(
                                    text = if (isBm) "Jadilah orang pertama memberi sokongan!" else "Be the first to leave a cheer!",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                post.comments.forEach { comment ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = comment.author,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = comment.timeAgo,
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                            Text(
                                                text = comment.role,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(Modifier.height(3.dp))
                                            Text(
                                                text = comment.text,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // Comment Input Field Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = currentCommentText,
                                    onValueChange = { commentInputs = commentInputs + (post.id to it) },
                                    placeholder = {
                                        Text(
                                            if (isBm) "Tulis komen sokongan anda..." else "Write an encouraging comment...",
                                            fontSize = 11.sp
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("comment_input_${post.id}")
                                )

                                Spacer(Modifier.width(6.dp))

                                IconButton(
                                    onClick = {
                                        if (currentCommentText.isNotBlank()) {
                                            viewModel.addPostComment(post.id, currentCommentText)
                                            commentInputs = commentInputs + (post.id to "")
                                        }
                                    },
                                    modifier = Modifier.testTag("send_comment_${post.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send Comment",
                                        tint = EcoGreenPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create New Post Dialog
    if (showCreatePostDialog) {
        var postContent by remember { mutableStateOf("") }
        var postTag by remember { mutableStateOf("#SMKBintangEco") }
        var postType by remember { mutableStateOf(CommunityPostType.ACTIVITY) }

        AlertDialog(
            onDismissRequest = { showCreatePostDialog = false },
            icon = { Text("✍️", fontSize = 28.sp) },
            title = {
                Text(
                    text = if (isBm) "Cipta Catatan Komuniti" else "Create Community Post",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBm) "Pilih Kategori Catatan:" else "Select Post Category:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = postType == CommunityPostType.ACTIVITY,
                            onClick = { postType = CommunityPostType.ACTIVITY },
                            label = { Text(if (isBm) "🌱 Aktiviti" else "Activity", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = postType == CommunityPostType.CAMPAIGN,
                            onClick = { postType = CommunityPostType.CAMPAIGN },
                            label = { Text(if (isBm) "📢 Kempen" else "Campaign", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = postType == CommunityPostType.LEADERBOARD,
                            onClick = { postType = CommunityPostType.LEADERBOARD },
                            label = { Text(if (isBm) "🏆 Skor" else "Scores", fontSize = 10.sp) }
                        )
                    }

                    OutlinedTextField(
                        value = postContent,
                        onValueChange = { postContent = it },
                        label = { Text(if (isBm) "Apa yang anda kitar semula hari ini?" else "What did you recycle today?") },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_post_content_input")
                    )

                    OutlinedTextField(
                        value = postTag,
                        onValueChange = { postTag = it },
                        label = { Text("Hashtag (cth: #WiraPlastik)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_post_tag_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postContent.isNotBlank()) {
                            viewModel.publishCommunityPost(postContent, postType, postTag)
                            showCreatePostDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    modifier = Modifier.testTag("confirm_create_post_button")
                ) {
                    Text(if (isBm) "Terbitkan" else "Publish", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePostDialog = false }) {
                    Text(if (isBm) "Batal" else "Cancel")
                }
            }
        )
    }
}
