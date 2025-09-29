package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackapp.data.entity.Exercise
import com.example.gymtrackapp.data.repository.ExerciseRepository
import kotlinx.coroutines.launch

class ExerciseViewModel(private val repository: ExerciseRepository): ViewModel() {

    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> get() = _exercises

    private val _levels = MutableLiveData<List<String>>()
    val levels: LiveData<List<String>> get() = _levels

    private val _equipments = MutableLiveData<List<String>>()
    val equipments: LiveData<List<String>> get() = _equipments

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> get() = _categories

    private val _mechanics = MutableLiveData<List<String>>()
    val mechanics: LiveData<List<String>> get() = _mechanics

    private val _forces = MutableLiveData<List<String>>()
    val forces: LiveData<List<String>> get() = _forces

    private val _primaryMuscles = MutableLiveData<List<String>>()
    val primaryMuscles: LiveData<List<String>> get() = _primaryMuscles

    private val _secondaryMuscles = MutableLiveData<List<String>>()
    val secondaryMuscles: LiveData<List<String>> get() = _secondaryMuscles

    private var allExercises: List<Exercise> = emptyList()

    // 🔹 Wybrane filtry (to, co faktycznie stosujemy do filtrowania)
    private var selectedLevels: Set<String> = emptySet()
    private var selectedEquipments: Set<String> = emptySet()
    private var selectedCategories: Set<String> = emptySet()
    private var selectedMechanics: Set<String> = emptySet()
    private var selectedForces: Set<String> = emptySet()
    private var selectedPrimaryMuscles: Set<String> = emptySet()
    private var selectedSecondaryMuscles: Set<String> = emptySet()

    // 🔹 Tymczasowe wybory (to, czym bawimy się w dialogu)
    var tempLevels: Set<String> = emptySet()
    var tempEquipments: Set<String> = emptySet()
    var tempCategories: Set<String> = emptySet()
    var tempMechanics: Set<String> = emptySet()
    var tempForces: Set<String> = emptySet()
    var tempPrimaryMuscles: Set<String> = emptySet()
    var tempSecondaryMuscles: Set<String> = emptySet()

    fun loadAllExercises() {
        viewModelScope.launch {
            repository.loadExercisesFromAssets()
            allExercises = repository.getAllExercises()
            _exercises.value = allExercises

            // ladowanie opcji do filtrów
            _levels.value = repository.exerciseDao.getAllLevels()
            _equipments.value = repository.exerciseDao.getAllEquipments()
            _categories.value = repository.exerciseDao.getAllCategories()
            _mechanics.value = repository.exerciseDao.getAllMechanics()
            _forces.value = repository.exerciseDao.getAllForces()
            _primaryMuscles.value = repository.getAllPrimaryMuscles()
            _secondaryMuscles.value = repository.getAllSecondaryMuscles()

            // domyślnie wszystkie zaznaczone
            selectedLevels = _levels.value?.toSet() ?: emptySet()
            selectedEquipments = _equipments.value?.toSet() ?: emptySet()
            selectedCategories = _categories.value?.toSet() ?: emptySet()
            selectedMechanics = _mechanics.value?.toSet() ?: emptySet()
            selectedForces = _forces.value?.toSet() ?: emptySet()
            selectedPrimaryMuscles = _primaryMuscles.value?.toSet() ?: emptySet()
            selectedSecondaryMuscles = _secondaryMuscles.value?.toSet() ?: emptySet()

            // tymczasowe = to samo
            copySelectionsToTemp()
        }
    }

    fun copySelectionsToTemp() {
        tempLevels = selectedLevels.toSet()
        tempEquipments = selectedEquipments.toSet()
        tempCategories = selectedCategories.toSet()
        tempMechanics = selectedMechanics.toSet()
        tempForces = selectedForces.toSet()
        tempPrimaryMuscles = selectedPrimaryMuscles.toSet()
        tempSecondaryMuscles = selectedSecondaryMuscles.toSet()
    }

    fun confirmSelections() {
        selectedLevels = tempLevels
        selectedEquipments = tempEquipments
        selectedCategories = tempCategories
        selectedMechanics = tempMechanics
        selectedForces = tempForces
        selectedPrimaryMuscles = tempPrimaryMuscles
        selectedSecondaryMuscles = tempSecondaryMuscles

        applyFilters()
    }

    private fun applyFilters() {
        var filtered = allExercises

        if (selectedLevels.isNotEmpty()) {
            filtered = filtered.filter { it.level in selectedLevels }
        }
        if (selectedEquipments.isNotEmpty()) {
            filtered = filtered.filter { it.equipment == null || it.equipment in selectedEquipments }
        }
        if (selectedCategories.isNotEmpty()) {
            filtered = filtered.filter { it.category in selectedCategories }
        }
        if (selectedMechanics.isNotEmpty()) {
            filtered = filtered.filter { it.mechanic == null || it.mechanic in selectedMechanics }
        }
        if (selectedForces.isNotEmpty()) {
            filtered = filtered.filter { it.force == null || it.force in selectedForces }
        }
        if (selectedPrimaryMuscles.isNotEmpty()) {
            filtered = filtered.filter { ex -> ex.primaryMuscles.any { it in selectedPrimaryMuscles } }
        }
        if (selectedSecondaryMuscles.isNotEmpty()) {
            filtered = filtered.filter { ex -> ex.secondaryMuscles.any { it in selectedSecondaryMuscles } }
        }

        _exercises.value = filtered
    }
}
