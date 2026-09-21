package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DocumentItem
import com.example.data.model.FilterType
import com.example.ui.components.DocDetailsBottomSheet
import com.example.ui.components.RenameDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            val darkTheme = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            // Handle intent to open file from other apps
            LaunchedEffect(intent) {
                handleIncomingIntent(intent, viewModel)
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?, viewModel: MainViewModel) {
        if (intent == null) return
        val uri = intent.data
        if (intent.action == Intent.ACTION_VIEW && uri != null) {
            viewModel.importDocument(uri)
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val activeDoc by viewModel.activeDocument.collectAsState()
    val detailDoc by viewModel.detailDocument.collectAsState()
    val renameDoc by viewModel.renameDocument.collectAsState()
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importDocument(uri)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // MAIN APP SCAFFOLD
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (activeDoc == null) {
                    NavigationBar(
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { viewModel.setTab(0) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Home"
                                )
                            },
                            label = { Text("Home", fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_home")
                        )

                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { viewModel.setTab(1) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == 1) Icons.Filled.Folder else Icons.Outlined.Folder,
                                    contentDescription = "Library"
                                )
                            },
                            label = { Text("Library", fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_library")
                        )

                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = { viewModel.setTab(2) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == 2) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Starred"
                                )
                            },
                            label = { Text("Starred", fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_favorites")
                        )

                        NavigationBarItem(
                            selected = currentTab == 3,
                            onClick = { viewModel.setTab(3) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == 3) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Settings"
                                )
                            },
                            label = { Text("Settings", fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_settings")
                        )
                    }
                }
            },
            floatingActionButton = {
                if (activeDoc == null && currentTab != 3) {
                    FloatingActionButton(
                        onClick = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "*/*",
                                    "application/pdf",
                                    "application/msword",
                                    "application/vnd.openxmlformats-officedocument.*",
                                    "text/*",
                                    "image/*"
                                )
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .testTag("fab_import_doc")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Import document")
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onImportClick = {
                            filePickerLauncher.launch(arrayOf("*/*"))
                        },
                        onNavigateToLibrary = { filterType ->
                            viewModel.setFilter(filterType)
                            viewModel.setTab(1)
                        }
                    )
                    1 -> LibraryScreen(viewModel = viewModel)
                    2 -> FavoritesScreen(viewModel = viewModel)
                    3 -> SettingsScreen(viewModel = viewModel)
                }
            }
        }

        // FULL SCREEN DOCUMENT VIEWER OVERLAY
        AnimatedVisibility(
            visible = activeDoc != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            activeDoc?.let { doc ->
                DocumentViewerContainer(
                    doc = doc,
                    viewModel = viewModel
                )
            }
        }

        // DETAILS BOTTOM SHEET
        detailDoc?.let { doc ->
            DocDetailsBottomSheet(
                doc = doc,
                onDismiss = { viewModel.showDocumentDetails(null) },
                onRename = {
                    viewModel.showDocumentDetails(null)
                    viewModel.showRenameDialog(doc)
                },
                onShare = {
                    shareDoc(context, doc)
                },
                onToggleFavorite = {
                    viewModel.toggleFavorite(doc)
                },
                onDelete = {
                    viewModel.deleteDocument(doc)
                },
                onOpenExternal = {
                    openDocExternal(context, doc)
                }
            )
        }

        // RENAME DIALOG
        renameDoc?.let { doc ->
            RenameDialog(
                doc = doc,
                onDismiss = { viewModel.showRenameDialog(null) },
                onConfirm = { newName ->
                    viewModel.performRename(doc.id, newName)
                }
            )
        }

        // ONBOARDING OVERLAY (if not completed)
        if (!isOnboardingCompleted) {
            OnboardingScreen(
                onFinish = { viewModel.setOnboardingCompleted(true) }
            )
        }
    }
}

private fun shareDoc(context: android.content.Context, doc: DocumentItem) {
    try {
        val file = File(doc.path)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share ${doc.name}"))
    } catch (e: Exception) {
        // ignore
    }
}

private fun openDocExternal(context: android.content.Context, doc: DocumentItem) {
    try {
        val file = File(doc.path)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(viewIntent, "Open ${doc.name}"))
    } catch (e: Exception) {
        // ignore
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
