import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;

public class Pixel {
    // CONSTANTS
    private static double COMPATIBLE = 128.0;
    private static int width1 = 0;
    private static double difPercentage = 0.0;
    private static double countDiff = 0.0;
    private static int delayTime = 0;
    private static int totalCount = 0;

    public static void main(String[] args) throws IOException {
        // input the GIF to find the duration of
        duration("/Users/seanjoo/Desktop/giphy.gif");
        BufferedImage imgA = null;
        BufferedImage imgB = null;

        try {
            // input the path of the two frames to compare
            File fileA = new File("/Users/seanjoo/Desktop/a1.gif");
            File fileB = new File("/Users/seanjoo/Desktop/a5.gif");
            // read file
            imgA = ImageIO.read(fileA);
            imgB = ImageIO.read(fileB);
        } catch (IOException e) {
            System.out.println(e);
        }
        //obtain width value of image A
        width1 = imgA.getWidth();
        BufferedImage outImg = getDifferenceImage(imgA, imgB);
        // pathway of the output file when finding difference between the two frames
        File f = new File("/Users/seanjoo/Documents/output.png");
        ImageIO.write(
                getDifferenceImage(
                        ImageIO.read(new File("/Users/seanjoo/Desktop/a1.gif")),
                        ImageIO.read(new File("/Users/seanjoo/Desktop/a5.gif"))),
                "png", f);
        // width of image B
        int width2 = imgB.getWidth();
        // height of image A and B
        int height1 = imgA.getHeight();
        int height2 = imgB.getHeight();
        if ((width1 != width2) || (height1 != height2))
            System.out.println("Error: Images dimensions" + " mismatch");
        else {
            long difference = 0;
            double intensityA = 0.0;
            double intensityB = 0.0;
            double totalIntensityA = 0.0;
            double totalIntensityB = 0.0;
            int count = 0;
            // Going through every pixel
            for (int y = 0; y < height1; y++) {
                for (int x = 0; x < width1; x++) {
                    int rgbA = imgA.getRGB(x, y);
                    int rgbB = imgB.getRGB(x, y);
                    Color imgAColor = new Color(rgbA, true);
                    Color imgBColor = new Color(rgbB, true);
                    int redA = imgAColor.getRed();
                    int greenA = imgAColor.getGreen();
                    int blueA = imgAColor.getBlue();
                    int redB = imgBColor.getRed();
                    int greenB = imgBColor.getGreen();
                    int blueB = imgBColor.getBlue();
                    difference += Math.abs(redA - redB);
                    difference += Math.abs(greenA - greenB);
                    difference += Math.abs(blueA - blueB);
                    // intensity levels of frame A and frame B
                    intensityA = intensity(redA, greenA, blueA);
                    totalIntensityA += intensity(redA, greenA, blueA);
                    intensityB = intensity(redB, greenB, blueB);
                    totalIntensityB += intensity(redB, greenB, blueB);
                    if (isCompatible(intensityA, intensityB)) {
                        count++;
                    }
                }
            }

            // EVALUATING THE OUTPUT

            // Total number of red pixels = width * height
            // Total number of blue pixels = width * height
            // Total number of green pixels = width * height
            // So total number of pixels = width * height * 3
            double totalPixels = width1 * height1 * 3;
            System.out.println("The total amount of pixels are: " + totalPixels);
            double differentPixelPercentage = countDiff / totalPixels;
            double dangerPercent = differentPixelPercentage * difPercentage;

            System.out.println("The percentage of dangerous Pixels are: " + dangerPercent + "%");
            if (dangerPercent > 30) {
                System.out.println("The percentage of dangerous pixels are dangerous");
                totalCount++;
            }
            if (isHzDangerous(delayTime)) {
                System.out.println("This GIF's Hz is in the range of being dangerous");
                totalCount++;
            } else {
                System.out.println("This GIF's Hz is in the range of being safe");
            }
            // Normalizing the value of different pixels for accuracy(average pixels per color component)
            double avg_different_pixels = difference / totalPixels;
            // There are 255 values of pixels in total
            double percentage = (avg_different_pixels / 255) * 100;
            System.out.println("Difference Percentage-->" + percentage);

            double avgIntensityA = totalIntensityA / totalPixels;
            double avgIntensityB = totalIntensityB / totalPixels;
            System.out.println("This is the average intensity of frame A: " + avgIntensityA);
            System.out.println("This is the average intensity of frame B: " + avgIntensityB);
            if (avgIntensityA >= avgIntensityB) {
                if (avgIntensityB / avgIntensityA <= .55) {
                    System.out.println("Average intensity ratio between frame A and B " + avgIntensityB / avgIntensityA + " is dangerous");
                    totalCount++;
                }
            } else {
                if (avgIntensityA / avgIntensityB <= .55) {
                    System.out.println("Average intensity ratio between frame A and B " + avgIntensityA / avgIntensityB + " is dangerous");
                    totalCount++;
                }
            }
            System.out.println("Average brightness of frame A: " + toBrightness(avgIntensityA));
            System.out.println("Average brightness of frame B: " + toBrightness(avgIntensityB));
            if (totalCount == 1) {
                System.out.println("This GIF is risky");
            } else if(totalCount == 2) {
                System.out.println("This GIF is dangerous");
            } else if (totalCount == 3) {
                System.out.println("This GIF is extreme");
            } else {
                System.out.println("This GIF is safe to watch");
            }
        }
    }

