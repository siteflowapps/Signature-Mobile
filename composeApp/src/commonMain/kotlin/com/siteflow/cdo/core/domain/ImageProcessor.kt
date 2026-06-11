package com.siteflow.cdo.core.domain

expect class ImageProcessor {
    /**
     * Resizes (if needed), burns an un-removable watermark containing the provided 
     * latitude, longitude, and current timestamp onto the lower portion of the image,
     * and aggressively compresses the result down to approximately 300-400KB.
     * 
     * Returns the absolute path of the newly generated & processed JPEG file.
     */
    suspend fun processImage(
        originalPath: String, 
        latitude: Double?, 
        longitude: Double?
    ): String
}
