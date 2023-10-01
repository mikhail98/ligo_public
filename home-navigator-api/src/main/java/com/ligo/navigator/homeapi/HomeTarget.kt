package com.ligo.navigator.homeapi

sealed class HomeTarget {
    object Delivery : HomeTarget()

    object History : HomeTarget()

    object Profile : HomeTarget()
}