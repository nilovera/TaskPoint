package com.example.apk_mock.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.ui.theme.ErrorRed
import com.example.apk_mock.ui.theme.LabelGray

@Composable
fun FormFieldLabel(
    text: String,
    modifier: Modifier = Modifier,
    required: Boolean = false
) {
    Text(
        text = buildAnnotatedString {
            append(text)
            if (required) {
                withStyle(SpanStyle(color = ErrorRed, fontWeight = FontWeight.Bold)) {
                    append(" *")
                }
            }
        },
        color = LabelGray,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(bottom = 8.dp)
    )
}
