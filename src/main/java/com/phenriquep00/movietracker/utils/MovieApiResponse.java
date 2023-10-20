package com.phenriquep00.movietracker.utils;

import com.phenriquep00.movietracker.model.MovieModel;
import lombok.Data;

public class MovieApiResponse 
{
    private MovieModel results;

    public MovieModel getResults() 
    {
        return results;
    }

    public void setResults(MovieModel results) 
    {
        this.results = results;
    }
}
