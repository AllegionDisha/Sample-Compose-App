package com.sampleCompose.myapplication

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

//created a sealed class so only the following types of items can be referenced in MainActivity
sealed class NavigationItem(val route: String, val label: String, val icons: ImageVector){
    object Home: NavigationItem("Home", "Home", Icons.Default.Home)
    object Settings: NavigationItem("Settings", "Settings", Icons.Default.Settings)
}
