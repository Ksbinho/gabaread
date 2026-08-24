package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.GabaritoDatabase
import com.example.data.repository.GabaritoRepository

class GabaritoViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GabaritoViewModel::class.java)) {
            val db = GabaritoDatabase.getDatabase(context)
            val repo = GabaritoRepository(db.gabaritoDao())
            return GabaritoViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
