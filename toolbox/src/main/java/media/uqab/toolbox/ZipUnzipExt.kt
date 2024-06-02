package com.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object FileExt {
    fun File.zip(): File {
        val zipFile = File(this.parent, this.nameWithoutExtension + ".zip")
        val fos = FileOutputStream(zipFile)
        val zipOut = ZipOutputStream(fos)

        val fis = FileInputStream(this)
        val zipEntry = ZipEntry(this.name)
        zipOut.putNextEntry(zipEntry)

        val bytes = ByteArray(1024)
        var length: Int
        while (fis.read(bytes).also { length = it } >= 0) {
            zipOut.write(bytes, 0, length)
        }

        zipOut.close()
        fis.close()
        fos.close()

        return File(this.parent, this.nameWithoutExtension + ".zip")
    }

    fun File.unZip(destinationPath: String): List<String> {
        val unzipped = mutableListOf<String>()
        val destDir = File(destinationPath)

        val buffer = ByteArray(1024)
        val zis = ZipInputStream(FileInputStream(this))
        var zipEntry = zis.nextEntry
        while (zipEntry != null) {
            val newFile = newFile(destDir, zipEntry)
            if (zipEntry.isDirectory) {
                if (!newFile.isDirectory && !newFile.mkdirs()) {
                    throw IOException("Failed to create directory $newFile")
                }
            } else {
                // fix for Windows-created archives
                val parent = newFile.parentFile
                if (parent != null) {
                    if (!parent.isDirectory && !parent.mkdirs()) {
                        throw IOException("Failed to create directory $parent")
                    }
                }

                // write file content
                val fos = FileOutputStream(newFile)
                var len: Int
                while (zis.read(buffer).also { len = it } > 0) {
                    fos.write(buffer, 0, len)
                }
                fos.close()
                unzipped.add(newFile.path)
            }
            zipEntry = zis.nextEntry
        }

        zis.closeEntry()
        zis.close()

        return unzipped
    }

    @Throws(IOException::class)
    private fun newFile(destinationDir: File, zipEntry: ZipEntry): File {
        val destFile = File(destinationDir, zipEntry.name)
        val destDirPath = destinationDir.canonicalPath
        val destFilePath = destFile.canonicalPath
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw IOException("Entry is outside of the target dir: " + zipEntry.name)
        }
        return destFile
    }
}
