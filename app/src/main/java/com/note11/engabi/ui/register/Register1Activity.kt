package com.note11.engabi.ui.register

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.insets.ProvideWindowInsets
import com.gun0912.tedpermission.coroutine.TedPermission
import com.note11.engabi.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class Register1Activity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EngabiTheme {
                ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)
                    ) {
                        val (indicator, content) = createRefs()
                        val focusManager = LocalFocusManager.current

                        ProgressIndicator(
                            nowIndex = 0,
                            modifier = Modifier.constrainAs(indicator) {
                                top.linkTo(parent.top)
                                bottom.linkTo(content.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)

                            }
                        )
                        ContentSection(modifier = Modifier.constrainAs(content) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start, margin = 24.dp)
                            end.linkTo(parent.end, margin = 24.dp)

                            width = Dimension.fillToConstraints
                        })
                    }
                }
            }
        }
    }

    @Composable
    private fun ContentSection(modifier: Modifier = Modifier) {
        val year = remember { mutableStateOf("") }
        val month = remember { mutableStateOf("") }
        val date = remember { mutableStateOf("") }
        val monthRequester = remember { FocusRequester() }
        val dateRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

        val scrollState = rememberScrollState()

        Column(modifier.scrollable(scrollState, Orientation.Vertical), Arrangement.Center) {
            Column {
                Text("??????????????? ??????????????????", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White)
                Spacer(Modifier.size(4.dp))
                Text(
                    "???????????? ?????? ????????? ???????????? ?????????",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Gray300
                )
            }
            Spacer(Modifier.size(64.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CustomTextField(
                    Modifier.width(104.dp), year.value,
                    {
                        if (it.toIntOrNull() == null && it.isNotEmpty()) return@CustomTextField
                        if (it.length >= 4) {
                            month.value = ""
                            monthRequester.requestFocus()
                            if (it.length > 4) return@CustomTextField
                        }
                        year.value = it
                    }, hint = "2004"
                ) {
                    month.value = ""
                    monthRequester.requestFocus()
                }
                CustomTextFieldGuildText("???", 8.dp)
                CustomTextField(Modifier.width(64.dp), month.value, {
                    if (it.toIntOrNull() == null && it.isNotEmpty()) return@CustomTextField
                    if (it.length >= 2 || (it.isNotEmpty() && it != "1" && it != "0")) {
                        date.value = ""
                        dateRequester.requestFocus()
                        if (it.length > 2) return@CustomTextField
                    }
                    month.value = it
                }, monthRequester, hint = "6") {
                    date.value = ""
                    dateRequester.requestFocus()
                }
                CustomTextFieldGuildText("???", 8.dp)
                CustomTextField(Modifier.width(64.dp), date.value, {
                    if (it.toIntOrNull() == null && it.isNotEmpty()) return@CustomTextField
                    if (it.length >= 2 || (it.isNotEmpty() && it != "1" && it != "2" && it != "3" && it != "0")) {
                        focusManager.clearFocus()
                        if (it.length > 2) return@CustomTextField
                    }
                    date.value = it
                }, dateRequester, ImeAction.Done, hint = "28") {
                    focusManager.clearFocus()
                }
                CustomTextFieldGuildText("???", 28.dp)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color = Gray300)
            )
            Spacer(Modifier.size(80.dp))
            Text(
                "??????",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.medium)
                    .background(BlueAccent)
                    .clickable { goToNextStep("${year.value}${if (month.value.length == 1) "0" else ""}${month.value}${if (date.value.length == 1) "0" else ""}${date.value}") }
                    .padding(vertical = 16.dp)
            )
        }
    }

    private fun goToNextStep(birth: String) {
        if (birth.length == 8 && birth.toIntOrNull() != null) {
            Intent(this@Register1Activity, Register2Activity::class.java).let{
                it.putExtra("birth", birth)
                it.putExtra("uid", intent.getStringExtra("uid") ?: "")
                startActivity(it)
            }
        }
        else {
            Toast.makeText(this, "??????????????? ????????? ??????????????????", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    onChange: (String) -> Unit,
    focusRequester: FocusRequester? = null,
    imeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Number,
    hint: String = "",
    onAction: () -> Unit = {}
) {
    TextField(
        value = value,
        textStyle = TextStyle(
            fontFamily = spoqaFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = White
        ),
        onValueChange = onChange,
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction,
            keyboardType = keyboardType
        ),
        placeholder = {
            Text(
                hint, style = TextStyle(
                    fontFamily = spoqaFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = Gray500,
                    textAlign = TextAlign.Center
                ), modifier = Modifier.fillMaxWidth()
            )
        },
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            backgroundColor = Color.Transparent
        ),
        keyboardActions = KeyboardActions(onAny = { onAction() }),
        singleLine = true,
        modifier = if (focusRequester != null) modifier.focusRequester(focusRequester) else modifier
    )
}

