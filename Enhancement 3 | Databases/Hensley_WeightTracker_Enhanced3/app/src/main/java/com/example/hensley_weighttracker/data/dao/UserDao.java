package com.example.hensley_weighttracker.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.hensley_weighttracker.data.entity.UserEntity;

@Dao
public interface UserDao {
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    boolean usernameExists(String username);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    UserEntity findByUsername(String username);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(UserEntity user);
}
