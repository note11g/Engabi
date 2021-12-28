package com.note11.engabi.ui.util

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.note11.engabi.ui.theme.Blue600
import kotlinx.coroutines.launch


@ExperimentalMaterialApi
@Composable
fun CustomBottomSheetScaffold(
    scaffoldState: BottomSheetScaffoldState,
    sheetContent: @Composable ColumnScope.() -> Unit,
    sheetBackgroundColor: Color = Blue600,
    content: @Composable (PaddingValues) -> Unit
) {
    BottomSheetScaffold(
        sheetContent = sheetContent,
        scaffoldState = scaffoldState,
        sheetElevation = 0.dp,
        sheetGesturesEnabled = false,
        sheetBackgroundColor = sheetBackgroundColor,
        sheetShape = RoundedCornerShape(
            bottomStart = 0.dp,
            bottomEnd = 0.dp,
            topStart = 16.dp,
            topEnd = 16.dp
        ),
        content = content
    )
}

@ExperimentalMaterialApi
@Composable
fun CustomBottomSheetLayout(
    modifier: Modifier = Modifier,
    bottomSheetState: ModalBottomSheetState,
    sheetContent: @Composable ColumnScope.() -> Unit,
    sheetBackgroundColor: Color = Blue600,
    sheetShape: Shape = RoundedCornerShape(
        bottomStart = 0.dp,
        bottomEnd = 0.dp,
        topStart = 16.dp,
        topEnd = 16.dp
    ),
    content: @Composable () -> Unit
) {
    ModalBottomSheetLayout(
        modifier = modifier,
        sheetContent = sheetContent,
        sheetState = bottomSheetState,
        sheetBackgroundColor = sheetBackgroundColor,
        sheetShape = sheetShape,
        content = content
    )
}


@ExperimentalMaterialApi
@Composable
fun CustomBottomDrawer(
    drawerState: BottomDrawerState = rememberBottomDrawerState(BottomDrawerValue.Closed),
    gesturesEnabled: Boolean = false,
    drawerShape: Shape = RoundedCornerShape(
        bottomStart = 0.dp,
        bottomEnd = 0.dp,
        topStart = 16.dp,
        topEnd = 16.dp
    ), drawerElevation: Dp = 16.dp,
    drawerBackgroundColor: Color = Blue600,
    drawerContent: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit
) = BottomDrawer(
    gesturesEnabled = gesturesEnabled,
    drawerState = drawerState,
    drawerShape = drawerShape,
    drawerElevation = drawerElevation,
    drawerBackgroundColor = drawerBackgroundColor,
    drawerContent = drawerContent,
    content = content,
)
