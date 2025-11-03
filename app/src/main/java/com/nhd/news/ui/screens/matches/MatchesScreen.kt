package com.nhd.news.ui.screens.matches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhd.news.data.models.Match
import com.nhd.news.ui.theme.TinTứcThểThaoTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    modifier: Modifier = Modifier,
    scrollToTopTrigger: Int = 0,
    viewModel: MatchesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Scroll to top khi double-tap
    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = "Trận đấu",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Làm mới"
                    )
                }
                IconButton(onClick = { /* TODO: Implement calendar */ }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Lịch thi đấu"
                    )
                }
            }
        )
        
        // Status Filter
        StatusFilterRow(
            selectedStatus = uiState.selectedStatus,
            onStatusSelected = { viewModel.selectStatus(it) }
        )
        
        // Content based on state
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "Có lỗi xảy ra",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            
            uiState.matches.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có trận đấu nào",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            else -> {
                // Matches List
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.matches) { match ->
                        MatchCard(match = match)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusFilterRow(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val statuses = listOf("Tất cả", "Đang diễn ra", "Đã kết thúc", "Sắp diễn ra")
    
    LazyRow(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(statuses) { status ->
            FilterChip(
                onClick = { onStatusSelected(status) },
                label = { Text(status) },
                selected = selectedStatus == status
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchCard(
    match: Match,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { /* TODO: Navigate to match detail */ }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // League and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.tournamentName ?: "Tournament",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                MatchStatusChip(
                    status = match.status ?: "scheduled",
                    time = formatMatchTime(match)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Teams and Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = match.homeTeam?.teamName ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
                
                // Score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    if (match.homeScore != null && match.awayScore != null) {
                        Text(
                            text = "${match.homeScore} - ${match.awayScore}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "vs",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Away Team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = match.awayTeam?.teamName ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

private fun formatMatchTime(match: Match): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(match.matchDate)
        date?.let { outputFormat.format(it) } ?: match.matchDate
    } catch (e: Exception) {
        match.matchDate
    }
}

@Composable
fun MatchStatusChip(
    status: String,
    time: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor, text) = when (status.lowercase()) {
        "live" -> Triple(
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onError,
            "🔴 LIVE"
        )
        "finished" -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Kết thúc"
        )
        "scheduled" -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            time
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            status
        )
    }
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MatchesScreenPreview() {
    TinTứcThểThaoTheme {
        MatchesScreen()
    }
}
