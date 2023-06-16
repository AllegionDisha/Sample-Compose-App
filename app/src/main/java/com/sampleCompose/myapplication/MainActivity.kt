package com.sampleCompose.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sampleCompose.myapplication.ui.theme.SampleComposeAppTheme
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleComposeAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation()
                }
            }
        }
    }
}

@Composable
fun NavigationController(navController: NavHostController){
    NavHost(navController = navController, startDestination = NavigationItem.Home.route){
        composable(NavigationItem.Home.route){
            HomeUI(navController = navController)
        }
        composable(NavigationItem.Settings.route){
            SettingsUI(navController = navController)
        }
        composable(
            route = "logging/{city}",
            arguments = listOf(navArgument("city") { type = NavType.StringType })
        ){ entry ->
            val city = entry.arguments?.getString("city") ?: ""
            LoggingUI(navController = navController, cityState = city)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation() {
    // Create variables to list the different screens and remember the current screen
    val navController = rememberNavController()
    val items = listOf(NavigationItem.Home, NavigationItem.Logging, NavigationItem.Settings)

    Scaffold(
        bottomBar = {
            // Use BottomNavigation instead of BottomAppBar in case we need to add more screens later
            BottomNavigation(backgroundColor = MaterialTheme.colorScheme.background) {
                // Retrieve the current navigation back stack entry and current route
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val context = LocalContext.current
                //simple loop which iterates through item list of nav links and lists them as BottomNavItem
                items.forEach { item ->
                    BottomNavigationItem(
                        selected = currentRoute == item.route,
                        label = {
                            Text(
                                text = item.label,
                                color = if (currentRoute == item.route) Color.DarkGray else Color.LightGray
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = item.icons,
                                contentDescription = null,
                                tint = if (currentRoute == item.route) Color.DarkGray else Color.LightGray
                            )
                        },
                        onClick = {
                            //ensures that we are only navigating when we click on a different route
                            if (currentRoute != item.route) {
                                if (item == NavigationItem.Logging ){
                                    val savedCity = navBackStackEntry?.arguments?.getString("city")
                                    if (savedCity == null || savedCity.isBlank()) {
                                        Toast.makeText(context,"Please enter a city name!", Toast.LENGTH_SHORT).show()
                                    }
                                }else{
                                    // Pop back stack to the start destination and navigate to the selected item
                                    navController.graph.startDestinationRoute?.let {
                                        navController.popBackStack(it, inclusive = true)
                                    }

                                    // Navigate to the selected item's route
                                    navController.navigate(item.route) {
                                        // Specify true to ensure the item is not added to the back stack again
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }
                    )
                }
            }
        },
        content = { padding ->
            Box(modifier = Modifier.padding(padding)) {
                NavigationController(navController = navController)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUI(navController:NavController){
    val cityState = remember { mutableStateOf("") }
    val isError = cityState.value.isEmpty()
    val context = LocalContext.current

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(start = 25.dp, top = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "Input a City",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            )
        Spacer(modifier = Modifier.height(40.dp))
        // Input field for the city
        // Store the entered text in cityState.value
        TextField(
            value = cityState.value,
            onValueChange = { cityState.value = it },
            modifier = Modifier.size(width = 300.dp, height = 50.dp),
            textStyle = TextStyle(
                fontSize = 15.sp,
            )

        )
        Spacer(modifier = Modifier.height(40.dp))
        // Submit button
        Button(onClick = {
            if (!isError) {
                navController.navigate("logging/${cityState.value}")
            } else{
                Toast.makeText(context,"Please enter a city name!", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Submit")
        }
    }
}
@Composable
fun SettingsUI(navController:NavController){
    var isStandardMeasurement: MutableState<Boolean> = remember {mutableStateOf(true)}
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Submit button
        Button(onClick = { isStandardMeasurement.value = false }) {
            Text(text = "Metric")
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(onClick = { isStandardMeasurement.value = true}) {
            Text(text = "Standard")
        }
    }
}

@Composable
fun LoggingUI(navController:NavController, cityState:String){
    Text(
        text = cityState,
        style = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )
    )
}