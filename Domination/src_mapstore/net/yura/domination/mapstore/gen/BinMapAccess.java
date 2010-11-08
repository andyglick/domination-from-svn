package net.yura.domination.mapstore.gen;
import net.yura.abba.persistence.ClientResource;
import net.yura.domination.mapstore.Category;
import net.yura.domination.mapstore.Map;
import net.yura.mobile.io.ServiceLink.Task;
import java.util.Hashtable;
import java.util.Vector;
import java.io.IOException;
import net.yura.mobile.io.BinUtil;
import java.io.DataOutputStream;
import java.io.DataInputStream;
/**
 * THIS FILE IS GENERATED, DO NOT EDIT
 */
public class BinMapAccess extends BinUtil {
    public static final int TYPE_CLIENTRESOURCE=20;
    public static final int TYPE_CATEGORY=21;
    public static final int TYPE_MAP=22;
    public static final int TYPE_TASK=23;
    public BinMapAccess() {
    }
    protected void writeObject(DataOutputStream out, Object object) throws IOException {
        if (object instanceof ClientResource) {
            out.writeInt(TYPE_CLIENTRESOURCE);
            saveClientResource(out,(ClientResource)object);
        }
        else if (object instanceof Category) {
            out.writeInt(TYPE_CATEGORY);
            saveCategory(out,(Category)object);
        }
        else if (object instanceof Map) {
            out.writeInt(TYPE_MAP);
            saveMap(out,(Map)object);
        }
        else if (object instanceof Task) {
            out.writeInt(TYPE_TASK);
            saveTask(out,(Task)object);
        }
        else {
            super.writeObject(out, object);
        }
    }
    protected void saveClientResource(DataOutputStream out,ClientResource object) throws IOException {
        out.writeInt(5);
        writeObject(out, object.getData() );
        out.writeInt( TYPE_LONG);
        out.writeLong( object.getDateModified() );
        writeObject(out, object.getResourceId() );
        writeObject(out, object.getUid() );
        out.writeInt( TYPE_INTEGER);
        out.writeInt( object.getVersion() );
    }
    protected void saveCategory(DataOutputStream out,Category object) throws IOException {
        out.writeInt(3);
        writeObject(out, object.getIconURL() );
        writeObject(out, object.getId() );
        writeObject(out, object.getName() );
    }
    protected void saveMap(DataOutputStream out,Map object) throws IOException {
        out.writeInt(14);
        writeObject(out, object.getAuthorId() );
        writeObject(out, object.getAuthorName() );
        writeObject(out, object.getDateAdded() );
        writeObject(out, object.getDescription() );
        writeObject(out, object.getId() );
        writeObject(out, object.getMapHeight() );
        writeObject(out, object.getMapUrl() );
        writeObject(out, object.getMapWidth() );
        writeObject(out, object.getName() );
        writeObject(out, object.getNumberOfDownloads() );
        writeObject(out, object.getNumberOfRatings() );
        writeObject(out, object.getPreviewUrl() );
        writeObject(out, object.getRating() );
        writeObject(out, object.getVersion() );
    }
    protected void saveTask(DataOutputStream out,Task object) throws IOException {
        out.writeInt(2);
        writeObject(out, object.getMethod() );
        writeObject(out, object.getObject() );
    }
    protected Object readObject(DataInputStream in,int type,int size) throws IOException {
        switch (type) {
            case TYPE_CLIENTRESOURCE: return readClientResource(in,size);
            case TYPE_CATEGORY: return readCategory(in,size);
            case TYPE_MAP: return readMap(in,size);
            case TYPE_TASK: return readTask(in,size);
            default: return super.readObject(in,type,size);
        }
    }
    protected ClientResource readClientResource(DataInputStream in,int size) throws IOException {
        ClientResource object = new ClientResource();
        if (size>0) {
            object.setData( (byte[])readObject(in) );
        }
        if (size>1) {
            checkType(in.readInt() , TYPE_LONG);
            object.setDateModified( in.readLong() );
        }
        if (size>2) {
            object.setResourceId( (String)readObject(in) );
        }
        if (size>3) {
            object.setUid( (String)readObject(in) );
        }
        if (size>4) {
            checkType(in.readInt() , TYPE_INTEGER);
            object.setVersion( in.readInt() );
        }
        if (size>5) {
            skipUnknownObjects(in,size - 5);
        }
        return object;
    }
    protected Category readCategory(DataInputStream in,int size) throws IOException {
        Category object = new Category();
        if (size>0) {
            object.setIconURL( (String)readObject(in) );
        }
        if (size>1) {
            object.setId( (String)readObject(in) );
        }
        if (size>2) {
            object.setName( (String)readObject(in) );
        }
        if (size>3) {
            skipUnknownObjects(in,size - 3);
        }
        return object;
    }
    protected Map readMap(DataInputStream in,int size) throws IOException {
        Map object = new Map();
        if (size>0) {
            object.setAuthorId( (String)readObject(in) );
        }
        if (size>1) {
            object.setAuthorName( (String)readObject(in) );
        }
        if (size>2) {
            object.setDateAdded( (String)readObject(in) );
        }
        if (size>3) {
            object.setDescription( (String)readObject(in) );
        }
        if (size>4) {
            object.setId( (String)readObject(in) );
        }
        if (size>5) {
            object.setMapHeight( (String)readObject(in) );
        }
        if (size>6) {
            object.setMapUrl( (String)readObject(in) );
        }
        if (size>7) {
            object.setMapWidth( (String)readObject(in) );
        }
        if (size>8) {
            object.setName( (String)readObject(in) );
        }
        if (size>9) {
            object.setNumberOfDownloads( (String)readObject(in) );
        }
        if (size>10) {
            object.setNumberOfRatings( (String)readObject(in) );
        }
        if (size>11) {
            object.setPreviewUrl( (String)readObject(in) );
        }
        if (size>12) {
            object.setRating( (String)readObject(in) );
        }
        if (size>13) {
            object.setVersion( (String)readObject(in) );
        }
        if (size>14) {
            skipUnknownObjects(in,size - 14);
        }
        return object;
    }
    protected Task readTask(DataInputStream in,int size) throws IOException {
        Task object = new Task();
        if (size>0) {
            object.setMethod( (String)readObject(in) );
        }
        if (size>1) {
            object.setObject( (Object)readObject(in) );
        }
        if (size>2) {
            skipUnknownObjects(in,size - 2);
        }
        return object;
    }
}
