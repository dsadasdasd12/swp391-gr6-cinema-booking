/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package dto;

public class MovieAssignmentItem {

    private int movieId;
    private String title;
    private int durationMin;
    private String status;
    private boolean assigned;

    public MovieAssignmentItem() {
    }

    public MovieAssignmentItem(int movieId, String title,
            int durationMin, String status, boolean assigned) {
        this.movieId = movieId;
        this.title = title;
        this.durationMin = durationMin;
        this.status = status;
        this.assigned = assigned;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(int durationMin) {
        this.durationMin = durationMin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public String getDurationLabel() {
        int hours = durationMin / 60;
        int minutes = durationMin % 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }

        return minutes + "m";
    }

    public String getStatusLabel() {
        if (status == null) {
            return "";
        }

        switch (status) {
            case "COMING_SOON":
                return "Sắp chiếu";
            case "NOW_SHOWING":
                return "Đang chiếu";
            case "ENDED":
                return "Đã chiếu";
            default:
                return status;
        }
    }
}
