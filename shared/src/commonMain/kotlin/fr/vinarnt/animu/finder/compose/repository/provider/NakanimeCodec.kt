package fr.vinarnt.animu.finder.compose.repository.provider

/**
 * Decodes Nakanime's XOR-obfuscated API responses.
 *
 * The site does not use real encryption: the 32-byte key is derived from the
 * request path (query string included) prefixed with `"nkapiv1"`, and the response
 * body is XOR-ed with that repeating key. Both `/api/catalog/search` and
 * `/api/sources/anime` use this scheme.
 */
internal object NakanimeCodec {

    private const val PREFIX = "nkapiv1"

    fun key(path: String): ByteArray {
        val source = PREFIX + path
        val key = ByteArray(32)
        for (v in 0 until 32) {
            var g = 0
            for (q in source.indices) {
                g = (g * 31 + source[q].code + v) and 0xFF
            }
            key[v] = g.toByte()
        }
        return key
    }

    fun decode(bytes: ByteArray, path: String): String {
        val key = key(path)
        val out = ByteArray(bytes.size)
        for (i in bytes.indices) {
            out[i] = (bytes[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
        return out.decodeToString()
    }
}
