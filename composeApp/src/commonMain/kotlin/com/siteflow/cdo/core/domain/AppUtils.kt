package com.siteflow.cdo.core.domain


object IdGenerator {
    private var counter = 0

    fun nextId(): String {
        counter += 1
        return counter.toString()
    }
}