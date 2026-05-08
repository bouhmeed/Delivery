package com.example.delivery.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSectionCard(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedStatuses: Set<String>,
    onStatusFilterChange: (Set<String>) -> Unit,
    selectedTypes: Set<String>,
    onTypeFilterChange: (Set<String>) -> Unit,
    customerQuery: String,
    onCustomerQueryChange: (String) -> Unit,
    sortBy: com.example.delivery.viewmodel.SortOption,
    onSortByChange: (com.example.delivery.viewmodel.SortOption) -> Unit,
    sortOrder: com.example.delivery.viewmodel.SortOrder,
    onSortOrderChange: (com.example.delivery.viewmodel.SortOrder) -> Unit,
    activeFiltersCount: Int,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusOptions = listOf("TO_PLAN", "EXPEDITION", "DELIVERED")
    val typeOptions = listOf("OUTBOUND", "INBOUND", "TRANSFER")
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with expand/collapse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left section - Title and badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Filters",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (activeFiltersCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error
                                ) {
                                    Text(
                                        text = activeFiltersCount.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Right section - Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (activeFiltersCount > 0) {
                        IconButton(
                            onClick = onClearFilters,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear filters",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { onExpandedChange(!isExpanded) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse filters" else "Expand filters",
                            tint = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            // Expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = LinearEasing
                    )
                ),
                exit = shrinkVertically(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutLinearInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = LinearEasing
                    )
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Status filter
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Statut",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(statusOptions) { option ->
                                    FilterChip(
                                        selected = option in selectedStatuses,
                                        onClick = {
                                            val newSelection = if (option in selectedStatuses) {
                                                selectedStatuses - option
                                            } else {
                                                selectedStatuses + option
                                            }
                                            onStatusFilterChange(newSelection)
                                        },
                                        label = { 
                                            Text(
                                                text = when (option) {
                                                    "TO_PLAN" -> "À planifier"
                                                    "EXPEDITION" -> "En expédition"
                                                    "DELIVERED" -> "Livrée"
                                                    else -> option
                                                },
                                                style = MaterialTheme.typography.labelMedium
                                            ) 
                                        },
                                        modifier = Modifier.animateContentSize()
                                    )
                                }
                            }
                        }
                    }
                    
                    // Type filter
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Type d'expédition",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(typeOptions) { option ->
                                    FilterChip(
                                        selected = option in selectedTypes,
                                        onClick = {
                                            val newSelection = if (option in selectedTypes) {
                                                selectedTypes - option
                                            } else {
                                                selectedTypes + option
                                            }
                                            onTypeFilterChange(newSelection)
                                        },
                                        label = { 
                                            Text(
                                                text = when (option) {
                                                    "OUTBOUND" -> "Sortant"
                                                    "INBOUND" -> "Entrant"
                                                    "TRANSFER" -> "Transfert"
                                                    else -> option
                                                },
                                                style = MaterialTheme.typography.labelMedium
                                            ) 
                                        },
                                        modifier = Modifier.animateContentSize()
                                    )
                                }
                            }
                        }
                    }
                    
                    // Customer search
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Recherche client",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            OutlinedTextField(
                                value = customerQuery,
                                onValueChange = onCustomerQueryChange,
                                placeholder = { Text("Nom du client...") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                    
                    // Sort options
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Tri",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            SortDropdown(
                                sortBy = sortBy,
                                onSortByChange = onSortByChange,
                                sortOrder = sortOrder,
                                onSortOrderChange = onSortOrderChange
                            )
                        }
                    }
                    
                    // Sort options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sort by
                        OutlinedTextField(
                            value = when (sortBy) {
                                com.example.delivery.viewmodel.SortOption.SEQUENCE -> "Séquence"
                                com.example.delivery.viewmodel.SortOption.DISTANCE -> "Distance"
                                com.example.delivery.viewmodel.SortOption.QUANTITY -> "Quantité"
                                com.example.delivery.viewmodel.SortOption.DURATION -> "Durée"
                            },
                            onValueChange = { /* Read-only */ },
                            label = { Text("Trier par") },
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuBox(
                                    expanded = false,
                                    onExpandedChange = { },
                                    modifier = Modifier
                                ) {
                                    // Dropdown menu items would go here
                                    // For now, using a simple approach
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Sort order
                        FilterChip(
                            selected = sortOrder == com.example.delivery.viewmodel.SortOrder.ASC,
                            onClick = { 
                                onSortOrderChange(
                                    if (sortOrder == com.example.delivery.viewmodel.SortOrder.ASC) 
                                        com.example.delivery.viewmodel.SortOrder.DESC 
                                    else 
                                        com.example.delivery.viewmodel.SortOrder.ASC
                                )
                            },
                            label = { 
                                Text(
                                    if (sortOrder == com.example.delivery.viewmodel.SortOrder.ASC) "A→Z" else "Z→A"
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (sortOrder == com.example.delivery.viewmodel.SortOrder.ASC) 
                                        Icons.Default.ArrowUpward 
                                    else 
                                        Icons.Default.ArrowDownward,
                                    contentDescription = "Sort order"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    optionLabels: Map<String, String>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(options) { option ->
                FilterChip(
                    selected = option in selectedOptions,
                    onClick = {
                        val newSelection = if (option in selectedOptions) {
                            selectedOptions - option
                        } else {
                            selectedOptions + option
                        }
                        onSelectionChange(newSelection)
                    },
                    label = { 
                        Text(
                            text = optionLabels[option] ?: option,
                            style = MaterialTheme.typography.labelMedium
                        ) 
                    }
                )
            }
        }
    }
}
