package com.phenriquep00.movietracker.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.phenriquep00.movietracker.model.UserMovieModel;

public interface IUserMovieRepository extends JpaRepository<UserMovieModel, UUID>
{
    
}
