package com.example.apk_mock.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.ui.theme.TaskPointTheme

@Composable
fun appTextFieldColors(
    focusedContainerColor: Color = TaskPointTheme.colors.fieldBackground,
    unfocusedContainerColor: Color = TaskPointTheme.colors.fieldBackground,
    errorContainerColor: Color = TaskPointTheme.colors.fieldBackground,
    focusedBorderColor: Color = TaskPointTheme.colors.primary,
    unfocusedBorderColor: Color = TaskPointTheme.colors.fieldBorder,
    errorBorderColor: Color = TaskPointTheme.colors.destructive,
    focusedTextColor: Color = TaskPointTheme.colors.textPrimary,
    unfocusedTextColor: Color = TaskPointTheme.colors.textPrimary,
    errorTextColor: Color = TaskPointTheme.colors.textPrimary,
    cursorColor: Color = TaskPointTheme.colors.primary
): TextFieldColors {
    val colors = TaskPointTheme.colors

    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = focusedContainerColor,
        unfocusedContainerColor = unfocusedContainerColor,
        errorContainerColor = errorContainerColor,
        focusedBorderColor = focusedBorderColor,
        unfocusedBorderColor = unfocusedBorderColor,
        errorBorderColor = errorBorderColor,
        focusedTextColor = focusedTextColor,
        unfocusedTextColor = unfocusedTextColor,
        errorTextColor = errorTextColor,
        cursorColor = cursorColor,
        focusedPlaceholderColor = colors.placeholder,
        unfocusedPlaceholderColor = colors.placeholder,
        errorPlaceholderColor = colors.placeholder
    )
}

@Composable
fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val colors = TaskPointTheme.colors

    Column {
        if (label.isNotBlank()) {
            Text(
                text = label,
                color = colors.label,
                fontSize = 17.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = colors.placeholder, fontSize = 18.sp) },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = isError,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = appTextFieldColors(),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 19.sp,
                color = colors.textPrimary
            )
        )
        if (isError && errorMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(errorMessage, color = colors.destructive, fontSize = 17.sp)
        }
    }
}

@Composable
fun AppTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null,
    maxLength: Int? = null,
    counterFontSize: TextUnit = 11.sp,
    errorFontSize: TextUnit = 12.sp
) {
    val colors = TaskPointTheme.colors

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            onValueChange(maxLength?.let { newValue.take(it) } ?: newValue)
        },
        modifier = modifier,
        placeholder = { Text(placeholder, color = colors.placeholder, fontSize = 14.sp) },
        isError = isError,
        shape = RoundedCornerShape(12.dp),
        colors = appTextFieldColors(),
        textStyle = LocalTextStyle.current.copy(
            fontSize = 18.sp,
            color = colors.textPrimary
        )
    )

    if ((isError && errorMessage != null) || maxLength != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isError && errorMessage != null) {
                Text(errorMessage, color = colors.destructive, fontSize = errorFontSize)
            } else {
                Spacer(Modifier.width(1.dp))
            }

            maxLength?.let {
                Text("${value.length}/$it", color = colors.textSecondary, fontSize = counterFontSize)
            }
        }
    }
}
