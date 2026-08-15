# TermiSymbian 
> Turn your retro **Nokia C5-03 (Symbian S60v5)** into an Arch Linux terminal simulator!
**TermiSymbian**, J2ME (MIDP-2.0 / CLDC-1.1) altyapısı kullanılarak Symbian dokunmatik cihazlar için özel olarak geliştirilmiş hafif ve eğlenceli bir terminal simülatörüdür.
---
## Özellikler
**Matrix / CRT Yeşili Görsel Stil**: Gerçek bir Linux TTY hissiyatı.
**Sanal Pacman Paket Yöneticisi**: `pacman -S` komutu ile yeni araçlar yükleyin.
**CMatrix Entegrasyonu**: Matrix yağmuru efekti ile retro cihazınızı renklendirin.
**Dokunmatik Kaydırma (Touch Scroll)**: Ekranda parmağınızı kaydırarak terminal geçmişini inceleyin.
**Sanal Yön Pedinden Arındırılmış**: Tam ekran dokunmatik deneyimi.
**Sistem Komutları**: `fastfetch`, `htop`, `netstat`, `free`, `uptime` ve daha fazlası.
---
## Desteklenen Komutlar

| Komut | Açıklama |
| :--- | :--- |
| `pacman -S <paket>` | Yeni bir paket yükler (`cmatrix`, `fastfetch`, `htop` vb.) |
| `cmatrix` | Matrix kod yağmuru simülasyonunu başlatır (Durdurmak için ekrana dokunun) |
| `fastfetch` / `neofetch` | Sistem ve cihaz bilgilerini gösterir |
| `htop` | Canlı CPU ve bellek kullanımını simüle eder |
| `netstat` | Ağ bağlantı durumlarını listeler |
| `free` / `free -h` | RAM kullanımını gösterir |
| `uptime` | Uygulama çalışma süresini gösterir |
| `whoami` | Aktif kullanıcı adını basar |
| `uname -a` | Çekirdek bilgisini gösterir |
| `ls` | Dizin içeriğini listeler |
| `clear` | Terminal ekranını temizler |

---
## Derleme ve Yükleme
### Gereksinimler
* Java JDK 8 (`openjdk-8`)
* Apache Maven
* Bluetooth bağdaştırıcısı ve `blueman` (Telefona doğrudan göndermek için)
### Derleme & Bluetooth ile Gönderme
Projeyi kopyalayın ve terminalinizden tek satırla derleyip Symbian cihazınıza gönderin:
```bash
# Projeyi derleyin ve JAR paketini oluşturun
JAVA_HOME=/usr/lib/jvm/java-8-openjdk mvn clean package
# (Opsiyonel) Bluetooth üzerinden Nokia C5-03'e gönderin
blueman-sendto --device=28:D1:AF:0C:CC:75 target/TermiSymbian-1.0-SNAPSHOT.jar