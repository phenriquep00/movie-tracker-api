package com.phenriquep00.movietracker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.phenriquep00.movietracker.model.MovieModel;
import com.phenriquep00.movietracker.model.UserMovieModel;
import com.phenriquep00.movietracker.repository.IMovieRepository;
import com.phenriquep00.movietracker.repository.IUserMovieRepository;
import com.phenriquep00.movietracker.utils.MovieApiResponse;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/movie")
public class MovieController 
{

    @Value("${spring.application.x_rapid_api_key}")
    private String apiKey;
    @Value("${spring.application.x_rapid_api_host}")
    private String apiHost;

    @Autowired
    private IMovieRepository movieRepository;

    @Autowired
    private IUserMovieRepository userMovieRepository;

    @PostMapping("/{movieTitle}")
    public ResponseEntity create(@PathVariable String movieTitle, HttpServletRequest request)
    {
        MovieModel movieModel = this.movieRepository.findByTitle(movieTitle);
        UserMovieModel userMovieModel = new UserMovieModel();

        // if the movie is not in the db: register it
        if (movieModel == null) 
        {
            // Movie not registered, proceed to request for it on MoviesMiniDatabase API

            // Step 1 -> Retrieve imdb_id by given movie title
            String imdbId = this.getImdbId(movieTitle);

            if (imdbId == null) 
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Movie not found");
            }

            // Step 2 -> Retrieve movie data by given imdb_id
            HttpResponse<String> results = this.getMovieData(imdbId);

            if (results == null) 
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Movie data could not be retrieved");
            }

            // Step 3 -> Save movie data to the database
            MovieModel newMovie = this.saveMovieFromJson(results.getBody());

            if (newMovie == null) 
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Movie wasn't saved in the database");
            }

            // Step 4 -> Conect user and movie information

            userMovieModel.setUserId((UUID) request.getAttribute("userId"));

            userMovieModel.setMovieImdbId(newMovie.getImdb_id());

            userMovieModel.setUserRating(0);

            userMovieRepository.save(userMovieModel);

            return ResponseEntity.status(HttpStatus.CREATED).body(newMovie);
        }

        // if the movie is already in the db: return it
        return ResponseEntity.status(HttpStatus.CREATED).body(movieModel);
    }

    private String getImdbId(String movieTitle) 
    {
        try 
        {

            String urlGetByTitle = "https://moviesminidatabase.p.rapidapi.com/movie/imdb_id/byTitle/" + movieTitle;

            HttpResponse<String> response = Unirest
                    .get(urlGetByTitle)
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", apiHost)
                    .header("Content-Type", "application/json")
                    .asString();

            if (response == null) 
            {
                return null;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            String imdbId = jsonNode.get("results").get(0).get("imdb_id").asText();

            return imdbId;

        } 
        catch (UnirestException e) 
        {
            e.printStackTrace();
        } 
        catch (JsonProcessingException e) 
        {
            throw new RuntimeException(e);
        }

        return null;

    }

    private HttpResponse<String> getMovieData(String imdbId) {
        try
        {
            String urlGetByImdbId = "https://moviesminidatabase.p.rapidapi.com/movie/id/" + imdbId + "/";

            return Unirest
                    .get(urlGetByImdbId)
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", apiHost)
                    .header("Content-Type", "application/json")
                    .asString();
        } 
        catch (UnirestException e) 
        {
            e.printStackTrace();
        }

        return null;
    }

    public MovieModel saveMovieFromJson(String json) 
    {
        try 
        {
            ObjectMapper objectMapper = new ObjectMapper();
            MovieApiResponse movieApiResponse = objectMapper.readValue(json, MovieApiResponse.class);

            MovieModel movieData = movieApiResponse.getResults();

            // Assuming you have a repository for MovieModel, save the movie to the database
            movieRepository.save(movieData);

            return movieData;

        } 
        catch (Exception e) 
        {
            e.printStackTrace(); // Handle any exceptions (e.g., JSON parsing errors)
        }
        return null;
    }

}
