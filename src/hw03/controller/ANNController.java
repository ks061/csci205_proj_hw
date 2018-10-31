/* *****************************************
* CSCI205 - Software Engineering and Design
* Fall 2018
*
* Name: Logan Stiles and Kartikeya Sharma
* Date: Oct 24, 2018
* Time: 5:25:27 PM
*
* Project: csci205_proj_hw
* Package: hw03.view
* File: ANNController
* Description: This file contains ANNController, which represents the controller
*              of the neural network application.
* ****************************************
 */
package hw03.controller;

import hw03.ANNConfig;
import hw03.ANNLogger.ANNLogger;
import hw03.ANNLogger.ANNLoggerStatus;
import hw03.ActivationFunction.ActivationFunction;
import hw03.ActivationFunction.HyperbolicTangentActivationFunction;
import hw03.ActivationFunction.SigmoidActivationFunction;
import hw03.ActivationFunction.StepActivationFunction;
import hw03.Edge;
import hw03.Layer.Layer;
import hw03.Layer.OutputLayer;
import hw03.NeuralNet;
import hw03.Neuron.Neuron;
import hw03.ProgramMode;
import hw03.model.ANNModel;
import hw03.utility.ANNUtility;
import hw03.utility.ANNUtilityGUICompatible;
import hw03.utility.ANNViewUtility;
import hw03.view.ANNMenuBar;
import hw03.view.ANNView;
import hw03.view.EdgeLine;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

/**
 * ANNController represents the controller of the neural network MVC
 * application.
 *
 * @author lts010, ks061
 */
public class ANNController implements EventHandler<ActionEvent> {

    /**
     * The model of the neural network MVC application.
     */
    private ANNModel theModel;

    /**
     * The view of the neural network MVC application.
     */
    private ANNView theView;
    private ANNConfig theConfig;
    private double[][] theData;

    private Thread neuralNetThread = new Thread();

    /**
     * Constructor to initialize the controller of the neural network MVC
     * application
     *
     * @param theModel pointer to the model of the neural network MVC
     * application
     * @param theView pointer to the model of the neural network MVC application
     *
     * @author ks061, lts010
     */
    public ANNController(ANNModel theModel, ANNView theView) {
        this.theView = theView;
        this.theModel = theModel;

        this.theView.getAlphaInput().setOnAction(this);
        this.theView.getMuInput().setOnAction(this);
        this.theView.getClassifyBtn().setOnAction(this);
        this.theView.getLearnBtn().setOnAction(this);
        this.theView.getStepBtn().setOnAction(this);

        this.theView.getRunRBtn().setOnAction(this);
        this.theView.getInputStepRBtn().setOnAction(this);
        this.theView.getEpochStepRBtn().setOnAction(this);
        this.theView.getTerminateRBtn().setOnAction(this);

        this.theView.getANNMenuBar().getLoadConfigFileMI().setOnAction(this);
        this.theView.getANNMenuBar().getSaveConfigFileMI().setOnAction(this);
        this.theView.getANNMenuBar().getLoadTestFileMI().setOnAction(this);
        this.theView.getANNMenuBar().getLoadTrainingFileMI().setOnAction(this);
        this.theView.getANNMenuBar().getConfigMI().setOnAction(this);
        this.theView.getANNMenuBar().getExitMI().setOnAction(this);
        this.theView.getANNMenuBar().getCancelBtn().setOnAction(this);
        this.theView.getANNMenuBar().getSubmitBtn().setOnAction(this);

        CreateButtonBindings();
        //TODO remove next line
        InitNetorkBindings();

    }

