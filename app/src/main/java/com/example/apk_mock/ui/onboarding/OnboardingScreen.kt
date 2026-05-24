package com.example.apk_mock.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

private val OnboardingBackground = Color(0xFF080B14)
private val TitleWhite = Color(0xFFFAFAFF)
private val BodyText = Color(0xFF969DBA)
private val MutedText = Color(0xFF9BA0BC)
private val IndicatorInactive = Color(0xFF2E3358)
private val SecondaryBorder = Color(0xFF242A5A)
private val PillGray = Color(0xFF282B35)

private data class OnboardingPage(
    val eyebrow: String,
    val titleLead: String,
    val titleAccent: String,
    val description: String,
    val accent: Color,
    val primaryText: String = "Siguiente",
    val secondaryText: String = "Saltar introducci\u00f3n"
)

private val onboardingPages = listOf(
    OnboardingPage(
        eyebrow = "ORGANIZA",
        titleLead = "Tus tareas,\nsiempre ",
        titleAccent = "bajo control",
        description = "Cre\u00e1 recordatorios simples o asociados a una ubicaci\u00f3n. Task Point te ayuda a no olvidar nada en tu d\u00eda a d\u00eda.",
        accent = Color(0xFF5278FF)
    ),
    OnboardingPage(
        eyebrow = "UBICACION",
        titleLead = "Recordatorios\ndonde los ",
        titleAccent = "necesit\u00e1s",
        description = "Asoci\u00e1 una tarea a un punto en el mapa. Task Point verifica si est\u00e1s cerca y te avisa en el momento justo.",
        accent = Color(0xFF32C86A)
    ),
    OnboardingPage(
        eyebrow = "CAPTURA",
        titleLead = "Sac\u00e1 una foto,\nno te ",
        titleAccent = "olvid\u00e9s m\u00e1s",
        description = "Adjunt\u00e1 una imagen a cualquier tarea para tener contexto visual. Ideal para objetos, documentos o lugares.",
        accent = Color(0xFFE14965)
    ),
    OnboardingPage(
        eyebrow = "SIEMPRE ACTIVO",
        titleLead = "Disponible\nincluso sin ",
        titleAccent = "conexi\u00f3n",
        description = "Tus tareas siempre disponibles aunque no tengas internet. Cuando vuelve la conexi\u00f3n, se sincroniza solo.",
        accent = Color(0xFF8148E6),
        primaryText = "Empezar ahora \u2197",
        secondaryText = "Ya tengo una cuenta"
    )
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    onSkip: () -> Unit,
    onLogin: () -> Unit
) {
    var pageIndex by rememberSaveable { mutableStateOf(0) }
    val page = onboardingPages[pageIndex]

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingBackground)
    ) {
        val scrollState = rememberScrollState()
        val compactHeight = maxHeight < 760.dp
        val heroHeight = if (maxHeight < 700.dp) 255.dp else 330.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (compactHeight) Modifier.verticalScroll(scrollState) else Modifier)
                .systemBarsPadding()
                .padding(horizontal = 32.dp)
                .padding(top = if (compactHeight) 14.dp else 24.dp, bottom = 24.dp)
        ) {
            OnboardingHero(
                pageIndex = pageIndex,
                accent = page.accent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight)
            )

            Spacer(Modifier.height(if (compactHeight) 14.dp else 22.dp))

            Text(
                text = page.eyebrow,
                color = page.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 5.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = buildAnnotatedString {
                    append(page.titleLead)
                    withStyle(SpanStyle(color = page.accent)) {
                        append(page.titleAccent)
                    }
                },
                color = TitleWhite,
                fontSize = 34.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(17.dp))
            Text(
                text = page.description,
                color = BodyText,
                fontSize = 16.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.Normal
            )

            if (compactHeight) {
                Spacer(Modifier.height(24.dp))
            } else {
                Spacer(Modifier.weight(1f))
            }

            PageIndicator(
                selectedIndex = pageIndex,
                selectedColor = page.accent,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (pageIndex == onboardingPages.lastIndex) {
                        onFinish()
                    } else {
                        pageIndex += 1
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = page.accent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = page.primaryText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(13.dp))

            OutlinedButton(
                onClick = {
                    if (pageIndex == onboardingPages.lastIndex) onLogin() else onSkip()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.3.dp, SecondaryBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MutedText
                )
            ) {
                Text(
                    text = page.secondaryText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PageIndicator(
    selectedIndex: Int,
    selectedColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(onboardingPages.size) { index ->
            Box(
                modifier = Modifier
                    .size(width = if (index == selectedIndex) 27.dp else 8.dp, height = 5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (index == selectedIndex) selectedColor else IndicatorInactive)
            )
        }
    }
}

@Composable
private fun OnboardingHero(
    pageIndex: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (pageIndex) {
            0 -> TasksHero(accent)
            1 -> LocationHero(accent)
            2 -> CameraHero(accent)
            else -> OfflineHero(accent)
        }
    }
}

@Composable
private fun TasksHero(accent: Color) {
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.52f, size.height * 0.53f)
            val radius = min(size.width, size.height) * 0.39f
            val cardWidth = 112.dp.toPx()
            val cardHeight = 88.dp.toPx()
            val cardTopLeft = Offset(center.x - cardWidth * 0.42f, center.y - cardHeight * 0.47f)

            drawCircle(Color(0xFF11163A), radius, center)
            drawCircle(OnboardingBackground, radius * 0.75f, center)
            drawRoundRect(
                color = Color(0xFF1A2044),
                topLeft = cardTopLeft,
                size = Size(cardWidth, cardHeight),
                cornerRadius = CornerRadius(18.dp.toPx())
            )
            drawLine(
                color = Color(0xFF3E4C9D),
                start = Offset(cardTopLeft.x + 16.dp.toPx(), cardTopLeft.y + 22.dp.toPx()),
                end = Offset(cardTopLeft.x + 84.dp.toPx(), cardTopLeft.y + 22.dp.toPx()),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF344282),
                start = Offset(cardTopLeft.x + 16.dp.toPx(), cardTopLeft.y + 39.dp.toPx()),
                end = Offset(cardTopLeft.x + 72.dp.toPx(), cardTopLeft.y + 39.dp.toPx()),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF2D3975),
                start = Offset(cardTopLeft.x + 16.dp.toPx(), cardTopLeft.y + 56.dp.toPx()),
                end = Offset(cardTopLeft.x + 60.dp.toPx(), cardTopLeft.y + 56.dp.toPx()),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(accent, 17.dp.toPx(), Offset(cardTopLeft.x + cardWidth - 16.dp.toPx(), cardTopLeft.y + 5.dp.toPx()))
        }

        StatusPill(
            text = "3 pendientes",
            dotColor = accent,
            containerColor = Color(0xFF171B49),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-2).dp, y = 10.dp)
        )
        StatusPill(
            text = "2 completadas hoy",
            dotColor = Color(0xFF44D890),
            containerColor = Color(0xFF0D3D2B),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-8).dp, y = (-2).dp)
        )
    }
}

