package com.example.hensley_weighttracker.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.hensley_weighttracker.data.entity.WeightEntity;
import java.util.List;

@Dao
public interface WeightDao {
    @Insert long insert(WeightEntity entry);

    @Query("UPDATE weights SET weight_date = :date, weight_value = :pounds " +
            "WHERE id = :id AND user_id = :userId")
    int update(int id, int userId, String date, double pounds);

    @Query("DELETE FROM weights WHERE id = :id AND user_id = :userId")
    int delete(int id, int userId);

    @Query("SELECT * FROM weights WHERE id = :id AND user_id = :userId")
    WeightEntity find(int id, int userId);

    @Query("SELECT * FROM weights WHERE user_id = :userId ORDER BY weight_date ASC, id ASC")
    List<WeightEntity> history(int userId);

    @Query("SELECT * FROM weights WHERE user_id = :userId " +
            "AND weight_date >= :start AND weight_date <= :end ORDER BY weight_date ASC, id ASC")
    List<WeightEntity> historyBetween(int userId, String start, String end);
}
