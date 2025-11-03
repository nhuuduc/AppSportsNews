package com.nhd.news.ui.components.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Fade in animation
 */
@Composable
fun FadeInAnimation(
    durationMillis: Int = 300,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis))
    ) {
        content()
    }
}

/**
 * Slide in from bottom animation
 */
@Composable
fun SlideInBottomAnimation(
    durationMillis: Int = 400,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(durationMillis),
            initialOffsetY = { it / 2 }
        ) + fadeIn(animationSpec = tween(durationMillis))
    ) {
        content()
    }
}

/**
 * Slide in from right animation
 */
@Composable
fun SlideInRightAnimation(
    durationMillis: Int = 400,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            animationSpec = tween(durationMillis),
            initialOffsetX = { it / 2 }
        ) + fadeIn(animationSpec = tween(durationMillis))
    ) {
        content()
    }
}

/**
 * Scale in animation
 */
@Composable
fun ScaleInAnimation(
    durationMillis: Int = 300,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = tween(durationMillis),
            initialScale = 0.8f
        ) + fadeIn(animationSpec = tween(durationMillis))
    ) {
        content()
    }
}

/**
 * Bounce scale animation (cho button clicks)
 */
fun Modifier.bounceClick(
    onClick: () -> Unit,
    enabled: Boolean = true
) = composed {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce_scale"
    )
    
    this
        .scale(scale)
        .clickable(
            enabled = enabled,
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick
        )
}

/**
 * Press down animation
 */
fun Modifier.pressDownAnimation(
    enabled: Boolean = true,
    onClick: () -> Unit
) = composed {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "press_scale"
    )
    
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            enabled = enabled,
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick
        )
}

/**
 * Rotate animation (cho loading indicators)
 */
@Composable
fun rememberInfiniteRotation(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )
    return rotation
}

/**
 * Pulse animation (cho notifications)
 */
@Composable
fun rememberPulseAnimation(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    return scale
}

/**
 * Fade animation giữa các states
 */
@Composable
fun <T> AnimatedContent(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    androidx.compose.animation.AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        label = "animated_content"
    ) { state ->
        content(state)
    }
}

/**
 * Slide transition giữa các màn hình
 */
fun slideInOutTransition(): ContentTransform {
    return slideInHorizontally(
        animationSpec = tween(300),
        initialOffsetX = { it }
    ) + fadeIn(animationSpec = tween(300)) togetherWith
            slideOutHorizontally(
                animationSpec = tween(300),
                targetOffsetX = { -it }
            ) + fadeOut(animationSpec = tween(300))
}

/**
 * Expand/Collapse animation
 */
@Composable
fun ExpandableContent(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        modifier = modifier,
        enter = expandVertically(
            animationSpec = tween(300),
            expandFrom = Alignment.Top
        ) + fadeIn(animationSpec = tween(300)),
        exit = shrinkVertically(
            animationSpec = tween(300),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = tween(300))
    ) {
        content()
    }
}

/**
 * Stagger animation cho list items
 */
@Composable
fun StaggeredAnimation(
    index: Int,
    delayMillis: Int = 50,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * delayMillis).toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(300),
            initialOffsetY = { it / 4 }
        ) + fadeIn(animationSpec = tween(300))
    ) {
        content()
    }
}

/**
 * Shake animation (cho error states)
 */
fun Modifier.shakeAnimation(trigger: Boolean): Modifier = composed {
    var shake by remember { mutableStateOf(false) }
    
    LaunchedEffect(trigger) {
        if (trigger) {
            shake = true
            kotlinx.coroutines.delay(500)
            shake = false
        }
    }
    
    val offsetX by animateFloatAsState(
        targetValue = if (shake) 10f else 0f,
        animationSpec = repeatable(
            iterations = 3,
            animation = tween(50),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )
    
    this.graphicsLayer {
        translationX = offsetX
    }
}

