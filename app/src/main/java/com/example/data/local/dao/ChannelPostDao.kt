package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.ChannelPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelPostDao {
    @Query("SELECT * FROM channel_posts ORDER BY timestamp DESC")
    fun getAllChannelPostsFlow(): Flow<List<ChannelPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<ChannelPostEntity>)
}
