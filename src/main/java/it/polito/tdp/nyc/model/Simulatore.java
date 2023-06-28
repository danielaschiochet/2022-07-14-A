package it.polito.tdp.nyc.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.nyc.model.Event.EventType;

public class Simulatore {

	// Parametri di input
	private double probShare;
	private int durationShare;

	// Stato del sistema
	private Graph<NTA, DefaultWeightedEdge> grafo;
	private Map<NTA, Integer> numShare;

	private List<NTA> vertici;

	// Output
	private Map<NTA, Integer> numTotShare;

	// Eventi
	private PriorityQueue<Event> queue;

	public Simulatore(Graph<NTA, DefaultWeightedEdge> grafo, double probShare, int durationShare) {
		super();
		this.grafo = grafo;
		this.probShare = probShare;
		this.durationShare = durationShare;
	}

	public void init() {
		this.numShare = new HashMap<NTA, Integer>();
		this.numTotShare = new HashMap<NTA, Integer>();

		for (NTA n : this.grafo.vertexSet()) {
			this.numShare.put(n, 0);
			this.numTotShare.put(n, 0);
		}

		this.vertici = new ArrayList<>(this.grafo.vertexSet());

		this.queue = new PriorityQueue<Event>();

		// creo eventi iniziali
		for (int t = 0; t < 100; t++) {
			if (Math.random() <= this.probShare) {
				int n = (int) (Math.random() * this.vertici.size());
				this.queue.add(new Event(EventType.SHARE, t, this.vertici.get(n), this.durationShare));
			}
		}
	}

	public void run() {
		while (!this.queue.isEmpty()) {
			Event e = this.queue.poll();
			if (e.getTime() >= 100)
				break;

			int time = e.getTime();
			int duration = e.getDuration();
			NTA nta = e.getNta();

			System.out.println(e.getType() + " " + time + " " + nta.getNTACode() + " " + duration);

			switch (e.getType()) {
			case SHARE:
				this.numShare.put(nta, this.numShare.get(nta) + 1);
				this.numTotShare.put(nta, this.numTotShare.get(nta) + 1);

				this.queue.add(new Event(EventType.STOP, time + duration, nta, 0));

				// ri-condivisione
				if (duration / 2 > 0) {
					NTA nuovo = trovaNTA(nta);
					if (nuovo != null) {
						this.queue.add(new Event(EventType.SHARE, time + 1, nuovo, duration / 2));
					}
				}

				break;

			case STOP:
				this.numShare.put(nta, this.numShare.get(nta) - 1);
				break;
			}
		}
	}

	private NTA trovaNTA(NTA nta) {
		int max = -1;
		NTA best = null;

		for (DefaultWeightedEdge e : this.grafo.outgoingEdgesOf(nta)) {
			NTA vicino = Graphs.getOppositeVertex(this.grafo, e, nta);
			int peso = (int) this.grafo.getEdgeWeight(e);

			if (peso > max && this.numShare.get(vicino) == 0) {
				max = peso;
				best = vicino;
			}
		}

		return best;
	}

	public Map<NTA, Integer> getNumTotShare() {
		return numTotShare;
	}

	
	
}