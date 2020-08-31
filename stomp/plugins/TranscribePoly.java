import stomp.command.PluginCommand;
import stomp.data3d.*;
import stomp.FastVector;
import stomp.transform.*;

import javax.vecmath.*;
import java.text.NumberFormat;

/**
 * TranscribePoly plugin.
 *
 * Makes a copy of each selected polygon and rotates it to align the
 * polygon normal with +Z axis.
 *
 * @author Tony Schreiner
 */
public class TranscribePoly extends PluginCommand
{
    /**
     * Execute the plugin.
     */
    public boolean execute()
    {
        //Make this false to see what is going on and not delete the new
        //polygons from the scene.
        boolean deleteWhenDone = true;
        
        //Make this true to move polygons to the upper right (+X, +Y)
        //quadrant, sitting flush against X=0 and Y=0.
        boolean placeInQuadrant = false;

        //For formatting the numbers so you don't get nasty scientific notation
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(4);
        numberFormat.setMinimumFractionDigits(4);

        System.out.println("================================================");
        System.out.println("Transcribing polygon coordinates");
        System.out.println("================================================");
        FastVector scenePrimitives = getScenePrimitives();
        FastVector sceneVertices = getSceneVertices();
        Vector3f zAxis = new Vector3f(0, 0, 1);

        //Loop through all the primitives
        int numPrims = scenePrimitives.sizeFast();
        for(int i = 0; i < numPrims; i++)
        {
            Primitive p = (Primitive)scenePrimitives.elementAtFast(i);

            //Only do polygons (no splines, etc.)
            if(p instanceof Polygon3d)
            {
                Polygon3d poly = (Polygon3d)p;
                int indices[] = poly.getIndices();

                //Add vertices for copy of polygon to the scene
                Vertex newVertices[] = new Vertex[indices.length];
                int newIndies[] = new int[indices.length];                
                for(int j = 0; j < indices.length; j++)
                {
                    Vertex v = (Vertex)((Vertex)sceneVertices.elementAtFast(indices[j])).clone();
                    addVertex(v);
                    newVertices[j] = v;                    
                }

                //Make the new polygon and add it to the scene.  (The polygon normal
                //computed automatically)
                Polygon3d newPoly = makePolygon(newVertices);

                //Get the polygon normal
                Vector3f v1 = newPoly.getNormal();
                Vertex center = newPoly.getCenter(sceneVertices);
                v1.normalize();
                
                //Find axis of rotation
                Vector3f axisVector = new Vector3f();
                axisVector.cross(v1, zAxis);
                axisVector.normalize();

                float angle = v1.angle(zAxis);

                //Cross product not useful at 0 or 180 degrees exactly
                //Just flip polygon at 180 degrees to be consistant
                if(Math.abs(angle - Math.PI) < .001)
                {
                    newPoly.flip();
                    newPoly.computeNormal(sceneVertices);
                }
                else if(Math.abs(angle) > .001)
                {
                    //Axis-angle rotation to orient normal with Z axis
                    Rotation rotation = new Rotation();
                    rotation.setRotation(center.x, center.y, center.z,
                                         axisVector.x, axisVector.y, axisVector.z,
                                         angle);

                    //Transform the vertices w/ rotation
                    for(int j = 0; j < indices.length; j++)
                    {
                        Vertex v = newVertices[j];
                        Vertex oldv = (Vertex)v.clone();
                        rotation.transformVertex(oldv, v);
                    }
                }

                //Place in 2nd quadrant (positive xy) so it is convenient for
                //getting coordinates for measuring
                if(placeInQuadrant)
                {
                    float minX = 1000000.0f;
                    float minY = 1000000.0f;

                    for(int j = 0; j < indices.length; j++)
                    {
                        if(newVertices[j].x < minX)
                            minX = newVertices[j].x;

                        if(newVertices[j].y < minY)
                            minY = newVertices[j].y;
                    }

                    for(int j = 0; j < indices.length; j++)
                    {
                        newVertices[j].x -= minX;
                        newVertices[j].y -= minY;
                    }
                }
                
                //Print polygon info.
                System.out.println("Polygon: " + i);
                for(int j = 0; j < indices.length; j++)
                {
                    System.out.println("  Vertex " + indices[j] + ": ( " +
                                       numberFormat.format(newVertices[j].x) + "    " +
                                       numberFormat.format(newVertices[j].y) + "    " +
                                       numberFormat.format(newVertices[j].z) + " )");
                }
                
                if(deleteWhenDone)
                {
                    //Delete added vertices.  Polygon will dissapear by itself.
                    for(int j = 0; j < indices.length; j++)
                    {
                        deleteVertex(newVertices[j]);
                    }
                }
            }   
        }
        
        return true;
    }

    /**
     * No undo needed.
     */
    public void unExecute()
    {
    }

    /**
     * Name of plugin.
     */
    public String toString()
    {
        return "TranscribePoly";
    }
}
