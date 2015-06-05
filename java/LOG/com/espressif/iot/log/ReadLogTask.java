package com.espressif.iot.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;

import android.os.AsyncTask;

public abstract class ReadLogTask extends AsyncTask<Void, String, Boolean>
{

    @Override
    protected Boolean doInBackground(Void... params)
    {
        String logPath;
        int logFileNum;
        RollingFileAppender rollingFileAppender =
            (RollingFileAppender)Logger.getRootLogger().getAppender(LogConfigurator.APPENDER_NAME);
        if (rollingFileAppender != null)
        {
            logPath = rollingFileAppender.getFile().toString();
            logFileNum = rollingFileAppender.getMaxBackupIndex();
        }
        else
        {
            logPath = LogConfigurator.DefaultLogFileDirPath + LogConfigurator.DefaultLogFileName;
            logFileNum = LogConfigurator.DefaultMaxBackupSize;
        }
        
        for (int i = logFileNum - 1; i >=0 ; i--)
        {
            String extension = i == 0 ? "" : "." + i;
            File file = new File(logPath + extension);
            try
            {
                FileInputStream fis = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String log;
                while ((log = br.readLine()) != null)
                {
                    if (isCancelled())
                    {
                        br.close();
                        fis.close();
                        return false;
                    }
                    publishProgress(log);
                }
                
                br.close();
                fis.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        
        return true;
    }

    @Override
    protected abstract void onProgressUpdate(String... values);
}
