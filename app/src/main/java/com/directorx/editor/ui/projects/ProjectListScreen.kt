package com.directorx.editor.ui.projects

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.video.VideoFrameDecoder
import com.directorx.editor.R
import com.directorx.editor.engine.IncomingDocumentImportPreview
import com.directorx.editor.engine.IncomingDocumentImportStatus
import com.directorx.editor.engine.IncomingDocumentItem
import com.directorx.editor.engine.IncomingMediaItem
import com.directorx.editor.model.AspectRatio
import com.directorx.editor.model.CanvasDimensions
import com.directorx.editor.model.ExportConfig
import com.directorx.editor.model.Project
import com.directorx.editor.model.ProjectFilterMode
import com.directorx.editor.model.SortMode
import com.directorx.editor.ui.DirectorXTestTags
import com.directorx.editor.ui.editor.PremiumSnackbarHost
import com.directorx.editor.ui.editor.ToastSeverity
import com.directorx.editor.ui.editor.inferSeverity
import com.directorx.editor.ui.theme.DirectorXChromeIconButton
import com.directorx.editor.ui.theme.DirectorXDialogIcon
import com.directorx.editor.ui.theme.DirectorXFilterChip
import com.directorx.editor.ui.theme.DirectorXMetricPill
import com.directorx.editor.ui.theme.DirectorXPrimaryButton
import com.directorx.editor.ui.theme.DirectorXScreenBackground
import com.directorx.editor.ui.theme.DirectorXSectionHeader
import com.directorx.editor.ui.theme.DirectorXSecondaryButton
import com.directorx.editor.ui.theme.LocalDirectorXColors
import com.directorx.editor.ui.theme.Radius
import com.directorx.editor.ui.theme.Spacing
import java.util.Locale

private const val PROJECT_RENAME_MAX_CHARS = 80

