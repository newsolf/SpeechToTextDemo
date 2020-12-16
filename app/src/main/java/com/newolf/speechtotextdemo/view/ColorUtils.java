package com.newolf.speechtotextdemo.view;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Arrays;

/**
 * 功能描述
 *
 * @author NeWolf
 * @since 2020-12-16
 */
public class ColorUtils {
    private static final int DEFAULT_FILTERED_ALPHA_VALUE = 128;

    public static int getMainColor(Bitmap bitmap) {
        return a.a(bitmap);
    }

    public static String color2String(int i) {
        String hexString = Integer.toHexString(Color.alpha(i));
        if (hexString.length() < 2) {
            hexString = '0' + hexString;
        }
        String hexString2 = Integer.toHexString(Color.red(i));
        if (hexString2.length() < 2) {
            hexString2 = '0' + hexString2;
        }
        String hexString3 = Integer.toHexString(Color.green(i));
        if (hexString3.length() < 2) {
            hexString3 = '0' + hexString3;
        }
        String hexString4 = Integer.toHexString(Color.blue(i));
        if (hexString4.length() < 2) {
            hexString4 = '0' + hexString4;
        }
        return hexString + hexString2 + hexString3 + hexString4;
    }

    public static int getDefaultFilteredColor(int i) {
        return Integer.MIN_VALUE | (16777215 & i);
    }

    public static int changeColorAlpha(int i, int i2) {
        return (i2 << 24) | (16777215 & i);
    }

    static class a {
        public static int a(Bitmap bitmap) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] iArr = new int[(width * height)];
            bitmap.getPixels(iArr, 0, width, 0, 0, width, height);
            return a(iArr);
        }

        public static int a(int[] iArr) {
            Arrays.sort(iArr);
            int b = b(iArr);
            int[] iArr2 = new int[b];
            int[] iArr3 = new int[b];
            a(iArr, iArr2, iArr3);
            int i = 0;
            int i2 = 0;
            for (int i3 = 1; i3 < b; i3++) {
                if (iArr3[i3] > iArr3[i]) {
                    if (iArr2[i3] != 0) {
                        i = i3;
                    }
                    if (Color.alpha(iArr2[i3]) == 255) {
                        i2 = i3;
                    }
                }
            }
            if (i2 == i) {
                return iArr2[i2];
            }
            if (i2 == 0) {
                return iArr2[i];
            }
            return iArr2[i2];
        }

        private static int b(int[] iArr) {
            int i;
            if (iArr.length < 2) {
                return iArr.length;
            }
            int i2 = iArr[0];
            int i3 = 1;
            int i4 = 1;
            while (i3 < iArr.length) {
                if (iArr[i3] != i2) {
                    i = iArr[i3];
                    i4++;
                } else {
                    i = i2;
                }
                i3++;
                i2 = i;
            }
            return i4;
        }

        private static void a(int[] iArr, int[] iArr2, int[] iArr3) {
            int i = 0;
            if (iArr.length != 0) {
                int i2 = iArr[0];
                iArr2[0] = i2;
                iArr3[0] = 1;
                if (iArr.length != 1) {
                    for (int i3 = 1; i3 < iArr.length; i3++) {
                        if (iArr[i3] == i2) {
                            iArr3[i] = iArr3[i] + 1;
                        } else {
                            i2 = iArr[i3];
                            i++;
                            iArr2[i] = i2;
                            iArr3[i] = 1;
                        }
                    }
                }
            }
        }
    }
}
