package com.example.hensley_weighttracker.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import com.example.hensley_weighttracker.data.entity.GoalEntity;

@Dao
public abstract class GoalDao {
    @Query("SELECT * FROM goals WHERE user_id = :userId")
    public abstract GoalEntity find(int userId);

    @Insert public abstract long insert(GoalEntity goal);

    @Query("UPDATE goals SET goal_weight = :pounds WHERE user_id = :userId")
    public abstract int update(int userId, double pounds);

    @Transaction
    public void save(int userId, double pounds) {
        if (update(userId, pounds) == 0) {
            GoalEntity goal = new GoalEntity();
            goal.userId = userId;
            goal.pounds = pounds;
            insert(goal);
        }
    }
}
