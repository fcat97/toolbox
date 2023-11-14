/*
MIT License

Copyright (c) [2023] [Shahriar Zaman]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package com.quran_library.utils.fuzzy_search

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.util.zip.ZipFile

/**
 * Extract a zip file into any directory
 *
 * @param zipFile src zip file
 * @param extractTo directory to extract into.
 * There will be new folder with the zip's name inside [extractTo] directory.
 * @param extractHere no extra folder will be created and will be extracted
 * directly inside [extractTo] folder.
 *
 * @return the extracted directory i.e, [extractTo] folder if [extractHere] is `true`
 * and [extractTo]\zipFile\ folder otherwise.
 */
fun extractZipFile(
    zipFile: File,
    extractTo: File,
    extractHere: Boolean = false,
): File? {
    return try {
        val outputDir = if (extractHere) {
            extractTo
        } else {
            File(extractTo, zipFile.nameWithoutExtension)
        }

        val zip = ZipFile(zipFile)
        zip.entries().asSequence().forEach { entry ->
            zip.getInputStream(entry).use { input ->
                if (entry.isDirectory) {
                    val d = File(outputDir, entry.name)
                    if (!d.exists()) d.mkdirs()
                } else {
                    val f = File(outputDir, entry.name)
                    if (f.parentFile?.exists() != true)  f.parentFile?.mkdirs()

                    f.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
        zip.close()

        extractTo
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}