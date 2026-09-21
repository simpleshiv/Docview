package com.example.data.model

enum class FilterType(val label: String, val iconEmoji: String) {
    ALL("All", "📁"),
    PDF("PDF", "📄"),
    WORD("Word", "📝"),
    EXCEL("Excel", "📊"),
    POWERPOINT("PowerPoint", "📽️"),
    IMAGES("Images", "🖼️"),
    TEXT("Text", "📋"),
    FAVORITES("Favorites", "⭐"),
    RECENT("Recent", "🕒")
}

enum class SortType(val label: String) {
    NAME("Name"),
    DATE_MODIFIED("Date modified"),
    DATE_OPENED("Date opened"),
    SIZE("File size"),
    TYPE("File type")
}

enum class SortOrder(val label: String) {
    ASC("Ascending"),
    DESC("Descending")
}

enum class ViewMode {
    LIST,
    GRID
}

enum class ReadingBackground(val label: String) {
    DEFAULT("Default"),
    SEPIA("Warm Sepia"),
    PAPER("Soft Paper"),
    DARK("True Dark"),
    INVERT("High Contrast")
}
