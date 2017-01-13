/* Copyright (C) 2016 Tcl Corporation Limited */
/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tct.weather.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

/**
 * User : user
 * Date : 2016-03-31
 * Time : 10:36
 */
public class ColorCutQuantizer {
    public  static final String LOG_TAG = "ColorCutQuantizer";

    private final float[] mTempHsl = new float[3];
    private static final float BRIGHT_MIN_LIGHTNESS  = 0.85f;
    private static final float BRIGHT_MIN_PROPORTION = 0.33f;
    double brightColorProportion;

    /**
     * Factory-method to generate a {@link ColorCutQuantizer} from a {@link Bitmap} object.
     *
     * @param bitmap Bitmap to extract the pixel data from
     */
    public static ColorCutQuantizer fromBitmap(Bitmap bitmap) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        final int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        return new ColorCutQuantizer(new ColorHistogram(pixels));
    }

    /**
     * Private constructor.
     *
     * @param colorHistogram histogram representing an image's pixel data
     */
    private ColorCutQuantizer(ColorHistogram colorHistogram) {
        final int[] rawColors = colorHistogram.getColors();
        final int[] rawColorCounts = colorHistogram.getColorCounts();

        int color = 0;
        int colorCount = 0;
        int brightColorCount = 0;
        int allColorCount = 0;
        for (int i = 0; i < rawColors.length; i++) {
            color = rawColors[i];
            colorCount = rawColorCounts[i];
            RGBtoHSL(Color.red(color), Color.green(color), Color.blue(color), mTempHsl);
            if (isBright(mTempHsl)) {
                brightColorCount += colorCount;
            }
            allColorCount += colorCount;
        }
        brightColorProportion = (double)brightColorCount/allColorCount;
        Log.d(LOG_TAG, "Proportion bright:"+brightColorProportion);
    }

    /**
     * @return true if the proportion of light color more than 0.33
     */
    public boolean getBrightColor(){
        if (brightColorProportion > BRIGHT_MIN_PROPORTION) {
            return false;
        }
        return true;
    }

    /**
     * @return true if the color represents a color which is close to white.
     */
    private static boolean isBright(float[] hslColor) {
        return hslColor[2] >= BRIGHT_MIN_LIGHTNESS;
    }

    static void RGBtoHSL(int r, int g, int b, float[] hsl) {
        final float rf = r / 255f;
        final float gf = g / 255f;
        final float bf = b / 255f;

        final float max = Math.max(rf, Math.max(gf, bf));
        final float min = Math.min(rf, Math.min(gf, bf));
        final float deltaMaxMin = max - min;

        float h, s;
        float l = (max + min) / 2f;

        if (max == min) {
            // Monochromatic
            h = s = 0f;
        } else {
            if (max == rf) {
                h = ((gf - bf) / deltaMaxMin) % 6f;
            } else if (max == gf) {
                h = ((bf - rf) / deltaMaxMin) + 2f;
            } else {
                h = ((rf - gf) / deltaMaxMin) + 4f;
            }

            s =  deltaMaxMin / (1f - Math.abs(2f * l - 1f));
        }

        hsl[0] = (h * 60f) % 360f;
        hsl[1] = s;
        hsl[2] = l;
    }

    static int HSLtoRGB (float[] hsl) {
        final float h = hsl[0];
        final float s = hsl[1];
        final float l = hsl[2];

        final float c = (1f - Math.abs(2 * l - 1f)) * s;
        final float m = l - 0.5f * c;
        final float x = c * (1f - Math.abs((h / 60f % 2f) - 1f));

        final int hueSegment = (int) h / 60;

        int r = 0, g = 0, b = 0;

        switch (hueSegment) {
            case 0:
                r = Math.round(255 * (c + m));
                g = Math.round(255 * (x + m));
                b = Math.round(255 * m);
                break;
            case 1:
                r = Math.round(255 * (x + m));
                g = Math.round(255 * (c + m));
                b = Math.round(255 * m);
                break;
            case 2:
                r = Math.round(255 * m);
                g = Math.round(255 * (c + m));
                b = Math.round(255 * (x + m));
                break;
            case 3:
                r = Math.round(255 * m);
                g = Math.round(255 * (x + m));
                b = Math.round(255 * (c + m));
                break;
            case 4:
                r = Math.round(255 * (x + m));
                g = Math.round(255 * m);
                b = Math.round(255 * (c + m));
                break;
            case 5:
            case 6:
                r = Math.round(255 * (c + m));
                g = Math.round(255 * m);
                b = Math.round(255 * (x + m));
                break;
        }

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        return Color.rgb(r, g, b);
    }
}
