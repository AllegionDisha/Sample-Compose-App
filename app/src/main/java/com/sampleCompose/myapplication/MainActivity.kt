package com.sampleCompose.myapplication

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sampleCompose.myapplication.ui.theme.SampleComposeAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //pass this to composable so every screen can have access to metric or standard vars
        var stateVars = AppViewModel()

        super.onCreate(savedInstanceState)
        setContent {
            SampleComposeAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation(stateVars)
                }
            }
        }
    }
}

@Composable
fun NavigationController(navController: NavHostController, stateVars: AppViewModel){
    NavHost(navController = navController, startDestination = NavigationItem.Home.route){
        composable(NavigationItem.Home.route){
            HomeUI(navController = navController, model = stateVars)
        }
        composable(NavigationItem.Settings.route){
            SettingsUI(navController = navController, model = stateVars)
        }
        composable(
            route = "logging/{city}",
            arguments = listOf(
                navArgument("city") {
                    type = NavType.StringType
                },
                navArgument("measure"){
                    type = NavType.StringType
                    defaultValue = "standard"
                }
            )
        ){ entry ->
            val city = entry.arguments?.getString("city") ?: ""
            LoggingUI(navController = navController, cityState = city, model = stateVars)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(stateVars: AppViewModel) {
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
                NavigationController(navController = navController, stateVars)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUI(navController:NavController, model:AppViewModel){
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
fun SettingsUI(navController:NavController, model:AppViewModel){
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Submit button
        Button(onClick = {
            model.setMeasurementOption("metric")
        }) {
            Text(text = "Metric")
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(onClick = {
            model.setMeasurementOption("standard")
        }) {
            Text(text = "Standard")
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = {
            model.setMeasurementOption("imperial")
        }) {
            Text(text = "Imperial")
        }
    }
}

@Composable
fun LoggingUI(navController:NavController, cityState:String, model: AppViewModel){
    val measurementType = model.MeasurementOption.value
    var weatherData by remember { mutableStateOf<DataItem?>(null) }
    val context = LocalContext.current
    val errorMessage = remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = cityState,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            ),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Text(
            text = "Reported in $measurementType",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            modifier = Modifier.padding(bottom = 20.dp)
        )
        LaunchedEffect(cityState) {
            Log.d("Start", "NOTE: HERE BEFORE")
            Log.d("City Name", cityState)
            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCall(cityState, model, context)
                }
                Log.d("Main", "NOTE: HERE AFTER")
                if (response.isSuccessful) {
                    Log.d("Main activity", "NOTE: Successful API call")
                    val weatherData = response.body()
                    Log.d("response data", weatherData.toString())
                } else {
                    Log.d("Main fail", "NOTE: Failed API call")
                    errorMessage.value = "Sorry, that city name is not retrievable"
                }
            } catch (e: Exception) {
                Log.e("Main exception", "NOTE: Exception during API call", e)
                errorMessage.value = "An error occurred during the API call"
            }
        }

        Log.d("AFTER", "NOTE: Continued call")
        errorMessage.value?.let { message ->
            Text(text = message)
        }
        weatherData?.let { data ->
            val temperatureUnit = when (measurementType) {
                "standard" -> "K"
                "metric" -> "°C"
                "imperial" -> "°F"
                else -> ""
            }

            val minTemp = "${data.main.temp_min?.toString()}$temperatureUnit"
            val maxTemp = "${data.main.temp_max?.toString()}$temperatureUnit"
            val weatherDescription = data.weather.firstOrNull()?.description
            val currTemp = "${data.main.temp?.toString()}$temperatureUnit"

            Text(text = "Current Temp: $currTemp")
            Text(text = "Min Temp: $minTemp")
            Text(text = "Max Temp: $maxTemp")
            Text(text = "Weather: $weatherDescription")
        }
    }
}
suspend fun makeApiCall(city: String, model: AppViewModel, context: Context):Response<DataItem> {
    val apiKey = context.getString(R.string.api_key)
    val units = model.MeasurementOption.value
    val api = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        // we need to add converter factory to
        // convert JSON object to Java object
        .build()
    val apiCall = api.create(ApiInterface::class.java)
    return apiCall.getDataByCity(city, apiKey, units)
}
