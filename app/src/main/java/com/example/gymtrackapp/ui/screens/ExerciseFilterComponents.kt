package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Wspólne komponenty dla ekranów wyboru/filtrów ćwiczeń.
 */
@Composable
fun MultiSelectDropdown(
    label: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var localSelected by remember(selectedOptions) { mutableStateOf(selectedOptions.toMutableSet()) }

    // Synchronizacja – żeby wartości w dropdownie były aktualne po otwarciu dialogu
    LaunchedEffect(selectedOptions) {
        localSelected = selectedOptions.toMutableSet()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = "$label (${localSelected.size} wybrane)",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                val isChecked = option in localSelected
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    localSelected = localSelected.toMutableSet().apply {
                                        if (checked) add(option) else remove(option)
                                    }
                                    onSelectionChanged(localSelected)
                                }
                            )
                            Text(option)
                        }
                    },
                    onClick = {
                        // klik w wiersz też przełącza
                        val newChecked = !isChecked
                        localSelected = localSelected.toMutableSet().apply {
                            if (newChecked) add(option) else remove(option)
                        }
                        onSelectionChanged(localSelected)
                    }
                )
            }
        }
    }
}
