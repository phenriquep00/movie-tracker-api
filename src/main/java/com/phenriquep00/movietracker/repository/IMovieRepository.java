package com.phenriquep00.movietracker.repository;

import com.phenriquep00.movietracker.model.MovieModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IMovieRepository extends JpaRepository<MovieModel, String>
{
    MovieModel findByTitle(String title);
}