@Composable
fun CustomTextFieldGuildText(text: String, endPadding: Dp, modifier: Modifier = Modifier) {
    Text(
        text,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        color = Gray500,
        modifier = modifier.padding(end = endPadding)
    )
}

@Composable
fun ProgressIndicator(
    modifier: Modifier = Modifier,
    stepList: List<String> = listOf("???????????? ??????", "???????????? ??????", "?????? ??????"),
    nowIndex: Int
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth(0.6f)) {
        val (idc1, idc2, idc3) = createRefs()
        val (str1, str2, str3) = createRefs()
        val (dot1, dot2) = createRefs()

        CircleIndicatorItem(Modifier.constrainAs(idc1) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(dot1.start)
        }, 1, nowIndex)

        DottedLine(Modifier.constrainAs(dot1) {
            top.linkTo(idc1.top)
            bottom.linkTo(idc1.bottom)
            start.linkTo(idc1.end)
            end.linkTo(idc2.start)

            width = Dimension.fillToConstraints
        })

        CircleIndicatorItem(Modifier.constrainAs(idc2) {
            top.linkTo(parent.top)
            start.linkTo(dot1.end)
            end.linkTo(dot2.start)
        }, 2, nowIndex)

        DottedLine(Modifier.constrainAs(dot2) {
            top.linkTo(idc2.top)
            bottom.linkTo(idc2.bottom)
            start.linkTo(idc2.end)
            end.linkTo(idc3.start)

            width = Dimension.fillToConstraints
        })

        CircleIndicatorItem(Modifier.constrainAs(idc3) {
            top.linkTo(parent.top)
            start.linkTo(dot2.end)
            end.linkTo(parent.end)
        }, 3, nowIndex)


        IndicatorText(Modifier.constrainAs(str1) {
            top.linkTo(idc1.bottom, margin = 12.dp)
            bottom.linkTo(parent.bottom)
            start.linkTo(idc1.start)
            end.linkTo(idc1.end)
        }, stepList[0], nowIndex == 0)

        IndicatorText(Modifier.constrainAs(str2) {
            top.linkTo(str1.top)
            bottom.linkTo(str1.bottom)
            start.linkTo(idc2.start)
            end.linkTo(idc2.end)
        }, stepList[1], nowIndex == 1)

        IndicatorText(Modifier.constrainAs(str3) {
            top.linkTo(str1.top)
            bottom.linkTo(str1.bottom)
            start.linkTo(idc3.start)
            end.linkTo(idc3.end)
        }, stepList[2], nowIndex == 2)
    }
}

@Composable
fun IndicatorText(modifier: Modifier, text: String, isNow: Boolean) {
    Text(
        text, color = if (isNow) Gray100 else Gray500,
        textAlign = TextAlign.Center,
        fontSize = if (isNow) 12.sp else 10.sp,
        fontWeight = if (isNow) FontWeight.Medium else FontWeight.Normal,
        modifier = modifier
    )
}

@Composable
fun CircleIndicatorItem(modifier: Modifier = Modifier, num: Int, nowIndex: Int) {
    Surface(
        shape = CircleShape,
        modifier = modifier
            .size(44.dp),
        color = if (num == nowIndex + 1) BlueAccent else Color.Transparent,
        border = BorderStroke(1.dp, color = BlueAccent)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "$num",
                color = if (num == nowIndex + 1) White else BlueAccent,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DottedLine(
    modifier: Modifier = Modifier,
    lineColor: Color = BlueAccent,
    height: Dp = 1.dp,
    step: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .padding(start = 4.dp)
            .height(height)
            .background(lineColor, shape = DottedShape(step))
    )
}

data class DottedShape(
    val step: Dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = Outline.Generic(Path().apply {
        val stepPx = with(density) { step.toPx() }
        val stepsCount = (size.width / stepPx).roundToInt()
        val actualStep = size.width / stepsCount
        val dotSize = Size(width = actualStep / 2, height = size.height)
        for (i in 0 until stepsCount) {
            addRect(
                Rect(
                    offset = Offset(x = i * actualStep, y = 0f),
                    size = dotSize
                )
            )
        }
        close()
    })
}