package bs7trafficlight;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;


public class TrafficLightControl extends JFrame implements ActionListener, TimerCallback {
	/** default */
	private static final long serialVersionUID = 1L;

	/** Start of the program - needs only the constructor call, 
	 * everything else will be handled by the gui thread
	 * @param args
	 */
	public static void main(String[] args) {
		new TrafficLightControl();
	}
	
	/** allowed values of the state variable */
	public enum States {
		start, end, all_red, red_yellow, green, request, yellow, red_red, red_green
	}
	
	/** here the current state is held. The start state will be set as default, because
	 * it is assumed that the model starts with the software
	 */
	private States state = States.start;
		
	/** button for the switch off command (event) */
	private JButton offBtt;
	
	/** button for the pedestrian light request (event) */
	private JButton callBtt;
	
	/** reference to the view class */
	private TrafficLightView tlView;
	
	/** The car model holds the info, which light must be active */
	private CarTrafficLightModel carLight = new CarTrafficLightModel();
	
	/** The person model holds the info, which light must be active */
	private PersonTrafficLightModel persLight = new PersonTrafficLightModel();
	
	/** the model expects only one timer */
	private TlTimer trafficLightTimer = new TlTimer(this);
	
	/** flag will be set to true as soon as the off button is clicked */
	private boolean offReceived = false;
	
	/** constructor cares for the initialization of the software */
	public TrafficLightControl() {
		initializeGui();
		setElementsGui();
		
		setVisible(true);

		stateMachine(new Event(Event.Types.ON));
	}
	
	/**
	 * Basic initializations
	 */
	private void initializeGui() {
		// title of the OS Frame 
		setTitle("State Machine Example");
		
		// gues what :-)
		setSize(TrafficLightView.SIZE_X, TrafficLightView.SIZE_Y);
		
		// force the frame to terminate the software as soon as the
		// x on the right upper corner is clicked. Otherwise, the ui thread
		// will be sent to the background.
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		// I use the grid bag layout manager
		getContentPane().setLayout(new GridBagLayout());
	}

	/**
	 * Placement of the gui elements on the frame
	 */
	private void setElementsGui() {
		// config object for the layout manager
		GridBagConstraints gbc = new GridBagConstraints();
		
		// place the first element on the upper right corner
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		// weight is 1 as a reference for the other elements
		gbc.weighty = 1;
		gbc.weightx = 1;
		
		// Expand the element to its size
		gbc.fill = GridBagConstraints.BOTH;
		
		// spread the element over two columns
		gbc.gridwidth = 2;
		
		// create the view (JPanel) and place it to the frame
		tlView = new TrafficLightView(this);
		add(tlView, gbc);
		
		// now care for the off button
		gbc = new GridBagConstraints();
		offBtt = new JButton("Off");
		
		// the TrafficLightControl can handle the actionPerformed calls and is
		// set as the action listener for the button
		offBtt.addActionListener(this);
		
		// Place the button on the left, second row
		gbc.gridx = 0;
		gbc.gridy = 1;
		
		// smaller weight in y direction - the view is dominant
		gbc.weighty = 0.02;
		
		// the two buttons share the two columns evenly
		gbc.weightx = 0.5;
		// add the button
		add(offBtt, gbc);
		
		// same with the call button
		gbc = new GridBagConstraints();
		callBtt = new JButton("Call");
		callBtt.addActionListener(this);
		gbc.gridx = 1; // second column
		gbc.gridy = 1;
		gbc.weighty = 0.02;
		gbc.weightx = 0.5;
		add(callBtt, gbc);
	}
	
