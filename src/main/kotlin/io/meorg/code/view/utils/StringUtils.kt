package io.meorg.code.view.utils

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*

fun String?.toHttps() = this?.replace("^http://".toRegex(), "https://")

val domainStripperPattern =
    "(?:https?:\\/\\/)?(?:www\\.)?((?:\\.?[a-z0-9\\-])+)(?:[\\/]?).*?".toRegex(setOf(RegexOption.IGNORE_CASE))
        .toPattern()

fun String.extractDomainOnly(): String {
    return try {
        val matcher = domainStripperPattern.matcher(this)
        matcher.lookingAt()
        return matcher.group(1)!!
    } catch (e: Exception) {
        this
    }
}

fun String.isEmail(): Boolean = contains('@')

fun List<String>.toRegexGroup(atEnd: String = "", forceInclude: Boolean = false) =
    filter { it.isNotBlank() }
        .joinToString(
            "|",
            prefix = "(?:",
            postfix = "$atEnd)${if (!forceInclude && any { it.isBlank() }) "?" else ""}"
        )

fun String.hasComma() = this.contains(',') || this.contains(' ')

fun String.normalizedDomain() =
    replace("\\s+".toRegex(), "")
        .replace("/$".toRegex(), "")
        .replace("^http[s]?://".toRegex(), "")
        .replace("^www\\.".toRegex(), "")

private val splittersRegex = """[^\p{L}\d']+""".toRegex()
private val apostropheRegex = """[']+""".toRegex()

fun String.toSuggest() =
    replace(splittersRegex, " ")
        .replace(apostropheRegex, "")
        .trim()
        .toLowerCase()

fun String.encodeUrl(charset: Charset = Charsets.UTF_8) = URLEncoder.encode(this, charset)
fun String.decodeUrl(charset: Charset = Charsets.UTF_8) = URLDecoder.decode(this, charset)

fun String.toCurrency(): Currency = resolveCurrency(this)

fun String.bearer(): String = AuthConstants.BEARER_TOKEN_PREFIX + this