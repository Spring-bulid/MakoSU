package com.sukisu.ultra.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukisu.ultra.data.repository.KpmRepository
import com.sukisu.ultra.data.repository.KpmRepositoryImpl
import com.sukisu.ultra.ui.component.SearchStatus
import com.sukisu.ultra.ui.screen.kpm.InputDialogState
import com.sukisu.ultra.ui.screen.kpm.KpmUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class KpmViewModel(
    private val repo: KpmRepository = KpmRepositoryImpl()
) : ViewModel() {

    companion object {
        private const val TAG = "KpmViewModel"
    }

    private val _uiState = MutableStateFlow(KpmUiState())
    val uiState: StateFlow<KpmUiState> = _uiState.asStateFlow()

    private var tempFile: File? = null

    fun setTempFileForInstall(file: File) {
        tempFile = file
    }

    fun clearTempFile() {
        tempFile?.delete()
        tempFile = null
    }

    fun getTempFile(): File? = tempFile

    fun fetchModuleList() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isRefreshing = true, error = null) }

            repo.getModuleList()
                .onSuccess { modules ->
                    _uiState.update {
                        it.copy(
                            moduleList = modules,
                            isRefreshing = false
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "fetchModuleList failed", e)
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            error = e
                        )
                    }
                }
        }
    }

    fun showInstallDialog(moduleName: String) {
        _uiState.update {
            it.copy(
                showInstallModeDialog = true,
                tempModuleName = moduleName
            )
        }
    }

    fun dismissInstallDialog() {
        _uiState.update {
            it.copy(
                showInstallModeDialog = false,
                tempModuleName = null
            )
        }
    }

    fun showInputDialog(moduleId: String) {
        _uiState.update {
            it.copy(
                inputDialogState = InputDialogState(
                    visible = true,
                    moduleId = moduleId,
                    args = it.moduleList.find { m -> m.id == moduleId }?.args ?: ""
                )
            )
        }
    }

    fun hideInputDialog() {
        _uiState.update {
            it.copy(inputDialogState = InputDialogState())
        }
    }

    fun updateInputArgs(args: String) {
        _uiState.update {
            it.copy(inputDialogState = it.inputDialogState.copy(args = args))
        }
    }

    suspend fun executeControl(): Int {
        val moduleId = _uiState.value.inputDialogState.moduleId ?: return -1
        val args = _uiState.value.inputDialogState.args

        return repo.controlModule(moduleId, args)
            .onSuccess { _ ->
                hideInputDialog()
            }
            .onFailure { e ->
                Log.e(TAG, "Failed to control module", e)
                hideInputDialog()
            }
            .getOrElse { -1 }
    }

    fun updateSearchStatus(status: SearchStatus) {
        _uiState.update { it.copy(searchStatus = status) }
    }

    suspend fun updateSearchText(text: String) {
        _uiState.update {
            it.copy(
                searchStatus = it.searchStatus.copy(searchText = text)
            )
        }

        if (text.isEmpty()) {
            _uiState.update {
                it.copy(
                    searchStatus = it.searchStatus.copy(resultStatus = SearchStatus.ResultStatus.DEFAULT),
                    searchResults = emptyList()
                )
            }
            return
        }

        _uiState.update {
            it.copy(searchStatus = it.searchStatus.copy(resultStatus = SearchStatus.ResultStatus.LOAD))
        }

        val result = withContext(Dispatchers.Default) {
            _uiState.value.moduleList.filter {
                it.id.contains(text, true) ||
                it.name.contains(text, true) ||
                it.description.contains(text, true) ||
                it.author.contains(text, true) ||
                it.version.contains(text, true)
            }
        }

        _uiState.update {
            it.copy(
                searchResults = result,
                searchStatus = it.searchStatus.copy(
                    resultStatus = if (result.isEmpty()) SearchStatus.ResultStatus.EMPTY else SearchStatus.ResultStatus.SHOW
                )
            )
        }
    }

    data class ModuleInfo(
        val id: String,
        val name: String,
        val version: String,
        val author: String,
        val description: String,
        val args: String,
        val enabled: Boolean,
        val hasAction: Boolean
    )
}
