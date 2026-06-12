package com.siteflow.signature.core.analytics

object AnalyticsParams {

    // ── Common ────────────────────────────────────────────────────────────────
    const val USER_ID = "user_id"
    const val USER_ROLE = "user_role"
    const val BUSINESS_ID = "business_id"
    const val SCREEN_NAME = "screen_name"
    const val APP_VERSION = "app_version"
    const val BUILD_NUMBER = "build_number"
    const val PLATFORM = "platform"
    const val OS_VERSION = "os_version"
    const val DEVICE_MODEL = "device_model"
    const val NETWORK_TYPE = "network_type"
    const val LOCALE = "locale"
    const val IS_FRESH_INSTALL = "is_fresh_install"
    const val IS_RETURNING_USER = "is_returning_user"
    const val SESSION_DURATION_MS = "session_duration_ms"
    const val SCREENS_VIEWED_COUNT = "screens_viewed_count"
    const val ACTIONS_TAKEN_COUNT = "actions_taken_count"

    // ── Auth ──────────────────────────────────────────────────────────────────
    const val IS_AUTO_REDIRECT = "is_auto_redirect"
    const val MOBILE_NUMBER_LENGTH = "mobile_number_length"
    const val MOBILE_NUMBER_MASKED = "mobile_number_masked"
    const val TIME_TO_ENTER_MS = "time_to_enter_ms"
    const val TIME_TO_VERIFY_MS = "time_to_verify_ms"
    const val IS_FIRST_LOGIN = "is_first_login"
    const val RESEND_ATTEMPT_NUMBER = "resend_attempt_number"
    const val COUNTDOWN_REMAINING = "countdown_remaining"
    const val ATTEMPT_NUMBER = "attempt_number"
    const val SCREEN_ON_EXPIRY = "screen_on_expiry"

    // ── Navigation ────────────────────────────────────────────────────────────
    const val PREVIOUS_SCREEN = "previous_screen"
    const val PREVIOUS_TAB = "previous_tab"
    const val TAB = "tab"
    const val SCREEN = "screen"
    const val SOURCE = "source"

    // ── Network ───────────────────────────────────────────────────────────────
    const val STATUS = "status"
    const val NEW_STATUS = "new_status"
    const val ENDPOINT = "endpoint"
    const val METHOD = "method"
    const val STATUS_CODE = "status_code"
    const val LATENCY_MS = "latency_ms"
    const val IS_RETRY = "is_retry"
    const val ERROR_TYPE = "error_type"
    const val LOAD_TIME_MS = "load_time_ms"
    const val DURATION_MS = "duration_ms"
    const val TOTAL_UPLOAD_DURATION_MS = "total_upload_duration_ms"

    // ── Onboarding ────────────────────────────────────────────────────────────
    const val STEP_NUMBER = "step_number"
    const val STEP_LABEL = "step_label"
    const val TIME_ON_STEP_MS = "time_on_step_ms"
    const val OUTLET_ID = "outlet_id"
    const val IS_NEW = "is_new"
    const val RESUME_STEP = "resume_step"
    const val TOTAL_DURATION_MS = "total_duration_ms"
    const val STEPS_WITH_ERRORS_COUNT = "steps_with_errors_count"
    const val FIELDS_FILLED_COUNT = "fields_filled_count"
    const val LAT = "lat"
    const val LNG = "lng"
    const val ACCURACY_METERS = "accuracy_meters"
    const val TIME_TO_CAPTURE_MS = "time_to_capture_ms"
    const val PINCODE = "pincode"
    const val SUCCESS = "success"
    const val AUTO_FILLED_CITY = "auto_filled_city"
    const val AUTO_FILLED_STATE = "auto_filled_state"
    const val SLAB_ID = "slab_id"
    const val SLAB_LABEL = "slab_label"
    const val DISTRIBUTOR_NAME = "distributor_name"
    const val HAS_UPI = "has_upi"
    const val HAS_CHEQUE_PHOTO = "has_cheque_photo"
    const val KYC_ID_TYPE = "kyc_id_type"
    const val HAS_GST = "has_gst"
    const val PHOTO_TYPE = "photo_type"
    const val SLOT_ID = "slot_id"
    const val PHOTO_COUNT = "photo_count"
    const val TOTAL_REQUIRED = "total_required"
    const val PHOTOS_COUNT = "photos_count"

