package me.stuartdouglas.models;


/*
 * Expects a cassandra columnfamily defined as
 * use keyspace2;
 CREATE TABLE Tweets (
 user varchar,
 interaction_time timeuuid,
 tweet varchar,
 PRIMARY KEY (user,interaction_time)
 ) WITH CLUSTERING ORDER BY (interaction_time DESC);
 * To manually generate a UUID use:
 * http://www.famkruithof.net/uuid/uuidgen
 */
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.jhlabs.image.ChromeFilter;
import com.jhlabs.image.CrystallizeFilter;
import com.jhlabs.image.ExposureFilter;
import com.jhlabs.image.FlareFilter;
import com.jhlabs.image.GrayscaleFilter;
import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.PointillizeFilter;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

import javax.imageio.ImageIO;










import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import static org.imgscalr.Scalr.*;

import org.imgscalr.Scalr.Method;

import me.stuartdouglas.lib.*;
import me.stuartdouglas.stores.Pic;

public class PicModel {
	private static Cluster cluster;
	int width;
	   int height;

    public PicModel() {
    	
    }
    
    /*
     * getPublicPosts()
     * Gets all public posts from all users
     */
    
    public LinkedList<Pic> getPublicPosts() {
    	LinkedList<Pic> instaList = new LinkedList<Pic>();
    	LinkedList<Pic> instaSortedList = new LinkedList<Pic>();
    	Session session = cluster.connect("instashutter");
    	
    	PreparedStatement statement = session.prepare("SELECT * from pics where public = true allow filtering");
    	
    	BoundStatement boundStatement = new BoundStatement(statement);
    	
    	ResultSet rs = session.execute(boundStatement);
    	if (rs.isExhausted()){
    		System.out.println("No posts returned");
    		return null;
    	} else {
    		for (Row row : rs) {
    			Pic pic = new Pic();
    			UUID picid = row.getUUID("picid");
    			pic.setUUID(row.getUUID("picid"));
    			pic.setCaption(row.getString("caption"));
    			pic.setPostedUsername(row.getString("user"));
    			pic.setPicAdded(row.getDate("interaction_time"));
    			instaList.add(pic);
    			//Get comments from post and limit to 5 (stops endless list on dashboard)
    			PreparedStatement pss = session.prepare("SELECT * from comments where picid =? limit 5");
    	    	ResultSet rss = null;
    	    	BoundStatement boundStatement2 = new BoundStatement(pss);
    	    	rss = session.execute(boundStatement2.bind(picid));
    	    	if (rss.isExhausted())	{
    	    	}	else	{
    	    		for (Row rows : rss)	{
    	    			pic.setCommentlist(rows.getString("username"), rows.getUUID("picid"), rows.getString("comment_text"), rows.getDate("comment_added"));
    	    		}
    	    	}
    		}
    		session.close();
    	}
    	
    	
    	
    	
    	
    	//Sorting posts by pic_added
    	instaList.stream()
        .sorted((e1, e2) -> e2.getPicAdded()
                .compareTo(e1.getPicAdded()))
        .forEach(e ->  instaSortedList.add(e));
    	
		return instaSortedList;
		
    }
    /*
     * GetPosts()
     * Gets all posts public and private from all users
     * 
     */
    public LinkedList<Pic> getPosts() {
    	LinkedList<Pic> instaList = new LinkedList<Pic>();
    	LinkedList<Pic> instaSortedList = new LinkedList<Pic>();
    	Session session = cluster.connect("instashutter");
    	
    	PreparedStatement statement = session.prepare("SELECT * from pics");
    	
    	BoundStatement boundStatement = new BoundStatement(statement);
    	
    	ResultSet rs = session.execute(boundStatement);
    	if (rs.isExhausted()){
    		System.out.println("No posts returned");
    		return null;
    	} else {
    		for (Row row : rs) {
    			Pic pic = new Pic();
    			UUID picid = row.getUUID("picid");
    			pic.setUUID(row.getUUID("picid"));
    			pic.setCaption(row.getString("caption"));
    			pic.setPostedUsername(row.getString("user"));
    			pic.setPicAdded(row.getDate("interaction_time"));
    			instaList.add(pic);
    			//Get comments from post and limit to 5 (stops endless list on dashboard)
    			PreparedStatement pss = session.prepare("SELECT * from comments where picid =? limit 5");
    	    	ResultSet rss = null;
    	    	BoundStatement boundStatement2 = new BoundStatement(pss);
    	    	rss = session.execute(boundStatement2.bind(picid));
    	    	if (rss.isExhausted())	{
    	    	}	else	{
    	    		for (Row rows : rss)	{
    	    			pic.setCommentlist(rows.getString("username"), rows.getUUID("picid"), rows.getString("comment_text"), rows.getDate("comment_added"));
    	    		}
    	    	}
    		}
    		session.close();
    	}
    	
    	
    	
    	
    	
    	//Sorting posts by pic_added
    	instaList.stream()
        .sorted((e1, e2) -> e2.getPicAdded()
                .compareTo(e1.getPicAdded()))
        .forEach(e ->  instaSortedList.add(e));
    	
		return instaSortedList;
		
    }
    
