/* *****************************************
 * CSCI205 - Software Engineering and Design
 * Fall 2018
 *
 * Name: Logan Stiles and Kartikeya Sharma
 * Date: Oct 10, 2018
 * Time: 5:00:00 PM
 *
 * Project: 205-FA18Class
 * Package: hw02
 * File: Edge
 * Description: This file contains Edge, which represents the connection between
 *              neurons in adjacent layers within a neural network.
 *
 * ****************************************
 */
package hw03.model.neuralnet;

import hw03.model.neuralnet.neuron.Neuron;

/**
 * Edge represents the connection between neurons in adjacent layers within a
 * neural network.
 *
 * @author lts010, ks061
 */
public class Edge {

    /**
     * A default mu (momentum constant) value
     */
    public final static double DEFAULTMU = 0.5;

    /**
     * The mu (momentum constant) value of this edge
     */
    private double mu = DEFAULTMU;

    /**
     * Represents the index of this edge amongst the edges connecting from the
     * layer that has neurons that this edge receives input from, i.e. the edge
     * connecting the first neuron in a layer to the first neuron in the next
     * adjacent layer has an index of 0, the edge connecting the first neuron in
     * a layer to the second neuron in the next adjacent layer has an index of
     * 1, etc.
     */
    private final int edgeNum;
    /**
     * Represents the index of the layer within a neural network that the edge
     * receives input from, i.e. the input layer has an index of 0, etc.
     */
    private final int layerNum;
    /**
     * Represents the neural network that this edge resides within
     */
    private final NeuralNet neuralNet;

    /**
     * Represents the weights of each edge
     */
    private Double weight;

    /**
     * Represents the change in weight during the current learn cycle
     */
    private double deltaWeight;

    /**
     * Represents the weight applied to the delta that this edge receives from
     * its output neuron during back propagation
     */
    private double weightTimesErrorGradient = 0;
    /**
     * Represents the neuron that this edge connects to or delivers output to
     */
    private Neuron to;
    /**
     * Represents the neuron that this edge connects from or receives input from
     */
    private Neuron from;

    /**
     * Constructor for creating an edge
     *
     * @param nN represents the neural network that this edge resides within
     * @param edgeNum represents the index of this edge amongst the edges
     * connecting from the layer that has neurons that this edge receives input
     * from, i.e. the edge connecting the first neuron in a layer to the first
     * neuron in the next adjacent layer has an index of 0, the edge connecting
     * the first neuron in a layer to the second neuron in the next adjacent
     * layer has an index of 1, etc.
     * @param layerNum represents the index of the layer within a neural network
     * that the edge receives input from, i.e. the input layer has an index of
     * 0, etc.
     *
     * @author ks061, lts010
     */
    public Edge(NeuralNet nN, int layerNum, int edgeNum) {
        this.neuralNet = nN;
        this.weight = neuralNet.getWeight(layerNum, edgeNum);
        this.edgeNum = edgeNum;
        this.layerNum = layerNum;
    }

    /**
     * Gets the weight applied to the net value of the neuron this edge receives
     * input from
     *
     * @return weighted net value of input neuron
     *
     * @author lts010, ks061
     */
    public double getWeightedValue() {
        return weight * from.getNetValue();
    }

    /**
     * Sets the neuron that this edge gets data from
     *
     * @param neuron neuron that this edge should get data from
     *
     * @author ks061, lts010
     */
    public void setFrom(Neuron neuron) {
        this.from = neuron;
    }

    /**
     * Sets the neuron that this edge delivers data to
     *
     * @param neuron neuron that this edge should deliver data to
     *
     * @author ks061, lts010
     */
    public void setTo(Neuron neuron) {
        this.to = neuron;
    }

    /**
     * Gets the neuron that this edge gets data from
     *
     * @return neuron that this edge gets data from
     *
     * @author ks061, lts010
     */
    public Neuron getFrom() {
        return this.from;
    }

    /**
     * Gets the neuron that this edge delivers data to
     *
     * @return neuron that this edge delivers data to
     *
     * @author ks061, lts010
     */
    public Neuron getTo() {
        return this.to;
    }

    /**
     * Gets the weight applied to the delta that this edge receives from its
     * output neuron during back propagation
     *
     * @return the weight applied to the delta that this edge receives from its
     * output neuron during back propagation
     *
     * @author ks061, lts010
     */
    public double getWeightTimesErrorGradient() {
        return weightTimesErrorGradient;
    }

    /**
     * Gets the weight of the edge
     *
     * @return the weight of the edge
     *
     * @author ks061, lts010
     */
    public double getWeight() {
        return this.weight;
    }

    /**
     * Stores the value of the weight applied to the error gradient received
     * from the output neuron of this edge, updates the weight for the edge by
     * adding the product of the momentum term, change in weight for the last
     * set of inputs, learning rate, the net value of the neuron this edge
     * receives input from, and the error gradient received from the output
     * neuron of this edge
     *
     * @param errorGradient the value of delta, the error gradient
     *
     * @author lts010, ks061
     */
    public void learn(double errorGradient) {
        double previousDeltaWeight = this.deltaWeight;
        this.deltaWeight = this.neuralNet.getAlpha() * this.from.getNetValue() * errorGradient;

        if ((this.deltaWeight < 0 && previousDeltaWeight < 0)
            || (this.deltaWeight > 0 && previousDeltaWeight > 0)) {
            this.deltaWeight += DEFAULTMU * previousDeltaWeight;
        }

        this.weight += this.deltaWeight;
        this.weightTimesErrorGradient = this.weight * errorGradient;
        neuralNet.storeWeight(layerNum, edgeNum, this.weight);
    }

    /**
     * Gets the momentum constant of this edge
     *
     * @return the momentum constant of this edge
     *
     * @author lts010, ks061
     */
    public double getMu() {
        return mu;
    }

    /**
     * Sets the weight of this edge
     *
     * @param weight weight of this edge
     *
     * @author lts010, ks061
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * Sets the momentum constant of this edge
     *
     * @param mu the new momentum constant of this edge
     *
     * @author lts010, ks061
     */
    public void setMu(double mu) {
        this.mu = mu;
    }

}
