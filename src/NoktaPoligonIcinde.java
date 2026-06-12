import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * GRUP 4 - Paralel Programlama Projesi
 * -------------------------------------
 * Konkav (concave) ya da konveks (convex) bir poligon, (x, y) koordinat
 * ikilileriyle verilir. Verilen çok sayıda test noktasının bu poligonun
 * içinde olup olmadığı, "Ray Casting" (ışın gönderme) algoritması ile
 * SIRALI (sequential) ve PARALEL (thread) olarak hesaplanır; iki yöntemin
 * süreleri ölçülerek HIZLANMA KATSAYISI (speedup) raporlanır.
 *
 * Derleme : javac NoktaPoligonIcinde.java
 * Çalıştır : java  NoktaPoligonIcinde
 *
 * @author  Paralel Programlama
 */
public class NoktaPoligonIcinde {

    // ---------------------------------------------------------------------
    // 1) ÇEKİRDEK ALGORİTMA: Ray Casting (Işın Gönderme)
    // ---------------------------------------------------------------------
    /**
     * Bir noktanın poligon içinde olup olmadığını test eder.
     *
     * Yöntem: Test noktasından sağa doğru yatay bir ışın gönderilir. Bu ışının
     * poligon kenarlarını kaç kez kestiği sayılır. Kesişim sayısı TEK ise nokta
     * İÇERİDE, ÇİFT ise DIŞARIDADIR. Bu yöntem hem konveks hem de konkav (içbükey)
     * poligonlarda doğru çalışır.
     *
     * @param polyX poligon köşelerinin x koordinatları
     * @param polyY poligon köşelerinin y koordinatları
     * @param px    test noktasının x'i
     * @param py    test noktasının y'si
     * @return      nokta poligon içindeyse true
     */
    public static boolean nokativarMi(double[] polyX, double[] polyY, double px, double py) {
        int n = polyX.length;
        boolean icerde = false;
        // j, i'nin bir önceki köşesidir (kenar = i -> j)
        for (int i = 0, j = n - 1; i < n; j = i++) {
            // Kenarın y aralığı, test noktasının y'sini içeriyor mu?
            boolean yKesisiyor = (polyY[i] > py) != (polyY[j] > py);
            if (yKesisiyor) {
                // Kenarın, py yüksekliğindeki x kesişim noktası
                double kesisimX = (polyX[j] - polyX[i]) * (py - polyY[i])
                        / (polyY[j] - polyY[i]) + polyX[i];
                // Kesişim, test noktasının sağındaysa bir kesişim say
                if (px < kesisimX) {
                    icerde = !icerde;
                }
            }
        }
        return icerde;
    }

    // ---------------------------------------------------------------------
    // 2) SIRALI ÇÖZÜM: Tüm noktaları tek thread ile test eder
    // ---------------------------------------------------------------------
    public static int sirali(double[] polyX, double[] polyY,
                             double[] noktaX, double[] noktaY) {
        int sayac = 0;
        for (int k = 0; k < noktaX.length; k++) {
            if (nokativarMi(polyX, polyY, noktaX[k], noktaY[k])) {
                sayac++;
            }
        }
        return sayac;
    }

    // ---------------------------------------------------------------------
    // 3) PARALEL ÇÖZÜM: Nokta dizisini parçalara böler, her parçayı ayrı
    //    bir thread işler (ExecutorService + Callable/Future).
    // ---------------------------------------------------------------------
    public static int paralel(final double[] polyX, final double[] polyY,
                              final double[] noktaX, final double[] noktaY,
                              int threadSayisi)
            throws InterruptedException, ExecutionException {

        final int N = noktaX.length;
        ExecutorService havuz = Executors.newFixedThreadPool(threadSayisi);
        List<Future<Integer>> sonuclar = new ArrayList<Future<Integer>>(threadSayisi);
        int parcaBoyu = (N + threadSayisi - 1) / threadSayisi; // tavana yuvarla

        for (int t = 0; t < threadSayisi; t++) {
            final int bas = t * parcaBoyu;
            final int son = Math.min(bas + parcaBoyu, N);
            // Her thread, kendi [bas, son) aralığındaki noktaları sayar
            sonuclar.add(havuz.submit(new Callable<Integer>() {
                @Override
                public Integer call() {
                    int yerel = 0;
                    for (int k = bas; k < son; k++) {
                        if (nokativarMi(polyX, polyY, noktaX[k], noktaY[k])) {
                            yerel++;
                        }
                    }
                    return yerel;
                }
            }));
        }

        int toplam = 0;
        for (Future<Integer> f : sonuclar) {
            toplam += f.get(); // her thread'in kısmi sonucunu topla (redüksiyon)
        }
        havuz.shutdown();
        return toplam;
    }

