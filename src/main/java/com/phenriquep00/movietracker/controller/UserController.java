package com.phenriquep00.movietracker.controller;

import com.phenriquep00.movietracker.model.UserModel;
import com.phenriquep00.movietracker.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import at.favre.lib.crypto.bcrypt.BCrypt;

@RestController
@RequestMapping("/user")
public class UserController 
{
    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody UserModel userModel)
    {
        if(this.userRepository.findByUsername(userModel.getUsername()) != null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
        };

        userModel.setPassword(BCrypt.withDefaults().hashToString(12, userModel.getPassword().toCharArray()));

        return ResponseEntity.status(HttpStatus.CREATED).body(this.userRepository.save(userModel));
    }

    @GetMapping("/{username}")
    public ResponseEntity getById(@PathVariable String username)
    {
        System.out.println(username);
        UserModel userModel = this.userRepository.findByUsername(username);

        if(userModel == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }
}
