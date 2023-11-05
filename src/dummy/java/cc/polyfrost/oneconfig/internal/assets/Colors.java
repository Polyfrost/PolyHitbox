package cc.polyfrost.oneconfig.internal.assets;

import java.awt.Color;

public interface Colors {
    int GRAY_700 = new Color(34, 35, 38, 255).getRGB();           // Gray 700
    int GRAY_600 = new Color(42, 44, 48, 255).getRGB();           // Gray 600
    int GRAY_500 = new Color(49, 51, 56, 255).getRGB();           // Gray 500         // button sidebar hover, button gray normal
    int GRAY_500_80 = new Color(49, 51, 56, 204).getRGB();        // Gray 500 80%     // button sidebar pressed
    int GRAY_400 = new Color(55, 59, 69, 255).getRGB();           // Gray 400
    int GRAY_400_40 = new Color(55, 59, 69, 102).getRGB();        // Gray 400 40%
    int GRAY_400_60 = new Color(55, 59, 69, 153).getRGB();        // Gray 400 60%
    int GRAY_300 = new Color(73, 79, 92, 255).getRGB();           // Gray 300         // button gray hover
    int GRAY_400_80 = new Color(55, 59, 69, 204).getRGB();        // Gray 400 80%     // button gray pressed
    int PRIMARY_800 = new Color(13, 51, 128, 255).getRGB();          // Blue 800
    int PRIMARY_700 = new Color(18, 71, 178, 255).getRGB();          // Blue 700
    int PRIMARY_700_80 = new Color(18, 71, 178, 204).getRGB();       // Blue 700 80%
    int PRIMARY_600 = new Color(20, 82, 204, 255).getRGB();          // Blue 600         // button blue normal
    int PRIMARY_500 = new Color(25, 103, 255, 255).getRGB();         // Blue 500         // button blue hover
    int PRIMARY_400 = new Color(48, 129, 242, 255).getRGB();
    int WHITE_50 = new Color(255, 255, 255, 127).getRGB();        // White 50%
    int WHITE_60 = new Color(255, 255, 255, 153).getRGB();        // White 60%
    int WHITE_80 = new Color(255, 255, 255, 204).getRGB();        // White 80%
    int WHITE_90 = new Color(255, 255, 255, 229).getRGB();        // White 90%
    int WHITE_95 = new Color(255, 255, 255, 242).getRGB();        // White 95%
    int WHITE = new Color(255, 255, 255, 255).getRGB();           // White 100%
    int SUCCESS_600 = new Color(3, 152, 85).getRGB();
    int SUCCESS_700 = new Color(2, 121, 72).getRGB();
    int WARNING_500 = new Color(247, 144, 9).getRGB();
    int WARNING_600 = new Color(220, 104, 3).getRGB();
    int ERROR_600_80 = new Color(217, 32, 32, 204).getRGB();
    int ERROR_600 = new Color(217, 32, 32).getRGB();
    int ERROR_700 = new Color(180, 24, 24).getRGB();         // Red 700
    int ERROR_800 = new Color(145, 24, 24).getRGB();         // Red 800
    int ERROR_800_80 = new Color(145, 24, 24, 204).getRGB();         // Red 800
    int ERROR_300 = new Color(253, 155, 155).getRGB();
    int ERROR_300_80 = new Color(253, 155, 155, 204).getRGB();

}
