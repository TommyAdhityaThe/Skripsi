package models;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 * @author Tommy Adhitya The
 */
public class Message {
    String httpcode;
    ObjectNode alert;

    public Message() {
    }

    public Message(String httpcode, ObjectNode alert) {
        this.httpcode = httpcode;
        this.alert = alert;
    }

    public void setHttpcode(String httpcode) {
        this.httpcode = httpcode;
    }

    public void setAlert(ObjectNode alert) {
        this.alert = alert;
    }

    public String getHttpcode() {
        return httpcode;
    }

    public ObjectNode getAlert() {
        return alert;
    }

}
