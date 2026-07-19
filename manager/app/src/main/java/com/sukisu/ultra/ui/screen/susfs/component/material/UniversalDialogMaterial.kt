package com.sukisu.ultra.ui.screen.susfs.component.material

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sukisu.ultra.R

sealed class DialogFieldMaterial {
    data class TextField(
        val value: String,
        val onValueChange: (String) -> Unit,
        val labelRes: Int,
        val enabled: Boolean = true,
        val modifier: Modifier = Modifier.fillMaxWidth()
    ) : DialogFieldMaterial()

    data class Dropdown(
        val titleRes: Int,
        val summary: String,
        val items: List<String>,
        val selectedIndex: Int,
        val onSelectedIndexChange: (Int) -> Unit,
        val enabled: Boolean = true
    ) : DialogFieldMaterial()

    data class CustomContent(
        val content: @Composable ColumnScope.() -> Unit
    ) : DialogFieldMaterial()
}

@Composable
fun UniversalDialogMaterial(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Boolean,
    titleRes: Int,
    isLoading: Boolean = false,
    fields: List<DialogFieldMaterial>,
    confirmTextRes: Int = R.string.add,
    cancelTextRes: Int = R.string.cancel,
    isConfirmEnabled: Boolean = true,
    scrollable: Boolean = false,
    onReset: (() -> Unit)? = null
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
                onReset?.invoke()
            },
            title = { Text(stringResource(titleRes)) },
            text = {
                val contentModifier = if (scrollable) {
                    Modifier.verticalScroll(rememberScrollState())
                } else {
                    Modifier
                }

                Column(
                    modifier = contentModifier,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    fields.forEach { field ->
                        when (field) {
                            is DialogFieldMaterial.TextField -> {
                                OutlinedTextField(
                                    value = field.value,
                                    onValueChange = field.onValueChange,
                                    label = { Text(stringResource(field.labelRes)) },
                                    modifier = field.modifier,
                                    enabled = field.enabled && !isLoading,
                                    singleLine = true
                                )
                            }
                            is DialogFieldMaterial.Dropdown -> {
                                var expanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = field.summary,
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text(stringResource(field.titleRes)) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        modifier = Modifier
                                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, field.enabled)
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        field.items.forEachIndexed { index, item ->
                                            DropdownMenuItem(
                                                text = { Text(item) },
                                                onClick = {
                                                    field.onSelectedIndexChange(index)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            is DialogFieldMaterial.CustomContent -> {
                                field.content.invoke(this)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (onConfirm()) {
                            onDismiss()
                            onReset?.invoke()
                        }
                    },
                    enabled = isConfirmEnabled && !isLoading
                ) {
                    Text(stringResource(confirmTextRes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                        onReset?.invoke()
                    }
                ) {
                    Text(stringResource(cancelTextRes))
                }
            }
        )
    }
}

@Composable
fun ConfirmDialogMaterial(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    titleRes: Int,
    messageRes: Int,
    isLoading: Boolean = false
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(titleRes)) },
            text = {
                Text(stringResource(messageRes))
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    enabled = !isLoading
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun InputDialogMaterial(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    titleRes: Int,
    labelRes: Int,
    initialValue: String = "",
    isLoading: Boolean = false,
    confirmTextRes: Int = R.string.confirm,
    cancelTextRes: Int = R.string.cancel
) {
    var textValue by remember { mutableStateOf(initialValue) }

    LaunchedEffect(showDialog, initialValue) {
        if (showDialog) {
            textValue = initialValue
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
                textValue = ""
            },
            title = { Text(stringResource(titleRes)) },
            text = {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text(stringResource(labelRes)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (textValue.isNotBlank()) {
                            onConfirm(textValue.trim())
                            textValue = ""
                        }
                    },
                    enabled = textValue.isNotBlank() && !isLoading
                ) {
                    Text(stringResource(confirmTextRes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                        textValue = ""
                    }
                ) {
                    Text(stringResource(cancelTextRes))
                }
            }
        )
    }
}
