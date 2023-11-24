/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An implementation of Graph.
 * 
 * <p>PS2 instructions: you MUST use the provided rep.
 */
public class ConcreteVerticesGraph implements Graph<String> {
    
    private final List<Vertex> vertices = new ArrayList<>();
    
 // Abstraction function:
    //   represents a directed weighted graph as multiple vertices 
    //   connecting as source to target pairs with each pair having a
    //   weight.
    //   
    // Representation invariant:
    //   only one instance of a vertex can exist in vertices
    // Safety from rep exposure:
    //   vertices is a mutable list, so operation make defensive
    //   copies and use immutable views to avoid sharing the rep
    //   A Vertex is a mutable type, operations use defensive copies 
    //   to avoid sharing the rep
    
    public ConcreteVerticesGraph(){
    }
    private void checkRep(){        
        assert vertices().size() == vertices.size();
    }
    //helper method
    /**
     * Returns the index of a vertex in list of vertices
     * @param label the label of the vertex being searched
     * @return the index, i, of the vertex having label such that
     *         vertices.get(i).getLabel() == label, or -1 if
     *         no vertex was found
     */
    private int indexInVertices(String label){
        for(int i = 0; i < vertices.size(); i++){
            if ( vertices.get(i).getLabel().equals(label) ) {
                return i;
            }
        }
        return -1;
    }
    
    @Override public boolean add(String vertex) {
    	if ( vertices().contains(vertex) ) {
            return false;
        }
        Vertex vertexObj = new Vertex(vertex);    
        final boolean vertexAdded = vertices.add(vertexObj);
        checkRep();
        return vertexAdded;
    }
    
    @Override public int set(String source, String target, int weight) {
    	assert source != target;
        assert weight >= 0;
        
        final Vertex sourceVertex;
        final Vertex targetVertex;
        
        Set<String> verticeLabels = vertices();
        if ( verticeLabels.contains(source) ) {
            int sourceIndex = indexInVertices(source);
            sourceVertex = vertices.get(sourceIndex);
        } else {
            sourceVertex = new Vertex(source);
            vertices.add(sourceVertex);
        }
        
        if ( verticeLabels.contains(target) ) {
            int targetIndex = indexInVertices(target);
            targetVertex = vertices.get(targetIndex);
        } else {
            targetVertex = new Vertex(target);
            vertices.add(targetVertex);
        }
        
        int sourcePrevWeight = sourceVertex.setTarget(target, weight);
        int targetPrevWeight = targetVertex.setSource(source, weight);
        assert sourcePrevWeight == targetPrevWeight;
        
        checkRep();
        return sourcePrevWeight;
    }
    
    @Override public boolean remove(String vertex) {
    	if ( !( vertices().contains(vertex)) ) {
            return false;
        }
        int vertexIndex = indexInVertices(vertex);
        assert vertexIndex != -1;
        final Vertex removedVertex = vertices.remove(vertexIndex);
        assert removedVertex.getLabel() == vertex;
        
        for( Vertex v: vertices ) {
            v.remove(vertex);
        }
        return removedVertex != null;
    }
    
    @Override public Set<String> vertices() {
    	return vertices.stream()
                .map(Vertex::getLabel)
                .collect(Collectors.toSet());
    }
    
    @Override public Map<String, Integer> sources(String target) {
    	final int targetIndex = indexInVertices(target);
        if ( targetIndex < 0 ) {
            return Collections.emptyMap();
        }
        Vertex targetVertex = vertices.get(targetIndex);
        
        return Collections.unmodifiableMap(targetVertex.getSources());
    }
    
    @Override public Map<String, Integer> targets(String source) {
    	final int sourceIndex = indexInVertices(source);
        if ( sourceIndex < 0 ) {
            return Collections.emptyMap();
        }
        Vertex sourceVertex = vertices.get(sourceIndex);
        
        return Collections.unmodifiableMap(sourceVertex.getTargets());
    }
    