    // ---------------------------------------------------------------------
    // 4) YARDIMCI: Konkav (yıldız biçimli) bir test poligonu üretir.
    //    Tek/çift köşelerde yarıçap değiştirilerek içbükey çıkıntılar elde edilir.
    // ---------------------------------------------------------------------
    static double[][] yildizPoligon(int kose, double disR, double icR,
                                    double merkezX, double merkezY) {
        double[] x = new double[kose];
        double[] y = new double[kose];
        for (int i = 0; i < kose; i++) {
            double r = (i % 2 == 0) ? disR : icR;       // çift köşe dış, tek köşe iç
            double aci = 2.0 * Math.PI * i / kose;
            x[i] = merkezX + r * Math.cos(aci);
            y[i] = merkezY + r * Math.sin(aci);
        }
        return new double[][]{x, y};
    }

    // ---------------------------------------------------------------------
    // 5) ANA PROGRAM: Farklı nokta sayıları için süre ölçer, speedup yazar.
    // ---------------------------------------------------------------------
    public static void main(String[] args) throws Exception {

        // --- Test poligonu: 12 köşeli konkav (yıldız) poligon ---
        int kose = 12;
        double[][] poly = yildizPoligon(kose, 500.0, 200.0, 500.0, 500.0);
        double[] polyX = poly[0];
        double[] polyY = poly[1];

        // -----------------------------------------------------------------
        // TEK NOKTA MODU:  java NoktaPoligonIcinde nokta <x> <y>
        // Verilen tek bir noktanin poligon icinde olup olmadigini soyler.
        // -----------------------------------------------------------------
        if (args.length >= 3 && args[0].equalsIgnoreCase("nokta")) {
            double px = Double.parseDouble(args[1]);
            double py = Double.parseDouble(args[2]);
            boolean ic = nokativarMi(polyX, polyY, px, py);
            System.out.println("Poligon: 12 koseli konkav (merkez 500,500 | dis R=500, ic R=200)");
            System.out.printf("Nokta (%.2f, %.2f) -> %s%n",
                    px, py, ic ? "ICERIDE" : "DISARIDA");
            return;
        }

        int cekirdek = Runtime.getRuntime().availableProcessors();
        int threadSayisi = cekirdek;

        System.out.println("============================================================");
        System.out.println(" GRUP 4 - Nokta Poligon Icinde mi? (Paralel Cozum)");
        System.out.println("============================================================");
        System.out.println(" Poligon       : " + kose + " koseli konkav (yildiz) poligon");
        System.out.println(" CPU cekirdek  : " + cekirdek);
        System.out.println(" Thread sayisi : " + threadSayisi);
        System.out.println(" Algoritma     : Ray Casting (O(N * V))");
        System.out.println("------------------------------------------------------------");

        // CSV çıktısı: Excel'de grafik çizmek için ölçümler dosyaya yazılır
        PrintWriter csv = new PrintWriter(new FileWriter("sonuclar.csv"));
        csv.println("deney;parametre;deger;sirali_ms;paralel_ms;speedup;verim_yuzde;icerde");

        // Test edilecek nokta sayıları
        int[] noktaSayilari = {100, 1_000, 10_000, 100_000, 1_000_000, 5_000_000};
        int tekrar = 5; // her ölçüm için ortalama alınacak tekrar sayısı

        System.out.printf("%-12s | %-13s | %-13s | %-9s | %-8s%n",
                "Nokta", "Sirali (ms)", "Paralel (ms)", "Speedup", "Icerde");
        System.out.println("------------------------------------------------------------");

        Random rnd = new Random(42); // sabit tohum -> tekrar edilebilir sonuç

        for (int N : noktaSayilari) {
            // Rastgele test noktaları üret (0..1000 karesi içinde)
            double[] noktaX = new double[N];
            double[] noktaY = new double[N];
            for (int k = 0; k < N; k++) {
                noktaX[k] = rnd.nextDouble() * 1000.0;
                noktaY[k] = rnd.nextDouble() * 1000.0;
            }

            // --- Isınma (JIT derleyicisinin devreye girmesi için) ---
            sirali(polyX, polyY, noktaX, noktaY);
            paralel(polyX, polyY, noktaX, noktaY, threadSayisi);

            // --- SIRALI ölçüm ---
            long tSirali = 0;
            int icerdeS = 0;
            for (int r = 0; r < tekrar; r++) {
                long b = System.nanoTime();
                icerdeS = sirali(polyX, polyY, noktaX, noktaY);
                tSirali += System.nanoTime() - b;
            }
            double msSirali = (tSirali / (double) tekrar) / 1_000_000.0;

            // --- PARALEL ölçüm ---
            long tParalel = 0;
            int icerdeP = 0;
            for (int r = 0; r < tekrar; r++) {
                long b = System.nanoTime();
                icerdeP = paralel(polyX, polyY, noktaX, noktaY, threadSayisi);
                tParalel += System.nanoTime() - b;
            }
            double msParalel = (tParalel / (double) tekrar) / 1_000_000.0;

            // --- Doğruluk kontrolü: iki yöntem aynı sonucu vermeli ---
            String dogruluk = (icerdeS == icerdeP) ? "OK" : "HATA!";
            double speedup = msSirali / msParalel;

            System.out.printf("%-12d | %-13.3f | %-13.3f | %-9.2f | %d (%s)%n",
                    N, msSirali, msParalel, speedup, icerdeP, dogruluk);
            csv.printf("nokta_sayisi;N;%d;%.3f;%.3f;%.3f;;%d%n",
                    N, msSirali, msParalel, speedup, icerdeP);
        }

        System.out.println("------------------------------------------------------------");
        System.out.println(" Speedup = Sirali sure / Paralel sure");
        System.out.println(" 'Icerde' = poligon icinde bulunan nokta sayisi");
        System.out.println("============================================================");

        // -----------------------------------------------------------------
        // DENEY 2: Nokta sayisi SABIT, thread sayisi degisken -> olceklenme
        // -----------------------------------------------------------------
        int Nsabit = 5_000_000;
        double[] nx = new double[Nsabit];
        double[] ny = new double[Nsabit];
        for (int k = 0; k < Nsabit; k++) {
            nx[k] = rnd.nextDouble() * 1000.0;
            ny[k] = rnd.nextDouble() * 1000.0;
        }

        // Referans: tek thread'lik sirali sure
        sirali(polyX, polyY, nx, ny); // isinma
        long tref = 0;
        for (int r = 0; r < tekrar; r++) {
            long b = System.nanoTime();
            sirali(polyX, polyY, nx, ny);
            tref += System.nanoTime() - b;
        }
        double msRef = (tref / (double) tekrar) / 1_000_000.0;

        System.out.println();
        System.out.println("============================================================");
        System.out.println(" DENEY 2 - Thread olceklenmesi (N = " + Nsabit + " sabit)");
        System.out.println("============================================================");
        System.out.printf("%-12s | %-13s | %-9s | %-10s%n",
                "Thread", "Paralel (ms)", "Speedup", "Verim(%)");
        System.out.println("------------------------------------------------------------");

        int[] threadler = {1, 2, 4, 8};
        for (int T : threadler) {
            paralel(polyX, polyY, nx, ny, T); // isinma
            long tp = 0;
            for (int r = 0; r < tekrar; r++) {
                long b = System.nanoTime();
                paralel(polyX, polyY, nx, ny, T);
                tp += System.nanoTime() - b;
            }
            double msP = (tp / (double) tekrar) / 1_000_000.0;
            double sp = msRef / msP;            // hizlanma katsayisi
            double verim = 100.0 * sp / T;      // paralel verimlilik = speedup / thread
            System.out.printf("%-12d | %-13.3f | %-9.2f | %-10.1f%n", T, msP, sp, verim);
            csv.printf("thread_olcekleme;T;%d;%.3f;%.3f;%.3f;%.1f;%n",
                    T, msRef, msP, sp, verim);
        }
        System.out.println("------------------------------------------------------------");
        System.out.println(" Verim(%) = Speedup / Thread sayisi  (ideal = %100)");
        System.out.println("============================================================");

        csv.close();
        System.out.println();
        System.out.println("Olcumler CSV'ye yazildi: sonuclar.csv");
    }
}
