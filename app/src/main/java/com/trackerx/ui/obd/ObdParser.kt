package com.trackerx.ui.obd

/** Decodificação de respostas OBD2 modo 01/03 (funções puras, cobertas por testes). */
object ObdParser {

    /** Limpa a resposta do ELM327 e devolve os bytes após o cabeçalho "41 <pid>". */
    fun dataBytes(response: String, pid: Int): IntArray? {
        val hex = response.uppercase().filter { it in "0123456789ABCDEF" }
        val header = "41" + "%02X".format(pid)
        val idx = hex.indexOf(header)
        if (idx < 0) return null
        val payload = hex.substring(idx + header.length)
        val out = ArrayList<Int>()
        var i = 0
        while (i + 1 < payload.length) {
            out.add(payload.substring(i, i + 2).toInt(16))
            i += 2
        }
        return out.toIntArray()
    }

    fun speedKmh(r: String): Double? = dataBytes(r, 0x0D)?.getOrNull(0)?.toDouble()

    fun rpm(r: String): Double? = dataBytes(r, 0x0C)?.let {
        if (it.size < 2) null else (it[0] * 256 + it[1]) / 4.0
    }

    fun coolantC(r: String): Double? = dataBytes(r, 0x05)?.getOrNull(0)?.let { it - 40.0 }
    fun intakeC(r: String): Double? = dataBytes(r, 0x0F)?.getOrNull(0)?.let { it - 40.0 }
    fun engineLoadPct(r: String): Double? = dataBytes(r, 0x04)?.getOrNull(0)?.let { it * 100.0 / 255.0 }
    fun throttlePct(r: String): Double? = dataBytes(r, 0x11)?.getOrNull(0)?.let { it * 100.0 / 255.0 }
    fun fuelPct(r: String): Double? = dataBytes(r, 0x2F)?.getOrNull(0)?.let { it * 100.0 / 255.0 }
    fun manifoldKpa(r: String): Double? = dataBytes(r, 0x0B)?.getOrNull(0)?.toDouble()

    /** Resposta do comando ATRV, ex.: "12.6V". */
    fun voltage(r: String): Double? =
        Regex("(\\d{1,2}\\.\\d)").find(r)?.groupValues?.get(1)?.toDoubleOrNull()

    /** Modo 03: "43 01 33 00 00 00 00" -> ["P0133"]. */
    fun dtcs(response: String): List<String>? {
        val hex = response.uppercase().filter { it in "0123456789ABCDEF" }
        val idx = hex.indexOf("43")
        if (idx < 0) return null
        val payload = hex.substring(idx + 2)
        val out = ArrayList<String>()
        var i = 0
        while (i + 3 < payload.length) {
            val a = payload.substring(i, i + 2).toInt(16)
            val b = payload.substring(i + 2, i + 4).toInt(16)
            i += 4
            if (a == 0 && b == 0) continue
            val letter = "PCBU"[a shr 6]
            out.add("%c%d%X%02X".format(letter, (a shr 4) and 0x3, a and 0xF, b))
        }
        return out
    }
}