    public LinkedList<Pic> getPost(String user) {
    	LinkedList<Pic> instaList = new LinkedList<Pic>();
    	LinkedList<Pic> instaSortedList = new LinkedList<Pic>();
    	Session session = cluster.connect("instashutter");
    	UUID picid = UUID.fromString(user);
    	PreparedStatement statement = session.prepare("SELECT * from pics where picid =?");
    	
    	BoundStatement boundStatement = new BoundStatement(statement);
    	
    	ResultSet rs = session.execute(boundStatement.bind(picid));
    	if (rs.isExhausted()){
    		System.out.println("No posts returned");
    	} else {
    		for (Row row : rs) {
    			Pic ps = new Pic();
    			ps.setUUID(row.getUUID("picid"));
    			ps.setCaption(row.getString("caption"));
    			ps.setPostedUsername(row.getString("user"));
    			ps.setPicAdded(row.getDate("interaction_time"));
    			instaList.add(ps);
    			//Get comments from posts
    			PreparedStatement pss = session.prepare("SELECT * from comments where picid =?");
    	    	ResultSet rss = null;
    	    	BoundStatement boundStatement2 = new BoundStatement(pss);
    	    	rss = session.execute(boundStatement2.bind(picid));
    	    	if (rss.isExhausted())	{
    	    	}	else	{
    	    		for (Row rows : rss)	{
    	    			ps.setCommentlist(rows.getString("username"), rows.getUUID("picid"), rows.getString("comment_text"), rows.getDate("comment_added"));
    	    		}
    	    	}
    		}
    	}
    	instaList.stream()
        .sorted((e1, e2) -> e2.getPicAdded()
                .compareTo(e1.getPicAdded()))
        .forEach(e ->  instaSortedList.add(e));
    	session.close();
		return instaSortedList;
    }
    
    
    
    
    public static java.util.LinkedList<Pic> getPicsForUser(String User) {
        java.util.LinkedList<Pic> Pics = new java.util.LinkedList<>();
        Session session = cluster.connect("instashutter");
        PreparedStatement ps = session.prepare("select * from userpiclist where user =?");
        ResultSet rs = null;
        BoundStatement boundStatement = new BoundStatement(ps);
        rs = session.execute( // this is where the query is executed
                boundStatement.bind( // here you are binding the 'boundStatement'
                        User));
        if (rs.isExhausted()) {
            System.out.println("No Images returned");
            return null;
        } else {
            for (Row row : rs) {
                Pic pic = new Pic();
                java.util.UUID UUID = row.getUUID("picid");
                pic.setCaption(row.getString("caption"));
                pic.setPostedUsername(row.getString("user"));
                pic.setPicAdded(row.getDate("pic_added"));
                System.out.println("UUID" + UUID.toString());
                pic.setUUID(UUID);
                Pics.add(pic);
                //Get comments from posts
    			PreparedStatement pss = session.prepare("SELECT * from comments where picid =?");
    	    	ResultSet rss = null;
    	    	BoundStatement boundStatement2 = new BoundStatement(pss);
    	    	rss = session.execute(boundStatement2.bind(UUID));
    	    	if (rss.isExhausted())	{
    	    	}	else	{
    	    		for (Row rows : rss)	{
    	    			pic.setCommentlist(rows.getString("username"), rows.getUUID("picid"), rows.getString("comment_text"), rows.getDate("comment_added"));
    	    		}
    	    	}
            }
        }
        session.close();
        return Pics;
    }
    
    
    
