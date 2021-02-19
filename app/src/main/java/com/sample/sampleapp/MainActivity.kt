package com.sample.sampleapp

import android.os.Bundle
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.sample.sampleapp.ui.theme.ExperimentAppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExperimentAppTheme {
                val navController = rememberNavController()
                var actionCallback: ActionCallback? = null

                Scaffold(
                    topBar = {
                        val navEntry by navController.currentBackStackEntryAsState()
                        val route = navEntry?.arguments?.getString(KEY_ROUTE)
                        TopAppBar(
                            title = {
                                val title = when (route) {
                                    Destinations.SCREEN_A_ROUTE -> "Intro"
                                    Destinations.SCREEN_B_ROUTE -> "Intermediate"
                                    Destinations.SCREEN_C_ROUTE -> "Profile"
                                    else -> ""
                                }
                                Text(title)
                            },
                            navigationIcon = {
                                /**
                                 * Also not sure how to show back button properly
                                 * */
                                val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current.onBackPressedDispatcher
                                if (navEntry != null) {
                                    IconButton(onClick = {
                                        backPressedDispatcher.onBackPressed()
                                    }) {
                                        Icon(
                                            Icons.Rounded.ArrowBack,
                                            null,
                                        )
                                    }
                                }
                            },
                            actions = {
                                /**
                                 * This is our current approach to be able to change,
                                 * AppBar action with destination changes.
                                 *
                                 * This doesn't feel like an ideal solution, as this
                                 * approach would bloat as more screen gets added.
                                 * */
                                when (route) {
                                    Destinations.SCREEN_B_ROUTE -> {
                                        IconButton(onClick = {
                                            /* This should do something */
                                        }) {
                                            Icon(
                                                Icons.Rounded.Calculate,
                                                null,
                                            )
                                        }
                                        IconButton(onClick = {
                                            /* This should do something */
                                        }) {
                                            Icon(
                                                Icons.Rounded.CalendarToday,
                                                null,
                                            )
                                        }
                                    }
                                    Destinations.SCREEN_C_ROUTE -> {
                                        IconButton(onClick = {
                                            /**
                                             * When this button is clicked, it should save
                                             * the details (name & address) defined in ScreenC.
                                             *
                                             * The problem is that name & address are defined
                                             * locally in Screen C while this button is defined
                                             * above it. Name and Address could not be hoisted up, as
                                             * apart from this button (and in ScreenC), it won't
                                             * be referenced by anything else.
                                             *
                                             * It would have been ideal to simply call:
                                             *     save(name, address)
                                             *
                                             * To work around this we set a callback to which a
                                             * specific screen could set to. [ActionCallback]
                                             * But this doesn't feel very ideal as well.
                                             *
                                             * It is kind of the same idea that
                                             * [Activity.onOptionsItemSelected]
                                             * applies but it feels like going a step back.
                                             * */
                                            actionCallback?.onAction(SAVE_ID)
                                        }) {
                                            Icon(
                                                Icons.Rounded.Save,
                                                null,
                                            )
                                        }
                                    }
                                    else -> { }
                                }
                            },
                        )
                    }
                ) { padding ->
                    val modifier = Modifier.padding(padding)
                    NavHost(
                        navController = navController,
                        startDestination = Destinations.SCREEN_A_ROUTE
                    ) {
                        composable(Destinations.SCREEN_A_ROUTE) {
                            ScreenA(
                                modifier = modifier,
                                navController = navController,
                            )
                        }
                        composable(Destinations.SCREEN_B_ROUTE) {
                            ScreenB(
                                modifier = modifier,
                                navController = navController,
                            )
                        }
                        composable(Destinations.SCREEN_C_ROUTE) {
                            ScreenC(
                                modifier = modifier,
                                navController = navController,
                                actionCallbackSetter = {
                                    actionCallback = it
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenA(
    modifier: Modifier,
    navController: NavController,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Button(onClick = {
            navController.navigate(Destinations.SCREEN_B_ROUTE)
        }) {
            Text(text = "Next")
        }
    }
}

@Composable
fun ScreenB(
    modifier: Modifier,
    navController: NavController,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Button(onClick = {
            navController.navigate(Destinations.SCREEN_C_ROUTE)
        }) {
            Text(text = "Next")
        }
    }
}

@Composable
fun ScreenC(
    modifier: Modifier,
    navController: NavController,
    actionCallbackSetter: (ActionCallback?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    val profileViewModel = viewModel<ProfileViewModel>()

    DisposableEffect(navController, profileViewModel) {
        actionCallbackSetter(ActionCallback {
            when (it) {
                SAVE_ID -> {
                    profileViewModel.save(name, address)
                }
            }
        })
        onDispose {
            actionCallbackSetter(null)
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextField(
            value = name,
            onValueChange = {
                name = it
            },
            label = {
                Text(text = "Name")
            }
        )
        TextField(
            value = address,
            onValueChange = {
                address = it
            },
            label = {
                Text(text = "Address")
            }
        )
    }
}

object Destinations {
    const val SCREEN_A_ROUTE = "screenA"
    const val SCREEN_B_ROUTE = "screenB"
    const val SCREEN_C_ROUTE = "screenC"
}

fun interface ActionCallback {
    fun onAction(id: Int)
}

const val SAVE_ID = 100