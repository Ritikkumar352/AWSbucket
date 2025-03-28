package com.VidAWS.bucket;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Video {
    @Id
    private int id;
    private String title;
    private String viewUrl;
    private String downloadURl;
}
