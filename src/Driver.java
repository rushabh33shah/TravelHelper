
import java.nio.file.Paths;

import java_project.HotelData;
import java_project.touristAttraction;

public class Driver {
    public static void main(String[] args) {
        HotelData data = new HotelData();
        touristAttraction ta = new touristAttraction();
        ta.fetch_attraction(10323, 2);

        // Load hotel info from hotels200.json

        //===================================DONT RUN LOAD HOTEL OR LOAD REVIEWS
        //data.loadHotelInfo("input/hotels200.json");
        //   data.loadReviews(Paths.get("input/reviews"));
        //==============================================

        // Traverse input/reviews directory recursively,
        // find all the json files and load reviews
        //	data.printToFile(Paths.get("outputFile"));

    }
}
