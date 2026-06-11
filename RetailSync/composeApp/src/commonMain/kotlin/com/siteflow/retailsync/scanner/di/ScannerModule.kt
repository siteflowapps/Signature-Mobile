package com.siteflow.retailsync.scanner.di

import com.siteflow.retailsync.scanner.domain.ScannerViewModel
import org.koin.dsl.module

val scannerModule = module {
    factory { ScannerViewModel(get()) }
}
