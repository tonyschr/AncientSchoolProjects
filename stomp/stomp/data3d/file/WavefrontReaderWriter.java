package stomp.data3d.file;

import stomp.data3d.*;
import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.text.*;
import java.awt.*;

/**
 * File I/O class for exporting to the Alias|Wavefront .OBj file format.
 * This format is a popular one that facilitates interaction between
 * Stomp3D and other 3D programs.
 */
public class WavefrontReaderWriter extends SceneReaderWriter
{
    /**
     * Read a scene using wavefront format.
     * (Not done yet)
     *
     * @param filename Name of file to write
     * @return Loaded scene.
     */
    public void read(Scene scene, String filename)
    {
        System.out.println("Cannot read Wavefront OBJ format yet.");
    }

    /**
     * Write a scene using Wavefront format.
     * (No surfacing is done yet)
     *
     * @param scene Scene to write
     * @param filenane Name of file to write.
     */
    public void write(Scene scene, String filename)
    {
        SurfaceList surfaces = scene.getSurfaceList();
        try
        {
            //Create an output stream for writing to ASCII
            FileOutputStream fos = new FileOutputStream(filename);
            PrintWriter out = new PrintWriter(fos);

            out.println("#Model written using STOMP\n");
            
            Vector vertices = scene.getVerticesVector();
            out.println("#" + vertices.size() + " vertices");
            out.println("# ObjectBegin");

            //Write the vertex list
            for(int i = 0; i < vertices.size(); i++)
            {
                out.print("v ");
                Vertex element = (Vertex)vertices.elementAt(i);
                out.print(element.x);
                out.print(" ");
                out.print(element.y);
                out.print(" ");
                out.println(element.z);
            }

            //Write the Polygon3d list
            Vector primitives = scene.getPrimitivesVector();
            out.println("#" + primitives.size() + " polygon3ds");
            out.println("g StompObject");

            String surfName = "";
            for(int i = 0; i < primitives.size(); i++)
            {
                Primitive prim = (Primitive)primitives.elementAt(i);
                if(prim instanceof Polygon3d)
                {
                    Surface surface = ((Polygon3d)prim).getSurface();
                    String currentName = surfaces.getSurfaceName(surface);
                    if(!currentName.equals(surfName))
                    {
                        surfName = currentName;
                        out.println("usemtl " + currentName);
                    }
                    int ind[];
                    ind = ((Polygon3d)prim).getIndices();
                    
                    out.print("f ");

                    //Invert vertex order.
                    for(int j = ind.length - 1; j >= 0; j--)
                    {
                        //Note: Wavefront is 1-based indexing, so we must
                        //add a 1 to each of our indices.
                        out.print("" + (ind[j]+1) + " ");
                    }
                    
//                      for(int j = 0; j < ind.length; j++)
//                      {
//                          //Note: Wavefront is 1-based indexing, so we must
//                          //add a 1 to each of our indices.
//                          out.print("" + (ind[j]+1) + " ");
//                      }
                    out.print("\n");
                }
            }

            out.flush();
            out.close();
        }
        catch(IOException e)
        {
            System.out.println("Error: Cannot write file: " + e);
        }

        String materialFile = new String("" + filename + ".mtl");
        if(filename.endsWith(".obj"))
        {
            materialFile =
                new String("" + filename.substring(0, filename.length()-4) +
                           ".mtl");
        }

        //Write material file.
        try
        {
            //Create an output stream for writing to ASCII
            FileOutputStream fos = new FileOutputStream(materialFile);
            PrintWriter out = new PrintWriter(fos);

            out.println("#Material file written using STOMP\n");
            
            Vector surfaceNames = surfaces.getSurfaceNames();

            for(int i = 0; i < surfaceNames.size(); i++)
            {
                String surfaceName = (String)surfaceNames.elementAt(i);
                Surface surface = surfaces.getSurface(surfaceName);
                Color surfaceColor = surface.getColor();
                
                out.println();
                out.println("newmtl " + surfaceName);
                out.println("  Kd " +
                                   surfaceColor.getRed()/255.0f + " " +
                                   surfaceColor.getGreen()/255.0f + " " +
                                   surfaceColor.getBlue()/255.0f);
                out.println("  Ks " +
                                   (float)surface.getSpecular() + " " +
                                   (float)surface.getSpecular() + " " +
                                   (float)surface.getSpecular());
                out.println("  Tf " +
                                   (float)surface.getTransparent() + " " +
                                   (float)surface.getTransparent() + " " +
                                   (float)surface.getTransparent());

                out.println("  Ns 0");
                out.println("  illum 2");
            }
            
            out.flush();
            out.close();
        }
        catch(IOException e)
        {
            System.out.println("Error: Cannot write file: " + e);
        }
    }
}
