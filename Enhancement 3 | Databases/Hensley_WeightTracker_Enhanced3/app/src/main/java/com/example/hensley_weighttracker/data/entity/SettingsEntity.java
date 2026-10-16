package com.example.hensley_weighttracker.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "settings", foreignKeys = @ForeignKey(entity = UserEntity.class, parentColumns = "id",
        childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = {"user_id"}, unique = true)})
public class SettingsEntity {
    @PrimaryKey(autoGenerate = true) public int id;
    @ColumnInfo(name = "user_id") public int userId;
    @ColumnInfo(name = "phone_number") public String phoneNumber;
    @ColumnInfo(name = "sms_enabled", defaultValue = "0") public boolean smsEnabled;
    @NonNull @ColumnInfo(name = "weight_unit", defaultValue = "'LB'") public String weightUnit = "LB";
}
