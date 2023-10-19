package media.uqab.toolbox

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.IOException

/**
 * copy all the contents of a folder to another.
 *
 * @param srcDir [Uri] of source directory
 * @param destRootDir parent directory of destination.
 */
private fun copyDir(context: Context, srcDir: Uri, destRootDir: File) {
    if (!DocumentFile.isDocumentUri(context, srcDir)) return

    if (!destRootDir.exists()) destRootDir.mkdirs()

    val srcDoc = DocumentFile.fromTreeUri(context, srcDir)
        ?: DocumentFile.fromSingleUri(context, srcDir)
        ?: return

    if (srcDoc.isDirectory) {
        val destDir = File(destRootDir, srcDoc.name ?: return)
        srcDoc.listFiles().forEach {
            copyDir(context, it.uri, destDir)
        }
    }

    if (srcDoc.isFile) {
        copyFile(context, srcDoc.uri, File(destRootDir, srcDoc.name ?: return))
    }
}

@Throws(IOException::class)
private fun copyFile(context: Context, pathFrom: Uri, pathTo: File) {
    context.contentResolver.openInputStream(pathFrom).use { `in` ->
        `in`?.copyTo(pathTo.outputStream())
    }
}