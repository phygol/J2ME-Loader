package com.satse.jarj2me.data

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.util.jar.JarFile
import java.util.jar.Manifest

object JarParser {
    private const val TAG = "JarParser"

    data class MidletInfo(
        val name: String = "",
        val className: String = "",
        val iconPath: String = "",
        val vendor: String = "",
        val version: String = "",
        val description: String = "",
        val permissions: List<String> = emptyList()
    )

    fun parseJar(context: Context, file: File): MidletInfo? {
        return try {
            val jarFile = JarFile(file)
            val manifest = jarFile.manifest
            jarFile.close()
            parseManifest(manifest)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JAR: ${file.name}", e)
            null
        }
    }

    fun parseJad(file: File): MidletInfo? {
        return try {
            val lines = file.readText().split("\n")
            val props = mutableMapOf<String, String>()
            for (line in lines) {
                val idx = line.indexOf(':')
                if (idx > 0) {
                    val key = line.substring(0, idx).trim()
                    val value = line.substring(idx + 1).trim()
                    props[key] = value
                }
            }
            MidletInfo(
                name = props["MIDlet-Name"] ?: "",
                vendor = props["MIDlet-Vendor"] ?: "",
                version = props["MIDlet-Version"] ?: "",
                description = props["MIDlet-Description"] ?: "",
                iconPath = props["MIDlet-Icon"] ?: "",
                className = props["MIDlet-1"]?.split(",")?.getOrNull(1)?.trim() ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JAD: ${file.name}", e)
            null
        }
    }

    private fun parseManifest(manifest: Manifest?): MidletInfo? {
        if (manifest == null) return null
        val attrs = manifest.mainAttributes
        return MidletInfo(
            name = attrs.getValue("MIDlet-Name") ?: "",
            vendor = attrs.getValue("MIDlet-Vendor") ?: "",
            version = attrs.getValue("MIDlet-Version") ?: "",
            description = attrs.getValue("MIDlet-Description") ?: "",
            iconPath = attrs.getValue("MIDlet-Icon") ?: "",
            className = attrs.getValue("MIDlet-1")?.split(",")?.getOrNull(1)?.trim() ?: ""
        )
    }

    fun extractIcon(jarFile: File, iconPath: String): ByteArray? {
        if (iconPath.isBlank()) return null
        return try {
            val jar = JarFile(jarFile)
            val entry = jar.getEntry(iconPath)
            if (entry != null) {
                val input = jar.getInputStream(entry)
                input.readAllBytes()
            } else null
        } catch (e: Exception) {
            null
        }
    }
}