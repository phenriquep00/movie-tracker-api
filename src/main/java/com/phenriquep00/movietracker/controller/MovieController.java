package com.phenriquep00.movietracker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.phenriquep00.movietracker.model.MovieModel;
import com.phenriquep00.movietracker.repository.IMovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/movie")
public class MovieController
{
    @Autowired
    private IMovieRepository movieRepository;

    @PostMapping("/{movieTitle}")
    public ResponseEntity create(@PathVariable String movieTitle) {

        if (this.movieRepository.findByTitle(movieTitle) == null) {
            System.out.println("Movie not registered, proceed to request for it on MoviesMiniDatabase API");

            // Step 1 -> Retrieve imdb_id by given movie title
            String imdbId = this.getImdbId(movieTitle);

            // Step 2 -> Retrieve movie data by given imdb_id
            HttpResponse<String> results =  this.getMovieData(imdbId);

            // Step 3 -> Save movie data in the database

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(results.getBody());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Movie registered successfully");
    }

    private String getImdbId(String movieTitle)
    {
        try {

            String urlGetByTitle = "https://moviesminidatabase.p.rapidapi.com/movie/imdb_id/byTitle/" + movieTitle;

            System.out.println(urlGetByTitle);

            HttpResponse<String> response = Unirest
                    .get(urlGetByTitle)
                    .header("X-RapidAPI-Key", "e86aa47846msh08cfcd98266767ep14765ajsn3734f94f6bab")
                    .header("X-RapidAPI-Host", "moviesminidatabase.p.rapidapi.com")
                    .header("Content-Type", "application/json")
                    .asString();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            String imdbId = jsonNode.get("results").get(0).get("imdb_id").asText();
            System.out.println("imdb_id: " + imdbId);

            return imdbId;

        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }

        return null;

    }

    private HttpResponse<String> getMovieData(String imdbId) {
        try
        {
            String urlGetByImdbId = "https://moviesminidatabase.p.rapidapi.com/movie/id/" + imdbId + "/";

            System.out.println(urlGetByImdbId);

            HttpResponse<String> response = Unirest
                    .get(urlGetByImdbId)
                    .header("X-RapidAPI-Key", "e86aa47846msh08cfcd98266767ep14765ajsn3734f94f6bab")
                    .header("X-RapidAPI-Host", "moviesminidatabase.p.rapidapi.com")
                    .header("Content-Type", "application/json")
                    .asString();

            return response;
        }
        catch (UnirestException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
