import javafx.beans.property.SimpleStringProperty;

public class Company {
    private String companyId;
    private String companyName;
    private String hrName;
    private String companyAddress;
    private String contactInfo;

    private final SimpleStringProperty companyIdProp = new SimpleStringProperty();
    private final SimpleStringProperty companyNameProp = new SimpleStringProperty();
    private final SimpleStringProperty hrNameProp = new SimpleStringProperty();
    private final SimpleStringProperty companyAddressProp = new SimpleStringProperty();
    private final SimpleStringProperty contactInfoProp = new SimpleStringProperty();

    public Company(String companyId, String companyName, String hrName, String companyAddress, String contactInfo) {
        setCompanyId(companyId);
        setCompanyName(companyName);
        setHrName(hrName);
        setCompanyAddress(companyAddress);
        setContactInfo(contactInfo);
    }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String value) { this.companyId = value; this.companyIdProp.set(value); }
    public SimpleStringProperty companyIdProperty() { return companyIdProp; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String value) { this.companyName = value; this.companyNameProp.set(value); }
    public SimpleStringProperty companyNameProperty() { return companyNameProp; }

    public String getHrName() { return hrName; }
    public void setHrName(String value) { this.hrName = value; this.hrNameProp.set(value); }
    public SimpleStringProperty hrNameProperty() { return hrNameProp; }

    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String value) { this.companyAddress = value; this.companyAddressProp.set(value); }
    public SimpleStringProperty companyAddressProperty() { return companyAddressProp; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String value) { this.contactInfo = value; this.contactInfoProp.set(value); }
    public SimpleStringProperty contactInfoProperty() { return contactInfoProp; }
}
