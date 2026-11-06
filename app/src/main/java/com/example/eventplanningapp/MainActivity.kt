
package com.example.eventplanningapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val ElegantColors = lightColorScheme(
    primary = Color(0xFF526B5A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE7DD),
    onPrimaryContainer = Color(0xFF203A2A),
    secondary = Color(0xFF8A7865),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFECE3D8),
    onSecondaryContainer = Color(0xFF382D23),
    background = Color(0xFFFAF8F3),
    onBackground = Color(0xFF292D29),
    surface = Color(0xFFFFFEFB),
    onSurface = Color(0xFF292D29),
    surfaceVariant = Color(0xFFF0EEE7),
    onSurfaceVariant = Color(0xFF5F665F),
    outline = Color(0xFFB8BDB4),
    error = Color(0xFFBA3939)
)

private val ElegantTypography = Typography(
    headlineMedium = Typography().headlineMedium.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp
    ),
    headlineSmall = Typography().headlineSmall.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = Typography().titleLarge.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = Typography().titleMedium.copy(
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = Typography().bodyLarge.copy(
        fontSize = 16.sp
    )
)

class MainActivity : ComponentActivity() {

    private val eventViewModel: EventViewModel by viewModels()

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ ->
            // The app remains usable if permission is declined.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EventReminderScheduler.createNotificationChannel(this)

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        setContent {
            MaterialTheme(
                colorScheme = ElegantColors,
                typography = ElegantTypography
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EventPlanningScreen(eventViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventPlanningScreen(
    eventViewModel: EventViewModel = viewModel()
) {
    val events by eventViewModel.events.collectAsState()
    val operationError by eventViewModel.errorMessage.collectAsState()
    val operationSuccess by eventViewModel.successMessage.collectAsState()

    var showForm by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<EventEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<EventEntity?>(null) }

    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var reminderMinutes by remember { mutableStateOf(0) }
    var showReminderMenu by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingSave by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(is24Hour = false)

    val reminderOptions = listOf(
        0 to "No reminder",
        5 to "5 minutes before",
        15 to "15 minutes before",
        30 to "30 minutes before",
        60 to "1 hour before",
        1440 to "1 day before"
    )

    fun reminderLabel(minutes: Int): String {
        return reminderOptions.firstOrNull { it.first == minutes }?.second
            ?: "No reminder"
    }

    fun clearForm() {
        name = ""
        date = ""
        time = ""
        location = ""
        description = ""
        reminderMinutes = 0
        showReminderMenu = false
        errorMessage = null
        editingEvent = null
        pendingSave = false
        showForm = false
    }

    fun editEvent(event: EventEntity) {
        editingEvent = event
        name = event.name
        date = event.date
        time = event.time
        location = event.location
        description = event.description
        reminderMinutes = event.reminderMinutes
        errorMessage = null
        pendingSave = false
        eventViewModel.clearError()
        eventViewModel.clearSuccess()
        showForm = true
    }

    fun saveEvent() {
        if (name.isBlank() || date.isBlank() || location.isBlank()) {
            errorMessage = "Please enter an event name, date, and location."
            return
        }

        if (reminderMinutes > 0 && time.isBlank()) {
            errorMessage = "Please choose an event time to use a reminder."
            return
        }

        if (pendingSave) return

        errorMessage = null
        pendingSave = true
        eventViewModel.clearError()
        eventViewModel.clearSuccess()

        val currentEvent = editingEvent

        if (currentEvent == null) {
            eventViewModel.addEvent(
                name = name,
                date = date,
                time = time,
                location = location,
                description = description,
                reminderMinutes = reminderMinutes
            )
        } else {
            eventViewModel.updateEvent(
                currentEvent.copy(
                    name = name.trim(),
                    date = date.trim(),
                    time = time.trim(),
                    location = location.trim(),
                    description = description.trim(),
                    reminderMinutes = reminderMinutes
                )
            )
        }
    }

    LaunchedEffect(operationSuccess) {
        if (operationSuccess != null && pendingSave) {
            clearForm()
        }
    }

    LaunchedEffect(operationError) {
        if (operationError != null) {
            pendingSave = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Event Planner",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Keep all your plans in one place.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        operationSuccess?.let { message ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        operationError?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (showForm) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (editingEvent == null) {
                        "Create an event"
                    } else {
                        "Edit your event"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Add the details below to keep everything organized.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Event name *") },
                    placeholder = { Text("e.g. Birthday dinner") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !pendingSave
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Event date *") },
                    placeholder = { Text("Choose a date") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !pendingSave,
                    trailingIcon = {
                        TextButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.semantics {
                                contentDescription = "Choose event date"
                            },
                            enabled = !pendingSave
                        ) {
                            Text("Choose")
                        }
                    }
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Event time") },
                    placeholder = { Text("Optional") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !pendingSave,
                    trailingIcon = {
                        TextButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.semantics {
                                contentDescription = "Choose event time"
                            },
                            enabled = !pendingSave
                        ) {
                            Text("Choose")
                        }
                    }
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = {
                        location = it
                        errorMessage = null
                    },
                    label = { Text("Location *") },
                    placeholder = { Text("Where will it happen?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !pendingSave
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes") },
                    placeholder = { Text("Add any extra details") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = !pendingSave
                )

                // Reminder selection
                Column {
                    Text(
                        text = "Event reminder",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Choose when to receive a notification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box {
                        OutlinedButton(
                            onClick = { showReminderMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !pendingSave,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                if (reminderMinutes == 0) {
                                    "Would you like a reminder?"
                                } else {
                                    reminderLabel(reminderMinutes)
                                }
                            )
                        }

                        DropdownMenu(
                            expanded = showReminderMenu,
                            onDismissRequest = {
                                showReminderMenu = false
                            }
                        ) {
                            reminderOptions.forEach { (minutes, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        reminderMinutes = minutes
                                        showReminderMenu = false
                                        errorMessage = null
                                    }
                                )
                            }
                        }
                    }
                }

                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = { saveEvent() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !pendingSave
                ) {
                    Text(
                        text = when {
                            pendingSave -> "Saving..."
                            editingEvent == null -> "Save event"
                            else -> "Update event"
                        }
                    )
                }

                OutlinedButton(
                    onClick = { clearForm() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !pendingSave
                ) {
                    Text("Cancel")
                }
            }
        } else {
            Button(
                onClick = {
                    clearForm()
                    eventViewModel.clearError()
                    eventViewModel.clearSuccess()
                    showForm = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("+  Create new event")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Your events",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "${events.size} planned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (events.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 1.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "A fresh beginning",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Your plans will appear here. Create your first event to get started.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(
                        items = events,
                        key = { it.id }
                    ) { event ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = event.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = "DATE   ${event.date}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (event.time.isNotBlank()) {
                                    Text(
                                        text = "TIME   ${event.time}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = "LOCATION   ${event.location}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (event.description.isNotBlank()) {
                                    Text(
                                        text = event.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (event.reminderMinutes > 0) {
                                    Text(
                                        text = "REMINDER   ${reminderLabel(event.reminderMinutes)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { editEvent(event) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Edit")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            eventViewModel.clearError()
                                            eventViewModel.clearSuccess()
                                            deleteTarget = event
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat(
                                "MM/dd/yyyy",
                                Locale.US
                            ).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            date = formatter.format(Date(millis))
                            errorMessage = null
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        Dialog(
            onDismissRequest = { showTimePicker = false }
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Choose a time",
                        style = MaterialTheme.typography.titleLarge
                    )

                    TimePicker(state = timePickerState)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showTimePicker = false }
                        ) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(
                            onClick = {
                                val hour = timePickerState.hour
                                val minute = timePickerState.minute
                                val amPm = if (hour < 12) "AM" else "PM"
                                val displayHour = when (hour % 12) {
                                    0 -> 12
                                    else -> hour % 12
                                }

                                time = String.format(
                                    Locale.US,
                                    "%d:%02d %s",
                                    displayHour,
                                    minute,
                                    amPm
                                )
                                errorMessage = null
                                showTimePicker = false
                            }
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { event ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete this event?") },
            text = {
                Text("Are you sure you want to delete \"${event.name}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        eventViewModel.deleteEvent(event)
                        deleteTarget = null
                    }
                ) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deleteTarget = null }
                ) {
                    Text("Keep event")
                }
            }
        )
    }
}
