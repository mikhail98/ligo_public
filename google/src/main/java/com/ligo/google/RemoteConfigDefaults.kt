package com.ligo.google

internal object RemoteConfigDefaults {

    const val DEFAULT_CURRENCIES = "{" +
        "\"currencies\":[" +
        "{\"code\": \"EUR\", fullNameKey: \"CURRENCY_FULL_NAME_EUR\"}," +
        "{\"code\": \"PLN\", fullNameKey: \"CURRENCY_FULL_NAME_PLN\"}," +
        "{\"code\": \"USD\", fullNameKey: \"CURRENCY_FULL_NAME_USD\"}" +
        "]" +
        "}"

    const val DEFAULT_PARCEL_TYPES = "{" +
        "\"types\":[" +
        "{\"type\": \"SMALL\", \"titleKey\": \"PARCEL_TYPE_SMALL_TITLE\", \"descriptionKey\": \"PARCEL_TYPE_SMALL_DESCRIPTION\"}," +
        "{\"type\": \"MEDIUM\", \"titleKey\": \"PARCEL_TYPE_MEDIUM_TITLE\", \"descriptionKey\": \"PARCEL_TYPE_MEDIUM_DESCRIPTION\"}," +
        "{\"type\": \"LARGE\", \"titleKey\": \"PARCEL_TYPE_LARGE_TITLE\", \"descriptionKey\": \"PARCEL_TYPE_LARGE_DESCRIPTION\"}," +
        "{\"type\": \"OVERSIZE\", \"titleKey\": \"PARCEL_TYPE_OVERSIZE_TITLE\", \"descriptionKey\": \"PARCEL_TYPE_OVERSIZE_DESCRIPTION\"}," +
        "{\"type\": \"DOCUMENTS\", \"titleKey\": \"PARCEL_TYPE_DOCUMENTS_TITLE\", \"descriptionKey\": \"PARCEL_TYPE_DOCUMENTS_DESCRIPTION\"}" +
        "]" +
        "}"
}
