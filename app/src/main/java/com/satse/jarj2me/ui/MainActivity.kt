package com.satse.jarj2me.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.satse.jarj2me.R
import com.satse.jarj2me.data.GameEntity
import com.satse.jarj2me.data.GameRepositoryProvider
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: GameAdapter
    private val repository by lazy { GameRepositoryProvider(application).repository }
    private val PICK_FILE_REQUEST = 1001
    private val MANAGE_STORAGE_REQUEST = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbar()
        setupRecyclerView()
        observeGames()
        checkStoragePermission()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupRecyclerView() {
        adapter = GameAdapter(
            onClick = { game -> launchGame(game) },
            onLongClick = { game -> showGameOptions(game) }
        )
        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.gameList)
        rv.layoutManager = GridLayoutManager(this, 2)
        rv.adapter = adapter
    }

    private fun observeGames() {
        lifecycleScope.launch {
            repository.getAllGames().observe(this@MainActivity) { games ->
                adapter.submitList(games)
            }
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    .setData(Uri.parse("package:$packageName"))
                startActivityForResult(intent, MANAGE_STORAGE_REQUEST)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                importGameFromUri(uri)
            }
        }
    }

    private fun importGameFromUri(uri: Uri) {
        lifecycleScope.launch {
            try {
                contentResolver.openInputStream(uri)?.use { input ->
                    val tempFile = File(cacheDir, "import_${System.currentTimeMillis()}.jar")
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                    val game = repository.scanAndAddGame(tempFile)
                    if (game != null) {
                        runOnUiThread {
                            Snackbar.make(findViewById(android.R.id.content), 
                                "已导入: ${game.title}", Snackbar.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                            Snackbar.make(findViewById(android.R.id.content), 
                                "导入失败: 无效的 JAR/JAD 文件", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Snackbar.make(findViewById(android.R.id.content), 
                        "导入出错: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun launchGame(game: GameEntity) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("game_path", game.filePath)
            putExtra("game_title", game.title)
        }
        startActivity(intent)
    }

    private fun showGameOptions(game: GameEntity) {
        MaterialAlertDialogBuilder(this)
            .setTitle(game.title)
            .setItems(arrayOf("删除", "重命名", "详情")) { _, which ->
                when (which) {
                    0 -> deleteGame(game)
                    1 -> renameGame(game)
                    2 -> showGameDetails(game)
                }
            }
            .show()
    }

    private fun deleteGame(game: GameEntity) {
        MaterialAlertDialogBuilder(this)
            .setTitle("删除游戏")
            .setMessage("确定要删除「${game.title}」吗？")
            .setPositiveButton("删除") { _, _ ->
                lifecycleScope.launch {
                    repository.deleteGameById(game.id)
                    runOnUiThread {
                        Snackbar.make(findViewById(android.R.id.content), 
                            "已删除", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun renameGame(game: GameEntity) {
        // TODO: Show input dialog
    }

    private fun showGameDetails(game: GameEntity) {
        MaterialAlertDialogBuilder(this)
            .setTitle(game.title)
            .setMessage("""
                厂商: ${game.vendor.ifBlank{"未知"}}
                版本: ${game.version.ifBlank{"未知"}}
                分辨率: ${game.screenWidth}x${game.screenHeight}
                游玩次数: ${game.playCount}
                添加时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date(game.dateAdded))}
                文件: ${game.fileName}
            """.trimIndent())
            .setPositiveButton("确定", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_import -> {
                pickFile()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/java-archive"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/java-archive",
                "application/x-java-archive",
                "text/vnd.sun.j2me.app-descriptor"
            ))
        }
        startActivityForResult(intent, PICK_FILE_REQUEST)
    }
}