    public void setCluster(Cluster cluster) {
        PicModel.cluster = cluster;
    }
    
    
    
    
    public void insertPic(byte[] b, String type, String name, String user, String caption, String filter, boolean publicPhoto) {
    	
    	try {
        	
        	
            //Convertors convertor = new Convertors();

            String types[]=Convertors.SplitFiletype(type);
            ByteBuffer buffer = ByteBuffer.wrap(b);
            int length = b.length;
            java.util.UUID picid = Convertors.getTimeUUID();
            
            //The following is a quick and dirty way of doing this, will fill the disk quickly !
            Boolean success = (new File("/var/tmp/instashutter/")).mkdirs();
            FileOutputStream output = new FileOutputStream(new File("/var/tmp/instashutter/" + picid));

            
            output.write(b);
            
            
            byte []  thumbb = picresize(picid.toString(),types[1]);
            int thumblength= thumbb.length;
            ByteBuffer thumbbuf=ByteBuffer.wrap(thumbb);
            byte[] processedb = null;
            System.out.println("Image type:" + type);
            if (type.equals("image/gif")){
            	processedb = b;
            }	else	{
            	processedb = picdecolour(picid.toString(),types[1]);
            }
            ByteBuffer processedbuf=ByteBuffer.wrap(processedb);
            int processedlength=processedb.length;
            Session session = cluster.connect("instashutter");

            PreparedStatement psInsertPic = session.prepare("insert into pics ( picid, image,thumb,processed, user, interaction_time,imagelength,thumblength,processedlength,type,name, caption, public) values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
            PreparedStatement psInsertPicToUser = session.prepare("insert into userpiclist ( picid, user, pic_added, caption, public) values(?,?,?,?,?)");
            BoundStatement bsInsertPic = new BoundStatement(psInsertPic);
            BoundStatement bsInsertPicToUser = new BoundStatement(psInsertPicToUser);

            Date DateAdded = new Date();
            session.execute(bsInsertPic.bind(picid, buffer, thumbbuf,processedbuf, user, DateAdded, length,thumblength,processedlength, type, name, caption, publicPhoto));
            session.execute(bsInsertPicToUser.bind(picid, user, DateAdded, caption, publicPhoto));
            try {
            	session.close();
            } catch (Exception e) {
            	System.out.println("Session.close error " + e);
            }
            try {
            	output.close();
            } catch (Exception e) {
            	System.out.println("output.close error " + e);
            }
            

        } catch (IOException ex) {
            System.out.println("Error --> " + ex);
        }
    }

    public byte[] picresize(String picid,String type) {
        try {
            BufferedImage BI = ImageIO.read(new File("/var/tmp/instashutter/" + picid));
            BufferedImage thumbnail = createThumbnail(BI);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, type, baos);
            baos.flush();
            
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            return imageInByte;
        } catch (IOException et) {

        }
        return null;
    }
    
    public void applyFilter(String filter, UUID picid)	{
    	try {
    		File input = new File("/var/tmp/instashutter/" + picid);
    		File output1 = new File("/var/tmp/instashutter/" + picid);
    		
        	BufferedImage imageIn = ImageIO.read(input);
        	BufferedImage imageOut = ImageIO.read(input);

        	if (filter.equals("bw"))	{
        		GrayscaleFilter greyFilter = new GrayscaleFilter();
            	greyFilter.filter(imageIn, imageOut);  
            }	else if (filter.equals("invert"))	{
            	InvertFilter invert = new InvertFilter();
            	invert.filter(imageIn, imageOut);
            }	else if (filter.equals("exposure"))	{
            	ExposureFilter exposure = new ExposureFilter();
            	exposure.filter(imageIn, imageOut);
            }	else if (filter.equals("flare"))	{
            	FlareFilter flare = new FlareFilter();
            	flare.filter(imageIn, imageOut);
            }	else if (filter.equals("pointillize"))	{
            	PointillizeFilter pointillize = new PointillizeFilter();
            	pointillize.filter(imageIn, imageOut);
            }	else if (filter.equals("crystallize"))	{
            	CrystallizeFilter crystallize = new CrystallizeFilter();
            	crystallize.filter(imageIn, imageOut);
            }	else if (filter.equals("chrome"))	{
            	ChromeFilter chrome = new ChromeFilter();
            	chrome.filter(imageIn, imageOut);
            }
            
            
            else	{
            	
            }
            	System.out.println("not applying filter");
            
        	
            ImageIO.write(imageOut, "gif", output1);
        }	catch(Exception e)	{
        	System.out.println("Error applying filter");
        	e.printStackTrace();
        }
        
    }
    
