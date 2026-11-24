package com.example.coroutinedemo.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutinedemo.data.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class TasksViewModel(
        private val repository: TaskRepository = TaskRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadData(shouldFail: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSorted = false) }
            try {
                coroutineScope {
                    // Latihan 1: tiga async berjalan bersamaan, total waktu tetap ~2 detik
                    val tasks        = async { repository.getTasks(shouldFail) }
                    val announcement = async { repository.getAnnouncement() }
                    val courseCount  = async { repository.getCourseCount() }

                    _uiState.update {
                        it.copy(
                                isLoading    = false,
                                tasks        = tasks.await(),
                                announcement = announcement.await(),
                                courseCount  = courseCount.await()
                        )
                    }
                }
            } catch (e: IOException) {
                // Latihan 3: hanya IOException yang ditangkap agar CancellationException
                // tidak ikut tertangkap saat Cancel ditekan
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to load: ${e.message}")
                }
            }
        }
    }

    fun cancelLoading() {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = false, errorMessage = "Loading cancelled") }
    }

    fun onTap() {
        _uiState.update { it.copy(tapCount = it.tapCount + 1) }
    }

    fun blockMainThread() {
        Thread.sleep(3000L)
    }

    // Latihan 6: sort di Dispatchers.Default
    fun sortTasks() {
        viewModelScope.launch {
            val sorted = withContext(Dispatchers.Default) {
                _uiState.value.tasks.sortedBy { it.title }
            }
            _uiState.update { it.copy(tasks = sorted, isSorted = true) }
        }
    }
}
