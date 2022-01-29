import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Control a DC Motor attached to a DC Motor Controller
 */
public class DcMotor {
	static DatagramSocket TXsocket;
	static DatagramSocket RXsocket;
	static String lastestRX;

	public enum Direction {
		FORWARD, REVERSE
	}

	Direction direction;
	int portNumber;

	public DcMotor(int portNumber) {
		this(portNumber, Direction.FORWARD);
	}

	public DcMotor(int portNumber, Direction direction) {
		this.portNumber = portNumber;
		setDirection(direction);
	}

	synchronized public void setDirection(Direction direction) {
		this.direction = direction;
	}

	/**
	 * Get the direction
	 * 
	 * @return direction
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * Get port number
	 *
	 * @return portNumber
	 */
	public int getPortNumber() {
		return portNumber;
	}

	/**
	 * Set the current motor power
	 *
	 * @param power from -1.0 to 1.0
	 */

	double currentPower = 0.0;

	synchronized public void setPower(double power) {
		currentPower = power;
		internalSetPower(power);
	}

	protected void internalSetPower(double power) {
		// sendMessage(power + " ");
      // TODO: Properly implement JSON msging to send to unity
	}

	/**
	 * Get the current motor power
	 *
	 * @return scaled from -1.0 to 1.0
	 */
	synchronized public double getPower() {
		return currentPower;
	}

	public static void sendMessage(String message) {
		try {
			TXsocket.send(new DatagramPacket(message.getBytes(), message.length()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getLastReceivedMessage() {
		return lastestRX;
	}

	public static void openRXTXSockets() {
		try {
			TXsocket = new DatagramSocket();
			TXsocket.connect(InetAddress.getByName("127.0.0.1"), 9051);
			String message = "Opened Socket";
			TXsocket.send(new DatagramPacket(message.getBytes(), message.length()));

			RXsocket = new DatagramSocket();
			RXsocket.connect(InetAddress.getByName("127.0.0.1"), 9052);

			Thread RXSocketThread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							byte[] buffer = new byte[1024];
							DatagramPacket response = new DatagramPacket(buffer, buffer.length);
							RXsocket.receive(response);
							lastestRX = new String(buffer, 0, response.getLength());
							System.out.println("RX SOCKET RESPONSE: " + lastestRX);
							TXsocket.send(new DatagramPacket(lastestRX.getBytes(), lastestRX.length()));
							// JSONObject jsonObject = new JSONObject(responseText);
							// DcMotorMaster.motorImpl1.encoderPosition = jsonObject.getDouble("motor1");
							// DcMotorMaster.motorImpl2.encoderPosition = jsonObject.getDouble("motor2");
							// DcMotorMaster.motorImpl3.encoderPosition = jsonObject.getDouble("motor3");
							// DcMotorMaster.motorImpl4.encoderPosition = jsonObject.getDouble("motor4");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});

			RXSocketThread.setPriority(Thread.MAX_PRIORITY);
			RXSocketThread.setName("RX Socket Thread");
			RXSocketThread.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//   /**
//    * Is the motor busy?
//    *
//    * @return true if the motor is busy
//    */
//   public boolean isBusy() {
//     return controller.isBusy(portNumber);
//   }

//   @Override
//   public synchronized void setZeroPowerBehavior(ZeroPowerBehavior zeroPowerBehavior) {
//     controller.setMotorZeroPowerBehavior(portNumber, zeroPowerBehavior);
//   }

//   @Override
//   public synchronized ZeroPowerBehavior getZeroPowerBehavior() {
//     return controller.getMotorZeroPowerBehavior(portNumber);
//   }

//   /**
//    * Allow motor to float
//    */
//   @Deprecated
//   public synchronized void setPowerFloat() {
//     setZeroPowerBehavior(ZeroPowerBehavior.FLOAT);
//     setPower(0.0);
//   }

//   /**
//    * Is motor power set to float?
//    *
//    * @return true of motor is set to float
//    */
//   public synchronized boolean getPowerFloat() {
//     return getZeroPowerBehavior() == ZeroPowerBehavior.FLOAT && getPower() == 0.0;
//   }

//   /**
//    * Set the motor target position, using an integer. If this motor has been set to REVERSE,
//    * the passed-in "position" value will be multiplied by -1.
//    *
//    *  @param position range from Integer.MIN_VALUE to Integer.MAX_VALUE
//    *
//    */
//   synchronized public void setTargetPosition(int position) {
//     position = adjustPosition(position);
//     internalSetTargetPosition(position);
//   }

//   protected void internalSetTargetPosition(int position) {
//     controller.setMotorTargetPosition(portNumber, position);
//   }

//   /**
//    * Get the current motor target position. If this motor has been set to REVERSE, the returned
//    * "position" will be multiplied by -1.
//    *
//    * @return integer, unscaled
//    */
//   synchronized public int getTargetPosition() {
//     int position = controller.getMotorTargetPosition(portNumber);
//     return adjustPosition(position);
//   }

//   /**
//    * Get the current encoder value, accommodating the configured directionality of the motor.
//    *
//    * @return double indicating current position
//    */
//   synchronized public int getCurrentPosition() {
//     int position = controller.getMotorCurrentPosition(portNumber);
//     return adjustPosition(position);
//   }

//   protected int adjustPosition(int position) {
//     if (getOperationalDirection() == Direction.REVERSE) position = -position;
//     return position;
//   }

//   protected double adjustPower(double power) {
//     if (getOperationalDirection() == Direction.REVERSE) power = -power;
//     return power;
//   }

//   protected Direction getOperationalDirection() {
//     return motorType.getOrientation() == Rotation.CCW ? direction.inverted() : direction;
//   }

//   /**
//    * Set the current mode
//    *
//    * @param mode run mode
//    */
//   synchronized public void setMode(RunMode mode) {
//     mode = mode.migrate();
//     internalSetMode(mode);
//   }

//   protected void internalSetMode(RunMode mode) {
//     controller.setMotorMode(portNumber, mode);
//   }

//   /**
//    * Get the current mode
//    *
//    * @return run mode
//    */
//   public RunMode getMode() {
//     return controller.getMotorMode(portNumber);
//   }
}