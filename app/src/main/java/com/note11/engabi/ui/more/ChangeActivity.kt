package com.note11.engabi.ui.more

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import com.note11.engabi.ui.theme.EngabiTheme

class ChangeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EngabiTheme {
                Surface(color = MaterialTheme.colors.background) {

                }
            }
        }
    }
}
