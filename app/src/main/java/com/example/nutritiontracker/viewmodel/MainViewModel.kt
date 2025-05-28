
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritiontracker.data.database.entities.*
import com.example.nutritiontracker.data.models.EntryType
import com.example.nutritiontracker.data.models.IngredientWithAmount
import com.example.nutritiontracker.data.models.MealType
import com.example.nutritiontracker.data.models.WeeklyStats
import com.example.nutritiontracker.data.repository.NutritionRepository
import com.example.nutritiontracker.utils.NutritionCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: NutritionRepository
) : ViewModel() {

    // Exposed flows for UI
    val ingredients = repository.getAllIngredients()
    val recipes = repository.getAllRecipes()
    val shoppingListItems = repository.getAllShoppingItems()

    // Ingredient operations
    fun addIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            repository.insertIngredient(ingredient)
        }
    }

    fun updateIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            repository.updateIngredient(ingredient)
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            repository.deleteIngredient(ingredient)
        }
    }

    suspend fun getIngredientById(id: Long): Ingredient? {
        return repository.getIngredientById(id)
    }

    fun searchIngredients(query: String): Flow<List<Ingredient>> {
        return repository.searchIngredients(query)
    }

    // Recipe operations
    fun addRecipe(recipe: Recipe, ingredients: List<Pair<Long, Double>>) {
        viewModelScope.launch {
            repository.addOrUpdateRecipe(recipe, ingredients)
        }
    }

    fun addOrUpdateRecipe(recipe: Recipe, ingredients: List<Pair<Long, Double>>) {
        viewModelScope.launch {
            repository.addOrUpdateRecipe(recipe, ingredients)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
        }
    }

    fun getIngredientsForRecipe(recipeId: Long): Flow<List<IngredientWithAmount>> {
        return repository.getIngredientsForRecipe(recipeId)
    }

    fun searchRecipes(query: String): Flow<List<Recipe>> {
        return repository.searchRecipes(query)
    }

    // Diary operations
    fun getDiaryEntriesForDate(date: Long): Flow<List<DiaryEntry>> {
        return repository.getEntriesForDate(date)
    }

    fun addDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.insertDiaryEntry(entry)
        }
    }

    fun updateDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.updateDiaryEntry(entry)
        }
    }

    fun deleteDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.deleteDiaryEntry(entry)
        }
    }

    // Shopping list operations
    fun addShoppingListItem(item: ShoppingListItem) {
        viewModelScope.launch {
            repository.insertShoppingItem(item)
        }
    }

    fun updateShoppingListItemChecked(id: Long, checked: Boolean) {
        viewModelScope.launch {
            repository.updateShoppingItemChecked(id, checked)
        }
    }

    fun deleteShoppingListItem(item: ShoppingListItem) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
        }
    }

    fun deleteCheckedShoppingListItems() {
        viewModelScope.launch {
            repository.deleteCheckedShoppingItems()
        }
    }

    fun addRecipeToShoppingList(recipeId: Long) {
        viewModelScope.launch {
            repository.addRecipeToShoppingList(recipeId)
        }
    }

    // Complex operations
    fun addIngredientAndCreateDiaryEntry(
        ingredient: Ingredient,
        mealType: MealType,
        date: Long,
        amount: Double
    ) {
        viewModelScope.launch {
            repository.addIngredientAndCreateDiaryEntry(ingredient, mealType, date, amount)
        }
    }

    // Statistics and calculations
    fun getWeeklyStats(endDate: Long): Flow<WeeklyStats> {
        return repository.getWeeklyStats(endDate)
    }

    suspend fun calculateDailyNutrition(entries: List<DiaryEntry>): NutritionCalculator.NutritionValues {
        return repository.calculateDailyNutrition(entries)
    }

    suspend fun calculateMealCalories(entries: List<DiaryEntry>): Double {
        val nutrition = repository.calculateDailyNutrition(entries)
        return nutrition.calories
    }

    suspend fun getEntryDisplayName(entry: DiaryEntry): String {
        return repository.getEntryDisplayName(entry)
    }

    // Export/Import operations
    suspend fun exportNutritionData(context: Context): Uri? {
        return repository.exportNutritionData(context)
    }

    suspend fun exportDiaryData(context: Context): Uri? {
        return repository.exportDiaryData(context)
    }

    suspend fun importNutritionData(context: Context, uri: Uri): Boolean {
        return repository.importNutritionData(context, uri)
    }

    suspend fun importDiaryData(context: Context, uri: Uri): Boolean {
        return repository.importDiaryData(context, uri)
    }

    suspend fun getRecipeById(id: Long): Recipe? {
        return repository.getRecipeById(id)
    }

    // Außerdem füge updateShoppingListItem hinzu:
    fun updateShoppingListItem(item: ShoppingListItem) {
        viewModelScope.launch {
            repository.updateShoppingItem(item)
        }
    }
}