@Composable
private fun LocationHero(accent: Color) {
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.47f, size.height * 0.52f)
            val radius = min(size.width, size.height) * 0.39f
            val dashed = PathEffect.dashPathEffect(floatArrayOf(9.dp.toPx(), 9.dp.toPx()))

            drawCircle(Color(0xFF0D371C), radius, center)
            drawCircle(
                color = accent.copy(alpha = 0.5f),
                radius = radius * 0.69f,
                center = center,
                style = Stroke(width = 1.2.dp.toPx(), pathEffect = dashed)
            )
            drawCircle(
                color = accent.copy(alpha = 0.62f),
                radius = radius * 0.42f,
                center = center,
                style = Stroke(width = 1.2.dp.toPx(), pathEffect = dashed)
            )
            drawCircle(accent, 6.dp.toPx(), center)
            drawCircle(Color.White, 3.4.dp.toPx(), Offset(center.x + radius * 0.38f, center.y - radius * 0.18f))
            drawCircle(Color.White, 3.4.dp.toPx(), Offset(center.x - radius * 0.40f, center.y + radius * 0.55f))
        }

        StatusPill(
            text = "Radio: 200 m",
            dotColor = accent,
            containerColor = Color(0xFF0A4A2B),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = 28.dp)
        )
        StatusPill(
            text = "Alerta activa",
            containerColor = PillGray,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = (-38).dp)
        )
    }
}

