package com.satse.jarj2me.data

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameRepository(private val gameDao: GameDao) {

    suspend fun addGame(game: GameEntity) = gameDao.insert(game)

    suspend fun updateGame(game: GameEntity) = gameDao.update(game)

    suspend fun deleteGame(game: GameEntity) = gameDao.delete(game)

    suspend fun deleteGameById(id: Long) = gameDao.deleteById(id)

    suspend fun findByPath(path: String) = gameDao.findByPath(path)

    fun getAllGames(): LiveData<androidx.paging.PagingData<GameEntity>> {
        return Pager(PagingConfig(pageSize = 20)) {
            gameDao.getAllGames()
        }.liveData
    }

    suspend fun getRecentGames(limit: Int = 10) = gameDao.getRecentGames(limit)

    suspend fun scanAndAddGame(file: java.io.File): GameEntity? {
        val existing = findByPath(file.absolutePath)
        if (existing != null) {
            return existing.copy(lastPlayed = System.currentTimeMillis()).also { updateGame(it) }
        }

        val midletInfo = JarParser.parseJar(J2MEApplication.instance, file)
            ?: JarParser.parseJad(file)
            ?: return null

        val game = GameEntity(
            filePath = file.absolutePath,
            fileName = file.name,
            title = midletInfo.name.ifBlank { file.nameWithoutExtension },
            vendor = midletInfo.vendor,
            version = midletInfo.version,
            description = midletInfo.description,
            iconPath = midletInfo.iconPath,
            dateAdded = System.currentTimeMillis()
        )
        val id = gameDao.insert(game)
        game.copy(id = id)
    }
}

class GameRepositoryProvider(private val application: Application) {
    val repository: GameRepository by lazy {
        GameRepository(J2MEApplication.database.gameDao())
    }
}