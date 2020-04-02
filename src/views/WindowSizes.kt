package views

import javafx.scene.Parent
import javafx.scene.Scene

object WindowSizes {
    object main {
        const val width: Double = 800.0
        const val height: Double = 620.0

        fun create(parent: Parent) : Scene {
            return Scene(parent, width, height)
        }
    }
}