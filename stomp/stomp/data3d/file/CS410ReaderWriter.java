package stomp.data3d.file;

import stomp.*;
import stomp.data3d.*;
import stomp.command.*;

import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.text.*;
import java.awt.*;

/**
 * File I/O class for importing and exporting for CS410.
 */
public class CS410ReaderWriter extends SceneReaderWriter
{
    /**
     */
    public void read(Scene scene, String filename)
    {
        int existingVert = scene.numVertices();
        
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
            Hashtable groupHash = new Hashtable();

            //Read the object and create a scene.
            StringTokenizer tokens = new StringTokenizer(sb.toString(),
                                                         "\n\r\t, ");
            NumberFormat numformat = NumberFormat.getInstance();
            
            int numVertices;
            numVertices = numformat.parse(tokens.nextToken()).intValue();

            Stomp.statusBar.startProgress("Loading CS410 Scene, step 1...",
                                          100.0/numVertices);
            for(int i = 0; i < numVertices; i++)
            {
                float x, y, z;
                x = numformat.parse(tokens.nextToken()).floatValue();
                y = numformat.parse(tokens.nextToken()).floatValue();
                z = numformat.parse(tokens.nextToken()).floatValue();

                Vertex v = new Vertex(x, y, z);
                scene.addVertex(v);
                Stomp.statusBar.incrementProgress();
            }

            int numSurfaces = numformat.parse(tokens.nextToken()).intValue();
            for(int i = 0; i < numSurfaces; i++)
            {
                String surfName = tokens.nextToken();
                int r = numformat.parse(tokens.nextToken()).intValue();
                int g = numformat.parse(tokens.nextToken()).intValue();
                int b = numformat.parse(tokens.nextToken()).intValue();
                float diff = numformat.parse(tokens.nextToken()).floatValue();
                float spec = numformat.parse(tokens.nextToken()).floatValue();

                Surface surf = new Surface(new Color(r, g, b),
                                           diff, spec, 0, 0);
                surfaces.addSurface(surfName, surf);
            }
            
            int numGroups = numformat.parse(tokens.nextToken()).intValue();
            for(int gr = 0; gr < numGroups; gr++)
            {
                String groupName = tokens.nextToken();
                String parentGroup = tokens.nextToken();
                for(int j = 0; j < 16; j++)
                {
                    String dummy = tokens.nextToken();
                }
            }
            
            int numPrims = numformat.parse(tokens.nextToken()).intValue();
            Stomp.statusBar.startProgress("Loading CS410 Scene, step 2...",
                                          100.0/numPrims);
            for(int p = 0; p < numPrims; p++)
            {
                String groupName = tokens.nextToken();
                String surfName = tokens.nextToken();
                
                int numIndices = numformat.parse(tokens.nextToken()).intValue();
                int indices[] = new int[numIndices];
                for(int i = 0; i < numIndices; i++)
                {
                    indices[i] = numformat.parse(tokens.nextToken()).intValue();
                    indices[i] += existingVert;
                }

                Group group = null;

//                  if(build <= -170)
//                  {
//                      String groupName = tokens.nextToken();
//                      if(!groupName.equals("nullgroup"))
//                      {
//                          group = (Group)groupHash.get(groupName);
//                      }
//                  }

                //                System.out.println("Group = " + group);
                
                Polygon3d plane = new Polygon3d(indices);
                plane.setSurface(surfaces.getSurface(surfName));
                Surface surfa = plane.getSurface();
                scene.addPrimitive(plane);

                Stomp.statusBar.incrementProgress();
            }
            
            scene.validateScene();
            scene.updateViews();
            
            //return scene;
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
     * Write a scene using CS410 format.
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
            
            SurfaceList surfaceList = scene.getSurfaceList();
            FastVector vertices = scene.getVerticesVector();
            
            NumberFormat numformat = NumberFormat.getInstance();
            numformat.setMaximumFractionDigits(8);

            //Write the vertex list
            out.println("" + vertices.size());
            for(int i = 0; i < vertices.size(); i++)
            {
                Vertex element = (Vertex)vertices.elementAtFast(i);            

                out.print(numformat.format(element.x));
                out.print(" ");
                out.print(numformat.format(element.y));
                out.print(" ");
                out.println(numformat.format(element.z));
            }

            //Write the material list
            Vector surfaceNames = surfaceList.getSurfaceNames();
            out.println("\n" + surfaceNames.size());
            for(int i = 0; i < surfaceNames.size(); i++)
            {
                String name = (String)surfaceNames.elementAt(i);
                Surface surf = surfaceList.getSurface(name);
                Color color = surf.getColor();

                out.println("" + name + " " +
                            color.getRed() + " " +
                            color.getGreen() + " " +
                            color.getBlue() + " " +
                            numformat.format(surf.getDiffuse()) + " " +
                            numformat.format(surf.getSpecular()));
            }

            FastVector primitives = scene.getPrimitivesVector();
            FastVector groups = new FastVector();

            //Get all the groups and add them to a vector so we can
            //count them.
            for(int i = 0; i < primitives.sizeFast(); i++)
            {
                if(primitives.elementAtFast(i) instanceof Group)
                {
                    groups.addElement((Group)primitives.elementAtFast(i));
                }
            }

            //Write the group list.
            out.println("\n" + groups.sizeFast());
            for(int i = 0; i < groups.sizeFast(); i++)
            {
                Group group = (Group)groups.elementAtFast(i);
                out.print("" + group.getName() + " ");
                if(group.getGroup() == null)
                {
                    out.println("nullgroup");
                }
                else
                {
                    out.println("" + group.getGroup().getName());
                }
                out.println("  1 0 0 0");
                out.println("  0 1 0 0");
                out.println("  0 0 1 0");
                out.println("  0 0 0 1");
            }

            //Count the number of polygons in the scene.  Ignore all other
            //types of primitives!
            int numPolygons = 0;
            for(int i = 0; i < primitives.sizeFast(); i++)
            {
                if(primitives.elementAtFast(i) instanceof Polygon3d)
                {
                    numPolygons++;
                }
            }
            
            //Write the polygon list
            out.println("\n" + numPolygons);
                                    
            for(int i = 0; i < primitives.sizeFast(); i++)
            {
                if(primitives.elementAtFast(i) instanceof Polygon3d)
                {
                    Polygon3d poly = (Polygon3d)primitives.elementAtFast(i);
                    int indices[] = poly.getIndices();
                    Group group = poly.getGroup();
                    Surface surf = poly.getSurface();

                    if(group == null)
                    {
                        out.print("nullgroup ");
                    }
                    else
                    {
                        out.print("" + group.getName() + " ");
                    }

                    out.print("" + surfaceList.getSurfaceName(surf) +
                              " " + indices.length + "  ");

                    for(int ind = 0; ind < indices.length; ind++)
                    {
                        out.print("" + indices[ind] + " ");
                    }

                    out.println("");
                }
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