    /**
     * Comparing the two given frames (changing the pixel to magenta when a different pixel is found
     * @param img1 first image to compare
     * @param img2 second image to compare
     * @return a BufferedImage with the differences between 2 images highlighted in pink
     */
    private static BufferedImage getDifferenceImage(BufferedImage img1, BufferedImage img2) {
        // convert images to pixel arrays...
        final int w = img1.getWidth(),
                h = img1.getHeight(),
                highlight = Color.MAGENTA.getRGB();
        final int[] p1 = img1.getRGB(0, 0, w, h, null, 0, w);
        final int[] p2 = img2.getRGB(0, 0, w, h, null, 0, w);
        // compare image 1 to image 2, pixel by pixel. If different, highlight img1's pixel magenta
        countDiff = 0.0;
        double intensityA = 0.0;
        double intensityB = 0.0;
        double totalIntensityA = 0.0;
        double totalIntensityB = 0.0;
        double notCompatibleCount = 0.0;
        for (int i = 0; i < p1.length; i++) {
            if (p1[i] != p2[i]) {
                p1[i] = highlight;
                int width = i % width1;
                int height = i / width1;
                int rgbA = img1.getRGB(width, height);
                int rgbB = img2.getRGB(width, height);
                Color imgAColor = new Color(rgbA, true);
                Color imgBColor = new Color(rgbB, true);
                int redA = imgAColor.getRed();
                int greenA = imgAColor.getGreen();
                int blueA = imgAColor.getBlue();
                int redB = imgBColor.getRed();
                int greenB = imgBColor.getGreen();
                int blueB = imgBColor.getBlue();
                // intensity levels of frame A and frame B
                intensityA = intensity(redA, greenA, blueA);
                totalIntensityA += intensity(redA, greenA, blueA);
                intensityB = intensity(redB, greenB, blueB);
                totalIntensityB += intensity(redB, greenB, blueB);
                //evaluating the pixels if they are not compatible
                if (isCompatible(intensityA, intensityB)) {
                    notCompatibleCount++;
                }
                countDiff++;
            }
        }
        difPercentage = notCompatibleCount / countDiff * 100;
        // save image 1's pixels to a new BufferedImage
        final BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        out.setRGB(0, 0, w, h, p1, 0, w);
        return out;
    }

    /**
     * Finds the duration of the given GIF
     * @param gif gif to get duration of
     * @throws IOException
     */
    private static void duration(String gif) throws IOException {
        ImageInputStream in = ImageIO.createImageInputStream(new File(gif));
        ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
        reader.setInput(in, false);
        int totalDuration = 0;
        for (int i = 0; i < reader.getNumImages(true); i++) {
            IIOMetadata meta = reader.getImageMetadata(i);
            Node root = meta.getAsTree("javax_imageio_gif_image_1.0");
            NodeList children = root.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                if (children.item(j).getNodeName().equals("GraphicControlExtension")) {
                    NamedNodeMap attributes = children.item(j).getAttributes();
                    Node namedItem = attributes.getNamedItem("delayTime");
                    System.out.println("Delay time for frame " + (i + 1) + " is " + Integer.parseInt(namedItem.getNodeValue()) * 10 + " milliseconds");
                    delayTime = Integer.parseInt(namedItem.getNodeValue()) * 10;
                    totalDuration += Integer.parseInt(namedItem.getNodeValue()) * 10;
                }
            }
        }
        System.out.println("The total duration time for the GIF is " + totalDuration + " milliseconds");
    }

    /**
     * Evaluating the Hertz value of the given GIF
     * @param duration duration of GIF
     * @return if the Hertz value is dangerous, return true, else false.
     */
    private static boolean isHzDangerous(int duration) {
        double hz = 1.0 / (duration / 1000.0);
        System.out.println("Hertz: " + hz);
        if (hz >= 3 && hz <= 30) {
            return true;
        }
        return false;
    }

    /**
     * Obtaining the intensity levels of a given pixel
     * @param red value of red of the pixel
     * @param green value of green of the pixel
     * @param blue value of blue of the pixel
     * @return intensity level (luminance) range of 0.0 to 255.0
     */
    private static double intensity(int red, int green, int blue) {
        return 0.299 * red + 0.587 * green + 0.114 * blue;
    }

    /**
     * Finding oout whether the intensity levels of the two pixels are compatible.
     * @param intensityA intensity value of pixel A
     * @param intensityB intensity value of pixel B
     * @return true/false Two colors are compatible if the difference in their monochrome luminance is at least 128.0).
     */
    private static boolean isCompatible(double intensityA, double intensityB) {
        double value = Math.abs(intensityA - intensityB);
        return value < COMPATIBLE;
    }

    /**
     * Obtaining the brightness level through luminance.
     * @param luminance luminance value
     * @return brightness level
     */
    private static double toBrightness(double luminance) {
        return 413.435 * Math.pow(0.002745 * luminance + 0.0189623, 2.2);
    }
}