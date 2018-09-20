package com.github.romualdrousseau.nari;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

public class GeoCache
{
	public GeoCache() {
		m_storeFilePath = null;
		m_store = null;
	}

	public static GeoCache getInstance() {
		if(GeoCache.SINGLETON == null) {
			GeoCache.SINGLETON = new GeoCache();
		}
		return GeoCache.SINGLETON;
	}

    public void clear() {
        m_store = null;
    }

	public GeoData get(String query) {
		ensureStoreLoaded();
        return m_store.get(query);
	}

	public void put(String query, GeoData data) {
		ensureStoreLoaded();
        m_store.put(query, data);

    	if(isPersistent()) {
    		saveStoreToDisk(new File(m_storeFilePath));
    	}
	}

    public void setPersistent(String storeFilePath) {
        if(!storeFilePath.equals(m_storeFilePath)) {
            m_storeFilePath = storeFilePath;
            m_store = null;
        }
    }

    public boolean isPersistent() {
        return !(m_storeFilePath == null || m_storeFilePath.equals(""));
    }

    private void ensureStoreLoaded() {
    	if(m_store != null) {
    		return;
    	}

        if(isPersistent()) {
        	m_store = loadStoreFromDisk(new File(m_storeFilePath));
        }
        else {
        	m_store = new HashMap<String, GeoData>();
        }
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, GeoData> loadStoreFromDisk(File storeFile) {
      	ObjectInputStream in = null;

        try {
            in = new ObjectInputStream(new FileInputStream(storeFile));
            return (HashMap<String, GeoData>) in.readObject();
        }
        catch(ClassNotFoundException x) {
            return new HashMap<String, GeoData>();
        }
        catch(IOException x) {
            return new HashMap<String, GeoData>();
        }
        finally {
        	if(in != null) {
        		try {
        			in.close();
        		}
        		catch(IOException x) {
        		}
        	}
        }
    }

	private void saveStoreToDisk(File storeFile) {
      	ObjectOutputStream out = null;

        try {
        	File tmpFile = File.createTempFile("geo", null);
            out = new ObjectOutputStream(new FileOutputStream(tmpFile));
            out.writeObject(m_store);
            out.close();
            out = null;

            Files.move(tmpFile.toPath(), storeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch(IOException x) {
        }
        finally {
        	if(out != null) {
        		try {
        			out.close();
        		}
        		catch(IOException x) {
        		}
        	}
        }
    }

    private String m_storeFilePath;
    private HashMap<String, GeoData> m_store;

    private static GeoCache SINGLETON = null;
}