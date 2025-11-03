package com.nhd.news.ui.components.shimmer

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shimmer effect animation
 * Tạo hiệu ứng loading sáng lấp lánh
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    colors: List<Color> = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.3f)
    )
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset(translateAnimation.value - 1000f, translateAnimation.value - 1000f),
        end = Offset(translateAnimation.value, translateAnimation.value)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

/**
 * Shimmer Box với kích thước cụ thể
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    ShimmerEffect(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height),
        shape = shape
    )
}

/**
 * Shimmer Circle (cho avatar)
 */
@Composable
fun ShimmerCircle(
    size: Dp,
    modifier: Modifier = Modifier
) {
    ShimmerEffect(
        modifier = modifier.size(size),
        shape = CircleShape
    )
}

/**
 * Article Card Shimmer
 */
@Composable
fun ArticleCardShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail
            ShimmerBox(
                width = 120.dp,
                height = 90.dp,
                shape = RoundedCornerShape(8.dp)
            )
            
            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShimmerBox(height = 16.dp)
                    ShimmerBox(width = 200.dp, height = 16.dp)
                }
                
                // Metadata
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(width = 80.dp, height = 14.dp)
                    ShimmerBox(width = 60.dp, height = 14.dp)
                }
            }
        }
    }
}

/**
 * Featured Article Card Shimmer (card lớn)
 */
@Composable
fun FeaturedArticleCardShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(300.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Image
            ShimmerBox(
                height = 180.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            
            // Content
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category badge
                ShimmerBox(width = 80.dp, height = 20.dp, shape = RoundedCornerShape(10.dp))
                
                // Title
                ShimmerBox(height = 18.dp)
                ShimmerBox(width = 250.dp, height = 18.dp)
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Metadata
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShimmerBox(width = 90.dp, height = 14.dp)
                    ShimmerBox(width = 70.dp, height = 14.dp)
                }
            }
        }
    }
}

/**
 * Match Card Shimmer
 */
@Composable
fun MatchCardShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tournament
            ShimmerBox(width = 120.dp, height = 14.dp)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Teams
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team 1
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerCircle(size = 48.dp)
                    ShimmerBox(width = 80.dp, height = 14.dp)
                }
                
                // Score
                ShimmerBox(width = 60.dp, height = 32.dp)
                
                // Team 2
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerCircle(size = 48.dp)
                    ShimmerBox(width = 80.dp, height = 14.dp)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Time/Status
            ShimmerBox(width = 100.dp, height = 14.dp)
        }
    }
}

/**
 * Video Card Shimmer
 */
@Composable
fun VideoCardShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(200.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Thumbnail with play icon
            Box {
                ShimmerBox(
                    height = 120.dp,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
            }
            
            // Content
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title
                ShimmerBox(height = 14.dp)
                ShimmerBox(width = 150.dp, height = 14.dp)
                
                // Duration & views
                ShimmerBox(width = 100.dp, height = 12.dp)
            }
        }
    }
}

/**
 * Profile Header Shimmer
 */
@Composable
fun ProfileHeaderShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        ShimmerCircle(size = 80.dp)
        
        // Name
        ShimmerBox(width = 150.dp, height = 20.dp)
        
        // Email
        ShimmerBox(width = 200.dp, height = 16.dp)
        
        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(3) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ShimmerBox(width = 60.dp, height = 24.dp)
                    ShimmerBox(width = 80.dp, height = 14.dp)
                }
            }
        }
    }
}

/**
 * Comment Item Shimmer
 */
@Composable
fun CommentItemShimmer(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        ShimmerCircle(size = 40.dp)
        
        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Username & time
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(width = 100.dp, height = 14.dp)
                ShimmerBox(width = 60.dp, height = 14.dp)
            }
            
            // Comment text
            ShimmerBox(height = 16.dp)
            ShimmerBox(width = 200.dp, height = 16.dp)
            
            // Action buttons
            ShimmerBox(width = 120.dp, height = 14.dp)
        }
    }
}

