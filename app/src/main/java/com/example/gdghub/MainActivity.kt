package com.example.gdghub
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.gdghub.ui.Theme.YourAppTheme
import com.example.gdghub.ui.screen.NewsDetailScreen
import com.example.gdghub.ui.screens.NewsFeedScreen
import com.example.gdghub.ui.viewModel.NewsViewModel
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val newsViewModel: NewsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourAppTheme { // Make sure this theme exists or use a default one
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
        lifecycleScope.launch {
            try {
                newsViewModel.exampleUsage()
            } catch (e: Exception) {
                println("Error calling exampleUsage from Activity: ${e.message}")
            }
        }

    }

}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val newsViewModel: com.example.gdghub.ui.viewModel.NewsViewModel = viewModel()

    NavHost(navController = navController, startDestination = "news_feed") {
        composable("news_feed") {
            NewsFeedScreen(
                newsViewModel = newsViewModel,
                onNavigateToDetail = { newsId ->
                    navController.navigate("news_detail/$newsId")
                }
            )
        }
        composable(
            route = "news_detail/{newsId}",
            arguments = listOf(navArgument("newsId") { type = NavType.StringType }) ,
            deepLinks = listOf(navDeepLink { uriPattern = "android-app://androidx.navigation/news_detail/{newsId}" }) // Or your custom scheme/host
        ) { backStackEntry ->
            val newsId = backStackEntry.arguments?.getString("newsId")
            NewsDetailScreen(
                newsViewModel = newsViewModel,
                newsId = newsId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
        