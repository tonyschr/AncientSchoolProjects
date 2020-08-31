package stomp.data3d.file;

import stomp.data3d.*;
import stomp.command.*;

import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.text.*;
import java.awt.*;

/**
 * File I/O class for importing and exporting to the CS510 GUI
 * used for the raytracer.
 */
public class CS510ReaderWriter extends SceneReaderWriter
{
    /**
     * Read a scene using CS510 gui format.
     * (Very slow implementation)
     *
     * @param filename Name of file to write
     * @return Loaded scene.
     */
    public void read(Scene scene, String filename)
    {
        CommandExecutor.clear();
        scene.removeAllContents();
        
        try
            {
            //Create compressed object input stream.
            FileReader reader = new FileReader(filename);
            BufferedReader in = new BufferedReader(reader);
            StringBuffer sb = new StringBuffer();

            int c;
            c = in.read();
            while(c != -1)
            {
                sb.append((char)c);
                c = in.read();
            }

            SurfaceList surfaces = scene.getSurfaceList();

            //Read the object and create a scene.
            StringTokenizer tokens = new StringTokenizer(sb.toString(),
                                                         "\n\r\t, ");
            NumberFormat numformat = NumberFormat.getInstance();
            
            int numVertices;
            numVertices = numformat.parse(tokens.nextToken()).intValue();

            for(int i = 0; i < numVertices; i++)
            {
                float x, y, z;
                x = numformat.parse(tokens.nextToken()).floatValue();
                y = numformat.parse(tokens.nextToken()).floatValue();
                z = numformat.parse(tokens.nextToken()).floatValue();

                Vertex v = new Vertex(x, y, z);
                scene.addVertex(v);
            }

            int numPlanes;
            numPlanes = numformat.parse(tokens.nextToken()).intValue();

            for(int i = 0; i < numPlanes; i++)
            {
                int numIndices;
                int r, g, b;
                numIndices = numformat.parse(tokens.nextToken()).intValue();
                r = numformat.parse(tokens.nextToken()).intValue();
                g = numformat.parse(tokens.nextToken()).intValue();
                b = numformat.parse(tokens.nextToken()).intValue();

                Surface surf = new Surface();
                surf.setColor(new Color(r, g, b));
                surfaces.addSurface("S " + r + ", " + g + " " + b, surf);
                
                int indices[] = new int[numIndices];
                for(int j = numIndices-1; j >= 0; j--)
                {
                    indices[j] = numformat.parse(tokens.nextToken()).intValue();
                }

                Polygon3d p = new Polygon3d(indices);
                p.setSurface(surfaces.getSurface("S " + r + ", " + g + " " + b));
                scene.addPrimitive(p);
            }
            
            scene.validateScene();
            scene.updateViews();
        }
        catch(IOException e)
        {
            System.out.println("Error reading file: " + e);
        }
        catch(ParseException e)
        {
            System.out.println("Error parsing scene file: " + e);
        }
    }

    /**
     * Write a scene using CS510 gui format.
     *
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
            
            Vector vertices = scene.getVerticesVector();
            out.println(vertices.size());

            //Write the vertex list
            for(int i = 0; i < vertices.size(); i++)
            {
                Vertex element = (Vertex)vertices.elementAt(i);
                out.print(element.x);
                out.print(", ");
                out.print(element.y);
                out.print(", ");
                out.println(element.z);
            }

            //Write the plane list
            Vector primitives = scene.getPrimitivesVector();
            out.println(primitives.size());
            for(int i = 0; i < primitives.size(); i++)
            {
                Primitive prim = (Primitive)primitives.elementAt(i);
                if(prim instanceof Polygon3d)
                {
                    int ind[];
                    ind = ((Polygon3d)prim).getIndices();
                    out.print("" + ind.length + " ");
                    Surface surf = ((Polygon3d)prim).getSurface();
                    Color col = surf.getColor();
                    out.print("" + col.getRed() + " " +
                              col.getGreen() + " " +
                              col.getBlue() + "  ");
                    for(int j = ind.length-1; j >= 0; j--)
                    {
                        out.print("" + ind[j] + " ");
                    }
                    out.print("\n");
                }
            }

            //write 0 spheres
            out.println("0");
            out.flush();
            out.close();
        }
        catch(IOException e)
        {
            System.out.println("Error: Cannot write file: " + e);
        }
    }
}
