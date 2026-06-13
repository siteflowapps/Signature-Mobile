package com.siteflow.signature.cso.onboarding.data

import com.siteflow.signature.cso.onboarding.data.dto.AssetRequestDto
import com.siteflow.signature.cso.onboarding.data.dto.BusinessDetailsRequestDto
import com.siteflow.signature.cso.onboarding.data.dto.CreateOutletRequestDto
import com.siteflow.signature.cso.onboarding.data.dto.CreateOutletResponseDto
import com.siteflow.signature.cso.onboarding.data.dto.OutletDecisionRequestDto
import com.siteflow.signature.cso.onboarding.data.dto.DistributorListResponseDto
import com.siteflow.signature.cso.onboarding.data.dto.OutletListResponseDto
import com.siteflow.signature.cso.onboarding.data.dto.SlabsResponseDto
import com.siteflow.signature.cso.onboarding.data.dto.KycDetailsRequestDto
import com.siteflow.signature.core.data.networking.client.NetworkConfig
import com.siteflow.signature.outlet.walkthrough.data.OutletKycResponseDto
import com.siteflow.signature.core.data.networking.error.ApiError
import com.siteflow.signature.core.data.networking.request.safeRequest
import com.siteflow.signature.core.data.networking.result.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val apiJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

class OutletApi(
    private val client: HttpClient
) {

    suspend fun getSlabs(): NetworkResult<SlabsResponseDto, ApiError> {
        return safeRequest<SlabsResponseDto>(
            block = {
                client.get(NetworkConfig.v1("slabs"))
            }
        )
    }

    suspend fun getDistributors(): NetworkResult<DistributorListResponseDto, ApiError> {
        return safeRequest<DistributorListResponseDto>(
            block = {
                client.get(NetworkConfig.v1("distributors"))
            }
        )
    }

    suspend fun createOutlet(
        request: CreateOutletRequestDto
    ): NetworkResult<CreateOutletResponseDto, ApiError> {
        return safeRequest<CreateOutletResponseDto>(
            block = {
                client.post(NetworkConfig.v1("outlets")) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            }
        )
    }

    suspend fun getOutlets(
        page: Int,
        size: Int,
        showLoader: Boolean = true
    ): NetworkResult<OutletListResponseDto, ApiError> {
        return safeRequest<OutletListResponseDto>(
            showLoader = showLoader,
            block = {
                client.get(NetworkConfig.v1("outlets")) {
                    parameter("page", page)
                    parameter("size", size)
                }
            }
        )
    }

    /**
     * Submits business details as multipart form data.
     * - "data" part: JSON-encoded BusinessDetailsRequestDto
     * - "cancelledCheque" part: optional image bytes
     */
    suspend fun submitBusinessDetails(
        outletId: String,
        data: BusinessDetailsRequestDto,
        cancelledChequeBytes: ByteArray?
    ): NetworkResult<CreateOutletResponseDto, ApiError> {
        return safeRequest<CreateOutletResponseDto>(
            block = {
                client.put(NetworkConfig.v1("outlets/$outletId/business-details")) {
                    setBody(MultiPartFormDataContent(formData {
                        // JSON data part
                        append(
                            "data",
                            apiJson.encodeToString(data),
                            Headers.build {
                                append(HttpHeaders.ContentType, "application/json")
                            }
                        )
                        // Cancelled cheque image part (optional)
                        if (cancelledChequeBytes != null) {
                            append(
                                "cancelledCheque",
                                cancelledChequeBytes,
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=cancelled_cheque.jpg")
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                }
                            )
                        }
                    }))
                }
            }
        )
    }

    /**
     * POST /outlets/{outletId}/kyc-details
     * Sent as multipart:
     * - data: KycDetailsRequestDto (JSON)
     * - idProof: image file
     * - gstCertificate: image file (optional)
     * - locationProof: image file
     */
    suspend fun submitKycDetails(
        outletId: String,
        data: KycDetailsRequestDto,
        idProofBytes: ByteArray,
        gstCertificateBytes: ByteArray?,
        locationProofBytes: ByteArray
    ): NetworkResult<CreateOutletResponseDto, ApiError> {
        return safeRequest<CreateOutletResponseDto>(
            block = {
                client.put(NetworkConfig.v1("outlets/$outletId/kyc-details")) {
                    setBody(MultiPartFormDataContent(formData {
                        // JSON data part
                        append(
                            "data",
                            apiJson.encodeToString(data),
                            Headers.build {
                                append(HttpHeaders.ContentType, "application/json")
                            }
                        )
                        // ID Proof
                        append(
                            "idProof",
                            idProofBytes,
                            Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=id_proof.jpg")
                                append(HttpHeaders.ContentType, "image/jpeg")
                            }
                        )
                        // GST Certificate (optional)
                        if (gstCertificateBytes != null) {
                            append(
                                "gstCertificate",
                                gstCertificateBytes,
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=gst_certificate.jpg")
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                }
                            )
                        }
                        // Location Proof
                        append(
                            "locationProof",
                            locationProofBytes,
                            Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=location_proof.jpg")
                                append(HttpHeaders.ContentType, "image/jpeg")
                            }
                        )
                    }))
                }
            }
        )
    }

    /**
     * GET /outlets/{outletId}
     * Fetches full details of a single outlet by its ID.
     */
    suspend fun getOutletById(
        outletId: String
    ): NetworkResult<CreateOutletResponseDto, ApiError> {
        return safeRequest<CreateOutletResponseDto>(
            block = {
                client.get(NetworkConfig.v1("outlets/$outletId"))
            }
        )
    }

    /**
     * GET /outlet-kyc/{outletId}
     * Fetches KYC and payment details for an outlet.
     */
    suspend fun getOutletKyc(
        outletId: String
    ): NetworkResult<OutletKycResponseDto, ApiError> {
        return safeRequest<OutletKycResponseDto>(
            block = {
                client.get(NetworkConfig.v1("outlet-kyc/$outletId"))
            }
        )
    }

    /**
     * POST /outlets/{outletId}/photos
     * Submits outlet photos as multipart form data.
     * Each photo type (SHOP_FRONT, INSIDE_SHOP, COOLER_AREA, SHELF, BRANDING)
     * is a separate form part containing image bytes.
     */
    suspend fun submitPhotos(
        outletId: String,
        photoEntries: List<Pair<String, ByteArray>>
    ): NetworkResult<CreateOutletResponseDto, ApiError> {
        return safeRequest<CreateOutletResponseDto>(
            block = {
                client.put(NetworkConfig.v1("outlets/$outletId/photos")) {
                    setBody(MultiPartFormDataContent(formData {
                        photoEntries.forEach { (type, bytes) ->
                            append(
                                type,
                                bytes,
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=${type.lowercase()}.jpg")
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                }
                            )
                        }
                    }))
                }
            }
        )
    }

    /**
     * POST /outlets/{outletId}/agreement/otp
     * Triggers agreement OTP to be sent to the outlet owner's phone.
     */
    suspend fun requestAgreementOtp(
        outletId: String
    ): NetworkResult<CreateOutletResponseDto, ApiError> {
        return safeRequest<CreateOutletResponseDto>(
            block = {
                client.post(NetworkConfig.v1("outlets/$outletId/agreement/otp"))
            }
        )
    }

    /**
     * POST /outlets/{outletId}/agreement
     * Verifies the agreement OTP. On success, outlet advances to ASE_PENDING
     * (L1) — or ASM_PENDING when the chain has no ASE.
     * Body: { "otp": "..." } (VerifyAgreementRequest).
     */
    suspend fun verifyAgreementOtp(
        outletId: String,
        otp: String
    ): NetworkResult<CreateOutletResponseDto, ApiError> {
        return safeRequest<CreateOutletResponseDto>(
            block = {
                client.post(NetworkConfig.v1("outlets/$outletId/agreement")) {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("otp" to otp))
                }
            }
        )
    }

    /**
     * POST /outlets/{outletId}/asm-approve
     * POST /outlets/{outletId}/decision
     * Single approval endpoint. The backend resolves the level from the
     * caller's role + the outlet's status:
     *  - ASE on ASE_PENDING  → L1 approve/reject
     *  - ASM on ASM_PENDING  → L2 approve/reject
     * Body: { "action": "APPROVE" | "REJECT" | "RESUBMIT", "reason": "..." }
     */
    suspend fun decide(
        outletId: String,
        action: String,
        reason: String? = null
    ): NetworkResult<CreateOutletResponseDto, ApiError> {
        return safeRequest<CreateOutletResponseDto>(
            block = {
                client.post(NetworkConfig.v1("outlets/$outletId/decision")) {
                    contentType(ContentType.Application.Json)
                    setBody(OutletDecisionRequestDto(action = action, reason = reason))
                }
            }
        )
    }

    /**
     * POST /outlets/{outletId}/request-asset
     * ASE requests an asset (cooler/branding) for an approved outlet.
     */
    suspend fun requestAsset(
        outletId: String,
        request: AssetRequestDto
    ): NetworkResult<CreateOutletResponseDto, ApiError> {
        return safeRequest<CreateOutletResponseDto>(
            block = {
                client.post(NetworkConfig.v1("outlets/$outletId/request-asset")) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            }
        )
    }

    /**
     * POST /outlets/{outletId}/compliance   multipart
     * data:            ComplianceRequestDto (JSON)
     * coolerImage:     ByteArray (required)
     * assetLabelImage: ByteArray (required)
     * signageImage:    ByteArray (required)
     * outerOutletImage:ByteArray (required)
     * innerOutletImage:ByteArray (required)
     */
    suspend fun submitCompliance(
        outletId: String,
        body: com.siteflow.signature.cso.onboarding.data.dto.ComplianceRequestDto,
        coolerImageBytes: ByteArray,
        assetLabelImageBytes: ByteArray,
        signageImageBytes: ByteArray,
        outerOutletImageBytes: ByteArray,
        innerOutletImageBytes: ByteArray
    ): NetworkResult<CreateOutletResponseDto, ApiError> {
        return safeRequest<CreateOutletResponseDto>(
            block = {
                client.submitFormWithBinaryData(
                    url = NetworkConfig.v1("outlets/$outletId/compliance"),
                    formData = formData {
                        append(
                            "data",
                            apiJson.encodeToString(body),
                            Headers.build {
                                append(HttpHeaders.ContentType, "application/json")
                            }
                        )
                        fun addImage(name: String, bytes: ByteArray) {
                            append(name, bytes, Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=${name}.jpg")
                                append(HttpHeaders.ContentType, "image/jpeg")
                            })
                        }
                        addImage("coolerImage", coolerImageBytes)
                        addImage("assetLabelImage", assetLabelImageBytes)
                        addImage("signageImage", signageImageBytes)
                        addImage("outerOutletImage", outerOutletImageBytes)
                        addImage("innerOutletImage", innerOutletImageBytes)
                    }
                )
            }
        )
    }


    /**
     * POST /outlets/compliance/{complianceId}/verify
     * ASM verifies/approves ASE's compliance submission.
     */
    suspend fun verifyCompliance(
        complianceId: String
    ): NetworkResult<CreateOutletResponseDto, ApiError> {
        return safeRequest<CreateOutletResponseDto>(
            block = {
                client.post(NetworkConfig.v1("outlets/compliance/$complianceId/verify"))
            }
        )
    
    /**
     * POST /outlets/{outletId}/deactivate
     * Deactivates (soft-deletes) an outlet.
     */
    suspend fun deactivateOutlet(
        outletId: String
    ): NetworkResult<CreateOutletResponseDto, ApiError> {
        return safeRequest<CreateOutletResponseDto>(
            block = {
                client.post(NetworkConfig.v1("outlets/$outletId/deactivate"))
            }
        )
    }
}
}
