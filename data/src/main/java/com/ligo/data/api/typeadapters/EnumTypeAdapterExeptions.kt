package com.ligo.data.api.typeadapters

class NoDefaultValueForEnumException(enumClass: Class<*>) : IllegalStateException(
    "no default value for ENUM: $enumClass"
)

class MultipleDefaultValuesEnumException(
    enumClass: Class<*>,
    defaultValueList: List<Enum<*>>,
) : IllegalStateException(
    "you have more then 1 default value for ENUM: $enumClass, " +
        "default values ${defaultValueList.joinToString { it.name }}"
)