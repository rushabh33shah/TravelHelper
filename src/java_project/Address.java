package java_project;

public class Address {

	private String streetaddress;
	private String city;
	private String state;
	private Double longitude;
	private Double latitude;
	
	public Address (String streetaddress, String city, String state, Double longitude, Double latitude){
		
		this.streetaddress = streetaddress;
		this.city = city;
		this.state = state;
		this.longitude = longitude;
		this.latitude = latitude;
	}

	private String getStreetaddress() {
		return streetaddress;
	}

	private void setStreetaddress(String streetaddress) {
		this.streetaddress = streetaddress;
	}

	private String getCity() {
		return city;
	}

	private void setCity(String city) {
		this.city = city;
	}

	private String getState() {
		return state;
	}

	private void setState(String state) {
		this.state = state;
	}

	private Double getLongitude() {
		return longitude;
	}

	private void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	private Double getLatitude() {
		return latitude;
	}

	private void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	@Override
	public String toString() {
		return (streetaddress+"\n"+city+", "+state );
		
	}
}
