package app.simetris.simapp.ui.components

import android.graphics.BitmapFactory
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import app.simetris.simapp.R

@Composable fun ImageThumbnail(path: String, modifier: Modifier,
    contentScale: ContentScale = ContentScale.Fit, @StringRes label: Int = R.string.photo) {
    val image = remember(path) { BitmapFactory.decodeFile(path)?.asImageBitmap() }
    if (image != null) Image(image, contentDescription = stringResource(label),
        modifier = modifier, contentScale = contentScale)
}

