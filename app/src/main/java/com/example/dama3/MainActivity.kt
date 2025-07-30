package com.example.dama3

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.example.dama3.ui.Instructions
import com.example.dama3.ui.Options
import com.example.dama3.ui.Play
import com.example.dama3.ui.theme.Dama3Theme
import com.example.domain.states.Difficulty
import com.example.domain.states.Mode
import com.example.domain.userConf.UserConf
import kotlinx.serialization.Serializable

//import kotlinx.serialization.Serializable as Serializable

class MainActivity : ComponentActivity() {
companion object {
    const val TAG = "MainActivity"

}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            Dama3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    Options
                ){
                    composable<Options> {
                      Options(modifier = Modifier.padding(innerPadding)){ getMode, userConf, dif ->
                          val userturn = userConf.userturn
                          val autoplay = userConf.userAuto
                          val mode = getMode()
                          Log.d(TAG, "sending arguments $userturn $autoplay $mode")
                          navController.navigate(Play(mode, userturn, autoplay, dif))
                      }
                    }
                    composable<Play> {
                        val args = it.toRoute<Play>()
                        Log.d(TAG, "getting arguments $args")
                        Play(modifier = Modifier.padding(innerPadding),
                            args.mode,
                            UserConf(args.userTurn, args.autoPlay),
                            args.dif)
                    }
                    composable<Instructions> {
                        Instructions(modifier = Modifier.padding(innerPadding))
                        }
                    }
                }
            }
        }
    }
}
@Serializable
object Instructions
@Serializable
object Options
@Serializable
data class Play(val mode: Mode, val userTurn: Int, val autoPlay: Boolean, val dif: Difficulty)
