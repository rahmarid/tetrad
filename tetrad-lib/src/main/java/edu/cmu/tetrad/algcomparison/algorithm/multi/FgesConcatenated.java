package edu.cmu.tetrad.algcomparison.algorithm.multi;

import edu.cmu.tetrad.algcomparison.algorithm.Algorithm;
import edu.cmu.tetrad.algcomparison.algorithm.MultiDataSetAlgorithm;
import edu.cmu.tetrad.algcomparison.independence.IndependenceWrapper;
import edu.cmu.tetrad.algcomparison.score.ScoreWrapper;
import edu.cmu.tetrad.algcomparison.utils.HasKnowledge;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.search.CcdMax;
import edu.cmu.tetrad.search.IndependenceTest;
import edu.cmu.tetrad.util.Parameters;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Requires that the parameter 'randomSelectionSize' be set to indicate how many
 * datasets should be taken at a time (randomly). This cannot given multiple values.
 *
 * @author jdramsey
 */
public class FgesConcatenated implements MultiDataSetAlgorithm, HasKnowledge {
    static final long serialVersionUID = 23L;
    private ScoreWrapper score;
    private IKnowledge knowledge = new Knowledge2();
    private IndependenceWrapper test;
    private Algorithm initialGraph = null;

    public FgesConcatenated(ScoreWrapper score) {
        this.score = score;
    }

    public FgesConcatenated(ScoreWrapper score, Algorithm initialGraph) {
        this.score = score;
        this.initialGraph = initialGraph;
    }

    @Override
    public Graph search(List<DataSet> dataSets, Parameters parameters) {
        DataSet dataSet = DataUtils.concatenate(dataSets);
        Graph initial = null;
        if (initialGraph != null) {

            initial = initialGraph.search(dataSet, parameters);
        }

        edu.cmu.tetrad.search.Fges search
                = new edu.cmu.tetrad.search.Fges(score.getScore(dataSet, parameters));
        search.setFaithfulnessAssumed(parameters.getBoolean("faithfulnessAssumed"));
        search.setKnowledge(knowledge);
        search.setVerbose(parameters.getBoolean("verbose"));
        search.setMaxDegree(parameters.getInt("maxDegree"));

        Object obj = parameters.get("printStedu.cmream");
        if (obj instanceof PrintStream) {
            search.setOut((PrintStream) obj);
        }

        if (initial != null) {
            search.setInitialGraph(initial);
        }

        return search.search();
    }

    @Override
    public Graph search(DataModel dataSet, Parameters parameters) {
        return search(Collections.singletonList(DataUtils.getContinuousDataSet(dataSet)), parameters);
    }

    @Override
    public Graph getComparisonGraph(Graph graph) {
//        Graph graph2 = new EdgeListGraph(graph.getNodes());
//
//        for (Edge edge : graph.getEdges()) {
//            List<Edge> edges = graph.getEdges(edge.getNode1(), edge.getNode2());
//            if (edges.size() == 1) {
//                graph2.addEdge(edge);
//            } else if (edges.size() == 2) {
//                graph2.addUndirectedEdge(edge.getNode1(), edge.getNode2());
//            }
//        }
//
//        return graph2;
        return new EdgeListGraph(graph);
    }

    @Override
    public String getDescription() {
        return "FGES (Fast Greedy Equivalence Search) on concatenated data using " + score.getDescription();
    }


    @Override
    public DataType getDataType() {
        return DataType.Continuous;
    }

    @Override
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add("faithfulnessAssumed");
        parameters.add("maxDegree");
        parameters.add("verbose");

        parameters.add("numRandomSelections");
        parameters.add("randomSelectionSize");

        return parameters;
    }

    @Override
    public IKnowledge getKnowledge() {
        return knowledge;
    }

    @Override
    public void setKnowledge(IKnowledge knowledge) {
        this.knowledge = knowledge;
    }
}