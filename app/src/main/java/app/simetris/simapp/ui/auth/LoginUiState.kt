package app.simetris.simapp.ui.auth

data class LoginUiState(
    val companyCode: String = "",
    val login: String = "",
    val password: String = "",
    val submitting: Boolean = false,
    val error: Int? = null,
)
