package com.ezenit.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipOperation {

	private static final int BUFFER = 2048;
	
	public static boolean extractZip(String zippath,String target_folderpath){
		return extractZip(zippath, target_folderpath,false, null);
	}
	
	public static boolean extractZip(File zippath){
		return extractZip(zippath.getAbsolutePath(), zippath.getParent(),false, null);
	}
	
	public static boolean extractZip(File zippath,boolean skipHidden,OnFileExtractedListener fileExtractListener){
		return extractZip(zippath.getAbsolutePath(), zippath.getParent(),false, fileExtractListener);
	}
	
	public static boolean extractZip(String zippath,String target_folderpath,boolean skipHidden,OnFileExtractedListener fileExtractListener) {
		
		boolean zipextractstat = false;
		
        // Create a directory in the SDCard to store the files
        File file = new File(target_folderpath);
        if (!file.exists())
        {	
            file.mkdirs();
        
        }
        ZipFile zip = null;
        try
        {	
        	zip = new ZipFile(zippath);
            // Open the ZipInputStream
            Enumeration<? extends ZipEntry> zipenum = zip.entries();
            // Loop through all the files and folders
            while(zipenum.hasMoreElements()){
            	
            	ZipEntry entry = zipenum.nextElement();
            	
                String innerFileName = target_folderpath + File.separator + entry.getName();
                
                File innerFile = new File(innerFileName);
               
                if(skipHidden&&innerFile.isHidden()){
                	continue;
                }
                
                if (innerFile.exists())
                {	
//                	continue;
                	
                }

                // Check if it is a folder
                if (entry.isDirectory())
                {
                    // Its a folder, create that folder
                    innerFile.mkdirs();
                }
                else
                {	
                	BufferedInputStream is = new BufferedInputStream(zip
            	            .getInputStream(entry));
                    // Create a file output stream
                    FileOutputStream outputStream = new FileOutputStream(innerFileName);
                   

                    // Buffer the ouput to the file
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream,
                            BUFFER);

                    // Write the contents
                    int count = 0;
                    
                    byte[] data = new byte[BUFFER];
                    while ((count = is.read(data, 0, BUFFER)) != -1)
                    {
                        bufferedOutputStream.write(data, 0, count);
                    }

                    // Flush and close the buffers
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                    if(fileExtractListener!=null)
                    	fileExtractListener.onFileExtracted(innerFile);
                }
                        	
            	
            }
            zipextractstat = true;
           
        }
        catch (IOException e)
        {	
            e.printStackTrace();
        }
        finally{
        	if(zip!=null){
        		try {
					zip.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	
        	File zipfile = new File(zippath);
            zipfile.delete();
           
        }
        return zipextractstat;
    }
	
	public interface OnFileExtractedListener{
		public void onFileExtracted(File file);
	}
	
	
	
}
