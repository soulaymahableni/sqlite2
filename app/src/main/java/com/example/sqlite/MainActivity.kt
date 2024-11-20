package com.example.sqlite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.sqlite.ui.theme.SqliteTheme
import android.content.ContentValues

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SqliteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    UserRegistrationScreen(this)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRegistrationScreen(context: Context) {
    var userName by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<String?>(null) }
    var userList by remember { mutableStateOf(emptyList<String>()) }

    val dbHelper = DatabaseHelper(context)

    LaunchedEffect(Unit) {
        userList = dbHelper.getAllUsers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("User Name") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (selectedUser == null) {
                dbHelper.insertUser(userName)
            } else {
                dbHelper.updateUser(selectedUser!!, userName)
                selectedUser = null // Clear selection after update
            }
            userName = ""
            userList = dbHelper.getAllUsers()
        }) {
            Text(if (selectedUser == null) "Register User" else "Update User")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            selectedUser?.let {
                dbHelper.deleteUser(it)
                userList = dbHelper.getAllUsers()
                selectedUser = null
                userName = ""
            }
        }, enabled = selectedUser != null) {
            Text("Delete User")
        }

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn {
            items(userList) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(user)
                    Button(onClick = {
                        selectedUser = user
                        userName = user // Set text field with selected user for updating
                    }) {
                        Text("Edit")
                    }
                }
            }
        }
    }
}

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "users.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE users (name TEXT PRIMARY KEY)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun insertUser(name: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
        }
        db.insert("users", null, values)
        db.close()
    }

    fun getAllUsers(): List<String> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name FROM users", null)
        val userList = mutableListOf<String>()
        while (cursor.moveToNext()) {
            userList.add(cursor.getString(0))
        }
        cursor.close()
        db.close()
        return userList
    }

    fun updateUser(oldName: String, newName: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", newName)
        }
        val selection = "name LIKE ?"
        val selectionArgs = arrayOf(oldName)
        db.update("users", values, selection, selectionArgs)
        db.close()
    }

    fun deleteUser(name: String) {
        val db = writableDatabase
        val selection = "name LIKE ?"
        val selectionArgs = arrayOf(name)
        db.delete("users", selection, selectionArgs)
        db.close()
    }
}