package cz.trask.zenid.sample.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import cz.trask.zenid.sample.viewmodel.MainViewModel
import cz.trask.zenid.sample.viewmodel.PickerType
import cz.trask.zenid.sample.viewmodel.PickerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickerScreen(
    pickerType: PickerType,
    mainViewModel: MainViewModel,
    pickerViewModel: PickerViewModel,
    onSelected: () -> Unit
) {
    val options by pickerViewModel.options.collectAsState()
    val mainState by mainViewModel.uiState.collectAsState()

    LaunchedEffect(pickerType, mainState.profiles, mainState.country, mainState.role) {
        pickerViewModel.loadOptions(pickerType, mainState.profiles, mainState.country, mainState.role)
    }

    val selectedValue = when (pickerType) {
        PickerType.COUNTRY -> mainState.country ?: ""
        PickerType.ROLE -> mainState.role ?: ""
        PickerType.PAGE -> mainState.page ?: ""
        PickerType.PROFILE -> mainState.selectedProfile
    }

    PickerScreenContent(
        pickerType = pickerType,
        options = options,
        selectedValue = selectedValue,
        onItemClick = { option ->
            when (pickerType) {
                PickerType.COUNTRY -> mainViewModel.setCountry(option)
                PickerType.ROLE -> mainViewModel.setRole(option)
                PickerType.PAGE -> mainViewModel.setPage(option)
                PickerType.PROFILE -> mainViewModel.setProfile(option)
            }
            onSelected()
        },
        onBack = onSelected,
        onReset = when (pickerType) {
            PickerType.COUNTRY -> null
            PickerType.ROLE -> { { mainViewModel.setRole(null); onSelected() } }
            PickerType.PAGE -> { { mainViewModel.setPage(null); onSelected() } }
            PickerType.PROFILE -> { { mainViewModel.setProfile(""); onSelected() } }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerScreenContent(
    pickerType: PickerType,
    options: List<String>,
    selectedValue: String,
    onItemClick: (String) -> Unit,
    onBack: () -> Unit,
    onReset: (() -> Unit)?
) {
    val title = when (pickerType) {
        PickerType.COUNTRY -> "Country"
        PickerType.ROLE -> "Role"
        PickerType.PAGE -> "Page"
        PickerType.PROFILE -> "Profile"
    }
    val resetLabel = when (pickerType) {
        PickerType.COUNTRY, PickerType.ROLE, PickerType.PAGE -> "None"
        PickerType.PROFILE -> "Default"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (onReset != null) {
                        TextButton(onClick = onReset) { Text(resetLabel) }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(options) { option ->
                val isSelected = option == selectedValue
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(option) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = option, modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider()
            }
        }
    }
}

@Preview(showBackground = true, name = "Picker - Country")
@Composable
private fun PickerScreenPreviewCountry() {
    MaterialTheme {
        PickerScreenContent(
            pickerType = PickerType.COUNTRY,
            options = listOf("Cz", "Sk", "De", "Pl", "At", "Hu"),
            selectedValue = "Cz",
            onItemClick = {},
            onBack = {},
            onReset = null
        )
    }
}

@Preview(showBackground = true, name = "Picker - Role")
@Composable
private fun PickerScreenPreviewRole() {
    MaterialTheme {
        PickerScreenContent(
            pickerType = PickerType.ROLE,
            options = listOf("Idc", "Drlic", "Pas", "Res"),
            selectedValue = "Idc",
            onItemClick = {},
            onBack = {},
            onReset = {}
        )
    }
}
