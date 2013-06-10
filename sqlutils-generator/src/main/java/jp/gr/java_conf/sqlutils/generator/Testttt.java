package jp.gr.java_conf.sqlutils.generator;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

public class Testttt {


	public static void main(String[] args) throws Exception {
        Customer customer = new Customer();
        Address address = new Address();
        address.setStreet("1 A Street");
        customer.setContactInfo(address);

        JAXBContext jc = JAXBContext.newInstance(Customer.class, Address.class, PhoneNumber.class);

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(customer, System.out);

        String test =
        		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
        		"<customer xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
        		"    <contactInfo " +
        		"      " +
        		"     xsi:type=\"phoneNumber\">" +
        		"        <street>1 A Street</street>" +
        		"    </contactInfo>" +
        		"</customer>" +
        		"" +
        		"";
        Unmarshaller unMarshaller = jc.createUnmarshaller();
        Customer ret = (Customer) unMarshaller.unmarshal(new StringReader(test));
        System.out.println(ret.contactInfo.getClass());

	}

	public abstract static class ContactInfo {
	}
	public static class PhoneNumber extends ContactInfo {
	}
	public static class Address extends ContactInfo {

	    private String street;

	    public String getStreet() {
	        return street;
	    }

	    public void setStreet(String street) {
	        this.street = street;
	    }
	}

	@XmlRootElement
	public static class Customer {

	    private ContactInfo contactInfo;

	    public ContactInfo getContactInfo() {
	        return contactInfo;
	    }

	    public void setContactInfo(ContactInfo contactInfo) {
	        this.contactInfo = contactInfo;
	    }

	}

}