    /**
     * Handles events that occur in the application
     *
     * @param event event that occurs in the application
     *
     * @author ks061, lts010
     */
    @Override
    public void handle(ActionEvent event) {
        //System.out.println("event source = " + event.getSource().toString());
        if (event.getSource() == this.theView.getAlphaInput()) {
            setNewAlpha();
        }
        else if (event.getSource() == this.theView.getMuInput()) {
            setNewMu();
        }
        else if (event.getSource() == this.theView.getLearnBtn()) {
            /*   System.out.println("about to start task");
            if (this.theData == null) {
                ANNViewUtility.showInputAlert("You must load data first",
                        "Select the file menu to load data");
                return;
            }
            //make sure the Neural net has upto date data
            theModel.getNeuralNetwork().setData(theData);
             */
            if (this.theData == null) {
                ANNViewUtility.showInputAlert("You must load data first",
                                              "Select the file menu to load data");
                return;
            }
            if (neuralNetThread.isAlive()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Active Thread");
                alert.setHeaderText("Cannot start learning process!");
                alert.setContentText(String.format(
                        "Neural Net is running another process",
                        this.theView.getAlphaInput().getText()));
                alert.show();
                return;
            }
            else {
                updateActivationFunction();
                Task learnTask = new Task<Void>() {
                    @Override
                    public Void call() throws FileNotFoundException {
                        theModel.neuralNetwork.getConfiguration().setProgramMode(
                                ProgramMode.TRAINING);
                        ANNLogger.setSwitch(ANNLoggerStatus.OFF);
                        theModel.getNeuralNetwork().train();
                        updateEdgeColors();
                        return null;
                    }
                };
                neuralNetThread = new Thread(learnTask);
                neuralNetThread.setDaemon(true);
                neuralNetThread.start();
            }

        }
        else if (event.getSource() == this.theView.getClassifyBtn()) {
            if (this.theData == null) {
                ANNViewUtility.showInputAlert("You must load data first",
                                              "Select the file menu to load data");
                return;
            }
            System.out.println("ClassifyBtn");
            if (neuralNetThread.isAlive()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Active Thread");
                alert.setHeaderText("Cannot start learning process!");
                alert.setContentText(String.format(
                        "Neural Net is running another process",
                        this.theView.getAlphaInput().getText()));
                alert.show();
            }
            else {
                updateActivationFunction();
                Task classifyTask = new Task<Void>() {
                    @Override
                    public Void call() throws FileNotFoundException {
                        theModel.neuralNetwork.getConfiguration().setProgramMode(
                                ProgramMode.CLASSIFICATION);
                        ANNLogger.setSwitch(ANNLoggerStatus.OFF);
                        theModel.getNeuralNetwork().classify();
                        return null;
                    }
                };
                neuralNetThread = new Thread(classifyTask);
                neuralNetThread.setDaemon(true);
                neuralNetThread.start();
            }

        }
        else if (event.getSource() == this.theView.getStepBtn()) {
            theModel.neuralNetwork.notifyNeuralNet();
            // System.out.println("StepBtn");

        }
        else if (event.getSource() == this.theView.getRunRBtn()) {
            theModel.neuralNetwork.notifyNeuralNet();
            System.out.println("RunRBtn");

        }
        else if (event.getSource() == this.theView.getInputStepRBtn()) {
            System.out.println("InputStepRBtn");

        }
        else if (event.getSource() == this.theView.getEpochStepRBtn()) {
            System.out.println("EpochStepRBtn");

        }
        else if (event.getSource() == this.theView.getTerminateRBtn()) {
            System.out.println("TerminateRBtn");

        }
        else if (event.getSource() == this.theView.getANNMenuBar().getExitMI()) {
            System.out.println("exitMI was selected.");
            System.exit(0);

        }
        else if (event.getSource() == this.theView.getANNMenuBar().getConfigMI()) {
            this.theView.makeConfigGroupVisable();
            this.theView.getANNMenuBar().getConfigInfo();
            System.out.println("MenuBar Config");

        }
        else if (event.getSource() == this.theView.getANNMenuBar().getLoadConfigFileMI()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open config file");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Text Files", "*.txt"));
            File configFile = fileChooser.showOpenDialog(theView.getTheStage());
            if (configFile != null) {
                try {
                    this.theConfig = ANNUtilityGUICompatible.createConfigurationFromFile(
                            configFile);
                } catch (FileNotFoundException ex) {
                    // This should not happen.
                    Logger.getLogger(ANNMenuBar.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }

            try {
                theModel.createNeuralNetwork(theConfig);
                String alpha = String.format("%f",
                                             this.theModel.neuralNetwork.getConfiguration().getAlpha());
                String mu = String.format("%f",
                                          this.theModel.neuralNetwork.getConfiguration().getMu());
                theView.getCurrentAlpha().setText(alpha);
                theView.getCurrentMu().setText(mu);
                ActivationFunction activationFunction = this.theModel.neuralNetwork.getConfiguration().getActivationFunction();
                if (activationFunction instanceof SigmoidActivationFunction) {
                    this.theView.getSigmoidBtn().setSelected(true);
                }
                else if (activationFunction instanceof StepActivationFunction) {
                    this.theView.getStepFunctionBtn().setSelected(
                            true);
                }
                else {
                    this.theView.getHyperbolicTangentBtn().setSelected(
                            true);
                }
            } catch (FileNotFoundException ex) {
                //TODO does NeuralNet still need to through exception?
                Logger.getLogger(ANNController.class.getName()).log(Level.SEVERE,
                                                                    null, ex);
            }
            if (theData != null) {
                theModel.getNeuralNetwork().setData(theData);
            }
            this.theView.MakeNetworkGraphic(theConfig);
            System.out.println("MenuBar LoadConfig");

        }
        else if (event.getSource() == this.theView.getANNMenuBar().getSaveConfigFileMI()) {

            try {
                exportConfig(theModel.getNeuralNetwork());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ANNController.class.getName()).log(Level.SEVERE,
                                                                    null, ex);
            }

            System.out.println("MenuBar SaveConfig");

        }
        else if (event.getSource() == this.theView.getANNMenuBar().getLoadTrainingFileMI()) {
            double[][] result;
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Training File");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Training Data", "*.csv"));
            File trainingFile = fileChooser.showOpenDialog(
                    this.theView.getTheStage());
            if (trainingFile == null) {
                ANNViewUtility.showInputAlert("Error", "Error openning file");
            }
            else {
                result = ANNUtilityGUICompatible.getData(
                        trainingFile);
                //if empty getData should have already notified the user
                if (result.length > 0) {
                    this.theData = result;
                    theModel.neuralNetwork.setData(this.theData);
                }
            };
            System.out.println("MenuBar LoadTraining");

        }
        else if (event.getSource() == this.theView.getANNMenuBar().getLoadTestFileMI()) {
            double[][] result;
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Input/Test Data");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Input/Test Data", "*.csv"));
            File testFile = fileChooser.showOpenDialog(
                    this.theView.getTheStage());
            if (testFile == null) {
                ANNViewUtility.showInputAlert("Error", "Error openning file");
            }
            else {
                result = ANNUtilityGUICompatible.getData(testFile);
                //if empty getData should have already notified the user
                if (result.length > 0) {
                    this.theData = result;
                }
            }
            this.theConfig.setProgramMode(ProgramMode.CLASSIFICATION);
            System.out.println("MenuBar LoadTest");

        }
        else if (event.getSource() == this.theView.getANNMenuBar().getCancelBtn()) {
            this.theView.makeNetorkGroupVisable();
            System.out.println("MenuBar Config cancelButton");

        }
        else if (event.getSource() == this.theView.getANNMenuBar().getSubmitBtn()) {
            int numInputs = ANNViewUtility.extractPositiveIntFromText(
                    theView.getANNMenuBar().getNumInputsTextField().getText(),
                    2,
                    "the number of inputs must be a positive integer.");
            if (numInputs == -1) {
                return;
            }
            int numHiddenNodes = ANNViewUtility.extractPositiveIntFromText(
                    theView.getANNMenuBar().getNumHiddenTextField().getText(),
                    3, "the number of hidden nodes must be a positive integer.");
            if (numHiddenNodes == -1) {
                return;
            }
            int numOutputs = ANNViewUtility.extractPositiveIntFromText(
                    theView.getANNMenuBar().getNumOutTextField().getText(),
                    1,
                    "the number of output nodes must be a positive integer.");
            if (numOutputs == -1) {
                return;
            }
            int maxEpochs = ANNViewUtility.extractPositiveIntFromText(
                    theView.getANNMenuBar().getMaxEpochTextField().getText(),
                    50000,
                    "the maximun number of epochs be a positive integer.");
            if (maxEpochs == -1) {
                return;
            }

            // TODO extract this chunk of code to a new method
            double maxSSE = -1;
            boolean formatOK = true;
            String sSEString = theView.getANNMenuBar().getMaxSSETextField().getText();
            if (sSEString == null || sSEString.isEmpty()) {
                maxSSE = 0.1;
            }
            else {
                try {
                    maxSSE = Double.parseDouble(sSEString);
                } catch (NumberFormatException e) {
                    formatOK = false;
                }
            }
            if (!formatOK || maxSSE <= 0) {
                ANNViewUtility.showInputAlert("SSE must be a positive number",
                                              (sSEString + " cannot be converted to a positive number."));
                return;
            }

            ArrayList<ArrayList<Double>> weights = ANNUtility.getRandomWeights(
                    numInputs,
                    numOutputs,
                    1,
                    numHiddenNodes);
            ArrayList<ArrayList<Double>> thetas = ANNUtilityGUICompatible.getListOfThetas(
                    numOutputs, 1,
                    numHiddenNodes);

            double alpha = NeuralNet.DEFAULT_ALPHA;
            double mu = Edge.DEFAULTMU;
            ActivationFunction activationFunction = new SigmoidActivationFunction();
            theView.getSigmoidBtn().setSelected(true);

            this.theConfig = new ANNConfig(numInputs, numOutputs, 1,
                                           numHiddenNodes,
                                           maxSSE, maxEpochs, alpha, mu,
                                           weights, thetas,
                                           ProgramMode.TRAINING,
                                           activationFunction);
            try {
                theModel.createNeuralNetwork(theConfig);
                theView.getCurrentAlpha().setText(String.format("%f",
                                                                this.theModel.neuralNetwork.getConfiguration().getAlpha()));
                theView.getCurrentMu().setText(String.format("%f",
                                                             this.theModel.neuralNetwork.getConfiguration().getMu()));

            } catch (FileNotFoundException ex) {
                //TODO does NeuralNet still need to through exception?
                Logger.getLogger(ANNController.class.getName()).log(Level.SEVERE,
                                                                    null, ex);
            }
            if (theData != null) {
                theModel.getNeuralNetwork().setData(theData);
            }
            this.theView.MakeNetworkGraphic(theConfig);
            System.out.println("MenuBar Config submitButton");
        }

        if (this.theModel.getNeuralNetwork().getConfiguration().getProgramMode() == ProgramMode.TRAINING) {
            // TODO unused reference to outputLayer, why do we need this?
            OutputLayer outputLayer = (OutputLayer) this.theModel.getNeuralNetwork().getLayers().get(
                    ANNModel.OUTPUT_LAYER_INDEX);
            this.theView.getCurrentSSE().setText(String.format("%f",
                                                               this.theModel.getNeuralNetwork().getTrainingAverageSSE()));
            this.theView.getCurrentEpochNum().setText(String.format("%d",
                                                                    this.theModel.getNeuralNetwork().getTrainingNumberOfEpochs()));
        }
    }

    /**
     * Updates activation functions stored in each neuron based on user
     * selection encapsulated within the MVC model ANNModel
     *
     * @author ks061, lts010
     */
    public void updateActivationFunction() {
        ActivationFunction currentActivationFunction = this.theModel.getNeuralNetwork().getLayers().get(
                ANNModel.INPUT_LAYER_INDEX).getNeurons().get(0).getActivationFunction();

        if (theModel.getPropSigmoid().get() && !(currentActivationFunction instanceof SigmoidActivationFunction)) {
            SigmoidActivationFunction newActivationFunction = new SigmoidActivationFunction();
            for (Layer layer : theModel.getNeuralNetwork().getLayers()) {
                for (Neuron neuron : layer.getNeurons()) {
                    neuron.setActivationFunction(newActivationFunction);
                }
            }
            this.theModel.neuralNetwork.getConfiguration().setActivationFunction(
                    newActivationFunction);
        }
        else if (theModel.getPropStepFunction().get() && !(currentActivationFunction instanceof StepActivationFunction)) {
            StepActivationFunction newActivationFunction = new StepActivationFunction();
            for (Layer layer : theModel.getNeuralNetwork().getLayers()) {
                for (Neuron neuron : layer.getNeurons()) {
                    neuron.setActivationFunction(newActivationFunction);
                }
            }
            this.theModel.neuralNetwork.getConfiguration().setActivationFunction(
                    newActivationFunction);
        }
        else if (theModel.getPropHyperbolicTangent().get() && !(currentActivationFunction instanceof HyperbolicTangentActivationFunction)) {
            HyperbolicTangentActivationFunction newActivationFunction = new HyperbolicTangentActivationFunction();
            for (Layer layer : theModel.getNeuralNetwork().getLayers()) {
                for (Neuron neuron : layer.getNeurons()) {
                    neuron.setActivationFunction(newActivationFunction);
                }
            }
            this.theModel.neuralNetwork.getConfiguration().setActivationFunction(
                    newActivationFunction);
        }
    }

    /**
     * Sets the alpha value for neural network of the program based on the alpha
     * value entered in the corresponding text box within the GUI
     *
     * @author ks061, lts010
     */
    public void setNewAlpha() {
        try {
            String alpha = theView.getAlphaInput().getText();
            if (alpha.length() > 0) {
                double newAlpha = Double.parseDouble(alpha);
                this.theModel.getNeuralNetwork().setAlpha(newAlpha);
                theView.getCurrentAlpha().setText(alpha);
                theConfig.setAlpha(newAlpha);
            }
        } catch (NumberFormatException numberFormatException) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Incorrect input!");
            alert.setHeaderText("Incorrect input specified!");
            alert.setContentText(String.format("Can not convert \"%s\"",
                                               this.theView.getAlphaInput().getText()));
            alert.show();
        }
    }

    /**
     * Sets the momentum value for the neural network of the program based on
     * the momentum value entered in the corresponding text box within the GUI.
     *
     * @author lts010, ks061
     */
    public void setNewMu() {
        try {
            String mu = theView.getMuInput().getText();
            if (mu.length() > 0) {
                double newMu = Double.parseDouble(mu);
                ArrayList<Neuron> inputNeurons = theModel.getNeuralNetwork().getLayers().get(
                        ANNModel.INPUT_LAYER_INDEX).getNeurons();
                ArrayList<Neuron> hiddenNeurons = theModel.getNeuralNetwork().getLayers().get(
                        ANNModel.HIDDEN_LAYER_INDEX).getNeurons();
                for (Neuron inputNeuron : inputNeurons) {
                    for (Edge edge : inputNeuron.getOutEdges()) {
                        edge.setMu(newMu);
                    }
                }
                for (Neuron hiddenNeuron : hiddenNeurons) {
                    for (Edge edge : hiddenNeuron.getOutEdges()) {
                        edge.setMu(newMu);
                    }
                }
                theView.getCurrentMu().setText(mu);
                theConfig.setMu(newMu);
            }
        } catch (NumberFormatException numberFormatException) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Incorrect input!");
            alert.setHeaderText("Incorrect input specified!");
            alert.setContentText(String.format("Can not convert \"%s\"",
                                               this.theView.getMuInput().getText()));
            alert.show();
        }
    }

    /**
     * Updates the color for each edge line based on the edge weight associated
     * with the provided edge and layer number (red for negative weights, green
     * for positive, and blue for zero)
     *
     * @author ks061, lts010
     */
    public void updateEdgeColors() {

        for (ArrayList<EdgeLine> edges : theView.getEdgeLines()) {
            for (EdgeLine edgeLine : edges) {
                edgeLine.updateColor();
            }
        }
    }

    /**
     * Updates the neuron values displayed in the view
     *
     * @author ks061, lts010
     */
    public void updateNeuronValues() {
        for (Neuron neuron : theModel.getNeuralNetwork().getLayers().get(
                ANNModel.INPUT_LAYER_INDEX).getNeurons()) {
            int neuronNum = neuron.getNeuronNum();
            double netValue = neuron.getNetValue();
            String text = String.format("%f", netValue);
            theView.getNodeCircles().get(ANNModel.INPUT_LAYER_INDEX).get(
                    neuronNum).setText(text);
        }
        for (Neuron neuron : theModel.getNeuralNetwork().getLayers().get(
                ANNModel.OUTPUT_LAYER_INDEX).getNeurons()) {
            int neuronNum = neuron.getNeuronNum();
            double netValue = neuron.getNetValue();
            String text = String.format("%f", netValue);
            theView.getNodeCircles().get(ANNModel.OUTPUT_LAYER_INDEX).get(
                    neuronNum).setText(text);
        }
    }

    /**
     * Updates bindings for the CircleNode text, and EdgeLine Color. This must
     * called whenever the configuration of the Neural Net changes. Since the
     * graphic depiction of the neural net changes because the number of nodes
     * and edges are likely to have changed.
     *
     * @author ks061, lts010
     */
    public void InitNetorkBindings() {

    }

    /**
     * Creates all of the binding to the view that don't need to be updated when
     * the configuration changes.
     *
     * @author ks061, lts010
     */
    public void CreateButtonBindings() {
        this.theModel.getPropSigmoid().bind(
                theView.getSigmoidBtn().selectedProperty());
        this.theModel.getPropStepFunction().bind(
                theView.getStepFunctionBtn().selectedProperty());
        this.theModel.getPropHyperbolicTangent().bind(
                theView.getHyperbolicTangentBtn().selectedProperty());
        this.theModel.getStepEpoch().bind(
                theView.getEpochStepRBtn().selectedProperty());
        this.theModel.getStepInput().bind(
                theView.getInputStepRBtn().selectedProperty());
        this.theModel.getTerminate().bind(
                theView.getTerminateRBtn().selectedProperty());

    }

    /**
     * Receives a new config and notifies
     *
     * @author ks061, lts010
     */
    public void UpdateConfig(ANNConfig newConfig) {
        this.theView.MakeNetworkGraphic(newConfig);
        InitNetorkBindings();
        //Need create new neraul net and set buttons/text to default values.

    }

    /**
     * Saves all configuration information of a neural net to a .txt file
     *
     * @param nN neural network whose configuration will be exported
     * @throws java.io.FileNotFoundException if the file for the configuration
     * to be written to as specified by the user cannot be written to or another
     * error occurs while opening or creating the file
     * @see
     * <a href=https://docs.oracle.com/javase/8/docs/api/java/io/PrintWriter.html>
     * PrintWriter </a>
     *
     * @author lts010, ks061
     */
    public void exportConfig(NeuralNet nN) throws FileNotFoundException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("SaveConfiguration");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Text Files", "*.txt"));
        File outputFile = fileChooser.showSaveDialog(theView.getTheStage());
        if (outputFile == null) {
            System.out.println("didn't get a filename.");
            return;
        }

        PrintWriter pWriter = new PrintWriter(outputFile);
        pWriter.printf("%d.0 %d.0 %d.0 %d.0 %f %d.0 %f %f %d\n",
                       nN.getConfiguration().getNumInputs(),
                       nN.getConfiguration().getNumOutputs(),
                       nN.getConfiguration().getNumHiddenLayers(),
                       nN.getConfiguration().getNumNeuronsPerHiddenLayer(),
                       nN.getConfiguration().getHighestSSE(),
                       nN.getConfiguration().getNumMaxEpochs(),
                       nN.getConfiguration().getAlpha(),
                       nN.getConfiguration().getMu(),
                       ANNUtilityGUICompatible.convertActivationFunctionToInt(
                               nN.getConfiguration().getActivationFunction()));
        ArrayList<ArrayList<Double>> weights = nN.getConfiguration().getWeights();
        String weightLayer;
        for (ArrayList<Double> weightList : weights) {
            weightLayer = "";
            for (double weight : weightList) {
                weightLayer += weight + " ";
            }
            pWriter.printf("%s\n", weightLayer);
        }
        pWriter.printf("%s\n", "THETAS");
        List<ArrayList<Double>> thetas = nN.getConfiguration().getThetas().subList(
                1,
                nN.getConfiguration().getThetas().size());
        String thetaLayer;
        for (ArrayList<Double> thetaList : thetas) {
            thetaLayer = "";
            for (double theta : thetaList) {
                thetaLayer += theta + " ";
                System.out.println("writing thetas");
            }
            pWriter.printf("%s\n", thetaLayer);
        }
        pWriter.flush();
        pWriter.close();
    }

}
