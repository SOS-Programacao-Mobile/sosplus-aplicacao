package br.com.sosplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.sosplus.ui.auth.AplicativoSos
import br.com.sosplus.ui.theme.SosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // teste do joao para fazer um pr
            SosTheme(
                darkTheme = true,
                dynamicColor = false,
            ) {
                AplicativoSos()
            }
        }
    }
}
