package fr.vinarnt.animu.finder.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.santimattius.persistent.cache.startup.injectContext
import org.koin.android.ext.koin.androidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectContext(applicationContext)

        setContent {
            App(
                koinAppDeclaration = {
                    androidContext(this@MainActivity)
                }
            )
        }
    }
}
