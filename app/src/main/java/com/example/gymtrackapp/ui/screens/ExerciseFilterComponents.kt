package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Box

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
    var showPicker by remember { mutableStateOf(false) }
    var localSelected by remember { mutableStateOf(selectedOptions) }

    // Synchronizacja – żeby wartości w dropdownie były aktualne po otwarciu dialogu
    LaunchedEffect(selectedOptions) {
        localSelected = selectedOptions
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = "$label (${localSelected.size} wybrane)",
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Text("▼", style = MaterialTheme.typography.labelMedium)
                }
            )

            // pełnoekranowy overlay klikający - cały prostokąt reaguje na tap
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { showPicker = true }
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(label, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    items(options) { option ->
                        val checked = option in localSelected
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    localSelected = if (checked) localSelected - option else localSelected + option
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    localSelected = if (isChecked) localSelected + option else localSelected - option
                                }
                            )
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSelectionChanged(localSelected)
                        showPicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        localSelected = selectedOptions
                        showPicker = false
                    }
                ) {
                    Text("Anuluj")
                }
            }
        )
    }
}
