package it.polito.tdp.nyc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.nyc.db.NYCDao;

public class Model {
	
	private List<String> boroughs;
	private List<NTA> NTAs;
	private Graph<NTA, DefaultWeightedEdge> grafo;
	
	public List<String> getBoroughs(){
		if(this.boroughs==null) {
			NYCDao dao = new NYCDao();
			this.boroughs = dao.getAllBoroughs();
		}
		
		return boroughs;
	}
	
	public void creaGrafo(String borough) {
		NYCDao dao = new NYCDao();
		this.NTAs = dao.getNTAByBorough(borough);
		
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		Graphs.addAllVertices(grafo, NTAs);
		
		for(NTA n1: NTAs) {
			for(NTA n2: NTAs) {
				if(!n1.equals(n2)) {
					Set<String> unione = new HashSet<>(n1.getSSIDs());
					unione.addAll(n2.getSSIDs());
					Graphs.addEdge(grafo, n1, n2, unione.size());
				}
			}
		}
		
		System.out.println(grafo.vertexSet().size());
	}
	
	public List<Arco> analisiArchi() {
		
		double media = 0.0;
		for(DefaultWeightedEdge e: this.grafo.edgeSet()){
			media = media + this.grafo.getEdgeWeight(e);
		}
		media = media/this.grafo.edgeSet().size();
		
		List<Arco> result = new ArrayList<>();
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e)>media) {
				result.add(new Arco(this.grafo.getEdgeSource(e).getNTACode(),
						this.grafo.getEdgeTarget(e).getNTACode(),
						(int)this.grafo.getEdgeWeight(e)));
			}
		}
		
		Collections.sort(result);
		return result;
	}
}
