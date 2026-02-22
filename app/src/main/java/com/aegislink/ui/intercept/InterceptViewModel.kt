package com.aegislink.ui.intercept

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aegislink.data.repo.ListRepository
import com.aegislink.data.repo.SecurePrefsRepository
import com.aegislink.data.repo.StatsRepository
import com.aegislink.domain.ClassificationResult
import com.aegislink.domain.UrlClassifier
import com.aegislink.network.ScanResult
import com.aegislink.network.VirusTotalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InterceptViewModel(
    private val classifier: UrlClassifier,
    private val listRepository: ListRepository,
    private val securePrefsRepository: SecurePrefsRepository,
    private val virusTotalRepository: VirusTotalRepository,
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _classification = MutableStateFlow<ClassificationResult?>(null)
    val classification: StateFlow<ClassificationResult?> = _classification

    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult

    fun classify(url: String) {
        viewModelScope.launch {
            val result = classifier.classify(url)
            _classification.value = result
            when (result) {
                is ClassificationResult.Blocked -> statsRepository.increment("blocked")
                is ClassificationResult.Trusted -> statsRepository.increment("trusted")
                is ClassificationResult.Unknown -> statsRepository.increment("unknown")
                is ClassificationResult.Invalid -> statsRepository.increment("invalid")
            }
        }
    }

    fun whitelist(domain: String) {
        viewModelScope.launch {
            listRepository.addWhitelist(domain)
        }
    }

    fun scanWithVirusTotal(url: String) {
        viewModelScope.launch {
            val apiKey = securePrefsRepository.getVirusTotalApiKey().orEmpty()
            _scanResult.value = virusTotalRepository.scanUrl(apiKey, url)
        }
    }

    fun clearScanResult() {
        _scanResult.value = null
    }
}

class InterceptViewModelFactory(
    private val classifier: UrlClassifier,
    private val listRepository: ListRepository,
    private val securePrefsRepository: SecurePrefsRepository,
    private val virusTotalRepository: VirusTotalRepository,
    private val statsRepository: StatsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return InterceptViewModel(
            classifier,
            listRepository,
            securePrefsRepository,
            virusTotalRepository,
            statsRepository
        ) as T
    }
}