    /**
     * Returns a string representation of this graph.
     *  
     * A graph is made up of connected pairs of vertices, from source
     * to target. The string rep for ConcreteVerticesGraph contains 
     * all source vertices and the target vertices they each connect to:
     *      sourceVertex -> targetVertices(a Map's string rep)
     *      read as from sourceVertex to targetVertices
     * 
     * @return a string representation of this graph
     */
    @Override public String toString(){
        return vertices.stream()
                .filter(vertex -> vertex.getTargets().size() > 0)
                .map(vertex -> vertex.getLabel().toString() + " -> " + vertex.getTargets())
                .collect(Collectors.joining("\n"));
    }
    
}

/**
 * TODO specification
 * Mutable.
 * This class is internal to the rep of ConcreteVerticesGraph.
 * 
 * <p>PS2 instructions: the specification and implementation of this class is
 * up to you.
 */
class Vertex {
    private final String label;
    private final Map<String, Integer> sources = new HashMap<>();
    private final Map<String, Integer> targets = new HashMap<>();
    
    // Abstraction Function:
    //   represents a vertex in a graph that connects to other vertices as a
    //   source and/or a target. A weighted connection between 2 vertices
    //   represents an edge, and direction is represented by the connection
    //   from a source vertex to the target vertex
    //
    // Representation Invariant:
    //   A vertex label must be immutable
    //   A vertex cannot be its own source
    //   A vertex cannot be its own target
    //   All sources and targets must be distinct vertices
    //   A connection must have a weight > 0
    //
    // Safety from Exposure:
    //   All fields are  private and final
    //   label is of type L, required to be immutable by the spec
    //   sources and targets are mutable, so operations use defensive copies
    //   and immutable views to prevent sharing the rep objects with clients
    
    public Vertex(final String label){
        this.label = label;        
    }
    private void checkRep(){
        final Set<String> sourceLabels = sources.keySet();
        final Set<String> targetLabels = targets.keySet();
        
        assert !sourceLabels.contains(this.label);
        assert !targetLabels.contains(this.label);
    }
    //helper code
    private void checkInputLabel(final String inputLabel){
        assert inputLabel != null;
        assert inputLabel != this.label;
    }
    
    /** Returns the label of this vertex */
    public String getLabel(){
        return this.label;
    }
    /**
     * Adds a source connection to this vertex.
     * 
     * @param source the label of the source vertex to this target vertex
     * @param weight positive integer weight of this connection,
     *               requires weight > 0
     * @return true if source is successfully added with weight to
     *         this vertex
     */
    public boolean addSource(final String source, final int weight){
        checkInputLabel(source);
        assert weight > 0;
        
        if ( sources.putIfAbsent(source, weight) == null ){
            checkRep();
            return true;
        }
        return false;
    }
    /**
     * Adds a target connection from this vertex
     * 
     * @param target the label of the target vertex from this vertex
     * @param weight positive integer weight of this connection,
     *               requires weight > 0
     * @return true if target is successfully added with weight to
     *         this vertex
     */
    public boolean addTarget(final String target, final int weight){
        checkInputLabel(target);
        assert weight > 0;
        
        if ( targets.putIfAbsent(target, weight) == null ) {
            checkRep();
            return true;
        }
        return false;
    }

