package com.signify.app.navigation

sealed class Screen(val route: String) {
    object Login      : Screen("login")
    object Register   : Screen("register")
    object Home       : Screen("home")
    object Lessons    : Screen("lessons")
    object Translator : Screen("translator")
    object History    : Screen("history")
    object Settings   : Screen("settings")
    object Profile    : Screen("profile")
    object ContactUs  : Screen("contact_us")
}
