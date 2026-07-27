package com.mohit.videoskipper.navigation

sealed class Routes(val route: String) {

    data object Home : Routes("home_screen")
    data object Text : Routes("text_screen")
    data object Image : Routes("image_screen")

}