    public byte[] picdecolour(String picid,String type) {
        try {
            BufferedImage BI = ImageIO.read(new File("/var/tmp/instashutter/" + picid));
            BufferedImage processed = createProcessed(BI);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processed, type, baos);
            baos.flush();     
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            return imageInByte;
        } catch (IOException et) {

        }
        return null;
    }

    public static BufferedImage createThumbnail(BufferedImage img) {
        img = resize(img, Method.AUTOMATIC, 500, OP_ANTIALIAS);
        return img;
    }
    
   public static BufferedImage createProcessed(BufferedImage img) {
        int Width=img.getWidth()-1;
        img = resize(img, Method.AUTOMATIC, Width, OP_ANTIALIAS);//, OP_GRAYSCALE);
        return img;
    }
   
   public Pic getPic(int image_type, java.util.UUID picid) {
       Session session = cluster.connect("instashutter");
       ByteBuffer bImage = null;
       String type = null;
       int length = 0;
       boolean privacy = false;
       
       try {
           //Convertors convertor = new Convertors();
           ResultSet rs = null;
           PreparedStatement ps = null;
           
           if (image_type == Convertors.DISPLAY_IMAGE) {
               ps = session.prepare("select image,imagelength,type,public from pics where picid =?");
           } else if (image_type == Convertors.DISPLAY_THUMB) {
               ps = session.prepare("select thumb,imagelength,thumblength,type,public from pics where picid =?");
           } else if (image_type == Convertors.DISPLAY_PROCESSED) {
               ps = session.prepare("select processed,processedlength,type,public from pics where picid =?");
           }
           BoundStatement boundStatement = new BoundStatement(ps);
           rs = session.execute( // this is where the query is executed
                   boundStatement.bind( // here you are binding the 'boundStatement'
                           picid));

           if (rs.isExhausted()) {
               System.out.println("No Images returned");
               return null;
           } else {
               for (Row row : rs) {
                   if (image_type == Convertors.DISPLAY_IMAGE) {
                       bImage = row.getBytes("image");
                       length = row.getInt("imagelength");
                   } else if (image_type == Convertors.DISPLAY_THUMB) {
                       bImage = row.getBytes("thumb");
                       length = row.getInt("thumblength");
               
                   } else if (image_type == Convertors.DISPLAY_PROCESSED) {
                       bImage = row.getBytes("processed");
                       length = row.getInt("processedlength");
                   }
                   privacy = row.getBool("public");
                   
                   type = row.getString("type");

               }
           }
       } catch (Exception et) {
           System.out.println("Can't get Pic" + et);
           return null;
       }
       session.close();
       Pic p = new Pic();
       p.setPic(bImage, length, type);
         
       
       p.setIsPublic(privacy);
       return p;

   }
   


	public void postComment(String username, String picid, String comment) {
		try {
			Session session = cluster.connect("instashutter");
            
            PreparedStatement ps = session.prepare("insert into comments (comment_added, picid, comment_text, username) values(?,?,?,?)");
            BoundStatement bs = new BoundStatement(ps);
            UUID uuid = UUID.fromString(picid);
            Date date = new Date();
            session.execute(bs.bind(date, uuid, comment, username));

            session.close();
		}	catch (Exception e)	{
			System.out.println("Error posting comment to database: " + e);
		}
		
	}

	public String getUserPosted(UUID picid)	{
		try {
			Session session = cluster.connect("instashutter");
			String username = null;
			PreparedStatement ps = session.prepare("select user from pics where picid=?");
			BoundStatement bs = new BoundStatement(ps);
			ResultSet rs = session.execute(bs.bind(picid));
			if (rs.isExhausted()) {
                System.out.println("No Images returned");
                return null;
            } else {
                for (Row row : rs) {
                    username = row.getString("user");
                }
                return username;
            }
		}	catch(Exception e)	{
			System.out.println("Error getting username: " + e);
		}
		return null;
	}

	public void deletePost(String username, UUID picid) {
		// TODO Auto-generated method stub
		try {
			Session session = cluster.connect("instashutter");
			PreparedStatement psPicList = session.prepare("delete from pics where picid =?");
			PreparedStatement psUserPicList = session.prepare("delete from userpiclist where user = ? and picid =?");
			PreparedStatement psComments = session.prepare("delete from comments where picid=?");
			BoundStatement bsPicList = new BoundStatement(psPicList);
			BoundStatement bsUserPicList = new BoundStatement(psUserPicList);
			BoundStatement bsComments = new BoundStatement(psComments);
			session.execute(bsPicList.bind(picid));
			session.execute(bsUserPicList.bind(username, picid));
			session.execute(bsComments.bind(picid));
		}	catch(Exception e)	{
			System.out.println("Error deleting post" + e);
		}
		
	}
	
}
