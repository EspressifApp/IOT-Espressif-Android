package com.espressif.iot.base.net.rest2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.log4j.Logger;

//import com.espressif.iot.base.api.EspBaseApiUtil.ProgressUpdateListener;
import com.espressif.iot.type.net.HeaderPair;

public class EspHttpDownloadUtil
{
    /**
     * the download Progress percent listener
     * 
     */
    public interface ProgressUpdateListener
    {
        void onProgress(long downloadSize, double percent);
    }
    
    private final static Logger log = Logger.getLogger(EspHttpDownloadUtil.class);
    
    final static int CONNECTION_TIMEOUT = 2 * 1000;
    
    final static int SO_TIMEOUT = 30 * 1000;
    
    public static boolean download(ProgressUpdateListener progressListener, String url, String folderPath,
        String saveFileName, HeaderPair... headers)
    {
        return __download(progressListener, url, folderPath, saveFileName, headers);
    }
    
    private static boolean __download(ProgressUpdateListener progressListener, String url, String folderPath,
        String saveFileName, HeaderPair... headers)
    {
        // build HttpGet
        HttpGet httpGet = new HttpGet(url);
        if (headers != null)
        {
            for (HeaderPair header : headers)
            {
                httpGet.addHeader(header.getName(), header.getValue());
            }
        }
        // mkdir if necessary
        File folderFile = new File(folderPath);
        if (!folderFile.exists())
        {
            folderFile.mkdirs();
        }
        
        DefaultHttpClient httpClient = new DefaultHttpClient();
        // set client params
        BasicHttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);
        httpClient.setParams(params);
        
        try
        {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                long totalSize = httpResponse.getEntity().getContentLength();
                InputStream is = httpResponse.getEntity().getContent();
                // Start download file
                FileOutputStream fos = new FileOutputStream(folderPath + "/" + saveFileName);
                byte[] buffer = new byte[8192];
                int count = 0;
                int size = 0;
                while ((count = is.read(buffer)) != -1)
                {
                    fos.write(buffer, 0, count);
                    size += count;
                    if (progressListener != null)
                    {
                        progressListener.onProgress(size, 1.0 * size / totalSize);
                    }
                }
                fos.close();
                is.close();
                log.debug(Thread.currentThread().toString() + "##__download(httpGet=[" + httpGet + "],folderPath=["
                    + folderPath + "],saveFileName=[" + saveFileName + "]): " + true);
                return true;
            }
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            httpGet.abort();
            httpClient.getConnectionManager().shutdown();
        }
        
        log.warn(Thread.currentThread().toString() + "##__download(httpGet=[" + httpGet + "],folderPath=[" + folderPath
            + "],saveFileName=[" + saveFileName + "]): " + false);
        return false;
        
    }
}
