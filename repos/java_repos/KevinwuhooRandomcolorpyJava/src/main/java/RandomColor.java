import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;

public class RandomColor {
    private final HashMap<String, ColorInfo> colormap;
    private final Random random;

    public RandomColor(Long seed) throws IOException {
        // Load the colormap from the JSON file
        try (InputStreamReader in = new InputStreamReader(getClass().getResourceAsStream("/colormap.json"));
             BufferedReader reader = new BufferedReader(in)) {
            Type type = new TypeToken<HashMap<String, ColorInfo>>(){}.getType();
            colormap = new Gson().fromJson(reader, type);
        }

        random = new Random(seed);

        for (ColorInfo colorInfo : colormap.values()) {
            List<double[]> lowerBounds = colorInfo.lowerBounds;
            double sMin = lowerBounds.get(0)[0];
            double sMax = lowerBounds.get(lowerBounds.size() - 1)[0];
            double bMin = lowerBounds.get(lowerBounds.size() - 1)[1];
            double bMax = lowerBounds.get(0)[1];

            colorInfo.saturationRange = new double[]{sMin, sMax};
            colorInfo.brightnessRange = new double[]{bMin, bMax};
        }
    }

    public List<String> generate(String hue, String luminosity, int count, String format) {
        List<String> colors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // Pick a hue
            double H = pickHue(hue);

            // Use H to determine saturation
            double S = pickSaturation(H, hue, luminosity);

            // Use S and H to determine brightness
            double B = pickBrightness(H, S, luminosity);

            // Return the HSB color in the desired format
            colors.add(setFormat(new double[]{H, S, B}, format));
        }
        return colors;
    }

    private double pickHue(String hue) {
        double[] hueRange = getHueRange(hue);
        double hueValue = randomWithin(hueRange);

        // Group red hues to make picking easier
        if (hueValue < 0) {
            hueValue += 360;
        }

        return hueValue;
    }

    private double pickSaturation(double hue, String hueName, String luminosity) {
        if ("random".equals(luminosity)) {
            return randomWithin(new double[]{0, 100});
        }
        if ("monochrome".equals(hueName)) {
            return 0;
        }

        double[] saturationRange = getSaturationRange(hue);
        double sMin = saturationRange[0];
        double sMax = saturationRange[1];

        if ("bright".equals(luminosity)) {
            sMin = 55;
        } else if ("dark".equals(luminosity)) {
            sMin = sMax - 10;
        } else if ("light".equals(luminosity)) {
            sMax = 55;
        }

        return randomWithin(new double[]{sMin, sMax});
    }

    private double pickBrightness(double H, double S, String luminosity) {
        double bMin = getMinimumBrightness(H, S);
        double bMax = 100;

        if ("dark".equals(luminosity)) {
            bMax = bMin + 20;
        } else if ("light".equals(luminosity)) {
            bMin = (bMax + bMin) / 2;
        } else if ("random".equals(luminosity)) {
            bMin = 0;
            bMax = 100;
        }

        return randomWithin(new double[]{bMin, bMax});
    }

    private String setFormat(double[] hsv, String format) {
        if ("hsv".equals(format)) {
            return Arrays.toString(hsv);
        } else if ("rgb".equals(format)) {
            int[] rgb = hsvToRgb(hsv);
            return String.format("rgb(%d, %d, %d)", rgb[0], rgb[1], rgb[2]);
        } else if ("hex".equals(format)) {
            int[] rgb = hsvToRgb(hsv);
            return String.format("#%02x%02x%02x", rgb[0], rgb[1], rgb[2]);
        } else {
            return "unrecognized format";
        }
    }

    private double getMinimumBrightness(double H, double S) {
        List<double[]> lowerBounds = getColorInfo(H).lowerBounds;

        for (int i = 0; i < lowerBounds.size() - 1; i++) {
            double s1 = lowerBounds.get(i)[0];
            double v1 = lowerBounds.get(i)[1];
            double s2 = lowerBounds.get(i + 1)[0];
            double v2 = lowerBounds.get(i + 1)[1];

            if (s1 <= S && S <= s2) {
                double m = (v2 - v1) / (s2 - s1);
                double b = v1 - m * s1;
                return m * S + b;
            }
        }

        return 0;
    }

    private double[] getHueRange(String colorInput) {
        if (colorInput != null && colorInput.matches("\\d+")) {
            int number = Integer.parseInt(colorInput);
            if (number >= 0 && number <= 360) {
                return new double[]{number, number};
            }
        } else if (colorInput != null && colormap.containsKey(colorInput)) {
            ColorInfo color = colormap.get(colorInput);
            if (color.hueRange != null) {
                return color.hueRange;
            }
        }
        return new double[]{0, 360};
    }

    private double[] getSaturationRange(double hue) {
        return getColorInfo(hue).saturationRange;
    }

    private ColorInfo getColorInfo(double hue) {
        if (hue >= 334 && hue <= 360) {
            hue -= 360;
        }
        for (ColorInfo colorInfo : colormap.values()) {
            double[] hueRange = colorInfo.hueRange;
            if (hueRange != null && hue >= hueRange[0] && hue <= hueRange[1]) {
				return colorInfo;
			}
		}
		throw new IllegalArgumentException("Color not found");
	}

	private double randomWithin(double[] range) {
		return range[0] + (range[1] - range[0]) * random.nextDouble();
	}

	private static int[] hsvToRgb(double[] hsv) {
		double h = hsv[0] / 360;
		double s = hsv[1] / 100;
		double v = hsv[2] / 100;

		int r = 0, g = 0, b = 0;

		if (s == 0) {
			r = g = b = (int) (v * 255.0 + 0.5);
		} else {
			double h6 = h * 6;
			int i = (int) Math.floor(h6);
			double f = h6 - i;
			double p = v * (1 - s);
			double q = v * (1 - s * f);
			double t = v * (1 - s * (1 - f));
			
			switch (i % 6) {
			case 0:
				r = (int) (v * 255 + 0.5);
				g = (int) (t * 255 + 0.5);
				b = (int) (p * 255 + 0.5);
				break;
			case 1:
				r = (int) (q * 255 + 0.5);
				g = (int) (v * 255 + 0.5);
				b = (int) (p * 255 + 0.5);
				break;
			case 2:
				r = (int) (p * 255 + 0.5);
				g = (int) (v * 255 + 0.5);
				b = (int) (t * 255 + 0.5);
				break;
			case 3:
				r = (int) (p * 255 + 0.5);
				g = (int) (q * 255 + 0.5);
				b = (int) (v * 255 + 0.5);
				break;
			case 4:
				r = (int) (t * 255 + 0.5);
				g = (int) (p * 255 + 0.5);
				b = (int) (v * 255 + 0.5);
				break;
			case 5:
				r = (int) (v * 255 + 0.5);
				g = (int) (p * 255 + 0.5);
				b = (int) (q * 255 + 0.5);
				break;
			}
		}
		
		return new int[] { r, g, b };
	}

	private static class ColorInfo {
        double[] hueRange;
        List<double[]> lowerBounds;
        double[] saturationRange;
        double[] brightnessRange;
    }
}
