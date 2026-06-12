# 🟢 Nokta Poligon İçinde mi? — Paralel Programlama (Grup 4)

![Java](https://img.shields.io/badge/Java-8-orange?logo=openjdk)
![Paralel](https://img.shields.io/badge/Paralel-Thread%20Havuzu-blue)
![Algoritma](https://img.shields.io/badge/Algoritma-Ray%20Casting-success)
![Lisans](https://img.shields.io/badge/Amaç-Eğitim-lightgrey)

Konkav (içbükey) ya da konveks (dışbükey) bir poligon, `(x, y)` köşe ikilileriyle verilir.
Çok sayıda noktanın bu poligonun **içinde** olup olmadığı **Ray Casting (ışın gönderme)**
algoritmasıyla, hem **sıralı** hem de **çok thread'li paralel** olarak hesaplanır; iki
yöntemin süreleri ölçülerek **hızlanma katsayısı (speedup)** raporlanır.

<p align="center">
  <img src="poligon_gorsel.png" width="480" alt="Poligon içinde/dışında noktalar">
  <br>
  <em>12 köşeli konkav (yıldız) poligon — <b>yeşil</b>: içeride, <b>kırmızı</b>: dışarıda</em>
</p>

---

## 🚀 Hızlı Başlangıç

> Gereksinim: **JDK 8+** (`javac` derleyicisi). Sadece JRE yeterli değildir.

```powershell
# Windows / PowerShell (en kolay yol):
.\calistir.ps1
```

veya elle:

```powershell
javac -encoding UTF-8 -d out src\NoktaPoligonIcinde.java src\PoligonGorsel.java
java  -cp out NoktaPoligonIcinde          # benchmark + CSV
java  -cp out PoligonGorsel 4000          # görsel (PNG) üretir
java  -cp out NoktaPoligonIcinde nokta 500 500   # tek nokta sorgusu
```

---

## 🧠 Nasıl Çalışır?

**Ray Casting:** Test noktasından sağa doğru yatay bir ışın gönderilir; ışının poligon
kenarlarını kaç kez kestiği sayılır. **Tek** sayıda kesişim → nokta **içeride**, **çift** →
**dışarıda**. Bu yöntem konkav poligonlarda da doğru çalışır.

**Paralelleştirme:** Her nokta diğerlerinden bağımsız test edilir (*embarrassingly
parallel*). `N` nokta, `T` thread'e bölünür; her thread `ExecutorService` üzerinden kendi
parçasını sayar (kilit yok), sonuçlar `Future.get()` ile toplanır (redüksiyon). Paylaşılan
yazılabilir durum olmadığı için **veri yarışı (race condition) oluşmaz**.

---

## 📊 Sonuçlar (8 çekirdekli makine)

**Deney 1 — Nokta sayısının etkisi (8 thread):**

| Nokta | Sıralı (ms) | Paralel (ms) | **Speedup** |
|------:|------------:|-------------:|------------:|
| 100 | 0.040 | 0.835 | **0.05** |
| 10.000 | 0.983 | 1.099 | **0.90** |
| 100.000 | 4.226 | 2.460 | **1.72** |
| 1.000.000 | 41.741 | 13.547 | **3.08** |
| 5.000.000 | 223.624 | 67.807 | **3.30** |

**Deney 2 — Thread sayısının etkisi (N = 5.000.000):**

| Thread | Paralel (ms) | **Speedup** | Verim (%) |
|-------:|-------------:|------------:|----------:|
| 1 | 220.083 | 0.99 | 98.8 |
| 2 | 174.794 | 1.24 | 62.2 |
| 4 | 130.596 | 1.67 | 41.6 |
| 8 | 84.739 | **2.57** | 32.1 |

➡️ Küçük veride paralel çözüm overhead nedeniyle yavaştır; **veri büyüdükçe hızlanma artar
(5M noktada ~3.3×)**. Tüm ölçümler `sonuclar.csv` dosyasına da yazılır.

---

## 📁 Proje Yapısı

```
.
├── src/
│   ├── NoktaPoligonIcinde.java   # Ana program: algoritma + sıralı/paralel + süre ölçümü + CSV
│   └── PoligonGorsel.java        # Poligon ve noktaları PNG'ye çizen görselleştirme
├── calistir.ps1                  # Derle + çalıştır (JDK'yı otomatik bulur)
├── RAPOR.html                    # Resmi proje raporu — kapaklı, Word/PDF'e çevrilmeye hazır
├── RAPOR.md                      # Raporun GitHub'da görüntülenen sade sürümü
├── poligon_gorsel.png            # Örnek görsel çıktı
├── sonuclar.csv                  # Ölçüm çıktıları (Excel'de grafik için)
└── README.md
```

---

## 📄 Rapor

Resmi rapor (kapaklı, biçimlendirilmiş) **`RAPOR.html`** dosyasındadır; çalışma prensibi,
kullanılan fonksiyonlar, paralel çözüm açıklaması, deneysel sonuçlar ve hızlanma analizini
içerir (~5 sayfa). Word/PDF'e çevirmek için `RAPOR.html`'i bir tarayıcıda veya Word'de açıp
**Yazdır → PDF olarak kaydet** (ya da Word'de **Farklı Kaydet → .docx**) yapabilirsiniz.
GitHub üzerinde hızlı okuma için sade markdown sürümü **[RAPOR.md](RAPOR.md)** dosyasındadır.

---

## 👤 Ekip

Paralel Programlama dersi — Grup 4 projesi. Eğitim amaçlıdır.
