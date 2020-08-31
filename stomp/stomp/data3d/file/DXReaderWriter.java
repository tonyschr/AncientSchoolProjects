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
public class DXReaderWriter extends SceneReaderWriter
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
            
            SurfaceList surfaces = scene.getSurfaceList();
            Vector surfaceNames = surfaces.getSurfaceNames();
            FastVector vertices = scene.getVerticesVector();

            //Writer the header stuff
            out.println("xof 0302txt 0064\nHeader {\n 1;\n 0;\n 1;\n}\n");

            for(int i = 0; i < surfaceNames.size(); i++)
            {
                Surface surf = surfaces.getSurface((String)surfaceNames.elementAt(i));
                Color color = surf.getColor();
                out.println("Material " + (String)surfaceNames.elementAt(i) + " {");
                out.println(color.getRed()/255.0f + ";" +
                            color.getGreen()/255.0f + ";" +
                            color.getBlue()/255.0f + ";" +
                            (1.0 - surf.getTransparent()) + ";;");
                out.println("" + surf.getDiffuse() * 10.0 + ";");
                double spec = surf.getSpecular();
                out.println("" + spec + ";" + spec + ";" + spec + ";;");
                out.println("0.00;0.00;0.00;;");
                out.println("}");
                
            }

            out.println("Mesh StompMesh {");
          
            FastVector primitives = scene.getPrimitivesVector();       
            out.println("" + vertices.size() + ";");            
            //Write the vertex list
            for(int i = 0; i < vertices.size(); i++)
            {
                Vertex element = (Vertex)vertices.elementAtFast(i);
                out.print(-element.x);
                out.print(";");
                out.print(element.y);
                out.print(";");
                out.print(element.z);
                
                if(i < vertices.size()-1) 
                {
                    out.print(";,\n");
                }
                else
                {
                    out.print(";;\n");
                }
            }       

            out.println("");
            

            //Write the Polygon list
            out.println("" + primitives.size() + ";");
            Stomp.statusBar.startProgress("DirectX Export: Writing data file...",
                                          100.0/primitives.size());
            for(int i = 0; i < primitives.size(); i++)
            {
                Primitive prim = (Primitive)primitives.elementAtFast(i);
                if(prim instanceof Polygon3d)
                {
                    int ind[];
                    ind = ((Polygon3d)prim).getIndices();

                    out.print("" + ind.length + ";");

                    //System.out.println("ind.length = " + ind.length);
                    for(int j = 0; j < ind.length; j++)
                    {
                        //                        System.out.println("j = " + j);
                        out.print("" + ind[j]);
                        
                        if(j < ind.length - 1)
                        {
                             out.print(",");
                        }
                        else
                        {
                            out.print(";");
                        }
                    }
                    if(i < primitives.size()-1) //not the last
                    {
                        out.print(",\n");
                    }
                    else
                    {
                        out.print(";\n");
                    }
                }
                
                Stomp.statusBar.incrementProgress();
            }

            out.println("MeshMaterialList {");
            out.println("" + surfaceNames.size() + ";");
            out.println("" + primitives.size() + ";");

            for(int i = 0; i < primitives.sizeFast(); i++)
            {
                Primitive prim = (Primitive)primitives.elementAtFast(i);
                if(prim instanceof Polygon3d)
                {
                    Polygon3d p = (Polygon3d)prim;
                    Surface surf = p.getSurface();
                    int ind = surfaces.getSurfaceIndex(surf);
                    out.print("" + ind);

                    if(i < primitives.sizeFast() - 1)
                    {
                        out.println(",");
                    }
                    else
                    {
                        out.println(";;");
                    }
                }
            }
            
            for(int i = 0; i < surfaceNames.size(); i++)
            {
                Surface surf = surfaces.getSurface((String)surfaceNames.elementAt(i));
                Color color = surf.getColor();
                out.println("{" + (String)surfaceNames.elementAt(i) + "}");
            }
            
            //End
            out.println("}");
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