    // ── Outlet Detail / Asset ─────────────────────────────────────────────────
    const val OUTLET_STATUS = "outlet_status"
    const val COMPLETION_PERCENT = "completion_percent"
    const val SIGNATURE_STEPS_COMPLETED = "signature_steps_completed"
    const val COOLER_TYPE = "cooler_type"
    const val CAPACITY = "capacity"
    const val SIGNAGE_TYPE = "signage_type"
    const val COOLER_INSTALLED = "cooler_installed"
    const val SIGNAGE_INSTALLED = "signage_installed"
    const val TIME_ON_SCREEN_MS = "time_on_screen_ms"

    // ── Invoice ───────────────────────────────────────────────────────────────
    const val INVOICE_ID = "invoice_id"
    const val ITEMS_COUNT = "items_count"
    const val ITEMS_EXTRACTED = "items_extracted"
    const val GRAND_TOTAL = "grand_total"
    const val TIME_ON_REVIEW_MS = "time_on_review_ms"
    const val HAS_NOTE = "has_note"
    const val ACTION = "action"
    const val REJECTION_REASON_LENGTH = "rejection_reason_length"
    const val REJECTION_REASON = "rejection_reason"
    const val INVOICE_STATUS = "invoice_status"
    const val OUTLET_NAME = "outlet_name"
    const val QUALITY_ISSUES_COUNT = "quality_issues_count"
    const val HAS_HARD_BLOCK = "has_hard_block"
    const val ISSUES = "issues"
    const val CONFIDENCE_AVG = "confidence_avg"
    const val REASON = "reason"
    const val PHASE_ON_CANCEL = "phase_on_cancel"
    const val TIME_ELAPSED_MS = "time_elapsed_ms"
    const val WAS_AUTO_FILLED = "was_auto_filled"
    const val EDITS_MADE_COUNT = "edits_made_count"

    // ── Error ─────────────────────────────────────────────────────────────────
    const val ERROR_MESSAGE = "error_message"
    const val ERROR_CODE = "error_code"
    const val RETRY_COUNT = "retry_count"

    // ── Dashboard ─────────────────────────────────────────────────────────────
    const val TOTAL_OUTLETS = "total_outlets"
    const val IN_PROGRESS = "in_progress"
    const val ASM_PENDING = "asm_pending"
    const val PENDING_INVOICES = "pending_invoices"
    const val TOTAL_ASES = "total_ases"
    const val ACTIVE_OUTLETS = "active_outlets"
    const val SUSPENDED = "suspended"
    const val TOTAL_INVOICES = "total_invoices"
    const val LAST_PAYOUT_AMOUNT = "last_payout_amount"
    const val MONTHLY_PERFORMANCE = "monthly_performance"
    const val TIER = "tier"
    const val CARD_TYPE = "card_type"
    const val CARD_NAME = "card_name"

    // ── Outlet List ───────────────────────────────────────────────────────────
    const val OUTLET_COUNT = "outlet_count"
    const val INITIAL_FILTER = "initial_filter"
    const val INVOICE_COUNT = "invoice_count"
    const val QUERY_LENGTH = "query_length"
    const val RESULTS_COUNT = "results_count"
    const val FILTER = "filter"
    const val FIELD_NAME = "field_name"
    const val PHASE = "phase"
    const val URL = "url"

    // ── Walkthrough ───────────────────────────────────────────────────────────
    const val IS_FIRST_TIME = "is_first_time"
    const val STEPS_COMPLETED = "steps_completed"
    const val STEP_ON_EXIT = "step_on_exit"
    const val TIME_SPENT_MS = "time_spent_ms"

    // ── Notifications ─────────────────────────────────────────────────────────
    const val NOTIFICATION_ID = "notification_id"
    const val NOTIFICATION_TYPE = "notification_type"

    // ── My Team ───────────────────────────────────────────────────────────────
    const val ASE_COUNT = "ase_count"

    // ── Profile ───────────────────────────────────────────────────────────────
    const val HAS_KYC_DATA = "has_kyc_data"

    // ── Onboarding Step-specific (kept for backward compat) ───────────────────
    const val OUTLET_TYPE = "outlet_type"
    const val DISTRIBUTOR_ID = "distributor_id"
    const val KYC_TYPE = "kyc_type"
}
