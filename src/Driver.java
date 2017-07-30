
import java.nio.file.Paths;

import java_project.HotelData;
import java_project.touristAttraction;

public class Driver {
    public static void main(String[] args) {
        HotelData data = new HotelData();
        touristAttraction ta = new touristAttraction();
        ta.fetch_attraction(10323, 2);
    }
}
