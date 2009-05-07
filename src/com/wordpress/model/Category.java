package com.wordpress.model;

import java.io.DataInputStream;
import java.io.IOException;

public class Category {

	private String id;
    private String label;
    
    private int parentCategory=-1; //-1 means no parent cat
    private String description;
	private String htmlUrl;
    private String rssUrl;

   
	public void read( DataInputStream in )
	throws IOException
	{
		this.id=in.readUTF();
		this.label=in.readUTF();
		this.parentCategory=in.readInt();
		
		boolean isNotNull = in.readBoolean();
		if (isNotNull) {
			this.description = in.readUTF();
		}
		
		isNotNull = in.readBoolean();
		if (isNotNull) {
			this.htmlUrl = in.readUTF();
		}
		
		isNotNull = in.readBoolean();
		if (isNotNull) {
			this.rssUrl = in.readUTF();
		}
		
	}
	
	public Category(String aId, String aLabel) {
        id = aId;
        label = aLabel;
	}
	
	public Category(String aId, String aLabel, String desc, int parentCat, String _htmlUrl, String _rssUrl) {
        id = aId;
        label = aLabel;
        parentCategory=parentCat;
        description=desc;
		htmlUrl = _htmlUrl;
		rssUrl = _rssUrl;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
    
    public String getDescription() {
		return description;
	}
    
    public String toString() {
        return '[' + id + '/' + label + ']';
    }
        
	public int getParentCategory() {
		return parentCategory;
	}
	
    public String getHtmlUrl() {
		return htmlUrl;
	}

	public String getRssUrl() {
		return rssUrl;
	}
	
	public void setParentCategory(int parentCategory) {
		this.parentCategory = parentCategory;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}

	public void setRssUrl(String rssUrl) {
		this.rssUrl = rssUrl;
	}

	
}

