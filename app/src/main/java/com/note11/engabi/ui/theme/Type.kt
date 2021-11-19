package com.note11.engabi.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.note11.engabi.R

val spoqaFamily = FontFamily(
    Font(R.font.spoqa_t, FontWeight.Thin),
    Font(R.font.spoqa_l, FontWeight.Light),
    Font(R.font.spoqa_r, FontWeight.Normal),
    Font(R.font.spoqa_m, FontWeight.Medium),
    Font(R.font.spoqa_b, FontWeight.Bold)
)

val Typography = Typography(
    defaultFontFamily = spoqaFamily,
    body1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)