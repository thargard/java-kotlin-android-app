package com.example.newtestproject.screen.screenComponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RatingStars(
    rating: Int?,
    editable: Boolean,
    max: Int = 5,
    onRatingSelected: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..max) {
            val filled = rating != null && i <= rating
            val color = if (filled) {

                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
            val modifier = if (editable) {
                Modifier
                    .padding(end = 2.dp)
                    .clickable { onRatingSelected(i) }
            } else {
                Modifier.padding(end = 2.dp)
            }
            Text(
                text = "★",
                color = color,
                fontSize = 20.sp,
                modifier = modifier
            )
        }
    }
}