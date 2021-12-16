package miio;

//loads file or defaults
public class Defaults {
    private String model;
    private String token;
    private String did;

    public String getModel() {
        return model != null ? model : EmulatorMain.DEFAULT_DEVICE.getModel();
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getToken() {
        return token != null ? token : "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDid() {
        return did != null ? did : "AABBCCDD";
    }

    public void setDid(String did) {
        this.did = did;
    }

    @Override
    public String toString() {
        return "ClassPojo [model = " + model + ", token = " + token + ", did = " + did + "]";
    }
}
