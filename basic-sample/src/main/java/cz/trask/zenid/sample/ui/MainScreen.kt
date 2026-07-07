package cz.trask.zenid.sample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cz.trask.zenid.sample.viewmodel.MainUiState
import cz.trask.zenid.sample.viewmodel.MainViewModel
import cz.trask.zenid.sdk.ZenId
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    MainScreenContent(state, navController, onLogout = { viewModel.logout() })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContent(state: MainUiState, navController: NavController, onLogout: () -> Unit = {}) {
    val showLogoutDialog = remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    if (showLogoutDialog.value) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog.value = false },
            title = { Text("Logout") },
            text = { Text("This will clear all saved settings. You will need to scan the QR code again.") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog.value = false; onLogout() }) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog.value = false }) { Text("Cancel") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = { Text("ZenID Sample") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    actions = {
                        if (state.serverUrl.isNotEmpty()) {
                            TextButton(onClick = { showLogoutDialog.value = true }) { Text("Logout") }
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Section 1: Server + Profile
                item { SectionHeader("Server settings", topPadding = 16.dp) }
                item {
                    val host = state.serverUrl
                        .takeIf { it.isNotEmpty() }
                        ?.let { it.toUri().host?.takeIf { h -> h.isNotEmpty() } ?: it }
                        ?: "Select server"
                    OptionRow(
                        title = "Server",
                        value = host,
                        icon = Icons.Outlined.QrCodeScanner,
                        valueBold = true,
                        onClick = { navController.navigate(Routes.QR_SCANNER) }
                    )
                }
                item {
                    OptionRow(
                        title = "Profile",
                        value = state.selectedProfile.ifEmpty { "Default" },
                        icon = Icons.Outlined.ManageAccounts,
                        enabled = state.serverUrl.isNotEmpty(),
                        valueBold = true,
                        showDivider = false,
                        onClick = { navController.navigate(Routes.picker("profile")) }
                    )
                }

                item {
                    Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(MaterialTheme.colorScheme.surfaceContainer))
                }

                // Section 2: Document scanner settings
                item { SectionHeader("Document scanner settings", topPadding = 16.dp) }
                item {
                    OptionRow(
                        title = "Country",
                        value = state.country ?: "Any",
                        icon = Icons.Outlined.Language,
                        enabled = state.serverUrl.isNotEmpty(),
                        valueBold = true,
                        onClick = { navController.navigate(Routes.picker("country")) }
                    )
                }
                item {
                    OptionRow(
                        title = "Role",
                        value = state.role ?: "None",
                        icon = Icons.Outlined.Description,
                        enabled = state.serverUrl.isNotEmpty(),
                        valueBold = true,
                        onClick = { navController.navigate(Routes.picker("role")) }
                    )
                }
                item {
                    OptionRow(
                        title = "Page",
                        value = state.page ?: "None",
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        enabled = state.role != null,
                        valueBold = true,
                        showDivider = false,
                        onClick = { navController.navigate(Routes.picker("page")) }
                    )
                }

                item {
                    Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(MaterialTheme.colorScheme.surfaceContainer))
                }

                // Section 3: Verifiers (only when authorized)
                if (state.isAuthorized) {
                    item { SectionHeader("Verifiers", topPadding = 16.dp) }
                    item {
                        OptionRow(
                            title = "Document",
                            icon = Icons.Outlined.CreditCard,
                            onClick = { navController.navigate(Routes.scanner("document")) }
                        )
                    }
                    item {
                        OptionRow(
                            title = "Selfie",
                            icon = Icons.Outlined.Face,
                            onClick = { navController.navigate(Routes.scanner("selfie")) }
                        )
                    }
                    item {
                        OptionRow(
                            title = "Faceliveness",
                            icon = Icons.Outlined.Face,
                            onClick = { navController.navigate(Routes.scanner("face_liveness")) }
                        )
                    }
                    item {
                        OptionRow(
                            title = "MS Liveness",
                            icon = Icons.Outlined.Visibility,
                            onClick = { navController.navigate(Routes.MS_LIVENESS) }
                        )
                    }
                    item {
                        OptionRow(
                            title = "Hologram",
                            icon = Icons.Outlined.Style,
                            onClick = { navController.navigate(Routes.scanner("hologram")) }
                        )
                    }
                }

                // Version footer
                item {
                    val version = try { ZenId.get().androidLibVersion } catch (_: Exception) { "N/A" }
                    Text(
                        text = "SDK version $version",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                    )
                }
            }
        }

        // Loading overlay – matches iOS WaitView
        if (state.isAuthorizing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Text("Initializing...", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun OptionRow(
    title: String,
    value: String? = null,
    icon: ImageVector,
    enabled: Boolean = true,
    valueBold: Boolean = false,
    showDivider: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.38f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = title,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (valueBold) FontWeight.Bold else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
    if (showDivider) HorizontalDivider(modifier = Modifier.padding(start = 53.dp, end = 24.dp))
}

@Composable
private fun SectionHeader(title: String, topPadding: Dp = 30.dp) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = topPadding, bottom = 6.dp)
    )
}

@Preview(showBackground = true, name = "Main - Not authorized")
@Composable
private fun MainScreenPreviewNotAuthorized() {
    MaterialTheme {
        MainScreenContent(
            state = MainUiState(serverUrl = "", isAuthorized = false),
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, name = "Main - Authorized")
@Composable
private fun MainScreenPreviewAuthorized() {
    MaterialTheme {
        MainScreenContent(
            state = MainUiState(
                serverUrl = "https://api.example.com",
                isAuthorized = true,
                country = "Cz",
                selectedProfile = "Default"
            ),
            navController = rememberNavController()
        )
    }
}