@Composable
fun ProjectListScreen(
    onProjectSelected: (String) -> Unit,
    onSettings: () -> Unit = {},
    openNewProject: Boolean = false,
    onNewProjectOpened: () -> Unit = {},
    pendingImportItems: List<IncomingMediaItem> = emptyList(),
    onPendingImportHandled: () -> Unit = {},
    pendingDocumentItems: List<IncomingDocumentItem> = emptyList(),
    onPendingDocumentImportHandled: () -> Unit = {},
    viewModel: ProjectListViewModel = hiltViewModel()
) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val projectTotalCount by viewModel.projectTotalCount.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()
    val filterMode by viewModel.filterMode.collectAsStateWithLifecycle()
    val userTemplates by viewModel.userTemplates.collectAsStateWithLifecycle()
    val restorableTemplate by viewModel.restorableTemplate.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val loadFailed by viewModel.loadFailed.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val operationState by viewModel.operationState.collectAsStateWithLifecycle()
    val documentImportPreview by viewModel.documentImportPreview.collectAsStateWithLifecycle()
    val crashReportNotice by viewModel.crashReportNotice.collectAsStateWithLifecycle()
    val crashReportSaving by viewModel.crashReportSaving.collectAsStateWithLifecycle()
    val actionsEnabled = operationState == null
    val currentLocale = LocalConfiguration.current.locales[0]
    val hasAnyProjects = projectTotalCount > 0
    var showTemplateSheet by remember { mutableStateOf(false) }
    var showCustomCanvasDialog by remember { mutableStateOf(false) }
    var customCanvasWidthText by remember { mutableStateOf("1920") }
    var customCanvasHeightText by remember { mutableStateOf("1080") }
    var customCanvasProjectName by remember { mutableStateOf("") }
    var pendingCrashReportNotice by remember { mutableStateOf<CrashReportNotice?>(null) }
    val templateImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importTemplate(uri)
        }
    }
    val crashReportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        val requestedNotice = pendingCrashReportNotice
        pendingCrashReportNotice = null
        if (uri != null) {
            viewModel.saveCrashReport(uri, requestedNotice)
        }
    }

    LaunchedEffect(openNewProject) {
        if (openNewProject) {
            showTemplateSheet = true
            onNewProjectOpened()
        }
    }

    LaunchedEffect(pendingImportItems) {
        if (pendingImportItems.isNotEmpty()) {
            val items = pendingImportItems
            onPendingImportHandled()
            viewModel.createProjectFromImports(items) { projectId ->
                onProjectSelected(projectId)
            }
        }
    }

    LaunchedEffect(pendingDocumentItems) {
        if (pendingDocumentItems.isNotEmpty()) {
            val items = pendingDocumentItems
            onPendingDocumentImportHandled()
            viewModel.previewIncomingDocuments(items)
        }
    }

    DirectorXScreenBackground(
        modifier = Modifier
            .fillMaxSize()
            .testTag(DirectorXTestTags.PROJECTS_SCREEN)
    ) {
        val importTemplate = { templateImportLauncher.launch(arrayOf("*/*")) }
        val showCollectionControls = projectTotalCount > 1 ||
            searchQuery.isNotBlank() ||
            filterMode != ProjectFilterMode.ALL

        Column(
            modifier = Modifier
                .fillMaxSize()
                // The template picker is a modal surface. Keep the project
                // controls visible behind it, but remove their semantics from
                // the accessibility tree while the picker owns interaction.
                .then(if (showTemplateSheet) Modifier.clearAndSetSemantics { } else Modifier)
        ) {
            ProjectHomeHero(
                searchQuery = searchQuery,
                sortMode = sortMode,
                onSearchQueryChanged = viewModel::setSearchQuery,
                onClearSearch = { viewModel.setSearchQuery("") },
                onSortModeChanged = viewModel::setSortMode,
                onCreateProject = { showTemplateSheet = true },
                onImportTemplate = importTemplate,
                onSettings = onSettings,
                showSearch = showCollectionControls,
                showSortControls = showCollectionControls && projects.isNotEmpty(),
                actionsEnabled = actionsEnabled
            )

            AnimatedVisibility(
                visible = crashReportNotice != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                crashReportNotice?.let { notice ->
                    CrashReportBanner(
                        notice = notice,
                        saving = crashReportSaving,
                        onSaveReport = {
                            pendingCrashReportNotice = notice
                            crashReportLauncher.launch(CrashReportNoticePolicy.reportFileName(notice))
                        },
                        onCopySummary = { viewModel.copyCrashReportSummary(notice) },
                        onDismiss = { viewModel.dismissCrashReportNotice(notice) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                    )
                }
            }

            if (showCollectionControls) {
                ProjectFilterChipsRow(
                    filterMode = filterMode,
                    onFilterModeChanged = viewModel::setFilterMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                )
            }

            AnimatedVisibility(
                visible = operationState != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                operationState?.let { operation ->
                    ProjectOperationCard(
                        operation = operation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                    )
                }
            }

            // Trash state is hoisted above the empty/non-empty branch on purpose.
            // Deleting the last active project used to switch the screen to a
            // bare empty state that never rendered the trash section — hiding
            // Restore, the only way back, at exactly the moment it is needed.
            val trashed by viewModel.trashedProjects.collectAsStateWithLifecycle()
            var showTrash by remember { mutableStateOf(false) }
            var confirmEmptyTrash by remember { mutableStateOf(false) }
            var pendingDeleteForever by remember { mutableStateOf<Project?>(null) }
            LaunchedEffect(projects.isEmpty(), trashed.isEmpty()) {
                // With nothing active, the trash is the whole screen's content:
                // expand it so Restore is reachable without a second tap.
                if (ProjectListTrashVisibilityPolicy.autoExpandsTrash(projects.size, trashed.size)) {
                    showTrash = true
                }
            }

            if (ProjectListTrashVisibilityPolicy.showsLoadFailureState(
                    projects.size,
                    trashed.size,
                    loadFailed
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        Text(
                            text = stringResource(R.string.projects_load_failed_title),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.projects_load_failed_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { viewModel.retryLoadProjects() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            } else if (ProjectListTrashVisibilityPolicy.showsLoadingState(projects.size, trashed.size, isLoading)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (ProjectListTrashVisibilityPolicy.showsFullScreenEmptyState(
                    projects.size,
                    trashed.size,
                    isLoading
                )
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
                    contentAlignment = Alignment.TopCenter
                ) {
                    ProjectEmptyState(
                        projectTotalCount = projectTotalCount,
                        searchQuery = searchQuery,
                        filterMode = filterMode,
                        onCreateProject = { showTemplateSheet = true },
                        onShowAllProjects = {
                            viewModel.setSearchQuery("")
                            viewModel.setFilterMode(ProjectFilterMode.ALL)
                        },
                        actionsEnabled = actionsEnabled,
                        // The hero already exposes the primary New Project action.
                        // Hide the redundant shortcut grid when expanded text or
                        // pseudo-localized copy leaves too little room, rather than
                        // clipping small, unreachable touch targets.
                        showTemplateShortcuts = maxHeight >= 280.dp
                    )
                }
            } else {
                val hasActiveSearch = searchQuery.isNotBlank()
                val hasActiveFilter = filterMode != ProjectFilterMode.ALL
                val sortLabel = sortMode.localizedLabel()
                val filterLabel = filterMode.localizedLabel()
                if (projects.isNotEmpty()) DirectorXSectionHeader(
                    title = if (hasActiveSearch) {
                        if (projects.size == 1) {
                            stringResource(R.string.projects_results_count_one)
                        } else {
                            stringResource(R.string.projects_results_count_many, projects.size)
                        }
                    } else if (hasActiveFilter) {
                        filterLabel
                    } else {
                        stringResource(R.string.projects_recent)
                    },
                    description = if (hasActiveSearch && hasActiveFilter) {
                        stringResource(
                            R.string.projects_filtered_sorted_summary,
                            filterLabel.lowercase(currentLocale),
                            sortLabel.lowercase(currentLocale)
                        )
                    } else if (hasActiveSearch) {
                        stringResource(
                            R.string.projects_sorted_summary,
                            sortLabel.lowercase(currentLocale)
                        )
                    } else if (hasActiveFilter) {
                        stringResource(
                            R.string.projects_filter_count_sorted_summary,
                            projects.size,
                            projectTotalCount,
                            sortLabel.lowercase(currentLocale)
                        )
                    } else null,
                    modifier = Modifier.padding(start = Spacing.xl, end = Spacing.xl, top = 14.dp, bottom = Spacing.sm),
                    trailing = {
                        DirectorXMetricPill(
                            text = sortLabel,
                            accent = LocalDirectorXColors.current.accent,
                            icon = Icons.Default.FilterList
                        )
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (ProjectListTrashVisibilityPolicy.showsInlineEmptyState(projects.size, trashed.size)) {
                        item(key = "__empty_state") {
                            ProjectEmptyState(
                                projectTotalCount = projectTotalCount,
                                searchQuery = searchQuery,
                                filterMode = filterMode,
                                onCreateProject = { showTemplateSheet = true },
                                onShowAllProjects = {
                                    viewModel.setSearchQuery("")
                                    viewModel.setFilterMode(ProjectFilterMode.ALL)
                                },
                                actionsEnabled = actionsEnabled,
                                scrollable = false
                            )
                        }
                    }

                    items(projects, key = { it.id }) { project ->
                        ProjectCard(
                            project = project,
                            onClick = { onProjectSelected(project.id) },
                            onRename = { newName -> viewModel.renameProject(project, newName) },
                            onDelete = { viewModel.deleteProject(project) },
                            onDuplicate = { viewModel.duplicateProject(project) }
                        )
                    }

                    if (!hasActiveSearch && !hasActiveFilter) {
                        item(key = "__templates_launcher") {
                            ProjectTemplateLibraryRow(
                                templateCount = projectTemplates.size,
                                enabled = actionsEnabled,
                                onClick = { showTemplateSheet = true },
                            )
                        }
                    }

                    if (trashed.isNotEmpty()) {
                        item(key = "__trash_header") {
                            TrashSectionHeader(
                                count = trashed.size,
                                expanded = showTrash,
                                onToggle = { showTrash = !showTrash },
                                onEmptyTrash = { confirmEmptyTrash = true }
                            )
                        }

                        if (showTrash) {
                            items(trashed, key = { "trash_${it.id}" }) { project ->
                                TrashedProjectCard(
                                    project = project,
                                    onRestore = { viewModel.restoreProject(project) },
                                    onDeleteForever = { pendingDeleteForever = project }
                                )
                            }
                        }
                    }
                }

                // Permanent deletions get an explicit confirmation: the trash IS
                // the undo path, so purging it must not ride on a single mis-tap
                // (the button sits directly beside the expand/collapse header).
                if (confirmEmptyTrash) {
                    AlertDialog(
                        onDismissRequest = { confirmEmptyTrash = false },
                        icon = { DirectorXDialogIcon(icon = Icons.Default.DeleteForever, accent = LocalDirectorXColors.current.danger) },
                        title = {
                            Text(
                                text = stringResource(R.string.trash_empty_confirm_title),
                                color = LocalDirectorXColors.current.text,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        text = {
                            Text(
                                text = pluralStringResource(
                                    R.plurals.trash_empty_confirm_message,
                                    trashed.size,
                                    trashed.size
                                ),
                                color = LocalDirectorXColors.current.subtext,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        confirmButton = {
                            DirectorXSecondaryButton(
                                text = stringResource(R.string.trash_empty_confirm_action),
                                onClick = {
                                    viewModel.emptyTrash()
                                    confirmEmptyTrash = false
                                },
                                icon = Icons.Default.DeleteForever,
                                contentColor = LocalDirectorXColors.current.danger
                            )
                        },
                        dismissButton = {
                            DirectorXSecondaryButton(
                                text = stringResource(R.string.cancel),
                                onClick = { confirmEmptyTrash = false }
                            )
                        },
                        containerColor = LocalDirectorXColors.current.panelHighest,
                        titleContentColor = LocalDirectorXColors.current.text,
                        textContentColor = LocalDirectorXColors.current.subtext,
                        shape = RoundedCornerShape(Radius.xxl)
                    )
                }

                pendingDeleteForever?.let { doomed ->
                    AlertDialog(
                        onDismissRequest = { pendingDeleteForever = null },
                        icon = { DirectorXDialogIcon(icon = Icons.Default.DeleteForever, accent = LocalDirectorXColors.current.danger) },
                        title = {
                            Text(
                                text = stringResource(R.string.trash_delete_forever_title),
                                color = LocalDirectorXColors.current.text,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.trash_delete_forever_message, doomed.name),
                                color = LocalDirectorXColors.current.subtext,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        confirmButton = {
                            DirectorXSecondaryButton(
                                text = stringResource(R.string.trash_delete_forever_action),
                                onClick = {
                                    viewModel.deleteProjectForever(doomed)
                                    pendingDeleteForever = null
                                },
                                icon = Icons.Default.DeleteForever,
                                contentColor = LocalDirectorXColors.current.danger
                            )
                        },
                        dismissButton = {
                            DirectorXSecondaryButton(
                                text = stringResource(R.string.cancel),
                                onClick = { pendingDeleteForever = null }
                            )
                        },
                        containerColor = LocalDirectorXColors.current.panelHighest,
                        titleContentColor = LocalDirectorXColors.current.text,
                        textContentColor = LocalDirectorXColors.current.subtext,
                        shape = RoundedCornerShape(Radius.xxl)
                    )
                }
            }
        }

        // Template picker
        if (showTemplateSheet) {
            val untitledProjectName = stringResource(R.string.project_untitled)
            ProjectTemplateSheet(
                onTemplateSelected = { template, templateName ->
                    showTemplateSheet = false
                    if (template.aspectRatio == AspectRatio.CUSTOM) {
                        customCanvasProjectName = templateName
                        showCustomCanvasDialog = true
                    } else {
                        viewModel.createProject(
                            name = if (template.id == "blank") untitledProjectName else templateName,
                            aspectRatio = template.aspectRatio,
                            templateId = template.id,
                            trackTypes = template.tracks
                        ) { id -> onProjectSelected(id) }
                    }
                },
                onUserTemplateSelected = { userTemplate ->
                    showTemplateSheet = false
                    viewModel.createFromTemplate(userTemplate) { id ->
                        onProjectSelected(id)
                    }
                },
                onShareTemplate = viewModel::shareTemplate,
                onImportTemplate = importTemplate,
                onDeleteUserTemplate = viewModel::deleteUserTemplate,
                userTemplates = userTemplates,
                onDismiss = { showTemplateSheet = false },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (showCustomCanvasDialog) {
            val fallbackName = stringResource(R.string.template_custom_canvas_name)
            val width = customCanvasWidthText.toIntOrNull()
            val height = customCanvasHeightText.toIntOrNull()
            val dimensionsValid = width != null && height != null &&
                CanvasDimensions.isValid(width, height)
            AlertDialog(
                onDismissRequest = { showCustomCanvasDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.project_custom_canvas_title),
                        color = LocalDirectorXColors.current.text,
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = customCanvasWidthText,
                            onValueChange = { customCanvasWidthText = it.filter(Char::isDigit).take(5) },
                            label = { Text(stringResource(R.string.project_custom_canvas_width)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                            ),
                        )
                        OutlinedTextField(
                            value = customCanvasHeightText,
                            onValueChange = { customCanvasHeightText = it.filter(Char::isDigit).take(5) },
                            label = { Text(stringResource(R.string.project_custom_canvas_height)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                        )
                        if (!dimensionsValid) {
                            Text(
                                text = stringResource(R.string.project_custom_canvas_invalid),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = dimensionsValid,
                        onClick = {
                            val validWidth = width ?: return@TextButton
                            val validHeight = height ?: return@TextButton
                            viewModel.createProject(
                                name = customCanvasProjectName.ifBlank { fallbackName },
                                aspectRatio = AspectRatio.CUSTOM,
                                templateId = "custom_canvas",
                                customWidth = validWidth,
                                customHeight = validHeight,
                                trackTypes = listOf(
                                    com.directorx.editor.model.TrackType.VIDEO,
                                    com.directorx.editor.model.TrackType.AUDIO,
                                ),
                            ) { id -> onProjectSelected(id) }
                            showCustomCanvasDialog = false
                        },
                    ) {
                        Text(stringResource(R.string.project_custom_canvas_create))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomCanvasDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                containerColor = LocalDirectorXColors.current.panelHighest,
                titleContentColor = LocalDirectorXColors.current.text,
                textContentColor = LocalDirectorXColors.current.subtext,
                shape = RoundedCornerShape(Radius.xxl),
            )
        }

        documentImportPreview?.let { preview ->
            IncomingDocumentImportDialog(
                preview = preview,
                onConfirm = viewModel::importPreviewedDocument,
                onDismiss = viewModel::dismissDocumentImportPreview
            )
        }

        PremiumSnackbarHost(
            message = toastMessage,
            severity = toastMessage?.let(::inferSeverity) ?: ToastSeverity.Info,
            actionLabel = if (restorableTemplate != null) {
                stringResource(R.string.project_template_restore_action)
            } else {
                null
            },
            onAction = if (restorableTemplate != null) {
                viewModel::restoreDeletedTemplate
            } else {
                null
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
        )
    }
}

@Composable
private fun IncomingDocumentImportDialog(
    preview: IncomingDocumentImportPreview,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val accent = when (preview.status) {
        IncomingDocumentImportStatus.READY,
        IncomingDocumentImportStatus.IMPORTED -> LocalDirectorXColors.current.success
        IncomingDocumentImportStatus.BLOCKED -> LocalDirectorXColors.current.warning
        IncomingDocumentImportStatus.INVALID -> LocalDirectorXColors.current.danger
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            DirectorXDialogIcon(
                icon = when (preview.status) {
                    IncomingDocumentImportStatus.READY -> Icons.Default.Description
                    IncomingDocumentImportStatus.IMPORTED -> Icons.Default.TaskAlt
                    IncomingDocumentImportStatus.BLOCKED -> Icons.Default.PendingActions
                    IncomingDocumentImportStatus.INVALID -> Icons.Default.ReportProblem
                },
                accent = accent
            )
        },
        title = {
            Text(
                text = preview.title,
                color = LocalDirectorXColors.current.text,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = preview.body,
                    color = LocalDirectorXColors.current.subtext,
                    style = MaterialTheme.typography.bodyMedium
                )
                preview.details.forEach { detail ->
                    DocumentReportLine(text = detail, color = LocalDirectorXColors.current.text)
                }
                preview.warnings.forEach { warning ->
                    DocumentReportLine(text = warning, color = LocalDirectorXColors.current.warning)
                }
            }
        },
        confirmButton = {
            if (preview.canImportNow) {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.project_document_import_confirm))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
        containerColor = LocalDirectorXColors.current.panelHighest,
        titleContentColor = LocalDirectorXColors.current.text,
        textContentColor = LocalDirectorXColors.current.subtext
    )
}

@Composable
private fun DocumentReportLine(
    text: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = color.copy(alpha = 0.84f),
            modifier = Modifier
                .padding(top = 7.dp)
                .size(6.dp)
        )
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectHomeHero(
    searchQuery: String,
    sortMode: SortMode,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSortModeChanged: (SortMode) -> Unit,
    onCreateProject: () -> Unit,
    onImportTemplate: () -> Unit,
    onSettings: () -> Unit,
    showSearch: Boolean,
    showSortControls: Boolean,
    actionsEnabled: Boolean
) {
    // Keep the primary project actions fully visible while leaving enough room
    // for the project list on phone-sized screens. Search/sort remain scrollable.
    // A 50% cap clipped the create button to a 6dp visible strip on API 35 phones.
    val maximumHeight = (LocalConfiguration.current.screenHeightDp * 0.65f)
        .coerceAtLeast(320f)
        .dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maximumHeight)
            .verticalScroll(rememberScrollState())
            .background(LocalDirectorXColors.current.background)
            .padding(horizontal = 20.dp, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.projects_app_title),
                color = LocalDirectorXColors.current.text,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            DirectorXChromeIconButton(
                icon = Icons.Default.Settings,
                contentDescription = stringResource(R.string.projects_settings),
                onClick = onSettings,
                modifier = Modifier.testTag(DirectorXTestTags.PROJECTS_SETTINGS)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(
                text = stringResource(R.string.projects_ready_title),
                color = LocalDirectorXColors.current.text,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.projects_ready_body),
                color = LocalDirectorXColors.current.subtext,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.widthIn(max = 520.dp)
            )
        }

        ProjectActionRow(
            primaryLabel = stringResource(R.string.projects_new_project),
            primaryIcon = Icons.Default.Add,
            onPrimary = onCreateProject,
            secondaryLabel = stringResource(R.string.project_document_import_confirm),
            secondaryIcon = Icons.Default.FileOpen,
            onSecondary = onImportTemplate,
            enabled = actionsEnabled,
            primaryTestTag = DirectorXTestTags.PROJECTS_CREATE_PROJECT,
            secondaryTestTag = DirectorXTestTags.PROJECTS_IMPORT_TEMPLATE
        )

        HorizontalDivider(color = LocalDirectorXColors.current.cardStroke)

        if (showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = {
                    Text(
                        text = stringResource(R.string.projects_search_placeholder),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.projects_search),
                        tint = LocalDirectorXColors.current.subtext,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        DirectorXChromeIconButton(
                            icon = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.projects_clear),
                            onClick = onClearSearch,
                            size = 40.dp
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.lg),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = LocalDirectorXColors.current.panelRaised.copy(alpha = 0.92f),
                    unfocusedContainerColor = LocalDirectorXColors.current.panelRaised.copy(alpha = 0.82f),
                    focusedBorderColor = LocalDirectorXColors.current.accentTertiary.copy(alpha = 0.55f),
                    unfocusedBorderColor = LocalDirectorXColors.current.cardStroke,
                    cursorColor = LocalDirectorXColors.current.accentTertiary,
                    focusedTextColor = LocalDirectorXColors.current.text,
                    unfocusedTextColor = LocalDirectorXColors.current.text,
                    focusedPlaceholderColor = LocalDirectorXColors.current.overlayStrong,
                    unfocusedPlaceholderColor = LocalDirectorXColors.current.overlayStrong
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = LocalDirectorXColors.current.text)
            )
        }

        if (showSortControls) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(SortMode.entries.toList()) { mode ->
                    DirectorXFilterChip(
                        onClick = { onSortModeChanged(mode) },
                        text = mode.localizedLabel(),
                        selected = sortMode == mode,
                        accent = LocalDirectorXColors.current.accentTertiary,
                        icon = if (sortMode == mode) Icons.Default.Check else null
                    )
                }
            }
        }

        HorizontalDivider(color = LocalDirectorXColors.current.cardStroke.copy(alpha = 0.82f))
    }
}

@Composable
private fun ProjectHomeReadinessRow(
    projectCount: Int,
    savedTemplateCount: Int,
    modifier: Modifier = Modifier
) {
    val exportDefaults = remember { ExportConfig() }
    val mediaHealthValue = if (projectCount == 0) {
        stringResource(R.string.projects_media_health_ready_value)
    } else {
        pluralStringResource(
            R.plurals.projects_media_health_projects_value,
            projectCount,
            projectCount
        )
    }
    BoxWithConstraints(modifier = modifier) {
        val stackCards = maxWidth < 520.dp
        val arrangement = Arrangement.spacedBy(Spacing.sm)
        if (stackCards) {
            Column(verticalArrangement = arrangement) {
                ProjectReadinessCard(
                    title = stringResource(R.string.projects_media_health_title),
                    value = mediaHealthValue,
                    body = stringResource(R.string.projects_media_health_body),
                    icon = Icons.Default.Verified,
                    accent = LocalDirectorXColors.current.success,
                    modifier = Modifier.fillMaxWidth()
                )
                ProjectReadinessCard(
                    title = stringResource(R.string.projects_render_ready_title),
                    value = stringResource(
                        R.string.projects_render_ready_value,
                        exportDefaults.codec.label,
                        exportDefaults.resolution.label,
                        exportDefaults.frameRate
                    ),
                    body = stringResource(R.string.projects_render_ready_body, projectTemplates.size + savedTemplateCount),
                    icon = Icons.Default.Speed,
                    accent = LocalDirectorXColors.current.accentTertiary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(horizontalArrangement = arrangement) {
                ProjectReadinessCard(
                    title = stringResource(R.string.projects_media_health_title),
                    value = mediaHealthValue,
                    body = stringResource(R.string.projects_media_health_body),
                    icon = Icons.Default.Verified,
                    accent = LocalDirectorXColors.current.success,
                    modifier = Modifier.weight(1f)
                )
                ProjectReadinessCard(
                    title = stringResource(R.string.projects_render_ready_title),
                    value = stringResource(
                        R.string.projects_render_ready_value,
                        exportDefaults.codec.label,
                        exportDefaults.resolution.label,
                        exportDefaults.frameRate
                    ),
                    body = stringResource(R.string.projects_render_ready_body, projectTemplates.size + savedTemplateCount),
                    icon = Icons.Default.Speed,
                    accent = LocalDirectorXColors.current.accentTertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProjectReadinessCard(
    title: String,
    value: String,
    body: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalDirectorXColors.current
    Surface(
        modifier = modifier.semantics {
            contentDescription = "$title. $value. $body"
        },
        color = colors.panel.copy(alpha = 0.92f),
        shape = RoundedCornerShape(Radius.lg),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (colors.highContrast) colors.cardStrokeStrong else colors.cardStroke.copy(alpha = 0.88f)
        )
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = accent.copy(alpha = 0.13f),
                shape = RoundedCornerShape(Radius.md),
                border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.28f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    color = colors.text,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    color = accent,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = body,
                    color = colors.subtext,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProjectOperationCard(
    operation: ProjectListOperationState,
    modifier: Modifier = Modifier
) {
    val colors = LocalDirectorXColors.current
    Surface(
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        color = colors.panelHighest,
        shape = RoundedCornerShape(Radius.lg),
        border = androidx.compose.foundation.BorderStroke(1.dp, LocalDirectorXColors.current.accentTertiary.copy(alpha = 0.26f))
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Surface(
                    color = LocalDirectorXColors.current.accentTertiary.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(Radius.lg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LocalDirectorXColors.current.accentTertiary.copy(alpha = 0.22f))
                ) {
                    CircularProgressIndicator(
                        color = LocalDirectorXColors.current.accentTertiary,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = operation.title,
                        color = colors.text,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = operation.description,
                        color = colors.subtext,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(Radius.sm)),
                color = LocalDirectorXColors.current.accentTertiary,
                trackColor = LocalDirectorXColors.current.surface
            )
        }
    }
}

@Composable
private fun ProjectFilterChipsRow(
    filterMode: ProjectFilterMode,
    onFilterModeChanged: (ProjectFilterMode) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(ProjectFilterMode.entries.toList()) { mode ->
            DirectorXFilterChip(
                onClick = { onFilterModeChanged(mode) },
                text = mode.localizedLabel(),
                selected = filterMode == mode,
                accent = LocalDirectorXColors.current.accentTertiary,
                icon = if (filterMode == mode) Icons.Default.Check else null
            )
        }
    }
}

@Composable
private fun ProjectEmptyState(
    projectTotalCount: Int,
    searchQuery: String,
    filterMode: ProjectFilterMode,
    onCreateProject: () -> Unit,
    onShowAllProjects: () -> Unit,
    actionsEnabled: Boolean,
    showTemplateShortcuts: Boolean = true,
    // A LazyColumn item is measured with an unbounded height, and a vertical
    // scroll inside one throws on the first frame. Inline callers pass false and
    // let the list scroll (issue #54: a trash-only dashboard crashed every launch).
    scrollable: Boolean = true
) {
    val hasAnyProjects = projectTotalCount > 0
    val hasActiveSearch = searchQuery.isNotBlank()
    val hasActiveFilter = filterMode != ProjectFilterMode.ALL
    val isConstrainedEmpty = hasAnyProjects && (hasActiveSearch || hasActiveFilter)
    val colors = LocalDirectorXColors.current
    val iconTint = if (isConstrainedEmpty) LocalDirectorXColors.current.accent else LocalDirectorXColors.current.accentTertiary
    val title = projectEmptyStateTitle(
        isConstrainedEmpty = isConstrainedEmpty,
        hasActiveSearch = hasActiveSearch,
        hasActiveFilter = hasActiveFilter,
        filterLabel = filterMode.localizedLabel()
    )
    val body = projectEmptyStateBody(
        isConstrainedEmpty = isConstrainedEmpty,
        hasActiveSearch = hasActiveSearch,
        hasActiveFilter = hasActiveFilter
    )
    val templatesDescription = stringResource(
        R.string.projects_templates_count,
        projectTemplates.size
    )

    if (!isConstrainedEmpty) {
        if (showTemplateShortcuts) Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(bottom = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .clickable(enabled = actionsEnabled, role = Role.Button, onClick = onCreateProject)
                    .semantics { contentDescription = templatesDescription }
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.template_built_in_section),
                    color = colors.text,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = projectTemplates.size.toString(),
                    color = LocalDirectorXColors.current.accent,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            projectTemplates.chunked(2).forEach { rowTemplates ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    rowTemplates.forEach { template ->
                        ProjectTemplateShortcut(
                            template = template,
                            enabled = actionsEnabled,
                            onClick = onCreateProject,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    repeat(2 - rowTemplates.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    } else {
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "$title. $body" }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = title,
                    color = colors.text,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = body,
                    color = colors.subtext,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                ProjectActionRow(
                    primaryLabel = stringResource(R.string.projects_show_all),
                    primaryIcon = Icons.Default.Clear,
                    onPrimary = onShowAllProjects,
                    secondaryLabel = stringResource(R.string.projects_new_project),
                    secondaryIcon = Icons.Default.Add,
                    onSecondary = onCreateProject,
                    enabled = actionsEnabled,
                    secondaryTestTag = DirectorXTestTags.PROJECTS_CREATE_PROJECT
                )
            }
        }
    }
}

@Composable
private fun projectEmptyStateTitle(
    isConstrainedEmpty: Boolean,
    hasActiveSearch: Boolean,
    hasActiveFilter: Boolean,
    filterLabel: String
): String = when {
    !isConstrainedEmpty -> stringResource(R.string.projects_ready_title)
    hasActiveSearch && hasActiveFilter -> stringResource(R.string.projects_no_matching)
    hasActiveFilter -> stringResource(R.string.projects_no_filter_results, filterLabel)
    else -> stringResource(R.string.projects_no_matching)
}

@Composable
private fun projectEmptyStateBody(
    isConstrainedEmpty: Boolean,
    hasActiveSearch: Boolean,
    hasActiveFilter: Boolean
): String = when {
    !isConstrainedEmpty -> stringResource(R.string.projects_ready_body)
    hasActiveSearch && hasActiveFilter -> stringResource(R.string.projects_try_different_view)
    hasActiveFilter -> stringResource(R.string.projects_filter_empty_body)
    else -> stringResource(R.string.projects_try_different_search)
}

@Composable
private fun ProjectTemplateShortcut(
    template: ProjectTemplateUI,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = stringResource(template.nameResId)
    Column(
        modifier = modifier
            .height(88.dp)
            .background(
                color = template.appearanceAccentColor().copy(alpha = 0.09f),
                shape = RoundedCornerShape(Radius.md)
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = name }
            .padding(Spacing.md),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = template.icon,
            contentDescription = null,
            tint = template.appearanceAccentColor(),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = name,
            color = LocalDirectorXColors.current.text,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProjectTemplateLibraryRow(
    templateCount: Int,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalDirectorXColors.current
    val description = stringResource(R.string.projects_templates_count, templateCount)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .testTag(DirectorXTestTags.PROJECTS_TEMPLATES),
        color = Color.Transparent,
        shape = RoundedCornerShape(0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = LocalDirectorXColors.current.accent.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(Radius.md),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesomeMosaic,
                    contentDescription = null,
                    tint = LocalDirectorXColors.current.accent,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.template_built_in_section),
                    color = colors.text,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = description,
                    color = colors.subtext,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.subtextStrong,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ProjectActionRow(
    primaryLabel: String,
    primaryIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    secondaryIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onSecondary: () -> Unit,
    enabled: Boolean = true,
    primaryTestTag: String? = null,
    secondaryTestTag: String? = null
) {
    // Equal weighted actions keep the dashboard balanced; both share the same
    // height and width so neither action looks secondary by accident.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DirectorXPrimaryButton(
            text = primaryLabel,
            icon = primaryIcon,
            onClick = onPrimary,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .then(primaryTestTag?.let { Modifier.testTag(it) } ?: Modifier)
        )
        DirectorXPrimaryButton(
            text = secondaryLabel,
            icon = secondaryIcon,
            onClick = onSecondary,
            modifier = Modifier
                .weight(1f)
                .then(secondaryTestTag?.let { Modifier.testTag(it) } ?: Modifier),
            enabled = enabled
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    val projectDuration = formatDuration(project.durationMs)
    val updatedLabel = formatDate(project.updatedAt)
    val projectCardDescription = stringResource(
        R.string.projects_card_cd,
        project.name,
        projectDuration,
        updatedLabel
    )

    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    LocalDirectorXColors.current.danger.copy(alpha = 0.24f)
                else LocalDirectorXColors.current.panel.copy(alpha = 0.45f),
                label = "swipeBg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(Radius.xl))
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.projects_delete),
                        color = LocalDirectorXColors.current.danger,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.projects_delete_cd),
                        tint = LocalDirectorXColors.current.danger
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 112.dp)
                .testTag("${DirectorXTestTags.PROJECT_CARD_PREFIX}${project.id}")
                .clickable(role = Role.Button, onClick = onClick)
                .semantics {
                    contentDescription = projectCardDescription
                },
            colors = CardDefaults.cardColors(containerColor = LocalDirectorXColors.current.background),
            shape = RoundedCornerShape(0.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .padding(vertical = 10.dp)
            ) {
                val compactCard = maxWidth < 390.dp
                val thumbnailWidth = if (compactCard) 112.dp else 156.dp
                val thumbnailHeight = if (compactCard) 76.dp else 92.dp
                val thumbnailGap = if (compactCard) Spacing.sm else 14.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProjectThumbnail(
                        project = project,
                        width = thumbnailWidth,
                        height = thumbnailHeight
                    )

                    Spacer(modifier = Modifier.width(thumbnailGap))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(if (compactCard) 6.dp else 8.dp)
                    ) {
                        Text(
                            project.name,
                            color = LocalDirectorXColors.current.text,
                            style = if (compactCard) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                projectDuration,
                                color = LocalDirectorXColors.current.accentTertiary,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                project.resolution.label,
                                color = LocalDirectorXColors.current.subtext,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                project.timelineTimebase.frameRateLabel,
                                color = LocalDirectorXColors.current.subtext,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Text(
                            text = stringResource(R.string.projects_updated, updatedLabel),
                            color = LocalDirectorXColors.current.subtext,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Box {
                        DirectorXChromeIconButton(
                            icon = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.projects_more_cd),
                            onClick = { showOverflowMenu = true },
                            shape = RoundedCornerShape(Radius.lg)
                        )
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false },
                            containerColor = LocalDirectorXColors.current.panelHighest
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.projects_rename), color = LocalDirectorXColors.current.text) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.projects_rename),
                                        tint = LocalDirectorXColors.current.subtext,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    showRenameDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.projects_duplicate), color = LocalDirectorXColors.current.text) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = stringResource(R.string.cd_duplicate_project),
                                        tint = LocalDirectorXColors.current.subtext,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    onDuplicate()
                                    showOverflowMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.projects_delete), color = LocalDirectorXColors.current.danger) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.cd_delete_project),
                                        tint = LocalDirectorXColors.current.danger,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                    if (!compactCard) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = LocalDirectorXColors.current.overlayStrong,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        var projectName by remember(project.name) { mutableStateOf(project.name) }
        val trimmedProjectName = projectName.trim()
        val canSubmitRename = trimmedProjectName.isNotBlank() && trimmedProjectName != project.name
        val renameSupportingText = if (trimmedProjectName.isBlank()) {
            stringResource(R.string.projects_rename_required)
        } else {
            stringResource(R.string.projects_rename_helper)
        }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            icon = {
                DirectorXDialogIcon(
                    icon = Icons.Default.Edit,
                    accent = LocalDirectorXColors.current.accentTertiary
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.projects_rename_title),
                    color = LocalDirectorXColors.current.text,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it.take(PROJECT_RENAME_MAX_CHARS) },
                    singleLine = true,
                    isError = trimmedProjectName.isBlank(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (canSubmitRename) {
                                onRename(trimmedProjectName)
                                showRenameDialog = false
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.lg),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.projects_rename_hint),
                            color = LocalDirectorXColors.current.overlayStrong
                        )
                    },
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = renameSupportingText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${projectName.length}/$PROJECT_RENAME_MAX_CHARS",
                                maxLines = 1
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LocalDirectorXColors.current.text,
                        unfocusedTextColor = LocalDirectorXColors.current.text,
                        errorTextColor = LocalDirectorXColors.current.text,
                        cursorColor = LocalDirectorXColors.current.accentTertiary,
                        focusedBorderColor = LocalDirectorXColors.current.accentTertiary,
                        unfocusedBorderColor = LocalDirectorXColors.current.cardStroke,
                        errorBorderColor = LocalDirectorXColors.current.danger,
                        focusedContainerColor = LocalDirectorXColors.current.panelRaised,
                        unfocusedContainerColor = LocalDirectorXColors.current.panelRaised
                    )
                )
            },
            confirmButton = {
                DirectorXPrimaryButton(
                    text = stringResource(R.string.done),
                    onClick = {
                        onRename(trimmedProjectName)
                        showRenameDialog = false
                    },
                    enabled = canSubmitRename,
                    icon = Icons.Default.Check
                )
            },
            dismissButton = {
                DirectorXSecondaryButton(
                    text = stringResource(R.string.cancel),
                    onClick = { showRenameDialog = false }
                )
            },
            containerColor = LocalDirectorXColors.current.panelHighest,
            titleContentColor = LocalDirectorXColors.current.text,
            textContentColor = LocalDirectorXColors.current.subtext,
            shape = RoundedCornerShape(Radius.xl)
        )
    }
}

@Composable
private fun ProjectThumbnail(
    project: Project,
    width: androidx.compose.ui.unit.Dp = 156.dp,
    height: androidx.compose.ui.unit.Dp = 92.dp
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .size(width, height)
            .clip(RoundedCornerShape(Radius.lg))
            .background(LocalDirectorXColors.current.panelHighest)
    ) {
        if (project.thumbnailUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(android.net.Uri.parse(project.thumbnailUri))
                    .decoderFactory(VideoFrameDecoder.Factory())
                    .build(),
                contentDescription = stringResource(R.string.projects_thumbnail_cd),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = stringResource(R.string.cd_movie_placeholder),
                tint = LocalDirectorXColors.current.accentTertiary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(28.dp)
            )
        }

        Surface(
            color = LocalDirectorXColors.current.background.copy(alpha = 0.78f),
            shape = RoundedCornerShape(Radius.sm),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(
                text = project.aspectRatio.label,
                color = LocalDirectorXColors.current.text,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(
                    horizontal = 8.dp,
                    vertical = 4.dp
                )
            )
        }
    }
}

