package com.jobportal.backend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobDto {

    private String title;
    private String description;
    private String requirements;
    private Double salary;
    private String location;
    private String jobType;
    private int experience;
    private String position;
    private String website;

    // Method to check if any field is missing
    public boolean isMissingFields() {
        return title == null || title.isEmpty()
                || description == null || description.isEmpty()
                || requirements == null || requirements.isEmpty()
                || salary <0
                || location == null || location.isEmpty()
                || jobType == null || jobType.isEmpty()
                || experience<0
                || position == null || position.isEmpty()
                || website == null;
    }
}

