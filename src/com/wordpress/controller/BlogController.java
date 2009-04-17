package com.wordpress.controller;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import javax.microedition.lcdui.Image;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import com.wordpress.model.Blog;
import com.wordpress.model.Category;
import com.wordpress.model.Post;



public class BlogController {
	
    public final static int TYPE_NEW = 1;
    public final static int TYPE_DRAFT = 2;
    public final static int TYPE_RECENT = 3;
	
    //le utilizzo per momorizzare nel RMS i post dei blog
    private final static String POST_HEADER = "MoPressPostV";
    private final static byte POST_VERSION = 1;
    
  //le utilizzo per momorizzare nel RMS il dettaglio del blog
    private final static String BLOG_HEADER = "MoPressBlogV";
    private final static byte BLOG_VERSION = 1;
    
    //le utilizzo per momorizzare nel RMS il sommario del blog
    private final static String STORE_BLOG_SUMMARY = "MoPressBlogSummary";
    private final static String BLOG_SUMMARY_HEADER = "MoPressBlogSumV";
    private final static byte BLOG_SUMMARY_VERSION = 1;
    
	
	private Blog[] blogs = new Blog[0]; //blog associati
	
	
	private static BlogController singletonObject;
	
	public static BlogController getIstance() {
		
		if (singletonObject == null) {
			singletonObject = new BlogController();
		}
		return singletonObject;
	}
    