@Composable
private fun CameraHero(accent: Color) {
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.5f, size.height * 0.52f)
            val radius = min(size.width, size.height) * 0.39f
            val iconWidth = 84.dp.toPx()
            val iconHeight = 62.dp.toPx()
            val iconLeft = center.x - iconWidth / 2
            val iconTop = center.y - iconHeight / 2

            drawCircle(Color(0xFF5A111A), radius, center)
            drawRoundRect(
                color = accent.copy(alpha = 0.88f),
                topLeft = Offset(iconLeft, iconTop + 7.dp.toPx()),
                size = Size(iconWidth, iconHeight),
                cornerRadius = CornerRadius(10.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )
            drawRoundRect(
                color = accent.copy(alpha = 0.88f),
                topLeft = Offset(center.x - 8.dp.toPx(), iconTop),
                size = Size(16.dp.toPx(), 8.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = accent.copy(alpha = 0.88f),
                radius = 16.dp.toPx(),
                center = Offset(center.x, center.y + 7.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = accent.copy(alpha = 0.88f),
                radius = 5.5.dp.toPx(),
                center = Offset(center.x, center.y + 7.dp.toPx())
            )
        }

        StatusPill(
            text = "Foto adjunta",
            containerColor = PillGray,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 20.dp)
        )
        StatusPill(
            text = "Carpeta roja \u00b7 hoy",
            dotColor = accent,
            containerColor = Color(0xFF3A0B12),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-4).dp, y = (-20).dp)
        )
    }
}

@Composable
private fun OfflineHero(accent: Color) {
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.5f, size.height * 0.53f)
            val radius = min(size.width, size.height) * 0.39f
            val noteWidth = 62.dp.toPx()
            val noteHeight = 78.dp.toPx()
            val noteLeft = center.x - noteWidth / 2
            val noteTop = center.y - noteHeight / 2
            val triangle = Path().apply {
                moveTo(center.x, noteTop - 22.dp.toPx())
                lineTo(center.x - 9.dp.toPx(), noteTop - 4.dp.toPx())
                lineTo(center.x + 9.dp.toPx(), noteTop - 4.dp.toPx())
                close()
            }

            drawCircle(Color(0xFF351952), radius, center)
            drawPath(triangle, accent)
            drawRoundRect(
                color = accent,
                topLeft = Offset(noteLeft, noteTop),
                size = Size(noteWidth, noteHeight),
                cornerRadius = CornerRadius(8.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )
            repeat(3) { index ->
                drawLine(
                    color = accent,
                    start = Offset(noteLeft + 13.dp.toPx(), noteTop + (26 + index * 12).dp.toPx()),
                    end = Offset(noteLeft + noteWidth - 13.dp.toPx(), noteTop + (26 + index * 12).dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        StatusPill(
            text = "Sin internet: OK",
            containerColor = PillGray,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = 20.dp)
        )
        StatusPill(
            text = "Sync autom\u00e1tico",
            containerColor = PillGray,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = (-42).dp)
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    dotColor: Color? = null
) {
    Row(
        modifier = modifier
            .heightIn(min = 39.dp)
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 15.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dotColor != null) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
