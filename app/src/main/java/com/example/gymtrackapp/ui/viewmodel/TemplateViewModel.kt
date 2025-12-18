package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackapp.data.entity.TemplateExercise
import com.example.gymtrackapp.data.entity.WorkoutTemplate
import com.example.gymtrackapp.data.repository.TemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TemplateViewModel(private val repository: TemplateRepository) : ViewModel() {

    private val _templates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    val templates = _templates.asStateFlow()

    // Mapa przechowująca listę ćwiczeń dla każdego szablonu (klucz: templateId)
    private val _templateExercisesMap = MutableStateFlow<Map<Long, List<TemplateExercise>>>(emptyMap())
    val templateExercisesMap = _templateExercisesMap.asStateFlow()

    fun loadTemplates() {
        viewModelScope.launch {
            _templates.value = repository.getAllTemplates()
        }
    }

    fun createTemplate(name: String, description: String?) {
        viewModelScope.launch {
            repository.createTemplate(name, description)
            loadTemplates() // Odśwież listę po dodaniu
        }
    }

    fun deleteTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
            loadTemplates() // Odśwież listę po usunięciu
        }
    }

    fun loadExercisesForTemplate(templateId: Long) {
        viewModelScope.launch {
            val exercises = repository.getExercisesForTemplate(templateId)
            _templateExercisesMap.value = _templateExercisesMap.value.toMutableMap().apply {
                this[templateId] = exercises
            }
        }
    }

    fun addExerciseToTemplate(templateId: Long, exerciseId: String) {
        viewModelScope.launch {
            repository.addExerciseToTemplate(templateId, exerciseId)
            loadExercisesForTemplate(templateId)
        }
    }

    fun deleteTemplateExercise(exercise: TemplateExercise) {
        viewModelScope.launch {
            repository.deleteTemplateExercise(exercise)
            loadExercisesForTemplate(exercise.templateId)
        }
    }

    fun renameTemplate(template: WorkoutTemplate, newName: String) {
        viewModelScope.launch {
            repository.renameTemplate(template, newName)
            loadTemplates()
        }
    }
}
