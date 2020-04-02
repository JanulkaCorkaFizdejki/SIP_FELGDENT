package views

import javafx.scene.control.Alert

object Alerts {
    object simple {
        fun show(alertType: Alert.AlertType, title: String, contentText: String)  {
            var alert = Alert(alertType)
            alert.setTitle(title)
            alert.setHeaderText(null)
            alert.setContentText(contentText)
            alert.showAndWait()
        }
    }
}