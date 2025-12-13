package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

// Intake- Rev; Position 1,2,3- GoBuilda Servo Cont.; Indexer- GoBuilda Servo; Shooter- 6000 motor
@Autonomous
public class BasicMoveAuto extends OpMode implements MotorRampWithTriggers {

    // 1. Define the states for our state machine
    enum LaunchStateEnum {
        IDLE,
        SPIN_UP,
        LAUNCH,
        LAUNCHING,
    }

    // Set the initial state
    private ShooterWithIntake.LaunchStateEnum launchState = ShooterWithIntake.LaunchStateEnum.IDLE;

    // Define constants for LauncherMotor power and ramp rate
    static final double RAMP_POWER_INCREMENT = 0.02;  // Power increment per cycle (was INCREMENT)
    static final long RAMP_CYCLE_MS = 50;           // Time in milliseconds per ramp cycle (was CYCLE_MS)
    static final double MAX_SPEED = 1.0;              // Maximum LauncherMotor speed (was MAX_FWD)

    // Declare OpMode members
    private DcMotorEx launcherMotor = null;
    public  double LAUNCHER_TARGET_VELOCITY = 1500;
    public  double LAUNCHER_MIN_VELOCITY = 1200;
    private double STOP_VELOCITY = 0;
    private double FEED_TIME_SECONDS = 0.7;
    private double LIFT_TIME_SECONDS = 1.4;

    private double FEED_POWER = -0.8;
    private double INTAKE_POSITION = 0.6;
    private double LAUNCH_POSITION = 0.2;
    private DcMotor feederMotor = null;
    private Servo liftServo = null;

    private double currentMotorPower = 0.0;
    // lift servo
    private DcMotor intakeMotor = null;
    private boolean IntakeOn = false;
    private DcMotor leftFrontDrive = null;
    private DcMotor rightFrontDrive = null;
    private DcMotor leftBackDrive = null;
    private DcMotor rightBackDrive = null;
    enum IntakeState {
        IntakeOff,
        Intake1,
        Intake2,
        Intake3
    }
    boolean Now = false;
    // Set the initial state
    private ShooterWithIntake.IntakeState currentIntakeState = ShooterWithIntake.IntakeState.IntakeOff;

    // Create a timer to manage the ramp rate
    private  ElapsedTime feederTimer = new ElapsedTime();
    private double timeAtStart;


