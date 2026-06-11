package com.siteflow.cdo.ase.di

import com.siteflow.cdo.ase.compliance.domain.ComplianceViewModel
import com.siteflow.cdo.ase.dashboard.domain.AseHomeViewModel
import com.siteflow.cdo.ase.dashboard.domain.AsmHomeViewModel
import com.siteflow.cdo.ase.dashboard.domain.AseDashboardViewModel
import com.siteflow.cdo.ase.dashboard.domain.OutletDetailViewModel
import com.siteflow.cdo.ase.invoices.domain.InvoiceViewModel
import com.siteflow.cdo.ase.onboarding.data.LocationApi
import com.siteflow.cdo.ase.onboarding.data.OutletApi
import com.siteflow.cdo.ase.onboarding.data.OutletRepository
import com.siteflow.cdo.ase.onboarding.domain.OnboardingViewModel
import com.siteflow.cdo.ase.profile.data.UserApi
import com.siteflow.cdo.ase.profile.data.UserRepository
import com.siteflow.cdo.ase.profile.domain.MyAsesViewModel
import com.siteflow.cdo.ase.profile.domain.ProfileViewModel
import org.koin.dsl.module

val aseModule = module {
    single { LocationApi(get()) }
    single { OutletApi(get()) }
    single { UserApi(get()) }
    single { OutletRepository(get(), get(), get()) }
    single { UserRepository(get(), get()) }
    single { OnboardingViewModel(get(), get(), get(), get(), get()) }
    single { AseHomeViewModel(get(), get(), get()) }
    single { AsmHomeViewModel(get(), get(), get()) }
    single { AseDashboardViewModel(get(), get(), get()) }
    factory { OutletDetailViewModel(get(), get(), get()) }
    factory { ComplianceViewModel(get(), get(), get(), get(), get()) }
    factory { MyAsesViewModel(get(), get()) }
    single { InvoiceViewModel(get(), get()) }
    single { ProfileViewModel(get(), get(), get(), get()) }
}
