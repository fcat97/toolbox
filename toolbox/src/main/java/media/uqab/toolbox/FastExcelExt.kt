package media.uqab.toolbox

import media.uqab.funTime.formatAs
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import org.dhatim.fastexcel.reader.Row
import java.io.FileOutputStream
import java.math.BigDecimal
import java.util.*

const val FAST_EXCEL_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss:SSS a"

/**
 * Creates a [Workbook]
 *
 * @param fos [FileOutputStream] Output stream eventually holding the serialized workbook.
 * @param name Name of the application which generated this workbook.
 * @param version Version of the application.
 * @param block lambda with [Workbook] context.
 */
inline fun workbook(
    fos: FileOutputStream,
    name: String,
    version: String,
    block: Workbook.() -> Unit
): Workbook {
    with(Workbook(fos, name, version)) {
        block()
        this.finish()
        return this
    }
}

/**
 * Create [Worksheet] in a [Workbook]
 */
inline fun Workbook.sheet(
    name: String = "",
    block: Worksheet.() -> Unit
): Worksheet {
    with(newWorksheet(name)) {
        block()
        return this
    }
}

/**
 * Writes data to a cell in [Worksheet]
 */
fun Worksheet.writeCell(
    r: Int,
    c: Int,
    value: Any
) {
    when(value) {
        is String -> value(r, c, value)
        is Number -> value(r, c, value)
        is Boolean -> value(r, c, value)
        is Date -> value(r, c, value formatAs FAST_EXCEL_DATE_FORMAT)
        else -> throw IllegalArgumentException("data class is not supported")
    }
}

fun Row.readString(col: Int): String {
    val o = this.getCellAsString(col)
    return if (o.isPresent) o.get() else ""
}

fun Row.readNumber(cell: Int): BigDecimal {
    val o = this.getCellAsNumber(cell)
    return if (o.isPresent) o.get() else BigDecimal.ZERO
}

fun Row.readBoolean(cell: Int): Boolean {
    val o = this.getCellAsBoolean(cell)
    return if (o.isPresent) o.get() else false
}
