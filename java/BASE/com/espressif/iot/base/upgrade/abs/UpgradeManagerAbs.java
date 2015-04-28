package com.espressif.iot.base.upgrade.abs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

public abstract class UpgradeManagerAbs
{
    
    private final Logger log = Logger.getLogger(UpgradeManagerAbs.class);
    
    protected boolean download(HttpGet httpGet, String folderPath, String saveFileName)
    {
        File folderFile = new File(folderPath);
        if (!folderFile.exists())
        {
            folderFile.mkdirs();
            log.debug("make download folder");
        }
        
        DefaultHttpClient httpClient = new DefaultHttpClient();
        
        try
        {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            int status = httpResponse.getStatusLine().getStatusCode();
            log.debug("download status = " + status);
            if (status == HttpStatus.SC_OK)
            {
                InputStream is = httpResponse.getEntity().getContent();
                // Start download file
                FileOutputStream fos = new FileOutputStream(folderPath + "/" + saveFileName);
                byte[] buffer = new byte[8192];
                int count = 0;
                long size = 0;
                while ((count = is.read(buffer)) != -1)
                {
                    fos.write(buffer, 0, count);
                    size += count;
                    updateDownloadProgress(size);
                }
                fos.close();
                is.close();
                log.debug("save download file success");
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
        
        return false;
    }
    
    /**
     * Update download progress here
     * 
     * @param downloadSize
     */
    protected void updateDownloadProgress(long downloadSize)
    {
    }
}
