import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Gui extends JFrame implements ActionListener, KeyListener {

	// Gui elements that need to be a public variable
	private JButton applySettingsButton = new JButton("apply"); // apply button on settingsFrame
	private final Font BUTTON_FONT = new Font("Ubuntu", Font.PLAIN, 40); // font on jbutton
	private JTextField functionInputField; // input field for function on settingsFrame
	private JTextField colorInputField;

	private Leinwand2D locationCanvas; // 2d plot of new location of f(z)

	private JLabel titleLabel; // top label of the jframe, showing the function name

	// variables that are needed for plotting
	private String function; // the function to calculate as a string
	private double[] inputArea; // input definition area for the function
	private int[] outputArea; // output area for the 2D canvas
	private ArrayList<Complex> functionInputPoints, functionOutputPoints; // resulting input and output locations

	private double secretNumber = -0.96875; // 0.11111 in binary TODO: remove?
	private Point numberOfPoints; // number of points in x and y axis that have been calculated

	private int calculationDensity, paintDensity; // will calculate/paint *Density^2 points in a 1x1 unit square
	private int coordinateLineDensity; // draws one coordinateLine every coordinateLineDensity units
	private int dotWidth, circleWidth; // width of dots in 2D plot and width of circles in 3D plot
	private boolean paintLocationDots, paintHorizontalLines, paintVerticalLines; // whether to paint these things or not
																					// on 2D canvas
	private boolean paintBackgroundImage; // paint background image on 2d canvas?
	private BufferedImage backgroundImage;
	private ArrayList<String> functions; // functions to be transformed
	private ArrayList<Color> colors; // colors of the functions to be transformed
	private ArrayList<ArrayList<Complex>> transformedFunctionOutputPoints;

	private int integralSteps, fourierCoefficient;

	/* MAIN FUNCTION */

	/**
	 * will creaate a jframe of this class with the panels to plot the complex
	 * function functionOutputPoints.add(new Complex(pointsInX, pointsInY, true));
	 * 
	 * @param args arguments from command line at startup, will be ignored
	 */
	public static void main(String[] args) {

		// first shown function is identity f(z) = z
		new Gui("z");

	}

	/* CONSTRUCTOR */
	/**
	 * builds this jframe with all panels and plots a first function
	 * 
	 * @param function the first function that will be plotted
	 */
	public Gui(String function) {

		// save given variables
		this.function = function;
		this.functions = new ArrayList<String>();
		this.colors = new ArrayList<Color>();
		functions.add("z,-Pi,Pi,100");
		colors.add(new Color(255, 255, 0));
		colors.add(new Color(255 / 2, 255 / 2, 0));

		outputArea = new int[] { -10, 10, -10, 10 };

		// setup this jframe
		this.setTitle("KomA_Uebungsserie09 || plot fourier series || by Michael Roth || 2.5.2019");
		this.setSize(new Dimension(2000, 2000));
		this.setBackground(Color.BLACK);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		functionInputPoints = new ArrayList<Complex>();
		functionOutputPoints = new ArrayList<Complex>();

		// creating the different jpanels to draw on
		locationCanvas = new Leinwand2D(this);

		// prepare for plotting
		setPlotSettings();
		calculateFunctionPoints();

		// plot calculated first function
		setupCanvas();

		// add gui to this jframe
		addGuiElements(this);

		// add listeners
		applySettingsButton.addActionListener(this);
		functionInputField.addKeyListener(this);

		useNewSettings();

		// show jframe
		this.setVisible(true);

	}

	/**
	 * gives the needed variables to the different canvas so they can plot when
	 * *.repaint(); is called
	 */
	private void setupCanvas() {

		if (outputArea == null) {
			outputArea = getOutputAreaSquare();
		}

		// this will also set inputArea and outputArea of locationCanvas
		locationCanvas.setFunctionValues(functionInputPoints, functionOutputPoints, transformedFunctionOutputPoints,
				colors);

		locationCanvas.setPlotSettings(paintDensity, coordinateLineDensity, dotWidth, paintLocationDots,
				paintHorizontalLines, paintVerticalLines, paintBackgroundImage);

	}

	/**
	 * repaints the canvas
	 */
	private void repaintCanvas() {

		locationCanvas.repaint();

	}

	/**
	 * adds the gui to the given jframe, here parentFrame == this
	 * 
	 * @param parentFrame jframe to containing new gui elements
	 */
	private void addGuiElements(JFrame parentFrame) {

		// top panel for the title
		JPanel titlePanel = new JPanel();
		titlePanel.setOpaque(true);
		titlePanel.setBackground(Color.BLACK);
//		titlePanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));

		titleLabel = new JLabel("f(z) = " + function);
		titleLabel.setFont(new Font("Ubuntu", Font.PLAIN, 50));
		titleLabel.setForeground(Color.WHITE);

		titlePanel.add(titleLabel);

		// settings panel
		JPanel settings = new JPanel();

		JLabel functionInputLabel = new JLabel("add function: y(x) = ");
		functionInputLabel.setFont(new Font("Ubuntu", Font.PLAIN, 50));

		functionInputField = new JTextField();
		functionInputField.setFont(new Font("Ubuntu", Font.PLAIN, 50));
		functionInputField.setSize(1 * this.getSize().width / 6, 50);
		functionInputField.setPreferredSize(functionInputField.getSize());
		functionInputField.addKeyListener(this);

		colorInputField = new JTextField("0, 255, 100");
		colorInputField.setFont(new Font("Ubuntu", Font.PLAIN, 50));
		colorInputField.setSize(1 * this.getSize().width / 6, 50);
		colorInputField.setPreferredSize(functionInputField.getSize());
		colorInputField.addKeyListener(this);

		JButton add = new JButton("add");
		add.setFont(new Font("Ubuntu", Font.PLAIN, 50));
		add.addActionListener(this);

		JButton remove = new JButton("remove last function");
		remove.setFont(new Font("Ubuntu", Font.PLAIN, 50));
		remove.addActionListener(this);

		settings.add(functionInputLabel, BorderLayout.LINE_START);
		settings.add(functionInputField, BorderLayout.CENTER);
		settings.add(colorInputField, BorderLayout.CENTER);
		settings.add(add, BorderLayout.PAGE_END);
		settings.add(remove, BorderLayout.PAGE_END);

		// add panels to jframe
		parentFrame.add(titlePanel, BorderLayout.PAGE_START);
		parentFrame.add(locationCanvas, BorderLayout.CENTER);
		parentFrame.add(settings, BorderLayout.PAGE_END);

	}

	/**
	 * sets or initializes settings to be able to plot the functions later on
	 * 
	 * @return true if we need to recalculate the function, false if we just can
	 *         adjust the visual part without recalculating the function
	 */
	private boolean setPlotSettings() {

		boolean needToRecalculateFunction = false; // will be returned

		// if we need to initialize because settingsFrame doesnt exist yet
		inputArea = new double[] { -2, 2, -2, 2 };

		integralSteps = 100;
		fourierCoefficient = 5;

		calculationDensity = 1000;
		paintDensity = 5;
		coordinateLineDensity = 2;
		dotWidth = 5;
		circleWidth = 5;
		paintLocationDots = false;
		paintHorizontalLines = true;
		paintVerticalLines = true;
		paintBackgroundImage = true;

		needToRecalculateFunction = true;

		return needToRecalculateFunction;

	}

	/**
	 * load new calculation and plot settings and recalculate function if needed
	 */
	private void useNewSettings() {

		if (setPlotSettings()) {
			calculateFunctionPoints();
		}

		// give parameters to the canvas and repaint them
		setupCanvas();
		repaintCanvas();

	}

	/**
	 * for each point in inputArea on the density, get a corresponding functionvalue
	 * 
	 * @param calculationDensity the density to calulate the points
	 */
	public void calculateFunctionPoints() {

		transformedFunctionOutputPoints = new ArrayList<ArrayList<Complex>>();

		if (functions != null) {

//			System.out.println("GUI: \t calculating " + functions.size() + " transformed functions");

			// for each x value, calculate y and transform that value
			Complex currentComplexInput;
			double y;
			double start, end;

			ArrayList<Complex> currentFunction = new ArrayList<>();

			// calculate for each function
			for (int i = 0; i < functions.size(); i++) {

				// start and end value on x-axis
//				System.out.println(functions.get(i));
				if (functions.get(i).split(",")[1].equals("Pi")) {
					start = Math.PI;
				} else if (functions.get(i).split(",")[1].equals("-Pi")) {
					start = -Math.PI;
				} else if (functions.get(i).split(",")[1].equals("2Pi")) {
					start = 2 * Math.PI;
				} else {
					start = Double.parseDouble(functions.get(i).split(",")[1]);
				}
				if (functions.get(i).split(",")[2].equals("Pi")) {
					end = Math.PI;
				} else if (functions.get(i).split(",")[2].equals("-Pi")) {
					end = -Math.PI;
				} else if (functions.get(i).split(",")[2].equals("2Pi")) {
					end = 2 * Math.PI;
				} else {
					end = Double.parseDouble(functions.get(i).split(",")[2]);
				}

				currentFunction = new ArrayList<Complex>();

				// calculate inputfunction
				for (double x = start; x <= end; x += 1.0 / locationCanvas.getPaintableDimension().getWidth()) {

					y = calculate(new Complex(x, 0, true), functions.get(i).split(",")[0]).getRe();
					currentFunction.add(calculate(new Complex(x, y, true), function));

				}

				transformedFunctionOutputPoints.add(currentFunction);

				currentFunction = new ArrayList<Complex>();

				// calculate coefficients for fourier series
				Complex[] coefficients = new Complex[fourierCoefficient + 1];
				for (int n = 0; n <= fourierCoefficient; n++) {

					coefficients[n] = sum("(" + functions.get(i).split(",")[0] + ")*exp((-i)*2*(" + Math.PI + ")*(" + n + ")*z/(" + (end - start) + "))", start, end)
							.divide(new Complex(end - start, 0, true));

//					System.out.print("CALCULATEFUNCTIONPOINTS: \t coefficients[" + n + "] = ");
					coefficients[n].print();

				}

				// calculate fourier series
				
				calculationDensity = Integer.parseInt(functions.get(i).split(",")[3]);
				
				for (double x = outputArea[0]; x <= outputArea[1] + 1.0/calculationDensity/2; x += 1.0
						/ calculationDensity) {

					y = coefficients[0].getRe();
					for (int n = 1; n < coefficients.length; n++) {
//						System.out.println("CALCULATEFUNCTIONPOINTS: \t fourier function string: ("
//								+ Double.toString(2 * coefficients[n].getRe()) + ")*cos(" + n + "*z)+("
//								+ Double.toString(2 * coefficients[n].getIm()) + ")*sin(" + n + "*z)");
						y += calculate(new Complex(x, 0, true),
								"(" + Double.toString(2 * coefficients[n].getRe()) + ")*cos(" + n + "*2*(" + Math.PI + ")*z/(" + (end - start) + "))+("
										+ Double.toString(2 * coefficients[n].getIm()) + ")*(0-sin(" + n + "*2*(" + Math.PI + ")*z/(" + (end - start) + ")))").getRe();
						
//						System.out.println("CALCULATEFUNCTIONPOINTS: \t x, y = " + x + ", " + y);
					}
					currentFunction.add(calculate(new Complex(x, y, true), function));

				}

				transformedFunctionOutputPoints.add(currentFunction);

			}
		}

	}

	/**
	 * 
	 */
	private Complex sum(String function, double start, double end) {

//		System.out.println("SUM: \t function string: " + function);

		Complex result = new Complex(0, 0, true);
		Complex currentX;
		double intervallLength = end - start;

		for (double i = start; i < end + intervallLength / integralSteps / 2; i += intervallLength / integralSteps) {

			currentX = new Complex(i, 0, true);

//			System.out.println("SUM: \t current t in integral = " + (i));

			result = result
					.add(calculate(currentX, function).multiply(new Complex(intervallLength / integralSteps, 0, true)));

		}

		if (Math.abs(result.getRe()) < 0.000001) {
//			System.out.println("SUM: \t result.getRe() = " + result.getRe());
			result = new Complex(0, result.getIm(), true);
		}
		if (Math.abs(result.getIm()) < 0.000001) {
//			System.out.println("SUM: \t result.getIm() = " + result.getIm());
			result = new Complex(result.getRe(), 0, true);
		}

//		System.out.print("SUM: \t integral = ");
//		result.print();

		return result;

	}

	/**
	 * calculates f(z) with the given z, this function is recursive!
	 * 
	 * @param z        value of z in the given function, null if we divided by
	 *                 something too small
	 * @param function contains the function
	 * @param return   returns the resulting value = function(z)
	 */
	public Complex calculate(Complex z, String function) {

//	System.out.println("to calculate: " + input);

		// test if a previous calculation returned null
		if (z == null) {
			return null;
		}

		Complex result = new Complex(0, 0, true); // will be returned
		int openBrackets = 0; // counts the open brackets while we read through the string
		boolean bracketsRemoved; // true if theres was nothing to do but removing brackets at start and end

		do {

			bracketsRemoved = false;

			// for each character in function, look for + and -
			for (int i = 0; i < function.length(); i++) {
				// System.out.println(input.charAt(i) + " found");
				switch (function.charAt(i)) {

				case '(':
					openBrackets++;
					break;

				case ')':
					openBrackets--;
					break;

				case '+':
					if (openBrackets == 0) {

						Complex number = calculate(z, function.substring(0, i));

						if (number == null) {
							return null;
						}

						result = number.add(calculate(z, function.substring(i + 1, function.length())));
						return result;
					}
					break;

				case '-':
//					System.out.println("GUI: \t i = " + i + ", function: " + function);
					if (openBrackets == 0 && i != 0 && function.charAt(i - 1) != 'E') { // except case "-n" and
																						// "...E-..."

						Complex number;

						// doesnt work here, because "-x+1" will be "-(x+1)"
						// case "-n"
//						if (i == 0) {
//							number = new Complex(0, 0, true);
//						} else {
						number = calculate(z, function.substring(0, i));
//						}

						if (number == null) {
							return null;
						}

						result = number.subtract(calculate(z, function.substring(i + 1, function.length())));
						return result;
					}
					break;

				}

			}

			// if there wasnt a + or -, for each character in function, look for * and /
			for (int i = function.length() - 1; i >= 0; i--) {
				switch (function.charAt(i)) {

				case '(':
					openBrackets++;
					break;

				case ')':
					openBrackets--;
					break;

				case '*':
					if (openBrackets == 0) {
						Complex number = calculate(z, function.substring(0, i));

						if (number == null) {
							return null;
						}

						result = calculate(z, function.substring(0, i))
								.multiply(calculate(z, function.substring(i + 1, function.length())));
						return result;
					}
					break;

				case '/':
					if (openBrackets == 0) {
						Complex numerator = calculate(z, function.substring(0, i));
						Complex denumerator = calculate(z, function.substring(i + 1, function.length()));

						if (numerator == null) {
							return null;
						}

						result = numerator.divide(denumerator);

						return result;
					}
					break;

				}
			}

			// if there wasnt a * or /, for each character in function, look for sin, cos
			// and exp
			for (int i = 0; i < function.length(); i++) {
				switch (function.charAt(i)) {

				case '(':
					openBrackets++;
					break;

				case ')':
					openBrackets--;
					break;

				case 's': // sin or sqrt
					if (openBrackets == 0) {

						// TODO: sqrt for complex functions
						if (function.charAt(i + 1) == 'q') { // this sqrt function will return sqrt(Re(z))
							result = new Complex(
									Math.sqrt(calculate(z, function.substring(i + 5, function.length() - 1)).getRe()),
									0, true);
						} else {
							Complex argument = calculate(z, function.substring(i + 4, function.length() - 1));
							if (argument == null) {
								return null;
							}
							result = argument.sin();
						}
						return result;
					}
					break;

				case 'c':
					if (openBrackets == 0) {
						Complex argument = calculate(z, function.substring(i + 4, function.length() - 1));
						if (argument == null) {
							return null;
						}
						result = argument.cos();
						return result;
					}
					break;

				case 'e':
					if (openBrackets == 0) {
						Complex argument = calculate(z, function.substring(i + 4, function.length() - 1));
						if (argument == null) {
							return null;
						}
						result = argument.exp();
						return result;
					}
					break;

				}
			}

			// if there was nothing to do, we need to remove brackets at start and end
			if (function.charAt(0) == '(' && function.charAt(function.length() - 1) == ')') {
//			System.out.println("removing brackets around " + input);
				function = function.substring(1, function.length() - 1);
				bracketsRemoved = true;
//			System.out.println("new input: " + input);
			}

		} while (bracketsRemoved);

		// if there also were no brackets to remove, we need to create a new complex
		// number
//	System.out.println("make a new Complex number from: " + input);

		// case "-n"
		if (function.charAt(0) == '-') {

			if (function.split("-").length == 3) {
				function = function.split("-")[1] + function.split("-")[2];
			} else {
				function = function.split("-")[1];
			}

			switch (function) {

			case "z":
				return z.multiply(new Complex(-1, 0, true));

			case "i":
				return new Complex(0, -1, true);

			default:
//				System.out.println("CALCULATE: \t creating complex from " + function);
				return new Complex(-Double.parseDouble(function), 0, true);
			}

		} else {

			switch (function) {

			case "z":
				return z;

			case "i":
				return new Complex(0, 1, true);

			default:
				// System.out.println("CALCULATE: \t creating complex from " + function);
				return new Complex(Double.valueOf(function), 0, true);
			}

		}

	}

	/**
	 * returns the first number that finds place on a gridline or a gridline +-
	 * factor / calculationDensity
	 * 
	 * @return smallest x value that is on gridline +- factor / calculationDensity
	 */
	private double getFirstXValue() {

		double result; // to be retuned

		// go above smallest gridline
		if (inputArea[0] < 0) {
			result = (int) inputArea[0];
		} else {
			result = 1 + (int) inputArea[0];
		}

		// subtract 1 / DENSITY as long as we are in the inputArea
		while (result > inputArea[0]) {
			result -= 1.0 / calculationDensity;
		}

		return result;

	}

	/**
	 * gets the function from the input textfield and saves in a arraylist together
	 * with its color
	 */
	private void readAndAddFunction() {

		functions.add(functionInputField.getText().replaceAll("\\s", "").replaceAll("x", "z"));

		// read color
		// TODO: colors as they are in leinwand2d, so based on x input value
		String[] color = colorInputField.getText().replaceAll("\\s", "").split(",");
		colors.add(new Color(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2])));
		colors.add(new Color(Integer.parseInt(color[0]) / 2, Integer.parseInt(color[1]) / 2,
				Integer.parseInt(color[2]) / 2));

		useNewSettings();

		colorInputField.setText((int) (Math.random()*100+155) + ", " + (int) (Math.random()*100+155) + ", " + (int) (Math.random()*100+155));
		
		locationCanvas.repaint();
		this.repaint();

	}

	/* GETTERS */

	/**
	 * @return a square area that contains all inputPoints
	 */
	public double[] getInputAreaSquare() {
		
		return new double[] { -10, 10, -10, 10 };

	}

	/**
	 * @return a square area that contains all outputPoints
	 */
	public int[] getOutputAreaSquare() {

		return new int[] { -10, 10, -10, 10 };

	}

	/**
	 * @return calculationDensity set in the settingsPanel
	 */
	public int getCalculationDensity() {
		return calculationDensity;
	}

	/**
	 * @return return the secretNumber
	 */
	public double getSecretNumber() {
		return secretNumber;
	}

	/**
	 * @return return number of points
	 */
	public Point getNumberOfPoints() {
		return numberOfPoints;
	}

	/**
	 * @return return paintDensity
	 */
	public int getPaintDensity() {
		return paintDensity;
	}

	/**
	 * @return return coordinateline density
	 */
	public int getCoordinatelineDensity() {
		return coordinateLineDensity;
	}

	/**
	 * @return return dotWidth
	 */
	public int getDotWidth() {
		return dotWidth;
	}

	/**
	 * @return return circleWidth
	 */
	public int getCircleWidth() {
		return circleWidth;
	}

	/**
	 * 
	 */
	public BufferedImage getBackgroundImage() {
		return backgroundImage;
	}

	/* IMPLEMENTED FUNCTIONS */

	/**
	 * actionListener for this class
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

//		System.out.println(e.getActionCommand());

		switch (e.getActionCommand()) {

		case "add":
			readAndAddFunction();
			break;

		case "remove last function":
			if (functions.size() > 0) {
//				System.out.println(functions.size());
				functions.remove(functions.size()-1);
				colors.remove(colors.size()-1);
				System.out.println(colors.size()-1);
				if(functions.size() == 0) {
					functions = new ArrayList<String>();
					colors = new ArrayList<Color>();
				} else {
					functions.remove(functions.size()-1);
					colors.remove(colors.size()-1);
				}
				useNewSettings();
				locationCanvas.repaint();
				this.repaint();
			}
			break;

		}

	}

	/**
	 * keyListener for this class, nothing done here so far
	 */
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * keyListener for this class
	 */
	@Override
	public void keyPressed(KeyEvent e) {

//		System.out.println(e.getKeyCode() + " pressed");

		if (e.getKeyCode() == 10) {// enter pressed, interpret as if "apply" was clicked, use the new settings

			readAndAddFunction();

		}

	}

	/**
	 * keyListener for this class, nothing done here so far
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}