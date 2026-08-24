package fr.vinarnt.animu.finder.compose.repository.provider

import kotlin.math.pow

/**
 * Ports of the self-contained extraction helpers used by the AnimePahe provider:
 * the classic JavaScript p.a.c.k.e.r unpacker and the Kwik HLS cipher.
 */
internal object KwikPacker {

    private val ALPHA62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val ALPHA95 =
        " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"

    fun substringBefore(str: String, pattern: String): String {
        val idx = str.indexOf(pattern)
        return if (idx == -1) str else str.substring(0, idx)
    }

    fun substringAfter(str: String, pattern: String): String {
        val idx = str.indexOf(pattern)
        return if (idx == -1) str else str.substring(idx + pattern.length)
    }

    fun substringAfterLast(str: String, pattern: String): String =
        str.split(pattern).lastOrNull() ?: ""

    fun decrypt(packedStr: String, key: String, offsetStr: String, delimiterIndex: Int): String {
        val offset = offsetStr.toIntOrNull() ?: throw IllegalArgumentException("Invalid offset")
        val delimiter = key.getOrNull(delimiterIndex) ?: throw IllegalArgumentException("Invalid delimiter index")
        val radix = delimiterIndex

        val sb = StringBuilder()
        var i = 0
        while (i < packedStr.length) {
            val chunk = StringBuilder()
            while (i < packedStr.length && packedStr[i] != delimiter) {
                chunk.append(packedStr[i])
                i++
            }
            var chunkWithDigits = chunk.toString()
            for (j in key.indices) {
                chunkWithDigits = chunkWithDigits.replace(key[j].toString(), j.toString())
            }
            val numericValue = chunkWithDigits.toIntOrNull(radix) ?: 0
            sb.append((numericValue - offset).toChar())
            i++
        }
        return sb.toString()
    }

    fun unpack(packedCode: String): String? {
        if (!Regex("""eval\(function\(p,a,c,k,e,(?:r|d)""").containsMatchIn(packedCode.replace(" ", ""))) {
            return null
        }

        val exp = Regex(
            """\}\s*\('(.*)',\s*(.*?),\s*(\d+),\s*'(.*?)'\.split\('\|'\)""",
            RegexOption.DOT_MATCHES_ALL,
        )
        val matches = exp.find(packedCode) ?: return null
        if (matches.groupValues.size != 5) return null

        val payload = matches.groupValues[1].replace("\\'", "'")
        val radix = matches.groupValues[2].toIntOrNull() ?: 36
        val count = matches.groupValues[3].toIntOrNull() ?: 0
        val symArray = matches.groupValues[4].split("|")
        if (symArray.size != count) return null

        val wordRegex = Regex("""\b\w+\b""")
        return wordRegex.replace(payload) { match ->
            val word = match.value
            val index = unBase(word, radix)
            if (index in symArray.indices && symArray[index].isNotEmpty()) symArray[index] else word
        }
    }

    private fun unBase(str: String, radix: Int): Int {
        val alphabet = alphabetFor(radix)
        if (alphabet.isEmpty()) {
            return str.toIntOrNull(radix) ?: -1
        }
        var ret = 0.0
        for (i in str.indices) {
            val ch = str[str.length - 1 - i]
            val value = alphabet.indexOf(ch)
            if (value >= 0) {
                ret += radix.toDouble().pow(i.toDouble()) * value
            }
        }
        return ret.toInt()
    }

    private fun alphabetFor(radix: Int): String = when {
        radix <= 36 -> ""
        radix < 62 -> ALPHA62.substring(0, radix)
        radix == 62 -> ALPHA62
        radix < 95 -> ALPHA95.substring(0, radix)
        radix == 95 -> ALPHA95
        else -> ""
    }
}