    @Override
    public void init() {

        // Initialize the hardware variables.
        // IMPORTANT: Make sure the LauncherMotor name "shooter_drive" matches your robot's configuration.
        launcherMotor = hardwareMap.get(DcMotorEx.class, "shooter_drive");
        // Optional: If the LauncherMotor runs backwards, uncomment the next line
        // LauncherMotor.setDirection(DcMotor.Direction.REVERSE);

        telemetry.addData("Status", "Initialized");
        telemetry.addData(">", "Press Start to begin");

        telemetry.update();
        // telemetry.update();
        //telemetry.speak("Six seven");

        // Map the motors to the names in the robot's configuration file
        leftFrontDrive = hardwareMap.get(DcMotor.class, "left_front_drive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "right_front_drive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "left_back_drive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "right_back_drive");
        intakeMotor = hardwareMap.get(DcMotor.class, "intake_motor");
        feederMotor = hardwareMap.get(DcMotor.class, "feeder_motor");

        liftServo = hardwareMap.get(Servo.class, "lift_servo");
    }

    @Override
    public void start() {
        timeAtStart = getRuntime();
        rightFrontDrive.setPower(1);
        leftFrontDrive.setPower(1);
        rightBackDrive.setPower(1);
        leftBackDrive.setPower(1);
    }

    @Override
    public void loop() {
        if (getRuntime() - timeAtStart > 2) {
            rightFrontDrive.setPower(0);
            leftFrontDrive.setPower(0);
            rightBackDrive.setPower(0);
            leftBackDrive.setPower(0);
            requestOpModeStop();
        }
    }

    private enum RampState {
        STEADY,
        RAMPING_UP,
        RAMPING_DOWN
    }

    // Set the initial state
    private MotorRampWithTriggers_StateMachine.RampState currentState = MotorRampWithTriggers_StateMachine.RampState.STEADY;

    // Define constants for motor power and ramp rate
    static final double RAMP_POWER_INCREMENT = 0.02;  // Power increment per cycle (was INCREMENT)
    static final long RAMP_CYCLE_MS = 50;           // Time in milliseconds per ramp cycle (was CYCLE_MS)
    static final double MAX_SPEED = 1.0;              // Maximum motor speed (was MAX_FWD)

    // Declare OpMode members
    private DcMotor motor = null;
    private double currentMotorPower = 0.0;


    // Create a timer to manage the ramp rate
    private final ElapsedTime rampTimer = new ElapsedTime();
    private final ElapsedTime moveTimer = new ElapsedTime();

 l
    @Override
    public void runOpMode() {

        // Initialize the hardware variables.
        // IMPORTANT: Make sure the motor name "shooter_drive" matches your robot's configuration.
        motor = hardwareMap.get(DcMotor.class, "shooter_drive");

        // Optional: If the motor runs backwards, uncomment the next line
        // motor.setDirection(DcMotor.Direction.REVERSE);

        telemetry.addData("Status", "Initialized");
        telemetry.addData(">", "Press Start to begin");

        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // Reset the timer once the OpMode starts
        rampTimer.reset();
        moveTimer.reset();

        // The main loop runs until the driver presses STOP
        while (opModeIsActive()) {

            // 2. Determine the next state based on trigger input
            updateStateFromInput();



            /*// 3. Execute logic based on the current state
            switch (currentState) {
                case RAMPING_UP:
                    // If enough time has passed, increment power
                    if (rampTimer.milliseconds() > RAMP_CYCLE_MS) {
                        currentMotorPower += RAMP_POWER_INCREMENT;
                        // Clamp the power to the maximum speed
                        if (currentMotorPower > MAX_SPEED) {
                            currentMotorPower = MAX_SPEED;
                        }
                        rampTimer.reset(); // Reset the timer for the next increment
                    }
                    break;

                case RAMPING_DOWN:
                    // If enough time has passed, decrement power
                    if (rampTimer.milliseconds() > RAMP_CYCLE_MS) {
                        currentMotorPower -= RAMP_POWER_INCREMENT;
                        // Clamp the power to zero (motor only runs forward in this design)
                        if (currentMotorPower < 0) {
                            currentMotorPower = 0;
                        }
                        rampTimer.reset(); // Reset the timer for the next decrement
                    }
                    break;

                case STEADY:
                    // In the STEADY state, the motor power does not change automatically.
                    // It holds its last value. We can still reset the timer to be ready.
                    rampTimer.reset();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + currentState);
            }

            // 4. Set the motor power

            motor.setPower(currentMotorPower);*/

            // 5. Provide telemetry for debugging
            telemetry.addData("State", currentState.toString());
            telemetry.addData("Motor Power", "%.2f", currentMotorPower);
            telemetry.addData("Right Trigger", "%.2f", gamepad1.right_trigger);
            telemetry.addData("Left Trigger", "%.2f", gamepad1.left_trigger);
            telemetry.update();
        }

        // Stop the motor when the OpMode ends
        motor.setPower(0);
    }

    private boolean opModeIsActive() {
        return false;
    }

    private void waitForStart() {

    }

    /**
     * Reads gamepad input and updates the currentState variable.
     * This method centralizes the logic for state transitions.
     */
    private void updateStateFromInput() {
        // Read trigger values using a threshold to avoid jitter from sensitive triggers
        boolean rightTriggerPressed = gamepad1.right_trigger > 0.1;
        boolean leftTriggerPressed = gamepad1.left_trigger > 0.1;

        // Safety check: If both triggers are pressed, force a ramp down.
        if (rightTriggerPressed && leftTriggerPressed) {
            currentState = MotorRampWithTriggers_StateMachine.RampState.RAMPING_DOWN;
        }
        // If only the right trigger is pressed, ramp up.
        else if (rightTriggerPressed) {
            currentState = MotorRampWithTriggers_StateMachine.RampState.RAMPING_UP;
        }
        // If only the left trigger is pressed, ramp down.
        else if (leftTriggerPressed) {
            currentState = MotorRampWithTriggers_StateMachine.RampState.RAMPING_DOWN;
        }
        // If no triggers are pressed, hold a steady speed.
        else {
            currentState = MotorRampWithTriggers_StateMachine.RampState.STEADY;


        }
    }
}