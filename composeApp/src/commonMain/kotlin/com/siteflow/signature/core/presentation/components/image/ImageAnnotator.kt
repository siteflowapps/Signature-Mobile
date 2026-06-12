package com.siteflow.signature.core.presentation.components.image

import com.siteflow.signature.core.domain.model.NormalizedRect

expect object ImageAnnotator {

    /**
     * Draws rectangles on image and saves final bitmap.
     * @param imagePath original image path
     * @param rects list of normalized rectangles (0f–1f)
     * @param color ARGB color (0xAARRGGBB)
     * @param onDone returns final saved image path
     */
    fun drawRectanglesAndSave(
        imagePath: String,
        rects: List<NormalizedRect>,
        color: Long,
        onDone: (String) -> Unit
    )
}
