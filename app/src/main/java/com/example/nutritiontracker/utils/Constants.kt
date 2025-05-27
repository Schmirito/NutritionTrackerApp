package com.example.nutritiontracker.utils

object Constants {

    // Database
    object Database {
        const val NAME = "nutrition_database"
        const val VERSION = 6
    }

    // UI
    object UI {
        const val ITEMS_PER_PAGE = 20
        const val SEARCH_DEBOUNCE_DELAY = 300L
        const val ANIMATION_DURATION_SHORT = 150
        const val ANIMATION_DURATION_LONG = 300

        // Search placeholders
        const val INGREDIENT_SEARCH_PLACEHOLDER = "Zutat suchen..."
        const val RECIPE_SEARCH_PLACEHOLDER = "Rezept suchen..."
        const val SEARCH_NO_RESULTS = "Keine Ergebnisse für"

        // Dialog titles
        const val ADD_INGREDIENT_TITLE = "Zutat hinzufügen"
        const val EDIT_INGREDIENT_TITLE = "Zutat bearbeiten"
        const val ADD_RECIPE_TITLE = "Rezept hinzufügen"
        const val EDIT_RECIPE_TITLE = "Rezept bearbeiten"

        // Button texts
        const val SAVE_BUTTON = "Speichern"
        const val CANCEL_BUTTON = "Abbrechen"
        const val ADD_BUTTON = "Hinzufügen"
        const val DELETE_BUTTON = "Löschen"
        const val EDIT_BUTTON = "Bearbeiten"
    }

    // Images
    object Images {
        const val MAX_IMAGE_SIZE = 800
        const val IMAGE_QUALITY = 85
        const val IMAGE_DIRECTORY = "images"
        const val IMAGE_PREFIX_INGREDIENT = "ingredient_"
        const val IMAGE_PREFIX_RECIPE = "recipe_"
    }

    // Nutrition
    object Nutrition {
        const val DEFAULT_SERVINGS = 1
        const val CALORIES_PER_GRAM_PROTEIN = 4.0
        const val CALORIES_PER_GRAM_CARBS = 4.0
        const val CALORIES_PER_GRAM_FAT = 9.0
        const val MIN_CALORIES = 0.0
        const val MAX_CALORIES = 10000.0
        const val DEFAULT_AMOUNT_GRAMS = 100.0
        const val DEFAULT_AMOUNT_MILLILITERS = 100.0
        const val DEFAULT_AMOUNT_PIECES = 1.0
    }

    // Export/Import
    object Export {
        const val JSON_INDENT = 2
        const val EXPORT_VERSION = 1
        const val FILE_PROVIDER_AUTHORITY = ".fileprovider"
        const val EXPORT_DIRECTORY = "exports"
        const val BACKUP_DIRECTORY = "NutritionTracker_Backups"
        const val NUTRITION_EXPORT_PREFIX = "nutrition_export_"
        const val DIARY_EXPORT_PREFIX = "diary_export_"
        const val BACKUP_PREFIX = "nutrition_backup_"
    }

    // Manual Entry
    object ManualEntry {
        const val PREFIX = "[Manuell]"
        const val DEFAULT_AMOUNT = 100.0
        const val DESCRIPTION_TEMPLATE = "Manueller Eintrag vom"
    }

    // Shopping List
    object ShoppingList {
        const val DEFAULT_AMOUNT_GRAMS = 100
        const val DEFAULT_AMOUNT_PIECES = 1
    }

    // Date/Time
    object DateTime {
        const val DATE_FORMAT_DISPLAY = "EEEE, dd. MMMM yyyy"
        const val DATE_FORMAT_EXPORT = "yyyy-MM-dd_HH-mm-ss"
        const val DATE_FORMAT_SHORT = "dd.MM.yyyy"
    }

    // Error Messages
    object ErrorMessages {
        const val NAME_REQUIRED = "Name darf nicht leer sein"
        const val CALORIES_REQUIRED = "Kalorien müssen angegeben werden"
        const val AMOUNT_REQUIRED = "Menge darf nicht leer sein"
        const val INVALID_AMOUNT = "Ungültige Mengenangabe"
        const val IMPORT_FAILED = "Import fehlgeschlagen"
        const val EXPORT_FAILED = "Export fehlgeschlagen"
        const val BACKUP_FAILED = "Backup fehlgeschlagen"
    }
}