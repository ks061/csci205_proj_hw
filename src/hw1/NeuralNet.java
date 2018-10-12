/* *****************************************
 * CSCI205 - Software Engineering and Design
 * Fall 2018
 *
 * Name: Logan Stiles and Kartikeya Sharma
 * Date: Oct 10, 2018
 * Time: 5:00:00 PM
 *
 * Project: 205-FA18Class
 * Package: hw01
 * File: NeuralNet
 * Description: Represents an artificial neural net
 *
 * ****************************************
 */
package hw1;

import java.util.*;

/**
 *
 * @author cld028, ks061
 */
public class NeuralNet {

    private double[][] data;
    private ConfigObject configuration;
    private ArrayList<Layer> layers;
    private double[] errors;

    protected static double alpha = 0.2;

    /**
     * A default learning rate if you decide to use this within the neurons
     */
    public final static double DEFAULTALPHA = 0.2;

    /**
     * Explicit constructor that creates the input layer, any hidden layers, and
     * output layer, along with creating connections among the layers.
     *
     * @author cld028, ks061, lts010
     */
    NeuralNet(double[][] data, ConfigObject config) {
        if (data.length == 0) {
            throw new NeuralNetConstructionException(
                    "No data has been provided.");
        }

        int dataLength = data[0].length;
        for (double[] inputSet : data) {
            // TODO remove
//            System.out.println(Arrays.toString(inputSet));
            if (inputSet.length != dataLength) {
                throw new NeuralNetConstructionException(
                        "Not all rows in data set have the same amount of entries.");
            }
        }
        this.data = data;
        this.configuration = config;

        layers = new ArrayList<>();
        InputLayer inputLayer = new InputLayer(config.getNumInputs(), "I1-",
                                               0,
                                               this);
        layers.add(inputLayer);
        for (int i = 1; i < this.configuration.getNumHiddenLayers() + 1; i++) {
            layers.add(new HiddenLayer(
                    this.configuration.getNumNeuronsPerHiddenLayer(),
                    "H" + i + "-",
                    i, this));
        }

        OutputLayer outputLayer = new OutputLayer(config.getNumOutputs(),
                                                  "O1-",
                                                  layers.size(), this);
        layers.add(outputLayer);
        Iterator it = layers.iterator();
        Layer currentLayer = (Layer) it.next();
        Layer nextLayer;
        while (it.hasNext()) {
            nextLayer = (Layer) it.next();
            currentLayer.connectLayer(nextLayer);
            currentLayer = nextLayer;

        }

        System.out.println("initial weights are:  ");
        for (ArrayList<Double> layer : config.getWeights()) {
            System.out.println(layer);
        }

        do {
            for (double[] inputOutputSet : this.data) {
                inputLayer.setInputs(
                        Arrays.copyOfRange(inputOutputSet, 0,
                                           config.getNumInputs()));
                outputLayer.setTargetOutputs(Arrays.copyOfRange(
                        inputOutputSet,
                        config.getNumInputs(),
                        inputOutputSet.length));
                inputLayer.fireNeurons();
                System.out.println(
                        "x_1: " + inputLayer.getNeurons().get(0).getNetValue());
                System.out.println(
                        "x_2: " + inputLayer.getNeurons().get(1).getNetValue());
                System.out.println(
                        "h_1: " + this.layers.get(1).getNeurons().get(0).getNetValue());
                System.out.println(
                        "h_2: " + this.layers.get(1).getNeurons().get(1).getNetValue());
                System.out.println(
                        "y_1: " + outputLayer.getNeurons().get(0).getNetValue());
                System.out.println(outputLayer.calculateSumOfSquaredErrors());
                // Read output layer
                // Back propogate
                // etc.
            }
        } while (false);
        // TODO: change while back to below line
        // outputLayer.calculateSumOfSquaredErrors() > config.getHighestSSE();

        // } while (i < 10);
        //        System.out.println("finial weights are:  ");
        //        for (ArrayList<Double> layer : config.getWeights()) {
        //            System.out.println(layer);
        //        }
        //Test your network here
    }

