package com.example.data.model

enum class SessionType(val displayName: String) {
    CLASS("Class"),
    POSTING("Posting"),
    LAB("Lab");

    companion object {
        fun fromString(value: String): SessionType {
            return when (value.trim().uppercase()) {
                "POSTING", "CLINICAL", "WARD" -> POSTING
                "LAB", "PRACTICAL", "LABORATORY" -> LAB
                else -> CLASS
            }
        }
    }
}
