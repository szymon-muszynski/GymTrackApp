package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.gymtrackapp.data.entity.Exercise
import com.example.gymtrackapp.data.repository.ExerciseRepository
import kotlinx.coroutines.launch

class ExerciseViewModel(private val repository: ExerciseRepository): ViewModel() {

    // Bazowa lista z bazy (seed + custom) – zawsze aktualna
    private val allExercisesLive: LiveData<List<Exercise>> = repository.observeAllExercises().asLiveData()

    private val _exercises = MediatorLiveData<List<Exercise>>().apply { value = emptyList() }
    val exercises: LiveData<List<Exercise>> get() = _exercises

    // --- custom exercises ---
    val customExercises: LiveData<List<Exercise>> = repository.observeCustomExercises().asLiveData()

    private val _customExerciseDetails = MutableLiveData<Exercise?>(null)
    val customExerciseDetails: LiveData<Exercise?> get() = _customExerciseDetails

    private val _customExerciseError = MutableLiveData<String?>(null)
    val customExerciseError: LiveData<String?> get() = _customExerciseError

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

    init {
        _exercises.addSource(allExercisesLive) { list ->
            _exercises.value = applyFiltersTo(list)
        }
    }

    fun loadAllExercises() {
        viewModelScope.launch {
            repository.loadExercisesFromAssets()

            _levels.value = repository.exerciseDao.getAllLevels()
            _equipments.value = repository.exerciseDao.getAllEquipments()
            _categories.value = repository.exerciseDao.getAllCategories()
            _mechanics.value = repository.exerciseDao.getAllMechanics()
            _forces.value = repository.exerciseDao.getAllForces()
            _primaryMuscles.value = repository.getAllPrimaryMuscles()
            _secondaryMuscles.value = repository.getAllSecondaryMuscles()

            resetFiltersToAll()

            copySelectionsToTemp()

            _exercises.value = applyFiltersTo(allExercisesLive.value ?: emptyList())
        }
    }

    private fun resetFiltersToAll() {
        // jeśli listy są puste (np. jeszcze się ładują), zostaw emptySet = brak filtracji
        val levelsAll = _levels.value?.toSet().orEmpty()
        val equipmentsAll = _equipments.value?.toSet().orEmpty()
        val categoriesAll = _categories.value?.toSet().orEmpty()
        val mechanicsAll = _mechanics.value?.toSet().orEmpty()
        val forcesAll = _forces.value?.toSet().orEmpty()
        val primaryAll = _primaryMuscles.value?.toSet().orEmpty()
        val secondaryAll = _secondaryMuscles.value?.toSet().orEmpty()

        selectedLevels = levelsAll
        selectedEquipments = equipmentsAll
        selectedCategories = categoriesAll
        selectedMechanics = mechanicsAll
        selectedForces = forcesAll
        selectedPrimaryMuscles = primaryAll
        selectedSecondaryMuscles = secondaryAll
    }

    fun createCustomExercise(
        name: String,
        level: String,
        category: String,
        equipment: String?,
        primaryMuscles: List<String>,
        secondaryMuscles: List<String>,
        instructions: List<String>,
        force: String?,
        mechanic: String?,
        createdByUserId: String?
    ) {
        viewModelScope.launch {
            try {
                _customExerciseError.value = null
                repository.createCustomExercise(
                    name = name,
                    level = level,
                    category = category,
                    equipment = equipment,
                    primaryMuscles = primaryMuscles,
                    secondaryMuscles = secondaryMuscles,
                    instructions = instructions,
                    force = force,
                    mechanic = mechanic,
                    createdByUserId = createdByUserId
                )
                // nic nie musimy robić: allExercisesLive z Room wyemituje nową listę
            } catch (e: Exception) {
                _customExerciseError.value = e.message ?: "Nie udało się utworzyć ćwiczenia"
            }
        }
    }

    fun deleteCustomExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
            // Room wyemituje aktualną listę
        }
    }

    fun loadExerciseDetails(exerciseId: String) {
        viewModelScope.launch {
            _customExerciseDetails.value = repository.getExerciseById(exerciseId)
        }
    }

    fun clearCustomExerciseError() {
        _customExerciseError.value = null
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

        _exercises.value = applyFiltersTo(allExercisesLive.value ?: emptyList())
    }

    private fun applyFiltersTo(source: List<Exercise>): List<Exercise> {
        var filtered = source

        // Jeżeli "wszystkie" zostały ustawione jako puste (bo listy opcji były puste), to nie filtruj.
        fun Set<String>.isActiveFilter(options: LiveData<List<String>>): Boolean {
            val all = options.value
            return all != null && all.isNotEmpty() && this.isNotEmpty() && this.size != all.size
        }

        // Level
        if (selectedLevels.isActiveFilter(levels)) {
            filtered = filtered.filter { it.level in selectedLevels }
        }

        // Equipment (nullable)
        if (selectedEquipments.isActiveFilter(equipments)) {
            filtered = filtered.filter { it.equipment == null || it.equipment in selectedEquipments }
        }

        // Category
        if (selectedCategories.isActiveFilter(categories)) {
            filtered = filtered.filter { it.category in selectedCategories }
        }

        // Mechanic (nullable)
        if (selectedMechanics.isActiveFilter(mechanics)) {
            filtered = filtered.filter { it.mechanic == null || it.mechanic in selectedMechanics }
        }

        // Force (nullable)
        if (selectedForces.isActiveFilter(forces)) {
            filtered = filtered.filter { it.force == null || it.force in selectedForces }
        }

        // Muscles
        if (selectedPrimaryMuscles.isActiveFilter(primaryMuscles)) {
            filtered = filtered.filter { ex -> ex.primaryMuscles.any { it in selectedPrimaryMuscles } }
        }
        if (selectedSecondaryMuscles.isActiveFilter(secondaryMuscles)) {
            filtered = filtered.filter { ex -> ex.secondaryMuscles.any { it in selectedSecondaryMuscles } }
        }

        return filtered
    }
}