@Composable
private fun TrashSectionHeader(
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    onEmptyTrash: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onToggle),
        color = LocalDirectorXColors.current.panel,
        shape = RoundedCornerShape(Radius.lg),
        border = androidx.compose.foundation.BorderStroke(1.dp, LocalDirectorXColors.current.cardStroke.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Surface(
                color = LocalDirectorXColors.current.danger.copy(alpha = 0.12f),
                shape = RoundedCornerShape(Radius.lg),
                border = androidx.compose.foundation.BorderStroke(1.dp, LocalDirectorXColors.current.danger.copy(alpha = 0.22f))
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = LocalDirectorXColors.current.danger,
                    modifier = Modifier
                        .padding(Spacing.sm)
                        .size(18.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.trash_title),
                    color = LocalDirectorXColors.current.text,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = pluralStringResource(R.plurals.trash_kept_summary, count, count),
                    color = LocalDirectorXColors.current.subtext,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (expanded) {
                TextButton(
                    onClick = onEmptyTrash,
                    shape = RoundedCornerShape(Radius.md),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = LocalDirectorXColors.current.danger,
                        containerColor = LocalDirectorXColors.current.danger.copy(alpha = 0.08f)
                    )
                ) {
                    Text(stringResource(R.string.trash_empty_button), style = MaterialTheme.typography.labelMedium)
                }
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) {
                    stringResource(R.string.trash_collapse_cd)
                } else {
                    stringResource(R.string.trash_expand_cd)
                },
                tint = LocalDirectorXColors.current.subtext,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

@Composable
private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> stringResource(R.string.projects_just_now)
        diff < 3_600_000 -> stringResource(R.string.projects_minutes_ago, diff / 60_000)
        diff < 86_400_000 -> stringResource(R.string.projects_hours_ago, diff / 3_600_000)
        diff < 604_800_000 -> stringResource(R.string.projects_days_ago, diff / 86_400_000)
        else -> {
            val locale = LocalConfiguration.current.locales[0]
            val sdf = java.text.SimpleDateFormat("MMM d", locale)
            sdf.format(java.util.Date(timestamp))
        }
    }
}

