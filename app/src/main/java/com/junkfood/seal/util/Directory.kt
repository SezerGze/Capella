package com.junkfood.seal.util

/**
 * İndirme klasörü türleri. Eskiden Seal'in ayarlar ekranında tanımlıydı; o ekranlar
 * kaldırılınca motor tarafına taşındı ([com.junkfood.seal.App.updateDownloadDir] kullanıyor).
 */
enum class Directory {
    AUDIO,
    VIDEO,
    SDCARD,
    CUSTOM_COMMAND,
}
