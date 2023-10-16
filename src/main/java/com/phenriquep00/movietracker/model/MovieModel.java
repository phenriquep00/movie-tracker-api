package com.phenriquep00.movietracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity(name = "tb_movies")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieModel
{

        @Id
        private String imdb_id;

        private String title;

        private String year;

        @Column(length = 2000)
        private String description;

        private int movieLength;

        private long rating;

        @Column(length = 400)
        private String trailer;

        @Column(length = 800)
        private String imageUrl;

        @Column(length = 800)
        private String banner;

        @Column(length = 800)
        private String plot;

        @CreationTimestamp
        private LocalDateTime addedAt;

        @JsonProperty("movie_length")
        public void setMovieLength(int duration) {
                this.movieLength = duration;
        }

        @JsonProperty("image_url")
        public void setImageUrl(String imageUrl) {
                this.imageUrl = imageUrl;
        }
}