@Composable
private fun SortMode.localizedLabel(): String = when (this) {
    SortMode.DATE_DESC -> stringResource(R.string.project_sort_recent)
    SortMode.DATE_ASC -> stringResource(R.string.project_sort_oldest)
    SortMode.NAME_ASC -> stringResource(R.string.project_sort_name_asc)
    SortMode.NAME_DESC -> stringResource(R.string.project_sort_name_desc)
    SortMode.DURATION_DESC -> stringResource(R.string.project_sort_longest)
}

@Composable
private fun ProjectFilterMode.localizedLabel(): String = when (this) {
    ProjectFilterMode.ALL -> stringResource(R.string.project_filter_all)
    ProjectFilterMode.RECENT_7D -> stringResource(R.string.project_filter_this_week)
    ProjectFilterMode.LONG -> stringResource(R.string.project_filter_long)
    ProjectFilterMode.SHORT -> stringResource(R.string.project_filter_short)
    ProjectFilterMode.EMPTY -> stringResource(R.string.project_filter_empty)
}

@Composable
private fun TrashedProjectCard(
    project: Project,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = LocalDirectorXColors.current.panel.copy(alpha = 0.72f),
        shape = RoundedCornerShape(Radius.lg),
        border = androidx.compose.foundation.BorderStroke(1.dp, LocalDirectorXColors.current.cardStroke.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Surface(
                color = LocalDirectorXColors.current.danger.copy(alpha = 0.10f),
                shape = RoundedCornerShape(Radius.md),
                border = androidx.compose.foundation.BorderStroke(1.dp, LocalDirectorXColors.current.danger.copy(alpha = 0.18f))
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = LocalDirectorXColors.current.danger,
                    modifier = Modifier
                        .padding(Spacing.sm)
                        .size(18.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = project.name,
                    color = LocalDirectorXColors.current.text,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                project.deletedAtEpochMs?.let { deletedAt ->
                    val daysAgo = ((System.currentTimeMillis() - deletedAt) / 86_400_000).toInt()
                    val daysLeft = (30 - daysAgo).coerceAtLeast(0)
                    Text(
                        text = pluralStringResource(R.plurals.trash_auto_delete_in, daysLeft, daysLeft),
                        color = LocalDirectorXColors.current.overlayStrong,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                DirectorXChromeIconButton(
                    icon = Icons.Default.RestoreFromTrash,
                    contentDescription = stringResource(R.string.trash_restore_cd),
                    onClick = onRestore,
                    tint = LocalDirectorXColors.current.success,
                    containerColor = LocalDirectorXColors.current.success.copy(alpha = 0.08f),
                    borderColor = LocalDirectorXColors.current.success.copy(alpha = 0.18f)
                )
                DirectorXChromeIconButton(
                    icon = Icons.Default.DeleteForever,
                    contentDescription = stringResource(R.string.trash_delete_forever_cd),
                    onClick = onDeleteForever,
                    tint = LocalDirectorXColors.current.danger,
                    containerColor = LocalDirectorXColors.current.danger.copy(alpha = 0.08f),
                    borderColor = LocalDirectorXColors.current.danger.copy(alpha = 0.18f)
                )
            }
        }
    }
}
