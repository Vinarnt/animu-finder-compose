package fr.vinarnt.animu.finder.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.koin.android.ext.koin.androidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App(
                koinAppDeclaration = {
                    androidContext(this@MainActivity)
                }
            )
        }
    }
}