    //singleton
	private BlogController() {
						
		try {
			String[] rmsBlogName= loadBlogNameFromRMS();
			for (int i = 0; i < rmsBlogName.length; i++) {
				Blog currBlog=loadBlog(rmsBlogName[i]);
				addBlog(currBlog, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			//TODO displayError(e, "Non riesco a leggere i blog precedenti");
		}
	}
	
	private String sanitizeBlogName(String blogName){
		return blogName.substring(0, 15);
	}


    private RecordStore getBlogRecordStore(String aName) throws RecordStoreException, IOException {
    RecordStore records = RecordStore.openRecordStore(sanitizeBlogName(aName), false);
    
    if (records == null) {
        throw new IOException("No data for blog: " + aName);
    }

    return records;
    }
	
    
    public void removeDraftPost(Blog aBlog, int aDraftId)  throws RecordStoreException, IOException {
	   RecordStore records = getBlogRecordStore(aBlog.getBlogName());
	   records.deleteRecord(aDraftId);
       try {
            records.closeRecordStore();
        } catch (Exception e) {
        	//#debug error
    		System.out.println("removeDraftPost failed: " +e);
        }	    
    }
    
    
	public void updateDraftPost(Post aDraft, Blog aBlog, int aDraftId)throws RecordStoreException, IOException {
	    RecordStore records = getBlogRecordStore(aDraft.getBlog().getBlogName());
	    byte[] record;
	    DataInputStream data;
	    byte version;
	
	    try {
	        record = records.getRecord(aDraftId);
	        data = new DataInputStream(new ByteArrayInputStream(record));
	
	        if (!POST_HEADER.equals(data.readUTF())) {
	            throw new IOException("Invalid post data");
	        }
	            
	        version = data.readByte();
	        if (version < 1) {
	            throw new IllegalArgumentException("Invalid version: "
	                                               + version);
	        }
	
	        data.readUTF(); // id
	        data.readUTF(); // titolo
	
	        aDraft.setAuthor(readNullSafeString(data));
	        aDraft.setAuthoredOn(readNullSafeDate(data));
	        aDraft.setPrimaryCategory(aBlog.getCategory(readNullSafeString(data)));
	        aDraft.setConvertLinebreaksEnabled(data.readBoolean());
	        aDraft.setCommentsEnabled(data.readBoolean());
	        aDraft.setTrackbackEnabled(data.readBoolean());
	        aDraft.setBody(readNullSafeString(data));
	        aDraft.setExtendedBody(readNullSafeString(data));
	        aDraft.setExcerpt(readNullSafeString(data));
	        data.close();
	    } finally {
	        try {
	            records.closeRecordStore();
	        } catch (Exception e) {
	        	//#debug error
	    		System.out.println("updateDraftPost failed: " +e);
	        }
	    }
	}
    
    public Object[] getDraftPostList(Blog aBlog) throws RecordStoreException, IOException {
    	//#debug 
		System.out.println("Inizio caricamento della draft post list dal datastore RMS");
	    RecordStore records = getBlogRecordStore(aBlog.getBlogName());
	    Object[] draftList;
	    RecordEnumeration recordEnum;
	    int recordId;
	    byte[] record;
	    DataInputStream data;

    try {
        draftList = new Object[2 * (records.getNumRecords() - 1)];
        recordEnum = records.enumerateRecords(null, null, false);
        
        for (int i = 0; recordEnum.hasNextElement(); i++) {
            recordId = recordEnum.nextRecordId();
            if (recordId != 1) {
                record = records.getRecord(recordId);
                data = new DataInputStream
                    (new ByteArrayInputStream(record));
                
                if (!POST_HEADER.equals(data.readUTF())) {
                    throw new IOException("Invalid post data");
                }
                
                data.readByte(); // versione
                //#debug 
        		System.out.println("trovato post draft nel datastore RMS");
                draftList[i] = new Integer(recordId);
                draftList[++i] = new Post(aBlog, readNullSafeString(data), readNullSafeString(data), null, null);
                data.close();
            } else {
                // trovati dati del blog, skip
            	i--;
            }
        }
    } finally {
        try {
            records.closeRecordStore();
        } catch (Exception e) {
        	//#debug error
    		System.out.println("getDraftPostList failed: " +e);
        }
    }
    //#debug 
	System.out.println("termine caricamento della draft post list dal datastore RMS");

    return draftList;
}

    
    
    /**
     * Memorizzo nel record store del telefono il blog, In questo modo non devo ricaricarlo
     * ogni volta.
     * @param aBlog
     * @throws RecordStoreException
     * @throws IOException
     */    
    public void saveBlog(Blog aBlog) throws RecordStoreException, IOException {
    RecordStore records = null;
    ByteArrayOutputStream bytes;
    DataOutputStream data;
    byte[] record;
    Category[] categories = aBlog.getCategories();
    
    try {
        records = RecordStore.openRecordStore(sanitizeBlogName(aBlog.getBlogName()), true);
        bytes = new ByteArrayOutputStream();
        data = new DataOutputStream(bytes);

        data.writeUTF(BLOG_HEADER);
        data.writeByte(BLOG_VERSION);

        data.writeUTF(aBlog.getBlogXmlRpcUrl());
        data.writeUTF(aBlog.getUsername());
        data.writeUTF(aBlog.getPassword());
        data.writeUTF(aBlog.getBlogId());
        data.writeUTF(aBlog.getBlogName());
        data.writeUTF(aBlog.getBlogUrl());

        if (categories != null) {
            data.writeInt(categories.length);
            for (int i = 0; i < categories.length; i++) {
                data.writeUTF(categories[i].getId());
                data.writeUTF(categories[i].getLabel());
            }
        } else {
            data.writeInt(0);
        }

        data.close();
        record = bytes.toByteArray();
            
        if (records.getNumRecords() == 0) {
            records.addRecord(record, 0, record.length);
        } else {
            records.setRecord(1, record, 0, record.length);
        }
    } finally {
        if (records != null) {
            try {
                records.closeRecordStore();
            } catch (Exception e) {
               	//#debug error
        		System.out.println("saveBlog failed: " +e);
            }
        }
    }
}
    
/**
 * questa funzione legge un blog, identificato tramite nome, dal datastore RMS
 * @param aName
 * @return
 * @throws RecordStoreException
 * @throws IOException
 */    
public Blog loadBlog(String aName) throws RecordStoreException, IOException {
	//#debug 
	System.out.println("carico il blog " + aName + " dal datastore RMS");
    RecordStore records = getBlogRecordStore(aName);
    byte[] record = null;
    DataInputStream data = null;
    byte version;
    Blog blog;
    int categoryLength;
    Category[] categories;

    try {
        record = records.getRecord(1);
        data = new DataInputStream(new ByteArrayInputStream(record));
            
            
        if (!BLOG_HEADER.equals(data.readUTF())) {
            throw new IOException("Invalid blog data");
        }
            
        version = data.readByte();

        if (version < 1) {
            throw new IllegalArgumentException("Invalid version: " + version);
        } else {
        	
       	
           String xmlRpcUrl = data.readUTF();
           String userName = data.readUTF();
           String password = data.readUTF();
           String blodIg= data.readUTF();
           String blogName= data.readUTF();
           String blodUrl= data.readUTF();
            blog = new Blog("",blodIg, blogName, blodUrl, xmlRpcUrl, userName, password);
        }
  
        categoryLength = data.readInt();
        if (categoryLength > 0) {
            categories = new Category[categoryLength];
            for (int i = 0; i < categoryLength; i++) {
                categories[i] = new Category(data.readUTF(),
                                             data.readUTF());
            }
            blog.setCategories(categories);
        }


        data.close();
    } finally {
        try {
            records.closeRecordStore();
        } catch (Exception e) {
           	//#debug error
    		System.out.println("loadBlog failed: " +e);

        }
    }
    return blog;
}
 
	/**
	 * Questa funzione legge tutti i blog presenti nel db
	 * @throws RecordStoreException
	 * @throws IOException
	 */
	public String[] loadBlogNameFromRMS() throws RecordStoreException, IOException {
	RecordStore records = null;
	byte[] record = null;
	DataInputStream data = null;
	byte version;
	String[] mBlogNames=null;
	
	try {
	    records = RecordStore.openRecordStore(STORE_BLOG_SUMMARY, true);
	    if (records.getNumRecords() > 0) {
	        record = records.getRecord(1);
	        data = new DataInputStream(new ByteArrayInputStream(record));
	        
	        
	        if (!BLOG_SUMMARY_HEADER.equals(data.readUTF())) {
	            throw new IOException("Invalid blog summary data");
	        }
	        
	        version = data.readByte();
	        if (version > BLOG_SUMMARY_VERSION) {
	            throw new IOException("Cannot read summary data version: " + version);
	        }
	
	        mBlogNames = new String[data.readInt()];
	
		    //#debug
	   		System.out.println("Blog load count: " + mBlogNames.length);
	        for (int i = 0; i < mBlogNames.length; i++) {
	            mBlogNames[i] = data.readUTF();
	        	//#debug
	    		System.out.println("Loaded blog name: " + mBlogNames[i]);
	        }
	        data.close();
	    } else {
         	//#debug
    		System.out.println("No blog summary data loaded");
	        mBlogNames = new String[0];
	    }
	    return mBlogNames;
	} finally {
	    if (records != null) {
	        try {
	            records.closeRecordStore();
	        } catch (Exception e) {
	         	//#debug error
	    		System.out.println("loadBlogNameFromRMS failed: " +e);
	        }
	    }
	}
	
}


	private void saveBlogSummary() throws RecordStoreException, IOException {
	RecordStore records = null;
	ByteArrayOutputStream bytes;
	DataOutputStream data;
	byte[] record;
	
	try {
	    records = RecordStore.openRecordStore(STORE_BLOG_SUMMARY, true);
	    bytes = new ByteArrayOutputStream(getBlogNames().length * 8);
	    data = new DataOutputStream(bytes);
	
	    data.writeUTF(BLOG_SUMMARY_HEADER);
	    data.writeByte(BLOG_SUMMARY_VERSION);
	    data.writeInt(getBlogNames().length);
	    
	
	    for (int i = 0; i < getBlogNames().length; i++) {
	        data.writeUTF(getBlogNames()[i]);
	        //#debug
			System.out.println("Saved blog name: " + getBlogNames()[i]);
	    }
	    data.close();
	    record = bytes.toByteArray();
	        
	    if (records.getNumRecords() == 0) {
	        records.addRecord(record, 0, record.length);
	    } else {
	        records.setRecord(1, record, 0, record.length);
	    }
	} finally {
	    if (records != null) {
	        try {
	            records.closeRecordStore();
	        } catch (Exception e) {
	         	//#debug error
	    		System.out.println("saveBlogSummary failed: " +e);
	        }
	    }
	}
	}

	
    /**
     * rimuove un blog all'applicativo!!
     * @param aBlog
     * @param saveOnRMS nel caso di nuovi blog è vera, nel caso di carimento di blog precedenti è false.
     * @return
     */	
    public void removeBlog(String name) throws RecordStoreException, IOException {
    	RecordStore.deleteRecordStore(sanitizeBlogName(name));
    	
    	//le righe sottostanti potrebbero essere sostituite con la più lenta reload all da rms. come avviene all'avvio
        Blog[] newBlogNames = new Blog[blogs.length - 1];
        int j=0;
        
        for (int i = 0; i < blogs.length; i++) {
            if (blogs[i].getBlogName().equals(name)) {
            	continue;
            } else {
            	newBlogNames[j] = blogs[i];
            	j++;
            }
            
        }
            
        blogs = newBlogNames;   	
    }

    
	public void saveDraftPost(Post aDraft, int aDraftId) throws RecordStoreException, IOException {
    RecordStore records = getBlogRecordStore(aDraft.getBlog().getBlogName());
    byte[] record = null;
    ByteArrayOutputStream bytes;
    DataOutputStream data;

    try {
        bytes = new ByteArrayOutputStream(getBlogNames().length * 8);
        data = new DataOutputStream(bytes);

        data.writeUTF(POST_HEADER);
        data.writeByte(POST_VERSION);
        writeNullSafeString(aDraft.getId(), data);
        writeNullSafeString(aDraft.getTitle(), data);
        writeNullSafeString(aDraft.getAuthor(), data);
        writeNullSafeDate(aDraft.getAuthoredOn(), data);
        writeNullSafeString(aDraft.getPrimaryCategory() != null ? aDraft.getPrimaryCategory().getId() : null, data);
        data.writeBoolean(aDraft.isConvertLinebreaksEnabled());
        data.writeBoolean(aDraft.isCommentsEnabled());
        data.writeBoolean(aDraft.isTrackbackEnabled());
        writeNullSafeString(aDraft.getBody(), data);
        writeNullSafeString(aDraft.getExtendedBody(), data);
        writeNullSafeString(aDraft.getExcerpt(), data);
        data.close();

        record = bytes.toByteArray();

        if (aDraftId == -1) {
            records.addRecord(record, 0, record.length);
        } else {
            records.setRecord(aDraftId, record, 0, record.length);
        }
    } finally {
        try {
            records.closeRecordStore();
        } catch (Exception e) {
         	//#debug error
    		System.out.println("saveDraftPost failed: " +e);
        }
    }
}
	
		
    public Blog getBlog(int aIndex) throws Exception {
        try {
            return loadBlog(getBlogNames()[aIndex]);
        } catch (Exception e) {
        	throw new Exception("Failed to load blog: " + e.getMessage());            
        }
    }
	
    /**
     * Aggiunge un blog all'applicativo!!
     * @param aBlog
     * @param saveOnRMS nel caso di nuovi blog è vera, nel caso di carimento di blog precedenti è false.
     * @return
     */
    public boolean addBlog(Blog aBlog, boolean saveOnRMS) throws Exception{
        String name = aBlog.getBlogName();
        Blog[] newBlogNames = new Blog[blogs.length + 1];

        for (int i = 0; i < blogs.length; i++) {
            if (blogs[i].getBlogName().equals(name)) {
            	throw new Exception("Cannot add this blog: " + name + " because another blog with same name already exist!");
            }
            newBlogNames[i] = blogs[i];
        }
        newBlogNames[blogs.length] = aBlog;
        blogs = newBlogNames;
        
        try {
        	if(saveOnRMS == true) {
	            saveBlog(aBlog); //salvo il blog nel datastore
	            saveBlogSummary();
        	}
        } catch (Exception e) {
        	throw new Exception("Failed to save blog: " + e.getMessage());
        }
        return true;
    }

		
	public int getBlogCount() {
		return blogs.length;
	}

	
	/**
	 * Carica i nomi di tutti i blog di tutti i provider
	 * @param provider
	 * @return
	 */
	public String[] getBlogNames() {
		Vector blogNames= new Vector(); 
		for (int j = 0; j < blogs.length; j++) {
			blogNames.addElement(blogs[j].getBlogName());
		}
    	String[] names= new String[blogNames.size()];
    	blogNames.copyInto(names);
    	return names;
	}
	

	public Image[] getBlogIcons() {
	        return null;
	}
		
	private String readNullSafeString(DataInputStream aStream) throws IOException {
      String value = aStream.readUTF();
      return (value.length() == 0) ? null : value;
  }
  
  private void writeNullSafeString(String aValue, DataOutputStream aStream)
      throws IOException {
      if (aValue == null) {
          aValue = "";
      }
      aStream.writeUTF(aValue);
  }
  
  private Date readNullSafeDate(DataInputStream aStream) throws IOException {
      long value = aStream.readLong();
      return (value == -1L) ? null : new Date(value);
  }
  
  private void writeNullSafeDate(Date aValue, DataOutputStream aStream) throws IOException {
      long time = (aValue == null) ? -1 : aValue.getTime();
      aStream.writeLong(time);
  }
	
}
