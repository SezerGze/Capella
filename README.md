# Capella

Bağlantıyı yapıştır, biçimi seç, indir. Android için kişisel bir video indirme uygulaması.

Mağazaya çıkmaz, tek kişi için yapıldı. Arayüz Türkçe.

## Ne yapar

YouTube, Instagram gibi sitelerdeki herkese açık videoların bağlantısını yapıştırınca dört
biçimden birini indirir:

| Biçim | Ne verir |
|---|---|
| MP3 | Ses, her cihazda çalar |
| M4A | Ses, kaynaktaki AAC akışı korunur |
| MP4 | Video, 720p |
| MP4 HD | Video, 1080p |

İndirilenler `Download/Capella` klasörüne, ses dosyaları `Download/Capella/Audio` altına iner.

## Nasıl güncellenir

İki ayrı katman var ve ikisi farklı çalışır:

- **yt-dlp** — sitelerle asıl konuşan motor. Uygulamanın içinde kendini günlük olarak
  yeniler. YouTube bir şey değiştirip indirmeler bozulduğunda düzelten budur ve kimsenin
  bir şey yapması gerekmez.
- **Capella'nın kendisi** — arayüz ve uygulama kodu. Yeni sürüm bu deponun
  [Releases](../../releases) sayfasına konur; uygulama açılışta kontrol eder ve varsa tek
  dokunuşla kurar. Android sideload kurulumlarda sessiz güncellemeye izin vermediği için
  son onayı kullanıcı verir.

## Geliştirme

```bash
./gradlew assembleGenericDebug      # test derlemesi
./gradlew assembleGenericRelease    # imzalı sürüm (keystore.properties gerekir)
```

Sürüm numarası tek yerde: `buildSrc/src/main/kotlin/Version.kt`. `versionCode` oradan
türetilir. Yükselttikten sonra Actions'tan **Release** akışını çalıştırmak yeterli.

## Kaynak ve lisans

Capella, [Seal](https://github.com/JunkFood02/Seal) uygulamasının bir çatallamasıdır.
İndirme motorunun tamamı — yt-dlp entegrasyonu, ffmpeg, indirme kuyruğu, kendi kendini
güncelleme — Seal'den gelir ve büyük ölçüde olduğu gibi korunmuştur. Değişen kısım
arayüz katmanıdır.

Seal ve Capella **GPL-3.0** lisanslıdır, ayrıntı için [LICENSE](LICENSE).

Emeği geçenler:

- [Seal](https://github.com/JunkFood02/Seal) — JunkFood02
- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [youtubedl-android](https://github.com/yausername/youtubedl-android)
- [Manrope](https://github.com/googlefonts/manrope) yazı tipi — SIL Open Font License,
  metni [THIRD_PARTY/Manrope-OFL.txt](THIRD_PARTY/Manrope-OFL.txt) içinde
