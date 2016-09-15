package examples.csci567.project;

import java.io.Serializable;

/**
 * Created by Pinak on 08-04-2016.
 */
public class User implements Serializable{
    private String displayName;
    private String uId;
    private String provider;

    public User(){};

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getProvider()
    {
        return provider;
    }

    public String getuId() {
        return uId;
    }
}