    /**
     * Stores the weight of a particular edge in a particular layer.
     *
     * @param layerNum layer that the edge resides in
     * @param edgeNum numerical identifier of the edge
     * @param newWeight weight that will be stored for the edge
     *
     * @author lts010, ks061
     */
    public void storeWeight(int layerNum, int edgeNum, double newWeight) {
        this.configuration.getWeights().get(layerNum).set(edgeNum, newWeight);
    }

    /**
     * Gets the weight of a particular edge in a particular layer.
     *
     * @param layerNum layer that the edge resides in
     * @param edgeNum numerical identifier of the edge
     *
     * @return weight of an edge in layerNumth layer and has a numerical
     * identifier of edgeNum
     *
     * @author lts010, ks061
     */
    public double getWeight(int layerNum, int edgeNum) {
        return this.configuration.getWeights().get(layerNum).get(edgeNum);
    }

    /**
     * Stores the theta of a particular neuron in a particular layer.
     *
     * @param layerNum layer that the neuron resides in
     * @param neuronNum numerical identifier of the neuron
     * @param newTheta theta that will be stored for the neuron
     *
     * @author lts010, ks061
     */
    public void storeTheta(int layerNum, int neuronNum, double newTheta) {
        this.configuration.getThetas().get(layerNum).set(neuronNum, newTheta);
    }

    /**
     * Gets the theta of a particular neuron in a particular layer.
     *
     * @param layerNum layer that the neuron resides in
     * @param neuronNum numerical identifier of the neuron
     *
     * @return theta of a neuron in layerNumth layer and has a numerical
     * identifier of neuronNum
     *
     * @author lts010, ks061
     */
    public double getTheta(int layerNum, int neuronNum) {
        return this.configuration.getThetas().get(layerNum).get(neuronNum);
    }

    /**
     * Gets the configuration for this neural network
     *
     * @return configuration for this neural network
     */
    public ConfigObject getConfiguration() {
        return this.configuration;
    }

    /**
     * Gets list of layers
     *
     * @return list of layers
     * @author lts010
     */
    public ArrayList<Layer> getLayers() {
        return layers;
    }

    /**
     * Explicit constructor used for JUnit testing that creates the input layer,
     * any hidden layers, and output layer, along with creating connections
     * among the layers.
     *
     * @author lts010
     */
    NeuralNet(ConfigObject config, double[][] data) {
        if (data.length == 0) {
            throw new NeuralNetConstructionException(
                    "No data has been provided.");
        }

        int dataLength = data[0].length;
        for (double[] inputSet : data) {
            if (inputSet.length != dataLength) {
                throw new NeuralNetConstructionException(
                        "Not all rows in data set have the same amount of entries.");
            }
        }
        this.data = data;
        this.configuration = config;
        ArrayList<Layer> layers = new ArrayList<>();
        InputLayer inputLayer = new InputLayer(config.getNumInputs(), "I1-", 0,
                                               this);
        layers.add(inputLayer);
        for (int i = 1; i < this.configuration.getNumHiddenLayers() + 1; i++) {
            layers.add(new HiddenLayer(
                    this.configuration.getNumNeuronsPerHiddenLayer(),
                    "H" + i + "-", i, this));
        }

        OutputLayer outputLayer = new OutputLayer(config.getNumOutputs(), "O1-",
                                                  layers.size(), this);
        layers.add(outputLayer);
        Iterator it = layers.iterator();
        Layer currentLayer = (Layer) it.next();
        Layer nextLayer;
        while (it.hasNext()) {
            nextLayer = (Layer) it.next();
            currentLayer.connectLayer(nextLayer);
            currentLayer = nextLayer;

        }
        this.layers = layers;
    }

}
