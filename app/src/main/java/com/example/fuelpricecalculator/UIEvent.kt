package com.example.fuelpricecalculator

/**
 * Represents snack bar visuals sent to the UI.
 *
 */
open class UIEvent{
    data class ShowSnackBar(val message: String) : UIEvent()
}