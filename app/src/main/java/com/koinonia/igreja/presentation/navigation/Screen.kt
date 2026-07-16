package com.koinonia.igreja.presentation.navigation

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object ForgotPassword : Screen("forgot_password")
    data object MembersList : Screen("members_list")
    data object MemberAdd : Screen("member_add")
    data class MemberDetails(val memberId: String) : Screen("member_details/$memberId") {
        companion object {
            const val ROUTE_TEMPLATE = "member_details/{memberId}"
        }
    }
    data object Calendar : Screen("calendar")
    data object Reception : Screen("reception")
    data object Reports : Screen("reports")
}
