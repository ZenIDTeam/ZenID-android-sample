package cz.trask.zenid.sample.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cz.trask.zenid.sample.viewmodel.MainViewModel
import cz.trask.zenid.sample.viewmodel.PickerType
import cz.trask.zenid.sample.viewmodel.PickerViewModel

object Routes {
    const val MAIN = "main"
    const val QR_SCANNER = "qr_scanner"
    const val MS_LIVENESS = "ms_liveness/{restart}"
    const val NFC = "nfc"
    const val RESULT = "result"
    const val PICKER = "picker/{type}"
    const val SCANNER = "scanner/{verifier}"

    fun picker(type: String) = "picker/$type"
    fun scanner(verifier: String) = "scanner/$verifier"
    fun msLiveness(restart: Boolean = false) = "ms_liveness/$restart"
}

@Composable
fun AppNavigation(mainViewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            MainScreen(navController = navController, viewModel = mainViewModel)
        }
        composable(Routes.QR_SCANNER) {
            QrScannerScreen(
                onQrScanned = { url, apiKey ->
                    mainViewModel.onQrScanned(url, apiKey)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.PICKER,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("type") ?: "country"
            val pickerType = when (typeStr) {
                "country" -> PickerType.COUNTRY
                "role" -> PickerType.ROLE
                "page" -> PickerType.PAGE
                "profile" -> PickerType.PROFILE
                else -> PickerType.COUNTRY
            }
            val pickerViewModel: PickerViewModel = viewModel()
            PickerScreen(
                pickerType = pickerType,
                mainViewModel = mainViewModel,
                pickerViewModel = pickerViewModel,
                onSelected = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.SCANNER,
            arguments = listOf(navArgument("verifier") { type = NavType.StringType })
        ) { backStackEntry ->
            val verifierStr = backStackEntry.arguments?.getString("verifier") ?: "document"
            ScannerScreen(
                verifierType = verifierStr,
                mainViewModel = mainViewModel,
                navController = navController
            )
        }
        composable(
            route = Routes.MS_LIVENESS,
            arguments = listOf(navArgument("restart") { type = NavType.BoolType; defaultValue = false })
        ) { backStackEntry ->
            val restart = backStackEntry.arguments?.getBoolean("restart") ?: false
            MsLivenessScreen(
                mainViewModel = mainViewModel,
                navController = navController,
                restart = restart
            )
        }
        composable(Routes.NFC) {
            NfcScreen(
                mainViewModel = mainViewModel,
                onDone = { navController.navigate(Routes.RESULT) }
            )
        }
        composable(Routes.RESULT) {
            ResultScreen(
                mainViewModel = mainViewModel,
                onClose = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = false }
                    }
                }
            )
        }
    }
}
