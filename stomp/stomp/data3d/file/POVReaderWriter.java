package stomp.data3d.file;

import stomp.*;
import stomp.data3d.*;
import stomp.command.*;
import stomp.view.CameraView;

import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.text.*;
import java.awt.*;

import javax.vecmath.*;

/**
 * File I/O class for exporting to the POV raytracer.
 */
public class POVReaderWriter extends SceneReaderWriter
{
    /**
     * Read a scene using POV gui format.
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
        DeselectAllCommand deselectAll = new DeselectAllCommand(scene);
        deselectAll.execute();
        CameraView camera = null;
        //Scene scene = (Scene)s.clone();
        FastVector lights = new FastVector();
        FastVector groups = new FastVector();
        
        FastVector allPrims = scene.getPrimitivesVector();
        for(int i = 0; i < allPrims.sizeFast(); i++)
        {
            Primitive prim = (Primitive)allPrims.elementAtFast(i);
            
            scene.select(prim);
            if(prim instanceof CameraView)
            {
                camera = (CameraView)prim;
            }
            else if(prim instanceof Light)
            {
                lights.addElement(prim);
            }
            else if(prim instanceof Group)
            {
                groups.addElement(prim);
            }
        }
        TriangulateCommand triangulate = new TriangulateCommand(scene);
        triangulate.execute();
        
        try
        {
            if(filename.endsWith(".pov"))
            {
                filename = filename.substring(0, filename.length()-4);
                if(filename.length() < 1)
                {
                    return;
                }
            }

            //Get base path of the file for writing the .inc files
            File outFile = new File(filename);
            String pureFile = outFile.getName();
            String filePath = filename.substring(0, filename.length() - pureFile.length());
            
            //Create an output stream for writing to ASCII
            FileOutputStream fos = new FileOutputStream(filename + ".pov");
            PrintWriter out = new PrintWriter(fos);
            FileOutputStream fosSurf = new FileOutputStream(filename +
                                                            "Surfaces.inc");
            PrintWriter outSurf = new PrintWriter(fosSurf);
            
            FastVector sceneVertices = scene.getVerticesVector();
            FastVector prims = scene.getPrimitivesVector();
            SurfaceList surfaces = scene.getSurfaceList();
            Vector surfaceNames = surfaces.getSurfaceNames();

            FastVector vertices = new FastVector();

            //flip all vertices along the y axis
            Matrix4f trans = new Matrix4f();
            trans.setIdentity();
            trans.m22 = -1;
            for(int i = 0; i < sceneVertices.sizeFast(); i++)
            {
                Vertex v = new Vertex();
                trans.transform((Vertex)sceneVertices.elementAtFast(i),
                                v);
                vertices.addElement(v);
            }
            
            out.println("#global_settings { assumed_gamma 2.2 }");
            out.println("");
            out.println("#include \"colors.inc\"");
            out.println("#include \"shapes.inc\"");
            out.println("#include \"" + pureFile + "Surfaces.inc\"" +
                        "//surfaces defined by you in Stomp3d");
            out.println("");
            out.println("//Include meshes that were put into groups");
            for(int groupInc = 0; groupInc < groups.sizeFast(); groupInc++)
            {
                Group gr = (Group)groups.elementAtFast(groupInc);
                out.println("#include \"" + gr.getName() + ".inc\"");
            }
            out.println("");
            out.println("camera {");

            if(camera != null)
            {
                Point3f pos = camera.getPosition();
                Point3f lookat = camera.getLookatPoint();
                out.println("  location <" + pos.x + ", " +
                            pos.y + ", " + -pos.z + ">");
                out.println("  right <4/3 0, 0>");
                out.println("  up <0, 1, 0>");
                out.println("  sky <0, 1, 0>");
                out.println("  angle " + stomp.gui.RenderDialog.FOV);
                out.println("  look_at <" + lookat.x + ", " +
                            lookat.y + ", " + -lookat.z + ">");
            }
            else
            {
                out.println("  location <4, 5, -10>");
                out.println("  right <4/3 0, 0>");
                out.println("  up <0, 1, 0>");
                out.println("  sky <0, 1, 0>");
                out.println("  angle 20");
                out.println("  look_at <0, 0, 0>");
            }
            
            out.println("}\n");

            if(lights.sizeFast() > 0)
            {
                for(int i = 0; i < lights.sizeFast(); i++)
                {
                    Light l = (Light)lights.elementAtFast(i);
                    if(l instanceof PointLight)
                    {
                        int indices[] = l.getIndices();
                        Vertex pos = (Vertex)sceneVertices.elementAtFast(indices[0]);
                        out.println("light_source { <" + pos.x + ", " +
                                    pos.y + ", " + pos.z + "> color White }");
                    }
                }
            }
            else //default light
            {
                out.println("light_source { <20, 15, -7> color White }");
            }
            
            out.println("background { color Black }\n");
            
            for(int i = 0; i < surfaceNames.size(); i++)
            {
                Surface surf = surfaces.getSurface((String)surfaceNames.elementAt(i));
                Color color = surf.getColor();
                outSurf.println("#declare " +
                                (String)surfaceNames.elementAt(i) +
                                " = texture {");
                outSurf.println("  pigment { color rgbt <" +
                                color.getRed()/255.0f + ", " +
                                color.getGreen()/255.0f + ", " +
                                color.getBlue()/255.0f + ", " +
                                (1 - surf.getTransparent()) + "> }");
                outSurf.println("  finish {");
                outSurf.println("    diffuse " + surf.getDiffuse());
                outSurf.println("    specular " + surf.getSpecular());
                outSurf.println("    reflection " + surf.getReflect());
                if(surf.getTransparent() > 0)
                {
                    outSurf.println("    refraction 1");
                    outSurf.println("    ior 1.1");
                }
                outSurf.println("  }");
                outSurf.println("}\n");
                
            }

            //For each vertex, find the vertex normal.
            Stomp.statusBar.startProgress("POV Export, step 1: Calculating Vertex Normals...",
                                          100.0/vertices.size());
            for(int i = 0; i < prims.size(); i++)
            {
                if(prims.elementAtFast(i) instanceof Polygon3d)
                {
                    ((Polygon3d)prims.elementAt(i)).computeNormal(vertices);
                }
            }
            
            Vector3f vertexNormals[] = new Vector3f[vertices.size()];
            int num;
            for(int i = 0; i < vertices.size(); i++)
            {
                num = 0;
                vertexNormals[i] = new Vector3f();
                for(int j = 0; j < prims.size(); j++)
                {
                    if(prims.elementAtFast(j) instanceof Polygon3d)
                    {
                        Polygon3d p = (Polygon3d)prims.elementAtFast(j);
                        Surface surf = p.getSurface();

                        if(surf.isSmooth() && p.containsIndex(i))
                        {
                            vertexNormals[i].add(p.getNormal());
                            num++;
                        }
                    }
                }
                if(num > 0)
                {
                    vertexNormals[i].x = vertexNormals[i].x/(float)num;
                    vertexNormals[i].y = vertexNormals[i].y/(float)num;
                    vertexNormals[i].z = vertexNormals[i].z/(float)num;
                }
                Stomp.statusBar.incrementProgress();
            }

            out.println("//Add objects to scene from .inc files");
            for(int groupInc = 0; groupInc < groups.sizeFast(); groupInc++)
            {
                Group gr = (Group)groups.elementAtFast(groupInc);
                out.println("object { " + gr.getName() + " }");
            }

            out.println("");
            
            Stomp.statusBar.startProgress("POV Export, step 2: Writing Main Data File...",
                                          100.0/prims.size());
            for(int i = 0; i < prims.size(); i++)
            {
                if(prims.elementAtFast(i) instanceof Polygon3d)
                {
                    Polygon3d plane = (Polygon3d)prims.elementAtFast(i);
                    int indices[] = plane.getIndices();
                    //Only save polygon3ds in no group at this time.
                    //Only save triangles!!!
                    if(plane.getGroup() == null && indices.length == 3)
                    {
                        Surface surf = plane.getSurface();

                        writeTriangle(out, vertices, vertexNormals,
                                      indices, surfaces, surf); 
                    }
                    Stomp.statusBar.incrementProgress();
                }
            }
            
            Stomp.statusBar.startProgress("POV Export, step 3: Saving group .INC files...", 100.0/groups.size());
            for(int g = 0; g < groups.sizeFast(); g++)
            {
                Group group = (Group)groups.elementAtFast(g);
                String name = filePath + File.separatorChar +
                    group.getName() + ".inc";
                if(filePath.length() < 1)
                {
                    name = group.getName() + ".inc";
                }
                FileOutputStream fos2 = new FileOutputStream(name);
                PrintWriter grOut = new PrintWriter(fos2);
                grOut.println("#include \"" + pureFile + "Surfaces.inc\"");
                grOut.println("");
                grOut.println("#declare " + group.getName() + " = mesh {");
                for(int i = 0; i < prims.sizeFast(); i++)
                {
                    if(prims.elementAtFast(i) instanceof Polygon3d)
                    {
                        Polygon3d plane = (Polygon3d)prims.elementAtFast(i);
                        int indices[] = plane.getIndices();
                        //Only save polygon3ds in no group at this time.
                        //Only save triangles!!!
                        if(plane.getGroup() == group && indices.length == 3)
                        {
                            Surface surf = plane.getSurface();

                            writeTriangle(grOut, vertices, vertexNormals,
                                          indices, surfaces, surf); 
                        }
                    }
                }
                Stomp.statusBar.incrementProgress();
                grOut.println("}");
                grOut.flush();
                grOut.close();
            }
            
            //out.println("}\n");
            //out.println("object { " + filename + " }");
            triangulate.unExecute();
            for(int i = 0; i < allPrims.sizeFast(); i++)
            {
                scene.deselect((Primitive)allPrims.elementAtFast(i));
            }
            deselectAll.unExecute();

            outSurf.flush();
            outSurf.close();
            out.flush();
            out.close();
        }
        catch(IOException e)
        {
            System.out.println("Error: Cannot write file: " + e);
        }
        
        //triangulate.unExecute();
    }
    
    private void writeTriangle(PrintWriter out, FastVector vertices,
                               Vector3f vertexNormals[], int indices[],
                               SurfaceList surfaces, Surface surf)
    {
        if(surf.isSmooth())
        {   
            out.println("  smooth_triangle {");
            
            for(int j = 0; j < 3; j++)
            {
                Vertex v = (Vertex)vertices.elementAtFast(indices[j]);
                out.print("    <" + v.x + ", " +
                          v.y + ", " +
                          v.z + ">, " +
                          "<" + vertexNormals[indices[j]].x + ", " +
                          vertexNormals[indices[j]].y + ", " +
                          vertexNormals[indices[j]].z + ">");
                if(j < 2)
                {
                    out.print(",");
                }
                out.println("");
            }
        }
        else
        {
            out.println("  triangle {");
            
            for(int j = 0; j < 3; j++)
            {
                Vertex v = (Vertex)vertices.elementAtFast(indices[j]);
                out.print("    <" + v.x + ", " +
                          v.y + ", " +
                          v.z + ">");
                if(j < 2)
                {
                    out.print(",");
                }
                out.println("");
            }
        }
        
        out.println("    texture { " +
                    surfaces.getSurfaceName(surf) + " }");
        out.println("  }");
    }                                       
}