	private void stateMachine(Event event){
		switch(state){
			case start:
				handleStateStart();
				break;
			case all_red:
				handleStateAllRed(event);
				break;
			case red_yellow:
				handleStateRedYellow(event);
				break;
			case green:
				handleStateGreen(event);
				break;
			case request:
				handleStateRequest(event);
				break;
			case yellow:
				handleStateYellow(event);
				break;
			case red_red:
				handleStateRedRed(event);
				break;
			case red_green:
				handleStateRedGreen(event);
				break;
		}
	}
	private void handleStateStart(){
		entryStateAllRed();
	}
	private void handleStateAllRed(Event event){
		if(offReceived){
			entryStateEnd();
		}else if(event.getType() == Event.Types.TIMER){
				entryStateRedYellow();
			}
		}
	private void handleStateRedYellow(Event event){
		if(event.getType() == Event.Types.OFF){
			entryStateAllRed();
		}else if(event.getType() == Event.Types.TIMER){
				entryStateGreen();
				}
			}
	private void handleStateGreen(Event event){
		if(event.getType() == Event.Types.OFF){
			entryStateYellow();
		}else if(event.getType() == Event.Types.CALL){
				entryStateRequest();
			}
		}
	private void handleStateRequest(Event event){
		if(event.getType() == Event.Types.OFF){
			entryStateYellow();
		}else if(event.getType() == Event.Types.TIMER){
				entryStateYellow();
			}
	}

	private void handleStateYellow(Event event){
		if(event.getType() == Event.Types.TIMER){
			if(offReceived){
			entryStateAllRed();
			}else{
			entryStateRedRed();
			}
		}
	}

	private void handleStateRedRed(Event event){
		if(event.getType() == Event.Types.TIMER){
			entryStateRedGreen();
		}else if(event.getType() == Event.Types.OFF){
			entryStateAllRed();
		}
	}

	private void handleStateRedGreen(Event event){
		if(event.getType() == Event.Types.TIMER){
			entryStateAllRed();
		}
	}
	private void entryStateEnd(){
		state = States.end;
		carLight.setLights(false, false, false);
		persLight.setLights(false, false);
		tlView.setImages(carLight, persLight);
	}
	private void entryStateAllRed(){
		state = States.all_red;
		carLight.setLights(true, false, false);
		persLight.setLights(true, false);
		tlView.setImages(carLight, persLight);
		startTimer(2000);
	}

	private void entryStateRedYellow(){
		state = States.red_yellow;
		carLight.setLights(true, true, false);
		persLight.setLights(true, false);
		tlView.setImages(carLight, persLight);
		startTimer(1000);
	}
	private void entryStateYellow(){
		state = States.yellow;
		carLight.setLights(false, true, false);
		persLight.setLights(true, false);
		tlView.setImages(carLight, persLight);
		startTimer(1000);
	}

	private void entryStateRequest(){
		state = States.request;
		startTimer(2000);
	}
	private void entryStateGreen(){
		state = States.green;
		carLight.setLights(false, false, true);
		persLight.setLights(true, false);
		tlView.setImages(carLight, persLight);
	}
	private void entryStateRedRed(){
		state = States.red_red;
		carLight.setLights(true, false, false);
		persLight.setLights(true, false);
		tlView.setImages(carLight, persLight);
		startTimer(1000);
	}

	private void entryStateRedGreen(){
		state = States.red_green;
		carLight.setLights(true, false, false);
		persLight.setLights(false, true);
		tlView.setImages(carLight, persLight);
		startTimer(3000);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// this method is enforced by the ActionListener interface
		// and is called by every button click
		if(e.getSource() == this.callBtt) {
			// this is handled if the call button was clicked
			stateMachine(new Event(Event.Types.CALL));
		} else if(e.getSource() == this.offBtt) {
			offReceived = true;
			stateMachine(new Event(Event.Types.OFF));
			// this is handled if the off button was clicked
		}
	}

	@Override
	public void timerExpired() {
		// this method is enforced by the TimerCallback interface
		// and is called as soon as the timer has expired
		stateMachine(new Event(Event.Types.TIMER));
	}
	
	/**
	 * Method will set the timeSpan of the timer and
	 * starts it. It does not check, if the timer is already running, so 
	 * you maybe add this feature (e.g. like while(trafficLightTimmer.timerIsRunning()) {Thread.sleep(100)})
	 * @param timeSpan Span, the timer is running.
	 */
	public void startTimer(long timeSpan) {
		trafficLightTimer.setWaitTime(timeSpan);
		new Thread(trafficLightTimer).start();
	}
}
