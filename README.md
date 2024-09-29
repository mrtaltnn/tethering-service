# tethering-service

Uses interactions to enable USB tethering as soon as a USB cable is connected. It does not require root. It is also activated when the device is restarted.

The following settings must be set in Developer Options;

Screen lock : false
Accessibility->Installed Services-> TetherinService: on

Available Device Uis:
EMUI (Huawei Models)

--------
** Cihaz açıldığında (BootReciver)
- Usb bağlantısı varsa TetherSettings açılır değilse injector app açılır.

** Usb bağlandığında (UsbConnectionReceiver)
- State temizlenir (buna detaylı bakalım)
- TetherSettings açılır

** CronJob (Her 15 dk da bir) //burada şey yapalım httpInjector ı  5 dk da bir konrol etsin, kopmuşsan bu operasyonlar yapılsın
- State temizlenir (buna detaylı bakalım)
- HttpInjector açılır