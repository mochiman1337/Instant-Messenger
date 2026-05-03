import java.io.DataOutput;
import java.io.DataOutputStream;

public class UserObject {
    private String username;
    private DataOutputStream datastream;

    public UserObject(String username, DataOutputStream datastream) {
        this.username = username;
        this.datastream = datastream;
    }

    public UserObject() {
        this.username = null;
        this.datastream = null;
    }

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public DataOutputStream getDataStream() {return datastream;}
    public void setDataStream(DataOutputStream datastream) {this.datastream = datastream;}
}
