package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.DocumentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DocumentRepository(application)

    val allDocuments: StateFlow<List<DocumentItem>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteDocuments: StateFlow<List<DocumentItem>> = repository.favoriteDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentDocuments: StateFlow<List<DocumentItem>> = repository.recentDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation & View State
    private val _currentTab = MutableStateFlow(0) // 0: Home, 1: Documents, 2: Favorites, 3: Settings
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _activeDocument = MutableStateFlow<DocumentItem?>(null)
    val activeDocument: StateFlow<DocumentItem?> = _activeDocument.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _selectedFilter = MutableStateFlow(FilterType.ALL)
    val selectedFilter: StateFlow<FilterType> = _selectedFilter.asStateFlow()

    private val _selectedSort = MutableStateFlow(SortType.DATE_MODIFIED)
    val selectedSort: StateFlow<SortType> = _selectedSort.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.LIST)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    // Multi-Select Mode
    private val _selectedDocIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedDocIds: StateFlow<Set<String>> = _selectedDocIds.asStateFlow()

    val isSelectionMode: StateFlow<Boolean> = _selectedDocIds.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Viewer Settings
    private val _continuousScroll = MutableStateFlow(true)
    val continuousScroll: StateFlow<Boolean> = _continuousScroll.asStateFlow()

    private val _darkReadingMode = MutableStateFlow(false)
    val darkReadingMode: StateFlow<Boolean> = _darkReadingMode.asStateFlow()

    private val _readingBackground = MutableStateFlow(ReadingBackground.DEFAULT)
    val readingBackground: StateFlow<ReadingBackground> = _readingBackground.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val _pageRotation = MutableStateFlow(0) // 0, 90, 180, 270
    val pageRotation: StateFlow<Int> = _pageRotation.asStateFlow()

    private val _isFullScreen = MutableStateFlow(false)
    val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()

    // Details sheet & Rename dialog state
    private val _detailDocument = MutableStateFlow<DocumentItem?>(null)
    val detailDocument: StateFlow<DocumentItem?> = _detailDocument.asStateFlow()

    private val _renameDocument = MutableStateFlow<DocumentItem?>(null)
    val renameDocument: StateFlow<DocumentItem?> = _renameDocument.asStateFlow()

    // Onboarding State
    private val _isOnboardingCompleted = MutableStateFlow(true)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    // App Theme (0: System, 1: Light, 2: Dark)
    private val _themeMode = MutableStateFlow(0)
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeSamplesIfNeeded()
        }
    }

    fun setTab(index: Int) {
        _currentTab.value = index
        _isSearchActive.value = false
    }

    fun openDocument(doc: DocumentItem) {
        _activeDocument.value = doc
        _currentPage.value = doc.lastViewedPage.coerceAtLeast(1)
        _totalPages.value = doc.pageCount.coerceAtLeast(1)
        _pageRotation.value = 0
        viewModelScope.launch {
            repository.recordDocumentOpened(doc.id, _currentPage.value)
        }
    }

    fun closeDocument() {
        val currentDoc = _activeDocument.value
        if (currentDoc != null) {
            viewModelScope.launch {
                repository.recordDocumentOpened(currentDoc.id, _currentPage.value)
            }
        }
        _activeDocument.value = null
        _isFullScreen.value = false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun setFilter(filter: FilterType) {
        _selectedFilter.value = filter
    }

    fun setSort(sort: SortType) {
        if (_selectedSort.value == sort) {
            _sortOrder.value = if (_sortOrder.value == SortOrder.ASC) SortOrder.DESC else SortOrder.ASC
        } else {
            _selectedSort.value = sort
            _sortOrder.value = SortOrder.DESC
        }
    }

    fun toggleViewMode() {
        _viewMode.value = if (_viewMode.value == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
    }

    fun toggleFavorite(doc: DocumentItem) {
        viewModelScope.launch {
            val updated = !doc.isFavorite
            repository.toggleFavorite(doc.id, updated)
            if (_activeDocument.value?.id == doc.id) {
                _activeDocument.value = _activeDocument.value?.copy(isFavorite = updated)
            }
        }
    }

    fun toggleBookmark(page: Int) {
        val doc = _activeDocument.value ?: return
        val currentBookmarks = doc.bookmarks.toMutableList()
        if (currentBookmarks.contains(page)) {
            currentBookmarks.remove(page)
        } else {
            currentBookmarks.add(page)
            currentBookmarks.sort()
        }
        viewModelScope.launch {
            repository.updateBookmarks(doc.id, currentBookmarks)
            _activeDocument.value = doc.copy(bookmarks = currentBookmarks)
        }
    }

    fun updateCurrentPage(page: Int) {
        _currentPage.value = page
        val doc = _activeDocument.value
        if (doc != null) {
            viewModelScope.launch {
                repository.recordDocumentOpened(doc.id, page)
            }
        }
    }

    fun setTotalPages(total: Int) {
        _totalPages.value = total.coerceAtLeast(1)
    }

    fun rotatePages() {
        _pageRotation.value = (_pageRotation.value + 90) % 360
    }

    fun toggleContinuousScroll() {
        _continuousScroll.value = !_continuousScroll.value
    }

    fun toggleDarkReadingMode() {
        _darkReadingMode.value = !_darkReadingMode.value
    }

    fun setReadingBackground(bg: ReadingBackground) {
        _readingBackground.value = bg
    }

    fun toggleFullScreen() {
        _isFullScreen.value = !_isFullScreen.value
    }

    fun showDocumentDetails(doc: DocumentItem?) {
        _detailDocument.value = doc
    }

    fun showRenameDialog(doc: DocumentItem?) {
        _renameDocument.value = doc
    }

    fun performRename(id: String, newName: String) {
        viewModelScope.launch {
            repository.renameDocument(id, newName)
            _renameDocument.value = null
        }
    }

    fun deleteDocument(doc: DocumentItem) {
        viewModelScope.launch {
            repository.deleteDocument(doc.id)
            if (_activeDocument.value?.id == doc.id) {
                closeDocument()
            }
            if (_detailDocument.value?.id == doc.id) {
                _detailDocument.value = null
            }
        }
    }

    fun toggleSelectDoc(id: String) {
        val current = _selectedDocIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedDocIds.value = current
    }

    fun clearSelection() {
        _selectedDocIds.value = emptySet()
    }

    fun deleteSelectedDocs() {
        val ids = _selectedDocIds.value.toList()
        viewModelScope.launch {
            repository.deleteMultiple(ids)
            clearSelection()
        }
    }

    fun importDocument(uri: Uri) {
        viewModelScope.launch {
            val imported = repository.importFromUri(uri)
            if (imported != null) {
                openDocument(imported)
            }
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        _isOnboardingCompleted.value = completed
    }

    fun setThemeMode(mode: Int) {
        _themeMode.value = mode
    }

    // Filtered and Sorted Documents Flow
    val filteredDocuments: StateFlow<List<DocumentItem>> = combine(
        allDocuments,
        _searchQuery,
        _selectedFilter,
        _selectedSort,
        _sortOrder
    ) { docs, query, filter, sort, order ->
        var list = docs

        // 1. Filter
        list = when (filter) {
            FilterType.ALL -> list
            FilterType.PDF -> list.filter { it.format == DocFormat.PDF }
            FilterType.WORD -> list.filter { it.format == DocFormat.WORD }
            FilterType.EXCEL -> list.filter { it.format == DocFormat.EXCEL }
            FilterType.POWERPOINT -> list.filter { it.format == DocFormat.POWERPOINT }
            FilterType.IMAGES -> list.filter { it.format == DocFormat.IMAGE }
            FilterType.TEXT -> list.filter { it.format == DocFormat.TEXT }
            FilterType.FAVORITES -> list.filter { it.isFavorite }
            FilterType.RECENT -> list.filter { it.lastOpened > 0 }.sortedByDescending { it.lastOpened }
        }

        // 2. Search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.extension.lowercase().contains(q) ||
                it.folderName.lowercase().contains(q) ||
                it.format.displayName.lowercase().contains(q)
            }
        }

        // 3. Sort
        val comparator: Comparator<DocumentItem> = when (sort) {
            SortType.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortType.DATE_MODIFIED -> compareBy { it.lastModified }
            SortType.DATE_OPENED -> compareBy { it.lastOpened }
            SortType.SIZE -> compareBy { it.sizeBytes }
            SortType.TYPE -> compareBy { it.format.name }
        }

        if (order == SortOrder.ASC) {
            list.sortedWith(comparator)
        } else {
            list.sortedWith(comparator.reversed())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
