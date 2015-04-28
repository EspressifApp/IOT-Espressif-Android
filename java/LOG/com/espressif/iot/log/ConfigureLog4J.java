/* 
   Copyright 2011 Rolf Kulemann

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.espressif.iot.log;

import java.io.File;

import org.apache.log4j.Level;

import android.os.Environment;

/**
 * Example how to to configure Log4J in Android. Call {@link #configure()} from your application's activity.
 * 
 * @author Rolf Kulemann
 */
public class ConfigureLog4J
{
    public static void configure()
    {
        final LogConfigurator logConfigurator = new LogConfigurator();
        
        logConfigurator.setFileName(Environment.getExternalStorageDirectory() + File.separator + "myapp.log");
        // Set the root log level
        logConfigurator.setRootLevel(Level.ERROR);
        // Set log level of a specific logger
        logConfigurator.setLevel("org.apache", Level.ERROR);
        logConfigurator.configure();
    }
}