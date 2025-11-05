package com.example.minimalphone

class DebounceManager(
    private val debounceMs: Long = 300
){
    private var lastPackage: String? = null
}