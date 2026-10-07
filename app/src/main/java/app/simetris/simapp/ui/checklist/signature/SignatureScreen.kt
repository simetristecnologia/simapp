package app.simetris.simapp.ui.checklist.signature

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.simetris.simapp.R
import app.simetris.simapp.domain.ChecklistRules
import app.simetris.simapp.ui.checklist.form.ChecklistFormViewModel
import app.simetris.simapp.ui.checklist.form.StepBadge
import app.simetris.simapp.ui.components.*
import java.io.ByteArrayOutputStream

@Composable
fun SignatureScreen(vm: ChecklistFormViewModel, id: String, cancel: () -> Unit, complete: () -> Unit) {
    val state by vm.state(id).collectAsState()
    val finishing by vm.finishing.collectAsState()
    val points = remember { mutableStateListOf<Offset?>() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val hasStroke = points.zipWithNext().any { (a, b) -> a != null && b != null }
    val canFinalize = !finishing && state.checklist != null &&
        ChecklistRules.readyForSignature(state.missingFields) && size.width > 0 && size.height > 0 &&
        (hasStroke || state.signature != null)
    Column(Modifier.fillMaxSize()) {
        AppTopBar(R.string.launch_checklist)
        ScreenColumn {
            SectionCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.executor_signature),
                        style = MaterialTheme.typography.titleMedium)
                    StepBadge(3)
                }
                Text(stringResource(R.string.signature_instruction))
                Box(Modifier.fillMaxWidth().height(260.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline)
                    .background(Color.White).clipToBounds()) {
                    Canvas(Modifier.fillMaxSize().onSizeChanged { size = it }
                        .pointerInput(size) {
                            detectDragGestures(
                                onDragStart = { start ->
                                    points.add(null)
                                    if (start.isInside(size)) points.add(start)
                                },
                                onDragEnd = { points.add(null) },
                                onDragCancel = { points.add(null) },
                            ) { change, _ ->
                                val position = change.position
                                if (position.isInside(size)) points.add(position)
                                else if (points.lastOrNull() != null) points.add(null)
                                change.consume()
                            }
                        }) {
                        points.zipWithNext().forEach { (a, b) ->
                            if (a != null && b != null) drawLine(Color.Black, a, b, 3.dp.toPx())
                        }
                    }
                }
            }
            PrimaryActionButton(R.string.complete_checklist, canFinalize) {
                val signatureBytes = if (hasStroke) renderSignature(points, size) else null
                vm.finishWithSignature(id, signatureBytes, complete)
            }
            BackButton(cancel)
        }
    }
}

private fun Offset.isInside(size: IntSize): Boolean =
    x >= 0f && y >= 0f && x <= size.width && y <= size.height

private fun renderSignature(points: List<Offset?>, size: IntSize): ByteArray {
    val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
    }
    points.zipWithNext().forEach { (a, b) ->
        if (a != null && b != null) canvas.drawLine(a.x, a.y, b.x, b.y, paint)
    }
    return ByteArrayOutputStream().use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        output.toByteArray()
    }
}
