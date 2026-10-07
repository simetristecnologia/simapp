package app.simetris.simapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import app.simetris.simapp.ui.navigation.AppNavigation
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppNavigation(application as SimAppApplication) }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { (application as SimAppApplication).auth.restore() }
    }
}
