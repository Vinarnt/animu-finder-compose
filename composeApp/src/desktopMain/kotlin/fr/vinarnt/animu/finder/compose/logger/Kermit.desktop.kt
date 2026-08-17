package fr.vinarnt.animu.finder.compose.logger

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

class StdoutLogWriter : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        println("${severity.name.padEnd(7)} [$tag] $message")
        throwable?.printStackTrace()
    }
}

fun initKermitLogging() {
    Logger.setLogWriters(StdoutLogWriter())
}
