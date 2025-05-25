package com.example.nutritiontracker.data.models


enum class Category(val displayName: String) {
    // Nährwert-Kategorien
    HIGH_PROTEIN("High Protein"),
    LOW_CARB("Low Carb"),
    LOW_FAT("Fettarm"),
    HIGH_FIBER("Ballaststoffreich"),
    LOW_CALORIE("Kalorienarm"),

    // Ernährungsform-Kategorien
    VEGAN("Vegan"),
    VEGETARIAN("Vegetarisch"),
    GLUTEN_FREE("Glutenfrei"),
    LACTOSE_FREE("Laktosefrei"),
    KETO("Keto"),
    PALEO("Paleo"),

    // Lebensmittel-Kategorien
    MEAT("Fleisch"),
    FISH("Fisch"),
    DAIRY("Milchprodukte"),
    CHEESE("Käse"),
    EGGS("Eier"),
    VEGETABLES("Gemüse"),
    FRUITS("Obst"),
    GRAINS("Getreide"),
    NUTS_SEEDS("Nüsse & Samen"),
    LEGUMES("Hülsenfrüchte"),
    SWEETS("Süßigkeiten"),
    BEVERAGES("Getränke"),

    // Mahlzeit-Kategorien
    BREAKFAST("Frühstück"),
    LUNCH("Mittagessen"),
    DINNER("Abendessen"),
    SNACK("Snack"),
    DESSERT("Dessert"),

    // Zubereitungs-Kategorien
    QUICK("Schnell"),
    EASY("Einfach"),
    MEAL_PREP("Meal Prep")
}