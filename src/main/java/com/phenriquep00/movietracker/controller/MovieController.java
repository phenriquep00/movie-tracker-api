package com.phenriquep00.movietracker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.phenriquep00.movietracker.model.MovieModel;
import com.phenriquep00.movietracker.repository.IMovieRepository;
import com.phenriquep00.movietracker.utils.MovieApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.env.Environment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/movie")
public class MovieController
{
    private Environment environment;

    @Autowired
    private IMovieRepository movieRepository;

    @PostMapping("/{movieTitle}")
    public ResponseEntity create(@PathVariable String movieTitle) {


        MovieModel movieModel = this.movieRepository.findByTitle(movieTitle);

        // if the movie is not in the db: register it
        if (movieModel == null) {
            System.out.println("Movie not registered, proceed to request for it on MoviesMiniDatabase API");

            // Step 1 -> Retrieve imdb_id by given movie title
            String imdbId = this.getImdbId(movieTitle);

            if(imdbId == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Movie not found");
            }

            // Step 2 -> Retrieve movie data by given imdb_id
            HttpResponse<String> results =  this.getMovieData(imdbId);

            if(results == null)
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Movie data could not be retrieved");
            }

            // TODO: Step 3 -> Save movie data in the database
            MovieModel NewMovie = this.saveMovieFromJson(results.getBody());

            if(NewMovie == null)
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Movie wasn't saved in the database");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(NewMovie);
        }

        // if the movie is already in the db: return it
        return ResponseEntity.status(HttpStatus.CREATED).body(movieModel);
    }

    private String getImdbId(String movieTitle)
    {
        try {

            String urlGetByTitle = "https://moviesminidatabase.p.rapidapi.com/movie/imdb_id/byTitle/" + movieTitle;

            HttpResponse<String> response = Unirest
                    .get(urlGetByTitle)
                    .header("X-RapidAPI-Key", environment.getProperty("MOVIES_API_KEY"))
                    .header("X-RapidAPI-Host", environment.getProperty("MOVIES_API_HOST"))
                    .header("Content-Type", "application/json")
                    .asString();

            if(response == null)
            {
                return null;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            String imdbId = jsonNode.get("results").get(0).get("imdb_id").asText();

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

            return Unirest
                    .get(urlGetByImdbId)
                    .header("X-RapidAPI-Key", "e86aa47846msh08cfcd98266767ep14765ajsn3734f94f6bab")
                    .header("X-RapidAPI-Host", "moviesminidatabase.p.rapidapi.com")
                    .header("Content-Type", "application/json")
                    .asString();
        }
        catch (UnirestException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public MovieModel saveMovieFromJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            MovieApiResponse movieApiResponse = objectMapper.readValue(json, MovieApiResponse.class);

            MovieModel movieData = movieApiResponse.getResults();

            // Assuming you have a repository for MovieModel, save the movie to the database
            movieRepository.save(movieData);

            return movieData;

        } catch (Exception e) {
            e.printStackTrace(); // Handle any exceptions (e.g., JSON parsing errors)
        }
        return null;
    }

}
