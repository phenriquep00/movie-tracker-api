package com.phenriquep00.movietracker.repository;

import com.phenriquep00.movietracker.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IUserRepository extends JpaRepository<UserModel, UUID>
{
    UserModel findByUsername(String username);
}