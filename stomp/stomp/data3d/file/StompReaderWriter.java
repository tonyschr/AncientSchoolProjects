package stomp.data3d.file;

import stomp.data3d.*;
import stomp.command.*;
import stomp.*;
import stomp.view.*;

import java.io.*;
import java.util.zip.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import java.math.BigDecimal;

/**
 * File I/O for native STOMP format.
 */
public class StompReaderWriter extends SceneReaderWriter
{
    /**
     * Read a scene using serialization.
     *
     * @param filename Name of file to write
     * @return Loaded scene.
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

            //It's not numVertices at all, it's the version tag!
            int build = -100;
            if(numVertices < 0)
            {
                build = numVertices;
                numVertices = numformat.parse(tokens.nextToken()).intValue();
            }
            
            Stomp.statusBar.startProgress("Loading Stomp Scene, step 1...",
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

            if(build <= -170)
            {
                int numGroups = numformat.parse(tokens.nextToken()).intValue();
                for(int gr = 0; gr < numGroups; gr++)
                {
                    String groupName = tokens.nextToken();
                    int r = numformat.parse(tokens.nextToken()).intValue();
                    int g = numformat.parse(tokens.nextToken()).intValue();
                    int b = numformat.parse(tokens.nextToken()).intValue();
//                     System.out.println("Read group: " + groupName +
//                                        " r = " + r + "  g = " + g +
//                                        "  b = " + b);
                    Group group = new Group(groupName, new Color(r, g, b));
                    groupHash.put(groupName, group);
                    scene.addPrimitive(group);
                }
            }
            
            int numPrims = numformat.parse(tokens.nextToken()).intValue();
            Stomp.statusBar.startProgress("Loading Stomp Scene, step 2...",
                                          100.0/numPrims);
            for(int p = 0; p < numPrims; p++)
            {
                int numIndices = numformat.parse(tokens.nextToken()).intValue();
                int indices[] = new int[numIndices];
                for(int i = 0; i < numIndices; i++)
                {
                    indices[i] = numformat.parse(tokens.nextToken()).intValue();
                    indices[i] += existingVert;
                }

                Group group = null;

                if(build <= -170)
                {
                    String groupName = tokens.nextToken();
                    if(!groupName.equals("nullgroup"))
                    {
                        group = (Group)groupHash.get(groupName);
                    }
                }

                //                System.out.println("Group = " + group);
                
                String type = tokens.nextToken();
                if(type.equals("p"))
                {
                    String surfName = tokens.nextToken();
                    int r = numformat.parse(tokens.nextToken()).intValue();
                    int g = numformat.parse(tokens.nextToken()).intValue();
                    int b = numformat.parse(tokens.nextToken()).intValue();
                    float diff = numformat.parse(tokens.nextToken()).floatValue();
                    float spec = numformat.parse(tokens.nextToken()).floatValue();
                    float ref = numformat.parse(tokens.nextToken()).floatValue();
                    float trans = numformat.parse(tokens.nextToken()).floatValue();
                    float smooth = 0;
                    if(build < -100)
                    {
                        smooth =
                            numformat.parse(tokens.nextToken()).intValue();
                    }
                    
                    Surface surf = new Surface();
                    surf.setColor(new Color(r, g, b));
                    surf.setDiffuse(diff);
                    surf.setSpecular(spec);
                    surf.setReflect(ref);
                    surf.setTransparent(trans);
                    surf.setSmooth(smooth == 1);
                    surfaces.addSurface(surfName, surf);
                    
                    Polygon3d plane = new Polygon3d(indices);
                    plane.setSurface(surfaces.getSurface(surfName));
                    Surface surfa = plane.getSurface();
                    scene.addPrimitive(plane);
                    if(group != null)
                    {
                        group.add(plane);
                    }
                }
                else if(type.equals("s"))
                {
                    Spline s = new Spline(indices);
                    scene.addPrimitive(s);
                    if(group != null)
                    {
                        group.add(s);
                    }
                }
                else if(type.equals("n"))
                {
                    int numSplines =
                        numformat.parse(tokens.nextToken()).intValue();
                    int indSplines =
                        numformat.parse(tokens.nextToken()).intValue();

                    Spline splines[] = new Spline[numSplines];

                    for(int s = 0; s < numSplines; s++)
                    {
                        int splineIndices[] = new int[indSplines];
                        for(int ind = 0; ind < indSplines; ind++)
                        {
                            splineIndices[ind] = indices[ind * numSplines + s];
                        }
                        splines[s] = new Spline(splineIndices);
                    }

                    SplineSurface surf = new SplineSurface(splines);
                    scene.addPrimitive(surf);
                    if(group != null)
                    {
                        group.add(surf);
                    }
                }
                else if(type.equals("c"))
                {
                    if(stomp.command.AddCameraCommand.numCameras() == 0)
                    {
                        FastVector vertices = scene.getVerticesVector();
                        Vertex vertex = (Vertex)vertices.elementAtFast(indices[0]);
                        Vertex lookat = (Vertex)vertices.elementAtFast(indices[1]);
                        
                        CameraView cameraView = new CameraView(scene, vertex,
                                                               lookat);
                        cameraView.setIndices(indices);
                        scene.addPrimitive(cameraView);
                        if(group != null)
                        {
                            group.add(cameraView);
                        }
                    }
                }
                else if(type.equals("l"))
                {
                    String lightType = tokens.nextToken();
                    if(lightType.equals("point"))
                    {
                        PointLight light = new PointLight(indices);
                        scene.addPrimitive(light);
                        if(group != null)
                        {
                            group.add(light);
                        }
                    }
                }
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
     * Write a scene using serialization.
     *
     * @param scene Scene to write
     * @param filenane Name of file to write.
     */
    public void write(Scene scene, String filename)
    {
        try
        {
            //Create compressed output stream.
            FileOutputStream fos = new FileOutputStream(filename);
            //GZIPOutputStream gzos = new GZIPOutputStream(fos);
            PrintWriter out = new PrintWriter(fos);

            SurfaceList surfaceList = scene.getSurfaceList();
            
            //Write the version number
            out.println("-" + Stomp.BUILD + "\n");

            FastVector vertices = scene.getVerticesVector();
            out.println(vertices.size());

            NumberFormat numformat = NumberFormat.getInstance();
            numformat.setMaximumFractionDigits(8);

            //Write the vertex list
            Stomp.statusBar.startProgress("Saving Stomp Scene, step 1...",
                                          100.0/vertices.size());
            for(int i = 0; i < vertices.size(); i++)
            {
                Vertex element = (Vertex)vertices.elementAtFast(i);            

                out.print(numformat.format(element.x));
                out.print(", ");
                out.print(numformat.format(element.y));
                out.print(", ");
                out.println(numformat.format(element.z));
                Stomp.statusBar.incrementProgress();
            }

            //Write the group list
            int numGroups = 0;
            FastVector primitives = scene.getPrimitivesVector();
            for(int i = 0; i < primitives.sizeFast(); i++)
            {
                Primitive prim = (Primitive)primitives.elementAtFast(i);
                if(prim instanceof Group)
                {
                    numGroups++;
                }
            }

            out.println("" + numGroups);
            
            for(int i = 0; i < primitives.sizeFast(); i++)
            {
                Primitive prim = (Primitive)primitives.elementAtFast(i);
                if(prim instanceof Group)
                {
                    Group group = (Group)prim;
                    Color color = group.getColor();
                    out.println("" + group.getName() +
                                " " + color.getRed() +
                                " " + color.getGreen() +
                                " " + color.getBlue());
                }
            }
            
            //Write the Polygon3d list
            Stomp.statusBar.startProgress("Saving Stomp Scene, step 2...",
                                          100.0/primitives.size());
            out.println("" + primitives.size());
            for(int i = 0; i < primitives.size(); i++)
            {
                Primitive prim = (Primitive)primitives.elementAtFast(i);

                //Write indices (all primitives have this in common)
                int ind[];
                ind = prim.getIndices();
                out.print("" + ind.length + " ");
                for(int j = 0; j < ind.length; j++)
                {
                    out.print("" + ind[j] + " ");
                }

                Group group = prim.getGroup();
                if(group == null)
                {
                    out.print("nullgroup ");
                }
                else
                {
                    out.print("" + group.getName() + " ");
                }
                
                if(prim instanceof Polygon3d)
                {
                    out.print("p "); //indicates a Polygon3d
                    Surface surf = ((Polygon3d)prim).getSurface();
                    Color col = surf.getColor();
                    String surfaceName = surfaceList.getSurfaceName(surf);
                    int smooth = 0;
                    if(surf.isSmooth())
                    {
                        smooth = 1;
                    }
                    out.print("" + surfaceName + " " +
                              col.getRed() + " " +
                              col.getGreen() + " " +
                              col.getBlue() + " " +
                              surf.getDiffuse() + " " +
                              surf.getSpecular() + " " +
                              surf.getReflect() + " " +
                              surf.getTransparent() + " " +
                              smooth);
                    
                }
                else if(prim instanceof Spline)
                {
                    out.print("s ");
                }
                else if(prim instanceof SplineSurface)
                {
                    out.print("n ");
                    Spline splines[] = ((SplineSurface)prim).getSplines();
                    int tempInd[] = splines[0].getIndices();
                    out.print("" + splines.length + " "); //number of splines
                    out.print("" + tempInd.length + " "); //indices per spline
                }
                else if(prim instanceof CameraView)
                {
                    out.print("c ");
                }
                else if(prim instanceof Light)
                {
                    out.print("l ");
                    if(prim instanceof PointLight)
                    {
                        out.print("point ");
                    }
                }
                else if(prim instanceof Group)
                {
                    out.println("gr");
                }
                out.print("\n");
                Stomp.statusBar.incrementProgress();
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

