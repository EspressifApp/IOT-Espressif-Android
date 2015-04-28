package com.google.zxing.qrcode.ui;

import java.util.Hashtable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import com.espressif.iot.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Help to create QR image, according to the String content
 * 
 * @author zxing
 * 
 */
public class CreateQRImageHelper
{
    
    /**
     * @param url: need transforming, both English and Chinese are OK
     * @return
     */
    public static Bitmap createQRImage(String url, Context context)
    {
        try
        {
            Resources resources = context.getResources();
            final int QR_WIDTH = resources.getDimensionPixelSize(R.dimen.qr_image_width);
            final int QR_HEIGHT = resources.getDimensionPixelSize(R.dimen.qr_image_height);
            // check the legality of the url
            /*
             * if (url == null || "".equals(url) || url.length() < 1) { return; }
             */
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // picture-data transform, the matrix is used
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            // get the Qr code according the Qr Algorithm
            // 2 for clause means horizontal and vertical
            for (int y = 0; y < QR_HEIGHT; y++)
            {
                for (int x = 0; x < QR_WIDTH; x++)
                {
                    if (bitMatrix.get(x, y))
                    {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    }
                    else
                    {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }
                }
            }
            // generating Qr code using ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            // display the Qr picture to ImageView
            // sweepIV.setImageBitmap(bitmap);
            return bitmap;
        }
        catch (WriterException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
