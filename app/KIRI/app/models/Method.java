package models;

import java.util.Random;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Method{
	protected String generate_password(){
        return this.generate_random("abcdefghiklmnopqrstuvwxyz0123456789", 8);
    }
    
    protected String generate_random(String chars, int length){
        int chars_size = chars.length();
        Random random=new Random();
        String string = "";
        for (int i = 0; i < length; i++) {
            string += chars.charAt(random.nextInt(chars_size));
        }
        return string;
    }

    protected ObjectNode well_done(String message) {
        ObjectNode obj = Json.newObject();
        obj.put("status", "ok");
        if (message != null) {
            obj.put("status", message);
        }
        return obj;
    }

    protected ObjectNode return_invalid_credentials(String logmessage) {
        ObjectNode obj = Json.newObject();
        obj.put("status", "credentialfail");
        return obj;
    }
}