package com.example.delivery.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortDropdown(
    sortBy: com.example.delivery.viewmodel.delivery.SortOption,
    onSortByChange: (com.example.delivery.viewmodel.delivery.SortOption) -> Unit,
    sortOrder: com.example.delivery.viewmodel.delivery.SortOrder,
    onSortOrderChange: (com.example.delivery.viewmodel.delivery.SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val sortOptions = listOf(
        com.example.delivery.viewmodel.delivery.SortOption.SEQUENCE to "Séquence",
        com.example.delivery.viewmodel.delivery.SortOption.DISTANCE to "Distance",
        com.example.delivery.viewmodel.delivery.SortOption.QUANTITY to "Quantité",
        com.example.delivery.viewmodel.delivery.SortOption.DURATION to "Durée"
    )
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = sortOptions.find { it.first == sortBy }?.second ?: "Séquence",
            onValueChange = { },
            readOnly = true,
            label = { Text("Trier par") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            sortOptions.forEach { (option, label) ->
                DropdownMenuItem(
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (option == sortBy) FontWeight.SemiBold else FontWeight.Normal
                            )
                            if (option == sortBy) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    onClick = {
                        onSortByChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.width(12.dp))
    
    // Sort order toggle
    FilterChip(
        selected = true,
        onClick = { 
            onSortOrderChange(
                if (sortOrder == com.example.delivery.viewmodel.delivery.SortOrder.ASC) 
                    com.example.delivery.viewmodel.delivery.SortOrder.DESC 
                else 
                    com.example.delivery.viewmodel.delivery.SortOrder.ASC
            )
        },
        label = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (sortOrder == com.example.delivery.viewmodel.delivery.SortOrder.ASC) "A→Z" else "Z→A",
                    style = MaterialTheme.typography.labelMedium
                )
                Icon(
                    imageVector = if (sortOrder == com.example.delivery.viewmodel.delivery.SortOrder.ASC) 
                        Icons.Default.ArrowUpward 
                    else 
                        Icons.Default.ArrowDownward,
                    contentDescription = "Sort order",
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        modifier = Modifier
    )
}
