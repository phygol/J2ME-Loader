package com.satse.jarj2me.data

import android.content.Context
import androidx.room.*

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val title: String = "",
    val vendor: String = "",
    val version: String = "",
    val description: String = "",
    val iconPath: String = "",
    val screenWidth: Int = 240,
    val screenHeight: Int = 320,
    val lastPlayed: Long = 0,
    val playCount: Int = 0,
    val dateAdded: Long = System.currentTimeMillis()
)

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: GameEntity): Long

    @Update
    suspend fun update(game: GameEntity)

    @Delete
    suspend fun delete(game: GameEntity)

    @Query("SELECT * FROM games ORDER BY lastPlayed DESC")
    suspend fun getAllGames(): List<GameEntity>

    @Query("SELECT * FROM games WHERE filePath = :path")
    suspend fun findByPath(path: String): GameEntity?

    @Query("SELECT * FROM games ORDER BY dateAdded DESC LIMIT :limit")
    suspend fun getRecentGames(limit: Int): List<GameEntity>

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Database(entities = [GameEntity::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "j2me_games.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}