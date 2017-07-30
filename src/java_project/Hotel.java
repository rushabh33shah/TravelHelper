package java_project;

public class Hotel implements Comparable<Hotel> {

    private String hotelId;
    private String hotelName;
    Address Address;

    public Hotel(String hotelId, String hotelName, Address address) {

        super();
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.Address = address;

    }

    private String getHotelID() {
        return hotelId;
    }

    private void setHotelID(String hotelID) {
        this.hotelId = hotelID;
    }

    String getHotelName() {
        return hotelName;
    }

    private void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public Address getAddress() {
        return Address;
    }

    public void setAddress(Address address) {
        Address = address;
        Address.toString();
    }

    @Override
    public int compareTo(Hotel o) {
        // TODO Auto-generated method stub
        return this.hotelName.compareTo(o.hotelName);
    }

    @Override
    public String toString() {
        return (hotelName + ": " + hotelId + "\n" + Address.toString());
    }

}
