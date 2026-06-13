package com.siteflow.signature.ase.di

import com.siteflow.signature.ase.approvals.domain.AseApprovalViewModel
import org.koin.dsl.module

val aseModule = module {
    // single (not factory): the queue loaded by the home screen must be visible
    // to the review screen via the same instance (outletFor lookup).
    single { AseApprovalViewModel(get(), get()) }
}
