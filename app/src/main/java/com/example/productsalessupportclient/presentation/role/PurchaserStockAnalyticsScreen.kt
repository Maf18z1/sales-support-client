package com.example.productsalessupportclient.presentation.role

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productsalessupportclient.data.network.CategoryHistoryPointResponse
import com.example.productsalessupportclient.data.network.ClientManagerDashboardApi
import com.example.productsalessupportclient.data.network.DashboardApi
import com.example.productsalessupportclient.data.network.ReportApi
import com.example.productsalessupportclient.data.network.StockApi
import com.example.productsalessupportclient.data.repository.AuthSession
import com.example.productsalessupportclient.data.repository.PurchaserStockRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaserStockAnalyticsScreen(
    session: AuthSession,
    onBack: () -> Unit
) {
    val repository = remember {
        PurchaserStockRepository(
            StockApi(), ReportApi()
        )
    }

    val vm: PurchaserStockAnalyticsViewModel = viewModel(
        factory = PurchaserStockAnalyticsViewModelFactory(repository, session.token)
    )

    val state = vm.uiState

    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var historyMode by rememberSaveable { mutableStateOf("DAY") }

    LaunchedEffect(Unit) {
        vm.loadCategories()
    }

    LaunchedEffect(state.categories) {
        if (selectedCategory.isBlank() && state.categories.isNotEmpty()) {
            selectedCategory = state.categories.first()
        }
    }

    LaunchedEffect(selectedCategory) {
        if (selectedCategory.isNotBlank()) {
            vm.loadHistory(selectedCategory)
        }
    }

    val historyBuckets = remember(state.history, historyMode) {
        when (historyMode) {
            "MONTH" -> buildStockAnalyticsMonthlyBuckets(state.history, LocalDate.now(), monthsBack = 12)
            else -> buildStockAnalyticsDailyBuckets(state.history, LocalDate.now(), daysBack = 14)
        }
    }

    val forecastBuckets = remember(state.history) {
        buildStockAnalyticsForecastBuckets(
            history = state.history,
            startDate = LocalDate.now().plusDays(1),
            daysCount = 7
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        Text(
            text = "Аналитика остатков",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        StockAnalyticsCategoryDropdown(
            categories = state.categories,
            selected = selectedCategory,
            onSelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = "Продажи по выбранной категории",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = historyMode == "DAY",
                onClick = { historyMode = "DAY" },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("Дни")
            }

            SegmentedButton(
                selected = historyMode == "MONTH",
                onClick = { historyMode = "MONTH" },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("Месяцы")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        StockAnalyticsBarChartCard(
            points = historyBuckets,
            yAxisLabel = "Продажи",
            xAxisLabel = "Дата"
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Прогноз спроса на неделю",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        StockAnalyticsBarChartCard(
            points = forecastBuckets,
            yAxisLabel = "Спрос",
            xAxisLabel = "Дата"
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = onBack) {
            Text("Назад")
        }
    }
}

@Composable
private fun StockAnalyticsCategoryDropdown(
    categories: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(
                text = selected.ifBlank { "Выберите категорию" },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        expanded = false
                        onSelected(category)
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun StockAnalyticsBarChartCard(
    points: List<StockAnalyticsBucket>,
    yAxisLabel: String,
    xAxisLabel: String
) {
    if (points.isEmpty()) {
        Text("Нет данных для графика")
        return
    }

    val maxValue = max(1, points.maxOf { it.value })
    var selectedBucket by remember(points) {
        mutableStateOf(points.lastOrNull())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F5FF))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            selectedBucket?.let { bucket ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "${bucket.tooltipLabel} • $yAxisLabel: ${bucket.value}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = yAxisLabel,
                        modifier = Modifier.rotate(-90f),
                        maxLines = 1,
                        softWrap = false,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(points) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            selectedBucket = stockAnalyticsBucketAtOffset(
                                                x = offset.x,
                                                totalWidth = size.width,
                                                buckets = points
                                            )
                                        },
                                        onDrag = { change, _ ->
                                            selectedBucket = stockAnalyticsBucketAtOffset(
                                                x = change.position.x,
                                                totalWidth = size.width,
                                                buckets = points
                                            )
                                        }
                                    )
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val axisColor = Color(0xFFB0B0B0)
                                val stroke = 3f
                                val arrowSize = 18f
                                val bottomAxisPadding = 18f
                                val xAxisY = size.height - bottomAxisPadding

                                drawLine(
                                    color = axisColor,
                                    start = Offset(0f, xAxisY),
                                    end = Offset(0f, 0f),
                                    strokeWidth = stroke
                                )

                                drawLine(
                                    color = axisColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(-arrowSize / 2f, arrowSize),
                                    strokeWidth = stroke
                                )
                                drawLine(
                                    color = axisColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(arrowSize / 2f, arrowSize),
                                    strokeWidth = stroke
                                )

                                drawLine(
                                    color = axisColor,
                                    start = Offset(0f, xAxisY),
                                    end = Offset(size.width, xAxisY),
                                    strokeWidth = stroke
                                )

                                drawLine(
                                    color = axisColor,
                                    start = Offset(size.width, xAxisY),
                                    end = Offset(size.width - arrowSize, xAxisY - arrowSize / 2f),
                                    strokeWidth = stroke
                                )
                                drawLine(
                                    color = axisColor,
                                    start = Offset(size.width, xAxisY),
                                    end = Offset(size.width - arrowSize, xAxisY + arrowSize / 2f),
                                    strokeWidth = stroke
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        start = 8.dp,
                                        bottom = 18.dp
                                    ),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                points.forEach { bucket ->
                                    val fraction = bucket.value.toFloat() / maxValue.toFloat()
                                    val barHeight = (180f * fraction)
                                        .coerceAtLeast(if (bucket.value == 0) 6f else 10f)
                                        .dp

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 3.dp)
                                            .fillMaxHeight(),
                                        verticalArrangement = Arrangement.Bottom,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(barHeight)
                                                .background(
                                                    color = if (selectedBucket == bucket) {
                                                        Color(0xFF5B3FE8)
                                                    } else {
                                                        Color(0xFF7C5CFF)
                                                    },
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = xAxisLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

private fun stockAnalyticsBucketAtOffset(
    x: Float,
    totalWidth: Int,
    buckets: List<StockAnalyticsBucket>
): StockAnalyticsBucket? {
    if (buckets.isEmpty() || totalWidth <= 0) return null

    val index = ((x / totalWidth.toFloat()) * buckets.size)
        .toInt()
        .coerceIn(0, buckets.lastIndex)

    return buckets.getOrNull(index)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun buildStockAnalyticsDailyBuckets(
    points: List<CategoryHistoryPointResponse>,
    today: LocalDate,
    daysBack: Int
): List<StockAnalyticsBucket> {
    val grouped = points
        .mapNotNull { point ->
            runCatching { LocalDate.parse(point.date) }
                .getOrNull()
                ?.let { it to point.quantity }
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, values) -> values.sum() }

    val startDate = today.minusDays(daysBack - 1L)

    return (0 until daysBack).map { offset ->
        val date = startDate.plusDays(offset.toLong())
        StockAnalyticsBucket(
            label = date.dayOfMonth.toString(),
            tooltipLabel = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
            value = grouped[date] ?: 0
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun buildStockAnalyticsMonthlyBuckets(
    points: List<CategoryHistoryPointResponse>,
    today: LocalDate,
    monthsBack: Int
): List<StockAnalyticsBucket> {
    val grouped = points
        .mapNotNull { point ->
            runCatching { LocalDate.parse(point.date) }
                .getOrNull()
                ?.let { it.withDayOfMonth(1) to point.quantity }
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, values) -> values.sum() }

    val endMonth = YearMonth.from(today)
    val startMonth = endMonth.minusMonths(monthsBack - 1L)

    return (0 until monthsBack).map { offset ->
        val ym = startMonth.plusMonths(offset.toLong())
        val monthDate = ym.atDay(1)
        val monthName = ym.month.name.lowercase().replaceFirstChar { it.uppercase() }

        StockAnalyticsBucket(
            label = monthName,
            tooltipLabel = "$monthName ${ym.year}",
            value = grouped[monthDate] ?: 0
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun buildStockAnalyticsForecastBuckets(
    history: List<CategoryHistoryPointResponse>,
    startDate: LocalDate,
    daysCount: Int
): List<StockAnalyticsBucket> {
    val parsed = history.mapNotNull { point ->
        runCatching { LocalDate.parse(point.date) }
            .getOrNull()
            ?.let { it to point.quantity }
    }

    if (parsed.isEmpty()) {
        return (0 until daysCount).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            StockAnalyticsBucket(
                label = date.dayOfMonth.toString(),
                tooltipLabel = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                value = 0
            )
        }
    }

    val weekdayAverages = parsed
        .groupBy({ it.first.dayOfWeek.value }, { it.second })
        .mapValues { (_, values) -> values.average() }

    return (0 until daysCount).map { offset ->
        val date = startDate.plusDays(offset.toLong())
        val avg = weekdayAverages[date.dayOfWeek.value] ?: 0.0

        StockAnalyticsBucket(
            label = date.dayOfMonth.toString(),
            tooltipLabel = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
            value = avg.roundToInt()
        )
    }
}

private data class StockAnalyticsBucket(
    val label: String,
    val tooltipLabel: String,
    val value: Int
)