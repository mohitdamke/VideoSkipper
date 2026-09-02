package com.mohit.videoskipper.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mohit.videoskipper.data.KeywordEntity
import com.mohit.videoskipper.presentation.components.ui.IconChip
import com.mohit.videoskipper.presentation.components.ui.ScanAccentDivider
import com.mohit.videoskipper.ui.theme.Coral500
import com.mohit.videoskipper.ui.theme.Ink400
import com.mohit.videoskipper.ui.theme.Signal500
import com.mohit.videoskipper.ui.theme.VideoSkipperTheme
import com.mohit.videoskipper.viewmodel.TextViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: TextViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },

        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Flagged Text", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                ScanAccentDivider()
            }
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddDialogOpen() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.TextFields,
                    contentDescription = "Add Text"
                )
            }
        }

    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                uiState.keywords.isEmpty() -> {
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = viewModel::onSearchQueryChange,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search flagged text...", color = Ink400) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = null,
                                        tint = Ink400
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(50.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        if (uiState.filteredKeywords.isEmpty()) {
                            item {
                                Text(
                                    text = "No results for \"${uiState.searchQuery}\"",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp),
                                    textAlign = TextAlign.Center,
                                    color = Ink400
                                )
                            }
                        }

                        items(uiState.filteredKeywords, key = { it.id }) { keyword ->
                            KeywordCard(
                                keyword = keyword,
                                onDelete = { viewModel.onDeleteKeyword(keyword) },
                                onToggleActive = { viewModel.onToggleActive(keyword) }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(72.dp)) } // clears the FAB
                    }
                }
            }
        }
    }

    if (uiState.isAddDialogVisible) {
        AddKeywordDialog(
            input = uiState.newKeywordInput,
            onInputChange = viewModel::onNewKeywordInputChange,
            onConfirm = viewModel::onConfirmAddKeyword,
            onDismiss = viewModel::onAddDialogDismiss
        )
    }
}

@Composable
private fun KeywordCard(
    keyword: KeywordEntity,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconChip(
                icon = Icons.Outlined.TextFields,
                isOn = keyword.isActive,
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = keyword.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            )

            Switch(
                checked = keyword.isActive,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.surface,
                    checkedTrackColor = Signal500
                ),
                onCheckedChange = { onToggleActive() }
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = Coral500
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconChip(
            icon = Icons.Outlined.TextFields,
            isOn = false,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No flagged text yet",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tap + to add a word like \"pizza\" to auto-skip.",
            style = MaterialTheme.typography.bodySmall,
            color = Ink400,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AddKeywordDialog(
    input: String,
    onInputChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Add flagged text", fontWeight = FontWeight.SemiBold) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                placeholder = { Text("e.g. pizza") },
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = input.isNotBlank()) {
                Text("Add", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun TextScreenPreview() {
    VideoSkipperTheme {
        TextScreen(navController = rememberNavController())
    }
}