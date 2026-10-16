package com.example.hensley_weighttracker.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = {"username"}, unique = true)})
public class UserEntity {
    @PrimaryKey(autoGenerate = true) public int id;
    @NonNull public String username = "";
    @NonNull @ColumnInfo(name = "password_hash") public String passwordHash = "";
    @NonNull @ColumnInfo(name = "password_salt") public String passwordSalt = "";
}
