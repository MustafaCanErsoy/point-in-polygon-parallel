import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * GRUP 4 - Gorsellestirme
 * -----------------------
 * Konkav test poligonunu ve rastgele uretilen noktalari bir PNG dosyasina cizer.
 * Poligon ICINDE kalan noktalar YESIL, DISARIDA kalanlar KIRMIZI gosterilir.
 * Boylece Ray Casting algoritmasinin dogru calistigi gozle de dogrulanir.
 *
 * Calistir : java PoligonGorsel              (varsayilan: 3000 nokta)
 *            java PoligonGorsel 5000          (nokta sayisi parametreli)
 *
 * Cikti    : poligon_gorsel.png
 */
public class PoligonGorsel {

    public static void main(String[] args) throws Exception {
        int noktaSayisi = (args.length > 0) ? Integer.parseInt(args[0]) : 3000;

        // Hesaplama ile ayni poligon (NoktaPoligonIcinde sinifindan)
        double[][] poly = NoktaPoligonIcinde.yildizPoligon(12, 500.0, 200.0, 500.0, 500.0);
        double[] polyX = poly[0];
        double[] polyY = poly[1];

        int W = 800, H = 800;       // resim boyutu (piksel)
        double duzlem = 1000.0;     // koordinat uzayi: 0..1000
        double olcek = W / duzlem;  // koordinat -> piksel olcegi

        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Arka plan
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, W, H);

        // Poligonu cizgisel yol olarak olustur (y ekranda ters cevrilir)
        Path2D yol = new Path2D.Double();
        for (int i = 0; i < polyX.length; i++) {
            double px = polyX[i] * olcek;
            double py = H - polyY[i] * olcek;
            if (i == 0) yol.moveTo(px, py); else yol.lineTo(px, py);
        }
        yol.closePath();

        // Poligon ic dolgusu (acik mavi yari saydam) + kenar cizgisi
        g.setColor(new Color(180, 210, 255));
        g.fill(yol);
        g.setColor(new Color(0, 70, 160));
        g.setStroke(new BasicStroke(2.5f));
        g.draw(yol);

        // Noktalari ciz: icerde -> yesil, disarda -> kirmizi
        Random rnd = new Random(42); // hesaplama ile ayni tohum
        int icerde = 0;
        for (int k = 0; k < noktaSayisi; k++) {
            double x = rnd.nextDouble() * duzlem;
            double y = rnd.nextDouble() * duzlem;
            boolean ic = NoktaPoligonIcinde.nokativarMi(polyX, polyY, x, y);
            if (ic) icerde++;
            g.setColor(ic ? new Color(0, 160, 0) : new Color(220, 50, 50));
            int sx = (int) (x * olcek);
            int sy = (int) (H - y * olcek);
            g.fillOval(sx - 2, sy - 2, 4, 4);
        }

        // Baslik / aciklama yazisi
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("Grup 4 - Nokta Poligon Icinde mi? (12 koseli konkav poligon)", 15, 25);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(0, 160, 0));
        g.drawString("YESIL = icerde (" + icerde + ")", 15, H - 30);
        g.setColor(new Color(220, 50, 50));
        g.drawString("KIRMIZI = disarida (" + (noktaSayisi - icerde) + ")", 200, H - 30);

        g.dispose();

        File cikti = new File("poligon_gorsel.png");
        ImageIO.write(img, "png", cikti);
        System.out.println("Gorsel olusturuldu: " + cikti.getAbsolutePath());
        System.out.println("Toplam nokta: " + noktaSayisi + " | Icerde: " + icerde
                + " | Disarida: " + (noktaSayisi - icerde));
    }
}
