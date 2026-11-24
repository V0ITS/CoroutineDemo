package com.example.coroutinedemo.ui.main

import com.example.coroutinedemo.data.Task

data class TasksUiState(
        val isLoading: Boolean = false,
        val tasks: List<Task> = emptyList(),
        val announcement: String = "",
        val errorMessage: String? = null,
        val tapCount: Int = 0,
        val courseCount: Int = 0,     // Latihan 1
        val isSorted: Boolean = false // Latihan 6
)
