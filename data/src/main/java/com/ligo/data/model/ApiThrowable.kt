package com.ligo.data.model

import com.google.gson.annotations.SerializedName

open class ApiThrowable(
    @SerializedName("errorCode") val errorCode: Int,
    @SerializedName("errorMessage") val errorMessage: String = "",
) : Throwable(errorMessage) {

    fun mapToLocalError(): ApiThrowable = when (errorCode) {
        NoSuchUser.CODE -> NoSuchUser(errorMessage)
        NoSuchTrip.CODE -> NoSuchTrip(errorMessage)
        NoSuchParcel.CODE -> NoSuchParcel(errorMessage)
        SecretNotFound.CODE -> SecretNotFound(errorMessage)
        CantFindRoute.CODE -> CantFindRoute(errorMessage)
        CantCreateUser.CODE -> CantCreateUser(errorMessage)

        UserExists.CODE -> UserExists(errorMessage)
        NotADriver.CODE -> NotADriver(errorMessage)
        SearchRequestRequired.CODE -> SearchRequestRequired(errorMessage)
        OriginAndDestinationRequired.CODE -> OriginAndDestinationRequired(errorMessage)

        WrongPassword.CODE -> WrongPassword(errorMessage)
        TokenRequired.CODE -> TokenRequired(errorMessage)
        InvalidToken.CODE -> InvalidToken(errorMessage)
        NotEnoughRights.CODE -> NotEnoughRights(errorMessage)
        AccessDenied.CODE -> AccessDenied(errorMessage)
        ParcelNotInYourTrip.CODE -> ParcelNotInYourTrip(errorMessage)
        ParcelInActiveTrip.CODE -> ParcelInActiveTrip(errorMessage)
        ProvideTokenOrPassword.CODE -> ProvideTokenOrPassword(errorMessage)

        DriverHasAnActiveTrip.CODE -> DriverHasAnActiveTrip(errorMessage)
        PointsEqual.CODE -> PointsEqual(errorMessage)
        RatingExist.CODE -> RatingExist(errorMessage)
        else -> this
    }

    class NoSuchUser(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 101
        }
    }

    class NoSuchTrip(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 102
        }
    }

    class NoSuchParcel(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 103
        }
    }

    class SecretNotFound(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 104
        }
    }

    class CantFindRoute(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 105
        }
    }

    class CantCreateUser(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 106
        }
    }

    class UserExists(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 201
        }
    }

    class NotADriver(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 202
        }
    }

    class SearchRequestRequired(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 203
        }
    }

    class OriginAndDestinationRequired(errorMessage: String = "") :
        ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 204
        }
    }

    class WrongPassword(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 301
        }
    }

    class TokenRequired(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 302
        }
    }

    class InvalidToken(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 303
        }
    }

    class NotEnoughRights(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 304
        }
    }

    class AccessDenied(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 305
        }
    }

    class ParcelNotInYourTrip(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 306
        }
    }

    class ParcelInActiveTrip(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 307
        }
    }

    class ProvideTokenOrPassword(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 308
        }
    }

    class DriverHasAnActiveTrip(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 401
        }
    }

    class PointsEqual(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 402
        }
    }

    class RatingExist(errorMessage: String = "") : ApiThrowable(CODE, errorMessage) {
        companion object {
            const val CODE = 403
        }
    }

    object Fatal : ApiThrowable(228)

    val errorLocalizationKey: String
        get() = when (this) {
            is NoSuchUser -> ConfigStringKey.API_ERROR_NO_SUCH_USER
            is NoSuchTrip -> ConfigStringKey.API_ERROR_NO_SUCH_TRIP
            is NoSuchParcel -> ConfigStringKey.API_ERROR_NO_SUCH_PARCEL
            is SecretNotFound -> ConfigStringKey.API_ERROR_SECRET_NOT_FOUND
            is CantFindRoute -> ConfigStringKey.API_ERROR_CANT_FIND_ROUTE
            is UserExists -> ConfigStringKey.API_ERROR_THIS_USER_ALREADY_EXIST
            is NotADriver -> ConfigStringKey.API_ERROR_YOU_ARE_NOT_A_DRIVER
            is WrongPassword -> ConfigStringKey.API_ERROR_WRONG_PASSWORD
            is TokenRequired -> ConfigStringKey.API_ERROR_TOKEN_REQUIRED
            is InvalidToken -> ConfigStringKey.API_ERROR_INVALID_TOKEN
            is NotEnoughRights -> ConfigStringKey.API_ERROR_NOT_ENOUGH_RIGHT
            is AccessDenied -> ConfigStringKey.API_ERROR_ACCESS_DENIED
            is ParcelNotInYourTrip -> ConfigStringKey.API_ERROR_THIS_PARCEL_IS_NOT_IN_YOUR_TRIP
            is DriverHasAnActiveTrip -> ConfigStringKey.API_ERROR_YOU_HAVE_AN_ACTIVE_TRIP
            is PointsEqual -> ConfigStringKey.API_ERROR_POINTS_EQUAL
            is RatingExist -> ConfigStringKey.API_ERROR_RATING_EXIST
            is ParcelInActiveTrip -> ConfigStringKey.API_ERROR_PARCEL_IN_ACTIVE_TRIP
            is ProvideTokenOrPassword -> ConfigStringKey.API_ERROR_PROVIDE_VALID_TOKEN_OR_PASSWORD
            is CantCreateUser -> ConfigStringKey.API_ERROR_CANT_CREATE_USER
            is SearchRequestRequired -> ConfigStringKey.API_ERROR_SEARCH_REQUEST_REQUIRED
            is OriginAndDestinationRequired -> ConfigStringKey.API_ERROR_ORIGIN_AND_DESTINATION_REQUIRED
            else -> ConfigStringKey.API_ERROR_SOMETHING_WENT_WRONG
        }
}