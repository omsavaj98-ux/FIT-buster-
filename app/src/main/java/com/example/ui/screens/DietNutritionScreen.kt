package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.FitBusterCatalog
import com.example.model.MealEntry
import com.example.model.MealType
import com.example.ui.FitBusterDashboardData
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FitBusterCard
import com.example.ui.components.NeonProgressBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.FlameAmber
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SuccessGreen
import com.example.util.FitBusterDateUtil

@Composable
fun DietNutritionScreen(
    data: FitBusterDashboardData,
    onLogMeal: (String, MealType, Long, Long, Long, Long, Boolean) -> Unit,
    onDeleteMeal: (String) -> Unit,
    onUpdateGoals: (Long, Long, Long, Boolean) -> Unit,
    onOpenWaterTracker: () -> Unit,
    onOpenDailyReport: () -> Unit
) {
    val today = remember { FitBusterDateUtil.todayKey() }
    val todayMeals = remember(data.meals, today) { data.meals.filter { it.dateKey == today } }
    val caloriesConsumed = remember(todayMeals) { todayMeals.sumOf { it.calories } }
    val proteinToday = remember(todayMeals) { todayMeals.sumOf { it.proteinG } }
    val carbsToday = remember(todayMeals) { todayMeals.sumOf { it.carbsG } }
    val fatsToday = remember(todayMeals) { todayMeals.sumOf { it.fatsG } }
    val caloriesBurnedToday = remember(data.workouts, today) {
        data.workouts.filter { it.dateKey == today }.sumOf { it.caloriesBurned }
    }

    var showManualDialog by remember { mutableStateOf(false) }
    var showGoalsDialog by remember { mutableStateOf(false) }
    var selectedMealFilter by remember { mutableStateOf<MealType?>(null) }

    val filteredTodayMeals = remember(todayMeals, selectedMealFilter) {
        if (selectedMealFilter == null) todayMeals
        else todayMeals.filter { it.mealType == selectedMealFilter!!.name }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Navigation Pills: Water Tracker & Daily Report
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = false,
                    onClick = onOpenWaterTracker,
                    label = { Text("Water Tracker") },
                    leadingIcon = { Icon(Icons.Filled.WaterDrop, null, Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        iconColor = HydrationCyan
                    ),
                    modifier = Modifier.testTag("fuel_water_chip")
                )
                FilterChip(
                    selected = false,
                    onClick = onOpenDailyReport,
                    label = { Text("Daily & Monthly Report") },
                    leadingIcon = { Icon(Icons.Filled.Assessment, null, Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        iconColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("fuel_reports_chip")
                )
            }
        }

        // Daily Nutrition Summary & Calorie Balance
        item {
            FitBusterCard(highlighted = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("DAILY NUTRITION SUMMARY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "$caloriesConsumed / ${data.profile.dailyCalorieGoal} kcal",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                    IconButton(onClick = { showGoalsDialog = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Edit Nutrition Goals", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                NeonProgressBar(
                    progress = caloriesConsumed.toFloat() / data.profile.dailyCalorieGoal.coerceAtLeast(1L).toFloat()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("CONSUMED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$caloriesConsumed kcal", style = MaterialTheme.typography.titleMedium, color = SuccessGreen)
                    }
                    Column {
                        Text("BURNED EST.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$caloriesBurnedToday kcal", style = MaterialTheme.typography.titleMedium, color = FlameAmber)
                    }
                    Column {
                        Text("NET CALORIES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${caloriesConsumed - caloriesBurnedToday} kcal", style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Protein: ${proteinToday}g / ${data.profile.dailyProteinGoalG}g", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Carbs: ${carbsToday}g", style = MaterialTheme.typography.labelMedium, color = HydrationCyan)
                    Text("Fats: ${fatsToday}g", style = MaterialTheme.typography.labelMedium, color = FlameAmber)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showManualDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = ObsidianBlack
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("add_food_manually_btn")
                ) {
                    Icon(Icons.Filled.Add, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Food Manually", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Vegetarian Food Options Quick-Add
        item {
            SectionHeader(
                title = "Vegetarian Food Options",
                subtitle = "Tap any balanced vegetarian meal to log instantly"
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(FitBusterCatalog.vegetarianFoods) { preset ->
                    FitBusterCard(
                        modifier = Modifier.width(220.dp),
                        onClick = {
                            onLogMeal(
                                preset.name,
                                preset.mealType,
                                preset.calories.toLong(),
                                preset.proteinG.toLong(),
                                preset.carbsG.toLong(),
                                preset.fatsG.toLong(),
                                true
                            )
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Eco, contentDescription = "Vegetarian", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                            Text(preset.mealType.displayName, style = MaterialTheme.typography.labelSmall, color = SuccessGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(preset.name, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${preset.calories} kcal • ${preset.proteinG}g P",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Meal Tracking Filter & Today's Diet Report
        item {
            SectionHeader(
                title = "Today's Meals & Diet Report",
                subtitle = "${todayMeals.size} entries today"
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedMealFilter == null,
                        onClick = { selectedMealFilter = null },
                        label = { Text("All") }
                    )
                }
                items(MealType.entries) { mt ->
                    FilterChip(
                        selected = selectedMealFilter == mt,
                        onClick = { selectedMealFilter = mt },
                        label = { Text(mt.displayName) }
                    )
                }
            }
        }

        if (filteredTodayMeals.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.Restaurant,
                    title = "No meals logged today",
                    description = "Tap Add Food Manually or pick a Vegetarian Food Option above."
                )
            }
        } else {
            items(filteredTodayMeals, key = { it.id.ifBlank { it.hashCode().toString() } }) { meal ->
                MealLogRow(meal = meal, onDelete = { if (meal.id.isNotBlank()) onDeleteMeal(meal.id) })
            }
        }

        // Previous Food History
        val olderMeals = data.meals.filter { it.dateKey != today }
        if (olderMeals.isNotEmpty()) {
            item {
                SectionHeader(title = "Food History", subtitle = "Previous days")
            }
            items(olderMeals.take(15), key = { it.id.ifBlank { it.hashCode().toString() } }) { meal ->
                MealLogRow(meal = meal, onDelete = null)
            }
        }
    }

    if (showManualDialog) {
        AddMealDialog(
            defaultVeg = data.profile.vegetarianDefault,
            onDismiss = { showManualDialog = false },
            onSave = { name, type, cal, prot, carbs, fats, veg ->
                showManualDialog = false
                onLogMeal(name, type, cal, prot, carbs, fats, veg)
            }
        )
    }

    if (showGoalsDialog) {
        EditNutritionGoalsDialog(
            currentCal = data.profile.dailyCalorieGoal,
            currentWater = data.profile.dailyWaterGoalMl,
            currentProtein = data.profile.dailyProteinGoalG,
            currentVeg = data.profile.vegetarianDefault,
            onDismiss = { showGoalsDialog = false },
            onSave = { cal, water, prot, veg ->
                showGoalsDialog = false
                onUpdateGoals(cal, water, prot, veg)
            }
        )
    }
}

@Composable
private fun MealLogRow(meal: MealEntry, onDelete: (() -> Unit)?) {
    FitBusterCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (meal.vegetarian) {
                        Icon(Icons.Filled.Eco, contentDescription = "Veg", tint = SuccessGreen, modifier = Modifier.size(15.dp))
                    }
                    Text(meal.foodName, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = "${MealType.fromRaw(meal.mealType).displayName} • ${FitBusterDateUtil.formatDisplayDate(meal.dateKey)} • ${meal.proteinG}g P / ${meal.carbsG}g C / ${meal.fatsG}g F",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        text = "${meal.calories} kcal",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete meal", tint = AlertCrimson)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddMealDialog(
    defaultVeg: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, MealType, Long, Long, Long, Long, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf(MealType.LUNCH) }
    var vegetarian by remember { mutableStateOf(defaultVeg) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Food Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("meal_name_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MealType.entries.forEach { mt ->
                        FilterChip(
                            selected = mealType == mt,
                            onClick = { mealType = mt },
                            label = { Text(mt.displayName.take(5)) }
                        )
                    }
                }
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("meal_calories_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein g") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs g") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fats,
                        onValueChange = { fats = it },
                        label = { Text("Fats g") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vegetarian Option")
                    Switch(checked = vegetarian, onCheckedChange = { vegetarian = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        mealType,
                        calories.toLongOrNull() ?: 0L,
                        protein.toLongOrNull() ?: 0L,
                        carbs.toLongOrNull() ?: 0L,
                        fats.toLongOrNull() ?: 0L,
                        vegetarian
                    )
                },
                modifier = Modifier.testTag("save_meal_dialog_btn")
            ) {
                Text("Save Meal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EditNutritionGoalsDialog(
    currentCal: Long,
    currentWater: Long,
    currentProtein: Long,
    currentVeg: Boolean,
    onDismiss: () -> Unit,
    onSave: (Long, Long, Long, Boolean) -> Unit
) {
    var cal by remember { mutableStateOf(currentCal.toString()) }
    var water by remember { mutableStateOf(currentWater.toString()) }
    var prot by remember { mutableStateOf(currentProtein.toString()) }
    var veg by remember { mutableStateOf(currentVeg) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Personal Nutrition Goals") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = cal,
                    onValueChange = { cal = it },
                    label = { Text("Daily Calories Goal (800–8000)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = prot,
                    onValueChange = { prot = it },
                    label = { Text("Daily Protein Goal (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = water,
                    onValueChange = { water = it },
                    label = { Text("Daily Water Goal (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vegetarian Default")
                    Switch(checked = veg, onCheckedChange = { veg = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        cal.toLongOrNull() ?: currentCal,
                        water.toLongOrNull() ?: currentWater,
                        prot.toLongOrNull() ?: currentProtein,
                        veg
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
