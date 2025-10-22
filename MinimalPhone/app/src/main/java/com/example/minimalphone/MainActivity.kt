// Lets the app know what package the code is in
// This should always be at the top
package com.example.minimalphone

//Import statements from the prototype
//Import statements from template are commented at the bottom
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.minimalphone.ui.theme.MinimalPhoneTheme
import com.example.minimalphone.ui.theme.MinimalPhoneTheme



// AppCompatActivity tells the code to act like a normal Android Screen, there are other options for this
class MainActivity : AppCompatActivity() {

    // Override the default onCreate function with this mess
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // RecyclerView is basically a list viewer, it's formed by grabbing ids, linearlayout just means its vertical
        val recyclerView: RecyclerView = findViewById(R.id.appsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // PackageManger asks Android for all the apps on the phone
        val pm: PackageManager = packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            // Only grabs apps that can be opened (apps with an icon)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            // Creates AppInfo object for each app
            .map { AppInfo(it.loadLabel(pm).toString(), it.packageName) }
        // Connects the list to the adapter(displays each app's name on screen)
        recyclerView.adapter = AppsAdapter(this, installedApps)
        val button: Button = findViewById(R.id.startServiceButton)
        button.setOnClickListener {
            val intent = Intent(this, ForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }
}




// This is a debug thing to make sure the UI is actually connected and its not an emulator problem
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MinimalPhoneTheme {
        Text(text = "Hello Android!")
    }
}




//Default import statements from template
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.core.view.WindowCompat.enableEdgeToEdge
//import com.example.minimalphone.ui.theme.MinimalPhoneTheme