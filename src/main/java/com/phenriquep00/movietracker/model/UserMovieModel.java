package com.phenriquep00.movietracker.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "tb_user_movies")
public class UserMovieModel 
{
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private UUID userId;
    
    private String movieImdbId;
    
    @Column
    private long userRating;
}
