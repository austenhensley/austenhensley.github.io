package com.example.hensley_weighttracker.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "weights", foreignKeys = @ForeignKey(entity = UserEntity.class, parentColumns = "id",
        childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = {"user_id", "weight_date"})})
public class WeightEntity {
    @PrimaryKey(autoGenerate = true) public int id;
    @ColumnInfo(name = "user_id") public int userId;
    @NonNull @ColumnInfo(name = "weight_date") public String date = "";
    @ColumnInfo(name = "weight_value") public double pounds;
}
