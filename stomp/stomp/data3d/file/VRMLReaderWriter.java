package stomp.data3d.file;

import stomp.*;
import stomp.data3d.*;
import stomp.view.*;
import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.text.*;
import java.awt.*;

import javax.vecmath.*;

/**
 * File I/O class for exporting to VRML.
 */
public class VRMLReaderWriter extends SceneReaderWriter
{
    /**
     * Read a scene using VRML gui format.
     * NO READ SUPPORT
     *
     * @param filename Name of file to write
     * @return Loaded scene.
     */
    public void read(Scene scene, String filename)
    {
    }

    /**

     * @param scene Scene to write
     * @param filenane Name of file to write.
     */
    public void write(Scene scene, String filename)
    {
        try
        {
            //Create an output stream for writing to ASCII
            FileOutputStream fos = new FileOutputStream(filename);
            PrintWriter out = new PrintWriter(fos);
            
            FastVector vertices = scene.getVerticesVector();

            //Writer the header stuff
            out.println("#VRML V1.0 ascii\n");

            FastVector primitives = scene.getPrimitivesVector();
//             for(int i = 0; i < primitives.size(); i++)
//             {
//                 if(primitives.elementAtFast(i) instanceof PointLight)
//                 {
//                     PointLight light = (PointLight)primitives.elementAtFast(i);
//                     int indices[] = light.getIndices();
//                     Vertex pos = (Vertex)vertices.elementAtFast(indices[0]);
//                     out.println("PointLight {");
//                     out.println("    on TRUE");
//                     out.println("    intensity 1");
//                     out.println("    color 1 1 1");
//                     out.println("    location " + pos.x + " " +
//                                 pos.y + " " + pos.z);
//                     out.println("}");
//                 }
//             }            

            out.println("Coordinate3 {");
            out.println("    point [ ");

            //Write the vertex list
            for(int i = 0; i < vertices.size(); i++)
            {
                Vertex element = (Vertex)vertices.elementAtFast(i);
                out.print("            ");
                out.print(element.x);
                out.print(" ");
                out.print(element.y);
                out.print(" ");
                out.print(element.z);

                if(i < vertices.size()-1) // not the last
                {
                    out.print(",\n");
                }
            }       

            out.println("          ]");
            out.println("}");

            out.println("Material {");
            out.println("    diffuseColor");
            out.println("    [");
            
            //Write a Polygon3d color list.  Crude.  Nasty.
            Stomp.statusBar.startProgress("VRML Export, step 1: Writing data file...",
                                          100.0/primitives.size());
            for(int i = 0; i < primitives.size(); i++)
            {
                Primitive prim = (Primitive)primitives.elementAtFast(i);
                if(prim instanceof Polygon3d)
                {
                    Surface surf = ((Polygon3d)prim).getSurface();
                    Color color = surf.getColor();

                    out.print("      " +
                              color.getRed()/255.0f + " " +
                              color.getGreen()/255.0f + " " +
                              color.getBlue()/255.0f);
                    
                    if(i < primitives.size()-1)
                    {
                        out.println(",");
                    }
                }
                Stomp.statusBar.incrementProgress();
            }

            out.println("    ]");
            out.println("}");
            out.println("MaterialBinding");
            out.println("{");
            out.println("    value PER_FACE");
            out.println("}");
            out.println("IndexedFaceSet");
            out.println("{");
            out.println("    coordIndex");
            out.println("    [ ");

            //Write the Polygon3d list
            //Vector primitives = scene.getPrimitivesVector();
            Stomp.statusBar.startProgress("VRML Export, step 2: Writing data file...",
                                          100.0/primitives.size());
            for(int i = 0; i < primitives.size(); i++)
            {
                Primitive prim = (Primitive)primitives.elementAtFast(i);
                if(prim instanceof Polygon3d)
                {
                    out.print("      ");
                    int ind[];
                    ind = ((Polygon3d)prim).getIndices();
                    
                    for(int j = ind.length-1; j >=0; j--)
                    {
                        out.print("" + ind[j] + ", ");
                    }

                    if(i < primitives.size()-1) //not the last
                    {
                        out.print("-1, \n");
                    }
                    else
                    {
                        out.print("-1\n");
                    }
                }
                
                Stomp.statusBar.incrementProgress();
            }
            
            //End
            out.println("    ]");
            out.println("}");

            out.flush();
            out.close();
        }
        catch(IOException e)
        {
            System.out.println("Error: Cannot write file: " + e);
        }        
    }
}
