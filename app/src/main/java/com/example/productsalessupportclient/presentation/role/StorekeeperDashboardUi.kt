package com.example.productsalessupportclient.presentation.role

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

private const val TITLE_COL_WEIGHT = 2.2f
private const val QTY_COL_WEIGHT = 1.0f
private const val UPDATED_COL_WEIGHT = 1.8f

@Composable
fun <T> DashboardSection(
    modifier: Modifier = Modifier,
    title: String,
    titleFontSize: TextUnit = 12.sp,
    headers: List<String>,
    headerFontSize: TextUnit = 7.sp,
    items: List<T>,
    emptyText: String,
    rowContent: @Composable (index: Int, item: T) -> Unit
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = titleFontSize
            )
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 6.dp))

        androidx.compose.material3.Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = Color(0xFFF8F5FF)
            )
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                HeaderRow(
                    headers = headers,
                    headerFontSize = headerFontSize
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 6.dp))

                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emptyText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 8.sp),
                            color = Color.Gray
                        )
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(items) { index, item ->
                            rowContent(index, item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderRow(
    headers: List<String>,
    headerFontSize: TextUnit = 7.sp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFE8E1FF),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        headers.forEachIndexed { index, header ->
            val weight = when (index) {
                0 -> TITLE_COL_WEIGHT
                1 -> QTY_COL_WEIGHT
                else -> UPDATED_COL_WEIGHT
            }

            Box(
                modifier = Modifier.weight(weight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = header,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = headerFontSize,
                        lineHeight = 0.9.em
                    ),
                    color = Color(0xFF3E2C6B),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}