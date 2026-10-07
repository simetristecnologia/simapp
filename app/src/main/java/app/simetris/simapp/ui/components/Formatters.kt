package app.simetris.simapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.simetris.simapp.R
import app.simetris.simapp.domain.MissingField
import app.simetris.simapp.domain.MissingFieldType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(value: Long): String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR")).format(Date(value))

fun parseDate(value: String): Long? = if (!value.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) null else
    runCatching { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }.parse(value)?.time }.getOrNull()

@Composable fun statusText(value: String): String = when (value) {
    "RASCUNHO" -> stringResource(R.string.draft)
    "FINALIZADO" -> stringResource(R.string.finalized)
    "PENDENTE" -> stringResource(R.string.pending)
    "CONFORME" -> stringResource(R.string.conforming)
    "NAO_CONFORME" -> stringResource(R.string.nonconforming)
    "ENVIANDO" -> stringResource(R.string.sending)
    "FALHOU" -> stringResource(R.string.failed)
    "CONFIRMADO" -> stringResource(R.string.confirmed)
    else -> stringResource(R.string.operation_failed)
}

@Composable fun stageText(value: String): String = when (value) {
    "CHECKLIST" -> stringResource(R.string.checklist_stage)
    "ANEXOS" -> stringResource(R.string.attachments_stage)
    else -> stringResource(R.string.confirmation_stage)
}

@Composable fun errorText(value: String): String = when (value) {
    "API_INDISPONIVEL" -> stringResource(R.string.api_unavailable)
    "FALHA_CATALOGO" -> stringResource(R.string.reference_data_failure)
    else -> stringResource(R.string.generic_upload_failure)
}

@Composable fun missingFieldText(missing: MissingField): String = when (missing.type) {
    MissingFieldType.RESPONSE -> stringResource(R.string.missing_response, missing.itemName)
    MissingFieldType.REASON -> stringResource(R.string.missing_reason, missing.itemName)
    MissingFieldType.MIN_PHOTOS -> stringResource(R.string.missing_min_photos, missing.itemName, missing.quantity)
    MissingFieldType.MAX_PHOTOS -> stringResource(R.string.missing_max_photos, missing.itemName, missing.quantity)
    MissingFieldType.VEHICLE -> stringResource(R.string.missing_vehicle)
    MissingFieldType.DRIVER -> stringResource(R.string.missing_driver)
    MissingFieldType.ODOMETER -> stringResource(R.string.missing_odometer)
    MissingFieldType.ITEMS -> stringResource(R.string.missing_items)
    MissingFieldType.SIGNATURE -> stringResource(R.string.missing_signature)
}


