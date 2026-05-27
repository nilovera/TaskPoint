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
import com.example.apk_mock.ui.theme.AccentBlue
import com.example.apk_mock.ui.theme.ErrorFieldBg
import com.example.apk_mock.ui.theme.ErrorRed
import com.example.apk_mock.ui.theme.FieldBorder
import com.example.apk_mock.ui.theme.LabelGray
import com.example.apk_mock.ui.theme.PlaceholderGray
import com.example.apk_mock.ui.theme.SubtitleGray
import com.example.apk_mock.ui.theme.SurfaceField

@Composable
fun appTextFieldColors(
    focusedContainerColor: Color = SurfaceField,
    unfocusedContainerColor: Color = SurfaceField,
    errorContainerColor: Color = SurfaceField,
    focusedBorderColor: Color = AccentBlue,
    unfocusedBorderColor: Color = FieldBorder,
    errorBorderColor: Color = ErrorRed,
    focusedTextColor: Color = Color.White,
    unfocusedTextColor: Color = Color.White,
    errorTextColor: Color = Color.White,
    cursorColor: Color = AccentBlue
): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = focusedContainerColor,
    unfocusedContainerColor = unfocusedContainerColor,
    errorContainerColor = errorContainerColor,
    focusedBorderColor = focusedBorderColor,
    unfocusedBorderColor = unfocusedBorderColor,
    errorBorderColor = errorBorderColor,
    focusedTextColor = focusedTextColor,
    unfocusedTextColor = unfocusedTextColor,
    errorTextColor = errorTextColor,
    cursorColor = cursorColor
)

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
    Column {
        if (label.isNotBlank()) {
            Text(
                text = label,
                color = LabelGray,
                fontSize = 17.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = PlaceholderGray, fontSize = 18.sp) },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = isError,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = appTextFieldColors(),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 19.sp,
                color = Color.White
            )
        )
        if (isError && errorMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(errorMessage, color = ErrorRed, fontSize = 17.sp)
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
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            onValueChange(maxLength?.let { newValue.take(it) } ?: newValue)
        },
        modifier = modifier,
        placeholder = { Text(placeholder, color = PlaceholderGray, fontSize = 14.sp) },
        isError = isError,
        shape = RoundedCornerShape(12.dp),
        colors = appTextFieldColors(),
        textStyle = LocalTextStyle.current.copy(
            fontSize = 18.sp,
            color = Color.White
        )
    )

    if ((isError && errorMessage != null) || maxLength != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isError && errorMessage != null) {
                Text(errorMessage, color = ErrorRed, fontSize = errorFontSize)
            } else {
                Spacer(Modifier.width(1.dp))
            }

            maxLength?.let {
                Text("${value.length}/$it", color = SubtitleGray, fontSize = counterFontSize)
            }
        }
    }
}
