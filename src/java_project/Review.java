package java_project;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Review implements Comparable<Review> {
    private String reviewID;
    private String hotelId;
    private String reviewTitle;
    private String reviewText;
    private String userName;
    private String date;
    private int overAllRating;
    String reviewDate;

    public Review(String reviewID, String hotelId, String reviewTitle,
                  String reviewText, String userName, String date, int overAllRating) {
        super();
        this.reviewID = reviewID;
        this.hotelId = hotelId;
        this.reviewTitle = reviewTitle;
        this.reviewText = reviewText;
        this.userName = userName;
        this.date = date;
        this.overAllRating = overAllRating;

    }

    private String getReviewID() {
        return reviewID;
    }

    private void setReviewID(String reviewID) {
        this.reviewID = reviewID;
    }

    private String getHotelId() {
        return hotelId;
    }

    private void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    private String getReviewTitle() {
        return reviewTitle;
    }

    private void setReviewTitle(String reviewTitle) {
        this.reviewTitle = reviewTitle;
    }

    private String getReviewText() {
        return reviewText;
    }

    private void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    private String getUserName() {
        return userName;
    }

    private void setUserName(String userName) {
        this.userName = userName;
    }

    private String getDate() {
        return date;
    }

    private void setDate(String date) {
        this.date = date;
    }

    private int getOverAllRating() {
        return overAllRating;
    }

    private void setOverAllRating(int overAllRating) {
        this.overAllRating = overAllRating;
    }

    // TODO: Still not complete.
    @Override
    public int compareTo(Review o) {
        // TODO Auto-generated method stub

        if (this.date.compareTo(o.date) == 0) {
            if (this.userName.compareTo(o.getUserName()) == 0) {
                return this.reviewID.compareTo(o.getReviewID());
            } else {
                return this.userName.compareTo(o.getUserName());
            }
        } else {
            return this.date.compareTo(o.getDate());
        }
    }

    @Override
    public String toString() {

        return ("\n--------------------" + "\n" +"Review by " + userName + ": " + overAllRating + "\n" + reviewTitle + "\n" + reviewText);

    }

}