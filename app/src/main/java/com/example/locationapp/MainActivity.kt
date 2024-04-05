package com.example.locationapp

import android.os.Bundle
import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationapp.ui.theme.LocationAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: LocationViewModel = viewModel()
            LocationAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                     MyApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun MyApp(viewModel: LocationViewModel){
    val context = LocalContext.current
    val locationUtils = LocationUtils(context)
    LocationDisplay(locationUtils = locationUtils,context = context, viewModel)
}

@Composable
fun LocationDisplay(locationUtils: LocationUtils, context: Context, viewModel: LocationViewModel){
    val location = viewModel.location.value
    val address = location?.let {
        locationUtils.reverseGeocodeLocation(location)
    }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
           permissions ->
             if(permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                 && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true){
                 // i have access to location
                 locationUtils.requestLocationUpdates(viewModel=viewModel)
             }else{
                // ask for permission
                 val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                     context as MainActivity,
                     Manifest.permission.ACCESS_FINE_LOCATION
                 ) || ActivityCompat.shouldShowRequestPermissionRationale(
                     context as MainActivity,
                     Manifest.permission.ACCESS_COARSE_LOCATION
                 )
                 if(rationalRequired){
                     Toast.makeText(context,
                         "Location Permission is required for this feature to work", Toast.LENGTH_LONG)
                         .show()
                 }else{
                     Toast.makeText(context,
                         "Location Permission is required. Please enable it in the android settings", Toast.LENGTH_LONG)
                         .show()
                 }
             }
        }
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) { 
        if(location!=null){
            Text(text = "Address:  Lat: ${location.latitude}   Log:  ${location.longitude} \n ${address}")
        }
        else{
             Text(text = "Location not available")
        }
        Spacer(modifier = Modifier.height(15.dp))
        Button(onClick = {
             if(locationUtils.hasLocationPermission(context)){
                 // permission already granted update the location
                 locationUtils.requestLocationUpdates(viewModel)
             }else{
                 // request location permission
                 requestPermissionLauncher.launch(
                     arrayOf(
                         Manifest.permission.ACCESS_FINE_LOCATION,
                         Manifest.permission.ACCESS_COARSE_LOCATION
                     )
                 )
             }
        }) {
             Text(text = "Get Location")
        }
    }
}
