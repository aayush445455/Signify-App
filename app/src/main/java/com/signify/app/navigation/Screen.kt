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
// sealed classes ensures only the define object intansces exist for type safety to navigation routes. simmlllar to enum class but each object can hold additional data or logiv