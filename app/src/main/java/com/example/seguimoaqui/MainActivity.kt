package com.example.seguimoaqui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.seguimoaqui.data.AppDatabase
import com.example.seguimoaqui.repository.MateriaRepository
import com.example.seguimoaqui.ui.screen.MainScreen
import com.example.seguimoaqui.ui.theme.SeguimoAquiTheme
import com.example.seguimoaqui.viewmodel.MateriaViewModel
import com.example.seguimoaqui.viewmodel.MateriaViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = MateriaRepository(database.materiaDao())
        val viewModelFactory = MateriaViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[MateriaViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            SeguimoAquiTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}