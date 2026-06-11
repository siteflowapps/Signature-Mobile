package com.siteflow.cdo.core.presentation.components.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import com.github.panpf.zoomimage.ZoomImage

@Composable
fun ZoomableImage(
    painter: Painter,
    modifier: Modifier = Modifier
) {
    ZoomImage(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
