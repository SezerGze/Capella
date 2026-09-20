package com.junkfood.seal.util

/**
 * yt-dlp'ye verilen çerez kaydı. Eskiden Seal'in çerez üretme ekranında tanımlıydı;
 * o ekranlar kaldırılınca motor tarafına taşındı ([DownloadUtil] kullanıyor).
 */
data class Cookie(
    val domain: String = "",
    val name: String = "",
    val value: String = "",
    val includeSubdomains: Boolean = true,
    val path: String = "/",
    val secure: Boolean = true,
    val expiry: Long = 0L,
) {
    /** yt-dlp'nin beklediği Netscape çerez dosyası satırı (sekmeyle ayrılmış). */
    fun toNetscapeCookieString(): String =
        connectWithDelimiter(
            domain,
            includeSubdomains.toString().uppercase(),
            path,
            secure.toString().uppercase(),
            expiry.toString(),
            name,
            value,
            delimiter = "\u0009",
        )
}
