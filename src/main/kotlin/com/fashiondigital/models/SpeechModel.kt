package com.fashiondigital.models

import java.time.LocalDate

class SpeechModel(
val speaker: String,
val topic: String,
val date: LocalDate,
val words: Int
)