    /**
     * Removes a vertex from this vertex, if it was a source, target or both
     * 
     * @param vertex the label of the vertex being removed
     * @return the previous weight of the vertex connection to this vertex
     *         zero if no such connection exists
     */
    public int remove(final String vertex) {
        checkInputLabel(vertex);
        int sourcePrevWeight = removeSource(vertex);
        int targetPrevWeight = removeTarget(vertex);
        
        if ( sourcePrevWeight > 0 && targetPrevWeight > 0 ) {
            assert sourcePrevWeight == targetPrevWeight;
        }
        return sourcePrevWeight == 0 ? targetPrevWeight : sourcePrevWeight;
    }
    /**
     * Removes a source connection to this vertex
     * 
     * @param source the label of the source vertex being removed
     * @return the previous weight from source to this vertex,
     *         zero if no such source exists
     */
    public int removeSource(final String source){
        checkInputLabel(source);
        
        Integer previousWeight = sources.remove(source);
        
        checkRep();
        return previousWeight == null ? 0 : previousWeight;
    }
    /**
     * Removes a target connection from this vertex
     * @param target the label of the target vertex being removed
     * @return the previous weight to target from this vertex,
     *         zero if no such target exists
     */
    public int removeTarget(final String target){
        checkInputLabel(target);
        
        Integer previousWeight = targets.remove(target);
        
        checkRep();
        return previousWeight == null ? 0 : previousWeight;
    }
    /**
     * Adds, removes or updates a source connection to this vertex
     * 
     * if weight > 0, add a source if it didn't exist otherwise update
     * the weight of that source (vertex not modified if the current weight
     * is equal to the input weight)
     * if weight = 0, remove the source if it existed, otherwise this vertex
     * is not modified:
     *      add:    weight > 0, source doesn't exist
     *      remove: weight = 0, source exists
     *      update: weight > 0, source exists
     *      no change: source exists, weight = previousWeight  
     *  
     * @param source the label of the source vertex to set
     * @param weight non-negative integer to set source connection to
     * @return the previous weight from source to this vertex,
     *         zero if no such source exists
     */
    public int setSource(final String source, final int weight){
        checkInputLabel(source);
        assert weight >= 0;
        final int previousWeight;
        
        if ( weight == 0 ) {
            previousWeight = removeSource(source); 
        } else if ( addSource(source, weight) || sources.get(source) == (Integer)weight) {
            previousWeight = 0;
        } else {
            previousWeight = sources.replace(source, weight);
        }
        checkRep();
        return previousWeight;
    }
    /**
     * Adds, removes or updates a target connection from this vertex
     * 
     * if weight > 0, add a target if it didn't exist, otherwise update
     * the weight of the target (vertex not modified if the current weight
     * is equal to the input weight)
     * if weight = 0, remove the target if it existed, otherwise this vertex
     * is not modified:
     *      add:    weight > 0, target doesn't exist
     *      remove: weight = 0, target exists
     *      update: weight > 0, target exists
     * 
     * @param target the label of the target vertex to set
     * @param weight non-negative integer to set target connection to
     * @return the previous weight from source to this vertex,
     *         zero if no such target exists
     */
    public int setTarget(final String target, final int weight){
        checkInputLabel(target);
        assert weight >= 0;
        final int previousWeight;
        
        if ( weight == 0 ) {
            previousWeight = removeTarget(target);
        } else if ( addTarget(target, weight) || targets.get(target) == (Integer)weight ) {
            previousWeight = 0;
        } else {
            previousWeight = targets.replace(target, weight);
        }
        checkRep();
        return previousWeight;
    }

    /** Returns an immutable view of this vertex's sources*/
    public Map<String, Integer> getSources(){
        return Collections.unmodifiableMap(sources);
    }
    /** Returns an immutable view of this vertex's targets*/
    public Map<String, Integer> getTargets(){
        return Collections.unmodifiableMap(targets);
    }
    /**
     * Checks if a vertex is a target from this vertex
     * 
     * A vertex is a target from this vertex if this vertex is a 
     * source to that vertex, similar to this implementation:
     *      getTargets().contains(vertex)
     *      
     * @param vertex the label of the vertex being checked
     * @return true if vertex is a target from this vertex
     */
    public boolean isTarget(final String vertex){
        return targets.containsKey(vertex);
    }
    /**
     * Checks if a vertex is a source to this vertex
     * 
     * A vertex is a source to this vertex if this vertex is a 
     * target from that vertex, similar to this implementation:
     *      getSources().contains(vertex)
     *      
     * @param vertex the label of the vertex being checked
     * @return true if vertex is a source to this vertex
     */
    public boolean isSource(final String vertex){
        return sources.containsKey(vertex);
    }
    /**
     * Returns a string rep of this vertex
     * 
     * A vertex is defined by the its label, its source vertices and
     * its targets vertices. The string returned is equivalent to:
     *    this.getLabel() -> this.getTargets(a Map's string rep)
     *    this.getLabel() <- this.getSources(a Map's string rep)
     *   
     * @return string rep of this vertex, containing its label,
     *         source vertices and target vertices with their respective
     *         weights
     */
    @Override public String toString(){
        return String.format(
                "%s -> %s \n" +
                "%s <- %s",
                this.label.toString(), this.targets,
                this.label.toString(), this.sources);
    }
}