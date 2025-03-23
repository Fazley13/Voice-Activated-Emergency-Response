module com.codevengers.voiceemergency {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.codevengers.voiceemergency to javafx.fxml;
    exports com.codevengers.voiceemergency;
}