package com.ghostmode.app.ui.contacts

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DialerSip
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghostmode.app.R
import com.ghostmode.app.data.GhostContact
import com.ghostmode.app.ui.MainViewModel
import com.ghostmode.app.ui.components.AlertCard
import com.ghostmode.app.ui.components.GhostCard
import com.ghostmode.app.ui.components.IconBadge
import com.ghostmode.app.ui.components.ScreenPadding
import com.ghostmode.app.ui.components.SectionHeader
import com.ghostmode.app.ui.components.SettingsGroup
import com.ghostmode.app.ui.components.SettingsRow
import com.ghostmode.app.ui.components.SubScreenTopBar
import com.ghostmode.app.ui.components.SwitchRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostContactsScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onPickContact: () -> Unit,
    onRequestRole: () -> Unit,
    onOpenDialer: (String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val enabled by vm.ghostContactsEnabled.collectAsStateWithLifecycle()
    val contacts by vm.ghostContacts.collectAsStateWithLifecycle()
    val silenceOnly by vm.ghostContactsSilenceOnly.collectAsStateWithLifecycle()
    val logCalls by vm.ghostContactsLogCalls.collectAsStateWithLifecycle()
    val isRoleHeld by vm.isCallScreeningRoleHeld.collectAsStateWithLifecycle()

    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SubScreenTopBar(
                title = stringResource(R.string.contacts_title),
                onBack = onBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Master toggle
            SettingsGroup {
                SwitchRow(
                    icon = Icons.Outlined.PersonOff,
                    title = stringResource(R.string.contacts_enable_title),
                    summary = stringResource(R.string.contacts_enable_summary),
                    checked = enabled,
                    onCheckedChange = vm::setGhostContactsEnabled
                )
            }

            // Role / permission check for Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isRoleHeld) {
                AlertCard(
                    icon = Icons.Outlined.Security,
                    title = stringResource(R.string.contacts_role_required_title),
                    text = stringResource(R.string.contacts_role_required_text),
                    container = MaterialTheme.colorScheme.tertiaryContainer,
                    content = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Button(onClick = onRequestRole) {
                        Text(stringResource(R.string.action_grant))
                    }
                }
            }

            // Call screening behavior settings
            SectionHeader(stringResource(R.string.contacts_section_behavior))
            SettingsGroup {
                SwitchRow(
                    icon = Icons.Outlined.CallEnd,
                    title = stringResource(R.string.contacts_reject_title),
                    summary = stringResource(R.string.contacts_reject_summary),
                    checked = !silenceOnly,
                    onCheckedChange = { reject -> vm.setGhostContactsSilenceOnly(!reject) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 66.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                SwitchRow(
                    icon = Icons.Outlined.History,
                    title = stringResource(R.string.contacts_log_title),
                    summary = stringResource(R.string.contacts_log_summary),
                    checked = logCalls,
                    onCheckedChange = vm::setGhostContactsLogCalls
                )
            }

            // Add Contact actions
            SectionHeader(stringResource(R.string.contacts_section_list))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onPickContact,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.contacts_pick_from_contacts))
                }
                OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.contacts_add_manually))
                }
            }

            // Contacts list
            if (contacts.isEmpty()) {
                GhostCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconBadge(Icons.Outlined.PersonOff, tint = MaterialTheme.colorScheme.outline, size = 44)
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.contacts_empty_text),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                SettingsGroup {
                    contacts.forEachIndexed { index, contact ->
                        ContactRow(
                            contact = contact,
                            onToggle = { vm.toggleGhostContact(contact.id) },
                            onDelete = { vm.removeGhostContact(contact.id) }
                        )
                        if (index < contacts.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 66.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // CFB Helper Card
            GhostCard(
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.contacts_cfb_guide_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.contacts_cfb_guide_text),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onOpenDialer("*#67#") }) {
                        Icon(Icons.Outlined.DialerSip, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.contacts_check_cfb))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, phone ->
                vm.addGhostContact(name, phone)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ContactRow(
    contact: GhostContact,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(
            icon = Icons.Outlined.Person,
            tint = if (contact.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            size = 36
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (contact.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = contact.phoneNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = if (contact.enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Switch(
            checked = contact.enabled,
            onCheckedChange = { onToggle() },
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.action_delete),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun AddContactDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.contacts_dialog_add_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.contacts_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.contacts_phone_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phone.isNotBlank()) {
                        val contactName = name.ifBlank { phone.trim() }
                        onAdd(contactName, phone.trim())
                    }
                },
                enabled = phone.isNotBlank()
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
