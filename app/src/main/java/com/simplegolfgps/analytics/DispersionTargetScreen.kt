package com.simplegolfgps.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.simplegolfgps.data.DirectionToTarget
import com.simplegolfgps.data.DistanceToTarget

private val fairwayGreen = Color(0xFF1B5E20)
private val ringColor = Color(0x55FFFFFF)
private val pinColor = Color.White

private fun heatColor(count: Int, maxCount: Int): Color {
    if (count == 0 || maxCount == 0) return Color.Transparent
    val ratio = count.toFloat() / maxCount
    return when {
        ratio <= 0.25f -> Color(0x80FFFF00)
        ratio <= 0.50f -> Color(0xB0FFA500)
        ratio <= 0.75f -> Color(0xD0FF4500)
        else -> Color(0xF0FF0000)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispersionTargetScreen(
    dispersion: DispersionGrid?,
    filterState: FilterState,
    onUpdateFilter: (FilterState.() -> FilterState) -> Unit,
    onClearFilters: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shot Dispersion") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            // Club filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EnumFilterDropdown(
                    label = "Club",
                    selected = filterState.club,
                    entries = emptyList<String>(),
                    displayName = { it },
                    onSelect = { club -> onUpdateFilter { copy(club = club) } },
                )
                if (filterState.hasActiveFilters) {
                    TextButton(onClick = onClearFilters) {
                        Text("Clear")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (dispersion != null) {
                DispersionTargetCanvas(
                    grid = dispersion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Summary stats
                DispersionSummary(dispersion)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "No direction/distance data yet.\nRecord shots with Direction to Target and Distance to Target fields.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun DispersionTargetCanvas(
    grid: DispersionGrid,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val textSizePx = with(density) { 12.dp.toPx() }
    val labelSizePx = with(density) { 10.dp.toPx() }

    val dirLabels = DirectionToTarget.entries.map { it.displayName }
    val distLabels = DistanceToTarget.entries.map { it.displayName }

    Canvas(modifier = modifier) {
        val leftMargin = 52.dp.toPx()
        val topMargin = 28.dp.toPx()
        val gridSize = minOf(size.width - leftMargin, size.height - topMargin)
        val cellSize = gridSize / 5f

        val gridLeft = leftMargin
        val gridTop = topMargin

        // Dark green background
        drawRect(
            color = fairwayGreen,
            topLeft = Offset(gridLeft, gridTop),
            size = Size(cellSize * 5, cellSize * 5),
        )

        // Heat map cells
        for (row in 0 until 5) {
            for (col in 0 until 5) {
                val count = grid.cells[row][col]
                val color = heatColor(count, grid.maxCount)
                if (count > 0) {
                    drawRect(
                        color = color,
                        topLeft = Offset(gridLeft + col * cellSize, gridTop + row * cellSize),
                        size = Size(cellSize, cellSize),
                    )
                }
            }
        }

        // Grid lines
        for (i in 0..5) {
            // Vertical
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(gridLeft + i * cellSize, gridTop),
                end = Offset(gridLeft + i * cellSize, gridTop + 5 * cellSize),
                strokeWidth = 1.dp.toPx(),
            )
            // Horizontal
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(gridLeft, gridTop + i * cellSize),
                end = Offset(gridLeft + 5 * cellSize, gridTop + i * cellSize),
                strokeWidth = 1.dp.toPx(),
            )
        }

        // Target rings
        val centerX = gridLeft + 2.5f * cellSize
        val centerY = gridTop + 2.5f * cellSize

        // Outer ring - covers full 5x5
        drawCircle(
            color = ringColor,
            radius = 2.5f * cellSize,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx()),
        )
        // Middle ring - covers 3x3 center
        drawCircle(
            color = ringColor,
            radius = 1.5f * cellSize,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx()),
        )
        // Inner ring - covers center cell (bullseye)
        drawCircle(
            color = ringColor,
            radius = 0.5f * cellSize,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx()),
        )

        // Pin marker at center
        drawCircle(
            color = pinColor,
            radius = 4.dp.toPx(),
            center = Offset(centerX, centerY),
        )

        // Shot count text in each cell
        val textPaint = android.graphics.Paint().apply {
            this.color = android.graphics.Color.WHITE
            this.textSize = textSizePx
            this.textAlign = android.graphics.Paint.Align.CENTER
            this.isFakeBoldText = true
            this.isAntiAlias = true
        }

        for (row in 0 until 5) {
            for (col in 0 until 5) {
                val count = grid.cells[row][col]
                if (count > 0) {
                    val cx = gridLeft + col * cellSize + cellSize / 2
                    val cy = gridTop + row * cellSize + cellSize / 2
                    drawContext.canvas.nativeCanvas.drawText(
                        count.toString(),
                        cx,
                        cy + textSizePx / 3,
                        textPaint,
                    )
                }
            }
        }

        // Axis labels - direction across top
        val labelPaint = android.graphics.Paint().apply {
            this.color = android.graphics.Color.GRAY
            this.textSize = labelSizePx
            this.textAlign = android.graphics.Paint.Align.CENTER
            this.isAntiAlias = true
        }

        dirLabels.forEachIndexed { col, label ->
            val cx = gridLeft + col * cellSize + cellSize / 2
            drawContext.canvas.nativeCanvas.drawText(
                label,
                cx,
                topMargin - 8.dp.toPx(),
                labelPaint,
            )
        }

        // Axis labels - distance down left side
        val leftLabelPaint = android.graphics.Paint().apply {
            this.color = android.graphics.Color.GRAY
            this.textSize = labelSizePx
            this.textAlign = android.graphics.Paint.Align.RIGHT
            this.isAntiAlias = true
        }

        distLabels.forEachIndexed { row, label ->
            val cy = gridTop + row * cellSize + cellSize / 2
            drawContext.canvas.nativeCanvas.drawText(
                label,
                leftMargin - 6.dp.toPx(),
                cy + labelSizePx / 3,
                leftLabelPaint,
            )
        }
    }
}

@Composable
private fun DispersionSummary(grid: DispersionGrid) {
    val bullseyeCount = grid.cells[2][2]
    val bullseyePct = if (grid.totalShots > 0) bullseyeCount.toFloat() / grid.totalShots else 0f

    // Inner ring = 3x3 center (rows 1-3, cols 1-3)
    var innerCount = 0
    for (r in 1..3) {
        for (c in 1..3) {
            innerCount += grid.cells[r][c]
        }
    }
    val innerPct = if (grid.totalShots > 0) innerCount.toFloat() / grid.totalShots else 0f

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader("Summary")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                CircularScoreIndicator(
                    label = "Bullseye",
                    score = bullseyePct,
                    modifier = Modifier.weight(1f),
                )
                CircularScoreIndicator(
                    label = "Inner Ring",
                    score = innerPct,
                    modifier = Modifier.weight(1f),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        grid.totalShots.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Total Shots",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
