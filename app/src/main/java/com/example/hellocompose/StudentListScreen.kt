package com.example.hellocompose

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hellocompose.ui.theme.HelloComposeTheme


data class Student(
    val nim: String,
    val nama: String,
    val ipk: Double
)


@Composable
fun StudentListScreen(students: List<Student>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        item {
            Text(
                text = "Daftar Mahasiswa",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // List Item
        items(students, key = { it.nim }) { student ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = student.nama,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = student.nim,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // warna GPA
                    val ipkColor = when {
                        student.ipk >= 3.5 -> Color(0xFF4CAF50) // Hijau
                        student.ipk >= 3.0 -> Color(0xFF2196F3) // Biru
                        else -> Color(0xFFF44336)               // Merah
                    }

                    Text(
                        text = "%.2f".format(student.ipk),
                        style = MaterialTheme.typography.titleMedium,
                        color = ipkColor
                    )
                }
            }
        }

        // footer
        item {
            Text(
                text = "Total: ${students.size} mahasiswa",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


fun getDummyStudents(): List<Student> {
    return listOf(
        Student("001", "Gabriel Emil", 3.85),
        Student("002", "Astolfo", 3.45),
        Student("003", "Baskara Putra", 2.90),
        Student("004", "Tiara Andini", 3.95),
        Student("005", "Yovie Widianto", 3.10),
        Student("006", "Navia Di Rosula", 3.60),
        Student("007", "Balmond", 2.75),
        Student("008", "Kiana Kaslana", 3.25),
        Student("009", "Raiden Bosenmori Mei", 3.70),
        Student("010", "Prabowo Subianto", 3.05)
    )
}

// LightMode and DarkMode
@Preview(showBackground = true, name = "Light Mode")
@Composable
fun StudentListScreenLightPreview() {
    HelloComposeTheme {
        Surface {
            StudentListScreen(students = getDummyStudents())
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun StudentListScreenDarkPreview() {
    HelloComposeTheme {
        Surface {
            StudentListScreen(students = getDummyStudents())
        }
    }
}

