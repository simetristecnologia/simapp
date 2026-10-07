package app.simetris.simapp

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationInstrumentedTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun unauthenticatedLaunchShowsLogin() {
        rule.onNodeWithText("Entre com seus dados para continuar.").assertExists()
        rule.onNodeWithText("CÓDIGO DA EMPRESA").assertExists()
        rule.onNodeWithText("SENHA DE ACESSO").assertExists()
    }
}

