package com.siteflow.signature.cso.di

import com.siteflow.signature.cso.compliance.domain.ComplianceViewModel
import com.siteflow.signature.cso.dashboard.domain.CsoHomeViewModel
import com.siteflow.signature.cso.dashboard.domain.AsmHomeViewModel
import com.siteflow.signature.cso.dashboard.domain.CsoDashboardViewModel
import com.siteflow.signature.cso.dashboard.domain.OutletDetailViewModel
import com.siteflow.signature.cso.invoices.domain.InvoiceViewModel
import com.siteflow.signature.cso.onboarding.data.LocationApi
import com.siteflow.signature.cso.onboarding.data.OutletApi
import com.siteflow.signature.cso.onboarding.data.OutletRepository
import com.siteflow.signature.cso.onboarding.domain.OnboardingViewModel
import com.siteflow.signature.cso.profile.data.UserApi
import com.siteflow.signature.cso.profile.data.UserRepository
import com.siteflow.signature.cso.profile.domain.MyAsesViewModel
import com.siteflow.signature.cso.profile.domain.ProfileViewModel
import com.siteflow.signature.asset.approvals.domain.AssetApprovalViewModel
import com.siteflow.signature.asset.approvals.domain.OutletAssetViewModel
import org.koin.dsl.module

val csoModule = module {
    single { LocationApi(get()) }
    single { OutletApi(get()) }
    single { UserApi(get()) }
    single { OutletRepository(get(), get(), get()) }
    single { UserRepository(get(), get()) }
    single { OnboardingViewModel(get(), get(), get(), get(), get()) }
    single { CsoHomeViewModel(get(), get(), get()) }
    single { AsmHomeViewModel(get(), get(), get()) }
    single { CsoDashboardViewModel(get(), get(), get()) }
    factory { OutletDetailViewModel(get(), get(), get()) }
    factory { ComplianceViewModel(get(), get(), get(), get(), get()) }
    factory { MyAsesViewModel(get(), get()) }
    single { InvoiceViewModel(get(), get()) }
    single { ProfileViewModel(get(), get(), get(), get()) }
    factory { AssetApprovalViewModel(get(), get()) }
    factory { OutletAssetViewModel(get(), get()) }
}
